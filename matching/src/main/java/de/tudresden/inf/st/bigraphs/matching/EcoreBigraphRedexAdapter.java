package de.tudresden.inf.st.bigraphs.matching;

import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class EcoreBigraphRedexAdapter extends AbstractMatchAdapter {

    public EcoreBigraphRedexAdapter(DynamicEcoreBigraph bigraph) {
        super(bigraph);
    }

    //TODO: move this into a ReactionRule class
    private boolean checkRedexConform() {
        for (BigraphEntity eachRoot : getRoots()) {
            for (BigraphEntity each : getChildren(eachRoot)) {
                if (isSite(each.getInstance())) return false;
            }
        }
        return true;
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
//                if (isSite(each)) {
//                    BigraphEntity convertedOne = BigraphEntity.create(each, BigraphEntity.SiteEntity.class);
//                    neighbors.add(convertedOne);
//                }
//            }
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
                    Control control = bigraph.getSignature().getControlByName(controlName);
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
        allNodes.addAll(bigraph.getNodes());
//        allNodes.addAll(bigraph.getSites());
//        allNodes.addAll(bigraph.getOuterNames());
//        allNodes.addAll(bigraph.getEdges());
        allNodes.addAll(bigraph.getRoots());
        return allNodes;
    }

    public Collection<BigraphEntity> getAllVerticesWithSites() {
        List<BigraphEntity> allNodes = new ArrayList<>();
        allNodes.addAll(bigraph.getNodes());
        allNodes.addAll(bigraph.getSites());
//        allNodes.addAll(bigraph.getOuterNames());
//        allNodes.addAll(bigraph.getEdges());
        allNodes.addAll(bigraph.getRoots());
        return allNodes;
    }
}
