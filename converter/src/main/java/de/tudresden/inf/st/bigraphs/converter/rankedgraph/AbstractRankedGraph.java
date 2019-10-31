package de.tudresden.inf.st.bigraphs.converter.rankedgraph;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.Control;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dominik Grzelak
 */
public abstract class AbstractRankedGraph<B extends Bigraph<?>, N, E> {

    protected Graph<N, E> graph;
    protected Map<String, N> roots = new HashMap<>();
    protected Map<String, N> variables = new HashMap<>();
    protected Map<String, List<N>> variableMap = new HashMap<>();
    protected Map<String, List<N>> rootMap = new HashMap<>();
    protected B bigraph;

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

    public static class LabeledNode {
        private String id;
        private Control control;
        private BigraphEntityType type;

        public LabeledNode(String id, BigraphEntityType type) {
            this(id, type, null);
        }

        /**
         * Constructs a labeled node
         *
         * @param id the label of the new node.
         */
        public LabeledNode(String id, BigraphEntityType type, Control control) {
            this.id = id;
            this.type = type;
            this.control = control;
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

        public BigraphEntityType getType() {
            return type;
        }

        @Override
        public String toString() {
            return "(" + id + ")";
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
