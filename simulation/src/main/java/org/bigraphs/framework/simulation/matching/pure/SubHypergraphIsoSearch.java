package org.bigraphs.framework.simulation.matching.pure;

import com.google.common.graph.Traverser;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;

import java.util.*;

/**
 * @author Dominik Grzelak
 */
public class SubHypergraphIsoSearch {

    /**
     * Structure for an embedding.
     * Maps a query node to a host/data node.
     */
    public static class Embedding extends HashMap<BigraphEntity.NodeEntity<?>, BigraphEntity.NodeEntity<?>> {

        public Embedding() {
        }

        public Embedding(Map<? extends BigraphEntity.NodeEntity<?>, ? extends BigraphEntity.NodeEntity<?>> m) {
            super(m);
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Bigraph<?> redex;
    private final Bigraph<?> agent;

    private final IHSFilter ihsFilter;
    private final MutableMap<BigraphEntity.NodeEntity<?>, List<BigraphEntity.NodeEntity<?>>> candidates;
    MutableMap<BigraphEntity.NodeEntity<Control<?, ?>>, Float> rankMap;
    private boolean initialized;
    Set<Embedding> embeddingSet = new HashSet<>();

    public SubHypergraphIsoSearch(Bigraph<?> redex, Bigraph<?> agent) {
        this.redex = redex;
        this.agent = agent;
        this.candidates = Maps.mutable.empty();
        this.rankMap = Maps.mutable.empty();
        this.ihsFilter = new IHSFilter(redex, agent);
        this.initialized = false;
    }

    public void init() {
        assert !redex.getNodes().isEmpty();
        if (!initialized) {
            for (BigraphEntity.NodeEntity<Control<?, ?>> u_i : redex.getNodes()) {
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
            if (candidates.size() == redex.getNodes().size()) {
                return allCandidateNodesHaveValues(candidates);
            }
        }
        return linkCountRedex == 0 && linkCountAgent > 0;
    }

    public void embeddings() {
        init();
        // Get node with the highest rank
        Optional<Map.Entry<BigraphEntity.NodeEntity<Control<?, ?>>, Float>> startNode = rankMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).findFirst();
        BigraphEntity.NodeEntity<?> u_s;
        if (startNode.isPresent()) {
            u_s = startNode.get().getKey();
        } else {
            u_s = ((PureBigraph) redex).getNodes().getFirst();
        }

        for (BigraphEntity.NodeEntity<Control<?, ?>> v_s : agent.getNodes()) {
            if (!candidateGenWithBFS(u_s, v_s)) {
                continue;
            }
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
        return agent.getNodes().stream().filter(x -> x.getControl().getNamedType().stringValue().equals(label)).count() * 1.0f;
    }

    private boolean candidateGen(BigraphEntity.NodeEntity<?> u_s, BigraphEntity.NodeEntity<?> v_s) {
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

    private boolean candidateGenWithBFS(BigraphEntity.NodeEntity<?> u_s, BigraphEntity.NodeEntity<?> v_s) {
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
            traverser.breadthFirst(startNode).forEach(u_i -> {
                if (!BigraphEntityType.isNode(u_i)) return;
                BigraphEntity.NodeEntity<?> u = (BigraphEntity.NodeEntity<?>) u_i;
                if (ihsFilter.condition1(u, v_s) &&
                        ihsFilter.condition2(u, v_s) &&
                        ihsFilter.condition3(u, v_s) &&
                        ihsFilter.condition4(u, v_s)) {
                    candidates.get(u).add(v_s);
                }
            });
        }

        return !candidates.isEmpty() && candidates.values().stream().noneMatch(List::isEmpty) &&
                candidates.values().stream().flatMap(Collection::stream).distinct().count() >= candidates.size();
    }

    private boolean allEmbeddingsNonNull(Embedding emb) {
        for (BigraphEntity.NodeEntity<?> each : emb.values()) {
            if (each == null)
                return false;
        }
        return true;
    }

    private boolean allCandidateNodesHaveValues(final MutableMap<BigraphEntity.NodeEntity<?>, List<BigraphEntity.NodeEntity<?>>> candidates) {
        for (List<BigraphEntity.NodeEntity<?>> each : candidates.values()) {
            if (each == null || each.isEmpty()) return false;
        }
        return true;
    }

    private void recursiveSearch3(BigraphEntity.NodeEntity<?> u_s, LinkedList<BigraphEntity.NodeEntity<?>> cands, int index, Embedding emb) {
        if (index > cands.size()) return;
        if (emb.size() == candidates.size() && allEmbeddingsNonNull(emb)) {
            int singleMatchCnt = 0;

            for (Map.Entry<BigraphEntity.NodeEntity<?>, BigraphEntity.NodeEntity<?>> next : emb.entrySet()) {
                List<BigraphEntity.NodeEntity<?>> incidentNodesRedex = getIncidentNodesOf(next.getKey(), redex);
                List<BigraphEntity.NodeEntity<?>> incidentNodesAgent = getIncidentNodesOf(next.getValue(), agent);

                List<BigraphEntity.NodeEntity<?>> collect = incidentNodesAgent.stream().filter(emb::containsValue).toList();
                List<? extends BigraphEntity.NodeEntity<?>> collect1 = emb.entrySet().stream().filter(e -> collect.stream().anyMatch(x -> e.getValue().equals(x))).map(Map.Entry::getKey).toList();
                if (collect1.size() == incidentNodesRedex.size()) {
                    singleMatchCnt++;
                }
            }
            if (singleMatchCnt == candidates.size()) {
                embeddingSet.add(new Embedding(emb));
            }
        } else {
            List<BigraphEntity.NodeEntity<?>> nodeEntities = candidates.get(cands.get(index));
            int nextIx = index + 1;
            for (BigraphEntity.NodeEntity<?> n : nodeEntities) {
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

        MutableList<BigraphEntity.NodeEntity<?>> collector = Lists.mutable.empty();
        for (BigraphEntity.Link x : incidentHyperedges) {
            Collection<BigraphEntity<?>> pointsFromLink = bigraph.getPointsFromLink(x);
            for (BigraphEntity<?> p : pointsFromLink) {
                if (BigraphEntityType.isPort(p)) {
                    BigraphEntity.NodeEntity<Control<?, ?>> nodeOfPort = bigraph.getNodeOfPort((BigraphEntity.Port) p);
                    if (!nodeOfPort.equals(node) && !collector.contains(nodeOfPort)) {
                        collector.add(nodeOfPort);
                    }
                }
            }
        }
        return collector;
    }

    public MutableMap<BigraphEntity.NodeEntity<?>, List<BigraphEntity.NodeEntity<?>>> getCandidates() {
        return candidates;
    }
}
