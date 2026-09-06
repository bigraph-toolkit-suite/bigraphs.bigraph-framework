/*
 * Copyright (c) 2022-2025 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.visualization;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxRectangle;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A graphics exporter for reaction graphs.
 *
 * @author Dominik Grzelak
 */
public class ReactionGraphExporter<B extends Bigraph<? extends Signature<?>>> implements BigraphGraphicsExporter<ReactionGraph<B>> {
    private final Logger logger = LoggerFactory.getLogger(ReactionGraphExporter.class);
    ReactiveSystem<B> reactiveSystem;
    ReactionGraph<B> bReactionGraph;
    boolean useSimpleStateLabel = true;

    /**
     * Constructor requires the BRS that is being model checking.
     *
     * @param reactiveSystem the system that is being model checked
     */
    public ReactionGraphExporter(ReactiveSystem<B> reactiveSystem) {
        this.reactiveSystem = reactiveSystem;
    }

    public ReactionGraphExporter(ReactiveSystem<B> reactiveSystem, boolean useSimpleStateLabel) {
        this.reactiveSystem = reactiveSystem;
        this.useSimpleStateLabel = useSimpleStateLabel;
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
    public void toPNG(ReactionGraph<B> bReactionGraph, File file) throws IOException {
        mxReactionGraph graphAdapter = new mxReactionGraph(bReactionGraph, reactiveSystem, useSimpleStateLabel);
        graphAdapter.getStylesheet().putCellStyle("MATCHED", StyleConstants.predicateMatchedNodeStylesheet());
        graphAdapter.getStylesheet().putCellStyle("DEFAULT", StyleConstants.defaultNodeStylesheet());
        graphAdapter.getStylesheet().putCellStyle("DEFAULT_EDGE", StyleConstants.defaultEdgeStylesheet());
//        mxIGraphLayout layout = new mxCompactTreeLayout(graphAdapter);
        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter, SwingConstants.NORTH);
        layout.execute(graphAdapter.getDefaultParent());

        mxRectangle bounds = graphAdapter.getGraphBounds();

        final double preferredScale = 2.0;
        final double maxWidth = 4000.0;
        final double maxHeight = 4000.0;

        double scaleX = maxWidth / bounds.getWidth();
        double scaleY = maxHeight / bounds.getHeight();

        double scale = Math.min(
                preferredScale,
                Math.min(scaleX, scaleY)
        );

        BufferedImage image =
                mxCellRenderer.createBufferedImage(
                        graphAdapter,
                        null,
                        scale,
                        Color.WHITE,
                        false,
                        null
                );

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
            logger.error(e.toString());
        }
    }

    @Override
    public BigraphGraphicsExporter<ReactionGraph<B>> with(GraphicalFeatureSupplier<?> supplier) {
        throw new RuntimeException("Not implemented yet.");
    }
}
