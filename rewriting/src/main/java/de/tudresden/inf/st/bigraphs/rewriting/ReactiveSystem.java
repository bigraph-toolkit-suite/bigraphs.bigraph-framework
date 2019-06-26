package de.tudresden.inf.st.bigraphs.rewriting;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;

import java.util.Collection;

/**
 * Base interface for bigraphical reactive systems.
 * <p>
 * When a reactive system is executed based on a rule set then it creates a labelled transition system which
 * represents the behavior of the bigraphical reactive system.
 *
 * @author Dominik Grzelak
 */
public interface ReactiveSystem<S extends Signature, B extends Bigraph<S>> {

    //TODO: add id/label supplier, add this also to RR and agent (create interface for that)
    //TODO: add list of applicants (is updated after each match and user is notified by a callback
    //TODO: add transitiontriple (is updated after each match and user is notified by a callback
    // or when the execution stops

    /**
     * Return the labels of the transition system which are called reaction rules for BRS.
     *
     * @return the labels of the transition system
     */
    Collection<ReactionRule<B>> getReactionRules();

    /**
     * Checks whether the bigraphical reactive system is simple. A BRS is simple if all its reaction rules are so.
     *
     * @return {@code true} if the BRS is simple, otherwise {@code false}
     */
    default boolean isSimple() {
        return getReactionRules().stream().allMatch(ReactionRule::isRedexSimple);
    }

    void setReactiveSystemListener(ReactiveSystemListener reactiveSystemListener);

    interface ReactiveSystemListener {

        void onReactiveSystemStarted();

        void onReactiveSystemFinished();

        void onUpdateReactionRuleApplies();

    }
}
