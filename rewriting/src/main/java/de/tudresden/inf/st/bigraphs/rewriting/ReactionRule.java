package de.tudresden.inf.st.bigraphs.rewriting;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.InstantiationMap;

/**
 * Common interface for reaction rules.
 * <p>
 * A reaction rule is a data structure containing the redex, reactum and an instantiation map.
 *
 * @param <B> type of the bigraph
 * @author Dominik Grzelak
 */
public interface ReactionRule<B extends Bigraph<? extends Signature<?>>> {

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
        return getRedex().isEpimorphic() && getRedex().isMonomorphic() &&
                getRedex().isGuarding() && //"doesn't contain inner names" is checked in the guarding clause
                getRedex().getEdges().size() == 0; // every link is open
    }

    boolean isReversable();

    /**
     * Return the instantiation map of the parametric reaction rule
     *
     * @return the instantiation map of the parametric reaction rule
     */
    InstantiationMap getInstantationMap();
}
