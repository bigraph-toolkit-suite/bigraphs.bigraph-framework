package de.tudresden.inf.st.bigraphs.core.impl;

import com.google.common.collect.HashBiMap;
import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

//TODO originalen bigraph type class zurückgeben falls user casten sicher will
public class DefaultBigraphComposite<S extends Signature> extends BigraphDelegator<S> implements BigraphComposite<S> {

    private final MutableBuilder<S> builder;

    public DefaultBigraphComposite(Bigraph<S> bigraphDelegate) {
        super(bigraphDelegate);
        // this is safe: S is inferred from the bigraph too where S is the same type as the builder's type S (they will have the same type thus)
        this.builder = PureBigraphBuilder.newMutableBuilder(getBigraphDelegate().getSignature()); //new PureBigraphFactory().createBigraphBuilder(getBigraphDelegate().getSignature());
    }

    /**
     * Function that makes the nodes disjunct in terms of there names. This is needed for composition.
     */
//    private Consumer<BigraphEntity.NodeEntity> rewriteNodeNames = nodeEntity -> nodeEntity.setName(supplier.get());
    @Override
    public Bigraph<S> getOuterBigraph() {
        return getBigraphDelegate();
    }

    @Override
    public BigraphComposite<S> juxtapose(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        Bigraph<S> g = getBigraphDelegate();
        //rule: first G then F when rewriting names, ordinals
        assertSignaturesAreSame(g.getSignature(), f.getSignature());
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

//            BigraphEntity.NodeEntity nodeEntity = V.get(each.getKey());
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
                    newNode = (BigraphEntity.NodeEntity) builder.createNewNode(each.getValue().getControl(), each.getKey());
                    myNodes.put(each.getKey(), newNode);
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
        HashBiMap<String, BigraphEntity.InnerName> I = HashBiMap.create();
        HashBiMap<String, BigraphEntity.OuterName> O = HashBiMap.create();

        Map<String, BigraphEntity.Edge> E_G = g.getEdges().stream().collect(Collectors.toMap(s -> rewriteEdgeNameSupplier.get(), Function.identity()));
        Map<String, BigraphEntity.Edge> E_F = f.getEdges().stream().collect(Collectors.toMap(s -> rewriteEdgeNameSupplier.get(), Function.identity()));
        E.putAll(E_G);
        E.putAll(E_F);

        I.putAll(g.getInnerNames().stream().collect(Collectors.toMap(s -> s.getName(), Function.identity())));
        I.putAll(f.getInnerNames().stream().collect(Collectors.toMap(s -> s.getName(), Function.identity())));
        O.putAll(g.getOuterNames().stream().collect(Collectors.toMap(s -> s.getName(), Function.identity())));
        O.putAll(f.getOuterNames().stream().collect(Collectors.toMap(s -> s.getName(), Function.identity())));

        for (Map.Entry<String, BigraphEntity.NodeEntity> each : V.entrySet()) {
//            if (each.getValue().getControl().getArity().getValue().longValue() == 0) continue;
//            each.getValue().getControl().getArity().getClass();
            if (each.getValue().getControl().getArity().compareTo(FiniteOrdinal.ofInteger(0)) == 0) continue;

            Collection<BigraphEntity.Port> ports = f.getPorts(each.getValue());
            if (Objects.isNull(ports)) {
                ports = g.getPorts(each.getValue());
            }


            String nodeName = V.inverse().get(each.getValue());
            //portsize
//            int portSize = 0;
//            if (Objects.nonNull(V_F.get(nodeName))) {
//                portSize = f.getPorts(V_F.get(nodeName)).size();
//            } else {
//                portSize = g.getPorts(V_G.get(nodeName)).size();
//            }
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
        for (Map.Entry<String, BigraphEntity.InnerName> each : I.entrySet()) {
            BigraphEntity.InnerName newInnerName = myInnerNames.get(each.getKey());
            if (Objects.isNull(newInnerName)) {
                newInnerName = (BigraphEntity.InnerName) builder.createNewInnerName(each.getKey());
                myInnerNames.put(each.getKey(), newInnerName);
            }

            BigraphEntity link = g.getLinkOfPoint(each.getValue());
            if (Objects.isNull(link)) {
                link = f.getLinkOfPoint(each.getValue());
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

        Bigraph<S> bigraph = (Bigraph<S>) new PureBigraph(meta);//TODO rework necessary -> unsure which bigraph should be employed here

        return new DefaultBigraphComposite<>(bigraph);
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
        assertSignaturesAreSame(g.getSignature(), f.getSignature());
//        assertBigraphsAreNotSame(); //TODO important because node set must be disjunct too
        assertInterfaceCompatibleForCompose(g, f);

        Supplier<String> rewriteNameSupplier = createNameSupplier("v");

        //rewrite names...to make them disjoint. This will leave the original nodes untouched
        Map<String, BigraphEntity.NodeEntity> V_G = g.getNodes().stream().collect(Collectors.toMap(s -> rewriteNameSupplier.get(), Function.identity()));

        //rewrite names...to make them disjoint. This will leave the original nodes untouched
        Map<String, BigraphEntity.NodeEntity> V_F = f.getNodes().stream().collect(Collectors.toMap(s -> rewriteNameSupplier.get(), Function.identity()));

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
        W_set.addAll(V.values());
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
                BigraphEntity.NodeEntity newNode = (BigraphEntity.NodeEntity) builder.createNewNode(w.getControl(), s);
                myNodes.put(s, newNode);
            } else {
                BigraphEntity.SiteEntity newNode = (BigraphEntity.SiteEntity) builder.createNewSite(((BigraphEntity.SiteEntity) w).getIndex());
                mySites.put(((BigraphEntity.SiteEntity) w).getIndex(), newNode);
            }

            BigraphEntity p = null;
            BigraphEntity prntFofW = f.getParent(w);
            FiniteOrdinal<Integer> j = Objects.nonNull(prntFofW) && BigraphEntityType.isRoot(prntFofW) ? FiniteOrdinal.ofInteger(((BigraphEntity.RootEntity) prntFofW).getIndex()) : null;
            if (kVF.contains(w) && V_F.containsValue(prntFofW)) {
                p = prntFofW;
            } else if (kVF.contains(w) && BigraphEntityType.isRoot(prntFofW) && mOrdinals.contains(j)) { // ist eine site of F AND is a site of G
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

//        HashBiMap<BigraphEntity, BigraphEntity.Port> allPorts_FG = HashBiMap.create();
//        HashBiMap<BigraphEntity, BigraphEntity.Port> portsF2 = HashBiMap.create();
//        HashBiMap<BigraphEntity, BigraphEntity.Port> portsG2 = HashBiMap.create();
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

        Set<BigraphEntity> Q_set = new LinkedHashSet<>(); // these are only points (inner names or ports
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

        //TODO zweite condition muss holden
        for (BigraphEntity q : Q_set) {
            System.out.println(q);

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
                    System.out.println("\tlink(q) <- link_F(q)");
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
                // is element and innername or port of F?
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
                    assert newLink != null;
                    System.out.println("\tlink(q) <- link_G(y)");
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
                        System.out.println("\tconnect inner name to " + link);

                    } else if (BigraphEntityType.isPort(q)) {
                        BigraphEntity.Port thePort = (BigraphEntity.Port) q;
                        //is a node of F

                        String nodeName = V.inverse().get(getNodeFromPort(allPorts_FG, thePort)); //allPorts_FG.inverse().get(thePort));
                        BigraphEntity.NodeEntity nodeEntity = myNodes.get(nodeName);
                        builder.connectToEdgeUsingIndex(nodeEntity, (BigraphEntity.Edge) newLink, thePort.getIndex());
                        System.out.println("\tconnect port to edge " + ((BigraphEntity.Edge) link).getName());
                    }
                }
            }
            //C3: is a port of G:
            //for G-nodes: connect ports to edges or outer names of G

            if (getNodeFromPort(portsG2, q) != null) { //portsG2.values().contains(q)) {
                System.out.println("\tlink(q) <- link_G(q)");
                assert BigraphEntityType.isPort(q);
                BigraphEntity.Port thePort = (BigraphEntity.Port) q;
                String nodeName = V.inverse().get(getNodeFromPort(allPorts_FG, thePort)); //allPorts_FG.inverse().get(thePort));
                BigraphEntity.NodeEntity nodeEntity = myNodes.get(nodeName);
                //is a node of F
                BigraphEntity linkQofG = g.getLinkOfPoint(thePort);
                if (BigraphEntityType.isOuterName(linkQofG)) {
                    //outer names are already created, they remain the same
                    BigraphEntity.OuterName outerName = myOuterNames.get(((BigraphEntity.OuterName) linkQofG).getName());
                    System.out.println(outerName);
                    BigraphEntity newPortWithIndex = builder.createNewPortWithIndex(thePort.getIndex());
                    newPortWithIndex.getInstance().eSet(
                            newPortWithIndex.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK),
                            outerName.getInstance()
                    );
                    System.out.println("connectn port to outer name " + outerName.getName());
                } else if (BigraphEntityType.isEdge(linkQofG)) {
                    String edgeName = E.inverse().get(linkQofG);
                    BigraphEntity.Edge edge = myEdges.get(edgeName); //((BigraphEntity.Edge) linkQofG).getName());
                    if (Objects.isNull(edge)) {
                        edge = (BigraphEntity.Edge) builder.createNewEdge(edgeName); //((BigraphEntity.Edge) linkQofG).getName());
                        myEdges.put(edge.getName(), edge);
                    }
                    System.out.println(edge);
                    builder.connectToEdgeUsingIndex(nodeEntity, edge, thePort.getIndex());
                    System.out.println("connectn port to edge " + edge.getName());
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

        return new DefaultBigraphComposite<>(bigraph);
    }

    private BigraphEntity getNodeFromPort(List<AbstractMap.SimpleImmutableEntry<BigraphEntity.NodeEntity, BigraphEntity.Port>> collect, BigraphEntity searchPattern) {
        if (!BigraphEntityType.isPort(searchPattern)) return null;
        for (Map.Entry<BigraphEntity.NodeEntity, BigraphEntity.Port> each : collect) {
            if (each.getValue().equals(searchPattern)) return each.getKey();
        }
        return null;
    }

    private Supplier<String> createNameSupplier(final String prefix) {
        return new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return prefix + id++;
            }
        };
    }

    private Supplier<Integer> createNameSupplier() {
        return new Supplier<Integer>() {
            private int id = 0;

            @Override
            public Integer get() {
                return id++;
            }
        };
    }

    public <K, V> Optional<K> keys(Map<K, V> map, V value) {
        return map
                .entrySet()
                .stream()
                .filter(entry -> value.equals(entry.getValue()))
                .map(Map.Entry::getKey).findFirst();
    }

//    @Deprecated
//    private BigraphEntity<?> createNewObjectFrom(BigraphEntity<?> entity) {
//        //copy constructor call?
//        if (entity.getType().equals(BigraphEntityType.NODE)) {
//            return BigraphEntity.createNode(entity.getInstance(), entity.getControl());
//        } else {
//            return BigraphEntity.create(entity.getInstance(), entity.getClass());
//        }
//        // if isNode
//        //BigraphEntity(@NonNull EObject instance, C control, BigraphEntityType type) casten und übergeben
//        //else: BigraphEntity(@NonNull EObject instance, BigraphEntityType type) übergeben
//    }

    private void applyNewLink() {

    }

    private void setParentOfNode(final BigraphEntity node, final BigraphEntity parent) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        node.getInstance().eSet(prntRef, parent.getInstance()); // child is automatically added to the parent according to the ecore model
    }

    protected void assertSignaturesAreSame(S signature1, S signature2) throws IncompatibleSignatureException {
        //then one must be an elementary bigraph
        if (signature1.getControls().size() == 0 || signature2.getControls().size() == 0) return;
        if (!signature1.equals(signature2)) {
            throw new IncompatibleSignatureException();
        }
    }

    protected void assertInterfaceCompatibleForCompose(Bigraph<S> outer, Bigraph<S> inner) throws IncompatibleInterfaceException {
        Set<FiniteOrdinal<Integer>> siteOrdinals = outer.getInnerFace().getKey();
        Set<FiniteOrdinal<Integer>> rootOrdinals = inner.getOuterFace().getKey();
        Set<StringTypedName> nameSetLeft = outer.getInnerFace().getValue();
        Set<StringTypedName> nameSetRight = inner.getOuterFace().getValue();
        boolean disjoint = Collections.disjoint(nameSetLeft, nameSetRight);
        if ((rootOrdinals.size() > 0 || siteOrdinals.size() > 0) && nameSetLeft.size() == 0 && nameSetRight.size() == 0)
            disjoint = false; // this is legit if they are only place graphs
        boolean disjoint2 = siteOrdinals.size() != rootOrdinals.size() || Collections.disjoint(siteOrdinals, rootOrdinals);
        if (siteOrdinals.size() == 0 && rootOrdinals.size() == 0) disjoint2 = false;
        if (disjoint || disjoint2) {
            throw new IncompatibleInterfaceException();
        }
    }

    protected void assertInterfaceCompatibleForJuxtaposition(Bigraph<S> outer, Bigraph<S> inner) throws IncompatibleInterfaceException {
//        Set<FiniteOrdinal<Integer>> siteOrdinalsOuter = outer.getInnerFace().getKey();
//        Set<FiniteOrdinal<Integer>> rootsOrdinalsOuter = outer.getOuterFace().getKey();
//        Set<FiniteOrdinal<Integer>> sitesOrdinalsInner = inner.getInnerFace().getKey();
//        Set<FiniteOrdinal<Integer>> rootsOrdinalsInner = inner.getOuterFace().getKey();

        Set<StringTypedName> innerNamesOuter = outer.getInnerFace().getValue();
        Set<StringTypedName> innerNamesInner = inner.getInnerFace().getValue();

        Set<StringTypedName> outerNamesOuter = outer.getOuterFace().getValue();
        Set<StringTypedName> outerNamesInner = inner.getOuterFace().getValue();

        boolean disjointInnerNames = Collections.disjoint(innerNamesOuter, innerNamesInner);
        boolean disjointOuterNames = Collections.disjoint(outerNamesOuter, outerNamesInner);
        if (!disjointInnerNames || !disjointOuterNames) {
            throw new IncompatibleInterfaceException("Common inner names or outer names ...");
        }
    }
}
