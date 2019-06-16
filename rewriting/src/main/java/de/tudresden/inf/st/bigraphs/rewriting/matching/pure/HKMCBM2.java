package de.tudresden.inf.st.bigraphs.rewriting.matching.pure;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.util.FixedSizeIntegerQueue;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Slightly modified version of the original {@link org.jgrapht.alg.matching.HopcroftKarpMaximumCardinalityBipartiteMatching}
 * algorithm.
 *
 * @author Dominik Grzelak
 * @see org.jgrapht.alg.matching.HopcroftKarpMaximumCardinalityBipartiteMatching
 */
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
    private PureBigraphRedexAdapter redexAdapter;
    private PureBigraphAgentAdapter agentAdapter;

    private Set<Control> availableControls = new LinkedHashSet<>();
    Set<Pair<Control, Control>> set = new HashSet<>();
    Table<Control, Control, Boolean> possibleLinks = HashBasedTable.create();
//    Table<String, String, Boolean> incidenceLeft = HashBasedTable.create();
//    Table<String, String, Boolean> incidenceRight = HashBasedTable.create();

    private List<Control> availableControlsRedex = new ArrayList<>();
    private List<Control> availableControlsAgent = new ArrayList<>();

    private boolean hasSite = false;
//    boolean crossBoundary = true;

    /**
     * Constructs a new instance of the Hopcroft Karp bipartite matching algorithm. The input graph
     * must be bipartite. For efficiency reasons, this class does not check whether the input graph
     * is bipartite. Invoking this class on a non-bipartite graph results in undefined behavior. To
     * test whether a graph is bipartite, use {@link GraphTests#isBipartite(Graph)}.
     *
     * @param graph      bipartite graph
     * @param partition1 the first partition of vertices in the bipartite graph (REDEX)
     * @param partition2 the second partition of vertices in the bipartite graph (AGENT)
     */
    public HKMCBM2(
            Graph<BigraphEntity, DefaultEdge> graph, Set<BigraphEntity> partition1, Set<BigraphEntity> partition2, PureBigraphRedexAdapter redexAdapter, PureBigraphAgentAdapter agentAdapter) {
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

//        System.out.println("swapedPlaces = " + swapedPlaces);
        List<Control> collect = partition1.stream().map(x -> x.getControl()).filter(Objects::nonNull)
                .collect(Collectors.toList());
        availableControlsRedex = new ArrayList<>(collect);
        List<Control> collect1 = partition2.stream().map(x -> x.getControl()).filter(Objects::nonNull)
                .collect(Collectors.toList());
        availableControlsAgent = new ArrayList<>(collect1);
        availableControls.addAll(collect);
        availableControls.addAll(collect1);

        for (Control a : collect) {
            for (Control b : collect1) {
                set.add(new Pair<>(a, b));
            }
        }
        for (Pair<Control, Control> eachPair : set) {
            possibleLinks.put(eachPair.getFirst(), eachPair.getSecond(), false);
        }

//    }
//        Permutations.of(new ArrayList<>(availableControls)).forEach(p -> {
//            p.forEach(System.out::print);
//            possibleLinks.put(p)
//            System.out.println(" ");
//        });
//        availableControls.stream().forEach(x -> System.out.print(x + ", " + x + "\n"));
    }

    public boolean isHasSite() {
        return hasSite;
    }

    public void setHasSite(boolean hasSite) {
        this.hasSite = hasSite;
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

    //preserve insertion order (duplicate names are anyway not allowed in bigraphs)
//    Map<String, Integer> mapRedex = new LinkedHashMap<>();
//    Map<String, Integer> mapAgent = new LinkedHashMap<>();

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

    private int matchCount = 0;

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

//        System.out.println("Redex = " + mapAgent);
//        System.out.println("Agent = " + mapRedex);
        Set<DefaultEdge> edges = new HashSet<>();
        for (int i = 0; i < vertices.size(); i++) {
            if (matching[i] != DUMMY) {
//                System.out.println("Control Redex = " + vertices.get(i).getControl());
//                System.out.println("Control Agent = " + vertices.get(matching[i]).getControl());
                edges.add(graph.getEdge(vertices.get(i), vertices.get(matching[i])));
            } else {
                loserNodes.add(vertices.get(i));
            }
        }

        return new MatchingImpl<>(graph, edges, edges.size());
    }

    List<BigraphEntity> loserNodes = new ArrayList<>();

    //TODO: think about to move this out from this class or integrate it better here
    public boolean areControlsSame() {
        Map<Control, Long> ctrlsRedex = availableControlsRedex.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        Map<Control, Long> ctrlsAgent = availableControlsAgent.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        if (hasSite) { //(!collect2.equals(collect)) {
            boolean controlsAreGood = false;
            Iterator<Map.Entry<Control, Long>> redexIterator = ctrlsRedex.entrySet().iterator();
            while (redexIterator.hasNext()) {
                Map.Entry<Control, Long> each = redexIterator.next();
                //is subset
                if (ctrlsAgent.get(each.getKey()) == null) {
//                    iterator.remove();
                    controlsAreGood = false;
                } else if (each.getValue() > ctrlsAgent.get(each.getKey())) {
                    controlsAreGood = false;
                } else {
//                    System.out.println("OK");
                    controlsAreGood = true;
                    redexIterator.remove();
                }
            }
//            System.out.println("controls stimmen überein...." + controlsAreGood);
            boolean isSubset = ctrlsRedex.size() == 0;
//            System.out.println("Subredex is subset of subagent=" + isSubset);
            return isSubset; //&& availableControlsRedex.size() >= availableControlsAgent.size()
        } else {
            boolean controlsAreGood = false;
            Iterator<Map.Entry<Control, Long>> redexIterator = ctrlsRedex.entrySet().iterator();
            while (redexIterator.hasNext()) {
                Map.Entry<Control, Long> each = redexIterator.next();
                //must be equal
                if (ctrlsAgent.get(each.getKey()) == null) {
                    controlsAreGood = false;
                } else if (!ctrlsAgent.get(each.getKey()).equals(each.getValue())) {
                    controlsAreGood = false;
                } else {
//                    System.out.println("OK");
                    controlsAreGood = true;
                    redexIterator.remove();
                }
            }
//            System.out.println("controls stimmen nicht überein...." + controlsAreGood);
            boolean isSubset = ctrlsRedex.size() == 0 && controlsAreGood;
//            System.out.println("Subredex isSubset of subagent=" + isSubset);
            return isSubset; //&& availableControlsRedex.size() <= availableControlsAgent.size()
        }
    }

    public int getMatchCount() {
        return matchCount;
    }

//    public boolean isCrossBoundary() {
//        return crossBoundary;
//    }
//
//    public void setCrossBoundary(boolean crossBoundary) {
//        this.crossBoundary = crossBoundary;
//    }

    //https://stackoverflow.com/a/32532049
    static String str(int i) {
        return i < 0 ? "" : str((i / 26) - 1) + (char) (65 + i % 26);
    }


}
