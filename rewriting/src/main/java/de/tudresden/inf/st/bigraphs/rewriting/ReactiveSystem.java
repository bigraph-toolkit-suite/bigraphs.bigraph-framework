package de.tudresden.inf.st.bigraphs.rewriting;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.ReactionGraph;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.predicates.ReactiveSystemPredicates;
import org.jgrapht.GraphPath;

import java.util.Collection;

/**
 * Base interface for bigraphical reactive systems.
 * <p>
 * When a reactive system is executed based on a rule set then it synthesizes a labelled transition system which
 * represents the behavior of the bigraphical reactive system.
 *
 * @author Dominik Grzelak
 */
public interface ReactiveSystem<B extends Bigraph<? extends Signature<?>>> {

    /**
     * Return the labels of the transition system which are called reaction rules for BRS.
     *
     * @return the labels of the transition system
     */
    Collection<ReactionRule<B>> getReactionRules();

    B getAgent();

    /**
     * Checks whether the bigraphical reactive system is simple. A BRS is simple if all its reaction rules are so.
     *
     * @return {@code true} if the BRS is simple, otherwise {@code false}
     */
    default boolean isSimple() {
        return getReactionRules().stream().allMatch(ReactionRule::isRedexSimple);
    }

    void setReactiveSystemListener(ReactiveSystemListener<B> reactiveSystemListener);

    interface ReactiveSystemListener<B extends Bigraph<? extends Signature<?>>> {

        default void onReactiveSystemStarted() {
        }

        default void onCheckingReactionRule(ReactionRule<B> reactionRule) {
        }

        default void onReactiveSystemFinished() {
        }

        default void onUpdateReactionRuleApplies() {
        }

        /**
         * Reports a violation of a predicate and supplies a counter-example trace from the starting state to the violating state
         * to the method.
         *
         * @param currentAgent
         * @param predicate
         * @param counterExampleTrace
         */
        default void onPredicateViolated(B currentAgent, ReactiveSystemPredicates<B> predicate, GraphPath<String, ReactionGraph.LabeledEdge> counterExampleTrace) {
        }

        default void onAllPredicateMatched(B currentAgent) {
        }

    }
}
