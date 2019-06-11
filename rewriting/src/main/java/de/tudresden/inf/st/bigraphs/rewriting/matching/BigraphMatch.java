package de.tudresden.inf.st.bigraphs.rewriting.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;

import java.util.Collection;

/**
 * A result of a matching of type {@code B}.
 *
 * @param <B> the bigraph type of the match
 * @author Dominik Grzelak
 */
public interface BigraphMatch<B extends Bigraph> {

    B getContext();

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
    B getRedexIdentity();

    Collection<B> getParameter();

}
