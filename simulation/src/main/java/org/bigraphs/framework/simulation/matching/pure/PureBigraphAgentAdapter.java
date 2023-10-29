package org.bigraphs.framework.simulation.matching.pure;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import org.bigraphs.framework.simulation.matching.AbstractDynamicMatchAdapter;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.*;

/**
 * @author Dominik Grzelak
 */
public class PureBigraphAgentAdapter extends AbstractDynamicMatchAdapter<DefaultDynamicSignature, PureBigraph> {

    MutableMap<BigraphEntity<?>, LinkedList<ControlLinkPair>> linkOfNodesMap = Maps.mutable.empty();

    public PureBigraphAgentAdapter(PureBigraph bigraph) {
        super(bigraph);
    }

    @Override
    public DefaultDynamicSignature getSignature() {
        return super.getSignature();
    }

    @Override
    public void clearCache() {
//        super.clearCache();
        linkOfNodesMap.clear();
    }

    /**
     * In the list are included edges and outer names.
     *
     * @param node the node
     * @return a list of all links connected to the given node
     */

    public LinkedList<ControlLinkPair> getLinksOfNode(BigraphEntity<?> node) {
        if (linkOfNodesMap.containsKey(node)) {
            return linkOfNodesMap.get(node);
        }
        EObject instance = node.getInstance();
        LinkedList<AbstractDynamicMatchAdapter.ControlLinkPair> children = new LinkedList<>();

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
        linkOfNodesMap.put(node, children);
        return children;
    }
}
