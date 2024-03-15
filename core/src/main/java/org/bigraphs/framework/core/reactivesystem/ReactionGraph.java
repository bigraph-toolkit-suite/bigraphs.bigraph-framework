package org.bigraphs.framework.core.reactivesystem;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * An extended data structure to build up a reaction graph, analogous to a labeled transition system.
 * It uses the abstract base class {@link AbstractTransitionSystem} as support.
 * <p>
 * A reaction graph is not to be confused with the notion of minimal LTSs in bigraphs.
 * This reaction graph has no minimal context labels as transitions; it has reactions as labels.
 * <p>
 * Here the transition relations are reaction rules, and the nodes are states (i.e., bigraphs, represented by their unique string encoding).
 *
 * @author Dominik Grzelak
 */
public class ReactionGraph<B extends Bigraph<? extends Signature<?>>> extends AbstractTransitionSystem<B> {

    private Graph<LabeledNode, LabeledEdge> graph;
    private Map<LabeledNode, Set<ReactiveSystemPredicate<B>>> predicateMatches;
    private ReactionGraphStats<B> graphStats;

    public ReactionGraph() {
        reset();
    }

    protected LabeledNode createNode(String label) {
        if (canonicalNodeLabel) {
            return new CanonicalLabeledNode(label, label);
        } else {
            return new DefaultLabeledNode(aSup.get(), label);
        }
    }

    public void addEdge(B source, String sourceLbl, B target, String targetLbl, B reaction, String reactionLbl) {
        LabeledNode sourceNode;
        if (stateMap.get(sourceLbl) != null) {
            sourceNode = graph.vertexSet().stream().filter(x -> x.canonicalForm.equals(sourceLbl)).findFirst().get();
        } else {
            sourceNode = createNode(sourceLbl);
            addState(sourceLbl, source);
            graph.addVertex(sourceNode);
        }
        LabeledNode targetNode;
        if (stateMap.get(targetLbl) != null) {
            targetNode = graph.vertexSet().stream().filter(x -> x.canonicalForm.equals(targetLbl)).findFirst().get();
        } else {
            targetNode = createNode(targetLbl);
            addState(targetLbl, target);
            graph.addVertex(targetNode);
        }
        Set<LabeledEdge> allEdges = graph.getAllEdges(sourceNode, targetNode);
        Optional<LabeledEdge> first = allEdges.stream().filter(x -> x.label.equals(reactionLbl)).findFirst();
        if (first.isEmpty()) {
            final LabeledEdge edge = new LabeledEdge(reactionLbl);
            boolean b = graph.addEdge(sourceNode, targetNode, edge);
            if (b) {
                addTransition(reactionLbl, reaction);
            }
        }
    }

    public Optional<LabeledNode> getLabeledNodeByCanonicalForm(String canonicalForm) {
        return graph.vertexSet().stream().filter(x -> x.getCanonicalForm().equals(canonicalForm)).findFirst();
    }

    @SuppressWarnings("UnusedReturnValue")
    public ReactionGraph<B> setCanonicalNodeLabel(boolean canonicalNodeLabel) {
        this.canonicalNodeLabel = canonicalNodeLabel;
        return this;
    }

    public Map<LabeledNode, Set<ReactiveSystemPredicate<B>>> getPredicateMatches() {
        return predicateMatches;
    }

    public void reset() {
        aSup = createSupplier();
        graph = buildEmptySimpleDirectedGraph();
        graphStats = new ReactionGraphStats<>(this);
        predicateMatches = Maps.mutable.empty();
    }

    @SuppressWarnings("UnusedReturnValue")
    public ReactionGraph<B> addPredicateMatchToNode(LabeledNode node, ReactiveSystemPredicate<B> predicates) {
        predicateMatches.putIfAbsent(node, Sets.mutable.empty());
        predicateMatches.get(node).add(predicates);
        return this;
    }

    /**
     * Get the data structure of the reaction graph
     *
     * @return the reaction graph
     */
    public Graph<LabeledNode, LabeledEdge> getGraph() {
        return graph;
    }


    /**
     * Get an object containing some informative statistics of the reaction graph
     *
     * @return some statistics about the reaction graph object
     */
    public ReactionGraphStats<B> getGraphStats() {
        graphStats.computeStatistics();
        return graphStats;
    }

    public boolean isEmpty() {
        return !(Objects.nonNull(graph) && graph.vertexSet().size() != 0);
    }

    private Graph<LabeledNode, LabeledEdge> buildEmptySimpleDirectedGraph() {
        return GraphTypeBuilder.<LabeledNode, LabeledEdge>directed()
                .allowingMultipleEdges(true)
                .allowingSelfLoops(true)
                .weighted(false)
                .buildGraph();
    }

    public static abstract class LabeledNode {
        protected String label;
        protected String canonicalForm;

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
        protected String label;

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
