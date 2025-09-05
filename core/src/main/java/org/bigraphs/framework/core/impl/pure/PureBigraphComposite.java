package org.bigraphs.framework.core.impl.pure;

import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.datatypes.NamedType;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.elementary.DiscreteIon;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.bigraphs.framework.core.utils.emf.EMFUtils;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

/**
 * Composable bigraph implementation of {@link BigraphComposite} for <b>pure bigraphs</b>.
 * <p>
 * Equips the bigraph with some categorical operators to compute the product of two bigraphs.
 * Operators can only be used by bigraphs of the same type and signature, except with elementary ones.
 *
 * @param <S> type of the signature.
 * @author Dominik Grzelak
 */
public class PureBigraphComposite<S extends AbstractEcoreSignature<? extends Control<?, ?>>> extends BigraphCompositeSupport<S> implements EcoreBigraph<S> {

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
        this.builder = MutableBuilder.newMutableBuilder(getBigraphDelegate().getSignature(), ((EcoreBigraph) getBigraphDelegate()).getMetaModel()); // ((EcoreBigraph) bigraph).getEMetaModelData());
    }

    /**
     * Function that makes the nodes disjunct in terms of there names. This is needed for composition.
     */
    @Override
    public PureBigraph getOuterBigraph() {
        return (PureBigraph) getBigraphDelegate();
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
        return parallelProduct((Bigraph<S>) f.getOuterBigraph());
    }

    @Override
    public BigraphComposite<S> compose(BigraphComposite<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        return this.compose((Bigraph<S>) f.getOuterBigraph());
    }

    @Override
    public BigraphComposite<S> nesting(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        Bigraph<S> g = BigraphUtil.copyIfSame(getBigraphDelegate(), f);
        assertSignaturesAreSame(g, f);

        // Get all outer names of 'f' and make identity graph of them
        Linkings<DefaultDynamicSignature> linkings = pureLinkings((DefaultDynamicSignature) getSignature());
        Set<StringTypedName> collect = f.getOuterNames().stream()
                .map(o -> StringTypedName.of(o.getName()))
                .collect(Collectors.toSet());
        ElementaryBigraph<DefaultDynamicSignature> identity = collect.size() != 0 ?
                linkings.identity(collect.toArray(new NamedType[0])) : // as array
                linkings.identity_e();                                 // empty identity
//        // (!) Order is important here, otherwise the root indexes may be swapped. First g, then the identity
        BigraphComposite<S> sBigraphComposite = ops(g).parallelProduct((Bigraph<S>) identity);
        return sBigraphComposite.compose(f);
    }

    @Override
    public BigraphComposite<S> nesting(BigraphComposite<S> inner) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        return this.nesting((Bigraph<S>) inner.getOuterBigraph());
    }

    @Override
    public BigraphComposite<S> juxtapose(BigraphComposite<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        return this.juxtapose((Bigraph<S>) f.getOuterBigraph());
    }

    public BigraphComposite<S> parallelProduct(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        Bigraph<S> g = getBigraphDelegate();
        assertSignaturesAreSame(g, f);

        Supplier<Integer> rewriteRootSupplier = createNameSupplier(g.getRoots().size());
        Supplier<Integer> rewriteSiteSupplier = createNameSupplier(0);
        Supplier<String> rewriteNameSupplier = createNameSupplier("v", g.getNodes().size());
        Supplier<String> rewriteEdgeNameSupplier = createNameSupplier("e", g.getEdges().size());

        // if we don't copy, both bigraph get 'destroyed'
        Bigraph<S> copy = BigraphUtil.copy(g);
        Bigraph<S> copyOuter = BigraphUtil.copy(f);

        EObject left = ((PureBigraph) copy).getInstanceModel();
        EObject right = ((PureBigraph) copyOuter).getInstanceModel();


        // Routine: Add roots side-by-side
        EStructuralFeature rightRootsFeature = right.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BROOTS);
        EStructuralFeature leftRootsFeature = left.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BROOTS);
        if (Objects.nonNull(right.eGet(rightRootsFeature))) {
            EList<EObject> roots = (EList<EObject>) right.eGet(rightRootsFeature);
            for (int i = roots.size() - 1; i >= 0; i--) {
                EObject eachRoot = roots.get(i);
                EAttribute indexAttr = EMFUtils.findAttribute(eachRoot.eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
                eachRoot.eSet(indexAttr, rewriteRootSupplier.get());
                if (Objects.nonNull(left.eGet(leftRootsFeature))) {
                    ((EList<EObject>) left.eGet(leftRootsFeature)).add(eachRoot);
                    TreeIterator<Object> allContents = EcoreUtil.getAllContents(eachRoot, true);
                    while (allContents.hasNext()) {
                        EObject next = (EObject) allContents.next();
                        if (((EcoreBigraph) copyOuter).isNameable(next) && ((EcoreBigraph) copyOuter).isBPlace(next)) {
                            EAttribute nameAttr = EMFUtils.findAttribute(next.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                            next.eSet(nameAttr, rewriteNameSupplier.get());
                        }
                    }
                }
            }
        }

        // Collect outer names
        EStructuralFeature leftOuterNamesFeature = left.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES);
        EStructuralFeature rightOuterNamesFeature = right.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES);
        EStructuralFeature leftInnerNameFeature = left.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BINNERNAMES);
        EStructuralFeature rightInnerNameFeature = right.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BINNERNAMES);
        EStructuralFeature leftEdgesFeature = left.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BEDGES);
        EStructuralFeature rightEdgesFeature = right.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BEDGES);

        // First, collect connectedness between inner and outer names. This is later needed to satisfy (!Condition!) below
        MutableMap<String, String> innerOuterLinkage = Maps.mutable.empty();
        MutableMap<EObject, Boolean> edgeToInner = Maps.mutable.empty();
        MutableMap<EObject, List<String>> portsToInnerLabels = Maps.mutable.empty();
        if (Objects.nonNull(left.eGet(leftOuterNamesFeature)) && Objects.nonNull(left.eGet(rightOuterNamesFeature))) {
            EList<EObject> outernamesL = (EList<EObject>) left.eGet(leftOuterNamesFeature);
            EList<EObject> outernamesR = (EList<EObject>) right.eGet(rightOuterNamesFeature);
            List<EObject> all = new ArrayList<>();
            all.addAll(outernamesL);
            all.addAll(outernamesR);
            for (EObject eachOuterName : all) {
                EAttribute nameAttr = EMFUtils.findAttribute(eachOuterName.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                String name = (String) eachOuterName.eGet(nameAttr);
                EStructuralFeature bPoints = eachOuterName.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
                if (bPoints != null) {
                    EList<EObject> points = (EList<EObject>) eachOuterName.eGet(bPoints);
                    for (int i = points.size() - 1; i >= 0; i--) {
                        EObject eachPoint = points.get(i);
                        if (isBInnerName(eachPoint)) {
                            EAttribute nameAttr2 = EMFUtils.findAttribute(eachPoint.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                            String name2 = (String) eachPoint.eGet(nameAttr2);
                            innerOuterLinkage.put(name2, name);
                        }
                    }
                }
            }
        }
        // Also collect information which edge (if exists) is connected to a previously found inner name
        if (Objects.nonNull(left.eGet(leftEdgesFeature)) && Objects.nonNull(left.eGet(rightEdgesFeature))) {
            EList<EObject> edgesL = (EList<EObject>) left.eGet(leftEdgesFeature);
            EList<EObject> edgesR = (EList<EObject>) right.eGet(rightEdgesFeature);
            List<EObject> all = new ArrayList<>();
            all.addAll(edgesL);
            all.addAll(edgesR);
            MutableMap<EObject, List<String>> edgesToInners = Maps.mutable.empty();
            for (int i = all.size() - 1; i >= 0; i--) {
                EObject edge = all.get(i);
                if (isBEdge(edge)) {
                    EStructuralFeature bPoint = edge.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
                    EList<EObject> allPoints = (EList<EObject>) edge.eGet(bPoint);
                    for (EObject eachPoint2 : allPoints) {
                        if (isBInnerName(eachPoint2)) {
                            String innerLabel = (String) eachPoint2.eGet(EMFUtils.findAttribute(eachPoint2.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME));
                            edgeToInner.put(edge, innerOuterLinkage.containsKey(innerLabel));
                            if (edgesToInners.get(edge) == null) {
                                edgesToInners.put(edge, Lists.mutable.of());
                            }
                            edgesToInners.get(edge).add(innerLabel);
                        }
                    }
                }
            }
            for (EObject edge : all) {
                if(edgeToInner.get(edge) == null) {
                    edgeToInner.remove(edge);
                    continue;
                }
                EStructuralFeature bPoint = edge.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
                EList<EObject> allPoints = (EList<EObject>) edge.eGet(bPoint);
                for (EObject eachPoint2 : allPoints) {
                    if (isBPort(eachPoint2) && (!edgeToInner.isEmpty() && edgeToInner.get(edge))) {
                        if (portsToInnerLabels.get(eachPoint2) == null) {
                            portsToInnerLabels.put(eachPoint2, Lists.mutable.of());
                        }
                        portsToInnerLabels.get(eachPoint2).addAll(edgesToInners.get(edge));
                    }
                }
            }
        }


        MutableMap<String, EObject> outernamesOuterIndexLeft = Maps.mutable.empty(); //new HashMap<>();
        // MutableMap<String, EObject> outernamesOuterIndexRight = Maps.mutable.empty(); //new HashMap<>();
        if (Objects.nonNull(left.eGet(leftOuterNamesFeature))) {
            EList<EObject> outernames = (EList<EObject>) left.eGet(leftOuterNamesFeature);
            for (EObject eachOuterName : outernames) {
                EAttribute nameAttr = EMFUtils.findAttribute(eachOuterName.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                outernamesOuterIndexLeft.put((String) eachOuterName.eGet(nameAttr), eachOuterName);
            }
        }

        // Adapter for relinking and renaming
        EContentAdapter adapter2 = new EContentAdapter() {
            public void notifyChanged(Notification notification) {
                // Edges
                if (notification.getFeature() == rightEdgesFeature) {
                    MutableList<EObject> edges = Lists.mutable.empty(); //new ArrayList<>();
                    if (notification.getNewValue() instanceof Collection) {
                        edges.addAll((Collection) notification.getNewValue());
                    } else {
                        edges.add((EObject) notification.getNewValue());
                    }
                    for (EObject eachEdge : edges) {
                        EAttribute nameAttr = EMFUtils.findAttribute(eachEdge.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                        eachEdge.eSet(nameAttr, rewriteEdgeNameSupplier.get());
                    }
                }

                // Outer names
                if (notification.getFeature() == rightOuterNamesFeature) {
                    MutableList<EObject> outernames = Lists.mutable.empty(); //new ArrayList<>();
                    if (notification.getNewValue() instanceof Collection) {
                        outernames.addAll((Collection) notification.getNewValue());
                    } else {
                        outernames.add((EObject) notification.getNewValue());
                    }
                    for (EObject outerNameRight : outernames) {
                        EAttribute nameAttr = EMFUtils.findAttribute(outerNameRight.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                        String name = (String) outerNameRight.eGet(nameAttr);
                        if (outernamesOuterIndexLeft.get(name) != null) {
                            EObject outerLeft = outernamesOuterIndexLeft.get(name);
                            EStructuralFeature bPoints = outerNameRight.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
                            if (bPoints != null) {
                                EList<EObject> points = (EList<EObject>) outerNameRight.eGet(bPoints);
                                for (int i = points.size() - 1; i >= 0; i--) {
                                    EObject eachPoint = points.get(i);
                                    EStructuralFeature bLink = eachPoint.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                                    eachPoint.eSet(bLink, outerLeft);
                                }
                            }
                        }
                    }
                }

                // Inner names
                if (notification.getFeature() == rightInnerNameFeature) {
                    MutableList<EObject> inner = Lists.mutable.empty();
                    if (notification.getNewValue() instanceof Collection) {
                        inner.addAll((Collection) notification.getNewValue());
                    } else {
                        inner.add((EObject) notification.getNewValue());
                    }
                    for (EObject eachInner : inner) {
                        EStructuralFeature bLink = eachInner.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                        EObject outerInner = (EObject) eachInner.eGet(bLink);
                        if (outerInner != null) {
                            EAttribute nameAttr = EMFUtils.findAttribute(outerInner.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                            if (outernamesOuterIndexLeft.get(outerInner.eGet(nameAttr)) != null) {
                                eachInner.eSet(bLink, outernamesOuterIndexLeft.get(outerInner.eGet(nameAttr)));
                            }
                        }
                    }
                }
            }
        };
        left.eAdapters().add(adapter2);
        ((EList<EObject>) left.eGet(leftEdgesFeature)).addAll((EList<EObject>) right.eGet(rightEdgesFeature));

        // add all outer names
//        EStructuralFeature leftOuterNamesFeature = left.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES);
//        EStructuralFeature rightOuterNamesFeature = right.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES);
        List<EObject> collect = ((EList<EObject>) right.eGet(rightOuterNamesFeature)).stream().filter(x -> {
            EAttribute nameAttr = EMFUtils.findAttribute(x.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
            return outernamesOuterIndexLeft.get(x.eGet(nameAttr)) == null;
        }).collect(Collectors.toList());
        ((EList<EObject>) left.eGet(leftOuterNamesFeature)).addAll(collect);

        // Add all inner names
        EList<EObject> innerNamesLeftInner = (EList<EObject>) left.eGet(leftInnerNameFeature);//.addAll(innernamesRight);
        EList<EObject> innernamesRight = (EList<EObject>) right.eGet(rightInnerNameFeature);
        Set<String> innerNames = g.getInnerNames().stream().map(BigraphEntity.InnerName::getName).collect(Collectors.toSet());
        for (int i = innernamesRight.size() - 1; i >= 0; i--) {
            EObject x = innernamesRight.get(i);
            EAttribute nameAttr = EMFUtils.findAttribute(x.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
            if (Objects.nonNull(nameAttr) &&
                    Objects.nonNull(x.eGet(nameAttr)) &&
                    (!innerNames.contains((String) x.eGet(nameAttr)))) {
                innerNamesLeftInner.add(x);
            }
        }

        // collect right outer names
        if (Objects.nonNull(right.eGet(rightOuterNamesFeature))) {
            EList<EObject> outernames = (EList<EObject>) right.eGet(rightOuterNamesFeature);
            for (EObject eachOuterName : outernames) {
                EAttribute nameAttr = EMFUtils.findAttribute(eachOuterName.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                if (outernamesOuterIndexLeft.containsKey((String) eachOuterName.eGet(nameAttr))) {
//                    System.out.println("reconnect" + (String) eachOuterName.eGet(nameAttr));
                    final EStructuralFeature pointsRef = eachOuterName.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
                    if (Objects.nonNull(pointsRef)) {
                        final EList<EObject> pointsObjects = (EList<EObject>) eachOuterName.eGet(pointsRef);
                        if (Objects.nonNull(pointsObjects)) {
                            for (int i = pointsObjects.size() - 1; i >= 0; i--) {
                                EObject eachpoint = pointsObjects.get(i);
                                EStructuralFeature bLink = eachpoint.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                                eachpoint.eSet(bLink, outernamesOuterIndexLeft.get((String) eachOuterName.eGet(nameAttr)));
                            }
                        }
                    }
                }
            }
        }

        left.eAdapters().clear();
        right.eAdapters().clear();

        // (!Condition!) "Condition that link_0 ∩ link_1 is a function amounts to requiring that,
        // for every inner name x ∈ X_0 ∩ X_1, there exists an outer name y ∈ Y_0 ∩ Y_1 such that
        // link_0(x) = link_1(x) = y"
        // if for left one port is connected to an inner name, and for right, an inner name is connected to an outer name
        // and left has also an idle outer name with the same name as right
        // then the edge is discarded from left and linked to the same outer name of right
        for (Map.Entry<String, String> each : innerOuterLinkage.entrySet()) {
            String innerLabel = each.getKey();
            String outerLabel = each.getValue();
            Optional<EObject> first = ((EList<?>) left.eGet(leftInnerNameFeature)).stream().map(x -> (EObject) x)
                    .filter(x -> {
                        String name = (String) x.eGet(EMFUtils.findAttribute(x.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME));
                        return name.equals(innerLabel);
                    }).findFirst();
            Optional<EObject> second = ((EList<?>) left.eGet(leftOuterNamesFeature)).stream().map(x -> (EObject) x)
                    .filter(x -> {
                        String name = (String) x.eGet(EMFUtils.findAttribute(x.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME));
                        return name.equals(outerLabel);
                    }).findFirst();
            if (first.isPresent() && second.isPresent()) {
                EStructuralFeature bLink = first.get().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                for (EObject eachPort : portsToInnerLabels.keySet()) {
                    if (portsToInnerLabels.get(eachPort).contains(innerLabel)) {
                        EStructuralFeature bLinkPort = eachPort.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                        eachPort.eSet(bLinkPort, second.get());
                    }
                }
                first.get().eSet(bLink, second.get());
            }
        }

        // remove idle edges (due to (!Condition!)) that may appear when mapping ports to outer names that were previously connected to an edge that
        // was connected to an inner name that was connected to an outer name.
        EList<EObject> edges = (EList<EObject>) left.eGet(leftEdgesFeature);
        for (int i = edges.size() - 1; i >= 0; i--) {
            EObject each = edges.get(i);
            final EStructuralFeature pointsRef = each.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
            if (Objects.nonNull(pointsRef) && ((EList<EObject>) each.eGet(pointsRef)).size() == 0) {
                EcoreUtil.remove(each);
            }
        }


//        Stream.concat(copy.getSites().stream().sorted(), copyOuter.getSites().stream().sorted()).forEachOrdered(s -> {
//            EObject site = s.getInstance();
//            final EStructuralFeature prnt = site.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
//            if (Objects.nonNull(site.eGet(prnt))) {
//                EAttribute indexAttr = EMFUtils.findAttribute(site.eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
//                if (Objects.nonNull(indexAttr)) {
//                    site.eSet(indexAttr, rewriteSiteSupplier.get());
//                }
//            }
//        });
        copy.getSites().stream().sorted().forEachOrdered(s -> {
            EObject site = s.getInstance();
            final EStructuralFeature prnt = site.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
            if (Objects.nonNull(site.eGet(prnt))) {
                EAttribute indexAttr = EMFUtils.findAttribute(site.eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
                if (Objects.nonNull(indexAttr)) {
                    site.eSet(indexAttr, rewriteSiteSupplier.get());
                }
            }
        });
        List<BigraphEntity.SiteEntity> sTemp = new ArrayList<>(copyOuter.getSites());
        for (int i = sTemp.size() - 1; i >= 0; i--) {
            BigraphEntity.SiteEntity s = sTemp.get(i);
            EObject site = s.getInstance();
            final EStructuralFeature prnt = site.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
            if (Objects.nonNull(site.eGet(prnt))) {
                EAttribute indexAttr = EMFUtils.findAttribute(site.eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
                if (Objects.nonNull(indexAttr)) {
                    site.eSet(indexAttr, rewriteSiteSupplier.get());
                }
            }
        }

        PureBigraph bigraph = PureBigraphBuilder.create(g.getSignature(), ((PureBigraph) copy).getMetaModel(), ((PureBigraph) copy).getInstanceModel()).createBigraph();
        return new PureBigraphComposite<>((Bigraph<S>) bigraph);
    }

    public BigraphComposite<S> juxtapose(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        Bigraph<S> g = getBigraphDelegate();
        assertSignaturesAreSame(g, f);
        assertInterfaceCompatibleForJuxtaposition(g, f);

        Supplier<Integer> rewriteRootSupplier = createNameSupplier(g.getRoots().size());
        Supplier<String> rewriteNameSupplier = createNameSupplier("v", g.getNodes().size());
        Supplier<String> rewriteEdgeNameSupplier = createNameSupplier("e", g.getEdges().size());
        Supplier<Integer> rewriteSitesNameSupplier = createNameSupplier(g.getSites().size());

        // if we don't copy, both bigraph get 'destroyed'
        Bigraph<S> copy = BigraphUtil.copy(g);
        Bigraph<S> copyOuter = BigraphUtil.copy(f);

        EObject left = ((PureBigraph) copy).getInstanceModel();
        EObject right = ((PureBigraph) copyOuter).getInstanceModel();

        // Routine: Add roots side-by-side
        EStructuralFeature rightRootsFeature = right.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BROOTS);
        EStructuralFeature leftRootsFeature = left.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BROOTS);
        if (Objects.nonNull(right.eGet(rightRootsFeature))) {
            EList<EObject> roots = (EList<EObject>) right.eGet(rightRootsFeature);
            for (int i = roots.size() - 1; i >= 0; i--) {
                EObject eachRoot = roots.get(i);
                EAttribute indexAttr = EMFUtils.findAttribute(eachRoot.eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
                eachRoot.eSet(indexAttr, rewriteRootSupplier.get());
                if (Objects.nonNull(left.eGet(leftRootsFeature))) {
                    ((EList<EObject>) left.eGet(leftRootsFeature)).add(eachRoot);
                    TreeIterator<Object> allContents = EcoreUtil.getAllContents(eachRoot, true);
                    while (allContents.hasNext()) {
                        EObject next = (EObject) allContents.next();
                        if (((EcoreBigraph) copyOuter).isNameable(next) && ((EcoreBigraph) copyOuter).isBPlace(next)) {
                            EAttribute nameAttr = EMFUtils.findAttribute(next.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                            next.eSet(nameAttr, rewriteNameSupplier.get());
                        } else if (((EcoreBigraph) copyOuter).isBSite(next)) {
                            EAttribute indexAttrSite = EMFUtils.findAttribute(next.eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
                            next.eSet(indexAttrSite, rewriteSitesNameSupplier.get());
                        }
                    }
                }
            }
        }

        // Add all edges
        EStructuralFeature leftEdgesFeature = left.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BEDGES);
        EStructuralFeature rightEdgesFeature = right.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BEDGES);
        EContentAdapter adapter2 = new EContentAdapter() {
            public void notifyChanged(Notification notification) {
                if (notification.getFeature() == rightEdgesFeature) {
                    MutableList<EObject> edges = Lists.mutable.empty(); //new ArrayList<>();
                    if (notification.getNewValue() instanceof Collection) {
                        edges.addAll((Collection) notification.getNewValue());
                    } else {
                        edges.add((EObject) notification.getNewValue());
                    }
                    for (EObject eachEdge : edges) {
                        EAttribute nameAttr = EMFUtils.findAttribute(eachEdge.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                        eachEdge.eSet(nameAttr, rewriteEdgeNameSupplier.get());
                    }
                }
            }
        };
        left.eAdapters().add(adapter2);
        ((EList<EObject>) left.eGet(leftEdgesFeature)).addAll((EList<EObject>) right.eGet(rightEdgesFeature));


        // Add all inner names
        EStructuralFeature leftInnerNameFeature = left.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BINNERNAMES);
        EStructuralFeature rightInnerNameFeature = right.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BINNERNAMES);
        EList<EObject> innernames = (EList<EObject>) right.eGet(rightInnerNameFeature);
        ((EList<EObject>) left.eGet(leftInnerNameFeature)).addAll(innernames);

        // add all outer names
        EStructuralFeature leftOuterNamesFeature = left.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES);
        EStructuralFeature rightOuterNamesFeature = right.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES);
        ((EList<EObject>) left.eGet(leftOuterNamesFeature)).addAll((EList<EObject>) right.eGet(rightOuterNamesFeature));

        left.eAdapters().clear();
        right.eAdapters().clear();

        PureBigraph bigraph = PureBigraphBuilder.create(g.getSignature(), ((PureBigraph) copy).getMetaModel(), ((PureBigraph) copy).getInstanceModel()).createBigraph();
        return new PureBigraphComposite<>((Bigraph<S>) bigraph);
    }

    @Override
    public BigraphComposite<S> merge(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        Bigraph<S> g = BigraphUtil.copyIfSame(getBigraphDelegate(), f);
        assertSignaturesAreSame(g, f);

        BigraphComposite<DefaultDynamicSignature> bigraphComposite = ops((Bigraph<DefaultDynamicSignature>) g).parallelProduct((Bigraph<DefaultDynamicSignature>) f);
        if (isLinking(f)) {
            if (g.getRoots().size() > 0) {
                return (BigraphComposite<S>) bigraphComposite;
            }
            Placings<DefaultDynamicSignature>.Barren barren = purePlacings((DefaultDynamicSignature) getSignature()).barren();
            return (BigraphComposite<S>) ops(barren).parallelProduct(bigraphComposite);

        } else {
            Placings<DefaultDynamicSignature>.Merge merge = purePlacings((DefaultDynamicSignature) getSignature()).merge(bigraphComposite.getOuterBigraph().getRoots().size());
            BigraphComposite<DefaultDynamicSignature> compose = ops(merge).nesting(bigraphComposite);
            return (BigraphComposite<S>) compose;
        }
    }

    @Override
    public BigraphComposite<S> merge(BigraphComposite<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        return merge((Bigraph<S>) f.getOuterBigraph());
    }

    public BigraphComposite<S> compose(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        Bigraph<S> g = getBigraphDelegate();
        assertSignaturesAreSame(g, f);
        assertInterfaceCompatibleForCompose(g, f, true);

        Supplier<String> rewriteNameSupplier = createNameSupplier("v");
        Supplier<String> rewriteEdgeNameSupplier = createNameSupplier("e", g.getEdges().size());
        Supplier<Integer> rewriteSiteIndexSupplier = createNameSupplier(0); //g.getSites().size());

        // if we don't copyOuter, both bigraph get 'destroyed'
        Bigraph<S> copyOuter = BigraphUtil.copy(g);
        Bigraph<S> copyInner = BigraphUtil.copy(f);

        int copyInnerRootSize = copyInner.getRoots().size();
        // Auto-infer the identity link graph for composition when there is no ambiguity - this is just a convenience feature
        // when the outer bigraph is an elementary bigraph
        if (BigraphUtil.isBigraphElementaryPlacing(copyOuter) && copyInner.getOuterNames().size() > 0) {
            Linkings<S>.Identity identity = pureLinkings(copyOuter.getSignature()).identity(
                    copyInner.getOuterNames().stream().map(x -> StringTypedName.of(x.getName()))
                            .map(NamedType.class::cast).toArray(NamedType<?>[]::new)
            );
            copyOuter = ops(copyOuter).juxtapose(identity).getOuterBigraph();
        } else if (BigraphUtil.isBigraphElementaryLinking(copyOuter) && copyInnerRootSize > 0) {
            Placings<S>.Permutation permutation = purePlacings(copyOuter.getSignature()).permutation(copyInnerRootSize);
            copyOuter = ops(copyOuter).juxtapose(permutation).getOuterBigraph();
        }

        EObject leftOuter = ((PureBigraph) copyOuter).getInstanceModel();
        EObject rightInner = ((PureBigraph) copyInner).getInstanceModel();


        EStructuralFeature rightOuterNamesFeature = rightInner.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES);
        MutableMap<Integer, EObject> rootsInnerIndex = Maps.mutable.empty(); //new HashMap<>();
        MutableMap<String, EObject> rightOuterNamesIndex = Maps.mutable.empty(); //new HashMap<>();
        if (Objects.nonNull(rightInner.eGet(rightOuterNamesFeature))) {
            EList<EObject> outernames = (EList<EObject>) rightInner.eGet(rightOuterNamesFeature);
            for (EObject eachOuterName : outernames) {
                EAttribute nameAttr = EMFUtils.findAttribute(eachOuterName.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                rightOuterNamesIndex.put((String) eachOuterName.eGet(nameAttr), eachOuterName);
            }
        }

        TreeIterator<Object> allContentsInner = EcoreUtil.getAllContents(rightInner, true);
        while (allContentsInner.hasNext()) {
            EObject next = (EObject) allContentsInner.next();
            if (((EcoreBigraph) copyInner).isBRoot(next)) {
                EAttribute indexAttr = EMFUtils.findAttribute(next.eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
                Integer index = (Integer) next.eGet(indexAttr);
                rootsInnerIndex.put(index, next);
                if (rootsInnerIndex.size() == copyInnerRootSize) {
                    break;
                }
            }
        }

        EStructuralFeature leftEdgesFeature = leftOuter.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BEDGES);
        EStructuralFeature rightEdgesFeature = rightInner.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BEDGES);

        TreeIterator<Object> allContents = EcoreUtil.getAllContents(leftOuter, true);
        while (allContents.hasNext()) {
            EObject next = (EObject) allContents.next();
            if (((EcoreBigraph) copyOuter).isNameable(next)) {
                if (((EcoreBigraph) copyOuter).isBPlace(next)) {
                    EAttribute nameAttr = EMFUtils.findAttribute(next.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                    next.eSet(nameAttr, rewriteNameSupplier.get());
                }
            }

            if (((EcoreBigraph) copyOuter).isBInnerName(next)) {
                EStructuralFeature innerBLinksRef = next.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                EObject theNewLink = (EObject) next.eGet(innerBLinksRef);
                if ((theNewLink) == null) { // otherwise the innername is a closure
                    // the current element must be a closure if no link is connected. So we should create a new edge
                    EClassifier edgeClass = next.eClass().getEPackage().getEClassifier(BigraphMetaModelConstants.CLASS_EDGE);
                    theNewLink = next.eClass().getEPackage().getEFactoryInstance().create((EClass) edgeClass);
                    EAttribute nameAttr = EMFUtils.findAttribute(next.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                    theNewLink.eSet(nameAttr, rewriteEdgeNameSupplier.get());
                    ((EList<EObject>) leftOuter.eGet(leftEdgesFeature)).add(theNewLink); // Important to add the edge to the bigraph
                }
                EAttribute nameAttr = EMFUtils.findAttribute(next.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                EObject outerName = rightOuterNamesIndex.get((String) next.eGet(nameAttr));
                if (Objects.nonNull(outerName)) {
                    EStructuralFeature pointsRef = outerName.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
                    if (Objects.nonNull(outerName.eGet(pointsRef))) {
                        EList<EObject> points = (EList<EObject>) outerName.eGet(pointsRef);
                        for (int i = points.size() - 1; i >= 0; i--) {
                            EObject eachPoint = points.get(i);
                            EStructuralFeature bLinkRef = eachPoint.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                            eachPoint.eSet(bLinkRef, theNewLink);
                        }
                    }
                    // last: delete inner of leftOuter
                    EStructuralFeature pointsRefInner = theNewLink.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
                    if (Objects.nonNull(theNewLink.eGet(pointsRefInner))) {
                        ((EList<EObject>) theNewLink.eGet(pointsRefInner)).remove(next);
                    }
                    rightOuterNamesIndex.remove((String) next.eGet(nameAttr));
                }
                continue;
            }

            if (((EcoreBigraph) copyOuter).isBSite(next)) {
                EAttribute indexAttr = EMFUtils.findAttribute(next.eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
                Integer index = (Integer) next.eGet(indexAttr);
                // retrieve corresponding parent from 'inner bigraph'
                EObject rootOuter = rootsInnerIndex.get(index);
                renameContentsRecursively((EcoreBigraph) copyInner, rootOuter, rewriteNameSupplier);
            }
        }

        MutableList<BigraphEntity.SiteEntity> leftSites = Lists.mutable.ofAll(copyOuter.getSites()); // new ArrayList<>(copyOuter.getSites());
        for (int k = leftSites.size() - 1; k >= 0; k--) {
            EObject nextSite = leftSites.get(k).getInstance();
            EAttribute indexAttr = EMFUtils.findAttribute(nextSite.eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
            Integer index = (Integer) nextSite.eGet(indexAttr);
            EStructuralFeature prntRef = nextSite.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
            EObject parentNodeLeft = (EObject) nextSite.eGet(prntRef);
            // retrieve corresponding parent from 'outer bigraph'
            EObject rootInner = rootsInnerIndex.get(index);
            EStructuralFeature childRefInner = rootInner.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
            EList<EObject> childsInner = (EList<EObject>) rootInner.eGet(childRefInner);
            for (int i = childsInner.size() - 1; i >= 0; i--) {
                EObject eachInnerChild = childsInner.get(i);
                EStructuralFeature prntRefInner = eachInnerChild.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
                if (prntRefInner != null)
                    eachInnerChild.eSet(prntRefInner, null);
                BigraphUtil.setParentOfNode(eachInnerChild, parentNodeLeft);
            }

            // finally: remove the site node from the 'outer bigraph'
            EStructuralFeature childRef = parentNodeLeft.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
            ((EList) parentNodeLeft.eGet(childRef)).remove(nextSite);
        }

//        leftOuter.eAdapters().add(adapter2); // for simple name rewriting of the 'outer bigraphs' edges

        if (Objects.nonNull(rightInner.eGet(rightEdgesFeature))) {
            EContentAdapter adapter2 = new EContentAdapter() {
                public void notifyChanged(Notification notification) {
                    if (notification.getFeature() == rightEdgesFeature) {
                        MutableList<EObject> edges = Lists.mutable.empty(); //new ArrayList<>();
                        if (notification.getNewValue() instanceof Collection) {
                            edges.addAll((Collection) notification.getNewValue());
                        } else {
                            edges.add((EObject) notification.getNewValue());
                        }
                        for (EObject eachEdge : edges) {
                            EAttribute nameAttr = EMFUtils.findAttribute(eachEdge.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                            eachEdge.eSet(nameAttr, rewriteEdgeNameSupplier.get());
                        }
                    }
                }
            };

            leftOuter.eAdapters().add(adapter2); // for simple name rewriting of the 'outer bigraphs' edges
            EList<EObject> edges = (EList<EObject>) rightInner.eGet(rightEdgesFeature);
            ((EList<EObject>) leftOuter.eGet(leftEdgesFeature)).addAll(edges);
        }

        // no inner names should be leftOuter from the 'outer bigraph'
        EStructuralFeature leftInnerNameFeature = leftOuter.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BINNERNAMES);
        EStructuralFeature rightInnerNameFeature = rightInner.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BINNERNAMES);
        ((EList<EObject>) leftOuter.eGet(leftInnerNameFeature)).clear();
        if (Objects.nonNull(rightInner.eGet(rightInnerNameFeature))) {
            EList<EObject> innernames = (EList<EObject>) rightInner.eGet(rightInnerNameFeature);
            ((EList<EObject>) leftOuter.eGet(leftInnerNameFeature)).addAll(innernames);
        }

        // remaining outer names of rightInner
        EStructuralFeature leftOuterNamesFeature = leftOuter.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES);
        if (Objects.nonNull(leftOuter.eGet(leftOuterNamesFeature))) {
            // Add only those outer names of rightInner that are not in leftOuter.
            // for these we reattach the links
            Map<String, BigraphEntity.OuterName> leftOuterNames = copyOuter.getOuterNames().stream().collect(Collectors.toMap(BigraphEntity.Link::getName, p -> p));

            Iterator<Map.Entry<String, EObject>> iterator = rightOuterNamesIndex.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, EObject> outerNameRight = iterator.next();
                String name = outerNameRight.getKey();
                if (leftOuterNames.get(name) != null) {
                    BigraphEntity.OuterName outerLeft = leftOuterNames.get(name);
                    EStructuralFeature bPoints = outerNameRight.getValue().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
                    if (bPoints != null) {
                        EList<EObject> points = (EList<EObject>) outerNameRight.getValue().eGet(bPoints);
                        for (int i = points.size() - 1; i >= 0; i--) {
                            EObject eachPoint = points.get(i);
                            EStructuralFeature bLink = eachPoint.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                            eachPoint.eSet(bLink, outerLeft.getInstance());
                        }
                    }
                    iterator.remove();
                }
            }
            ((EList<EObject>) leftOuter.eGet(leftOuterNamesFeature)).addAll(rightOuterNamesIndex.values());
        }

        leftOuter.eAdapters().clear();
        rightInner.eAdapters().clear();
        PureBigraph bigraph = PureBigraphBuilder.create(copyOuter.getSignature(), ((PureBigraph) copyOuter).getMetaModel(), ((PureBigraph) copyOuter).getInstanceModel()).createBigraph();
        return new PureBigraphComposite<>((Bigraph<S>) bigraph);
    }

    private void renameContentsRecursively(EcoreBigraph bigraph, EObject node, Supplier<String> nameSupplier) {
        if (bigraph.isNameable(node)) {
            if (bigraph.isBPlace(node)) {
                EAttribute nameAttr = EMFUtils.findAttribute(node.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                node.eSet(nameAttr, nameSupplier.get());
            }
        }
        for (EObject next1 : node.eContents()) {
            renameContentsRecursively(bigraph, next1, nameSupplier);
        }
    }

    protected void assertSignaturesAreSame(Bigraph<S> outer, Bigraph<S> inner) throws IncompatibleSignatureException {
        //special handling if one is an elementary bigraph
        if (inner instanceof DiscreteIon || outer instanceof DiscreteIon) {
            DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
            inner.getSignature().getControls().forEach(x -> {
                signatureBuilder.addControl((Control) x);
            });
            outer.getSignature().getControls().forEach(x -> {
                signatureBuilder.addControl((Control) x);
            });
            if (outer instanceof EcoreBigraph) {
                builder = (MutableBuilder<S>) MutableBuilder.newMutableBuilder(signatureBuilder.create(), ((EcoreBigraph) outer).getEMetaModelData());
            } else {
                builder = (MutableBuilder<S>) MutableBuilder.newMutableBuilder(signatureBuilder.create());
            }
            return;
        }
        if (isLinking(outer) || isPlacing(outer)) {
//            builder = PureBigraphBuilder.newMutableBuilder(inner.getSignature(), ((EcoreBigraph) outer).getEMetaModelData());
            if (outer instanceof EcoreBigraph) {
                builder = MutableBuilder.newMutableBuilder(inner.getSignature(), ((EcoreBigraph) outer).getEMetaModelData());
            } else {
                builder = MutableBuilder.newMutableBuilder(inner.getSignature());
            }
            return;
        } else if (isLinking(inner) || isPlacing(inner)) {
//            builder = PureBigraphBuilder.newMutableBuilder(outer.getSignature(), ((EcoreBigraph) inner).getEMetaModelData());
            if (inner instanceof EcoreBigraph) {
                builder = MutableBuilder.newMutableBuilder(outer.getSignature(), ((EcoreBigraph) inner).getEMetaModelData());
            } else {
                builder = MutableBuilder.newMutableBuilder(outer.getSignature());
            }
            return;
        }
        if (!outer.getSignature().equals(inner.getSignature())) {
            throw new IncompatibleSignatureException();
        }
    }

    @Override
    public EPackage getMetaModel() {
        return ((EcoreBigraph) getBigraphDelegate()).getMetaModel();
    }

    @Override
    public EObject getInstanceModel() {
        return ((EcoreBigraph) getBigraphDelegate()).getInstanceModel();
    }
}
