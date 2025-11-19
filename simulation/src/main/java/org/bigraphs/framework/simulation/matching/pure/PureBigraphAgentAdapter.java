/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.simulation.matching.pure;

import java.util.*;
import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.simulation.matching.AbstractDynamicMatchAdapter;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * @author Dominik Grzelak
 */
public class PureBigraphAgentAdapter extends AbstractDynamicMatchAdapter<DynamicSignature, PureBigraph> {

    MutableMap<BigraphEntity<?>, LinkedList<ControlLinkPair>> linkOfNodesMap = Maps.mutable.empty();

    public PureBigraphAgentAdapter(PureBigraph bigraph) {
        super(bigraph);
    }

    @Override
    public DynamicSignature getSignature() {
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
