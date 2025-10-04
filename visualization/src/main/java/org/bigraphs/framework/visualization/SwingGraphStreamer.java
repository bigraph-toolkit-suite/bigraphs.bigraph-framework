package org.bigraphs.framework.visualization;

import com.google.common.graph.Traverser;
import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.bigraphs.framework.core.utils.BigraphUtil.getUniqueIdOfBigraphEntity;

/**
 * Swing-based GraphStream-based renderer for {@link PureBigraph}s.
 * <p>
 * Builds a {@link Graph} and displays it via a GraphStream {@link Viewer} (Swing UI).
 * Supports optional rendering of roots/sites, dark mode styling.
 *
 * <h3>Typical usage</h3>
 * <ol>
 *   <li>{@link #prepareSystemEnvironment()} (sets UI system properties for Swing)</li>
 *   <li>{@link #getGraphViewer()} or {@link #getGraphViewer(String)} to build+display</li>
 * </ol>
 *
 * @author Dominik Grzelak
 * @see <a href="https://bigraphs.org/software/bigraph-framework/docs/visualization/visualization-interactive">
 * Visualization (Interactive) - Bigraph Framework Docs</a>
 */
public class SwingGraphStreamer {

    PureBigraph bigraph;
    boolean withSites = false;
    boolean withRoots = false;
    Graph graph;
    Viewer viewer;
    boolean darkMode = false;

    int delayForElements = -1;

    ExecutorService executorService = Executors.newFixedThreadPool(2);

    /**
     * @param bigraph the bigraph to be rendered
     */
    public SwingGraphStreamer(PureBigraph bigraph) {
        this(bigraph, false, false);
    }

    /**
     * @param bigraph   the bigraph to be rendered
     * @param withSites draw sites?
     * @param withRoots draw roots?
     */
    public SwingGraphStreamer(PureBigraph bigraph, boolean withSites, boolean withRoots) {
        this.bigraph = bigraph;
        this.withSites = withSites;
        this.withRoots = withRoots;
    }

    public SwingGraphStreamer(PureBigraph bigraph, boolean withSites, boolean withRoots, boolean darkMode) {
        this(bigraph, withSites, withRoots);
        this.darkMode = darkMode;
    }

    /**
     * Sets some system properties so that the UI can be shown.
     * Otherwise, an exception might be thrown: "No UI package detected! Please use System.setProperty("org.graphstream.ui") for the selected package."
     */
    public void prepareSystemEnvironment() {
        System.setProperty("java.awt.headless", "false");
        System.setProperty("org.graphstream.ui", "swing");
    }

    public void initGraph(String graphId) {
        if (graph == null) {
            graph = new SingleGraph(graphId);

            if (darkMode) {
                InputStream styleStream = SwingGraphStreamer.class.getResourceAsStream("/graphStreamStyleDark.css");
                if (styleStream != null) {
                    String style = new BufferedReader(new InputStreamReader(styleStream))
                            .lines()
                            .collect(Collectors.joining("\n"));
                    graph.setAttribute("ui.stylesheet", style);
                }
            }

            if (!darkMode || graph.getAttribute("ui.stylesheet") == null) {
                graph.setAttribute("ui.stylesheet", "node.link { text-size: 12px; fill-color: green; } node.innername { text-size: 12px; fill-color: green; } node.control { text-size: 12px; fill-color: red; } node.root {fill-color: blue; text-size: 12px;} node.site {fill-color: gray; text-size: 12px;} edge.hyperedge { fill-color: green; stroke-width: 3px; }");
            }
        }
    }

    public void initViewer() {
        if (viewer == null && graph != null) {
            viewer = graph.display();
            viewer.getDefaultView().getCamera().setAutoFitView(true);
            viewer.getDefaultView().getCamera().setViewPercent(1.2);
        }
    }

    /**
     * Get the underlying graph object for the visualization with GraphStream
     *
     * @return
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * GraphStream Visualization.
     * Returns the Viewer, which is responsible for the actual display of the bigraph.
     *
     * @return
     */
    public Viewer getGraphViewer() {
        return getGraphViewer("default");
    }

    /**
     * GraphStream Visualization
     *
     * @param graphId graphId of the window
     * @return
     */
    public Viewer getGraphViewer(String graphId) {
        if (viewer != null) {
            return viewer;
        }
        this.delayForElements = -1;
        initGraph(graphId);
        initViewer();
        renderBigraphAction.run();
        return viewer;
    }

    public CompletableFuture<Boolean> renderAsync(int delayPerElement) {
        return renderAsync("default", delayPerElement);
    }

    public CompletableFuture<Boolean> renderAsync(String graphId, int delayPerElement) {
        this.delayForElements = delayPerElement;
        initGraph(graphId);
        initViewer();

        // Create a CompletableFuture
        CompletableFuture<Boolean> futureResult = CompletableFuture.supplyAsync(() -> {
            renderBigraphAction.run();
            return true;
        }, executorService);

        // Execute the actual rendering by placing elements on the graph
        // Attach a callback to be executed when the CompletableFuture completes
        futureResult.thenAccept(result -> {
            try {
                executorService.shutdown();
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        return futureResult;
    }

    Runnable renderBigraphAction = new Runnable() {

        @Override
        public void run() {
            Supplier<String> edgeNameSupplier = createNameSupplier("e");
            // Draw the place graph
            Traverser<BigraphEntity> traverser = Traverser.forTree(x -> {
                List<BigraphEntity<?>> children = bigraph.getChildrenOf(x);
                return children;
            });
            Iterable<BigraphEntity> bigraphEntities = traverser.breadthFirst(bigraph.getRoots());
            bigraphEntities.forEach(x -> {
                if (BigraphEntityType.isRoot(x) && !withRoots) {
                    return;
                }
                if (BigraphEntityType.isSite(x) && !withSites) {
                    return;
                }
                String id = getUniqueIdOfBigraphEntity(x);
                assert !Objects.equals(id, "");
                Node gsNode = graph.addNode(id);
                switch (x.getType()) {
                    case ROOT -> {
                        gsNode.setAttribute("ui.class", "root");
                        gsNode.setAttribute("ui.label", id);
                    }
                    case NODE -> {
                        gsNode.setAttribute("ui.class", "control");
                        gsNode.setAttribute("ui.label", id + ":" + x.getControl().getNamedType().stringValue());
                    }
                    case SITE -> {
                        gsNode.setAttribute("ui.class", "site");
                        gsNode.setAttribute("ui.label", id);
                    }
                }

                BigraphEntity<?> prntNode = bigraph.getParent(x);
                if (!(BigraphEntityType.isRoot(prntNode) && !withRoots)) {
                    if (!BigraphEntityType.isRoot(x) && prntNode != null) {
                        String idPrnt = getUniqueIdOfBigraphEntity(prntNode);
                        assert !Objects.equals(idPrnt, "");
                        graph.addEdge(edgeNameSupplier.get(), id, idPrnt);
                    }
                }

                sleep(delayForElements);
            });
            // Draw the link graph
            bigraph.getAllLinks().forEach(l -> {
                List<BigraphEntity<?>> pointsFromLink = bigraph.getPointsFromLink(l);
                Node gsNode = graph.addNode(l.getName());
                gsNode.setAttribute("ui.class", "link"); // edges and outernames
                gsNode.setAttribute("ui.label", l.getName());
                pointsFromLink.forEach(p -> {
                    if (BigraphEntityType.isPort(p)) {
                        BigraphEntity.NodeEntity<DynamicControl> nodeOfPort = bigraph.getNodeOfPort((BigraphEntity.Port) p);
                        Edge hyperEdge = graph.addEdge(edgeNameSupplier.get(), getUniqueIdOfBigraphEntity(nodeOfPort), l.getName());
                        hyperEdge.setAttribute("ui.class", "hyperedge");

                    } else if (BigraphEntityType.isInnerName(p)) {
                        String innerNameID = getUniqueIdOfBigraphEntity(p);
                        if (graph.nodes().noneMatch(n -> n.getId().equals(innerNameID))) {
                            Node innerNameNode = graph.addNode(innerNameID);
                            innerNameNode.setAttribute("ui.class", "innername");
                            innerNameNode.setAttribute("ui.label", innerNameID);
                        }
                        Edge hyperEdge = graph.addEdge(edgeNameSupplier.get(), innerNameID, l.getName());
                        hyperEdge.setAttribute("ui.class", "hyperedge");
                    }
                    sleep(delayForElements);
                });
                sleep(delayForElements);
            });
        }
    };

    public Viewer getViewer() {
        return viewer;
    }

    private void sleep(int delayPerElement) {
        if (delayPerElement <= 0) return;
        try {
            Thread.sleep(delayPerElement);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public SwingGraphStreamer renderSites(boolean withSites) {
        this.withSites = withSites;
        return this;
    }

    public SwingGraphStreamer renderRoots(boolean withRoots) {
        this.withRoots = withRoots;
        return this;
    }

    private Supplier<String> createNameSupplier(final String prefix) {
        return new Supplier<>() {
            private int id = 0;

            @Override
            public String get() {
                return prefix + id++;
            }
        };
    }
}
