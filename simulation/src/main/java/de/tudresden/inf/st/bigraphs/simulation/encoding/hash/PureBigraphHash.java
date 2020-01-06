package de.tudresden.inf.st.bigraphs.simulation.encoding.hash;

import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;

/**
 * @author Dominik Grzelak
 */
public class PureBigraphHash implements BigraphHashFunction<PureBigraph> {

    @Override
    public long hash(PureBigraph bigraph) {
        return bigraph.getAllPlaces().size() +
                bigraph.getEdges().size() +
                bigraph.getOuterNames().size() +
                bigraph.getInnerNames().size()
                ;
    }
}
