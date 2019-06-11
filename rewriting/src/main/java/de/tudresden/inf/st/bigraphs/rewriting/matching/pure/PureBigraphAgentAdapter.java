package de.tudresden.inf.st.bigraphs.rewriting.matching.pure;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.matching.AbstractDynamicMatchAdapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.*;

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
    public LinkedList<ControlLinkPair> getLinksOfNode(BigraphEntity node) {
        EObject instance = node.getInstance();
        LinkedList<AbstractDynamicMatchAdapter.ControlLinkPair> children = new LinkedList<>();

        EStructuralFeature portRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
        if (Objects.nonNull(portRef)) {
            EList<EObject> portList = (EList<EObject>) instance.eGet(portRef);
            for (EObject eachPort : portList) {
                //bPoints: for links
                EStructuralFeature linkRef = eachPort.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                if (Objects.nonNull(linkRef) && Objects.nonNull(eachPort.eGet(linkRef))) {
                    final EObject obj = (EObject) eachPort.eGet(linkRef);
                    try {
                        if (isOuterName(obj)) {
                            Optional<BigraphEntity.OuterName> first = getOuterNames().stream()
                                    .filter(x -> x.getInstance().equals(obj))
                                    .findFirst();
                            children.add(
                                    new ControlLinkPair(node.getControl(), first.orElseThrow(throwableSupplier))
                            );
                        } else if (isBEdge(obj)) {
                            Optional<BigraphEntity.Edge> first = getEdges().stream()
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
}
