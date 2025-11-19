/*
 * Copyright (c) 2021-2024 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.core.reactivesystem.analysis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.jgrapht.Graph;

/**
 * This class provides various analysis methods/operations based on labelled transition system, specifically, for
 * the {@link ReactionGraph} class.
 * <p>
 * Instantiation is provided via a factory method {@link #createInstance()}.
 *
 * @param <B> type of the bigraph
 * @author Dominik Grzelak
 */
public class ReactionGraphAnalysis<B extends Bigraph<? extends Signature<?>>> {

    private ReactionGraphAnalysis() {
    }

    /**
     * Create an instance of {@link ReactionGraphAnalysis}.
     *
     * @param <B> type of the bigraph
     * @return an object of type {@link ReactionGraphAnalysis}
     */
    public static <B extends Bigraph<? extends Signature<?>>> ReactionGraphAnalysis<B> createInstance() {
        return new ReactionGraphAnalysis<>();
    }

    public List<StateTrace<B>> findAllPathsInGraphToLeaves(ReactionGraph<B> reactionGraph) {
//        System.out.println("findAllPathsInGraphToLeaves()...");
        Set<ReactionGraph.LabeledNode> labeledNodes = reactionGraph.getGraph().vertexSet();
        ReactionGraph.LabeledNode firstGraphNode = labeledNodes.stream().findFirst().get();
//        System.out.println("firstGraphNode: " + firstGraphNode);
        List<StateTrace<B>> pathLists = recursPath(reactionGraph, firstGraphNode, new ArrayList<>());
//        System.out.println("Found pathes from node " + firstGraphNode.getLabel() + ": " + pathLists.size());
        return pathLists;
    }

    //TODO REMOVE CYCLES
    //Idea: https://cs.stackexchange.com/questions/124240/computing-all-paths-from-root-to-the-leaf-nodes-in-a-tree
    private List<StateTrace<B>> recursPath(ReactionGraph<B> reactionGraph, ReactionGraph.LabeledNode v, List<String> labelsTraversed) {
        String label = v.getCanonicalForm();
//        System.out.println("\tfor: " + v.getLabel());
        assert reactionGraph.containsBigraph(label);
        List<StateTrace> collect = new ArrayList<>();
//        String clabel = v.getCanonicalForm();
        // (1) First Case: If `v` is a leaf, then the list has one single path containing `v` itself
        //Check if has outgoing edges, if not it is a leave
        if (isLeaf(reactionGraph.getGraph()).apply(v) || (labelsTraversed.contains(v.getLabel()))) {
            List<B> pathList = new LinkedList<>();
            List<String> labelList = new LinkedList<>();
            B b = reactionGraph.getStateMap().get(label);
            labelList.add(v.getLabel());
            pathList.add(b);
            collect.add(new StateTrace<>(pathList, labelList));
        } else { // If v has k children then return the union of these k lists and append v to each list
            List<String> newLabelsTraversed = new ArrayList<>(labelsTraversed);
            newLabelsTraversed.add(v.getLabel());
            // Acquire list of children first
            List<ReactionGraph.LabeledNode> nextChildren = reactionGraph.getGraph().outgoingEdgesOf(v).stream()
                    .map(x -> reactionGraph.getGraph().getEdgeTarget(x))
//                    .filter(x -> x != v)
                    .collect(Collectors.toList());
            for (ReactionGraph.LabeledNode eachTarget : nextChildren) {
                List<StateTrace<B>> pathLists = recursPath(reactionGraph, eachTarget, newLabelsTraversed);
                for (StateTrace<B> eachPath : pathLists) {
                    B b = reactionGraph.getStateMap().get(label);
                    eachPath.path.add(b);
                    eachPath.stateLabels.add(v.getLabel());
                }
                collect.addAll(pathLists);
            }
        }
        return (List) collect;
    }

    public static Function<ReactionGraph.LabeledNode, Boolean> isLeaf(Graph<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> jGraph) {
        return nodeToCheck -> {
            Set<ReactionGraph.LabeledEdge> labeledEdges = jGraph.edgesOf(nodeToCheck);
            boolean hasOutgoingEdges = labeledEdges.stream().allMatch(x -> jGraph.getEdgeTarget(x) == nodeToCheck);
            return hasOutgoingEdges; //jGraph.outgoingEdgesOf(nodeToCheck).size() == 0; // <- same
        };
    }

    /**
     * Object containing one path from a state v of a reaction graph to one leave of the subtree rooted at state v
     * <p>
     * Both lists share the same index set to map from a bigraph state to the state label and vice versa.
     *
     * @param <B>
     * @author Dominik Grzelak
     */
    public static class StateTrace<B extends Bigraph<? extends Signature<?>>> {
        List<B> path = new LinkedList<>();
        List<String> stateLabels = new LinkedList<>();

        public StateTrace(List<B> path, List<String> stateLabels) {
            this.path = path;
            this.stateLabels = stateLabels;
        }

        public List<B> getPath() {
            return path;
        }

        public List<String> getStateLabels() {
            return stateLabels;
        }
    }

}
