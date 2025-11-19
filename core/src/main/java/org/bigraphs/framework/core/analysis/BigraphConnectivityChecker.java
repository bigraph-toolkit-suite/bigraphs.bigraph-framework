/*
 * Copyright (c) 2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.core.analysis;

import java.util.*;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicControl;

public class BigraphConnectivityChecker {

    /**
     * Checks if all nodes in the place graph of the given bigraph are fully connected.
     * This means that each node must be reachable from every other node via a link (either an edge or an outer name).
     *
     * @param bigraph the pure bigraph instance representing the bigraph to be checked.
     * @return {@code true} if the bigraph is fully connected, {@code false} otherwise.
     */
    public static boolean isFullyConnected(PureBigraph bigraph) {
        List<BigraphEntity.NodeEntity<DynamicControl>> nodes = bigraph.getNodes();
        if (nodes.isEmpty()) {
            return true; // An empty bigraph is trivially connected.
        }

        // Start traversal from the first node
        Set<BigraphEntity> visited = new HashSet<>();
        Queue<BigraphEntity> queue = new LinkedList<>();
        queue.add(nodes.get(0)); // Start with any node

        while (!queue.isEmpty()) {
            BigraphEntity current = queue.poll();
            if (!visited.contains(current)) {
                visited.add(current);
                List<BigraphEntity> neighbors = getConnectedEntities(bigraph, current);
                queue.addAll(neighbors);
            }
        }

        // If all nodes are visited, the bigraph is fully connected
        return visited.containsAll(nodes);
    }

    private static List<BigraphEntity> getConnectedEntities(PureBigraph bigraph, BigraphEntity node) {
        List<BigraphEntity> connected = new ArrayList<>();

        // Get all nodes connected through the link graph
        for (BigraphEntity.Link link : bigraph.getAllLinks()) {
            List<BigraphEntity<?>> points = bigraph.getPointsFromLink(link);
            if (points.contains(node)) {
                connected.addAll(points);
            }
        }

        // Get all parent-child connections from the place graph
        connected.addAll(bigraph.getChildrenOf(node));
        BigraphEntity parent = bigraph.getParent(node);
        if (parent != null) {
            connected.add(parent);
        }

        return connected;
    }
}
