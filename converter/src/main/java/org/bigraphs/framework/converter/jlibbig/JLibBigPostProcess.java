/*
 * Copyright (c) 2026 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.converter.jlibbig;

import java.util.*;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphMutable;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Post-processes a bigraph that was obtained from a JLibBig decode (e.g. after applying a rewrite).
 * Restores node names (from _id attribute first, then by structural matching) and removes
 * spurious edges (edges with only one connected point) that JLibBig may create.
 *
 * @see JLibBigBigraphEncoder
 * @see JLibBigBigraphDecoder
 */
public final class JLibBigPostProcess {

    private static final Logger logger = LoggerFactory.getLogger(JLibBigPostProcess.class);

    private static final int MAX_RESTORE_ITERATIONS = 10;

    private JLibBigPostProcess() {}

    /**
     * Post-process the transformed bigraph using the original as reference for name restoration.
     * Modifies {@code transformed} in place: restores node names (from _id or by structural match)
     * and removes spurious single-point edges.
     *
     * @param original    the bigraph before transformation (e.g. the agent)
     * @param transformed the mutable bigraph after transformation (decoded result); modified in place
     */
    public static void postProcess(final PureBigraph original, final PureBigraphMutable transformed) {
        restoreNodeNamesFromIdOrStructure(original, transformed);
        removeSpuriousEdges(transformed);
    }

    /**
     * Restore node names: first from _id attribute where present, then by structural matching
     * for nodes that still have auto-generated names (e.g. N_65, v42).
     */
    static void restoreNodeNamesFromIdOrStructure(final PureBigraph original, final PureBigraphMutable transformed) {
        boolean needsStructuralFallback = false;
        for (BigraphEntity.NodeEntity<DynamicControl> node : transformed.getNodes()) {
            String currentName = node.getName();
            if (node.getAttributes() != null && node.getAttributes().containsKey("_id")) {
                String originalName = String.valueOf(node.getAttributes().get("_id"));
                if (!originalName.equals(currentName)) {
                    node.setName(originalName);
                    logger.debug("Restored node name from _id: {} -> {}", currentName, originalName);
                }
            } else {
                needsStructuralFallback = true;
            }
        }
        if (needsStructuralFallback) {
            restoreNodeNamesByStructure(original, transformed);
        }
    }

    /**
     * Restore node names by matching transformed nodes to original nodes based on structure
     * (control type, parent, port count, connected outer names).
     */
    static void restoreNodeNamesByStructure(final PureBigraph original, final PureBigraphMutable transformed) {
        Map<BigraphEntity.NodeEntity<DynamicControl>, BigraphEntity.NodeEntity<DynamicControl>> transformedToOriginal = new HashMap<>();
        Set<BigraphEntity.NodeEntity<DynamicControl>> matchedOriginalNodes = new HashSet<>();

        for (BigraphEntity.NodeEntity<DynamicControl> transformedNode : transformed.getNodes()) {
            for (BigraphEntity.NodeEntity<DynamicControl> originalNode : original.getNodes()) {
                if (matchedOriginalNodes.contains(originalNode)) continue;
                if (nodesMatch(original, originalNode, transformed, transformedNode)) {
                    transformedToOriginal.put(transformedNode, originalNode);
                    matchedOriginalNodes.add(originalNode);
                    break;
                }
            }
        }

        boolean changed = true;
        int iteration = 0;
        while (changed && iteration < MAX_RESTORE_ITERATIONS) {
            changed = false;
            iteration++;
            for (BigraphEntity.NodeEntity<DynamicControl> transformedNode : transformed.getNodes()) {
                String currentName = transformedNode.getName();
                if (!currentName.startsWith("N_") && !currentName.startsWith("v")) continue;

                BigraphEntity.NodeEntity<DynamicControl> originalNode = transformedToOriginal.get(transformedNode);
                if (originalNode != null) {
                    String originalName = originalNode.getName();
                    if (!originalName.equals(currentName)) {
                        transformedNode.setName(originalName);
                        changed = true;
                        logger.debug("Restored node name by structure: {} -> {} (Control: {})",
                                currentName, originalName, transformedNode.getControl().getNamedType().stringValue());
                    }
                }
            }
        }
    }

    private static boolean nodesMatch(
            final PureBigraph originalBigraph,
            final BigraphEntity.NodeEntity<DynamicControl> originalNode,
            final PureBigraphMutable transformedBigraph,
            final BigraphEntity.NodeEntity<DynamicControl> transformedNode) {
        if (!originalNode.getControl().getNamedType().equals(transformedNode.getControl().getNamedType())) {
            return false;
        }
        if (originalBigraph.getPorts(originalNode).size() != transformedBigraph.getPorts(transformedNode).size()) {
            return false;
        }

        BigraphEntity<?> originalParent = originalBigraph.getParent(originalNode);
        BigraphEntity<?> transformedParent = transformedBigraph.getParent(transformedNode);

        if (originalParent instanceof BigraphEntity.RootEntity && transformedParent instanceof BigraphEntity.RootEntity) {
            if (((BigraphEntity.RootEntity) originalParent).getIndex() != ((BigraphEntity.RootEntity) transformedParent).getIndex()) {
                return false;
            }
        } else if (originalParent instanceof BigraphEntity.NodeEntity && transformedParent instanceof BigraphEntity.NodeEntity) {
            BigraphEntity.NodeEntity<?> origParentNode = (BigraphEntity.NodeEntity<?>) originalParent;
            BigraphEntity.NodeEntity<?> transParentNode = (BigraphEntity.NodeEntity<?>) transformedParent;
            if (!origParentNode.getControl().getNamedType().equals(transParentNode.getControl().getNamedType())) {
                return false;
            }
        } else {
            return false;
        }

        List<String> originalOuterNames = new ArrayList<>();
        for (BigraphEntity.Port port : originalBigraph.getPorts(originalNode)) {
            BigraphEntity.Link link = originalBigraph.getLinkOfPoint(port);
            if (link instanceof BigraphEntity.OuterName) {
                originalOuterNames.add(((BigraphEntity.OuterName) link).getName());
            }
        }
        List<String> transformedOuterNames = new ArrayList<>();
        for (BigraphEntity.Port port : transformedBigraph.getPorts(transformedNode)) {
            BigraphEntity.Link link = transformedBigraph.getLinkOfPoint(port);
            if (link instanceof BigraphEntity.OuterName) {
                transformedOuterNames.add(((BigraphEntity.OuterName) link).getName());
            }
        }
        Collections.sort(originalOuterNames);
        Collections.sort(transformedOuterNames);
        return originalOuterNames.equals(transformedOuterNames);
    }

    /**
     * Remove edges with only one connected point (spurious edges created by JLibBig for ports
     * that should be connected to outer names). Ports are disconnected before removing edges.
     */
    static void removeSpuriousEdges(final PureBigraphMutable bigraph) {
        List<BigraphEntity.Edge> edgesToRemove = new ArrayList<>();
        Map<BigraphEntity.Edge, List<BigraphEntity.Port>> edgeToPorts = new HashMap<>();

        for (BigraphEntity.Edge edge : bigraph.getEdges()) {
            Collection<BigraphEntity<?>> points = bigraph.getPointsFromLink(edge);
            int pointCount = points != null ? points.size() : 0;
            if (pointCount == 1) {
                edgesToRemove.add(edge);
                List<BigraphEntity.Port> ports = new ArrayList<>();
                for (BigraphEntity<?> point : points) {
                    if (point instanceof BigraphEntity.Port) {
                        ports.add((BigraphEntity.Port) point);
                    }
                }
                edgeToPorts.put(edge, ports);
            }
        }

        for (Map.Entry<BigraphEntity.Edge, List<BigraphEntity.Port>> entry : edgeToPorts.entrySet()) {
            for (BigraphEntity.Port port : entry.getValue()) {
                try {
                    bigraph.disconnectPort(port);
                } catch (Exception e) {
                    logger.warn("Failed to disconnect port from edge {}: {}", entry.getKey().getName(), e.getMessage());
                }
            }
        }
        for (BigraphEntity.Edge edge : edgesToRemove) {
            try {
                bigraph.removeEdge(edge);
            } catch (Exception e) {
                logger.warn("Failed to remove spurious edge {}: {}", edge.getName(), e.getMessage());
            }
        }
    }
}
