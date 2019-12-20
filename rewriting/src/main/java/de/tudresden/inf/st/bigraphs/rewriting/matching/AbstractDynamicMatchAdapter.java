package de.tudresden.inf.st.bigraphs.rewriting.matching;

import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import java.util.*;
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
public abstract class AbstractDynamicMatchAdapter<B extends Bigraph<? extends Signature>> extends BigraphDelegator<Signature> {

    @SuppressWarnings("unchecked")
    public AbstractDynamicMatchAdapter(Bigraph<? extends Signature> bigraph) {
        super((Bigraph<Signature>) bigraph);
    }

    @SuppressWarnings("unchecked")
    @Override
    public B getBigraphDelegate() {
        return (B) super.getBigraphDelegate();
    }

    @Override
    public List<BigraphEntity.RootEntity> getRoots() {
        return org.eclipse.collections.api.factory.Lists.mutable.ofAll(super.getRoots());
//        return new ArrayList<>(super.getRoots());
    }

    /**
     * <b>Note:</b> Only the port indices are important for the order, not the name itself.
     *
     * @param node the node
     * @return a list of all links connected to the given node
     */
    public abstract AbstractSequentialList<ControlLinkPair> getLinksOfNode(BigraphEntity node);

    /**
     * Data structure to represent a pair
     */
    public static class ControlLinkPair {
        Control control;
        BigraphEntity link;

        public ControlLinkPair(Control control, BigraphEntity link) {
            this.control = control;
            this.link = link;
        }

        public Control getControl() {
            return control;
        }

        public BigraphEntity getLink() {
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

    public List<BigraphEntity> getSubtreeOfNode(BigraphEntity node) {
        Traverser<BigraphEntity> stringTraverser = Traverser.forTree(this::getChildren);
//        Iterable<BigraphEntity> v0 = stringTraverser.depthFirstPostOrder(node);
        MutableList<BigraphEntity> bigraphEntities = org.eclipse.collections.api.factory.Lists.mutable
                .ofAll(stringTraverser.depthFirstPostOrder(node));
//        ArrayList<BigraphEntity> bigraphEntities = Lists.newArrayList(v0);
        bigraphEntities.remove(node);
        return bigraphEntities;
    }

    public List<BigraphEntity> getNodesOfLink(BigraphEntity.Link outerName) {
        EObject instance = outerName.getInstance();
//        List<BigraphEntity> linkedNodes = new ArrayList<>();
        MutableList<BigraphEntity> linkedNodes = org.eclipse.collections.api.factory.Lists.mutable.empty();
        EStructuralFeature pointsRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
        if (Objects.isNull(pointsRef)) return linkedNodes;
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
    public int degreeOf(BigraphEntity nodeEntity) {
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

    /**
     * Returns all siblings of the current node of the current bigraph. The node itself is not included.
     *
     * @param node the node whoms sibling should be returned
     * @return siblings of {@code node}
     */
    @Deprecated //check if needed, whats the difference to PureBigraph's impl?
    public List<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
        if (!isBPlace(node.getInstance())) return new ArrayList<>();
        EObject instance = node.getInstance();
//        List<BigraphEntity> siblings = new ArrayList<>();
        MutableList<BigraphEntity> siblings = org.eclipse.collections.api.factory.Lists.mutable.empty();
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (Objects.nonNull(prntRef) && Objects.nonNull(instance.eGet(prntRef))) {
            EObject each = (EObject) instance.eGet(prntRef);
            //get all childs
            EStructuralFeature childRef = each.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
            @SuppressWarnings("unchecked")
            EList<EObject> childs = (EList<EObject>) each.eGet(childRef);
            assert childs != null;
            for (EObject eachChild : childs) {
                if (node.getInstance().equals(eachChild)) continue;
                addPlaceToList(siblings, eachChild, true);
            }
        }
        return siblings;
    }

    protected List<BigraphEntity> neighborhoodHook(List<BigraphEntity> neighbors, BigraphEntity node) {
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
    protected void addPlaceToList(final List<BigraphEntity> list, final EObject each, boolean withSites) {
        try {

            if (isBNode(each)) {
                list.add(
                        getNodes().stream()
                                .filter(x -> x.getInstance().equals(each))
                                .findFirst()
                                .orElseThrow(throwableSupplier)
                );
            } else if (isRoot(each)) {
                list.add(
                        getRoots().stream()
                                .filter(x -> x.getInstance().equals(each))
                                .findFirst()
                                .orElseThrow(throwableSupplier)
                );
            }
            if (withSites && isSite(each)) {
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
    public List<BigraphEntity> getOpenNeighborhoodOfVertex(BigraphEntity node) {
        MutableList<BigraphEntity> neighbors = org.eclipse.collections.api.factory.Lists.mutable.empty();
        neighborhoodHook(neighbors, node);
//        List<BigraphEntity> neighbors2 = new ArrayList<>();
//        neighbors2 = neighborhoodHook(neighbors2, node);
//        assert neighbors.size() == neighbors2.size();
        return neighbors;
    }

    /**
     * Get all vertices (roots and nodes) without sites.
     *
     * @return all vertices of the bigraph without sites
     */
    public List<BigraphEntity> getAllVertices() {
        return org.eclipse.collections.api.factory.Lists.fixedSize.fromStream(Streams.concat(getNodes().stream(), getRoots().stream()));
//        List<BigraphEntity> allNodes = new ArrayList<>(getNodes().size() + getRoots().size());
//        allNodes.addAll(getNodes());
//        allNodes.addAll(getRoots());
//        return allNodes;
    }

    /**
     * Internal vertices have children
     *
     * @return
     */
    public List<BigraphEntity> getAllInternalVerticesPostOrder() {
        return org.eclipse.collections.api.factory.Lists.mutable.ofAll(getAllVerticesPostOrder())
                .select(x -> getChildren(x).size() > 0);
//        Iterable<BigraphEntity> allVerticesPostOrder = getAllVerticesPostOrder();
//        List<BigraphEntity> collect = StreamSupport.stream(allVerticesPostOrder.spliterator(), false)
//                .filter(x -> getChildren(x).size() > 0)
//                .collect(Collectors.toList());
//        return collect;
    }

    public Stream<BigraphEntity> getAllInternalVerticesPostOrderAsStream() {
        Iterable<BigraphEntity> allVerticesPostOrder = getAllVerticesPostOrder();
        return StreamSupport.stream(allVerticesPostOrder.spliterator(), false)
                .filter(x -> getChildren(x).size() > 0);
    }

    public Iterable<BigraphEntity> getAllVerticesPostOrder() {
        final MutableList<BigraphEntity> allVerticesPostOrder = org.eclipse.collections.api.factory.Lists.mutable.empty();
        getRoots().forEach(eachRoot -> {
            Traverser<BigraphEntity> stringTraverser = Traverser.forTree(node -> getChildren(node));
            allVerticesPostOrder.addAllIterable(stringTraverser.depthFirstPostOrder(eachRoot));
        });
//        for (BigraphEntity eachRoot : getRoots()) {
//        }
        return allVerticesPostOrder;
//        Collection<BigraphEntity> allVerticesPostOrder = new ArrayList<>();
//        for (BigraphEntity eachRoot : getBigraphDelegate().getRoots()) {
//            Traverser<BigraphEntity> stringTraverser = Traverser.forTree(node -> getChildren(node));
//            Iterable<BigraphEntity> v0 = stringTraverser.depthFirstPostOrder(eachRoot);
//            allVerticesPostOrder.addAll(Lists.newArrayList(v0));
//        }
//        return allVerticesPostOrder;
    }

    public Iterable<BigraphEntity> getAllVerticesBfsOrder() {
        MutableList<BigraphEntity> allVerticesBfsOrder = org.eclipse.collections.api.factory.Lists.mutable.empty();
        getBigraphDelegate().getRoots().stream().sorted(Comparator.comparingInt(BigraphEntity.RootEntity::getIndex))
                .forEachOrdered(eachRoot -> allVerticesBfsOrder.addAll(getAllVerticesBfsOrderFrom(eachRoot)));
//        Collection<BigraphEntity> allVerticesBfsOrder = new ArrayList<>();
//        for (BigraphEntity eachRoot : getBigraphDelegate().getRoots()) {
//            allVerticesBfsOrder.addAll(getAllVerticesBfsOrderFrom(eachRoot));
//        }
        return allVerticesBfsOrder;
    }

    public List<BigraphEntity> getAllVerticesBfsOrderFrom(BigraphEntity eachRoot) {
        Traverser<BigraphEntity> stringTraverser = Traverser.forTree(node -> getChildren(node));
//        Iterable<BigraphEntity> v0 = stringTraverser.breadthFirst(eachRoot);
        return org.eclipse.collections.api.factory.Lists.fixedSize.ofAll(stringTraverser.breadthFirst(eachRoot));
//        return new ArrayList<>(Lists.newArrayList(v0));
    }

    /**
     * Get all children of a bigraph node precluding all sites.
     * This method is used solely for the matching algorithm.
     *
     * @param node the node
     * @return all children of the given node
     */
    public List<BigraphEntity> getChildren(BigraphEntity node) {
        return getBigraphDelegate().getChildrenOf(node)
                .stream()
                .filter(x -> !BigraphEntityType.isSite(x)).collect(Collectors.toList());
    }

    /**
     * Get all leaves of a the bigraph's place graph (i.e., a tree).
     * This method is used solely for the matching algorithm.
     *
     * @return all leaves of the place graph
     */
    public List<BigraphEntity> getAllLeaves() {
        final MutableList<BigraphEntity> leaves = org.eclipse.collections.api.factory.Lists.mutable.empty();
        this.getAllVertices().forEach(each -> {
            if (degreeOf(each) <= 1 && !isRoot(each.getInstance())) {
                leaves.add(each);
            }
        });
//        List<BigraphEntity> leaves = new ArrayList<>();
//        for (BigraphEntity each : this.getAllVertices()) {
//            if (degreeOf(each) <= 1 && !isRoot(each.getInstance())) {
//                leaves.add(each);
//            }
//        }
        return leaves;
    }

    public boolean isOuterName(EObject eObject) {
        return eObject.eClass().getClassifierID() ==
                (((EPackageImpl) getBigraphDelegate().getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_OUTERNAME)).getClassifierID() ||
                eObject.eClass().equals(((EPackageImpl) this.getBigraphDelegate().getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_OUTERNAME)) ||
                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) this.getBigraphDelegate().getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_OUTERNAME));
    }

    public boolean isBPlace(EObject eObject) {
        return eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) this.getBigraphDelegate().getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_PLACE));
    }

    protected boolean isBEdge(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_EDGE);
    }

    //works only for elements of the calling class
    protected boolean isOfEClass(EObject eObject, String eClassifier) {
        return eObject.eClass().equals(((EPackageImpl) getModelPackage()).getEClassifierGen(eClassifier)) ||
                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) getModelPackage()).getEClassifierGen(eClassifier));
    }

    public boolean isBPort(EObject eObject) {
        return eObject.eClass().getClassifierID() ==
                (((EPackageImpl) this.getBigraphDelegate().getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_PORT)).getClassifierID() ||
                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) this.getBigraphDelegate().getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_PORT));
    }

    public boolean isBNode(EObject eObject) {
        return eObject.eClass().equals(((EPackageImpl) this.getBigraphDelegate().getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_NODE)) ||
                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) this.getBigraphDelegate().getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_NODE));
    }

    public boolean isRoot(EObject eObject) {
        return eObject.eClass().equals(((EPackageImpl) this.getBigraphDelegate().getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_ROOT)) ||
                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) this.getBigraphDelegate().getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_ROOT));
    }

    public boolean isSite(EObject eObject) {
        return eObject.eClass().equals(((EPackageImpl) this.getBigraphDelegate().getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_SITE)) ||
                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) this.getBigraphDelegate().getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_SITE));
    }


}
