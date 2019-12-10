package de.tudresden.inf.st.bigraphs.core.impl;

import com.google.common.collect.HashBiMap;
import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.DiscreteIon;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Composable bigraph implementation of {@link BigraphComposite} for <b>pure bigraphs</b>.
 * <p>
 * Equips the bigraph with some categorical operators to compute the product of two bigraphs.
 * Operators can only be used by bigraphs of the same type and signature, except with elementary ones.
 *
 * @param <S> type of the signature.
 * @author Dominik Grzelak
 */
public class PureBigraphComposite<S extends Signature> extends BigraphCompositeSupport<S> {//BigraphDelegator<S> implements BigraphComposite<S> {

    private MutableBuilder<S> builder;

    /**
     * Constructor creates a composable bigraph from the given bigraph.
     * The bigraph is then equipped with some categorical operators, such as composition and tensor product, to compute
     * the product of two bigraphs.
     * <p>
     * This "operational wrapper" can only be used with and by instances with the superclass/superinterface of {@link PureBigraph},
     * {@link ElementaryBigraph}, or this wrapper class itself.
     * <p>
     * The type of the argument is still {@link Bigraph} because {@link PureBigraph}s can be composed with another classes to,
     * for example, elementary ones. So we don't restrict the type here to not force casting or checking upon the developer.
     *
     * @param bigraph the bigraph which is being equipped with categorical operators
     */
    public PureBigraphComposite(Bigraph<S> bigraph) {
        super(bigraph);
        assert bigraph instanceof PureBigraphComposite || bigraph instanceof PureBigraph || bigraph instanceof ElementaryBigraph;
        // this is safe: S is inferred from the bigraph to where S is the same type as the builder's type S (they will have the same type thus)
        this.builder = PureBigraphBuilder.newMutableBuilder(getBigraphDelegate().getSignature());
    }

    /**
     * Function that makes the nodes disjunct in terms of there names. This is needed for composition.
     */
    @Override
    public Bigraph<S> getOuterBigraph() {
        return getBigraphDelegate();
    }

    @Override
    public BigraphComposite<S> juxtapose(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        Bigraph<S> g = getBigraphDelegate();
        //rule: first G then F when rewriting names, ordinals
        assertSignaturesAreSame(g, f);
        assertInterfaceCompatibleForJuxtaposition(g, f);

        Supplier<Integer> rewriteRootSupplier = createNameSupplier();
        Supplier<String> rewriteNameSupplier = createNameSupplier("v");
        Supplier<Integer> rewriteSiteSupplier = createNameSupplier();

        HashBiMap<String, BigraphEntity.NodeEntity> V = HashBiMap.create();
        Map<String, BigraphEntity.NodeEntity> V_G = g.getNodes().stream().collect(Collectors.toMap(s -> rewriteNameSupplier.get(), Function.identity()));
        Map<String, BigraphEntity.NodeEntity> V_F = f.getNodes().stream().collect(Collectors.toMap(s -> rewriteNameSupplier.get(), Function.identity()));
        V.putAll(V_G);
        V.putAll(V_F);

        HashMap<Integer, BigraphEntity.RootEntity> myRoots = new LinkedHashMap<>();
        HashBiMap<Integer, BigraphEntity.RootEntity> R = HashBiMap.create();
        R.putAll(g.getRoots().stream().collect(Collectors.toMap(s -> rewriteRootSupplier.get(), Function.identity())));
        R.putAll(f.getRoots().stream().collect(Collectors.toMap(s -> rewriteRootSupplier.get(), Function.identity())));
        for (Map.Entry<Integer, BigraphEntity.RootEntity> each : R.entrySet()) {
            myRoots.put(each.getKey(), (BigraphEntity.RootEntity) builder.createNewRoot(each.getKey()));
        }

        HashBiMap<Integer, BigraphEntity.SiteEntity> S = HashBiMap.create();
        S.putAll(g.getSites().stream().collect(Collectors.toMap(s -> rewriteSiteSupplier.get(), Function.identity())));
        S.putAll(f.getSites().stream().collect(Collectors.toMap(s -> rewriteSiteSupplier.get(), Function.identity())));
        HashMap<Integer, BigraphEntity.SiteEntity> mySites = new LinkedHashMap<>();
        for (Map.Entry<Integer, BigraphEntity.SiteEntity> each : S.entrySet()) {
            mySites.put(each.getKey(), (BigraphEntity.SiteEntity) builder.createNewSite(each.getKey()));
        }

        HashMap<String, BigraphEntity.NodeEntity> myNodes = new LinkedHashMap<>();
        //for nodes first
        for (Map.Entry<String, BigraphEntity.NodeEntity> each : V.entrySet()) {
            BigraphEntity.NodeEntity newNode = myNodes.get(each.getKey());
            if (Objects.isNull(newNode)) {
                newNode = (BigraphEntity.NodeEntity) builder.createNewNode(each.getValue().getControl(), each.getKey());
                myNodes.put(each.getKey(), newNode);
            }

            BigraphEntity parent = null;
            if (V_F.containsKey(each.getKey())) {
                parent = f.getParent(each.getValue());
            } else if (V_G.containsKey(each.getKey())) {
                parent = g.getParent(each.getValue());
            }
            assert parent != null;

            BigraphEntity theParentToSet = null;
            if (BigraphEntityType.isRoot(parent)) {
                Integer integer = R.inverse().get(parent);
                theParentToSet = myRoots.get(integer);
            } else {
                String s = V.inverse().get(parent);
                theParentToSet = myNodes.get(s);
                if (Objects.isNull(theParentToSet)) {
                    theParentToSet = builder.createNewNode(parent.getControl(), s);
                    myNodes.put(each.getKey(), (BigraphEntity.NodeEntity) theParentToSet);
                }
            }
            setParentOfNode(newNode, theParentToSet);
        }
        for (Map.Entry<Integer, BigraphEntity.SiteEntity> each : S.entrySet()) {
            BigraphEntity.SiteEntity newSite = mySites.get(each.getKey());
            if (Objects.isNull(newSite)) {
                newSite = (BigraphEntity.SiteEntity) builder.createNewSite(each.getKey());
                mySites.put(each.getKey(), newSite);
            }

            BigraphEntity parent = f.getParent(each.getValue());
            if (Objects.isNull(parent)) {
                parent = g.getParent(each.getValue());
            }
            assert parent != null;
            BigraphEntity theParentToSet = null;
            if (BigraphEntityType.isRoot(parent)) {
                Integer integer = R.inverse().get(parent);
                theParentToSet = myRoots.get(integer);
            } else {
                String s = V.inverse().get(parent);
                theParentToSet = myNodes.get(s);
            }
            setParentOfNode(newSite, theParentToSet);
        }

        //create all inner names, outer names and edges
        HashMap<String, BigraphEntity.Edge> myEdges = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.InnerName> myInnerNames = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.OuterName> myOuterNames = new LinkedHashMap<>();
        Supplier<String> rewriteEdgeNameSupplier = createNameSupplier("e");
        HashBiMap<String, BigraphEntity.Edge> E = HashBiMap.create();
        List<BigraphEntity.InnerName> I = new ArrayList<>();
        I.addAll(g.getInnerNames());
        I.addAll(f.getInnerNames());
        HashBiMap<String, BigraphEntity.OuterName> O = HashBiMap.create();

        Map<String, BigraphEntity.Edge> E_G = g.getEdges().stream().collect(Collectors.toMap(s -> rewriteEdgeNameSupplier.get(), Function.identity()));
        Map<String, BigraphEntity.Edge> E_F = f.getEdges().stream().collect(Collectors.toMap(s -> rewriteEdgeNameSupplier.get(), Function.identity()));
        E.putAll(E_G);
        E.putAll(E_F);

        O.putAll(g.getOuterNames().stream().collect(Collectors.toMap(BigraphEntity.OuterName::getName, Function.identity())));
        O.putAll(f.getOuterNames().stream().collect(Collectors.toMap(BigraphEntity.OuterName::getName, Function.identity())));

        for (Map.Entry<String, BigraphEntity.NodeEntity> each : V.entrySet()) {
            if (each.getValue().getControl().getArity().compareTo(FiniteOrdinal.ofInteger(0)) == 0) continue;

            Collection<BigraphEntity.Port> ports = f.getPorts(each.getValue());
            if (Objects.isNull(ports)) {
                ports = g.getPorts(each.getValue());
            }

            String nodeName = V.inverse().get(each.getValue());
            BigraphEntity.NodeEntity newNode = myNodes.get(nodeName);
            assert Objects.nonNull(newNode);
            int portIx = 0;
            for (BigraphEntity.Port eachPort : ports) {

                BigraphEntity link = g.getLinkOfPoint(eachPort);
                if (Objects.isNull(link)) {
                    link = f.getLinkOfPoint(eachPort);
                }

                assert BigraphEntityType.isLinkType(link);

                BigraphEntity newLink = null;
                if (BigraphEntityType.isEdge(link)) {
                    String edgeName = E.inverse().get(link);
                    newLink = myEdges.get(edgeName);
                    if (Objects.isNull(newLink)) {
                        newLink = builder.createNewEdge(edgeName);
                        myEdges.put(edgeName, (BigraphEntity.Edge) newLink);
                    }
                } else if (BigraphEntityType.isOuterName(link)) {
                    String outerNameValue = O.inverse().get(link);
                    newLink = myOuterNames.get(outerNameValue);
                    if (Objects.isNull(newLink)) {
                        newLink = builder.createNewOuterName(outerNameValue);
                        myOuterNames.put(outerNameValue, (BigraphEntity.OuterName) newLink);
                    }
                }

                BigraphEntity.Port newPortWithIndex = (BigraphEntity.Port) builder.createNewPortWithIndex(portIx++);
                //add port to node
                EStructuralFeature portsRef = newNode.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
                EList<EObject> portsList = (EList<EObject>) newNode.getInstance().eGet(portsRef);
                portsList.add(newPortWithIndex.getInstance());
                //connect node to link
                EStructuralFeature lnkRef = newPortWithIndex.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                newPortWithIndex.getInstance().eSet(lnkRef, newLink.getInstance());
            }
        }
        //now the inner names
        for (BigraphEntity.InnerName each : I) {
            BigraphEntity.InnerName newInnerName = myInnerNames.get(each.getName());
            if (Objects.isNull(newInnerName)) {
                newInnerName = (BigraphEntity.InnerName) builder.createNewInnerName(each.getName());
                myInnerNames.put(each.getName(), newInnerName);
            }

            BigraphEntity link = g.getLinkOfPoint(each);
            if (Objects.isNull(link)) {
                link = f.getLinkOfPoint(each);
            }
            if (Objects.isNull(link)) continue;

            BigraphEntity newLink = null;
            if (BigraphEntityType.isEdge(link)) {
                String edgeName = E.inverse().get(link);
                newLink = myEdges.get(edgeName);
                if (Objects.isNull(newLink)) {
                    newLink = builder.createNewEdge(edgeName);
                    myEdges.put(edgeName, (BigraphEntity.Edge) newLink);
                }
            } else if (BigraphEntityType.isOuterName(link)) {
                String outerNameValue = O.inverse().get(link);
                newLink = myOuterNames.get(outerNameValue);
                if (Objects.isNull(newLink)) {
                    newLink = builder.createNewOuterName(outerNameValue);
                    myOuterNames.put(outerNameValue, (BigraphEntity.OuterName) newLink);
                }
            }

            //connect the inner name directly to this link
            EStructuralFeature lnkRef = newInnerName.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
            newInnerName.getInstance().eSet(lnkRef, newLink.getInstance());
        }

        PureBigraphBuilder.InstanceParameter meta = builder.new InstanceParameter(
                getModelPackage(),
                getSignature(),
                myRoots,
                mySites,
                myNodes,
                myInnerNames, myOuterNames, myEdges);
        builder.reset();
        Bigraph<S> bigraph = (Bigraph<S>) new PureBigraph(meta);//TODO rework necessary -> unsure which bigraph should be employed here

        return new PureBigraphComposite<>(bigraph);
    }

    @Override
    public BigraphComposite<S> parallelProductOf(Bigraph<S>... bigraphs) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        if (bigraphs.length == 0) return this;
        BigraphComposite<S> next = parallelProduct(bigraphs[0]);
        for (int i = 1, n = bigraphs.length; i < n; i++) {
            next = next.parallelProduct(bigraphs[i]);
        }
        return next;
    }

    @Override
    public BigraphComposite<S> juxtpositionOf(Bigraph<S>... bigraphs) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        if (bigraphs.length == 0) return this;
        BigraphComposite<S> next = juxtapose(bigraphs[0]);
        for (int i = 1, n = bigraphs.length; i < n; i++) {
            next = next.juxtapose(bigraphs[i]);
        }
        return next;
    }

    @Override
    public BigraphComposite<S> parallelProduct(BigraphComposite<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        return parallelProduct(f.getOuterBigraph());
    }

    @Override
    public BigraphComposite<S> parallelProduct(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        Bigraph<S> g = getBigraphDelegate();
        assertSignaturesAreSame(g, f);

        Supplier<Integer> rewriteRootSupplier = createNameSupplier();
        Supplier<String> rewriteNameSupplier = createNameSupplier("v");
        Supplier<Integer> rewriteSiteSupplier = createNameSupplier();

        HashBiMap<String, BigraphEntity.NodeEntity> V = HashBiMap.create();
        Map<String, BigraphEntity.NodeEntity> V_G = g.getNodes().stream().collect(Collectors.toMap(s -> rewriteNameSupplier.get(), Function.identity()));
        Map<String, BigraphEntity.NodeEntity> V_F = f.getNodes().stream().collect(Collectors.toMap(s -> rewriteNameSupplier.get(), Function.identity()));
        V.putAll(V_G);
        V.putAll(V_F);

        HashMap<Integer, BigraphEntity.RootEntity> myRoots = new LinkedHashMap<>();
        HashBiMap<Integer, BigraphEntity.RootEntity> R = HashBiMap.create();
        R.putAll(g.getRoots().stream().collect(Collectors.toMap(s -> rewriteRootSupplier.get(), Function.identity())));
        R.putAll(f.getRoots().stream().collect(Collectors.toMap(s -> rewriteRootSupplier.get(), Function.identity())));
        for (Map.Entry<Integer, BigraphEntity.RootEntity> each : R.entrySet()) {
            myRoots.put(each.getKey(), (BigraphEntity.RootEntity) builder.createNewRoot(each.getKey()));
        }

        HashBiMap<Integer, BigraphEntity.SiteEntity> S = HashBiMap.create();
        S.putAll(g.getSites().stream().collect(Collectors.toMap(s -> rewriteSiteSupplier.get(), Function.identity())));
        S.putAll(f.getSites().stream().collect(Collectors.toMap(s -> rewriteSiteSupplier.get(), Function.identity())));
        HashMap<Integer, BigraphEntity.SiteEntity> mySites = new LinkedHashMap<>();
        for (Map.Entry<Integer, BigraphEntity.SiteEntity> each : S.entrySet()) {
            mySites.put(each.getKey(), (BigraphEntity.SiteEntity) builder.createNewSite(each.getKey()));
        }

        HashMap<String, BigraphEntity.NodeEntity> myNodes = new LinkedHashMap<>();
        //for nodes first
        for (Map.Entry<String, BigraphEntity.NodeEntity> each : V.entrySet()) {
            BigraphEntity.NodeEntity newNode = myNodes.get(each.getKey());
            if (Objects.isNull(newNode)) {
                newNode = (BigraphEntity.NodeEntity) builder.createNewNode(each.getValue().getControl(), each.getKey());
                myNodes.put(each.getKey(), newNode);
            }

            BigraphEntity parent = null;
            if (V_F.containsKey(each.getKey())) {
                parent = f.getParent(each.getValue());
            } else if (V_G.containsKey(each.getKey())) {
                parent = g.getParent(each.getValue());
            }
            assert parent != null;

            BigraphEntity theParentToSet = null;
            if (BigraphEntityType.isRoot(parent)) {
                Integer integer = R.inverse().get(parent);
                theParentToSet = myRoots.get(integer);
            } else {
                String s = V.inverse().get(parent);
                theParentToSet = myNodes.get(s);
                if (Objects.isNull(theParentToSet)) {
                    theParentToSet = builder.createNewNode(parent.getControl(), s);
                    myNodes.put(s, (BigraphEntity.NodeEntity) theParentToSet);
                }
            }
            setParentOfNode(newNode, theParentToSet);
        }
        for (Map.Entry<Integer, BigraphEntity.SiteEntity> each : S.entrySet()) {
            BigraphEntity.SiteEntity newSite = mySites.get(each.getKey());
            if (Objects.isNull(newSite)) {
                newSite = (BigraphEntity.SiteEntity) builder.createNewSite(each.getKey());
                mySites.put(each.getKey(), newSite);
            }

            BigraphEntity parent = f.getParent(each.getValue()); //S.get(each.getKey())); //each.getValue());
            if (Objects.isNull(parent)) {
                parent = g.getParent(each.getValue()); //S.get(each.getKey()));
            }
            assert parent != null;
            BigraphEntity theParentToSet = null;
            if (BigraphEntityType.isRoot(parent)) {
                Integer integer = R.inverse().get(parent);
                theParentToSet = myRoots.get(integer);
            } else {
                String s = V.inverse().get(parent);
                theParentToSet = myNodes.get(s);
            }
            setParentOfNode(newSite, theParentToSet);
        }

        //create all inner names, outer names and edges
        HashMap<String, BigraphEntity.Edge> myEdges = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.InnerName> myInnerNames = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.OuterName> myOuterNames = new LinkedHashMap<>();
        Supplier<String> rewriteEdgeNameSupplier = createNameSupplier("e");
        HashBiMap<String, BigraphEntity.Edge> E = HashBiMap.create();

        List<BigraphEntity.InnerName> I = new ArrayList<>();
        I.addAll(g.getInnerNames());
        I.addAll(f.getInnerNames());
        List<BigraphEntity.OuterName> O = new ArrayList<>();
        O.addAll(g.getOuterNames());
        O.addAll(f.getOuterNames());
        for (BigraphEntity.OuterName each : O) {
            String outerNameValue = each.getName();//O.inverse().get(link);
            BigraphEntity.OuterName newLink = myOuterNames.get(outerNameValue);
            if (Objects.isNull(newLink)) {
                newLink = (BigraphEntity.OuterName) builder.createNewOuterName(outerNameValue);
                myOuterNames.put(outerNameValue, newLink);
            }
        }

        Map<String, Long> innerNamegroupCounter = I.stream().collect(Collectors.groupingBy(e -> e.getName(), Collectors.counting()));
        Map<String, String> collectGroup = new ConcurrentHashMap<>();

        for (BigraphEntity.InnerName eachInner : I) {
            Collection<BigraphEntity.InnerName> siblingsOfInnerName = g.getSiblingsOfInnerName(eachInner);
            if (siblingsOfInnerName.size() == 0) {
                siblingsOfInnerName = f.getSiblingsOfInnerName(eachInner);
            }
            if (siblingsOfInnerName.size() == 0) siblingsOfInnerName = new ArrayList<>();
            siblingsOfInnerName.add(eachInner);
            String newEdgeName = null; //rewriteEdgeNameSupplier.get();
            for (BigraphEntity.InnerName second : siblingsOfInnerName) {
                if (!collectGroup.containsKey(second.getName())) {
                    if (Objects.isNull(newEdgeName)) {
                        newEdgeName = rewriteEdgeNameSupplier.get();
                    }
                    collectGroup.put(second.getName(), newEdgeName);
                }
            }
        }


        Map<String, BigraphEntity.Edge> E_G = g.getEdges().stream().collect(Collectors.toMap(s -> rewriteEdgeNameSupplier.get(), Function.identity()));
        Map<String, BigraphEntity.Edge> E_F = f.getEdges().stream().collect(Collectors.toMap(s -> rewriteEdgeNameSupplier.get(), Function.identity()));
        E.putAll(E_G);
        E.putAll(E_F);

        for (Map.Entry<String, BigraphEntity.NodeEntity> each : V.entrySet()) {
            if (each.getValue().getControl().getArity().compareTo(FiniteOrdinal.ofInteger(0)) == 0) continue;

            Collection<BigraphEntity.Port> ports = f.getPorts(each.getValue());
            if (Objects.isNull(ports)) {
                ports = g.getPorts(each.getValue());
            }

            String nodeName = V.inverse().get(each.getValue());
            BigraphEntity.NodeEntity newNode = myNodes.get(nodeName);
            assert Objects.nonNull(newNode);
            int portIx = 0;
            for (BigraphEntity.Port eachPort : ports) {

                BigraphEntity link = g.getLinkOfPoint(eachPort);
                if (Objects.isNull(link)) {
                    link = f.getLinkOfPoint(eachPort);
                }

                assert BigraphEntityType.isLinkType(link);

                BigraphEntity newLink = null;

                if (BigraphEntityType.isEdge(link)) {
                    String edgeName = E.inverse().get(link);

                    Collection<BigraphEntity> pointsFromLink = g.getPointsFromLink(link);
                    if (pointsFromLink.size() == 0) {
                        pointsFromLink = f.getPointsFromLink(link);
                    }

                    for (BigraphEntity eachInner : pointsFromLink) {
                        if (BigraphEntityType.isInnerName(eachInner)) {
                            String innerName = ((BigraphEntity.InnerName) eachInner).getName();
                            if (collectGroup.containsKey(innerName) && innerNamegroupCounter.get(innerName) > 1) {
                                edgeName = collectGroup.get(innerName);
                                //all inner siblings are merged under one edge, thus, we can break here
                                break;
                            }
                        }
                    }
                    //zeigt edge zu einem inner name der mehrmals vorkommt?

                    newLink = myEdges.get(edgeName);
                    if (Objects.isNull(newLink)) {
                        newLink = builder.createNewEdge(edgeName);
                        myEdges.put(edgeName, (BigraphEntity.Edge) newLink);
                    }
                } else if (BigraphEntityType.isOuterName(link)) {
                    String outerNameValue = ((BigraphEntity.OuterName) link).getName();//O.inverse().get(link);
                    newLink = myOuterNames.get(outerNameValue);
                    if (Objects.isNull(newLink)) {
                        newLink = builder.createNewOuterName(outerNameValue);
                        myOuterNames.put(outerNameValue, (BigraphEntity.OuterName) newLink);
                    }
                }
                BigraphEntity.Port newPortWithIndex = (BigraphEntity.Port) builder.createNewPortWithIndex(portIx++);
                //add port to node
                EStructuralFeature portsRef = newNode.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
                EList<EObject> portsList = (EList<EObject>) newNode.getInstance().eGet(portsRef);
                portsList.add(newPortWithIndex.getInstance());
                //connect node to link
                EStructuralFeature lnkRef = newPortWithIndex.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                newPortWithIndex.getInstance().eSet(lnkRef, newLink.getInstance());
            }
        }
        //now the inner names
        for (BigraphEntity.InnerName each : I) {
            BigraphEntity.InnerName newInnerName = myInnerNames.get(each.getName());
            if (Objects.isNull(newInnerName)) {
                newInnerName = (BigraphEntity.InnerName) builder.createNewInnerName(each.getName());
                myInnerNames.put(each.getName(), newInnerName);
            }
//
            BigraphEntity link = g.getLinkOfPoint(each);
            if (Objects.isNull(link)) {
                link = f.getLinkOfPoint(each);
            }
            if (Objects.isNull(link)) continue;
//
            BigraphEntity newLink = null;
            if (BigraphEntityType.isEdge(link)) {
                String edgeName = E.inverse().get(link);
                if (collectGroup.containsKey(each.getName()) && innerNamegroupCounter.get(each.getName()) > 1) {
                    edgeName = collectGroup.get(each.getName());
                }
                newLink = myEdges.get(edgeName);
                if (Objects.isNull(newLink)) {
                    newLink = builder.createNewEdge(edgeName);
                    myEdges.put(edgeName, (BigraphEntity.Edge) newLink);
                }
            } else if (BigraphEntityType.isOuterName(link)) {
                String outerNameValue = ((BigraphEntity.OuterName) link).getName(); //O.inverse().get(link);
                newLink = myOuterNames.get(outerNameValue);
                if (Objects.isNull(newLink)) {
                    newLink = builder.createNewOuterName(outerNameValue);
                    myOuterNames.put(outerNameValue, (BigraphEntity.OuterName) newLink);
                    builder.connectInnerToOuter(newInnerName, (BigraphEntity.OuterName) newLink);
                }
            }

            //connect the inner name directly to this link
            EStructuralFeature lnkRef = newInnerName.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
            newInnerName.getInstance().eSet(lnkRef, newLink.getInstance());
        }

        PureBigraphBuilder.InstanceParameter meta = builder.new InstanceParameter(
                getModelPackage(),
                getSignature(),
                myRoots,
                mySites,
                myNodes,
                myInnerNames, myOuterNames, myEdges);
        builder.reset();
        Bigraph<S> bigraph = (Bigraph<S>) new PureBigraph(meta);
//        bigraph.getPointsFromLink(new ArrayList<>(bigraph.getOuterNames()).get(1));
        return new PureBigraphComposite<>(bigraph);
    }

    @Override
    public BigraphComposite<S> compose(BigraphComposite<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        return this.compose(f.getOuterBigraph());
    }

    @Override
    public BigraphComposite<S> juxtapose(BigraphComposite<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        return this.juxtapose(f.getOuterBigraph());
    }

    @Override
    public BigraphComposite<S> compose(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        Bigraph<S> g = getBigraphDelegate();
        assertSignaturesAreSame(g, f);
        // "disjoint support" of bigraphs is not really important (relevant) here as we are re-creating everything anyway
        // assertBigraphsAreNotSame();
        assertInterfaceCompatibleForCompose(g, f);

        Supplier<String> rewriteNameSupplier = createNameSupplier("v");

        //rewrite names...to make them disjoint. This will leave the original nodes untouched
        Map<String, BigraphEntity.NodeEntity> V_G = g.getNodes().stream().collect(Collectors.toMap(s -> rewriteNameSupplier.get(), Function.identity(),
                (v1, v2) -> v1,
                LinkedHashMap::new));

        //rewrite names...to make them disjoint. This will leave the original nodes untouched
        Map<String, BigraphEntity.NodeEntity> V_F = f.getNodes().stream().collect(Collectors.toMap(s -> rewriteNameSupplier.get(), Function.identity(),
                (v1, v2) -> v1,
                LinkedHashMap::new));

        // aggregated node set of the new bigraph to be composed
        HashBiMap<String, BigraphEntity.NodeEntity> V = HashBiMap.create();
        V.putAll(V_G);
        V.putAll(V_F);

        // all sites of f
        Collection<BigraphEntity.SiteEntity> k = new LinkedHashSet<>(f.getSites());

        //collect all sites and roots of g and f, respectively
        Collection<FiniteOrdinal> mOrdinals = new LinkedHashSet<>(g.getInnerFace().getKey());
        mOrdinals.addAll(f.getOuterFace().getKey());

        // nodes are disjoint now - see above rewriteNodeNames
        Set<BigraphEntity> W_set = new LinkedHashSet<>();
        W_set.addAll(V.values()); //.stream().sorted(Comparator.comparing(x -> ((BigraphEntity.NodeEntity) x).getName())).collect(Collectors.toList()));
        W_set.addAll(k);
        Set<BigraphEntity> kVF = new LinkedHashSet<>();
        kVF.addAll(V_F.values());
        kVF.addAll(k);


        //insertion order for the nodes is here important because we've "rewritten" the names
        // and we need this to do for the new nodes too in the same order
        HashMap<Integer, BigraphEntity.SiteEntity> mySites = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.NodeEntity> myNodes = new LinkedHashMap<>();


        HashMap<Integer, BigraphEntity.RootEntity> myRoots = new LinkedHashMap<>();
        for (BigraphEntity.RootEntity eachRoot : g.getRoots()) {
            myRoots.put(eachRoot.getIndex(), (BigraphEntity.RootEntity) builder.createNewRoot(eachRoot.getIndex()));
        }

        // new node name supplier for the acutal bigraph in question
        Supplier<String> supplier2 = createNameSupplier("v");
        for (BigraphEntity w : W_set) {
            if (BigraphEntityType.isNode(w)) {
                String s = supplier2.get();
//                s = ((BigraphEntity.NodeEntity) w).getName();
                BigraphEntity.NodeEntity newNode = (BigraphEntity.NodeEntity) builder.createNewNode(w.getControl(), s);
                myNodes.put(s, newNode);
            } else {
                BigraphEntity.SiteEntity newNode = (BigraphEntity.SiteEntity) builder.createNewSite(((BigraphEntity.SiteEntity) w).getIndex());
                mySites.put(((BigraphEntity.SiteEntity) w).getIndex(), newNode);
            }
        }


        for (BigraphEntity w : W_set) {
            BigraphEntity p = null;
            BigraphEntity prntFofW = f.getParent(w);
            FiniteOrdinal<Integer> j = Objects.nonNull(prntFofW) && BigraphEntityType.isRoot(prntFofW) ? FiniteOrdinal.ofInteger(((BigraphEntity.RootEntity) prntFofW).getIndex()) : null;
            if (kVF.contains(w) && V_F.containsValue(prntFofW)) {
                p = prntFofW;
            } else if (kVF.contains(w) && BigraphEntityType.isRoot(prntFofW) &&
                    mOrdinals.contains(j)) { // ist eine site of F AND is a site of G
                int index = ((BigraphEntity.RootEntity) prntFofW).getIndex();
                //find the site of G with index j
                Optional<BigraphEntity.SiteEntity> first = g.getSites().stream().filter(x -> x.getIndex() == index).findFirst();
                assert first.isPresent();
                p = g.getParent(first.get());
            } else if (V_G.containsValue(w)) {
                p = g.getParent(w);
            }
            //HERE:
            BigraphEntity w0; // get corresponding newly created node
            //can only be a node or site
            if (BigraphEntityType.isNode(w)) {
                String name = V.inverse().get(w); // get the rewritten name of the "old" node first
                w0 = myNodes.get(name); // get the new corresponding one
            } else {
                w0 = mySites.get(((BigraphEntity.SiteEntity) w).getIndex());
            }

            BigraphEntity p0 = null;
            if (Objects.nonNull(p)) {
                switch (p.getType()) {
                    case ROOT:
                        p0 = myRoots.get(((BigraphEntity.RootEntity) p).getIndex());
                        break;
                    case NODE:
                        String name = V.inverse().get(p); // get the rewritten name of the "old" node first
                        p0 = myNodes.get(name); // get the new corresponding one
                        break;
                    default:
                        break;
                }
                assert p0 != null;
                assert w0 != null;
                setParentOfNode(w0, p0);
//                System.out.println("Child: " + w0.getControl() + " -> Parent: " + p0);
            }
        }

        Supplier<String> rewriteEdgeNameSupplier = createNameSupplier("e");


        // Now the link graph ...

        //the next two will be kept for the new bigraph (the link graph interfaces)
        HashMap<String, BigraphEntity.OuterName> myOuterNames = new LinkedHashMap<>();//coming from G
        for (BigraphEntity.OuterName each : g.getOuterNames()) {
            myOuterNames.put(each.getName(), (BigraphEntity.OuterName) builder.createNewOuterName(each.getName())); //BigraphEntity.create(each.getInstance(), BigraphEntity.OuterName.class));
        }
        HashMap<String, BigraphEntity.InnerName> myInnerNames = new LinkedHashMap<>(); //coming from F
        for (BigraphEntity.InnerName each : f.getInnerNames()) {
            myInnerNames.put(each.getName(), (BigraphEntity.InnerName) builder.createNewInnerName(each.getName())); //BigraphEntity.create(each.getInstance(), BigraphEntity.InnerName.class));
        }

        HashMap<String, BigraphEntity.Edge> myEdges = new LinkedHashMap<>();


        HashBiMap<String, BigraphEntity.Edge> E = HashBiMap.create();
        Map<String, BigraphEntity.Edge> E_G = g.getEdges().stream().collect(Collectors.toMap(s -> rewriteEdgeNameSupplier.get(), Function.identity()));
        Map<String, BigraphEntity.Edge> E_F = f.getEdges().stream().collect(Collectors.toMap(s -> rewriteEdgeNameSupplier.get(), Function.identity()));
        // aggregated node set of the new bigraph to be composed
        E.putAll(E_G);
        E.putAll(E_F);

        Collection<BigraphEntity.InnerName> X = new LinkedHashSet<>(f.getInnerNames());

        List<AbstractMap.SimpleImmutableEntry<BigraphEntity.NodeEntity, BigraphEntity.Port>> portsF2 = V_F.values()
                .stream()
                .filter(n -> f.getPorts(n).size() != 0)
                .collect(Collectors.toMap(o -> o, nodeEntity -> f.getPorts(nodeEntity)))
                .entrySet()
                .stream()
                .flatMap(e -> e.getValue().stream().map(v -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), v)))
                .collect(Collectors.toList());
        List<AbstractMap.SimpleImmutableEntry<BigraphEntity.NodeEntity, BigraphEntity.Port>> portsG2 = V_G.values()
                .stream()
                .filter(n -> g.getPorts(n).size() != 0)
                .collect(Collectors.toMap(o -> o, nodeEntity -> g.getPorts(nodeEntity)))
                .entrySet()
                .stream()
                .flatMap(e -> e.getValue().stream().map(v -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), v)))
                .collect(Collectors.toList());
        List<AbstractMap.SimpleImmutableEntry<BigraphEntity.NodeEntity, BigraphEntity.Port>> allPorts_FG = new LinkedList<>();
        allPorts_FG.addAll(portsF2);
        allPorts_FG.addAll(portsG2);

        Set<BigraphEntity> Q_set = new LinkedHashSet<>(); // these are only points (inner names and ports)
        Q_set.addAll(X);
        Q_set.addAll(portsF2.stream().map(AbstractMap.SimpleImmutableEntry::getValue).collect(Collectors.toList()));
        Q_set.addAll(portsG2.stream().map(AbstractMap.SimpleImmutableEntry::getValue).collect(Collectors.toList()));

        // Convenience maps on the link graph interfaces of F and G
        HashMap<String, BigraphEntity.OuterName> outerNames_G = new LinkedHashMap<>();
        for (BigraphEntity.OuterName eachOuterName : g.getOuterNames()) {
            outerNames_G.put(eachOuterName.getName(), eachOuterName); //(BigraphEntity.OuterName) builder.createNewOuterName(eachOuterName.getName()));
        }
        //die verschwinden im neuen graph
        HashMap<String, BigraphEntity.InnerName> innerNames_G = new LinkedHashMap<>();
        for (BigraphEntity.InnerName eachInnerName : g.getInnerNames()) {
            innerNames_G.put(eachInnerName.getName(), eachInnerName); //(BigraphEntity.InnerName) builder.createNewInnerName(eachInnerName.getName()));
        }
        //die werden auch neu gebildet
        HashMap<String, BigraphEntity.InnerName> innerNames_F = new LinkedHashMap<>();
        for (BigraphEntity.InnerName eachInnerName : f.getInnerNames()) {
            innerNames_F.put(eachInnerName.getName(), eachInnerName); //(BigraphEntity.InnerName) builder.createNewInnerName(eachInnerName.getName()));
        }
        //Die verschwinden dann
        HashMap<String, BigraphEntity.OuterName> outerNames_F = new LinkedHashMap<>();
        for (BigraphEntity.OuterName each : f.getOuterNames()) {
            outerNames_F.put(each.getName(), each); //(BigraphEntity.InnerName) builder.createNewInnerName(eachInnerName.getName()));
        }
        //innerNames_G und outerNames_F können schonmal verbunden werden?
        //TODO 2nd condition must hold
        for (BigraphEntity q : Q_set) {
//            System.out.println(q);

            //C1,C3 preserving links
            //C2: is recreating links (outer --connect-> inner == edge, or inner name of inner big is connected to the edge
            // of a node)


            BigraphEntity linkQofF = f.getLinkOfPoint(q);
            if (Objects.nonNull(linkQofF)) {

                //C1: preserve links
                // is element and innername or port of F?
                // is element connected to an edge of F?
                // for F: connect innernames/ports to edges

                if ((X.contains(q) || getNodeFromPort(portsF2, q) != null) && (E_F.containsValue(linkQofF))) {
//                    System.out.println("\tlink(q) <- link_F(q)");
                    //link of current element must be the link_f of the current element
                    //determine if port or inner name
                    //determine index

                    //create the edge... linkQofF
                    assert BigraphEntityType.isEdge(linkQofF);
                    //edge name
                    String edgeName = E.inverse().get(linkQofF);
                    BigraphEntity.Edge edge = myEdges.get(edgeName); //((BigraphEntity.Edge) linkQofF).getName());
                    if (Objects.isNull(edge)) {
                        edge = (BigraphEntity.Edge) builder.createNewEdge(edgeName); //((BigraphEntity.Edge) linkQofF).getName());
                        myEdges.put(edge.getName(), edge);
                    }

                    if (BigraphEntityType.isPort(q)) {
                        //hole index
                        BigraphEntity.Port q1 = (BigraphEntity.Port) q;
                        String nodeName = V.inverse().get(getNodeFromPort(allPorts_FG, q1)); //allPorts_FG.inverse().get(q1));
                        BigraphEntity.NodeEntity nodeEntity = myNodes.get(nodeName);
                        assert nodeEntity != null;
                        BigraphEntity.Port newPortWithIndex = (BigraphEntity.Port) builder.createNewPortWithIndex(q1.getIndex());

                        EStructuralFeature portsRef = nodeEntity.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
                        EList<EObject> portsList = (EList<EObject>) nodeEntity.getInstance().eGet(portsRef);
                        portsList.add(newPortWithIndex.getInstance()); //newPortWithIndex.getIndex(),
                        //connect port to edge
                        EStructuralFeature lnkRef = newPortWithIndex.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                        newPortWithIndex.getInstance().eSet(lnkRef, edge.getInstance());

                    } else if (BigraphEntityType.isInnerName(q)) {
                        BigraphEntity.InnerName innerName = myInnerNames.get(((BigraphEntity.InnerName) q).getName());
                        assert innerName != null;
                        // connect inner name to edge
                        EStructuralFeature lnkRef = innerName.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                        innerName.getInstance().eSet(lnkRef, edge.getInstance());
                    }
                }

                //C2: (connect outer to inner names)
                // is element an inner name or port of F?
                // is element connected to an inner name of G?

                //q ist ein port und zeigt auf einen outername von F der gleich ist mit dem Inner name von G
                boolean contains = false;
                // we need the name: from the edge or an outer name
                String edgeName = E.inverse().get(linkQofF);
                //TODO move into next if clause. we get the name directly w/ null check because it
                // it is an outer name and cannot have an edge name
                if (Objects.isNull(edgeName) &&
                        Objects.nonNull(innerNames_G.get(((BigraphEntity.OuterName) linkQofF).getName()))) {
                    edgeName = ((BigraphEntity.OuterName) linkQofF).getName();
                }

                if (BigraphEntityType.isOuterName(linkQofF)) {
                    contains = g.getInnerFace().getValue().contains(StringTypedName.of(edgeName)); //StringTypedName.of(((BigraphEntity.OuterName) linkQofF).getName()));
                }
                //q is inner name or port of F AND link of q is a outer name of F with the same name as the inner name of g

                if ((X.contains(q) || getNodeFromPort(portsF2, q) != null) && contains) {
                    //get the corresponding outername
                    StringTypedName nameValue = StringTypedName.of(edgeName); //((BigraphEntity.OuterName) linkQofF).getName());
                    BigraphEntity.InnerName innerNameG = innerNames_G.get(nameValue.stringValue());
                    BigraphEntity.OuterName outerNameF = outerNames_F.get(nameValue.stringValue());

                    BigraphEntity link = g.getLinkOfPoint(innerNameG);
                    // ((BigraphEntity.OuterName) link).getName() == outerNameF.getName()
                    BigraphEntity newLink = null;
                    //is it an edge or an outer name?
                    if (BigraphEntityType.isEdge(link)) {
                        String name = ((BigraphEntity.Edge) link).getName();
                        newLink = myEdges.get(name);
                        if (Objects.isNull(newLink)) {
                            newLink = builder.createNewEdge(name);
                            myEdges.put(((BigraphEntity.Edge) newLink).getName(), (BigraphEntity.Edge) newLink);
                        }
                    } else if (BigraphEntityType.isOuterName(link)) {
                        String name = ((BigraphEntity.OuterName) link).getName();
                        newLink = myOuterNames.get(name);
                    }
                    if (newLink == null) continue;
                    assert newLink != null;
//                    System.out.println("\tlink(q) <- link_G(y)");
                    //index beachten
                    //via node names aus nodes holen

                    if (BigraphEntityType.isInnerName(q)) {
                        //erstelle neuen innername
                        BigraphEntity.InnerName newInnerName = myInnerNames.get(((BigraphEntity.InnerName) q).getName());
                        assert newInnerName != null;
//                        if(Objects.isNull(newInnerName)) {
//                            newInnerName = (BigraphEntity.InnerName) builder.createNewInnerName(((BigraphEntity.InnerName) q).getName());
//                        }
                        //connect://TODO move into the lightweight builder
                        EStructuralFeature pointRef = newLink.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
                        EList<EObject> pointsOfOuterName = (EList<EObject>) newLink.getInstance().eGet(pointRef);
                        pointsOfOuterName.add(newInnerName.getInstance());
//                        System.out.println("\tconnect inner name to " + link);

                    } else if (BigraphEntityType.isPort(q)) {
                        BigraphEntity.Port thePort = (BigraphEntity.Port) q;
                        //is a node of F

                        String nodeName = V.inverse().get(getNodeFromPort(allPorts_FG, thePort)); //allPorts_FG.inverse().get(thePort));
                        BigraphEntity.NodeEntity nodeEntity = myNodes.get(nodeName);
                        builder.connectToLinkUsingIndex(nodeEntity, newLink, thePort.getIndex());
//                        System.out.println("\tconnect port to edge " + ((BigraphEntity.Edge) link).getName());
                    }
                }
            }
            //C3: is a port of G:
            //for G-nodes: connect ports to edges or outer names of G

            if (getNodeFromPort(portsG2, q) != null) { //portsG2.values().contains(q)) {
//                System.out.println("\tlink(q) <- link_G(q)");
                assert BigraphEntityType.isPort(q);
                BigraphEntity.Port thePort = (BigraphEntity.Port) q;
                String nodeName = V.inverse().get(getNodeFromPort(allPorts_FG, thePort)); //allPorts_FG.inverse().get(thePort));
                BigraphEntity.NodeEntity nodeEntity = myNodes.get(nodeName);
                //is a node of F
                BigraphEntity linkQofG = g.getLinkOfPoint(thePort);
                if (BigraphEntityType.isOuterName(linkQofG)) {
                    //outer names are already created, they remain the same
                    BigraphEntity.OuterName outerName = myOuterNames.get(((BigraphEntity.OuterName) linkQofG).getName());
//                    System.out.println(outerName);
//                    BigraphEntity newPortWithIndex = builder.createNewPortWithIndex(thePort.getIndex());
//                    newPortWithIndex.getInstance().eSet(
//                            newPortWithIndex.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK),
//                            outerName.getInstance()
//                    );
                    builder.connectToLinkUsingIndex(nodeEntity, outerName, thePort.getIndex());
                } else if (BigraphEntityType.isEdge(linkQofG)) {
                    String edgeName = E.inverse().get(linkQofG);
                    BigraphEntity.Edge edge = myEdges.get(edgeName); //((BigraphEntity.Edge) linkQofG).getName());
                    if (Objects.isNull(edge)) {
                        edge = (BigraphEntity.Edge) builder.createNewEdge(edgeName); //((BigraphEntity.Edge) linkQofG).getName());
                        myEdges.put(edge.getName(), edge);
                    }
                    builder.connectToLinkUsingIndex(nodeEntity, edge, thePort.getIndex());
                }

            }
        }


        PureBigraphBuilder.InstanceParameter meta = builder.new InstanceParameter(
                getModelPackage(),
                getSignature(),
                myRoots,
                mySites,
                myNodes,
                myInnerNames, myOuterNames, myEdges);

        Bigraph<S> bigraph = (Bigraph<S>) new PureBigraph(meta);//TODO rework necessary -> unsure which bigraph should be employed here

        return new PureBigraphComposite<>(bigraph);
    }

    private BigraphEntity getNodeFromPort(List<AbstractMap.SimpleImmutableEntry<BigraphEntity.NodeEntity, BigraphEntity.Port>> collect, BigraphEntity searchPattern) {
        if (!BigraphEntityType.isPort(searchPattern)) return null;
        for (Map.Entry<BigraphEntity.NodeEntity, BigraphEntity.Port> each : collect) {
            if (each.getValue().equals(searchPattern)) return each.getKey();
        }
        return null;
    }

    protected void assertSignaturesAreSame(Bigraph<S> outer, Bigraph<S> inner) throws IncompatibleSignatureException {
        //special handling if one is an elementary bigraph
        if (inner instanceof DiscreteIon || outer instanceof DiscreteIon) {
            SignatureBuilder signatureBuilder = AbstractBigraphFactory.createPureBigraphFactory().createSignatureBuilder();
            inner.getSignature().getControls().forEach(x -> {
                signatureBuilder.addControl((Control) x);
            });
            outer.getSignature().getControls().forEach(x -> {
                signatureBuilder.addControl((Control) x);
            });
            builder = (MutableBuilder<S>) PureBigraphBuilder.newMutableBuilder(signatureBuilder.create());
            return;
        }
        if (outer instanceof ElementaryBigraph) {
            builder = (MutableBuilder<S>) PureBigraphBuilder.newMutableBuilder(inner.getSignature());
            return;
        } else if (inner instanceof ElementaryBigraph) {
            builder = (MutableBuilder<S>) PureBigraphBuilder.newMutableBuilder(outer.getSignature());
            return;
        }
        if (!outer.getSignature().equals(inner.getSignature())) {
            throw new IncompatibleSignatureException();
        }
    }
}
