package de.tudresden.inf.st.bigraphs.visualization;

import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionGraph;

import java.io.File;
import java.io.IOException;

//TODO a dot exporter for reaction graphs? https://github.com/bigmc/bigmc/blob/master/src/graph.cpp next to JGraphT

/**
 * A graphics exporter for reaction graphs.
 *
 * @author Dominik Grzelak
 */
public class ReactionGraphExporter implements BigraphGraphicsExporter<ReactionGraph<?>> {

    @Override
    public void toPNG(ReactionGraph<?> reactionGraph, File file) throws IOException {
        //TODO insert BMC operation here
    }

    @Override
    public BigraphGraphicsExporter<ReactionGraph<?>> with(GraphicalFeatureSupplier<?> supplier) {
        throw new RuntimeException("Not implemented yet.");
    }


}
