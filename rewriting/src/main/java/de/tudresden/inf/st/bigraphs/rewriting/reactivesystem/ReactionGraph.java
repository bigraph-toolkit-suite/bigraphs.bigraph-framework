package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A data structure to build a reaction graph, similar to a transition system.
 * <p>
 * This is not to be compared with bigraphical LTS - the reaction graph is not minimal according to the context of a transition.
 * Here the arrows are reaction rules.
 *
 * @author Dominik Grzelak
 */
public class ReactionGraph<B extends Bigraph<?>> {

    private Map<String, B> bigraphMap = new ConcurrentHashMap<>();
    private Map<String, B> labelMap = new ConcurrentHashMap<>();
    private Graph<String, String> graph;

    public ReactionGraph() {
        graph = buildEmptySimpleDirectedGraph();
    }

    public void addEdge(B source, String sourceLbl, B target, String targetLbl, B reaction, String reactionLbl) {
        graph.addVertex(sourceLbl);
        graph.addVertex(targetLbl);
        graph.addEdge(sourceLbl, targetLbl, reactionLbl);
        bigraphMap.putIfAbsent(sourceLbl, source);
        bigraphMap.putIfAbsent(targetLbl, target);
        labelMap.putIfAbsent(reactionLbl, reaction);
    }

    public boolean containsBigraph(String label) {
        return bigraphMap.containsKey(label);
    }

    private Graph<String, String> buildEmptySimpleDirectedGraph() {
        return GraphTypeBuilder.<String, String>undirected()
                .allowingMultipleEdges(true)
                .allowingSelfLoops(true)
                .weighted(false).buildGraph();
    }

    public Graph<String, String> getGraph() {
        return graph;
    }
}
