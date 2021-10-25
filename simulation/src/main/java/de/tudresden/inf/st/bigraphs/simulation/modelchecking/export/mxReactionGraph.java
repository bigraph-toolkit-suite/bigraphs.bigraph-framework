package de.tudresden.inf.st.bigraphs.simulation.modelchecking.export;

import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactiveSystem;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionGraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactiveSystemPredicates;
import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom implementation of {@link mxGraph} to adjust the node and edge styling.
 * Its source code is fully based on {@link org.jgrapht.ext.JGraphXAdapter}.
 * Only small adjustments are made.
 *
 * @author Dominik Grzelak
 * @see org.jgrapht.ext.JGraphXAdapter
 */
public class mxReactionGraph extends mxGraph implements GraphListener<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> {

    private final ReactionGraph reactionGraph;
    private final ReactiveSystem reactiveSystem;

    /**
     * The graph to be drawn. Has vertices "V" and edges "E".
     */
    private Graph<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> graphT;

    /**
     * Maps the JGraphT-Vertices onto JGraphX-mxICells. {@link #cellToVertexMap} is for the opposite
     * direction.
     */
    private HashMap<ReactionGraph.LabeledNode, mxICell> vertexToCellMap = new HashMap<>();

    /**
     * Maps the JGraphT-Edges onto JGraphX-mxICells. {@link #cellToEdgeMap} is for the opposite
     * direction.
     */
    private HashMap<ReactionGraph.LabeledEdge, mxICell> edgeToCellMap = new HashMap<>();

    /**
     * Maps the JGraphX-mxICells onto JGraphT-Edges. {@link #edgeToCellMap} is for the opposite
     * direction.
     */
    private HashMap<mxICell, ReactionGraph.LabeledNode> cellToVertexMap = new HashMap<>();

    /**
     * Maps the JGraphX-mxICells onto JGraphT-Vertices. {@link #vertexToCellMap} is for the opposite
     * direction.
     */
    private HashMap<mxICell, ReactionGraph.LabeledEdge> cellToEdgeMap = new HashMap<>();


//    /**
//     * Constructs and draws a new ListenableGraph. If the graph changes through
//     * as ListenableGraph, the JGraphXAdapter will automatically add/remove the
//     * new edge/vertex as it implements the GraphListener interface. Throws a
//     * IllegalArgumentException if the graph is null.
//     *
//     * @param graph casted to graph
//     */
//    public BigLayouter(ListenableGraph<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> graph) {
//        // call normal constructor with graph class
//        this((Graph<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge>) graph);
//
//        graph.addGraphListener(this);
//    }

    /**
     * Constructs and draws a new mxGraph from a jGraphT graph. Changes on the
     * jgraphT graph will not edit this mxGraph any further; use the constructor
     * with the ListenableGraph parameter instead or use this graph as a normal
     * mxGraph. Throws an IllegalArgumentException if the parameter is null.
     */
    public mxReactionGraph(ReactionGraph reactionGraph, ReactiveSystem reactiveSystem) {
        super();
        this.reactionGraph = reactionGraph;
        this.reactiveSystem = reactiveSystem;
        Graph<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> graph = reactionGraph.getGraph();
        if (graph instanceof ListenableGraph) {
            ((ListenableGraph<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge>) graph).addGraphListener(this);
        }
        // Don't accept null as jgrapht graph
        if (graph == null) {
            throw new IllegalArgumentException();
        } else {
            this.graphT = graph;
        }

        // generate the drawing
        insertJGraphT(graph);

        setAutoSizeCells(true);
    }


    /**
     * Returns Hashmap which maps the vertices onto their visualization mxICells.
     *
     * @return {@link #vertexToCellMap}
     */
    public HashMap<ReactionGraph.LabeledNode, mxICell> getVertexToCellMap() {
        return vertexToCellMap;
    }

    /**
     * Returns Hashmap which maps the edges onto their visualization mxICells.
     *
     * @return {@link #edgeToCellMap}
     */
    public HashMap<ReactionGraph.LabeledEdge, mxICell> getEdgeToCellMap() {
        return edgeToCellMap;
    }

    /**
     * Returns Hashmap which maps the visualization mxICells onto their edges.
     *
     * @return {@link #cellToEdgeMap}
     */
    public HashMap<mxICell, ReactionGraph.LabeledEdge> getCellToEdgeMap() {
        return cellToEdgeMap;
    }

    /**
     * Returns Hashmap which maps the visualization mxICells onto their vertices.
     *
     * @return {@link #cellToVertexMap}
     */
    public HashMap<mxICell, ReactionGraph.LabeledNode> getCellToVertexMap() {
        return cellToVertexMap;
    }

    @Override
    public void vertexAdded(GraphVertexChangeEvent<ReactionGraph.LabeledNode> e) {
        addJGraphTVertex(e.getVertex());
    }

    @Override
    public void vertexRemoved(GraphVertexChangeEvent<ReactionGraph.LabeledNode> e) {
        mxICell cell = vertexToCellMap.remove(e.getVertex());
        removeCells(new Object[]{cell});

        // remove vertex from hashmaps
        cellToVertexMap.remove(cell);
        vertexToCellMap.remove(e.getVertex());

        // remove all edges that connected to the vertex
        ArrayList<ReactionGraph.LabeledEdge> removedEdges = new ArrayList<>();

        // first, generate a list of all edges that have to be deleted
        // so we don't change the cellToEdgeMap.values by deleting while
        // iterating
        // we have to iterate over this because the graphT has already
        // deleted the vertex and edges so we can't query what the edges were
        for (ReactionGraph.LabeledEdge edge : cellToEdgeMap.values()) {
            if (!graphT.containsEdge(edge)) {
                removedEdges.add(edge);
            }
        }

        // then delete all entries of the previously generated list
        for (ReactionGraph.LabeledEdge edge : removedEdges) {
            removeEdge(edge);
        }
    }

    @Override
    public void edgeAdded(GraphEdgeChangeEvent<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> e) {
        addJGraphTEdge(e.getEdge());
    }

    @Override
    public void edgeRemoved(GraphEdgeChangeEvent<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> e) {
        removeEdge(e.getEdge());
    }

    /**
     * Removes a jgrapht edge and its visual representation from this graph completely.
     *
     * @param edge The edge that will be removed
     */
    private void removeEdge(ReactionGraph.LabeledEdge edge) {
        mxICell cell = edgeToCellMap.remove(edge);
        removeCells(new Object[]{cell});

        // remove edge from hashmaps
        cellToEdgeMap.remove(cell);
        edgeToCellMap.remove(edge);
    }

    /**
     * Draws a new vertex into the graph.
     *
     * @param vertex vertex to be added to the graph
     */
    private void addJGraphTVertex(ReactionGraph.LabeledNode vertex) {
        getModel().beginUpdate();

        try {
            Optional<ReactionGraph.LabeledNode> labeledNodeByCanonicalForm = reactionGraph.getLabeledNodeByCanonicalForm(vertex.getCanonicalForm());
            String style = "DEFAULT";
            String oldLabel = vertex.getLabel();
            // create a new JGraphX vertex at position 0
            if (labeledNodeByCanonicalForm.isPresent()) {
                Set<ReactiveSystemPredicates> o = (Set<ReactiveSystemPredicates>) reactionGraph.getPredicateMatches().get(labeledNodeByCanonicalForm.get());
                if (Objects.nonNull(o) && o.size() > 0) {
                    style = "MATCHED";
                    String predList = o.stream().map(x -> (String) reactiveSystem.getPredicateMap().inverse().get(x))
                            .collect(Collectors.joining(","));
                    oldLabel = String.format("%s\n(%s)", oldLabel, predList);
                }
            }
            vertex.changeLabel(oldLabel);
            mxICell cell = (mxICell) insertVertex(defaultParent, null, vertex, 0, 0, 0, 0, style);

            // update cell size so cell isn't "above" graph
            updateCellSize(cell);

            // Save reference between vertex and cell
            vertexToCellMap.put(vertex, cell);
            cellToVertexMap.put(cell, vertex);
        } finally {
            getModel().endUpdate();
        }
    }

    /**
     * Draws a new egde into the graph.
     *
     * @param edge edge to be added to the graph. Source and target vertices are needed.
     */
    private void addJGraphTEdge(ReactionGraph.LabeledEdge edge) {
        getModel().beginUpdate();

        try {
            // find vertices of edge
            ReactionGraph.LabeledNode sourceVertex = graphT.getEdgeSource(edge);
            ReactionGraph.LabeledNode targetVertex = graphT.getEdgeTarget(edge);

            // if the one of the vertices is not drawn, don't draw the edge
            if (!(vertexToCellMap.containsKey(sourceVertex)
                    && vertexToCellMap.containsKey(targetVertex))) {
                return;
            }

            // get mxICells
            Object sourceCell = vertexToCellMap.get(sourceVertex);
            Object targetCell = vertexToCellMap.get(targetVertex);

            // add edge between mxICells
            mxICell cell = (mxICell) insertEdge(defaultParent, null, edge, sourceCell, targetCell, "DEFAULT_EDGE");

            // update cell size so cell isn't "above" graph
            updateCellSize(cell);

            // Save reference between vertex and cell
            edgeToCellMap.put(edge, cell);
            cellToEdgeMap.put(cell, edge);
        } finally {
            getModel().endUpdate();
        }
    }

    /**
     * Draws a given graph with all its vertices and edges.
     *
     * @param graph the graph to be added to the existing graph.
     */
    private void insertJGraphT(Graph<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> graph) {
        for (ReactionGraph.LabeledNode vertex : graph.vertexSet()) {
            addJGraphTVertex(vertex);
        }

        for (ReactionGraph.LabeledEdge edge : graph.edgeSet()) {
            addJGraphTEdge(edge);
        }
    }
}
