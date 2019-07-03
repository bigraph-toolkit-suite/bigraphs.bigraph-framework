package de.tudresden.inf.st.bigraphs.rewriting.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.ElementaryBigraph;

import java.util.Collection;

/**
 * A result of a matching of type {@code B}.
 *
 * @param <B> the bigraph type of the match
 * @author Dominik Grzelak
 */
public interface BigraphMatch<B extends Bigraph<?>> {

    B getContext();

    /**
     * Identity link graph for the composition of the context and the redex image (with params)
     *
     * @return
     */
    Bigraph getContextIdentity();

    /**
     * Returns the redex of the reaction rule.
     *
     * @return the redex of the reaction rule
     */
    B getRedex();

    /**
     * juxtaposition of the redex and a suitable identity is called the redex image
     *
     * @return
     */
    B getRedexImage();

    /**
     * identity for the redex image
     *
     * @return
     */
    ElementaryBigraph getRedexIdentity();

    Collection<B> getParameters();

}
