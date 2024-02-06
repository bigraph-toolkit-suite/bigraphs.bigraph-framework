package org.bigraphs.framework.simulation.encoding.hash;

import org.bigraphs.framework.core.impl.pure.PureBigraph;

/**
 * @author Dominik Grzelak
 */
public class PureBigraphHash implements BigraphHashFunction<PureBigraph> {

    /**
     * Computes a "hash" for a pure bigraph by only considering the number of places, edges, inner and outer names.
     * It does not compute a unique hash. However, it can be used as "pre-check" to early terminate a more complex algorithm.
     *
     * @param bigraph the bigraph
     * @return a possibly non-unique hash
     */
    @Override
    public long hash(PureBigraph bigraph) {
        return bigraph.getAllPlaces().size() +
                bigraph.getEdges().size() +
                bigraph.getOuterNames().size() +
                bigraph.getInnerNames().size()
                ;
    }
}
