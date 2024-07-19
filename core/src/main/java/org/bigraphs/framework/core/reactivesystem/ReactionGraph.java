package org.bigraphs.framework.core.reactivesystem;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.io.Serializable;
import java.util.*;

/**
 * This data structure represents a "reaction graph", analogous to a labeled transition system.
 * It extends the abstract base class {@link AbstractTransitionSystem}.
 * Its states are bigraphs, and its transition relations are reaction rules of the same bigraph class type.
 * <p>
 * A reaction graph is not to be confused with the notion of minimal LTSs in bigraphs.
 * This reaction graph has no minimal context labels as transitions; it has reactions as labels.
 * <p>
 * The canonical string encoding of a bigraph is also stored here.
 *
 * @param <B> the type of the bigraph of the states and transition relations of the transition system
 * @author Dominik Grzelak
 */
public class ReactionGraph<B extends Bigraph<? extends Signature<?>>> extends AbstractTransitionSystem<B, BMatchResult<B>> implements Serializable {

    //TODO better: instead of keeping to copies (ATS also maintains graph), just transform the native one to JGraphT's graph
    protected Graph<LabeledNode, LabeledEdge> graph; // this structure is also for rendering the graph via JGraphT

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

    protected CollapsedLabeledNode collapseNodes(String newLabel, LabeledNode ...labeledNodes) {
        return new CollapsedLabeledNode(newLabel, newLabel, labeledNodes);
    }

    public void addEdge(B source, String sourceLbl, B target, String targetLbl, BMatchResult<B> reaction, String reactionLbl) {
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
                addTransition(source, target, reactionLbl, reaction);
            } else {
                transitionMap.get(reactionLbl).add(reaction);
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

    public static abstract class LabeledNode implements Serializable {
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

    public static class CollapsedLabeledNode extends LabeledNode {
        List<LabeledNode> collapsedNodes;
        public CollapsedLabeledNode(String label, String canonicalForm, List<LabeledNode> collapsedNodes) {
            super(label, canonicalForm);
            this.collapsedNodes = new ArrayList<>(collapsedNodes);
        }

        public CollapsedLabeledNode(String label, String canonicalForm, LabeledNode ...collapsedNodes) {
            this(label, canonicalForm, Arrays.asList(collapsedNodes));
        }

        public List<LabeledNode> getCollapsedNodes() {
            return collapsedNodes;
        }

        public void setCollapsedNodes(List<LabeledNode> collapsedNodes) {
            this.collapsedNodes = collapsedNodes;
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
        public Object getSource() {
            return super.getSource();
        }

        @Override
        public Object getTarget() {
            return super.getTarget();
        }

        @Override
        public String toString() {
//            return "(" + getSource() + " : " + getTarget() + " : " + label + ")";
            return "(" + label + ")";
        }
    }

    public static class CollapsedLabeledEdge extends LabeledEdge {
        List<LabeledEdge> collapsedEdges;
        public CollapsedLabeledEdge(String label, List<LabeledEdge> collapsedEdges) {
            super(label);
            this.collapsedEdges = new ArrayList<>(collapsedEdges);
        }

        public List<LabeledEdge> getCollapsedEdges() {
            return collapsedEdges;
        }

        public void setCollapsedEdges(List<LabeledEdge> collapsedEdges) {
            this.collapsedEdges = collapsedEdges;
        }
    }

}
