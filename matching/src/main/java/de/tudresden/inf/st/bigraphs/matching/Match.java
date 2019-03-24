package de.tudresden.inf.st.bigraphs.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;

/**
 * A match result for a ground bigraph
 *
 * @param <B>
 */
public interface Match<B extends Bigraph> {

    B getContext();

    B getRedex();

    B getRedexImage();

    B getRedexIdentity();

}
