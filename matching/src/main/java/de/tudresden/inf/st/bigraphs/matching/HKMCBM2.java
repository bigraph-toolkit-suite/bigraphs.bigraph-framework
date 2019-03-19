package de.tudresden.inf.st.bigraphs.matching;

import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;

public class HKMCBM2 implements MatchingAlgorithm<BigraphEntity, DefaultEdge> {

    private final Graph<BigraphEntity, DefaultEdge> graph;
    private final Set<BigraphEntity> partition1;
    private final Set<BigraphEntity> partition2;

    /* Ordered list of vertices */
    private List<BigraphEntity> vertices;
    /* Mapping of a vertex to their unique position in the ordered list of vertices */
    private Map<BigraphEntity, Integer> vertexIndexMap;

    /* Number of matched vertices i partition 1. */
    private int matchedVertices;

    /* Dummy vertex. All vertices are initially matched against this dummy vertex */
    private final int DUMMY = 0;
    /* Infinity */
    private final int INF = Integer.MAX_VALUE;

    /* Array keeping track of the matching. */
    private int[] matching;
    /* Distance array. Used to compute shoretest augmenting paths */
    private int[] dist;

    /* queue used for breadth first search */
    private FixedSizeIntegerQueue queue;
    private EcoreBigraphRedexAdapter redexAdapter;
    private EcoreBigraphAgentAdapter agentAdapter;

    /**
     * Constructs a new instance of the Hopcroft Karp bipartite matching algorithm. The input graph
     * must be bipartite. For efficiency reasons, this class does not check whether the input graph
     * is bipartite. Invoking this class on a non-bipartite graph results in undefined behavior. To
     * test whether a graph is bipartite, use {@link GraphTests#isBipartite(Graph)}.
     *
     * @param graph      bipartite graph
     * @param partition1 the first partition of vertices in the bipartite graph
     * @param partition2 the second partition of vertices in the bipartite graph
     */
    public HKMCBM2(
            Graph<BigraphEntity, DefaultEdge> graph, Set<BigraphEntity> partition1, Set<BigraphEntity> partition2, EcoreBigraphRedexAdapter redexAdapter, EcoreBigraphAgentAdapter agentAdapter) {
        this.graph = GraphTests.requireUndirected(graph);
        this.redexAdapter = redexAdapter;
        this.agentAdapter = agentAdapter;
        // Ensure that partition1 is smaller or equal in size compared to partition 2
        if (partition1.size() <= partition2.size()) {
            this.partition1 = partition1;
            this.partition2 = partition2;
        } else { // else, swap
            this.partition1 = partition2;
            this.partition2 = partition1;
        }
    }

    /**
     * Initialize data structures
     */
    private void init() {
        vertices = new ArrayList<>();
        vertices.add(null);
        vertices.addAll(partition1);
        vertices.addAll(partition2);
        vertexIndexMap = new HashMap<>();
        for (int i = 0; i < vertices.size(); i++)
            vertexIndexMap.put(vertices.get(i), i);

        matching = new int[vertices.size() + 1];
        dist = new int[partition1.size() + 1];
        queue = new FixedSizeIntegerQueue(vertices.size());
    }

    /**
     * Greedily compute an initial feasible matching
     */
    private void warmStart() {
        for (BigraphEntity uOrig : partition1) {
            int u = vertexIndexMap.get(uOrig);

            for (BigraphEntity vOrig : Graphs.neighborListOf(graph, uOrig)) {
                int v = vertexIndexMap.get(vOrig);
                if (matching[v] == DUMMY) {
                    matching[v] = u;
                    matching[u] = v;
                    matchedVertices++;
                    break;
                }
            }
        }
    }

    /**
     * BFS function which finds the shortest augmenting path. The length of the shortest augmenting
     * path is stored in dist[DUMMY].
     *
     * @return true if an augmenting path was found, false otherwise
     */
    private boolean bfs() {
        queue.clear();

        for (int u = 1; u <= partition1.size(); u++)
            if (matching[u] == DUMMY) { // Add all unmatched vertices to the queue and set their
                // distance to 0
                dist[u] = 0;
                queue.enqueue(u);
            } else // Set distance of all matched vertices to INF
                dist[u] = INF;
        dist[DUMMY] = INF;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            if (dist[u] < dist[DUMMY])
                for (BigraphEntity vOrig : Graphs.neighborListOf(graph, vertices.get(u))) {
                    int v = vertexIndexMap.get(vOrig);
                    if (dist[matching[v]] == INF) {
                        dist[matching[v]] = dist[u] + 1;
                        queue.enqueue(matching[v]);
                    }
                }
        }
        return dist[DUMMY] != INF; // Return true if an augmenting path is found
    }

    /**
     * Find all vertex disjoint augmenting paths of length dist[DUMMY]. To find paths of dist[DUMMY]
     * length, we simply follow nodes that are 1 distance increments away from each other.
     *
     * @param u vertex from which the DFS is started
     * @return true if an augmenting path from vertex u was found, false otherwise
     */
    private boolean dfs(int u) {
        if (u != DUMMY) {
            for (BigraphEntity vOrig : Graphs.neighborListOf(graph, vertices.get(u))) {
                int v = vertexIndexMap.get(vOrig);
                if (dist[matching[v]] == dist[u] + 1)
                    if (dfs(matching[v])) {
                        matching[v] = u;
                        matching[u] = v;
                        return true;
                    }
            }
            // No augmenting path has been found. Set distance of u to INF to ensure that u isn't
            // visited again.
            dist[u] = INF;
            return false;
        }
        return true;
    }

    @Override
    public Matching<BigraphEntity, DefaultEdge> getMatching() {
        this.init();
        this.warmStart();

        while (matchedVertices < partition1.size() && bfs()) {
            // Greedily search for vertex disjoint augmenting paths
            for (int v = 1; v <= partition1.size() && matchedVertices < partition1.size(); v++)
                if (matching[v] == DUMMY) // v is unmatched
                    if (dfs(v))
                        matchedVertices++;
        }
        assert matchedVertices <= partition1.size();

        Set<DefaultEdge> edges = new HashSet<>();
        for (int i = 0; i < vertices.size(); i++) {
            if (matching[i] != DUMMY) {
                //todo: check if they have link matches agentAdapter.getLinksOfNode(vertices.get(matching[i]))
                //redexAdapter.getLinksOfNode(vertices.get(i)).get(0).getLink(); redex
                // Adapter.getLinksOfNode(vertices.get(i)).get(0).getLink().getName()
                //agent: matching[i]
                //redex: vertices.get(i)

//                System.out.println("\t-----");
                List<AbstractMatchAdapter.ControlLinkPair> linksOfNode = redexAdapter.getLinksOfNode(vertices.get(matching[i]));
//                linksOfNode.addAll(redexAdapter.getLinksOfNode(vertices.get(i)));
                List<AbstractMatchAdapter.ControlLinkPair> linksOfNode1 = agentAdapter.getLinksOfNode(vertices.get(i));
//                linksOfNode1.addAll(agentAdapter.getLinksOfNode(vertices.get(matching[i])));
//                if (linksOfNode.size() > 0) {
//                    BigraphEntity link = linksOfNode.get(0).getLink();
//                    String name = ((BigraphEntity.OuterName) link).getName();
//                    System.out.println("\tControl: " + vertices.get(i).getControl() + " // Name redex link: " + name + " Size=" + linksOfNode.size());
//                }
//                if (linksOfNode1.size() > 0) {
//                    BigraphEntity link1 = linksOfNode1.get(0).getLink();
//                    String name1 = ((BigraphEntity.OuterName) link1).getName();
//                    System.out.println("\tControl: " + vertices.get(matching[i]).getControl() + " // Name agent link: " + name1 + " Size=" + linksOfNode1.size());
//                }

                if (redexAdapter.isRoot(vertices.get(i).getInstance()) || redexAdapter.isRoot(vertices.get(matching[i]).getInstance())) {
                    edges.add(graph.getEdge(vertices.get(i), vertices.get(matching[i])));
                } else if (vertices.get(i).getControl().equals(vertices.get(matching[i]).getControl()) && linksOfNode.size() == linksOfNode1.size()) {
                    //&&
                    //                        vertices.get(i).getControl().equals(vertices.get(matching[i]).getControl())
                    edges.add(graph.getEdge(vertices.get(i), vertices.get(matching[i])));
                }

                //TODO: if controls are not the same, check if they have a link

            }
        }
        return new MatchingImpl<>(graph, edges, edges.size());
    }


}
