package de.tudresden.inf.st.bigraphs.rewriting.matching.pure;

import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import de.tudresden.inf.st.bigraphs.rewriting.matching.AbstractDynamicMatchAdapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import java.util.*;

/**
 * @author Dominik Grzelak
 */
public class PureBigraphRedexAdapter extends AbstractDynamicMatchAdapter<PureBigraph> {

    public PureBigraphRedexAdapter(PureBigraph bigraph) {
        super(bigraph);
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

    /**
     * Sites are not included in the count
     * <p>
     * All in/out-going edges of a node within the place graph.
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

    @Deprecated
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

    /**
     * Get all children of a node without sites included
     *
     * @param node
     * @return
     */
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
                } else if (isRoot(eachChild)) { //newRoot != null && //TODO this never happens!
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
}
