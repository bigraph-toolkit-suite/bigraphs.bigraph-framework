package de.tudresden.inf.st.bigraphs.core;

import com.google.common.collect.HashBiMap;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
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
        this.builder = BigraphBuilder.newMutableBuilder(getBigraphDelegate().getSignature()); //new SimpleBigraphFactory().createBigraphBuilder(getBigraphDelegate().getSignature());
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
    public BigraphComposite<S> compose(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        Bigraph<S> g = getBigraphDelegate();
        assertSignaturesAreSame(g.getSignature(), f.getSignature());
//        assertBigraphsAreNotSame(); //TODO important because node set must be disjunct too
        assertInterfaceCompatibleForCompose(getBigraphDelegate(), f);

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
                System.out.println("Child: " + w0.getControl() + " -> Parent: " + p0);
            }
        }

        Supplier<String> rewriteEdgeNameSupplier = createNameSupplier("e");


        // Now the link graph ...

        //the next two will be kept for the new bigraph (the link graph interfaces)
//        Supplier<String> edgeSupplier = createNameSupplier("e");
        HashMap<String, BigraphEntity.OuterName> myOuterNames = new LinkedHashMap<>();//coming from G
        for (BigraphEntity.OuterName each : g.getOuterNames()) {
            myOuterNames.put(each.getName(), BigraphEntity.create(each.getInstance(), BigraphEntity.OuterName.class));
        }
        HashMap<String, BigraphEntity.InnerName> myInnerNames = new LinkedHashMap<>(); //coming from F
        for (BigraphEntity.InnerName each : f.getInnerNames()) {
            myInnerNames.put(each.getName(), BigraphEntity.create(each.getInstance(), BigraphEntity.InnerName.class));
        }

        HashMap<String, BigraphEntity.Edge> myEdges = new LinkedHashMap<>();


        HashBiMap<String, BigraphEntity.Edge> E = HashBiMap.create();
        Map<String, BigraphEntity.Edge> E_G = g.getEdges().stream().collect(Collectors.toMap(s -> rewriteEdgeNameSupplier.get(), Function.identity()));
        Map<String, BigraphEntity.Edge> E_F = f.getEdges().stream().collect(Collectors.toMap(s -> rewriteEdgeNameSupplier.get(), Function.identity()));
        // aggregated node set of the new bigraph to be composed
        E.putAll(E_G);
        E.putAll(E_F);

        Collection<BigraphEntity.InnerName> X = new LinkedHashSet<>(f.getInnerNames());

        HashBiMap<BigraphEntity, BigraphEntity.Port> allPorts_FG = HashBiMap.create();
        HashBiMap<BigraphEntity, BigraphEntity.Port> portsF2 = HashBiMap.create();
        HashBiMap<BigraphEntity, BigraphEntity.Port> portsG2 = HashBiMap.create();
        portsF2.putAll(V_F.values()
                .stream()
                .filter(n -> f.getPorts(n).size() != 0)
                .collect(Collectors.toMap(o -> o, nodeEntity -> f.getPorts(nodeEntity)))
                .entrySet()
                .stream().flatMap(e -> e.getValue()
                        .stream().map(v -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), v)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        portsG2.putAll(V_G.values()
                .stream()
                .filter(n -> g.getPorts(n).size() != 0)
                .collect(Collectors.toMap(o -> o, nodeEntity -> g.getPorts(nodeEntity)))
                .entrySet()
                .stream().flatMap(e -> e.getValue()
                        .stream().map(v -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), v)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        allPorts_FG.putAll(portsF2);
        allPorts_FG.putAll(portsG2);

        Set<BigraphEntity> Q_set = new LinkedHashSet<>(); // these are only points (inner names or ports
        Q_set.addAll(portsF2.values());
        Q_set.addAll(portsG2.values());
        Q_set.addAll(X);

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


        for (BigraphEntity q : Q_set) {
            System.out.println(q);

            //C1,C3 preserving links
            //C2: is recreating links (outer --connect-> inner == edge)


            BigraphEntity linkQofF = f.getLink(q);
            if (Objects.nonNull(linkQofF)) {

                //C1: preserve links
                // is element and innername or port of F?
                // is element connected to an edge of F?
                // for F: connect innernames/ports to edges
                if ((X.contains(q) || portsF2.values().contains(q)) && (E_F.containsValue(linkQofF))) {
                    System.out.println("\tlink(q) <- link_F(q)");
                    //link of current element must be the link_f of the current element
                    //determine if port or inner name
                    //determine index

                    //create the edge... linkQofF
                    assert BigraphEntityType.isEdge(linkQofF);
                    BigraphEntity.Edge edge = myEdges.get(((BigraphEntity.Edge) linkQofF).getName());
                    if (Objects.isNull(edge)) {
                        edge = (BigraphEntity.Edge) builder.createNewEdge(((BigraphEntity.Edge) linkQofF).getName());
                        myEdges.put(edge.getName(), edge);
                    }

                    if (BigraphEntityType.isPort(q)) {
                        //hole index
                        BigraphEntity.Port q1 = (BigraphEntity.Port) q;
                        String nodeName = V.inverse().get(allPorts_FG.inverse().get(q1));
                        BigraphEntity.NodeEntity nodeEntity = myNodes.get(nodeName);
                        assert nodeEntity != null;
                        BigraphEntity.Port newPortWithIndex = (BigraphEntity.Port) builder.createNewPortWithIndex(q1.getIndex());

                        EStructuralFeature portsRef = nodeEntity.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
                        EList<EObject> portsList = (EList<EObject>) nodeEntity.getInstance().eGet(portsRef);
                        portsList.set(newPortWithIndex.getIndex(), newPortWithIndex.getInstance());
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
                if (BigraphEntityType.isOuterName(linkQofF)) {
                    contains = g.getInnerFace().getValue().contains(StringTypedName.of(((BigraphEntity.OuterName) linkQofF).getName()));
                }
                //q is inner name or port of F AND link of q is a outer name of F with the same name as the inner name of g
                if ((X.contains(q) || portsF2.values().contains(q)) && contains) {
                    //get the corresponding outername
                    StringTypedName nameValue = StringTypedName.of(((BigraphEntity.OuterName) linkQofF).getName());
                    BigraphEntity.InnerName innerNameG = innerNames_G.get(nameValue.stringValue());
                    BigraphEntity.OuterName outerNameF = outerNames_F.get(nameValue.stringValue());

                    BigraphEntity link = g.getLink(innerNameG);

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
                        System.out.println("\tconnect inner name to outer name " + ((BigraphEntity.OuterName) link).getName());

                    } else if (BigraphEntityType.isPort(q)) {
                        BigraphEntity.Port thePort = (BigraphEntity.Port) q;
                        //is a node of F
                        String nodeName = V.inverse().get(allPorts_FG.inverse().get(thePort));
                        BigraphEntity.NodeEntity nodeEntity = myNodes.get(nodeName);
                        builder.connectToEdgeUsingIndex(nodeEntity, (BigraphEntity.Edge) newLink, thePort.getIndex());
                        System.out.println("\tconnect port to edge " + ((BigraphEntity.Edge) link).getName());
                    }
                }
            }
            //C3: is a port of G:
            //for G-nodes: connect ports to edges or outer names of G
            if (portsG2.values().contains(q)) {
                System.out.println("\tlink(q) <- link_G(q)");
                assert BigraphEntityType.isPort(q);
                BigraphEntity.Port thePort = (BigraphEntity.Port) q;
                String nodeName = V.inverse().get(allPorts_FG.inverse().get(thePort));
                BigraphEntity.NodeEntity nodeEntity = myNodes.get(nodeName);
                //is a node of F
                BigraphEntity linkQofG = g.getLink(thePort);
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
                    BigraphEntity.Edge edge = myEdges.get(((BigraphEntity.Edge) linkQofG).getName());
                    if (Objects.isNull(edge)) {
                        edge = (BigraphEntity.Edge) builder.createNewEdge(((BigraphEntity.Edge) linkQofG).getName());
                        myEdges.put(edge.getName(), edge);
                    }
                    System.out.println(edge);
                    builder.connectToEdgeUsingIndex(nodeEntity, edge, thePort.getIndex());
                    System.out.println("connectn port to edge " + edge.getName());
                }

            }
        }


        BigraphBuilder.InstanceParameter meta = builder.new InstanceParameter(
                getModelPackage(),
                getSignature(),
                myRoots,
                mySites,
                myNodes,
                myInnerNames, myOuterNames, myEdges);

        Bigraph<S> bigraph = (Bigraph<S>) new DynamicEcoreBigraph(meta);//TODO rework necessary -> unsure which bigraph should be employed here

        return new DefaultBigraphComposite<>(bigraph);
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

    public <K, V> Optional<K> keys(Map<K, V> map, V value) {
        return map
                .entrySet()
                .stream()
                .filter(entry -> value.equals(entry.getValue()))
                .map(Map.Entry::getKey).findFirst();
    }

//    public BigraphEntity findByNodeName(Collection<BigraphEntity> list, String nodeName) {
//        return list.stream().filter(x -> BigraphEntityType.isNode(x)
//                && ((BigraphEntity.NodeEntity<Object>) x).getName().equals(nodeName))
//                .findFirst()
//                .get();
//    }
//
//    public BigraphEntity findRootByIndex(Collection<BigraphEntity.RootEntity> list, int index) {
//        return list.stream().filter(x -> x.getIndex() == index)
//                .findFirst()
//                .get();
//    }

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
        if (disjoint || siteOrdinals.size() != rootOrdinals.size() || Collections.disjoint(siteOrdinals, rootOrdinals)) {
            throw new IncompatibleInterfaceException();
        }
    }
}
