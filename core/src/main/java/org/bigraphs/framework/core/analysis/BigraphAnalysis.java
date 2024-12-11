package org.bigraphs.framework.core.analysis;

import org.bigraphs.framework.core.impl.pure.PureBigraph;

/**
 * This helper class (facade) contains a collection of methods to quickly bootstrap graph-theoretical analysis for bigraphs.
 *
 * <ul>
 *     <li>
 *         Connectedness: A simple graph is connected if it has no separation, i.e., there is a path between every
 *     pair of nodes, and removing any edge will disconnect the graph.
 *     </li>
 * </ul>
 *
 * @author Dominik Grzelak
 */
@Deprecated // TODO put somewhere else: topo-bigraphs? util method?
public class BigraphAnalysis {

    /**
     * This method determines whether the place graph of a bigraph is connected.
     * This is easy, since the PG is a forest. A forest is a graph whose connected components are trees, and
     * a tree is a connected graph without cycles.
     * <p>
     * This method exists for reasons of code symmetry (refer to {@link #linkGraphIsConnectedGraph(PureBigraph)}).
     *
     * @param placeGraph the bigraph, can also be {@code null}.
     * @return {@code true} always
     */
    public boolean placeGraphIsConnectedGraph(PureBigraph placeGraph) {
        return true;
    }

    /**
     * This method computes whether the link graph of a bigraph is connected.
     * It uses the Union-Find Algorithm with almost linear running time.
     *
     * @param linkGraph the bigraph
     * @return {@code true} if the link graph is connected, otherwise {@code false}
     */
    public boolean linkGraphIsConnectedGraph(PureBigraph linkGraph) {
        PureLinkGraphConnectedComponents lgCC = new PureLinkGraphConnectedComponents();
        lgCC.decompose(linkGraph);
//        System.out.println("Partitions: " + count);
        return lgCC.getUnionFindDataStructure().getCount() == 1;
    }

}
