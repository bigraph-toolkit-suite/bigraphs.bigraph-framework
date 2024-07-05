package org.bigraphs.framework.simulation.equivalence.adapter;

import bighuggies.bisimulation.se705.bisimulation.lts.Process;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An adapter for {@link org.bigraphs.framework.core.reactivesystem.AbstractTransitionSystem} objects in
 * Bigraph Framework for the external Java library "bighuggies:bisimulation"
 * (shaded in the Simulation Module dependency).
 * <p>
 * "bighuggies:bisimulation" computes bisimilarity for two LTSs.
 * This functionality is made available to objects of type
 * {@link org.bigraphs.framework.core.reactivesystem.AbstractTransitionSystem}
 * in Bigraph Framework via this adapter class.
 * <p>
 * An AST is converted to a Process object of the external Java library "bighuggies".
 *
 * @param <AST> type of the transition system in Bigraph Framework (type of {@link ReactionGraph})
 * @param <B> bigraph type of the AST states
 * @author Dominik Grzelak
 * @see "for https://github.com/bighuggies/bisimulation"
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

// bighuggies code:
//        p.addState(prefix + line[0].trim());
//        p.addState(prefix + line[2].trim());
//        p.addAction(line[1].trim());
//        p.addTransition(prefix + line[0].trim(), line[1].trim(), prefix + line[2].trim());

    }
}
