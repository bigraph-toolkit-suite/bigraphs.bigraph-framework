package de.tudresden.inf.st.bigraphs.matching;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import de.tudresden.inf.st.bigraphs.matching.impl.AbstractDynamicMatchAdapter;
import de.tudresden.inf.st.bigraphs.matching.impl.EcoreBigraphAgentAdapter;
import de.tudresden.inf.st.bigraphs.matching.impl.EcoreBigraphRedexAdapter;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BigraphMatchingEngine<B extends DynamicEcoreBigraph> {

    private EcoreBigraphRedexAdapter redexAdapter;
    private EcoreBigraphAgentAdapter agentAdapter;
    private Table<BigraphEntity, BigraphEntity, List<BigraphEntity>> S;
    private AtomicInteger treffer;
    private Table<BigraphEntity, BigraphEntity, Integer> results;
    private int[] rootsFound;
    private int itcnt;
    private List<BigraphEntity> internalVertsG;
    private List<BigraphEntity> allVerticesOfH;

    public BigraphMatchingEngine(B agent, B redex) throws IncompatibleSignatureException {
//        this.agent = agent;
//        this.redex = redex;
        //TODO: validate
        //signature, ground agent
        redexAdapter = new EcoreBigraphRedexAdapter((DynamicEcoreBigraph) redex);
        agentAdapter = new EcoreBigraphAgentAdapter((DynamicEcoreBigraph) agent);
        init();
    }

    private void init() {
        S = HashBasedTable.<BigraphEntity, BigraphEntity, List<BigraphEntity>>create();
        for (BigraphEntity gVert : agentAdapter.getAllVertices()) {
            for (BigraphEntity hVert : redexAdapter.getAllVertices()) {
                S.put(gVert, hVert, new ArrayList<>());
            }
        }

        Iterable<BigraphEntity> leavesG = agentAdapter.getAllLeaves();
        Iterable<BigraphEntity> leavesH = redexAdapter.getAllLeaves();
        for (BigraphEntity gVert : leavesG) {
            for (BigraphEntity hVert : leavesH) {
                S.put(gVert, hVert, redexAdapter.getOpenNeighborhoodOfVertex(hVert));
            }
        }

        internalVertsG = agentAdapter.getAllInternalVerticesPostOrder();//TODO ROOT AS LAST
        // restrict search space because of redex - only roots are needed
        allVerticesOfH = new ArrayList<>(redexAdapter.getAllVertices()); //new ArrayList<>(redexAdapter.getRoots()); // // redexAdapter.getAllInternalVerticesPostOrder(); //
        System.out.println("n = " + agentAdapter.getAllVertices().size());
        System.out.println("k = " + allVerticesOfH.size());
//        System.out.println("Complexity: " + Math.pow(allVerticesOfH.size(), 1.5) * bigraphAdapter.getAllVertices().size());
        rootsFound = new int[allVerticesOfH.size()];
        itcnt = 0;
        treffer = new AtomicInteger(0);
        results = HashBasedTable.create();
    }


    /**
     * Computes all matches
     * <p>
     * First, structural matching, afterwards link matching
     */
    public void beginMatch() {
        for (BigraphEntity eachV : internalVertsG) {
            List<BigraphEntity> childrenOfV = agentAdapter.getChildren(eachV);
            List<BigraphEntity> u_vertsOfH = new ArrayList<>(allVerticesOfH);
            //d(u) <= t + 1
            int t = childrenOfV.size();
            for (int i = u_vertsOfH.size() - 1; i >= 0; i--) {
                BigraphEntity each = u_vertsOfH.get(i);
                if (redexAdapter.degreeOf(each) > t + 1) u_vertsOfH.remove(each);
            }
            int childcnt = 0;
//            System.out.println("For eachV=" + eachV.toString());
            for (BigraphEntity eachU : u_vertsOfH) {
//                System.out.println("\tFor eachU=" + eachU.toString());
                itcnt++;
                childcnt++;
                System.out.println("");
                System.out.println("New Round");
                System.out.println("itcnt = " + itcnt);
                List<BigraphEntity> neighborsOfU = redexAdapter.getOpenNeighborhoodOfVertex(eachU);
//                neighborsOfU = neighborsOfU.stream().filter(x -> !x.getType().equals(BigraphEntityType.SITE)).collect(Collectors.toList());
                Graph<BigraphEntity, DefaultEdge> bipartiteGraph = createBipartiteGraph(neighborsOfU, childrenOfV);


                //TODO: additional Conditions
                //C1: hat eachU sites als sibling? wenn nicht, dann muss die gleiche anzahl an siblings bei eachV vorhanden sein
                //C2: control check
                // stimmen controls von childrenOfV und neighborsOfU überein? (root ist platzhalter)
                // eachU und eachV müssen auch übereinstimmen


                // Connect the edges
                for (int j = 0, vn = childrenOfV.size(); j < vn; j++) {
                    for (int i = 0, un = neighborsOfU.size(); i < un; i++) {
                        if (S.get(childrenOfV.get(j), neighborsOfU.get(i)) != null && S.get(childrenOfV.get(j), neighborsOfU.get(i)).contains(eachU)) {
                            bipartiteGraph.addEdge(childrenOfV.get(j), neighborsOfU.get(i));
                        }
                    }
                }
                List<List<BigraphEntity>> partitionSets = new ArrayList<>();
                partitionSets.add(neighborsOfU);
                for (int i = 1, un = neighborsOfU.size(); i <= un; i++) {
                    List<BigraphEntity> tmp = new ArrayList<>(neighborsOfU);
                    tmp.remove(neighborsOfU.get(i - 1));
                    partitionSets.add(tmp);
                }

                //zwei sachen überprüfen:
                // 1) hat das aktuelle eachU eine Site als Children? -> dann durfen auch childs vorhanden sein
                // 2) hat das aktuelle eachU eine Site als Sibling? -> dann dürfen auch mehr siblings eachV vorhanden sein
                List<BigraphEntity> childrenWithSitesOfU = redexAdapter.getChildrenWithSites(eachU);
                boolean hasSite = false;
                for (BigraphEntity eachSibOfU : childrenWithSitesOfU) {
                    if (eachSibOfU.getType().equals(BigraphEntityType.SITE)) hasSite = true;
                }
                if (redexAdapter.isRoot(eachU.getInstance())) //if the current element is a root then automatically a "site" is inferred
                    hasSite = true;

                // compute size of maximum matching of bipartite graph for all partitions
                List<Integer> matchings = new ArrayList<>();
//                List<Integer> matchings2 = new ArrayList<>();
//                List<Boolean> matchings3 = new ArrayList<>();
                List<BigraphEntity> uSetAfterMatching = new ArrayList<>();
                int ic = 0;
                for (List<BigraphEntity> eachPartitionX : partitionSets) {
                    try {
                        HKMCBM2 alg =
                                new HKMCBM2(bipartiteGraph,
                                        new HashSet<>(eachPartitionX), new HashSet<>(childrenOfV),
                                        redexAdapter, agentAdapter
                                );
                        alg.setHasSite(hasSite);
                        MatchingAlgorithm.Matching<BigraphEntity, DefaultEdge> matching = alg.getMatching();
//                        System.out.println(matching);
//                        matchings2.add(alg.getMatchCount());
                        int m = matching.getEdges().size();
                        matchings.add(m);
//                        matchings3.add(alg.areControlsSame());
                        boolean m3 = alg.areControlsSame();
                        System.out.println((m == eachPartitionX.size()) + " <> " + m3);
                        if (m == eachPartitionX.size()) {
                            if (m3) {
                                System.out.println("\tDARF SETZEN");

                                if (ic == 0) {
                                    uSetAfterMatching.add(eachU);
                                } else {
                                    uSetAfterMatching.add(neighborsOfU.get(ic - 1));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ic++;
                }
                // update map S
                S.put(eachV, eachU, uSetAfterMatching);
//                S.get(eachV, eachU).addAll(uSetAfterMatching);
                if (S.get(eachV, eachU).contains(eachU)) {

                    int i = redexAdapter.getRoots().indexOf(eachU);
                    if (!eachU.getType().equals(BigraphEntityType.ROOT)) {
                        System.out.println("Kein root...");
                        continue;
                    }
//                    if (treffer.get() < redexAdapter.getRoots().size())
                    if (results.get(eachV, eachU) == null)
                        results.put(eachV, eachU, i);
//                    boolean isgoodcontrols = checkSubtreesControl(bigraphAdapter, eachV, redexAdapter, eachU, 0); //TODO prüfen ob mit site gemacht werden kann
//                    System.out.println("Is good? => " + isgoodcontrols);
//                    if (!isgoodcontrols) continue;
//                    if (matchings3.contains(false)) continue;
                    treffer.incrementAndGet();
//                    rootsFound[i] = 1;
                    System.out.println("FOUND A MATCHING: Agent=" + eachV.getControl() + " and Redex=" + eachU.getControl() + " // Root_ix = " + i);
                    System.out.println("Children of U");
                    redexAdapter.getChildren(eachU).forEach(x -> System.out.println(x.getControl()));
                    System.out.println("Children of V");
                    agentAdapter.getChildren(eachV).forEach(x -> System.out.println(x.getControl()));
//                    System.out.println();
//                    return;
                }
            }
        }
        System.out.println("itcnt = " + itcnt);
        System.out.println("Treffer=" + treffer.get());
        boolean b = linkMatching();


    }


    boolean linkMatching() {
        Set<BigraphEntity> bigraphEntities2 = results.rowMap().keySet();
        Set<BigraphEntity> bigraphEntities = results.columnMap().keySet();
        List<BigraphEntity> allChildrenFromNodeU = new ArrayList<>();
        List<BigraphEntity> allChildrenFromNodeV = new ArrayList<>();
        for (BigraphEntity eachV : bigraphEntities2) {
            allChildrenFromNodeV.addAll(agentAdapter.getAllChildrenFromNode(eachV));
            allChildrenFromNodeV.remove(eachV);
        }
        for (BigraphEntity eachU : bigraphEntities) {
            allChildrenFromNodeU.addAll(redexAdapter.getAllChildrenFromNode(eachU));
        }
        System.out.println(allChildrenFromNodeU.size());
        System.out.println(allChildrenFromNodeV.size());
        boolean areLinksOK = areLinksOK(allChildrenFromNodeU, allChildrenFromNodeV);
        System.out.println(areLinksOK);
        return areLinksOK;
    }

    private boolean areLinksOK(List<BigraphEntity> paritionRedex, List<BigraphEntity> paritionAgent) {
        //TODO only arity > 0
        HashMap<BigraphEntity, Boolean> lnk = new HashMap<>();
        boolean linksAreGood = true;
        for (BigraphEntity v : paritionAgent) {
            for (BigraphEntity u : paritionRedex) {
                if (!redexAdapter.isRoot(u.getInstance()) && !agentAdapter.isRoot(v.getInstance()) &&
                        u.getControl().equals(v.getControl())) {
                    linksAreGood = checkLinksForNode(u, v);
//                if (lnk.get(u) != null)
                    lnk.put(u, linksAreGood);
                }
//                if (!linksAreGood) return false;
            }
        }
        System.out.println(paritionAgent.size() * paritionRedex.size());
        return !lnk.containsValue(false);
    }

    private boolean checkLinksForNode(BigraphEntity u, BigraphEntity v) {
        List<BigraphEntity> bigraphEntities = S.get(v, u);//corresponding?
        if (bigraphEntities.size() != 0) {
            System.out.println(v.getControl() + " // " + u.getControl());
//            if (v.getControl().equals(u.getControl())) {
            List<AbstractDynamicMatchAdapter.ControlLinkPair> lnkRedex = redexAdapter.getLinksOfNode(u);
            List<AbstractDynamicMatchAdapter.ControlLinkPair> lnkAgent = agentAdapter.getLinksOfNode(v);

            //Die Anzahl muss auch stimmen
            if (lnkRedex.size() != 0 && lnkAgent.size() != 0) {
                if (lnkRedex.size() != lnkAgent.size()) return false;
                for (int i = 0, n = lnkRedex.size(); i < n; i++) {
                    List<BigraphEntity> redexLinksOfEachU = redexAdapter.getNodesOfLink((BigraphEntity.OuterName) lnkRedex.get(i).getLink());
                    List<BigraphEntity> agentLinksOfEachV = agentAdapter.getNodesOfLink((BigraphEntity.OuterName) lnkAgent.get(i).getLink());
//                    System.out.println("Schon besser2");
                    boolean isDistinctLinkR = redexLinksOfEachU.size() == 1;
                    boolean isDistinctLinkA = agentLinksOfEachV.size() == 1;
                    if (isDistinctLinkA) {
                        if (isDistinctLinkR) {
                            System.out.println("\tControl " + u.getControl() + " kann gematcht werden");
                        } else {
                            System.out.println("\tControl " + u.getControl() + " kann NICHT gematcht werden");
                            return false;
                        }
                    } else {
                        System.out.println("\tControl " + u.getControl() + " kann gematcht werden");
                    }
                }
            }
//            }
//                    System.out.println(bigraphEntities.size());

        }
        return true;

    }

    private static Graph<BigraphEntity, DefaultEdge> createBipartiteGraph(List<BigraphEntity> x, List<BigraphEntity> y) {
        SimpleGraph<BigraphEntity, DefaultEdge> bg = (SimpleGraph<BigraphEntity, DefaultEdge>) buildEmptySimpleDirectedGraph();
        for (BigraphEntity eachX : x) {
            bg.addVertex(eachX);
        }
        for (BigraphEntity eachY : y) {
            bg.addVertex(eachY);
        }
        return bg;
    }

    private static Graph<BigraphEntity, DefaultEdge> buildEmptySimpleDirectedGraph() {
        return GraphTypeBuilder.<BigraphEntity, DefaultEdge>undirected()
//                .vertexClass()
//                .vertexSupplier(vSupplier)
                .allowingMultipleEdges(false)
                .allowingSelfLoops(false)
                .edgeClass(DefaultEdge.class)
                .weighted(false).buildGraph();
    }

}
