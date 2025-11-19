/*
 * Copyright (c) 2019-2024 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.matching;

import com.google.common.collect.Lists;
import com.google.common.graph.Traverser;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;

/**
 * @author Dominik Grzelak
 */
public abstract class BigraphMatchingSupport {
    final protected static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public BigraphMatchingSupport() {

    }

    public List<BigraphEntity<?>> getSubBigraphFrom(final BigraphEntity<?> node, final AbstractDynamicMatchAdapter adapter) {
        Traverser<BigraphEntity<?>> childTraverser = Traverser.forTree(xx -> adapter.getChildren(xx));
        return Lists.newArrayList(childTraverser.breadthFirst(node));
    }

    /**
     * Helper method to check whether two nodes have the same control
     *
     * @param node1 first node
     * @param node2 second node
     * @return {@code true} if both nodes have the same control, otherwise {@code false}
     */
    public boolean isSameControl(BigraphEntity<?> node1, BigraphEntity<?> node2) {
        return node1.getControl().equals(node2.getControl());
    }


    /**
     * Helper function to create a bipartite graph.
     *
     * @param x first set of nodes
     * @param y second set of nodes
     * @return a bipartite graph
     */
    public static Graph<BigraphEntity<?>, DefaultEdge> createBipartiteGraph(List<BigraphEntity<?>> x, List<BigraphEntity<?>> y) {
        SimpleGraph<BigraphEntity<?>, DefaultEdge> bg = (SimpleGraph<BigraphEntity<?>, DefaultEdge>) BigraphMatchingSupport.buildEmptySimpleDirectedGraph();
        for (BigraphEntity<?> eachX : x) {
            bg.addVertex(eachX);
        }
        for (BigraphEntity<?> eachY : y) {
            bg.addVertex(eachY);
        }
        return bg;
    }

    /**
     * Helper function to create a directed graph.
     *
     * @return a directed graph
     */
    public static Graph<BigraphEntity<?>, DefaultEdge> buildEmptySimpleDirectedGraph() {
        return GraphTypeBuilder.<BigraphEntity<?>, DefaultEdge>undirected()
//                .vertexClass()
//                .vertexSupplier(vSupplier)
                .allowingMultipleEdges(false)
                .allowingSelfLoops(false)
                .edgeClass(DefaultEdge.class)
                .weighted(false).buildGraph();
    }
}
