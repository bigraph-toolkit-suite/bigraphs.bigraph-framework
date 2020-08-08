package de.tudresden.inf.st.bigraphs.simulation.matching.pure;

import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.simulation.matching.AbstractDynamicMatchAdapter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dominik Grzelak
 */
public class SubHypergraphIsoSearch {
    private final Bigraph<?> redex;
    private final Bigraph<?> agent;

    private final IHSFilter ihsFilter;
    private final Map<BigraphEntity.NodeEntity<?>, List<BigraphEntity.NodeEntity<?>>> candidates;
    Map<BigraphEntity.NodeEntity<Control<?, ?>>, Float> rankMap;
    private boolean initialized;
    Set<Embedding> embeddingSet = new HashSet<>();

    public SubHypergraphIsoSearch(Bigraph<?> redex, Bigraph<?> agent) {
        this.redex = redex;
        this.agent = agent;
        this.candidates = new HashMap<>();
        this.rankMap = new HashMap<>();
        this.ihsFilter = new IHSFilter(redex, agent);
        this.initialized = false;
    }

    public void init() {
        assert redex.getNodes().size() != 0;
        if (!initialized) {
            for (BigraphEntity.NodeEntity<Control<?, ?>> u_i : redex.getNodes()) {
//                if (redex.getPortCount(u_i) > 0)
                candidates.putIfAbsent(u_i, new ArrayList<>());
                computeRankFor(rankMap, u_i);
            }
            initialized = true;
        }
    }

    public void reset() {
        candidates.clear();
        embeddingSet.clear();
        initialized = false;
    }

    public boolean allCandidatesFound() {
        if (!initialized) return false;

        int linkCountRedex = redex.getAllLinks().size();
        int linkCountAgent = agent.getAllLinks().size();
        if (linkCountRedex == 0 && linkCountAgent == 0) return true;
        if (linkCountRedex > 0 && linkCountAgent > 0) {
//            long positveArityNode = redex.getNodes().stream().filter(x -> redex.getPortCount(x) > 0).count();
            if (candidates.size() == redex.getNodes().size()) {
                return candidates.values().stream().allMatch(x -> x.size() > 0);
            }
        }
        return linkCountRedex == 0 && linkCountAgent > 0;
    }

    public void embeddings() {
        init();
        Optional<Map.Entry<BigraphEntity.NodeEntity<Control<?, ?>>, Float>> startNode = rankMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).findFirst();
        BigraphEntity.NodeEntity<Control<?, ?>> u_s;
        if (startNode.isPresent()) {
            u_s = startNode.get().getKey();
        } else {
            u_s = redex.getNodes().iterator().next();
        }

        for (BigraphEntity.NodeEntity<Control<?, ?>> v_s : agent.getNodes()) {
//            System.out.println("new iteration: " + v_s);
            if (!candidateGenWithBFS(u_s, v_s)) {
                continue;
            }
//            System.out.println("yes: " + candidates);
            Embedding emb = new Embedding();
            LinkedList<BigraphEntity.NodeEntity<?>> cands = new LinkedList<>(candidates.keySet());
            recursiveSearch3(u_s, cands, 0, emb);
        }
    }

    private void computeRankFor(Map<BigraphEntity.NodeEntity<Control<?, ?>>, Float> rankMap, BigraphEntity.NodeEntity<Control<?, ?>> u) {
        float freq = freq(agent, u);
        float degree = ihsFilter.degree(u, redex) * 1.0f;
        float rank = freq / degree;
        rankMap.put(u, rank);
    }

    public Map<BigraphEntity.NodeEntity<Control<?, ?>>, Float> computeRanks() {
        Map<BigraphEntity.NodeEntity<Control<?, ?>>, Float> rankMap = new HashMap<>();
        for (BigraphEntity.NodeEntity<Control<?, ?>> u : redex.getNodes()) {
            float freq = freq(agent, u);
            float degree = ihsFilter.degree(u, redex) * 1.0f;
            float rank = freq / degree;
            rankMap.put(u, rank);
        }
        return rankMap;
    }

    private float freq(Bigraph<?> agent, BigraphEntity.NodeEntity<Control<?, ?>> redexNode) {
        String label = ihsFilter.getLabel(redexNode);
        float sameLabelInAgent = agent.getNodes().stream().filter(x -> x.getControl().getNamedType().stringValue().equals(label)).count() * 1.0f;
        return sameLabelInAgent;
    }

    private boolean candidateGen(BigraphEntity.NodeEntity<Control<?, ?>> u_s, BigraphEntity.NodeEntity<Control<?, ?>> v_s) {
        if (agent.getPortCount(v_s) > 0 && redex.getPortCount(u_s) > 0) {
            if (ihsFilter.condition1(u_s, v_s) &&
                    ihsFilter.condition2(u_s, v_s) &&
                    ihsFilter.condition3(u_s, v_s) &&
                    ihsFilter.condition4(u_s, v_s)) {
                candidates.get(u_s).add(v_s);
                return true;
            }
        }
        return false;
    }

    private boolean candidateGenWithBFS(BigraphEntity.NodeEntity<Control<?, ?>> u_s, BigraphEntity.NodeEntity<Control<?, ?>> v_s) {
        Traverser<BigraphEntity> traverser = Traverser.forGraph(redex::getOpenNeighborhoodOfVertex);
        int rootCnt = redex.getRoots().size();
        BigraphEntity<?>[] startNodes = new BigraphEntity[rootCnt];
        startNodes[0] = u_s;
        if (rootCnt > 1) {
            // if we have a forest, the start node cannot reach the other trees using the traverser as defined above
            // So we add the other root nodes to the start nodes
            BigraphEntity.RootEntity topLevelRoot = redex.getTopLevelRoot(u_s);
            Iterator<BigraphEntity.RootEntity> iterator = redex.getRoots().iterator();
            int ix = 1;
            while (iterator.hasNext()) {
                BigraphEntity.RootEntity root = iterator.next();
                if (root != topLevelRoot) {
                    startNodes[ix++] = root;
                }
            }
        }
        for (BigraphEntity<?> startNode : startNodes) {
//            Iterable<BigraphEntity> bigraphEntities = traverser.breadthFirst(startNode);
            traverser.breadthFirst(startNode).forEach(u_i -> {
                if (!BigraphEntityType.isNode(u_i)) return;
                BigraphEntity.NodeEntity<?> u = (BigraphEntity.NodeEntity<?>) u_i;
//                System.out.println(u.getName() + " -> " + v_s.getName());
                if (ihsFilter.condition1(u, v_s) &&
                        ihsFilter.condition2(u, v_s) &&
                        ihsFilter.condition3(u, v_s) &&
                        ihsFilter.condition4(u, v_s)) {
                    candidates.get(u).add(v_s);
                }
            });
        }

        boolean b = candidates.size() > 0 && candidates.values().stream().allMatch(x -> x.size() > 0) &&
                candidates.values().stream().flatMap(Collection::stream).distinct().count() >= candidates.size();
        return b;
    }

    private void recursiveSearch3(BigraphEntity.NodeEntity u_s, LinkedList<BigraphEntity.NodeEntity<?>> cands, int index, Embedding emb) {
//        System.out.println("entry of recSearch3; u_s = " + u_s);
        if (index > cands.size()) return;
        if (emb.size() == candidates.size() && emb.values().stream().allMatch(Objects::nonNull)) {
//            System.out.println("\t\t begin search now ...");
            Iterator<Map.Entry<BigraphEntity.NodeEntity<?>, BigraphEntity.NodeEntity<?>>> iterator = emb.entrySet().iterator();
            int singleMatchCnt = 0;

            while (iterator.hasNext()) {
                Map.Entry<BigraphEntity.NodeEntity<?>, BigraphEntity.NodeEntity<?>> next = iterator.next();
                List<BigraphEntity.NodeEntity<?>> incidentNodesRedex = getIncidentNodesOf(next.getKey(), redex);
                List<BigraphEntity.NodeEntity<?>> incidentNodesAgent = getIncidentNodesOf(next.getValue(), agent);

                List<BigraphEntity.NodeEntity<?>> collect = incidentNodesAgent.stream().filter(emb::containsValue).collect(Collectors.toList());
                List<? extends BigraphEntity.NodeEntity<?>> collect1 = emb.entrySet().stream().filter(e -> collect.stream().anyMatch(x -> e.getValue().equals(x))).map(Map.Entry::getKey).collect(Collectors.toList());
                if (collect1.size() == incidentNodesRedex.size()) {
                    singleMatchCnt++;
                }
            }
            if (singleMatchCnt == candidates.size()) {
//                System.out.println("\t\t Embedding found: " + emb);
                embeddingSet.add(new Embedding(emb));
            }
        } else {
            List<BigraphEntity.NodeEntity<?>> nodeEntities = candidates.get(cands.get(index));
            int nextIx = index + 1;
            for (BigraphEntity.NodeEntity<?> n : nodeEntities) {
//                System.out.println("\t find embeddings for " + cands.get(index) + " --[to]--> " + n);
                emb.put(cands.get(index), n);
                recursiveSearch3(cands.get(index), cands, nextIx, emb);
                emb.remove(cands.get(index));
            }
        }
    }

    public Set<Embedding> getEmbeddingSet() {
        return embeddingSet;
    }

    private List<BigraphEntity.NodeEntity<?>> getIncidentNodesOf(BigraphEntity.NodeEntity<?> node, Bigraph<?> bigraph) {
        Collection<BigraphEntity.Link> incidentHyperedges = bigraph.getIncidentLinksOf(node);

        List<BigraphEntity.NodeEntity<?>> collect = (List) incidentHyperedges.stream().flatMap(x -> bigraph.getPointsFromLink(x).stream())
                .filter(x -> BigraphEntityType.isPort((BigraphEntity) x))
                .map(p -> bigraph.getNodeOfPort((BigraphEntity.Port) p))
                .filter(n -> !n.equals(node)).distinct().collect(Collectors.toList());

        return collect;
    }

    public Map<BigraphEntity.NodeEntity<?>, List<BigraphEntity.NodeEntity<?>>> getCandidates() {
        return candidates;
    }

    public static class Embedding extends HashMap<BigraphEntity.NodeEntity<?>, BigraphEntity.NodeEntity<?>> {

        public Embedding() {
        }

        public Embedding(Map<? extends BigraphEntity.NodeEntity<?>, ? extends BigraphEntity.NodeEntity<?>> m) {
            super(m);
        }
    }
}
