package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A data structure to build up a reaction graph, similar to a labelled transition system.
 * <p>
 * This is not to be compared with bigraphical LTS - the reaction graph is not minimal according to the context of a transition.
 * Here the arrows are reaction rules, and the nodes are states (i.e., bigraphs, represented by their unique label).
 *
 * @author Dominik Grzelak
 */
public class ReactionGraph<B extends Bigraph<?>> {

    private Map<String, B> bigraphMap = new ConcurrentHashMap<>();
    private Map<String, B> labelMap = new ConcurrentHashMap<>();
    private Graph<String, LabeledEdge> graph;
    private BiMap<String, String> agentMap = HashBiMap.create();
    private ReactionGraphStats<B> graphStats;

    public ReactionGraph() {
        reset();
    }

    private Supplier<String> aSup = null;

    public void addEdge(B source, String sourceLbl, B target, String targetLbl, B reaction, String reactionLbl) {

        graph.addVertex(sourceLbl);
        graph.addVertex(targetLbl);
        boolean b = graph.addEdge(sourceLbl, targetLbl, new LabeledEdge(reactionLbl));

        // with a_i symbols in states ...
//        String s1 = agentMap.computeIfAbsent(sourceLbl, s -> aSup.get());
//        String s2 = agentMap.computeIfAbsent(targetLbl, s -> aSup.get());
        // graph.addVertex(s1);
//        graph.addVertex(s2);
//        boolean b = graph.addEdge(s1, s2, new LabeledEdge(reactionLbl));

        bigraphMap.putIfAbsent(sourceLbl, source);
        bigraphMap.putIfAbsent(targetLbl, target);
        labelMap.putIfAbsent(reactionLbl, reaction);
    }

    public void reset() {
        aSup = new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return "a_" + id++;
            }
        };
        graph = buildEmptySimpleDirectedGraph();
        graphStats = new ReactionGraphStats<>(this);
    }

    /**
     * Check if a bigraph is present in the graph as a state. The bigraph is identified by its label.
     * <p>
     * The label of a bigraph must be unique.
     *
     * @param label The unique label of a bigraph to find in the reaction graph.
     * @return {@code true}, if the bigraph is contained in the graph, otherwise {@code false}
     */
    public boolean containsBigraph(String label) {
        return bigraphMap.containsKey(label);
    }

    /**
     * Get the data structure of the reaction graph
     *
     * @return the reaction graph
     */
    public Graph<String, LabeledEdge> getGraph() {
        return graph;
    }

    public ReactionGraphStats<B> getGraphStats() {
        graphStats.computeStatistics();
        return graphStats;
    }

    public boolean isEmpty() {
        return !(Objects.nonNull(graph) && graph.vertexSet().size() != 0);
    }

    private Graph<String, LabeledEdge> buildEmptySimpleDirectedGraph() {
        return GraphTypeBuilder.<String, LabeledEdge>undirected()
                .allowingMultipleEdges(true)
                .allowingSelfLoops(true)
                .weighted(false)
                .buildGraph();
    }


    public static class LabeledEdge
            extends
            DefaultEdge {
        private String label;

        /**
         * Constructs a labeled edge
         *
         * @param label the label of the new edge.
         */
        public LabeledEdge(String label) {
            this.label = label;
        }

        /**
         * Gets the label associated with this edge.
         *
         * @return edge label
         */
        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
//            return "(" + getSource() + " : " + getTarget() + " : " + label + ")";
            return "(" + label + ")";
        }
    }
}
