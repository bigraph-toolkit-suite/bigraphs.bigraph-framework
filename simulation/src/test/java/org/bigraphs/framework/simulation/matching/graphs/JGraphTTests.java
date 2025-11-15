package org.bigraphs.framework.simulation.matching.graphs;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.graph.SuccessorsFunction;
import com.google.common.graph.Traverser;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.HopcroftKarpMaximumCardinalityBipartiteMatching;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JGraphTTests {

    //TODO: write adapter for guava graph class: internally still jgrapht's core
    public static void main(String[] args) {
        System.out.println("JGraphT");
        //tree is undirected which is a problem with the libraries (to generate an artificial tree)
        //we have to createNodeOfEClass a undirected graph for that and adopt the methods to get the in/out direction
        //i.e. we neede a prnt function. In bigraphs with sharing place graph is defined as DAG however
        //but "the root node of the tree implies the direction for each edge which points away from the root" [MOON JUNG CHUNG]

        //Create example trees from paper
        Graph<String, DefaultEdge> sampleTreeG = createSampleTreeG();
        Graph<String, DefaultEdge> sampleTreeH = createSampleTreeH2(); //both H and H2 generate the same result
        // the algo finds all subtree isos

        //createNodeOfEClass random artificial tree
//        SimpleDirectedGraph<String, DefaultEdge> tree = (SimpleDirectedGraph<String, DefaultEdge>) buildEmptySimpleDirectedGraph();
//        SimpleGraph<String, DefaultEdge> tree = (SimpleGraph<String, DefaultEdge>) buildEmptySimpleDirectedGraph();
//        BarabasiAlbertForestGenerator<String, DefaultEdge> generator = new BarabasiAlbertForestGenerator<>(1, 8);
//        generator.generateGraph(tree);
//        exportGraph(tree, "graph");
//        getOpenNeighborhoodOfVertex(sampleTreeG, "v2");
        Table<String, String, List<String>> S = HashBasedTable.create();
        for (String gVert : sampleTreeG.vertexSet()) {
            for (String hVert : sampleTreeH.vertexSet()) {
                S.put(gVert, hVert, new ArrayList<>());
            }
        }
        Iterable<String> leavesG = getAllLeaves(sampleTreeG);
        Iterable<String> leavesH = getAllLeaves(sampleTreeH);
        for (String gVert : leavesG) {
            for (String hVert : leavesH) {
                S.put(gVert, hVert, getOpenNeighborhoodOfVertex(sampleTreeH, hVert));
            }
        }

        //get all internal vertices in postorder
        List<String> internalVertsG = getAllInternalVerticesPostOrder(sampleTreeG, "v0");
//        List<String> internalVertsH = getAllInternalVerticesPostOrder(sampleTreeH, "u0");

        List<String> internalVertsH0 = new ArrayList<>(sampleTreeH.vertexSet());
        for (String eachV : internalVertsG) {
            List<String> childrenOfV = getChildren(sampleTreeG, eachV);

            List<String> u_vertsOfH = new ArrayList<>(internalVertsH0);
            //d(u) <= t + 1
            int t = childrenOfV.size();
            for (int i = u_vertsOfH.size() - 1; i >= 0; i--) {
                String each = u_vertsOfH.get(i);
                if (sampleTreeH.degreeOf(each) > t + 1) u_vertsOfH.remove(each);
            }
            for (String eachU : u_vertsOfH) {
                List<String> neighborsOfU = getOpenNeighborhoodOfVertex(sampleTreeH, eachU);
                // Construct the bipartite graph
                Graph<String, DefaultEdge> bipartiteGraph = createBipartiteGraph(neighborsOfU, childrenOfV);
//                System.out.println(bipartiteGraph);
                // Connect the edges
                for (int j = 0, vn = childrenOfV.size(); j < vn; j++) {
                    for (int i = 0, un = neighborsOfU.size(); i < un; i++) {
                        if (S.get(childrenOfV.get(j), neighborsOfU.get(i)).contains(eachU)) {
                            bipartiteGraph.addEdge(childrenOfV.get(j), neighborsOfU.get(i));
                        }
                    }
                }
                exportGraph(bipartiteGraph, "bip");
                // createNodeOfEClass partition sets
                List<List<String>> partitionSets = new ArrayList<>();
                partitionSets.add(neighborsOfU);

                for (int i = 1, un = neighborsOfU.size(); i <= un; i++) {
                    List<String> tmp = new ArrayList<>(neighborsOfU);
                    tmp.remove(neighborsOfU.get(i - 1));
                    partitionSets.add(tmp);
                }

                // compute size of maximum matching of bipartite graph for all partitions
                List<Integer> matchings = new ArrayList<>();
                List<String> uSetAfterMatching = new ArrayList<>();
                int ic = 0;
                for (List<String> eachPartitionX : partitionSets) {
                    HopcroftKarpMaximumCardinalityBipartiteMatching<String, DefaultEdge> alg =
                            new HopcroftKarpMaximumCardinalityBipartiteMatching<>(bipartiteGraph,
                                    new HashSet<>(eachPartitionX), new HashSet<>(childrenOfV));
                    try {
                        MatchingAlgorithm.Matching<String, DefaultEdge> matching = alg.getMatching();
                        System.out.println(matching);
                        int m = matching.getEdges().size();
                        if (m == eachPartitionX.size()) {
                            if (ic == 0) {
                                uSetAfterMatching.add(eachU);
                            } else {
                                uSetAfterMatching.add(neighborsOfU.get(ic - 1));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ic++;
                }
                // update map S
                S.put(eachV, eachU, uSetAfterMatching);
                if (S.get(eachV, eachU).contains(eachU)) {
                    System.out.println("FOUND A MATCHING: " + eachV + " and " + eachU);
//                    return;
                }
            }
        }


    }

    private static Graph<String, DefaultEdge> createBipartiteGraph(List<String> x, List<String> y) {
        SimpleGraph<String, DefaultEdge> bg = (SimpleGraph<String, DefaultEdge>) buildEmptySimpleDirectedGraph();
        for (String eachX : x) {
            bg.addVertex(eachX);
        }
        for (String eachY : y) {
            bg.addVertex(eachY);
        }
        return bg;
    }

    //OK
    private static Iterable<String> getAllLeaves(Graph<String, DefaultEdge> g) {
        Set<String> strings = g.vertexSet();
        List<String> leaves = new ArrayList<>();
        for (String each : strings) {
            if (g.degreeOf(each) <= 1) {
                leaves.add(each);
            }
        }
        return leaves;
    }

    private static List<String> getChildren(Graph<String, DefaultEdge> g, String node) {
        Set<DefaultEdge> defaultEdges = g.incomingEdgesOf(node);
        List<String> children = new ArrayList<>();
        for (DefaultEdge each : defaultEdges) {
            if (g.getEdgeTarget(each).equals(node)) {
                children.add(g.getEdgeSource(each));
            }
        }
        return children;
    }

    private static List<String> getOpenNeighborhoodOfVertex(Graph<String, DefaultEdge> g, String vertex) {
        List<String> strings1 = Graphs.successorListOf(g, vertex);
        //should contain the same information
//        List<String> strings = Graphs.predecessorListOf(g, vertex);
        return strings1;
    }

    private static List<String> getAllInternalVerticesPostOrder(Graph<String, DefaultEdge> g, String rootNode) {
        Iterable<String> allVerticesPostOrder = getAllVerticesPostOrder(g, rootNode);
        List<String> collect = StreamSupport.stream(allVerticesPostOrder.spliterator(), false).filter(x -> getChildren(g, x).size() > 0).collect(Collectors.toList());
        return collect;
    }

    private static Iterable<String> getAllVerticesPostOrder(Graph<String, DefaultEdge> g, String rootNode) {
        Traverser<String> stringTraverser = Traverser.forTree(new SuccessorsFunction<String>() {
            @Override
            public Iterable<String> successors(String node) {
                return getChildren(g, node);
            }
        });
        Iterable<String> v0 = stringTraverser.depthFirstPostOrder(rootNode);
        for (String each : v0) {
            System.out.print(each + ", ");
        }
        System.out.println();
        return v0;
    }

    private static Graph<String, DefaultEdge> createSampleTreeG() {
        SimpleGraph<String, DefaultEdge> g = (SimpleGraph<String, DefaultEdge>) buildEmptySimpleDirectedGraph();
        g.addVertex("v0");
        g.addVertex("v1");
        g.addVertex("v2");
        g.addVertex("v3");
        g.addVertex("v4");
        g.addVertex("v5");
        g.addVertex("v6");
        g.addVertex("v7");

        g.addEdge("v1", "v0");
        g.addEdge("v2", "v0");
        g.addEdge("v3", "v0");

        g.addEdge("v4", "v2");
        g.addEdge("v5", "v2");
        g.addEdge("v6", "v3");
        g.addEdge("v7", "v3");
        exportGraph(g, "g");
        return g;
    }

    private static Graph<String, DefaultEdge> createSampleTreeH() {
        SimpleGraph<String, DefaultEdge> h = (SimpleGraph<String, DefaultEdge>) buildEmptySimpleDirectedGraph();
        h.addVertex("u0");
        h.addVertex("u1");
        h.addVertex("u2");
        h.addVertex("u3");
        h.addVertex("u4");

        h.addEdge("u1", "u0");
        h.addEdge("u2", "u0");
        h.addEdge("u3", "u0");

        h.addEdge("u4", "u2");
        exportGraph(h, "h");
        return h;
    }

    // same as H but other slightly other layout (different root but structure is preserved)
    private static Graph<String, DefaultEdge> createSampleTreeH2() {
        SimpleGraph<String, DefaultEdge> h = (SimpleGraph<String, DefaultEdge>) buildEmptySimpleDirectedGraph();
        h.addVertex("u0");
        h.addVertex("u1");
        h.addVertex("u2");
        h.addVertex("u3");
        h.addVertex("u4");

        h.addEdge("u1", "u0");
        h.addEdge("u2", "u0");


        h.addEdge("u3", "u2");
        h.addEdge("u4", "u2");

        exportGraph(h, "h");
        return h;
    }

    //TODO: pay attention to equals/hashCode, important so that we can add "identical" nodes (i.e., edges)
    //TODO: but internally the names are really identical -> this aspect is used later in the algo
    static // Create the VertexFactory so the generator can createNodeOfEClass vertices
            Supplier<String> vSupplier = new Supplier<String>() {
        private int id = 0;

        @Override
        public String get() {
            return "v" + id++;
        }
    };


    private static Graph<String, DefaultEdge> buildEmptySimpleDirectedGraph() {
        vSupplier.get();
//        return GraphTypeBuilder.<String, DefaultEdge>directed()
        return GraphTypeBuilder.<String, DefaultEdge>undirected()
//                .vertexClass()
                .vertexSupplier(vSupplier)
                .allowingMultipleEdges(false)
                .allowingSelfLoops(false)
                .edgeClass(DefaultEdge.class)
                .weighted(false).buildGraph();
    }

    private static void exportGraph(Graph g, String filename) {
        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<String, DefaultEdge>(g);
//        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
//        mxIGraphLayout layout = new mxCompactTreeLayout(graphAdapter);
//        mxIGraphLayout layout = new mxOrthogonalLayout(graphAdapter);
        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter, SwingConstants.SOUTH);
        layout.execute(graphAdapter.getDefaultParent());

        BufferedImage image =
                mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        try {
            Path currentRelativePath = Paths.get("");
            Path completePath = Paths.get(currentRelativePath.toAbsolutePath().toString(), "rewriting", "src", "test", "resources", filename + ".png");
//            String s = currentRelativePath.toAbsolutePath().toString();
            File imgFile = new File(completePath.toUri());
            if (!imgFile.exists()) {
                imgFile.createNewFile();
            }
//            URL location = JGraphTTests.class.getProtectionDomain().getCodeSource().getLocation();
//            File imgFile = new File(JGraphTTests.class.getClassLoader().getResource("somefile").toURI());
//            imgFile.createNewFile();
            ImageIO.write(image, "PNG", imgFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        assertTrue(imgFile.exists());
    }

}
