package de.tudresden.inf.st.bigraphs.converter;

import de.tudresden.inf.st.bigraphs.core.Bigraph;

public interface BigraphObjectEncoder<T, B extends Bigraph<?>> {

//    T encode();

    T encode(B bigraph);
}
