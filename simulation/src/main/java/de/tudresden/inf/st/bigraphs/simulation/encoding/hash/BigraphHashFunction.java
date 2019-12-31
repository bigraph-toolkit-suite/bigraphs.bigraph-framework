package de.tudresden.inf.st.bigraphs.simulation.encoding.hash;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;

/**
 * @author Dominik Grzelak
 */
public interface BigraphHashFunction<B extends Bigraph<? extends Signature<?>>> {
    long hash(B bigraph);

    static <B extends Bigraph<? extends Signature<?>>> BigraphHashFunction<B> get(Class<B> bClass) {
        if (bClass.equals(PureBigraph.class)) {
            return (BigraphHashFunction<B>) new PureBigraphHash();
        }
        throw new RuntimeException("Not yet implemented.");
    }
}
