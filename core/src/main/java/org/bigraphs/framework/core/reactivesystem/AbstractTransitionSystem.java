package org.bigraphs.framework.core.reactivesystem;

import org.bigraphs.framework.core.AbstractRankedGraph;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.jgrapht.graph.DefaultEdge;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Abstract base class representing the minimal data structure for all concrete transition system implementations.
 * This class generalizes all possible variations of transition systems.
 *
 * @param <B> the type of the bigraph
 * @author Dominik Grzelak
 */
public class AbstractTransitionSystem<B extends Bigraph<? extends Signature<?>>> {
    protected final Map<String, B> stateMap = new ConcurrentHashMap<>();
    protected final Map<String, B> transitionMap = new ConcurrentHashMap<>();

    protected boolean canonicalNodeLabel = false;

    protected Supplier<String> aSup = null;

    public void addState(String sourceLbl, B source) {
        stateMap.put(sourceLbl, source);
    }

    public void addTransition(String reactionLbl, B reaction) {
        transitionMap.put(reactionLbl, reaction);
    }

    protected Supplier<String> createSupplier() {
        return new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return "a_" + id++;
            }
        };
    }

    /**
     * Get the actual redex of a rule by querying the string label of a transition.
     * <p>
     * Note that the label could either be an arbitrary string (user-defined) or the canonical form of the bigraph (redex).
     *
     * @return a map where transition labels are mapped to bigraphs (redexes)
     */
    public Map<String, B> getTransitionMap() {
        return transitionMap;
    }

    /**
     * Get the actual bigraph by querying the string label of a state.
     * <p>
     * Note that the label could either be an arbitrary string (user-defined) or the canonical form of the bigraph.
     *
     * @return a map where state labels are mapped to bigraphs
     */
    public Map<String, B> getStateMap() {
        return stateMap;
    }

    /**
     * Check if a bigraph is present in the graph as a state. The bigraph is identified by its label.
     * <p>
     * The label of a bigraph must be unique.
     *
     * @param label The unique label of a bigraph to find in the reaction graph.
     * @return {@code true}, if the bigraph is contained in the graph, otherwise {@code false}
     */
    public boolean containsBigraph(String label) {
        return stateMap.containsKey(label);
    }
}
