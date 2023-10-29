package org.bigraphs.framework.visualization;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionGraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

//TODO a dot exporter for reaction graphs? https://github.com/bigmc/bigmc/blob/master/src/graph.cpp next to JGraphT

/**
 * A graphics exporter for reaction graphs.
 *
 * @author Dominik Grzelak
 */
public class ReactionGraphExporter<B extends Bigraph<? extends Signature<?>>> implements BigraphGraphicsExporter<ReactionGraph<?>> {
    private final Logger logger = LoggerFactory.getLogger(ReactionGraphExporter.class);
    ReactiveSystem<B> reactiveSystem;
    ReactionGraph<B> bReactionGraph;

    /**
     * Constructor requires the BRS that is being model checking.
     *
     * @param reactiveSystem the system that is being model checked
     */
    public ReactionGraphExporter(ReactiveSystem<B> reactiveSystem) {
        this.reactiveSystem = reactiveSystem;
    }

    /**
     * Exports a reaction graph as a PNG file.
     * The reaction graph is the result of the model checking process over the reactive system passed
     * to the constructor of this graphics exporter.
     *
     * @param bReactionGraph the result of model checking the BRS that was passed to the constructor
     * @param file           the filename
     * @throws IOException
     */
    @Override
    public void toPNG(ReactionGraph<?> bReactionGraph, File file) throws IOException {
//        Graph g = bReactionGraph.getGraph();
        mxReactionGraph graphAdapter = new mxReactionGraph(bReactionGraph, reactiveSystem);
        graphAdapter.getStylesheet().putCellStyle("MATCHED", StyleConstants.predicateMatchedNodeStylesheet());
        graphAdapter.getStylesheet().putCellStyle("DEFAULT", StyleConstants.defaultNodeStylesheet());
        graphAdapter.getStylesheet().putCellStyle("DEFAULT_EDGE", StyleConstants.defaultEdgeStylesheet());
//        mxIGraphLayout layout = new mxCompactTreeLayout(graphAdapter);
        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter, SwingConstants.NORTH);
//        ((mxHierarchicalLayout) layout).setFineTuning(true);
//        ((mxHierarchicalLayout) layout).setResizeParent(true);
        layout.execute(graphAdapter.getDefaultParent());

        BufferedImage image =
                mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, false, null);
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IOException("Create new file failed.");
                }
            }
            if (image == null) {
                logger.warn("Image is null, cannot write image.");
            } else {
                ImageIO.write(image, "PNG", file);
            }
        } catch (IOException e) {
//            e.printStackTrace();
            logger.error(e.toString());
        }
    }

    @Override
    public BigraphGraphicsExporter<ReactionGraph<?>> with(GraphicalFeatureSupplier<?> supplier) {
        throw new RuntimeException("Not implemented yet.");
    }
}
