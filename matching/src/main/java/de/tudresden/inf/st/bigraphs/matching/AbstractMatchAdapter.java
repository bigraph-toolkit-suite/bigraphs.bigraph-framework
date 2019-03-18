package de.tudresden.inf.st.bigraphs.matching;

import com.google.common.collect.Lists;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

//TODO: künstlichen super node einfügen für die roots, wenn es mehrere gibt
public abstract class AbstractMatchAdapter {
    protected DynamicEcoreBigraph bigraph;

    public AbstractMatchAdapter(DynamicEcoreBigraph bigraph) {
        this.bigraph = bigraph;
    }

    public List<BigraphEntity> getRoots() {
        return new ArrayList<>(bigraph.getRoots());
    }

    public List<BigraphEntity> getSites() {
        return new ArrayList<>(bigraph.getSites());
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

    public abstract List<ControlLinkPair> getLinksOfNode(BigraphEntity node);

    //an edge is like an outername for an agent
    public List<ControlLinkPair> getAllLinks(BigraphEntity startNode) {
        List<ControlLinkPair> allVerticesPostOrder = new ArrayList<>();
//        for (BigraphEntity eachRoot : bigraph.getRoots()) {
//            BigraphEntity rootNode = new ArrayList<>(bigraph.getRoots()).get(0);
        Traverser<BigraphEntity> stringTraverser = Traverser.forTree(x -> {
            List<ControlLinkPair> linksOfNode = getLinksOfNode(x);
            allVerticesPostOrder.addAll(linksOfNode);
            return getChildren(x);
        });
        Iterable<BigraphEntity> v0 = stringTraverser.depthFirstPostOrder(startNode);
//            for(BigraphEntity e: v0) {
//                return new ControlLinkPair(null, null);
//            }
        Lists.newArrayList(v0);
//            allVerticesPostOrder.addAll();

//        System.out.println();
//        }
        return allVerticesPostOrder;
    }

    //FOR MATCHING: ONLY nodes, roots, site and outer names
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
//        EStructuralFeature portRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
//        if (Objects.nonNull(portRef)) {
//            EList<EObject> portList = (EList<EObject>) instance.eGet(portRef);
//            cnt += portList.size();
//        }
        //bPoints: for links
//        EStructuralFeature pntsRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
//        if (Objects.nonNull(pntsRef) && Objects.nonNull(instance.eGet(pntsRef))) {
//            cnt += ((EList<EObject>) instance.eGet(pntsRef)).size();
//        }
        return cnt;
    }

    public BigraphEntity getParent(BigraphEntity node) {
        if (!isBPlace(node.getInstance())) return null;
        EObject instance = node.getInstance();
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (Objects.nonNull(prntRef) && Objects.nonNull(instance.eGet(prntRef))) {
            EObject each = (EObject) instance.eGet(prntRef);
            if (isBNode(each)) {
                //get control by name
                String controlName = each.eClass().getName();
                Control control = bigraph.getSignature().getControlByName(controlName);
                return BigraphEntity.createNode(each, control);
            } else {
                return BigraphEntity.create(each, BigraphEntity.RootEntity.class);
            }
        }
        return node;
    }

    public List<BigraphEntity> getSiblings(BigraphEntity node) {
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
                if (isBNode(eachChild)) {
                    //get control by name
                    String controlName = eachChild.eClass().getName();
                    Control control = bigraph.getSignature().getControlByName(controlName);
                    siblings.add(BigraphEntity.createNode(eachChild, control));
                } else if (isSite(eachChild)) {
                    siblings.add(BigraphEntity.create(eachChild, BigraphEntity.SiteEntity.class));
                } else {
                    //can only be root...
                    siblings.add(BigraphEntity.create(eachChild, BigraphEntity.RootEntity.class));
                }
            }
        }
        return siblings;
    }

    protected List<BigraphEntity> neighborhoodHook(List<BigraphEntity> neighbors, BigraphEntity node) {
        EObject instance = node.getInstance();
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        if (Objects.nonNull(chldRef)) {
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            //create class ...
            //control can be acquired by the class name + signature
            for (EObject each : childs) {
                BigraphEntity convertedOne = null;
                // safe: no sites here ...
                if (isBNode(each)) {
                    //get control by name
                    String controlName = each.eClass().getName();
                    Control control = bigraph.getSignature().getControlByName(controlName);
                    assert control != null;
                    convertedOne = BigraphEntity.createNode(each, control);
                } else if (isRoot(each)) {
//                        Class clazz = BigraphEntity.RootEntity.class;
                    //can only be root...
                    convertedOne = BigraphEntity.create(each, BigraphEntity.RootEntity.class);
                }
                if (convertedOne != null)
                    neighbors.add(convertedOne);
            }
        }
        // parent
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (Objects.nonNull(prntRef) && Objects.nonNull(instance.eGet(prntRef))) {
            EObject each = (EObject) instance.eGet(prntRef);
            // safe: no sites here ...
            if (isBNode(each)) {
                //get control by name
                String controlName = each.eClass().getName();
                Control control = bigraph.getSignature().getControlByName(controlName);
                neighbors.add(BigraphEntity.createNode(each, control));
            } else {
                //can only be root...
                neighbors.add(BigraphEntity.create(each, BigraphEntity.RootEntity.class));
            }
        }
        return neighbors;
    }

    //TODO: do not create classes: get them from the entity map from the bigraph
    //FOR MATCHING
    public List<BigraphEntity> getOpenNeighborhoodOfVertex(BigraphEntity node) {
        List<BigraphEntity> neighbors = new ArrayList<>();
        EObject instance = node.getInstance();
        if (bigraph.isGround()) { // no sites here...
            neighborhoodHook(neighbors, node);
        }
        //TODO: now outer names and edges
        return neighbors;
    }

    //TODO: get link vertices from vertex method

    //ohne links
    public Collection<BigraphEntity> getAllVertices() {
        List<BigraphEntity> allNodes = new ArrayList<>(bigraph.getNodes());
//        allNodes.addAll(bigraph.getOuterNames());
//        allNodes.addAll(bigraph.getEdges());
        allNodes.addAll(bigraph.getRoots());
//        allNodes.addAll(bigraph.getSites());
        return allNodes;
    }

    public List<BigraphEntity> getAllInternalVerticesPostOrder() {
//        BigraphEntity rootNode = new ArrayList<>(bigraph.getRoots()).get(0);
        Iterable<BigraphEntity> allVerticesPostOrder = getAllVerticesPostOrder();
        List<BigraphEntity> collect = StreamSupport.stream(allVerticesPostOrder.spliterator(), false).filter(x -> getChildren(x).size() > 0).collect(Collectors.toList());
//        return collect;
        return collect;
    }

    //TODO: check if this is correcet with multiple roots
    public Iterable<BigraphEntity> getAllVerticesPostOrder() {
        Collection<BigraphEntity> allVerticesPostOrder = new ArrayList<>();
        for (BigraphEntity eachRoot : bigraph.getRoots()) {
//            BigraphEntity rootNode = new ArrayList<>(bigraph.getRoots()).get(0);
            Traverser<BigraphEntity> stringTraverser = Traverser.forTree(node -> getChildren(node));
            Iterable<BigraphEntity> v0 = stringTraverser.depthFirstPostOrder(eachRoot);
            allVerticesPostOrder.addAll(Lists.newArrayList(v0));
//        for (BigraphEntity each : v0) {
//            System.out.print(each + ", ");
//        }
//        System.out.println();
        }
        return allVerticesPostOrder;
    }

    //FOR MATCHING


    public List<BigraphEntity> getChildren(BigraphEntity node) {
        EObject instance = node.getInstance();
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        List<BigraphEntity> children = new ArrayList<>();
        if (Objects.nonNull(chldRef)) {
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            //create class ...
            //control can be acquired by the class name + signature
            for (EObject eachChild : childs) {
                if (isBNode(eachChild)) {
                    String controlName = eachChild.eClass().getName();
                    Control control = bigraph.getSignature().getControlByName(controlName);
                    children.add(BigraphEntity.createNode(eachChild, control));
                }
//                else if (isSite(eachChild)) {
//                    children.add(BigraphEntity.create(eachChild, BigraphEntity.SiteEntity.class));
//                }
            }
        }
        return children;
    }

    //FOR MATCHING
    public Collection<BigraphEntity> getAllLeaves() {
        List<BigraphEntity> leaves = new ArrayList<>();
        for (BigraphEntity each : this.getAllVertices()) {
            if (degreeOf(each) <= 1 && !isRoot(each.getInstance())) {
                leaves.add(each);
            }
        }
        return leaves;
    }

    public boolean isLink(BigraphEntity node) {
        return node.getType().equals(BigraphEntityType.OUTER_NAME) || node.getType().equals(BigraphEntityType.EDGE);
    }

    public boolean isOuterName(BigraphEntity node) {
        return node.getType().equals(BigraphEntityType.OUTER_NAME);
    }

    public boolean isOuterName(EObject eObject) {
        return eObject.eClass().equals(((EPackageImpl) bigraph.getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_OUTERNAME)) ||
                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) bigraph.getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_OUTERNAME));
    }

    protected boolean isBPlace(EObject eObject) {
        return eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) bigraph.getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_PLACE));
    }

    protected boolean isBNode(EObject eObject) {
        return eObject.eClass().equals(((EPackageImpl) bigraph.getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_NODE)) ||
                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) bigraph.getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_NODE));
    }

    protected boolean isRoot(EObject eObject) {
        return eObject.eClass().equals(((EPackageImpl) bigraph.getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_ROOT)) ||
                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) bigraph.getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_ROOT));
    }

    protected boolean isSite(EObject eObject) {
        return eObject.eClass().equals(((EPackageImpl) bigraph.getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_SITE)) ||
                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) bigraph.getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_SITE));
    }


}
