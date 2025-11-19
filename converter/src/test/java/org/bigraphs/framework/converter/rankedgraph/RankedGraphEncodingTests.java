/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.converter.rankedgraph;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.bigraphs.framework.core.AbstractRankedGraph;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.visualization.BigraphRankedGraphExporter;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.junit.jupiter.api.Test;

// Place graph hierarchy is encoded as attribute "parent"
// hierarchy is als reflected by the id of the nodes (to maintain the parent relationship)

//DPO rule: attribute count muss übereinstimmen von agent und RR, dürfen nicht leer sein sonst segfault in GMTE
//das gilt nur für nodes, bei edges ist es scheinbar egal

//(siehe: http://homepages.laas.fr/khalil/GMTE/index.php?n=GMTE.Tutorials)
//der redex in inv zone

/**
 * @author Dominik Grzelak
 */
public class RankedGraphEncodingTests {
    private final static String TARGET_TEST_PATH = "src/test/resources/dump/rankedgraphs/";
//    private PureBigraphFactory factory = pure();

    @Test
    void name2() throws TypeNotExistsException, InvalidConnectionException, IOException {
        PureBigraph bigraph = createBigraphA();
//        BigraphGraphvizExporter.toPNG(bigraph, true, new File("bigraph-example.png"));
        PureBigraphRankedGraphEncoding graphEncoding = new PureBigraphRankedGraphEncoding(bigraph);
//        graphEncoding.encode();
//        Graph<AbstractRankedGraph.LabeledNode, AbstractRankedGraph.LabeledEdge> graph = graphEncoding.getGraph();
//        drawGraph(graphEncoding.getGraph());
        new BigraphRankedGraphExporter().toPNG(graphEncoding, new File(TARGET_TEST_PATH + "graph2.png"));
    }

    private void drawGraph(Graph graph) throws IOException {
        JGraphXAdapter graphAdapter = new JGraphXAdapter(graph);

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

        graphAdapter.setCellStyle("shape=ellipse;fontColor=green;strokeColor=green;fillColor=white", filteredLinkNodes);
        graphAdapter.setCellStyle("shape=ellipse;fillColor=none;strokeColor=black;fontColor=black", filteredPlaceNodes);
        graphAdapter.setCellStyle("strokeWidth=0;fillColor=none;strokeColor=none", filteredRoots);
        graphAdapter.setCellStyle("strokeWidth=0;fillColor=none;strokeColor=none", filteredVariables);


        graphAdapter.setCellStyle("dashed=1;strokeColor=black", filteredEdgesForInterface);
        graphAdapter.setCellStyle("strokeColor=green", filteredEdgesForNodes);
        graphAdapter.setCellStyle("strokeColor=black", filteredEdgesForNodes2Node);

//        graphAdapter.moveCells(filteredVariables, 0, doubleSummaryStatistics2.getMax(), false);
        graphAdapter.alignCells("bottom", filteredVariables);
        graphAdapter.alignCells("top", filteredRoots);
//        graphAdapter.
        DoubleSummaryStatistics rootInterfacePositionEstimate = Arrays.stream(filteredRoots)
                .mapToDouble(x -> graphAdapter.getBoundingBox(x).getX()).summaryStatistics();
        double newPosRoot = rootInterfacePositionEstimate.getAverage() - averagePosition.getAverage() * averageHeight.getAverage();
        graphAdapter.moveCells(filteredRoots, 0, newPosRoot * 2, false);
//        graphAdapter.getM
        graphAdapter.getModel().endUpdate();


        BufferedImage image =
                mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        File imgFile = new File(TARGET_TEST_PATH + "graph.png");
//        imgFile.createNewFile();
        ImageIO.write(image, "PNG", imgFile);

        assertTrue(imgFile.exists());
    }


    public static void printAll(Document doc) {
        try {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(doc, System.out);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * write the xml document into file
     *
     * @param file the file name
     * @param doc  xml document
     */
    public static void save(String file, Document doc) {
        System.out.println("### document saved in : " + file);
        try {
            XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
            sortie.output(doc, new java.io.FileOutputStream(file));
        } catch (java.io.IOException e) {
        }
    }


    private PureBigraph createBigraphA() throws InvalidConnectionException, TypeNotExistsException, IOException {
        PureBigraphBuilder<DynamicSignature> builder;
        DynamicSignature signature = createExampleSignature();
        builder = pureBuilder(signature);

        BigraphEntity.InnerName x0 = builder.createInner("x0");
        BigraphEntity.InnerName e0 = builder.createInner("e0");
        BigraphEntity.InnerName e1 = builder.createInner("e1");

        builder.root()
                .child("K").linkInner(e0).down().child("K").linkInner(e0).down().site().up().up()
                .child("M").linkInner(e0).linkInner(e1);
        builder.root().child("L").linkInner(e1).linkInner(x0).down().site();
        builder.closeInner(e0);
        builder.closeInner(e1);

        PureBigraph bigraph = builder.create();

        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, new FileOutputStream(TARGET_TEST_PATH + "test_instance-model.xmi"));

        return bigraph;
    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder
                .add("M", 2)
                .add("K", 1)
                .add("L", 2)
        ;

        return (S) signatureBuilder.create();
    }
}
