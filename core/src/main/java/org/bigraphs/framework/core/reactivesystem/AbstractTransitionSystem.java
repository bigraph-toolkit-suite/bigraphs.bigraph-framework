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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * Abstract base class representing the minimal data structure for all concrete transition system implementations.
 * This class generalizes all possible variations of transition systems.
 *
 * @param <B> the bigraph type of the states
 * @param <T> the type for the data attached to the transition relations of type {@link Transition}
 * @author Dominik Grzelak
 */
public class AbstractTransitionSystem<B, T> {
    protected Supplier<String> createSupplier() {
        return new Supplier<>() {
            private int id = 0;

            @Override
            public String get() {
                return "a_" + id++;
            }
        };
    }

    /**
     * Inner class representing a transition relation.
     */
    public class Transition {
        private final B source;
        private final B target;
        private final String label;
        private final List<BMatchResult<? extends Bigraph<? extends Signature<?>>>> matchResults;

        public Transition(B source, B target, String label) {
            this(source, target, label, List.of());
        }

        public Transition(B source, B target, String label, List<BMatchResult<? extends Bigraph<? extends Signature<?>>>> matchResults) {
            this.source = source;
            this.target = target;
            this.label = label;
            this.matchResults = matchResults;
        }

        public B getSource() {
            return source;
        }

        public B getTarget() {
            return target;
        }

        public String getLabel() {
            return label;
        }

        public List<BMatchResult<? extends Bigraph<? extends Signature<?>>>> getMatchResults() {
            return matchResults;
        }

        @Override
        public String toString() {
            return "(" + source + ", " + label + ", " + target + ")";
        }
    }

    protected final Set<String> initialStateLabels = new HashSet<>();

    protected final Map<String, B> stateMap = new ConcurrentHashMap<>();

    //TODO can be merged in Transition
    // A transition is labeled. This stores an data object to a transition relation via its label
    protected final Map<String, List<T>> transitionMap = new ConcurrentHashMap<>();

    private final Map<B, Set<Transition>> transitionRelations = new ConcurrentHashMap<>();

    protected boolean canonicalNodeLabel = false;

    protected Supplier<String> aSup = null;

    public void addState(String sourceLbl, B source) {
        if(stateMap.isEmpty()) {
            initialStateLabels.add(sourceLbl);
        }
        stateMap.put(sourceLbl, source);
    }


    public Set<String> getInitialStateLabels() {
        return initialStateLabels;
    }

    public AbstractTransitionSystem<B, T> addInitialStateLabels(String... labels) {
        initialStateLabels.addAll(Arrays.asList(labels));
        return this;
    }

    /**
     * Get the actual redex of a rule by querying the string label of a transition.
     * <p>
     * Note that the label could either be an arbitrary string (user-defined) or the canonical form of the bigraph (redex).
     *
     * @return a map where transition labels are mapped to bigraphs (redexes)
     */
    public Map<String, List<T>> getTransitionMap() {
        return transitionMap;
    }

    public Map<B, Set<Transition>> getTransitionRelations() {
        return transitionRelations;
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

    // Method to get all states in the transition system
    public Set<B> getStates() {
        assert getStateMap().values().size() == transitionRelations.values().stream().mapToLong(Collection::size).sum();
        return new HashSet<>(getStateMap().values());
    }

    public Set<B> getStatesWithoutFinalStates() {
        return transitionRelations.keySet();
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


    /**
     * This methods returns all transitions from a state
     * Thus,
     * it gets all possible next states (represented as {@link Transition})
     * that can be reached from the given state.
     * That is, only out-going transition relations are considered.
     *
     * @param state the state in question to get all successor states.
     * @return possible out-going transitions reachable from a given state
     */
    public Set<Transition> getTransitions(B state) {
        return transitionRelations.getOrDefault(state, new HashSet<>());
    }

    /**
     * This doesnt work with source/target states coming from Transition.
     * Its better to compute the canonical string encoding directly to get the label.
     *
     * @param state
     * @return
     */
    public Optional<String> getLabelByState(B state) {
        // do reverse search in map
        return stateMap.entrySet().stream().filter(e -> e.getValue().equals(state)).map(Map.Entry::getKey).findFirst();
    }

    public Optional<B> getStateByLabel(String stateLabel) {
        return Optional.ofNullable(stateMap.get(stateLabel));
    }


//    public void addTransition(String reactionLbl, T transitionRelationObject) {
//        transitionMap.put(reactionLbl, transitionRelationObject);
//    }

    // Method to add a transition to the transition system
    public void addTransition(B source, B target, String reactionLbl, T transitionRelationObject) {
        transitionMap.computeIfAbsent(reactionLbl, k -> new LinkedList<>());
        transitionMap.get(reactionLbl).add(transitionRelationObject);
        transitionRelations.computeIfAbsent(source, k -> new HashSet<>())
                .add(new Transition(source, target, reactionLbl, (List) List.of(transitionRelationObject)));
    }


    // Method to remove a state from the transition system
    public void removeState(B state) {
        transitionRelations.remove(state);
        // Remove transitions pointing to this state
        transitionRelations.values().forEach(transitions -> transitions.removeIf(t -> t.getTarget().equals(state)));
    }
}
