package de.tudresden.inf.st.bigraphs.simulation.modelchecking;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.predicates.ReactiveSystemPredicates;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A data structure to build up a reaction graph, similar to a labeled transition system.
 * <p>
 * This is not to be compared with bigraphical LTS - the reaction graph is not minimal according to the context of a transition.
 * Here the arrows are reaction rules, and the nodes are states (i.e., bigraphs, represented by their unique label).
 *
 * @author Dominik Grzelak
 */
public class ReactionGraph<B extends Bigraph<? extends Signature<?>>> {

    private final Map<String, B> stateMap = new ConcurrentHashMap<>();
    private final Map<String, B> transitionMap = new ConcurrentHashMap<>();
    private Graph<LabeledNode, LabeledEdge> graph;
    private Map<LabeledNode, Set<ReactiveSystemPredicates<B>>> predicateMatches;
    private ReactionGraphStats<B> graphStats;
    private boolean canonicalNodeLabel = false;

    public ReactionGraph() {
        reset();
    }

    private Supplier<String> aSup = null;

    private LabeledNode createNode(String label) {
        if (canonicalNodeLabel) {
            return new CanonicalLabeledNode(label, label);
        } else {
            return new DefaultLabeledNode(aSup.get(), label);
        }
    }

    public void addEdge(B source, String sourceLbl, B target, String targetLbl, B reaction, String reactionLbl) {
        LabeledNode sourceNode = createNode(sourceLbl);
        LabeledNode targetNode = createNode(targetLbl);
        graph.addVertex(sourceNode);
        graph.addVertex(targetNode);
        boolean b = graph.addEdge(sourceNode, targetNode, new LabeledEdge(reactionLbl));

        stateMap.putIfAbsent(sourceLbl, source);
        stateMap.putIfAbsent(targetLbl, target);
        transitionMap.putIfAbsent(reactionLbl, reaction);
    }

    public Optional<LabeledNode> getLabeledNodeByCanonicalForm(String canonicalForm) {
        return graph.vertexSet().stream().filter(x -> x.getCanonicalForm().equals(canonicalForm)).findFirst();
    }

    @SuppressWarnings("UnusedReturnValue")
    public ReactionGraph<B> setCanonicalNodeLabel(boolean canonicalNodeLabel) {
        this.canonicalNodeLabel = canonicalNodeLabel;
        return this;
    }

    public Map<LabeledNode, Set<ReactiveSystemPredicates<B>>> getPredicateMatches() {
        return predicateMatches;
    }

    public void reset() {
        aSup = new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return "a:" + id++;
            }
        };
        graph = buildEmptySimpleDirectedGraph();
        graphStats = new ReactionGraphStats<>(this);
        predicateMatches = Maps.mutable.empty();
    }

    @SuppressWarnings("UnusedReturnValue")
    public ReactionGraph<B> addPredicateMatchToNode(LabeledNode node, ReactiveSystemPredicates<B> predicates) {
        predicateMatches.putIfAbsent(node, Sets.mutable.empty());
        predicateMatches.get(node).add(predicates);
        return this;
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
        return stateMap.containsKey(label);
    }

    /**
     * Get the data structure of the reaction graph
     *
     * @return the reaction graph
     */
    public Graph<LabeledNode, LabeledEdge> getGraph() {
        return graph;
    }

    public ReactionGraphStats<B> getGraphStats() {
        graphStats.computeStatistics();
        return graphStats;
    }

    public boolean isEmpty() {
        return !(Objects.nonNull(graph) && graph.vertexSet().size() != 0);
    }

    private Graph<LabeledNode, LabeledEdge> buildEmptySimpleDirectedGraph() {
        return GraphTypeBuilder.<LabeledNode, LabeledEdge>undirected()
                .allowingMultipleEdges(true)
                .allowingSelfLoops(true)
                .weighted(false)
                .buildGraph();
    }

    public static abstract class LabeledNode {
        private String label;
        private String canonicalForm;

        public LabeledNode(String label, String canonicalForm) {
            this.label = label;
            this.canonicalForm = canonicalForm;
        }

        public String getLabel() {
            return label;
        }

        public String getCanonicalForm() {
            return canonicalForm;
        }

        public void changeLabel(String newLabel) {
            this.label = newLabel;
        }
    }

    public static class DefaultLabeledNode extends LabeledNode {

        public DefaultLabeledNode(String label, String canonicalForm) {
            super(label, canonicalForm);
        }

        @Override
        public String toString() {
            return getLabel();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LabeledNode)) return false;
            LabeledNode that = (LabeledNode) o;
            return Objects.equals(getCanonicalForm(), that.canonicalForm);
        }

        @Override
        public int hashCode() {
            return Objects.hash(getCanonicalForm());
        }
    }

    public static class CanonicalLabeledNode extends LabeledNode {

        public CanonicalLabeledNode(String label, String canonicalForm) {
            super(label, canonicalForm);
        }

        @Override
        public String toString() {
            return getCanonicalForm();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LabeledNode)) return false;
            LabeledNode that = (LabeledNode) o;
            return Objects.equals(getCanonicalForm(), that.canonicalForm);
        }

        @Override
        public int hashCode() {
            return Objects.hash(getCanonicalForm());
        }
    }

    public static class LabeledEdge extends DefaultEdge {
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
