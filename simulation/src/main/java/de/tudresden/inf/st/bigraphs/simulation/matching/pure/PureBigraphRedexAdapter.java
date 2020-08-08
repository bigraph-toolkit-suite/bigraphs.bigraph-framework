package de.tudresden.inf.st.bigraphs.simulation.matching.pure;

import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.simulation.matching.AbstractDynamicMatchAdapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.*;

/**
 * @author Dominik Grzelak
 */
public class PureBigraphRedexAdapter extends AbstractDynamicMatchAdapter<PureBigraph> {

    public PureBigraphRedexAdapter(PureBigraph bigraph) {
        super(bigraph);
    }

    @Override
    public DefaultDynamicSignature getSignature() {
        return (DefaultDynamicSignature) super.getSignature();
    }

    public List<BigraphEntity<?>> getChildrenWithSites(BigraphEntity<?> node) {
        EObject instance = node.getInstance();
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        List<BigraphEntity<?>> children = new ArrayList<>();
        if (Objects.nonNull(chldRef)) {
            @SuppressWarnings("unchecked")
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            //control can be acquired by the class name + signature
            for (EObject eachChild : childs) {
                addPlaceToList(children, eachChild, true);
            }
        }
        return children;
    }

    /**
     * Only outer names are returned, edges are <b>not</b> considered for the result.
     * The order plays a role for checking (also in theory)
     *
     * @param node the node
     * @return a list of all links connected to the given node
     */
    public LinkedList<ControlLinkPair> getLinksOfNode(BigraphEntity node) {
        EObject instance = node.getInstance();
        LinkedList<ControlLinkPair> children = new LinkedList<>();

        EStructuralFeature portRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
        if (Objects.nonNull(portRef)) {
            @SuppressWarnings("unchecked")
            EList<EObject> portList = (EList<EObject>) instance.eGet(portRef);
            for (EObject eachPort : portList) {
                //bPoints: for links
                EStructuralFeature linkRef = eachPort.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                if (Objects.nonNull(linkRef) && Objects.nonNull(eachPort.eGet(linkRef))) {
                    final EObject obj = (EObject) eachPort.eGet(linkRef);

                    try {
                        if (isBOuterName(obj)) {
                            Optional<BigraphEntity.OuterName> first = getOuterNames().stream()
                                    .filter(x -> x.getInstance().equals(obj))
                                    .findFirst();
                            children.add(
                                    new ControlLinkPair(node.getControl(), first.orElseThrow(throwableSupplier))
                            );
                        }
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return children;
    }

    /**
     * Get the number of all in- and out-going edges of a node within the place graph. <br>
     * Sites are not included in the count
     *
     * @param nodeEntity the node
     * @return the degree of the node
     */
    public int degreeOf(BigraphEntity nodeEntity) {
        //get all edges
        EObject instance = nodeEntity.getInstance();
        int cnt = 0;
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        if (Objects.nonNull(chldRef)) {
            @SuppressWarnings("unchecked")
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            for (EObject eObject : childs) {
                if (!isBSite(eObject))
                    cnt++;
            }
        }
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (Objects.nonNull(prntRef) && Objects.nonNull(instance.eGet(prntRef))) {
            cnt++;
        }
        return cnt;
    }

//    /**
//     * Get all children of a node without sites included
//     *
//     * @param node
//     * @return
//     */
//    public List<BigraphEntity> getChildren(BigraphEntity node) {
//        EObject instance = node.getInstance();
//        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
//        List<BigraphEntity> children = new ArrayList<>();
//        if (Objects.nonNull(chldRef)) {
//            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
//            //control can be acquired by the class name + signature
//            for (EObject eachChild : childs) {
//                addPlaceToList(children, eachChild, false);
//            }
//        }
//        return children;
//    }

//    public List<BigraphEntity> getChildrenWithSites(BigraphEntity node) {
//        EObject instance = node.getInstance();
//        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
//        List<BigraphEntity> children = new ArrayList<>();
//        if (Objects.nonNull(chldRef)) {
//            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
//            //control can be acquired by the class name + signature
//            for (EObject eachChild : childs) {
//                addPlaceToList(children, eachChild, true);
//            }
//        }
//        return children;
//    }
}
