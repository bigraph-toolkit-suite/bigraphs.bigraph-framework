package org.bigraphs.framework.converter;

import org.bigraphs.framework.core.Bigraph;

public interface BigraphObjectDecoder<B extends Bigraph<?>, T> {

    B decode(T bigraph);
}
