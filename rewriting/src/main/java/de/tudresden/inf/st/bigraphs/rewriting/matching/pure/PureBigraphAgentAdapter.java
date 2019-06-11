package de.tudresden.inf.st.bigraphs.rewriting.matching.pure;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.matching.AbstractDynamicMatchAdapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Dominik Grzelak
 */
public class PureBigraphAgentAdapter extends AbstractDynamicMatchAdapter<PureBigraph> {

    public PureBigraphAgentAdapter(PureBigraph bigraph) {
        super(bigraph);
    }

    /**
     * includes also edges+outernames
     *
     * @param node
     * @return
     */
    public List<AbstractDynamicMatchAdapter.ControlLinkPair> getLinksOfNode(BigraphEntity node) {
        EObject instance = node.getInstance();
        List<AbstractDynamicMatchAdapter.ControlLinkPair> children = new ArrayList<>();

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
                    } else if (isBEdge(obj)) {
                        children.add(new ControlLinkPair(node.getControl(), BigraphEntity.create(obj, BigraphEntity.Edge.class)));
                    }
                }
            }

        }
        return children;
    }
}
