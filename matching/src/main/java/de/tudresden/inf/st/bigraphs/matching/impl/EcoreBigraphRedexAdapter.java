package de.tudresden.inf.st.bigraphs.matching.impl;

import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import java.util.*;

public class EcoreBigraphRedexAdapter extends AbstractDynamicMatchAdapter {

    public EcoreBigraphRedexAdapter(DynamicEcoreBigraph bigraph) {
        super(bigraph);
    }

//    @Deprecated
//    BigraphEntity newRoot;
//    List<BigraphEntity> crossBoundary = new ArrayList<>();


    //TODO: move this into a ReactionRule class
    @Deprecated
    public boolean checkRedexConform() {
        for (BigraphEntity eachRoot : getRoots()) {
            for (BigraphEntity each : getChildrenWithSites(eachRoot)) {
                if (isSite(each.getInstance())) return false;
            }
        }
        return true;
    }

//    @Override
//    public List<BigraphEntity> getRoots() {
//        if (newRoot == null) return super.getRoots();
//        return new ArrayList<>(Collections.singleton(newRoot));
//    }

    //BLEIBT
    private EObject createRootOfEClass() {
        EPackage loadedEPackage = getBigraphDelegate().getModelPackage();
//        EClassifier rootEClass = loadedEPackage.getEClassifiers().get(0);
        EClassifier eClassifierGen = ((EPackageImpl) getBigraphDelegate().getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_ROOT);
        EClass eClass = eClassifierGen.eClass();
        EObject eObject = loadedEPackage.getEFactoryInstance().create((EClass) eClassifierGen);
//        final int ix = rootIdxSupplier.get();
        eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), "index"), 0);
//        availableRoots.put(ix, eObject);
        return eObject;
    }

    /**
     * Only outer names
     * The order plays a role for checking (also in theory)
     *
     * @param node
     * @return
     */
    public List<ControlLinkPair> getLinksOfNode(BigraphEntity node) {
        EObject instance = node.getInstance();
        List<ControlLinkPair> children = new LinkedList<>();

        EStructuralFeature portRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
        if (Objects.nonNull(portRef)) {
            EList<EObject> portList = (EList<EObject>) instance.eGet(portRef);
            for (EObject eachPort : portList) {
                //bPoints: for links
                EStructuralFeature linkRef = eachPort.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                if (Objects.nonNull(linkRef) && Objects.nonNull(eachPort.eGet(linkRef))) {
                    EObject obj = (EObject) eachPort.eGet(linkRef);
                    if (isOuterName(obj)) {
                        children.add(new ControlLinkPair(node.getControl(), BigraphEntity.create(obj, BigraphEntity.OuterName.class)));
                    }
                }
            }
        }
        return children;
    }

    public int degreeOf(BigraphEntity nodeEntity) {
        //get all edges
        EObject instance = nodeEntity.getInstance();
        int cnt = 0;
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        if (Objects.nonNull(chldRef)) {
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
//            cnt += childs.size();
            for (EObject eObject : childs) {
                if (!isSite(eObject))
                    cnt++;
            }
        }
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (Objects.nonNull(prntRef) && Objects.nonNull(instance.eGet(prntRef))) {
            cnt++;
        }
        return cnt;
    }

    // Redex allows sites
    @Override
    public List<BigraphEntity> getOpenNeighborhoodOfVertex(BigraphEntity node) {
        List<BigraphEntity> neighbors = new ArrayList<>();
        neighbors = neighborhoodHook(neighbors, node);

//        EObject instance = node.getInstance();
//        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
//        if (Objects.nonNull(chldRef)) {
//            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
//            for (EObject each : childs) {
//                if (newRoot != null && isRoot(each)) neighbors.addAll(bigraph.getRoots());
////                if (isSite(each)) {
////                    BigraphEntity convertedOne = BigraphEntity.create(each, BigraphEntity.SiteEntity.class);
////                    neighbors.add(convertedOne);
//            }
////        }
//        }
        return neighbors;
    }

    public List<BigraphEntity> getOpenNeighborhoodOfVertexWithSites(BigraphEntity node) {
        List<BigraphEntity> neighbors = new ArrayList<>();
        neighbors = neighborhoodHook(neighbors, node);

        EObject instance = node.getInstance();
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        if (Objects.nonNull(chldRef)) {
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            for (EObject each : childs) {
                if (isSite(each)) {
                    BigraphEntity convertedOne = BigraphEntity.create(each, BigraphEntity.SiteEntity.class);
                    neighbors.add(convertedOne);
                }
//                if (newRoot != null && isRoot(each)) neighbors.addAll(getBigraphDelegate().getRoots());
            }
        }
        return neighbors;
    }

    public List<BigraphEntity> getChildren(BigraphEntity node) {
        EObject instance = node.getInstance();
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        List<BigraphEntity> children = new ArrayList<>();
        if (Objects.nonNull(chldRef)) {
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            //control can be acquired by the class name + signature
            for (EObject eachChild : childs) {
                if (isBNode(eachChild)) {
                    String controlName = eachChild.eClass().getName();
                    Control control = getBigraphDelegate().getSignature().getControlByName(controlName);
                    children.add(BigraphEntity.createNode(eachChild, control));
                } else if (isRoot(eachChild)) { //newRoot != null &&
                    children.add(BigraphEntity.create(eachChild, BigraphEntity.RootEntity.class));
                }
            }
        }
        return children;
    }

    public List<BigraphEntity> getChildrenWithSites(BigraphEntity node) {
        EObject instance = node.getInstance();
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        List<BigraphEntity> children = new ArrayList<>();
        if (Objects.nonNull(chldRef)) {
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            //control can be acquired by the class name + signature
            for (EObject eachChild : childs) {
                if (isBNode(eachChild)) {
                    String controlName = eachChild.eClass().getName();
                    Control control = getBigraphDelegate().getSignature().getControlByName(controlName);
                    children.add(BigraphEntity.createNode(eachChild, control));
                } else if (isSite(eachChild)) {
                    children.add(BigraphEntity.create(eachChild, BigraphEntity.SiteEntity.class));
                }
            }
        }
        return children;
    }


    @Override
    public Collection<BigraphEntity> getAllVertices() {
        List<BigraphEntity> allNodes = new ArrayList<>();
        allNodes.addAll(getBigraphDelegate().getNodes());
        allNodes.addAll(getRoots());
//        if (newRoot != null) allNodes.addAll(bigraph.getRoots());
//        allNodes.addAll(bigraph.getSites());
//        allNodes.addAll(bigraph.getOuterNames());
//        allNodes.addAll(bigraph.getEdges());
        return allNodes;
    }

    public Collection<BigraphEntity> getAllVerticesWithSites() {
        List<BigraphEntity> allNodes = new ArrayList<>();
        allNodes.addAll(getBigraphDelegate().getNodes());
        allNodes.addAll(getBigraphDelegate().getSites());
//        if (newRoot != null) allNodes.addAll(getBigraphDelegate().getRoots());
//        allNodes.addAll(bigraph.getOuterNames());
//        allNodes.addAll(bigraph.getEdges());
        allNodes.addAll(getRoots());
        return allNodes;
    }
}
