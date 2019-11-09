package de.tudresden.inf.st.bigraphs.rewriting.matching;

import com.google.common.collect.Lists;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;

import java.util.List;

/**
 * Interface for implementing a matching algorithm for a concrete bigraph kind (e.g., pure bigraphs).
 *
 * @author Dominik Grzelak
 */
public interface BigraphMatchEngine<B extends Bigraph<? extends Signature<?>>> {

    List<BigraphMatch<B>> getMatches();

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
}
