package org.bigraphs.framework.simulation.matching.pure;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dominik Grzelak
 */
public class IHSFilter {

    private final Bigraph<?> redex;
    private final Bigraph<?> agent;

    public IHSFilter(Bigraph<?> redexAdapter, Bigraph<?> agentAdapter) {
        this.redex = redexAdapter;
        this.agent = agentAdapter;
    }

    /**
     * Degree and label
     */
    public boolean condition1(BigraphEntity.NodeEntity<?> nodeRedex, BigraphEntity.NodeEntity<?> nodeAgent) {
        boolean labelsAreEqual = getLabel(nodeRedex).equals(getLabel(nodeAgent));
        boolean degreeIsLessOrEqual = degree(nodeRedex, redex) <= degree(nodeAgent, agent);
        return labelsAreEqual && degreeIsLessOrEqual;
    }

    /**
     * The number of adjacent nodes
     */
    public boolean condition2(BigraphEntity.NodeEntity<?> nodeRedex, BigraphEntity.NodeEntity<?> nodeAgent) {
        return numOfadj(nodeRedex, redex) <= numOfadj(nodeAgent, agent);
    }

    /**
     * Arity containment of hyperedges
     */
    public boolean condition3(BigraphEntity.NodeEntity<?> nodeRedex, BigraphEntity.NodeEntity<?> nodeAgent) {
        Map<Integer, List<BigraphEntity.Link>> incidentHyperedgesR = getIncidentHyperedges(nodeRedex, redex);
        Map<Integer, List<BigraphEntity.Link>> incidentHyperedgesA = getIncidentHyperedges(nodeAgent, agent);
        Stream<Integer> concat = Stream.concat(incidentHyperedgesR.keySet().stream(), incidentHyperedgesA.keySet().stream()).distinct();
        Set<Integer> arityValues = concat.collect(Collectors.toSet());

        // must be true for all arities ("the number of nodes in a hyperedge")
        for (Integer eachArity : arityValues) {
            boolean redexHasAritiy = (incidentHyperedgesR.get(eachArity) != null);
            boolean agentHasAritiy = (incidentHyperedgesA.get(eachArity) != null);
//            if (incidentHyperedgesR.get(eachArity) == null || incidentHyperedgesA.get(eachArity) == null) {
//                continue;
//            }
            boolean hasEdge = redexHasAritiy && incidentHyperedgesR.get(eachArity).stream().anyMatch(BigraphEntityType::isEdge);
            if (hasEdge) {
                // this branch: special treatment for bigraphs
                List<BigraphEntity.Link> edgesR = incidentHyperedgesR.get(eachArity).stream()
                        .filter(BigraphEntityType::isEdge).collect(Collectors.toList());
                List<BigraphEntity.Link> edgesA = incidentHyperedgesA.values().stream()
                        .flatMap(Collection::stream)
                        .filter(BigraphEntityType::isEdge).collect(Collectors.toList());

                if (edgesR.size() > edgesA.size()) {
                    clearMap(incidentHyperedgesA, incidentHyperedgesR);
                    return false;
                }
                for (int i = 0; i < edgesR.size(); i++) {
                    long count = redex.getPointsFromLink(edgesR.get(i)).stream().filter(BigraphEntityType::isPort)
                            .map(x -> redex.getNodeOfPort((BigraphEntity.Port) x))
                            .count();

                    boolean cmp = edgesA.stream().allMatch(x -> {
                        return agent.getPointsFromLink(x).stream().filter(BigraphEntityType::isPort)
                                .map(p -> agent.getNodeOfPort((BigraphEntity.Port) p))
                                .count() != count;
                    });

                    if (cmp) {
                        clearMap(incidentHyperedgesA, incidentHyperedgesR);
                        return false;
                    }
//                    long count1 = agent.getPointsFromLink(edgesA.get(i)).stream().filter(BigraphEntityType::isPort)
//                            .map(x -> agent.getNodeOfPort((BigraphEntity.Port) x))
//                            .count();
//                    if (count != count1) {
//                        return false;
//                    }
                }

            } else {
                // normal behavior as in the paper
                int arityR = redexHasAritiy ? incidentHyperedgesR.get(eachArity).size() : 0;
                int arityA = agentHasAritiy ? incidentHyperedgesA.get(eachArity).size() : arityR;
                if (arityR > arityA) {
                    clearMap(incidentHyperedgesA, incidentHyperedgesR);
                    return false;
                }
            }
        }
        clearMap(incidentHyperedgesA, incidentHyperedgesR);
        return true;
    }

    private void clearMap(Map... maps) {
        for (Map each : maps)
            each.clear();
    }

    /**
     * Label matchings of hyperedges
     */
    public boolean condition4(BigraphEntity.NodeEntity<?> nodeRedex, BigraphEntity.NodeEntity<?> nodeAgent) {
        Map<Integer, List<BigraphEntity.Link>> incidentHyperedgesR = getIncidentHyperedges(nodeRedex, redex);
        Map<Integer, List<BigraphEntity.Link>> incidentHyperedgesA = getIncidentHyperedges(nodeAgent, agent);
        if (incidentHyperedgesR.size() == 0 && incidentHyperedgesA.size() == 0) {
            return true;
        }
        Stream<Integer> concat = Stream.concat(incidentHyperedgesR.keySet().stream(), incidentHyperedgesA.keySet().stream()).distinct();
        Set<Integer> arityValues = concat.collect(Collectors.toSet());
        List<String> allLabels = (List<String>) agent.getSignature().getControls().stream()
                .map(x -> ((Control) x).getNamedType().stringValue())
                .collect(Collectors.toList());
        boolean noMatchAtAll = false;
        for (Integer eachArity : arityValues) {
            noMatchAtAll = false;
            List<BigraphEntity.Link> heRedex = incidentHyperedgesR.get(eachArity);
            List<BigraphEntity.Link> heAgents = incidentHyperedgesA.get(eachArity);
            if (heRedex == null) {
                noMatchAtAll = true;
                continue;
            } else if (heAgents == null) {
                noMatchAtAll = true;
                continue;
            }
            int lblMatchCount = 0;
            for (BigraphEntity.Link e1 : heRedex) {
                for (BigraphEntity.Link e2 : heAgents) {
                    for (String l : allLabels) {
                        Set<BigraphEntity.NodeEntity<?>> elR = getOfNodesForHyperedgeWithLabel(e1, l, redex);
                        Set<BigraphEntity.NodeEntity<?>> elA = getOfNodesForHyperedgeWithLabel(e2, l, agent);
                        if (elR.size() == elA.size()) {
                            lblMatchCount++;
                        }
                    }
                    if (lblMatchCount == allLabels.size()) {
                        // we can stop here, when at least for 2 hyperedges all label counts are equal above
                        return true;
                    }
                    lblMatchCount = 0;
                }
            }
        }
        return noMatchAtAll;
    }

    public Set<BigraphEntity.NodeEntity<?>> getOfNodesForHyperedgeWithLabel(BigraphEntity.Link he, String label, Bigraph<?> bigraph) {
        Set<BigraphEntity.NodeEntity<?>> collect1 = bigraph.getPointsFromLink(he).stream()
                .filter(BigraphEntityType::isPort)
                .map(x -> bigraph.getNodeOfPort((BigraphEntity.Port) x))
                .distinct()
                .filter(x -> x.getControl().getNamedType().stringValue().equals(label))
                .collect(Collectors.toSet());
        return collect1;
    }

    public Map<Integer, List<BigraphEntity.Link>> getIncidentHyperedges(BigraphEntity.NodeEntity<?> node, Bigraph<?> bigraph) {
        Map<Integer, List<BigraphEntity.Link>> heAll = new HashMap<>();
        List<BigraphEntity.Link> collect = bigraph
                .getPorts(node)
                .stream()
                .map(bigraph::getLinkOfPoint)
                .collect(Collectors.toList());
        for (BigraphEntity.Link eachLink : collect) {
            long arity = bigraph.getPointsFromLink(eachLink).stream().distinct().count();
            heAll.putIfAbsent((int) arity, new ArrayList<>());
            heAll.get((int) arity).add(eachLink);
        }
        return heAll;
    }

    /**
     * @return the number of incident hyperedges
     */
    public int degree(BigraphEntity.NodeEntity<?> node, Bigraph<?> bigraph) {
        return (int) bigraph.getPorts(node).stream()
                .map(bigraph::getLinkOfPoint)
                .filter(Objects::nonNull)
                .count();
    }

    public String getLabel(BigraphEntity.NodeEntity<?> node) {
        return node.getControl().getNamedType().stringValue();
    }

    /**
     * @return The number of adjacent nodes
     */
    public int numOfadj(BigraphEntity.NodeEntity<?> node, Bigraph<?> bigraph) {
        Set<BigraphEntity.NodeEntity<?>> collect1 = adj(node, bigraph);
        return collect1.size();
    }

    public Set<BigraphEntity.NodeEntity<?>> adj(BigraphEntity.NodeEntity<?> node, Bigraph<?> bigraph) {
        Collection<BigraphEntity.Link> collect =  bigraph.getIncidentLinksOf(node);
//                bigraph.getPorts(node)
//                .stream()
//                .map(bigraph::getLinkOfPoint)
//                .collect(Collectors.toList());
//        Set<BigraphEntity.NodeEntity<?>> collect1 = collect.stream()
//                .flatMap(x -> bigraph.getPointsFromLink(x).stream())
//                .filter(BigraphEntityType::isPort)
//                .map(x -> bigraph.getNodeOfPort((BigraphEntity.Port) x))
//                .filter(x -> x != node)
//                .collect(Collectors.toSet());
//        return collect1;
        MutableSet<BigraphEntity.NodeEntity<?>> collector = Sets.mutable.empty();
        for (BigraphEntity.Link x : collect) {
            Collection<BigraphEntity<?>> pointsFromLink = bigraph.getPointsFromLink(x);
            for (BigraphEntity<?> p : pointsFromLink) {
                if (BigraphEntityType.isPort(p)) {
                    BigraphEntity.NodeEntity<Control<?, ?>> nodeOfPort = bigraph.getNodeOfPort((BigraphEntity.Port) p);
//                    if(!nodeOfPort.equals(node)) {
                    if (nodeOfPort != node) {
                        collector.add(nodeOfPort);
                    }
                }
            }
        }
        return collector;
    }
}
