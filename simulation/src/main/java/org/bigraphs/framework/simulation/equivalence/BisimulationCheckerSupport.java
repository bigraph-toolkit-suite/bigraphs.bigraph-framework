package org.bigraphs.framework.simulation.equivalence;

import org.apache.commons.math3.util.Pair;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.AbstractTransitionSystem;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * This implementation provides a basic extensible (template) method for computing bisimilarity of LTSs.
 * The type of the transition system is those whose transition relations are reaction rules, and states are bigraphs.
 * <p>
 * This class contains the {@link #checkBisimulation(AbstractTransitionSystem, Bigraph, AbstractTransitionSystem, Bigraph)}
 * method that implements the bisimulation check using a breadth-first search approach.
 * <p>
 * It has quadratic runtime.
 * Depending on the complexity of the transition systems, this method needs to be adapted with more sophisticated logic.
 *
 * @param <B>   the type of the state of {@code <AST>}
 * @param <T>   the type of the transition relations of {@code <AST>}
 * @param <AST> the type of the transition system
 * @author Dominik Grzelak
 */
public class BisimulationCheckerSupport<B extends Bigraph<? extends Signature<?>>, T extends ReactionRule<B>, AST extends AbstractTransitionSystem<B, T>> {
    public boolean checkBisimulation(AST system1, B initialState1, AST system2, B initialState2) {
        Queue<Pair<B, B>> queue = new LinkedList<>();
        Set<Pair<B, B>> visited = new HashSet<>();

        queue.add(new Pair<>(initialState1, initialState2));
        visited.add(new Pair<>(initialState1, initialState2));

        while (!queue.isEmpty()) {
            Pair<B, B> pair = queue.poll();
            B state1 = pair.getFirst();
            B state2 = pair.getSecond();

            if (!areBisimilar(system1, state1, system2, state2)) {
                return false;
            }

            //TODO change transition type
            Set<B> transitions1 = system1.getTransitions(state1);
            Set<B> transitions2 = system2.getTransitions(state2);
            //TODO change transition type
            for (B transition1 : transitions1) {
                for (B transition2 : transitions2) {
                    Pair<B, B> nextPair = new Pair<>(transition1, transition2);
                    if (!visited.contains(nextPair)) {
                        queue.add(nextPair);
                        visited.add(nextPair);
                    }
                }
            }
        }

        return true;
    }

    /**
     * The areBisimilar() method compares the transitions of two states from different transition systems to determine if they are bisimilar.
     * It checks if the number of transitions is the same and then verifies that each transition in one system has a corresponding transition in the other system.
     * You can customize the logic inside the method based on specific requirements and the nature of transitions in your transition systems.
     * <p>
     * Method areBisimilar() Details:
     * <p>
     * Input: Two states, one from each transition system.
     * Output: Boolean value indicating whether the states are bisimilar.
     *
     * @param system1
     * @param state1
     * @param system2
     * @param state2
     * @return
     */
    protected boolean areBisimilar(AST system1, B state1, AST system2, B state2) {
        // Implement bisimulation check logic here
        // Return true if states are bisimilar, false otherwise
//        return true; // Placeholder logic
        //TODO change transition type
        Set<B> transitions1 = system1.getTransitions(state1);
        Set<B> transitions2 = system2.getTransitions(state2);

        // Check if the number of transitions is the same
        if (transitions1.size() != transitions2.size()) {//TODO necessary?
            return false;
        }


        // Check if each transition in system 1 has a corresponding transition in system 2
        //TODO change transition type
        for (B transition1 : transitions1) {
            boolean hasCorrespondingTransition = false;
            for (B transition2 : transitions2) {
                // Implement logic to check if transitions are equivalent
                if (transition1.equals(transition2)) {//TODO compare bigraphs here: make template method
                    hasCorrespondingTransition = true;
                    break;
                }
            }
            if (!hasCorrespondingTransition) {
                return false;
            }
        }

        return true;
    }
}
