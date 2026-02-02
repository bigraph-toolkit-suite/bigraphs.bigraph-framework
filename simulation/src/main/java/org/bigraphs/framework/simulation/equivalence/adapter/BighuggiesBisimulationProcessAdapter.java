/*
 * Copyright (c) 2024-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.equivalence.adapter;

import bighuggies.bisimulation.se705.bisimulation.lts.Process;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.jgrapht.Graph;


/**
 * The "bighuggies:bisimulation" library computes bisimilarity for two LTSs.
 * <p>
 * This class presents an adapter for {@link org.bigraphs.framework.core.reactivesystem.AbstractTransitionSystem} objects in
 * Bigraph Framework that integrates the external Java library "bighuggies:bisimulation"
 * (shaded in the Simulation Module dependency).
 * <p>
 * A transition system is converted into a {@code Process} object of the "bighuggies" library.
 *
 * @param <AST> transition system type in Bigraph Framework (type of {@link ReactionGraph})
 * @param <B>   bigraph type of the transition system states
 * @author Dominik Grzelak
 * @see <a href="https://github.com/bighuggies/bisimulation">https://github.com/bighuggies/bisimulation</a>
 */
public class BighuggiesBisimulationProcessAdapter<B extends Bigraph<? extends Signature<?>>, AST extends ReactionGraph<B>> extends Process {
    AST transitionSystem;
    String prefix;

    /**
     * This adapter takes three arguments: the AST to be adapted, and additionally a process name and prefix that the
     * bighuggies Process class requires.
     *
     * @param processName
     * @param prefix
     * @param transitionSystem
     */
    public BighuggiesBisimulationProcessAdapter(String processName, String prefix, AST transitionSystem) {
        super(processName);
        this.prefix = prefix;
        this.transitionSystem = transitionSystem;
        this.initProcessFromAST();
    }

    /**
     * Creates the structure of the Process (transition system) from the given AST.
     * Uses the setter-methods from the parent mostly.
     */
    protected void initProcessFromAST() {
        Graph<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> g = transitionSystem.getGraph();
        AtomicInteger cnt = new AtomicInteger(0);
        Map<String, Integer> simpleIntMap = new HashMap<>();
        g.vertexSet().forEach(l -> simpleIntMap.put(l.getLabel(), cnt.getAndIncrement()));
        g.vertexSet().forEach(vertex -> {
            String s1_lbl = prefix + simpleIntMap.get(vertex.getLabel());
            g.outgoingEdgesOf(vertex).forEach(edge -> {
                String action_lbl = edge.getLabel();
                String s2_lbl = prefix + simpleIntMap.get(g.getEdgeTarget(edge).getLabel());
//                System.out.println(s1_lbl + " --[" + action_lbl + "]--> " + s2_lbl);
                addState(s1_lbl);
                addState(s2_lbl);
                addAction(action_lbl);
                addTransition(s1_lbl, action_lbl, s2_lbl);
            });
        });
    }
}
