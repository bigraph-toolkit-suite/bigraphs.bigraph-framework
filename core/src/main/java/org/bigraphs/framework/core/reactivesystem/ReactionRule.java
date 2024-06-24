package org.bigraphs.framework.core.reactivesystem;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * Common interface for reaction rules.
 * <p>
 * A reaction rule is a data structure containing the redex, reactum and an instantiation map.
 *
 * @param <B> type of the bigraph
 * @author Dominik Grzelak
 */
public interface ReactionRule<B extends Bigraph<? extends Signature<?>>> extends HasLabel {

    /**
     * Return the redex of the reaction rule
     *
     * @return the redex of the reaction rule
     */
    B getRedex();

    /**
     * Return the reactum of the reaction rule
     *
     * @return the reactum of the reaction rule
     */
    B getReactum();

    /**
     * Checks whether the parametric redex is simple.
     *
     * @return {@code true} if the redex of the reaction rule is simple, otherwise {@code false}
     */
    default boolean isRedexSimple() {
        return
//                getRedex().isEpimorphic() &&
                getRedex().getEdges().isEmpty() && // every link is open
                        getRedex().isGuarding() && //no site has a root as parent (+ no idle inner names)
                        getRedex().isMonomorphic(); // inner-injective
    }

    /**
     * Checks if a parametric reaction rule is well-defined subject to the constraints:
     * <ul>
     *     <li>Redex and Reactum are lean (=no idle edges)</li>
     *     <li>R has no idle roots</li>
     *     <li>R has no idle names</li>
     * </ul>
     *
     * @return {@code true} if the parametric reaction rule is well-defined, otherwise {@code false}
     */
    default boolean isProperParametricRule() {
        return getRedex().isLean() && getReactum().isLean() && // both must be lean
                getRedex().isEpimorphic(); // only the redex must have no idle roots and names
    }

    /**
     * A rule is parametric if its redex contains sites.
     *
     * @return {@code true}, if the rule is a parametric reaction rule.
     */
    default boolean isParametricRule() {
        return getRedex().getSites().size() > 0;
    }

    /**
     * A flag that indicates whether the rule can be also executed in reverse.
     * For a rule R -> R' it means that R' -> R can also be used.
     * <p>
     * This flag is usually evaluated in a reactive system class.
     *
     * @return {@code true}, if the rule can also be used reversely, otherwise {@code false}
     */
    boolean isReversible();

    /**
     * Return the instantiation map of the parametric reaction rule
     *
     * @return the instantiation map of the parametric reaction rule
     */
    InstantiationMap getInstantationMap();


    /**
     * Returns the tracking map if one was specified for the rule.
     * A tracking map helps to trace node and link identities through reactions.
     *
     * @return the tracking map of the rule
     */
    TrackingMap getTrackingMap();
}
