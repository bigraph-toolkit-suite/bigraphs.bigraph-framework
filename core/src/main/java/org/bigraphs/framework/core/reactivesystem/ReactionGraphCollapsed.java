/*
 * Copyright (c) 2024 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.core.reactivesystem;

import java.util.*;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;


/**
 * This class represents a "collapsed", "reduced" reaction graph, possibly consolidated by a minimization algorithm
 * (e.g., bisim minimization).
 *
 * The states of a graph are {@link org.bigraphs.framework.core.reactivesystem.ReactionGraph.CollapsedLabeledNode}, and
 * the edges are of type {@link org.bigraphs.framework.core.reactivesystem.ReactionGraph.CollapsedLabeledEdge}.
 *
 * Collapsed nodes and edges contain the original nodes and edges before aggregation.
 * Collapsed nodes and edges have a new label.
 * The label and the canonical form are equal.
 *
 * @param <B>
 */
public class ReactionGraphCollapsed<B extends Bigraph<? extends Signature<?>>> extends ReactionGraph<B> {

    public ReactionGraphCollapsed() {
        super();
    }

    //source and target and reaction can be null
    @Override
    public void addEdge(B source, String sourceLbl, B target, String targetLbl, BMatchResult<B> reaction, String reactionLbl) {
        LabeledNode sourceNode;
        if (stateMap.get(sourceLbl) != null) {
            sourceNode = graph.vertexSet().stream().filter(x -> x.canonicalForm.equals(sourceLbl)).findFirst().get();
        } else {
            sourceNode = createNode(sourceLbl);
            addState(sourceLbl, source);
            graph.addVertex(sourceNode);
        }
        LabeledNode targetNode;
        if (stateMap.get(targetLbl) != null) {
            targetNode = graph.vertexSet().stream().filter(x -> x.canonicalForm.equals(targetLbl)).findFirst().get();
        } else {
            targetNode = createNode(targetLbl);
            addState(targetLbl, target);
            graph.addVertex(targetNode);
        }
        Set<LabeledEdge> allEdges = graph.getAllEdges(sourceNode, targetNode);
        Optional<LabeledEdge> first = allEdges.stream().filter(x -> x.label.equals(reactionLbl)).findFirst();
        if (first.isEmpty()) {
            final LabeledEdge edge = new LabeledEdge(reactionLbl);
            boolean b = graph.addEdge(sourceNode, targetNode, edge);
            if (b) {
                addTransition(source, target, reactionLbl, reaction);
            } else {
                transitionMap.get(reactionLbl).add(reaction);
            }
        }
    }
}
