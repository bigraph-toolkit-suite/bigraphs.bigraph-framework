package de.tudresden.inf.st.bigraphs.simulation.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.ElementaryBigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;

import java.util.Collection;

/**
 * This interface represents a result of a bigraph matching and is used by the {@link BigraphMatchingEngine}.
 *
 * @param <B> the bigraph type of the match
 * @author Dominik Grzelak
 * @see BigraphMatchingEngine
 */
public interface BigraphMatch<B extends Bigraph<? extends Signature<?>>> {

    /**
     * The context of the match
     *
     * @return the context
     */
    B getContext();

    /**
     * Identity link graph for the composition of the context and the redex image (i.e., redex composed with parameters).
     *
     * @return the identity link graph for the context
     */
    Bigraph<? extends Signature> getContextIdentity();

    /**
     * Returns the redex of the reaction rule.
     *
     * @return the redex of the reaction rule
     */
    B getRedex();

    /**
     * juxtaposition of the redex and a suitable identity is called the redex image
     *
     * @return the composed redex and identity link graph
     */
    B getRedexImage();

    /**
     * Get the identity link graph of the redex to build the "redex image".
     *
     * @return the identity link graph of the redex
     */
    ElementaryBigraph getRedexIdentity();

    /**
     * Get all parameters of the reaction rules as a list
     *
     * @return the parameters of the reaction rule
     */
    Collection<B> getParameters();

}
