package de.tudresden.inf.st.bigraphs.simulation;

import com.google.common.collect.BiMap;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.simulation.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.predicates.ReactiveSystemPredicates;

import java.util.Collection;
import java.util.List;

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

    List<ReactiveSystemPredicates<B>> getPredicates();

    B buildGroundReaction(final B agent, final BigraphMatch<B> match, ReactionRule<B> rule);

    B buildParametricReaction(final B agent, final BigraphMatch<B> match, ReactionRule<B> rule);

    /**
     * Checks whether the bigraphical reactive system is simple. A BRS is simple if all its reaction rules are so.
     *
     * @return {@code true} if the BRS is simple, otherwise {@code false}
     */
    default boolean isSimple() {
        return getReactionRules().stream().allMatch(ReactionRule::isRedexSimple);
    }
}
