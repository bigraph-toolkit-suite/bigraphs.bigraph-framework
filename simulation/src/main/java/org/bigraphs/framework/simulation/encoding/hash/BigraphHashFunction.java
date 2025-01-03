package org.bigraphs.framework.simulation.encoding.hash;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;

/**
 * Base interface to implement hash functions for bigraphs.
 * Note that some hash functions may not necessarily compute unique hashes. It may be a computational efficient
 * implementation which just considers the interfaces but not the overall structure of the bigraph.
 *
 * @author Dominik Grzelak
 */
public interface BigraphHashFunction<B extends Bigraph<? extends Signature<?>>> {

    /**
     * Factory method to create the respective hashing function class for the given bigraph class {@code bClass}.
     *
     * @param bClass the class of the bigraph variant (e.g., {@link PureBigraph}).
     * @param <B>    the bigraph class type
     * @return the hashing function for the bigraph class type
     */
    @SuppressWarnings("unchecked")
    static <B extends Bigraph<? extends Signature<?>>> BigraphHashFunction<B> get(Class<B> bClass) {
        if (bClass.equals(PureBigraph.class)) {
            return (BigraphHashFunction<B>) new PureBigraphHash();
        }
        throw new RuntimeException("Not yet implemented.");
    }

    /**
     * Compute a hash for a given bigraph.
     *
     * @param bigraph the bigraph
     * @return a hash of a bigraph.
     */
    long hash(B bigraph);


}
