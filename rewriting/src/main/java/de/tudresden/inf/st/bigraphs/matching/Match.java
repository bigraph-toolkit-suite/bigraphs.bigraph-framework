package de.tudresden.inf.st.bigraphs.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.matching.impl.AbstractDynamicMatchAdapter;

import java.util.Collection;

/**
 * A match result for a ground bigraph
 *
 * @param <B>
 */
public interface Match<B extends Bigraph> {

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
