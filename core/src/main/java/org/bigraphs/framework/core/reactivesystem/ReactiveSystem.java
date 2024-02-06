package org.bigraphs.framework.core.reactivesystem;

import com.google.common.collect.BiMap;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

import java.util.Collection;

/**
 * Base interface for bigraphical reactive systems.
 * <p>
 * When a reactive system is executed based on a rule set then it synthesizes a labelled transition system which
 * represents the behavior of the bigraphical reactive system.
 * <p>
 * The interface offers methods to build the result of a reaction.
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

    BiMap<String, ReactionRule<B>> getReactionRulesMap();

    B getAgent();

    Signature getSignature();

    Collection<ReactiveSystemPredicate<B>> getPredicates();

    BiMap<String, ReactiveSystemPredicate<B>> getPredicateMap();

    B buildGroundReaction(final B agent, final BigraphMatch<B> match, final ReactionRule<B> rule);

    B buildParametricReaction(final B agent, final BigraphMatch<B> match, final ReactionRule<B> rule);

    /**
     * Checks whether the bigraphical reactive system is simple. A BRS is simple if all its reaction rules are so.
     *
     * @return {@code true} if the BRS is simple, otherwise {@code false}
     */
    default boolean isSimple() {
        return getReactionRules().stream().allMatch(ReactionRule::isRedexSimple);
    }

//    boolean addReactionRule(ReactionRule<B> reactionRule) throws InvalidReactionRuleException;
//    ReactiveSystem<B> setAgent(B initialAgent);
}
