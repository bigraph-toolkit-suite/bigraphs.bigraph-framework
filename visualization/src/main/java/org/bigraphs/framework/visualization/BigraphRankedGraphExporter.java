package org.bigraphs.framework.visualization;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxCellRenderer;
import org.bigraphs.framework.core.AbstractRankedGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;

/**
 * Exports a bigraph, encoded as a ranked graph, to a diagram.
 *
 * @author Dominik Grzelak
 */
public class BigraphRankedGraphExporter implements BigraphGraphicsExporter<AbstractRankedGraph<?, ?, ?>> {

    @Override
    public void toPNG(AbstractRankedGraph<?, ?, ?> rankedGraphEncoding, File output) throws IOException {
        if (!rankedGraphEncoding.isEncodingFinished() && !rankedGraphEncoding.isEncodingStarted()) {
            rankedGraphEncoding.encode();
        }
        drawGraph(rankedGraphEncoding.getGraph(), output);
    }

    @Override
    public BigraphGraphicsExporter<AbstractRankedGraph<?, ?, ?>> with(GraphicalFeatureSupplier<?> supplier) {
        throw new RuntimeException("Not implemented yet.");
    }


    private void drawGraph(Graph<?, ?> graph, File imgFile) throws IOException {
        JGraphXAdapter<?, ?> graphAdapter = new JGraphXAdapter<>(graph);

//        mxHierarchicalLayout layout = new mxHierarchicalLayout(graphAdapter, SwingConstants.WEST);
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graphAdapter, SwingConstants.SOUTH);
        ((mxHierarchicalLayout) layout).setFineTuning(true);
        ((mxHierarchicalLayout) layout).setResizeParent(true);
        graphAdapter.setResetEdgesOnMove(true);
        graphAdapter.setResetEdgesOnResize(true);
        graphAdapter.setResetEdgesOnConnect(true);

        graphAdapter.getModel().beginUpdate();
        layout.execute(graphAdapter.getDefaultParent());
        Object[] ver = graphAdapter.getChildCells(graphAdapter.getDefaultParent());
        DoubleSummaryStatistics averagePosition = Arrays.stream(ver).mapToDouble(x -> graphAdapter.getBoundingBox(x).getX()).summaryStatistics();
        DoubleSummaryStatistics averageHeight = Arrays.stream(ver).mapToDouble(x -> graphAdapter.getBoundingBox(x).getHeight()).summaryStatistics();

        Object[] filteredVariables = Arrays.stream(ver).map(x -> (mxCell) x)
                .filter(x -> x.isVertex() && ((AbstractRankedGraph.LabeledNode) x.getValue()).isVariableNode())
                .toArray(Object[]::new);
        Object[] filteredRoots = Arrays.stream(ver).map(x -> (mxCell) x)
                .filter(x -> x.isVertex() && ((AbstractRankedGraph.LabeledNode) x.getValue()).isRootNode())
                .toArray(Object[]::new);
        Object[] filteredLinkNodes = Arrays.stream(ver).map(x -> (mxCell) x)
                .filter(x -> x.isVertex() && ((AbstractRankedGraph.LabeledNode) x.getValue()).isLinkNode())
                .toArray(Object[]::new);
        Object[] filteredPlaceNodes = Arrays.stream(ver).map(x -> (mxCell) x)
                .filter(x -> x.isVertex() && ((AbstractRankedGraph.LabeledNode) x.getValue()).isPlaceNode())
                .toArray(Object[]::new);
        Object[] filteredEdgesForInterface = Arrays.stream(ver).map(x -> (mxCell) x).filter(mxCell::isEdge)
                .filter(x -> {
                    AbstractRankedGraph.LabeledNode source = (AbstractRankedGraph.LabeledNode) ((AbstractRankedGraph.LabeledEdge) x.getValue()).getSource();
                    return source.isInterfaceNode();
                })
                .toArray(Object[]::new);
        Object[] filteredEdgesForNodes = Arrays.stream(ver).map(x -> (mxCell) x).filter(mxCell::isEdge)
                .filter(x -> {
                    AbstractRankedGraph.LabeledNode source = (AbstractRankedGraph.LabeledNode) ((AbstractRankedGraph.LabeledEdge) x.getValue()).getSource();
                    AbstractRankedGraph.LabeledNode target = (AbstractRankedGraph.LabeledNode) ((AbstractRankedGraph.LabeledEdge) x.getValue()).getTarget();
                    return !source.isInterfaceNode() && target.isLinkNode();
                })
                .toArray(Object[]::new);
        Object[] filteredEdgesForNodes2Node = Arrays.stream(ver).map(x -> (mxCell) x).filter(mxCell::isEdge)
                .filter(x -> {
                    AbstractRankedGraph.LabeledNode source = (AbstractRankedGraph.LabeledNode) ((AbstractRankedGraph.LabeledEdge) x.getValue()).getSource();
                    AbstractRankedGraph.LabeledNode target = (AbstractRankedGraph.LabeledNode) ((AbstractRankedGraph.LabeledEdge) x.getValue()).getTarget();
                    return source.isPlaceNode() && target.isPlaceNode();
                })
                .toArray(Object[]::new);

        // nodes
        graphAdapter.setCellStyle("shape=ellipse;fontColor=green;strokeColor=green;fillColor=white", filteredLinkNodes);
        graphAdapter.setCellStyle("shape=ellipse;fillColor=none;strokeColor=black;fontColor=black", filteredPlaceNodes);
        graphAdapter.setCellStyle("strokeWidth=0;fillColor=none;strokeColor=none", filteredRoots);
        graphAdapter.setCellStyle("strokeWidth=0;fillColor=none;strokeColor=none", filteredVariables);
        // edges
        graphAdapter.setCellStyle("dashed=1;strokeColor=black", filteredEdgesForInterface);
        graphAdapter.setCellStyle("strokeColor=green", filteredEdgesForNodes);
        graphAdapter.setCellStyle("strokeColor=black", filteredEdgesForNodes2Node);

        graphAdapter.alignCells("bottom", filteredVariables);
        graphAdapter.alignCells("top", filteredRoots);

        DoubleSummaryStatistics rootInterfacePositionEstimate = Arrays.stream(filteredRoots)
                .mapToDouble(x -> graphAdapter.getBoundingBox(x).getX()).summaryStatistics();
        double newPosRoot = rootInterfacePositionEstimate.getAverage() - averagePosition.getAverage() * averageHeight.getAverage();
        graphAdapter.moveCells(filteredRoots, 0, newPosRoot * 2, false);
//        graphAdapter.getM
        graphAdapter.getModel().endUpdate();


        BufferedImage image =
                mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        ImageIO.write(image, "PNG", imgFile);
    }
}
