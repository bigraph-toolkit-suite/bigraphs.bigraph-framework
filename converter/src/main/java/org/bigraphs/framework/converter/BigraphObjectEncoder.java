package org.bigraphs.framework.converter;

import org.bigraphs.framework.core.Bigraph;

public interface BigraphObjectEncoder<T, B extends Bigraph<?>> {

//    T encode();

    T encode(B bigraph);
}
