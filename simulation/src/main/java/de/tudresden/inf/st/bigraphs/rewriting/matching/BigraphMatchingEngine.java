package de.tudresden.inf.st.bigraphs.rewriting.matching;

import com.google.common.collect.Lists;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.List;

/**
 * Interface for implementing a matching algorithm for a concrete bigraph kind (e.g., pure bigraphs).
 *
 * @author Dominik Grzelak
 */
public interface BigraphMatchingEngine<B extends Bigraph<? extends Signature<?>>> {

    <M extends BigraphMatch<B>> List<M> getMatches();

    default List<BigraphEntity> getSubBigraphFrom(final BigraphEntity node, final AbstractDynamicMatchAdapter adapter) {
        Traverser<BigraphEntity> childTraverser = Traverser.forTree(xx -> adapter.getChildren(xx));
        return Lists.newArrayList(childTraverser.breadthFirst(node));
    }

    /**
     * Helper method to check whether two nodes have the same control
     *
     * @param node1 first node
     * @param node2 second node
     * @return {@code true} if both nodes have the same control, otherwise {@code false}
     */
    default boolean isSameControl(BigraphEntity node1, BigraphEntity node2) {
        return node1.getControl().equals(node2.getControl());
    }

    /**
     * Helper function to create a bipartite graph.
     *
     * @param x first set of nodes
     * @param y second set of nodes
     * @return a bipartite graph
     */
    static Graph<BigraphEntity, DefaultEdge> createBipartiteGraph(List<BigraphEntity> x, List<BigraphEntity> y) {
        SimpleGraph<BigraphEntity, DefaultEdge> bg = (SimpleGraph<BigraphEntity, DefaultEdge>) BigraphMatchingEngine.buildEmptySimpleDirectedGraph();
        for (BigraphEntity eachX : x) {
            bg.addVertex(eachX);
        }
        for (BigraphEntity eachY : y) {
            bg.addVertex(eachY);
        }
        return bg;
    }

    /**
     * Helper function to create a directed graph.
     *
     * @return a directed graph
     */
    static Graph<BigraphEntity, DefaultEdge> buildEmptySimpleDirectedGraph() {
        return GraphTypeBuilder.<BigraphEntity, DefaultEdge>undirected()
//                .vertexClass()
//                .vertexSupplier(vSupplier)
                .allowingMultipleEdges(false)
                .allowingSelfLoops(false)
                .edgeClass(DefaultEdge.class)
                .weighted(false).buildGraph();
    }
}
