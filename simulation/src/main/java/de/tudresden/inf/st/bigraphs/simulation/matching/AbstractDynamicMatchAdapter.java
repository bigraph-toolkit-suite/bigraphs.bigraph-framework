package de.tudresden.inf.st.bigraphs.simulation.matching;

import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.EcoreBigraph;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.AbstractSequentialList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * An adapter for bigraphs used for the matching procedure.
 * <p>
 * Encapsulates a bigraph with a dynamic signature and provides different accessor methods
 * for the underlying bigraph which are used/needed for the matching algorithm
 *
 * @author Dominik Grzelak
 */
//S extends AbstractEcoreSignature<? extends Control<?, ?>>
public abstract class AbstractDynamicMatchAdapter<S extends AbstractEcoreSignature<? extends Control<?, ?>>, B extends Bigraph<S> & EcoreBigraph<S>>
        extends BigraphDelegator<S> implements EcoreBigraph<S> {

    //    @SuppressWarnings("unchecked")
    public AbstractDynamicMatchAdapter(B bigraph) {
        super(bigraph);
    }

    @SuppressWarnings("unchecked")
    @Override
    public B getBigraphDelegate() {
        return (B) super.getBigraphDelegate();
    }

    @Override
    public List<BigraphEntity.RootEntity> getRoots() {
        return org.eclipse.collections.api.factory.Lists.mutable.ofAll(super.getRoots());
    }

    @Override
    public EPackage getModelPackage() {
        return getBigraphDelegate().getModelPackage();
    }

    @Override
    public EObject getModel() {
        return getBigraphDelegate().getModel();
    }

    public void clearCache() {

    }

    /**
     * <b>Note:</b> Only the port indices are important for the order, not the name itself.
     *
     * @param node the node
     * @return a list of all links connected to the given node
     */
    public abstract AbstractSequentialList<ControlLinkPair> getLinksOfNode(BigraphEntity<?> node);

    /**
     * Data structure to represent a pair
     */
    public static class ControlLinkPair {
        Control<?, ?> control;
        BigraphEntity.Link link;

        public ControlLinkPair(Control<?, ?> control, BigraphEntity.Link link) {
            this.control = control;
            this.link = link;
        }

        public Control<?, ?> getControl() {
            return control;
        }

        public BigraphEntity.Link getLink() {
            return link;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ControlLinkPair)) return false;
            ControlLinkPair that = (ControlLinkPair) o;
            return control.equals(that.control) &&
                    link.equals(that.link);
        }

        @Override
        public int hashCode() {
            return Objects.hash(control, link);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public List<BigraphEntity<?>> getSubtreeOfNode(BigraphEntity<?> node) {
        Traverser<BigraphEntity<?>> stringTraverser = Traverser.forTree(this::getChildren);
        MutableList<BigraphEntity<?>> bigraphEntities = org.eclipse.collections.api.factory.Lists.mutable
                .ofAll(stringTraverser.depthFirstPostOrder(node));
        bigraphEntities.remove(node);
        return bigraphEntities;
    }

    public List<BigraphEntity<?>> getNodesOfLink(BigraphEntity.Link outerName) {
        EObject instance = outerName.getInstance();
//        List<BigraphEntity> linkedNodes = new ArrayList<>();
        MutableList<BigraphEntity<?>> linkedNodes = org.eclipse.collections.api.factory.Lists.mutable.empty();
        EStructuralFeature pointsRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
        if ((pointsRef) == null) return linkedNodes;
        @SuppressWarnings("unchecked")
        EList<EObject> pointsList = (EList<EObject>) instance.eGet(pointsRef);
        for (EObject eachPoint : pointsList) {
            if (isBPort(eachPoint)) {
                EStructuralFeature nodeRef = eachPoint.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_NODE);
                EObject node = (EObject) eachPoint.eGet(nodeRef);
                addPlaceToList(linkedNodes, node, false);
            }
        }
        return linkedNodes;
    }

    /**
     * Get the number of all in- and out-going edges of a node within the place graph. <br>
     * Sites are included in the count.
     *
     * @param nodeEntity the node
     * @return degree of the node
     */
    public int degreeOf(BigraphEntity<?> nodeEntity) {
        //get all edges
        EObject instance = nodeEntity.getInstance();
        int cnt = 0;
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        if (Objects.nonNull(chldRef)) {
            @SuppressWarnings("unchecked")
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            cnt += childs.size();
        }
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (Objects.nonNull(prntRef) && Objects.nonNull(instance.eGet(prntRef))) {
            cnt++;
        }
        return cnt;
    }

    protected List<BigraphEntity<?>> neighborhoodHook(List<BigraphEntity<?>> neighbors, BigraphEntity<?> node) {
        EObject instance = node.getInstance();
        // first check the children of the node
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        if (Objects.nonNull(chldRef)) {
            @SuppressWarnings("unchecked")
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            for (EObject each : childs) {
                addPlaceToList(neighbors, each, false);
            }
        }
        // second, the parent
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (Objects.nonNull(prntRef) && Objects.nonNull(instance.eGet(prntRef))) {
            final EObject each = (EObject) instance.eGet(prntRef);
            addPlaceToList(neighbors, each, false);
        }
        return neighbors;
    }

    /**
     * Convenient method that finds the corresponding 'node type' (e.g., root) of a given {@link EObject} instance and
     * adds it to the given list {@code list}.
     * <p>
     * Throws a runtime exception of the node couldn't be found.
     *
     * @param list      the list
     * @param each      node entity (e.g., root, node or site)
     * @param withSites flag to consider sites or not
     */
    protected void addPlaceToList(final List<BigraphEntity<?>> list, final EObject each, boolean withSites) {
        try {

            if (isBNode(each)) {
                list.add(
                        getNodes().stream()
                                .filter(x -> x.getInstance().equals(each))
                                .findFirst()
                                .orElseThrow(throwableSupplier)
                );
            } else if (isBRoot(each)) {
                list.add(
                        getRoots().stream()
                                .filter(x -> x.getInstance().equals(each))
                                .findFirst()
                                .orElseThrow(throwableSupplier)
                );
            }
            if (withSites && isBSite(each)) {
                list.add(
                        getSites().stream()
                                .filter(x -> x.getInstance().equals(each))
                                .findFirst()
                                .orElseThrow(throwableSupplier)
                );
            }
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    protected Supplier<Throwable> throwableSupplier = new Supplier<Throwable>() {
        @Override
        public Throwable get() {
            return new RuntimeException("Node couldn't be found in the node set of the bigraph.");
        }
    };


    /**
     * This method is used solely for the matching algorithm. Sites are excluded in the list.
     * Doesn't include the node itself. Opposite is the closed neighborhood which includes the node itself.
     *
     * @param node the node
     * @return
     */
    public MutableList<BigraphEntity<?>> getOpenNeighborhoodOfVertex(BigraphEntity<?> node) {
        MutableList<BigraphEntity<?>> neighbors = org.eclipse.collections.api.factory.Lists.mutable.empty();
        neighborhoodHook(neighbors, node);
        return neighbors;
    }

    /**
     * Get all vertices (roots and nodes) without sites.
     *
     * @return all vertices of the bigraph without sites
     */
    public ImmutableList<BigraphEntity<?>> getAllVertices() {
        return Lists.immutable.fromStream(
                Streams.concat(
                        getNodes().stream().map(x -> (BigraphEntity<?>) x),
                        getRoots().stream().map(x -> (BigraphEntity<?>) x)
                )
        );
    }

    /**
     * Internal vertices have children
     *
     * @return
     */
    public ImmutableList<BigraphEntity<?>> getAllInternalVerticesPostOrder() {
        return Lists.immutable.ofAll(getAllVerticesPostOrder())
                .select(x -> getChildren(x).size() > 0);
    }

    public Stream<BigraphEntity<?>> getAllInternalVerticesPostOrderAsStream() {
        Iterable<BigraphEntity<?>> allVerticesPostOrder = getAllVerticesPostOrder();
        return StreamSupport.stream(allVerticesPostOrder.spliterator(), false)
                .filter(x -> getChildren(x).size() > 0);
    }

    @SuppressWarnings("UnstableApiUsage")
    public Iterable<BigraphEntity<?>> getAllVerticesPostOrder() {
        final MutableList<BigraphEntity<?>> allVerticesPostOrder = org.eclipse.collections.api.factory.Lists.mutable.empty();
        getRoots().forEach(eachRoot -> {
            Traverser<BigraphEntity<?>> stringTraverser = Traverser.forTree(this::getChildren);
            allVerticesPostOrder.addAllIterable(stringTraverser.depthFirstPostOrder(eachRoot));
        });
        return allVerticesPostOrder;
    }

    public Iterable<BigraphEntity<?>> getAllVerticesBfsOrder() {
        MutableList<BigraphEntity<?>> allVerticesBfsOrder = org.eclipse.collections.api.factory.Lists.mutable.empty();
        getBigraphDelegate().getRoots().stream().sorted(Comparator.comparingInt(BigraphEntity.RootEntity::getIndex))
                .forEachOrdered(eachRoot -> allVerticesBfsOrder.addAll(getAllVerticesBfsOrderFrom(eachRoot)));
        return allVerticesBfsOrder;
    }

    @SuppressWarnings("UnstableApiUsage")
    public Stream<BigraphEntity<?>> getAllVerticesBfsOrderStream() {
        Traverser<BigraphEntity<?>> stringTraverser = Traverser.forTree(this::getChildren);
        return StreamSupport.stream(stringTraverser.breadthFirst(getRoots().get(0)).spliterator(), false);
    }

    @SuppressWarnings("UnstableApiUsage")
    public List<BigraphEntity<?>> getAllVerticesBfsOrderFrom(BigraphEntity<?> eachRoot) {
        Traverser<BigraphEntity<?>> stringTraverser = Traverser.forTree(this::getChildren);
        return org.eclipse.collections.api.factory.Lists.fixedSize.ofAll(stringTraverser.breadthFirst(eachRoot));
    }

    /**
     * Get all children of a bigraph node precluding all sites.
     * This method is used solely for the matching algorithm.
     *
     * @param node the node
     * @return all children of the given node
     */
    public List<BigraphEntity<?>> getChildren(BigraphEntity<?> node) {
        MutableList<BigraphEntity<?>> childrenWithoutSites = Lists.mutable.empty();
        for (BigraphEntity<?> x : getBigraphDelegate().getChildrenOf(node)) {
            if (!BigraphEntityType.isSite(x)) {
                childrenWithoutSites.add(x);
            }
        }
        return childrenWithoutSites;
//        return getBigraphDelegate().getChildrenOf(node)
//                .stream()
//                .filter(x -> !BigraphEntityType.isSite(x)).collect(Collectors.toList());
    }

    /**
     * Get all leaves of a the bigraph's place graph (i.e., a tree).
     * This method is used solely for the matching algorithm.
     *
     * @return all leaves of the place graph
     */
    public List<BigraphEntity<?>> getAllLeaves() {
        final MutableList<BigraphEntity<?>> leaves = org.eclipse.collections.api.factory.Lists.mutable.empty();
        this.getAllVertices().forEach(each -> {
            if (degreeOf(each) <= 1 && !isBRoot(each.getInstance())) {
                leaves.add(each);
            }
        });
        return leaves;
    }
}
