package de.tudresden.inf.st.bigraphs.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;

public interface ParametricMatch<B extends Bigraph> extends Match<B> {
    B getParameter();
}
