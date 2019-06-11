package de.tudresden.inf.st.bigraphs.rewriting.matching;

import com.google.common.collect.Lists;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


/**
 * Matcher für dynamic signatures only
 * <p>
 * encapsulates a bigraph with a dynamic signature and provides different accessor methods
 * for the underlying bigraph which are used/needed for the matching algorithm
 *
 * @author Dominik Grzelak
 */
public abstract class AbstractDynamicMatchAdapter<B extends Bigraph<DefaultDynamicSignature>> extends BigraphDelegator<DefaultDynamicSignature> {

    public AbstractDynamicMatchAdapter(Bigraph<DefaultDynamicSignature> bigraph) {
        super(bigraph);
    }

    @SuppressWarnings("unchecked")
    @Override
    public B getBigraphDelegate() {
        return super.getBigraphDelegate();
    }

    @Override
    public List<BigraphEntity.RootEntity> getRoots() {
        return new ArrayList<>(super.getRoots());
    }

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

    /**
     * <b>IMPORTANT</b> ONLY the port indices are important for the order not the name itself
     *
     * @param node
     * @return
     */
    public abstract AbstractSequentialList<ControlLinkPair> getLinksOfNode(BigraphEntity node);

    public List<BigraphEntity> getAllChildrenFromNode(BigraphEntity node) {
        Traverser<BigraphEntity> stringTraverser = Traverser.forTree(this::getChildren);
        Iterable<BigraphEntity> v0 = stringTraverser.depthFirstPostOrder(node);
        return Lists.newArrayList(v0);
    }

    public List<BigraphEntity> getNodesOfLink(BigraphEntity.OuterName outerName) {
        EObject instance = outerName.getInstance();
        List<BigraphEntity> linkedNodes = new ArrayList<>();
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
     * All in/out-going edges of a node within the place graph.
     * Sites are included in the count.
     *
     * @param nodeEntity
     * @return
     */
    public int degreeOf(BigraphEntity nodeEntity) {
        //get all edges
        EObject instance = nodeEntity.getInstance();
        int cnt = 0;
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        if (Objects.nonNull(chldRef)) {
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
    public List<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
        if (!isBPlace(node.getInstance())) return new ArrayList<>();
        EObject instance = node.getInstance();
        List<BigraphEntity> siblings = new ArrayList<>();
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (Objects.nonNull(prntRef) && Objects.nonNull(instance.eGet(prntRef))) {
            EObject each = (EObject) instance.eGet(prntRef);
            //get all childs
            EStructuralFeature childRef = each.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
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
     * Find the corresponding node of the given {@link EObject} instance (representing a root or node) and
     * add it to the given list.
     * <p>
     * Throws a runtime exception of the node couldn't be found.
     *
     * @param neighbors
     * @param each
     */
    protected void addPlaceToList(final List<BigraphEntity> neighbors, final EObject each, boolean withSites) {
        try {

            if (isBNode(each)) {
                neighbors.add(
                        getNodes().stream()
                                .filter(x -> x.getInstance().equals(each))
                                .findFirst()
                                .orElseThrow(throwableSupplier)
                );
            } else if (isRoot(each)) {
                neighbors.add(
                        getRoots().stream()
                                .filter(x -> x.getInstance().equals(each))
                                .findFirst()
                                .orElseThrow(throwableSupplier)
                );
            }
            if (withSites && isSite(each)) {
                neighbors.add(
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
     * This method is used solely for the matching algorithm.
     *
     * @param node
     * @return
     */
    public List<BigraphEntity> getOpenNeighborhoodOfVertex(BigraphEntity node) {
        List<BigraphEntity> neighbors = new ArrayList<>();
        neighbors = neighborhoodHook(neighbors, node); //TODO: do not create classes: get them from the entity map from the bigraph
        return neighbors;
    }

    /**
     * Get all vertices (roots and nodes) without sites.
     *
     * @return
     */
    public Collection<BigraphEntity> getAllVertices() {
        List<BigraphEntity> allNodes = new ArrayList<>(getNodes());
        allNodes.addAll(getRoots());
        return allNodes;
    }

    //TODO: root at last
    public List<BigraphEntity> getAllInternalVerticesPostOrder() {
        Iterable<BigraphEntity> allVerticesPostOrder = getAllVerticesPostOrder();
        List<BigraphEntity> collect = StreamSupport.stream(allVerticesPostOrder.spliterator(), false)
                .filter(x -> getChildren(x).size() > 0)
                .collect(Collectors.toList());
        return collect;
    }

    public Iterable<BigraphEntity> getAllVerticesPostOrder() {
        Collection<BigraphEntity> allVerticesPostOrder = new ArrayList<>();
        for (BigraphEntity eachRoot : getBigraphDelegate().getRoots()) {
            Traverser<BigraphEntity> stringTraverser = Traverser.forTree(node -> getChildren(node));
            Iterable<BigraphEntity> v0 = stringTraverser.depthFirstPostOrder(eachRoot);
            allVerticesPostOrder.addAll(Lists.newArrayList(v0));
        }
        return allVerticesPostOrder;
    }

    public Iterable<BigraphEntity> getAllVerticesBfsOrder() {
        Collection<BigraphEntity> allVerticesBfsOrder = new ArrayList<>();
        for (BigraphEntity eachRoot : getBigraphDelegate().getRoots()) {
            allVerticesBfsOrder.addAll(getAllVerticesBfsOrderFrom(eachRoot));
        }
        return allVerticesBfsOrder;
    }

    public Collection<BigraphEntity> getAllVerticesBfsOrderFrom(BigraphEntity eachRoot) {
        Collection<BigraphEntity> allVerticesBfsOrder = new ArrayList<>();
        Traverser<BigraphEntity> stringTraverser = Traverser.forTree(node -> getChildren(node));
        Iterable<BigraphEntity> v0 = stringTraverser.breadthFirst(eachRoot);
        allVerticesBfsOrder.addAll(Lists.newArrayList(v0));
        return allVerticesBfsOrder;
    }

    /**
     * Get all children of a bigraph node precluding all sites.
     * This method is used solely for the matching algorithm.
     *
     * @param node
     * @return
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
     * @return
     */
    public Collection<BigraphEntity> getAllLeaves() {
        List<BigraphEntity> leaves = new ArrayList<>();
        for (BigraphEntity each : this.getAllVertices()) {
            if (degreeOf(each) <= 1 && !isRoot(each.getInstance())) {
                leaves.add(each);
            }
        }
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
