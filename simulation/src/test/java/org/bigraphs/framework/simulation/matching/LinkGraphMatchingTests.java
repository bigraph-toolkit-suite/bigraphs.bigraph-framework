package org.bigraphs.framework.simulation.matching;

import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.exceptions.*;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.simulation.matching.pure.SubHypergraphIsoSearch;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.apache.commons.io.FileUtils;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.view.Viewer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dominik Grzelak
 */
public class LinkGraphMatchingTests implements org.bigraphs.testing.BigraphUnitTestSupport, BigraphModelChecker.ReactiveSystemListener<PureBigraph> {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/lgm/";

    public LinkGraphMatchingTests() {
    }

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    private DynamicSignature sig() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add("A", 3)
                .add("B", 3)
                .add("C", 3)
        ;
        return defaultBuilder.create();
    }

    // Use Page-Up/Page-Down Buttons and Arrow-Keys to navigate in the GUI
    @Test
    void query_data_matching() throws Exception {
        // Create Data Graph
        PureBigraph dataLinkGraph = createDataLinkGraph();

        // Create Query
        PureBigraph queryLinkGraph = createQueryLinkGraph_1();
//        PureBigraph queryLinkGraph = createQueryLinkGraph_2();
//        PureBigraph queryLinkGraph = createQueryLinkGraph_3();
//        PureBigraph queryLinkGraph = createQueryLinkGraph_4();

        // Debug: Export Query and Data Bigraph
        toPNG(queryLinkGraph, "query", TARGET_DUMP_PATH);
        BigraphFileModelManagement.Store.exportAsInstanceModel(queryLinkGraph, System.out);
        toPNG(dataLinkGraph, "dataLinkGraph", TARGET_DUMP_PATH);
        BigraphFileModelManagement.Store.exportAsInstanceModel(dataLinkGraph, System.out);

        // Perform Hypergraph Matching
        SubHypergraphIsoSearch search = new SubHypergraphIsoSearch(queryLinkGraph, dataLinkGraph);
        search.embeddings();
        System.out.println(search.getCandidates());

        // Render Query and Data Bigraph
        Viewer queryGUI = GUI(queryLinkGraph, true, false, "/graphStreamStyleHighlight.css");
        GraphicGraph queryGraph = queryGUI.getGraphicGraph();
        queryGraph.setAttribute("ui.title", "Query");
        Viewer dataGUI = GUI(dataLinkGraph, true, false, "/graphStreamStyleHighlight.css");
        GraphicGraph dataGraph = dataGUI.getGraphicGraph();
        dataGraph.setAttribute("ui.title", "Data ");

        // Give time to render before highlighting
        Thread.sleep(1000);

        // Highlight Match
        for (SubHypergraphIsoSearch.Embedding next : search.getEmbeddingSet()) {
            System.out.println("Next: " + next);
            for (Map.Entry<BigraphEntity.NodeEntity<?>, BigraphEntity.NodeEntity<?>> each : next.entrySet()) {
                System.out.println("Key: " + each.getValue().getName());
                Node node = dataGraph.getNode(each.getValue().getName());
                if (node != null) {
                    node.setAttribute("ui.class", "highlight");

                }
            }
        }

        // Keep the windows open
        while (true) {
            Thread.sleep(100);
        }
    }

    private PureBigraph createQueryLinkGraph_1() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());

        BigraphEntity.InnerName x1 = builder.createInner("x1");
        BigraphEntity.InnerName x2 = builder.createInner("x2");
        builder.root()
                .child("A").linkInner(x1)
                .child("C").linkInner(x1).linkInner(x2)
                .child("B").linkInner(x1).linkInner(x2)
                .child("C").linkInner(x2)
        ;

        builder.closeInner(x1);
        builder.closeInner(x2);

        PureBigraph bigraph = builder.create();

        assertEquals(2, bigraph.getEdges().size());
        BigraphEntity.Edge e0 = bigraph.getEdges().stream().filter(x -> x.getName().equals("e0")).findFirst().get();
        BigraphEntity.Edge e1 = bigraph.getEdges().stream().filter(x -> x.getName().equals("e1")).findFirst().get();
        assertEquals(3, bigraph.getPointsFromLink(e0).size());
        assertEquals(3, bigraph.getPointsFromLink(e1).size());

        return bigraph;
    }

    private PureBigraph createQueryLinkGraph_2() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());

        builder.root().connectByEdge("C", "C", "B");

        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    private PureBigraph createQueryLinkGraph_3() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());

        builder.root().child("B", "x1")
                .child("C", "x1");

        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    private PureBigraph createQueryLinkGraph_4() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());

        builder.root().child("B", "x1").linkOuter("x2").linkOuter("x3")
                .child("C", "x1");

        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    private PureBigraph createDataLinkGraph() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());
        BigraphEntity.InnerName x1 = builder.createInner("x1");
        BigraphEntity.InnerName x2 = builder.createInner("x2");
        BigraphEntity.InnerName x3 = builder.createInner("x3");
        BigraphEntity.InnerName x4 = builder.createInner("x4");

        builder.root()
                .child("A").linkInner(x1)
                .child("C").linkInner(x1).linkInner(x2)
                .child("B").linkInner(x1).linkInner(x2).linkInner(x3)
                .child("C").linkInner(x2).linkInner(x2)
                .child("A").linkInner(x3)
                .child("C").linkInner(x3).linkInner(x4)
                .child("B").linkInner(x4)
                .child("C").linkInner(x4)
        ;

        builder.closeInner();
        PureBigraph bigraph = builder.create();

        assertEquals(4, bigraph.getEdges().size());
        BigraphEntity.Edge e0 = bigraph.getEdges().stream().filter(x -> x.getName().equals("e0")).findFirst().get();
        BigraphEntity.Edge e1 = bigraph.getEdges().stream().filter(x -> x.getName().equals("e1")).findFirst().get();
        BigraphEntity.Edge e2 = bigraph.getEdges().stream().filter(x -> x.getName().equals("e2")).findFirst().get();
        BigraphEntity.Edge e3 = bigraph.getEdges().stream().filter(x -> x.getName().equals("e3")).findFirst().get();
        assertEquals(3, bigraph.getPointsFromLink(e0).size());
        assertEquals(3, bigraph.getPointsFromLink(e1).size());
        assertEquals(3, bigraph.getPointsFromLink(e1).size());
        assertEquals(3, bigraph.getPointsFromLink(e1).size());
        return bigraph;
    }
}
