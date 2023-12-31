package org.bigraphs.framework.core;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract base class for a ranked graph representation for different classes of bigraphs.
 * The node and edge types for the ranked graph can be specified.
 * <p>
 * The concrete implementations can be found in the <strong>bigraph-converter</strong> module.
 *
 * @param <B> type of the bigraph
 * @param <N> type of the ranked graph's node
 * @param <E> type of the ranked graph's edge
 * @author Dominik Grzelak
 */
public abstract class AbstractRankedGraph<B extends Bigraph<?>, N, E> {

    protected B bigraph;

    protected Graph<N, E> graph;
    protected Map<String, N> roots = new HashMap<>(); // root and outer
    protected Map<String, N> variables = new HashMap<>(); // sites and inner

    protected Map<String, List<N>> variableMap = new HashMap<>(); //for convenience
    protected Map<String, List<N>> rootMap = new HashMap<>(); //for convenience

    protected boolean encodingStarted = false;
    protected boolean encodingFinished = false;

    public AbstractRankedGraph(B bigraph) {
        this.bigraph = bigraph;
        this.init();
    }

    protected abstract void init();

    public abstract void encode();

    protected <NT extends N, ET extends E> Graph<NT, ET> getDirectedGraph() {
        return GraphTypeBuilder.<NT, ET>directed()
                .allowingMultipleEdges(true)
                .allowingSelfLoops(false)
                .weighted(false)
                .buildGraph();
    }

    public Graph<N, E> getGraph() {
        return graph;
    }

    /**
     * Represents the two types of nodes of a ranked graph: place nodes and link nodes.
     * The method {@link LabeledNode#isPlaceNode()} can be called to determine the type.
     * It is automatically inferred by checking whether the supplied control is {@code null}.
     */
    public static class LabeledNode {
        private String id;
        private Control control;
        private BigraphEntityType type;
        private boolean isPlaceNode;

        public LabeledNode(String id, BigraphEntityType type) {
            this(id, type, null, false);
        }

        public LabeledNode(String id, BigraphEntityType type, Control control) {
            this(id, type, control, Objects.nonNull(control));
        }

        /**
         * Constructs a labeled node
         *
         * @param id the label of the new node.
         */
        private LabeledNode(String id, BigraphEntityType type, Control control, boolean isPlaceNode) {
            this.id = id;
            this.type = type;
            this.control = control;
            this.isPlaceNode = isPlaceNode;
        }


        /**
         * Gets the label associated with this node.
         *
         * @return edge label
         */
        public String getId() {
            return id;
        }

        public Control getControl() {
            return control;
        }

        /**
         * @return {@code true}, if the node is a <i>place node</i>, otherwise it is a <i>link node</i>
         */
        public boolean isPlaceNode() {
            return isPlaceNode && Objects.nonNull(control);
        }

        /**
         * Symmetric method to {@link #isPlaceNode()}.
         *
         * @return {@code true}, if the node is a <i>link node</i>, otherwise it is a <i>place node</i>
         */
        public boolean isLinkNode() {
            return !isPlaceNode();
        }

        /**
         * @return {@code true}, if the node is a root, site, outer name or inner name
         */
        public boolean isInterfaceNode() {
            return isVariableNode() || isRootNode();
        }

        public boolean isVariableNode() {
            return type == BigraphEntityType.SITE ||
                    type == BigraphEntityType.INNER_NAME;
        }

        public boolean isRootNode() {
            return type == BigraphEntityType.OUTER_NAME ||
                    type == BigraphEntityType.ROOT;
        }

        public BigraphEntityType getType() {
            return type;
        }

        @Override
        public String toString() {
            if (type == BigraphEntityType.SITE)
                return "site:" + id;
            if (type == BigraphEntityType.ROOT)
                return "root:" + id;
            if (type == BigraphEntityType.INNER_NAME)
                return "inner:" + id;
            if (type == BigraphEntityType.OUTER_NAME)
                return "outer:" + id;
            if (Objects.isNull(id) || id.isEmpty()) return "";
            return id;
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
        public Object getSource() {
            return super.getSource();
        }

        @Override
        public Object getTarget() {
            return super.getTarget();
        }

        @Override
        public String toString() {
            if (Objects.isNull(label) || label.isEmpty())
                return "";
            return "(" + getSource() + " : " + getTarget() + " : " + label + ")";
        }
    }

    public boolean isEncodingStarted() {
        return encodingStarted;
    }

    public boolean isEncodingFinished() {
        return encodingFinished;
    }
}
