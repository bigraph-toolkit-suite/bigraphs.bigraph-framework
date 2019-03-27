package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.HashBiMap;

//TODO originalen bigraph type class zurückgeben falls user casten sicher will
public class DefaultBigraphComposite<S extends Signature> extends BigraphDelegator<S> implements BigraphComposite<S> {

    private final MutableBuilder<S> builder;

    public DefaultBigraphComposite(Bigraph<S> bigraphDelegate) {
        super(bigraphDelegate);
        // this is safe: S is inferred from the bigraph too where S is the same type as the builder's type S (they will have the same type thus)
        this.builder = BigraphBuilder.newMutableBuilder(getBigraphDelegate().getSignature()); //new SimpleBigraphFactory().createBigraphBuilder(getBigraphDelegate().getSignature());
    }


    private Supplier<String> supplier = new Supplier<String>() {
        private int id = 0;

        @Override
        public String get() {
            return "v" + id++;
        }
    };

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

        //rewrite names...to make them disjoint. This will leave the original nodes untouched
//        Map<String, BigraphEntity.NodeEntity> V_G = new LinkedHashMap<>();//g.getNodes()
        Map<String, BigraphEntity.NodeEntity> V_G = g.getNodes().stream().collect(Collectors.toMap(s -> supplier.get(), Function.identity()));
//        Collections.copy(V_G, g.getNodes());
//        V_G.forEach(rewriteNodeNames); //rewrite names...to make them disjoint. This will leave the original nodes untouched

//        Collection<BigraphEntity.NodeEntity> V_F = new LinkedHashSet<>(f.getNodes());
//        Collections.copy(V_F, f.getNodes());
//        V_F.forEach(rewriteNodeNames); //rewrite names...to make them disjoint
        Map<String, BigraphEntity.NodeEntity> V_F = f.getNodes().stream().collect(Collectors.toMap(s -> supplier.get(), Function.identity()));
        HashBiMap<String, BigraphEntity.NodeEntity> V = HashBiMap.create();
        V.putAll(V_G);
        V.putAll(V_F);
//        Map<String, BigraphEntity.NodeEntity> V = new LinkedHashMap<>(V_G);
//        V.putAll(V_F);

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
        HashMap<Integer, BigraphEntity.SiteEntity> sites = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.NodeEntity> nodes = new LinkedHashMap<>();
//        for (BigraphEntity eachNode : W_set) {
//            if (BigraphEntityType.isNode(eachNode)) {
//                String s = supplier2.get();
//                BigraphEntity.NodeEntity newNode = (BigraphEntity.NodeEntity) builder.createNewNode(eachNode.getControl(), s);
//                nodes.put(s, newNode);
//            } else {
//                BigraphEntity.SiteEntity newNode = (BigraphEntity.SiteEntity) builder.createNewSite(((BigraphEntity.SiteEntity) eachNode).getIndex());
//                sites.put(((BigraphEntity.SiteEntity) eachNode).getIndex(), (BigraphEntity.SiteEntity) newNode);
//            }
//        }

        HashMap<Integer, BigraphEntity.RootEntity> roots = new LinkedHashMap<>();
//        Set<BigraphEntity.RootEntity> roots = new LinkedHashSet<>();
        for (BigraphEntity.RootEntity eachRoot : g.getRoots()) {
            roots.put(eachRoot.getIndex(), (BigraphEntity.RootEntity) builder.createNewRoot(eachRoot.getIndex()));
        }

        // new node name supplier for the acutal bigraph in question
        Supplier<String> supplier2 = new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return "v" + id++;
            }
        };
        for (BigraphEntity w : W_set) {
            if (BigraphEntityType.isNode(w)) {
                String s = supplier2.get();
                BigraphEntity.NodeEntity newNode = (BigraphEntity.NodeEntity) builder.createNewNode(w.getControl(), s);
                nodes.put(s, newNode);
            } else {
                BigraphEntity.SiteEntity newNode = (BigraphEntity.SiteEntity) builder.createNewSite(((BigraphEntity.SiteEntity) w).getIndex());
                sites.put(((BigraphEntity.SiteEntity) w).getIndex(), newNode);
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
                w0 = nodes.get(name); // get the new corresponding one
            } else {
                w0 = sites.get(((BigraphEntity.SiteEntity) w).getIndex());
            }

            BigraphEntity p0 = null;
            if (Objects.nonNull(p)) {
                switch (p.getType()) {
                    case ROOT:
                        p0 = roots.get(((BigraphEntity.RootEntity) p).getIndex());
                        break;
                    case NODE:
                        String name = V.inverse().get(p); // get the rewritten name of the "old" node first
                        p0 = nodes.get(name); // get the new corresponding one
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

        BigraphBuilder.InstanceParameter meta = builder.new InstanceParameter(
                getModelPackage(),
                getSignature(),
                roots,
                sites,
                nodes,
                Collections.EMPTY_MAP, Collections.EMPTY_MAP, Collections.EMPTY_MAP);

        Bigraph<S> bigraph = (Bigraph<S>) new DynamicEcoreBigraph(meta);//TODO rework necessary -> unsure which bigraph should be employed here

        return new DefaultBigraphComposite<>(bigraph);
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
