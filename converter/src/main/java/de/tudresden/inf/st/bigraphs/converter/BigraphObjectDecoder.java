package de.tudresden.inf.st.bigraphs.converter;

import de.tudresden.inf.st.bigraphs.core.Bigraph;

public interface BigraphObjectDecoder<B extends Bigraph<?>, T> {

    B decode(T bigraph);
}
