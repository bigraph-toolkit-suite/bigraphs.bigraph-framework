package de.tudresden.inf.st.bigraphs.matching;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.building.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import de.tudresden.inf.st.bigraphs.matching.impl.AbstractDynamicMatchAdapter;
import de.tudresden.inf.st.bigraphs.matching.impl.EcoreBigraphAgentAdapter;
import de.tudresden.inf.st.bigraphs.matching.impl.EcoreBigraphRedexAdapter;
import de.tudresden.inf.st.bigraphs.store.BigraphModelFileStore;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.junit.jupiter.api.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
@Disabled
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlaceGraphMatching {
//
//    @Test
//    void model_test_0() throws Exception {
//        simple_match_test((DynamicEcoreBigraph) createAgent_model_test_0(), (DynamicEcoreBigraph) createRedex_model_test_0());
//    }
//
//    @Test
//    void model_test_1() throws Exception {
//        simple_match_test((DynamicEcoreBigraph) createAgent_model_test_1(), (DynamicEcoreBigraph) createRedex_model_test_1());
//    }
//
//    @Test
//    void model_test_2() throws Exception {
//        simple_match_test((DynamicEcoreBigraph) createAgent_model_test_2(), (DynamicEcoreBigraph) createRedex_model_test_2());
//    }
//
//    @Test
//    @DisplayName("Printer/User example")
//    void model_test_3() throws Exception {
//        simple_match_test((DynamicEcoreBigraph) createAgent_model_test_3(), (DynamicEcoreBigraph) createRedex_model_test_3());
//    }
//
//    public static void simple_match_test(DynamicEcoreBigraph bigraph0, DynamicEcoreBigraph redex) throws Exception {
////        DynamicEcoreBigraph bigraph0 = (DynamicEcoreBigraph) createAgent_model_test_1();
////        DynamicEcoreBigraph redex = (DynamicEcoreBigraph) createRedex_model_test_1();
//        FileOutputStream fos = new FileOutputStream("./redex.xmi");
//        FileOutputStream fagent = new FileOutputStream("./agent.xmi");
//        BigraphModelFileStore.exportBigraph(redex, "redex.xmi", fos);
//        BigraphModelFileStore.exportBigraph(bigraph0, "agent.xmi", fagent);
//
//        EcoreBigraphAgentAdapter bigraphAdapter = new EcoreBigraphAgentAdapter(bigraph0);
//        EcoreBigraphRedexAdapter redexAdapter = new EcoreBigraphRedexAdapter(redex);
//        // everything under the root level of the redex is also considered as a site
//        boolean isConform = redexAdapter.checkRedexConform();
//        System.out.println("Redex is partial conform: " + isConform);
//        if (!isConform) throw new RuntimeException("Redex is not conform");
//
//        //TODO: check if links are cross boundary: dann müssen die roots zusammengeführt werden
//        //und im bmm ist das ein platzhalter control der alles matched
//
////        List<AbstractDynamicMatchAdapter.ControlLinkPair> allLinksAgent = bigraphAdapter.getAllLinks();
////        List<AbstractDynamicMatchAdapter.ControlLinkPair> allLinksRedex = redexAdapter.getAllLinks();
////        throw new RuntimeException("stio");
//        Table<BigraphEntity, BigraphEntity, List<BigraphEntity>> S = HashBasedTable.<BigraphEntity, BigraphEntity, List<BigraphEntity>>create();
//        for (BigraphEntity gVert : bigraphAdapter.getAllVertices()) {
//            for (BigraphEntity hVert : redexAdapter.getAllVertices()) {
//                S.put(gVert, hVert, new ArrayList<>());
//            }
//        }
//
//        Iterable<BigraphEntity> leavesG = bigraphAdapter.getAllLeaves();
//        Iterable<BigraphEntity> leavesH = redexAdapter.getAllLeaves();
//        for (BigraphEntity gVert : leavesG) {
//            for (BigraphEntity hVert : leavesH) {
//                S.put(gVert, hVert, redexAdapter.getOpenNeighborhoodOfVertex(hVert));
//            }
//        }
//
//        System.out.println(S.size());
//
//        //get all internal vertices in postorder
//        List<BigraphEntity> internalVertsG = bigraphAdapter.getAllInternalVerticesPostOrder();//TODO ROOT AS LAST
//        // restrict search space because of redex - only roots are needed
//        List<BigraphEntity> allVerticesOfH = new ArrayList<>(redexAdapter.getAllVertices()); //new ArrayList<>(redexAdapter.getRoots()); // // redexAdapter.getAllInternalVerticesPostOrder(); //
//        System.out.println("n = " + bigraphAdapter.getAllVertices().size());
//        System.out.println("k = " + allVerticesOfH.size());
////        System.out.println("Complexity: " + Math.pow(allVerticesOfH.size(), 1.5) * bigraphAdapter.getAllVertices().size());
//        int[] rootsFound = new int[allVerticesOfH.size()];
//        int itcnt = 0;
//        AtomicInteger treffer = new AtomicInteger(0);
//        Table<BigraphEntity, BigraphEntity, Integer> results = HashBasedTable.create();
//        for (BigraphEntity eachV : internalVertsG) {//TODO: FROM HERE
//            List<BigraphEntity> childrenOfV = bigraphAdapter.getChildren(eachV);
//            List<BigraphEntity> u_vertsOfH = new ArrayList<>(allVerticesOfH);
//            //d(u) <= t + 1
//            int t = childrenOfV.size();
//            for (int i = u_vertsOfH.size() - 1; i >= 0; i--) {
//                BigraphEntity each = u_vertsOfH.get(i);
//                if (redexAdapter.degreeOf(each) > t + 1) u_vertsOfH.remove(each);
//            }
//            int childcnt = 0;
////            System.out.println("For eachV=" + eachV.toString());
//            for (BigraphEntity eachU : u_vertsOfH) {
////                System.out.println("\tFor eachU=" + eachU.toString());
//                itcnt++;
//                childcnt++;
//                System.out.println("");
//                System.out.println("New Round");
//                System.out.println("itcnt = " + itcnt);
//                List<BigraphEntity> neighborsOfU = redexAdapter.getOpenNeighborhoodOfVertex(eachU);
////                neighborsOfU = neighborsOfU.stream().filter(x -> !x.getType().equals(BigraphEntityType.SITE)).collect(Collectors.toList());
//                Graph<BigraphEntity, DefaultEdge> bipartiteGraph = createBipartiteGraph(neighborsOfU, childrenOfV);
//
//
//                //TODO: additional Conditions
//                //C1: hat eachU sites als sibling? wenn nicht, dann muss die gleiche anzahl an siblings bei eachV vorhanden sein
//                //C2: control check
//                // stimmen controls von childrenOfV und neighborsOfU überein? (root ist platzhalter)
//                // eachU und eachV müssen auch übereinstimmen
//
//
//                // Connect the edges
//                for (int j = 0, vn = childrenOfV.size(); j < vn; j++) {
//                    for (int i = 0, un = neighborsOfU.size(); i < un; i++) {
//                        if (S.get(childrenOfV.get(j), neighborsOfU.get(i)) != null && S.get(childrenOfV.get(j), neighborsOfU.get(i)).contains(eachU)) {
//                            bipartiteGraph.addEdge(childrenOfV.get(j), neighborsOfU.get(i));
//                        }
//                    }
//                }
//                List<List<BigraphEntity>> partitionSets = new ArrayList<>();
//                partitionSets.add(neighborsOfU);
//                for (int i = 1, un = neighborsOfU.size(); i <= un; i++) {
//                    List<BigraphEntity> tmp = new ArrayList<>(neighborsOfU);
//                    tmp.remove(neighborsOfU.get(i - 1));
//                    partitionSets.add(tmp);
//                }
//
//                //zwei sachen überprüfen:
//                // 1) hat das aktuelle eachU eine Site als Children? -> dann durfen auch childs vorhanden sein
//                // 2) hat das aktuelle eachU eine Site als Sibling? -> dann dürfen auch mehr siblings eachV vorhanden sein
//                List<BigraphEntity> childrenWithSitesOfU = redexAdapter.getChildrenWithSites(eachU);
//                boolean hasSite = false;
//                for (BigraphEntity eachSibOfU : childrenWithSitesOfU) {
//                    if (eachSibOfU.getType().equals(BigraphEntityType.SITE)) hasSite = true;
//                }
//                if (redexAdapter.isRoot(eachU.getInstance())) //if the current element is a root then automatically a "site" is inferred
//                    hasSite = true;
//
//                // compute size of maximum matching of bipartite graph for all partitions
//                List<Integer> matchings = new ArrayList<>();
////                List<Integer> matchings2 = new ArrayList<>();
////                List<Boolean> matchings3 = new ArrayList<>();
//                List<BigraphEntity> uSetAfterMatching = new ArrayList<>();
//                int ic = 0;
//                for (List<BigraphEntity> eachPartitionX : partitionSets) {
//                    try {
//                        HKMCBM2 alg =
//                                new HKMCBM2(bipartiteGraph,
//                                        new HashSet<>(eachPartitionX), new HashSet<>(childrenOfV),
//                                        redexAdapter, bigraphAdapter
//                                );
//                        alg.setHasSite(hasSite);
//                        MatchingAlgorithm.Matching<BigraphEntity, DefaultEdge> matching = alg.getMatching();
////                        System.out.println(matching);
////                        matchings2.add(alg.getMatchCount());
//                        int m = matching.getEdges().size();
//                        matchings.add(m);
////                        matchings3.add(alg.areControlsSame());
//                        boolean m3 = alg.areControlsSame();
//                        System.out.println((m == eachPartitionX.size()) + " <> " + m3);
//                        if (m == eachPartitionX.size()) {
//                            if (m3) {
//                                System.out.println("\tDARF SETZEN");
//
//                                if (ic == 0) {
//                                    uSetAfterMatching.add(eachU);
//                                } else {
//                                    uSetAfterMatching.add(neighborsOfU.get(ic - 1));
//                                }
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    ic++;
//                }
//                // update map S
//                S.put(eachV, eachU, uSetAfterMatching);
////                S.get(eachV, eachU).addAll(uSetAfterMatching);
//                if (S.get(eachV, eachU).contains(eachU)) {
//
//                    int i = redexAdapter.getRoots().indexOf(eachU);
//                    if (!eachU.getType().equals(BigraphEntityType.ROOT)) {
//                        System.out.println("Kein root...");
//                        continue;
//                    }
////                    if (treffer.get() < redexAdapter.getRoots().size())
//                    if (results.get(eachV, eachU) == null)
//                        results.put(eachV, eachU, i);
////                    boolean isgoodcontrols = checkSubtreesControl(bigraphAdapter, eachV, redexAdapter, eachU, 0); //TODO prüfen ob mit site gemacht werden kann
////                    System.out.println("Is good? => " + isgoodcontrols);
////                    if (!isgoodcontrols) continue;
////                    if (matchings3.contains(false)) continue;
//                    treffer.incrementAndGet();
////                    rootsFound[i] = 1;
//                    System.out.println("FOUND A MATCHING: Agent=" + eachV.getControl() + " and Redex=" + eachU.getControl() + " // Root_ix = " + i);
//                    System.out.println("Children of U");
//                    redexAdapter.getChildren(eachU).forEach(x -> System.out.println(x.getControl()));
//                    System.out.println("Children of V");
//                    bigraphAdapter.getChildren(eachV).forEach(x -> System.out.println(x.getControl()));
////                    System.out.println();
////                    return;
//                }
//            }
//        }
//
//        //FALLS ich nun allen roots was zugeordnet haben...
//        //ist es innerhalb der roots cross boundary?
//        //ist es zwischen den roots cross boundary?
//        //-> das entscheidet welche node sets ich übergebe
//
//
//        //Agent == crossboundary? Redex != crossboundary? => kein problem -> false
//        //Agent == crossboundary? Redex == crossboundary? => kein problem -> true/false
//        //Agent != crossboundary? Redex != crossboundary? => kein problem -> false
//        //Agent != crossboundary? Redex == crossboundary? => PROBLEM
//        System.out.println(itcnt);
//        System.out.println(treffer.get());
//
//        //crossboundary: mind. 1 link geht über zwei roots/parents -> dann muss ich jeweils immer alles nehmen aus U
//        //und von V
//        //Agent: mindestens ein outername/edge hat nodes verlinkt die zwei verschiedene parents haben
//        //Redex: mindestens ein outername/edge hat nodes verlinkt die zwei verschiedene parents haben
//
//        //falls nicht: dann so lassen:
//        //
//
//        boolean cross = true;
//        Set<BigraphEntity> bigraphEntities2 = results.rowMap().keySet();
//        Set<BigraphEntity> bigraphEntities = results.columnMap().keySet();
//        cross = false;
//        if (cross) {
//            List<BigraphEntity> allChildrenFromNodeU = new ArrayList<>();
//            List<BigraphEntity> allChildrenFromNodeV = new ArrayList<>();
//            for (BigraphEntity eachV : bigraphEntities2) {
//                allChildrenFromNodeV.addAll(bigraphAdapter.getAllChildrenFromNode(eachV));
//                allChildrenFromNodeV.remove(eachV);
//            }
//            for (BigraphEntity eachU : bigraphEntities) {
//                allChildrenFromNodeU.addAll(redexAdapter.getAllChildrenFromNode(eachU));
//            }
//            System.out.println(allChildrenFromNodeU.size());
//            System.out.println(allChildrenFromNodeV.size());
//            boolean areLinksOK = areLinksOK(redexAdapter, allChildrenFromNodeU, bigraphAdapter, allChildrenFromNodeV, S);
//            System.out.println(areLinksOK);
//        } else {
//            for (BigraphEntity eachV : bigraphEntities2) {
//                for (BigraphEntity eachU : bigraphEntities) {
//                    List<BigraphEntity> allChildrenFromNodeU = redexAdapter.getAllChildrenFromNode(eachU);
//                    List<BigraphEntity> allChildrenFromNodeV = bigraphAdapter.getAllChildrenFromNode(eachV);
//                    allChildrenFromNodeV.remove(eachV);
//                    System.out.println("U= " + eachU.getControl() + " // V= " + eachV.getControl());
//                    System.out.println(allChildrenFromNodeU.size() + " // " + allChildrenFromNodeV.size());
//                    boolean areLinksOK = areLinksOK(redexAdapter, allChildrenFromNodeU, bigraphAdapter, allChildrenFromNodeV, S);
//                    System.out.println("areLinksOK = " + areLinksOK);
//                }
//            }
//        }
//
//        System.out.println(itcnt);
//        System.out.println(treffer.get());
//
//    }
//
//    public static boolean checkLinksForNode(EcoreBigraphRedexAdapter redexAdapter, BigraphEntity u,
//                                            EcoreBigraphAgentAdapter agentAdapter, BigraphEntity v, Table<BigraphEntity, BigraphEntity, List<BigraphEntity>> S) {
//        List<BigraphEntity> bigraphEntities = S.get(v, u);//corresponding?
//        if (bigraphEntities.size() != 0) {
//            System.out.println(v.getControl() + " // " + u.getControl());
////            if (v.getControl().equals(u.getControl())) {
//            List<AbstractDynamicMatchAdapter.ControlLinkPair> lnkRedex = redexAdapter.getLinksOfNode(u);
//            List<AbstractDynamicMatchAdapter.ControlLinkPair> lnkAgent = agentAdapter.getLinksOfNode(v);
//
//            //Die Anzahl muss auch stimmen
//            if (lnkRedex.size() != 0 && lnkAgent.size() != 0) {
//                if (lnkRedex.size() != lnkAgent.size()) return false;
//                for (int i = 0, n = lnkRedex.size(); i < n; i++) {
//                    List<BigraphEntity> redexLinksOfEachU = redexAdapter.getNodesOfLink((BigraphEntity.OuterName) lnkRedex.get(i).getLink());
//                    List<BigraphEntity> agentLinksOfEachV = agentAdapter.getNodesOfLink((BigraphEntity.OuterName) lnkAgent.get(i).getLink());
////                    System.out.println("Schon besser2");
//                    boolean isDistinctLinkR = redexLinksOfEachU.size() == 1;
//                    boolean isDistinctLinkA = agentLinksOfEachV.size() == 1;
//                    if (isDistinctLinkA) {
//                        if (isDistinctLinkR) {
//                            System.out.println("\tControl " + u.getControl() + " kann gematcht werden");
//                        } else {
//                            System.out.println("\tControl " + u.getControl() + " kann NICHT gematcht werden");
//                            return false;
//                        }
//                    } else {
//                        System.out.println("\tControl " + u.getControl() + " kann gematcht werden");
//                    }
//                }
//            }
////            }
////                    System.out.println(bigraphEntities.size());
//
//        }
//        return true;
//
//    }
//
//    public static boolean areLinksOK(EcoreBigraphRedexAdapter redexAdapter, List<BigraphEntity> paritionRedex,
//                                     EcoreBigraphAgentAdapter agentAdapter, List<BigraphEntity> paritionAgent, Table<BigraphEntity, BigraphEntity, List<BigraphEntity>> S) {
//        //TODO only arity > 0
//        HashMap<BigraphEntity, Boolean> lnk = new HashMap<>();
//        boolean linksAreGood = true;
//        for (BigraphEntity v : paritionAgent) {
//            for (BigraphEntity u : paritionRedex) {
//                if (!redexAdapter.isRoot(u.getInstance()) && !agentAdapter.isRoot(v.getInstance()) &&
//                        u.getControl().equals(v.getControl())) {
//                    linksAreGood = checkLinksForNode(redexAdapter, u, agentAdapter, v, S);
////                if (lnk.get(u) != null)
//                    lnk.put(u, linksAreGood);
//                }
////                if (!linksAreGood) return false;
//            }
//        }
//        System.out.println(paritionAgent.size() * paritionRedex.size());
//        return !lnk.containsValue(false);
//    }
//
//    @Deprecated
//    public boolean checkSubtreesControl(EcoreBigraphAgentAdapter adapterLeft, BigraphEntity nodeLeft,
//                                        EcoreBigraphRedexAdapter adapterRight,
//                                        BigraphEntity nodeRight, int round) {
////        if(adapterLeft.getSiblings(nodeLeft).size() == 0 || adapterRight.getSiblings(nodeRight).size() == 0) {
////            return false;
////        }
//        List<BigraphEntity> sibLeft = adapterLeft.getChildren(nodeLeft);
//        List<BigraphEntity> sibRight = adapterRight.getChildrenWithSites(nodeRight);
//        if (sibLeft.size() == 0 && sibRight.size() == 0) return true;
//        boolean hasSite = false;
//        for (BigraphEntity eachRight : sibRight) {
//            if (eachRight.getType().equals(BigraphEntityType.SITE)) hasSite = true;
//        }
//        if (round == 0) hasSite = true;
//        //TODO control-linkname pair?
//        List<Control> sibLeftControls = sibLeft.stream().map(x -> x.getControl()).filter(Objects::nonNull).collect(Collectors.toList());
//        List<Control> sibRightControls = sibRight.stream().map(x -> x.getControl()).filter(Objects::nonNull).collect(Collectors.toList());
//
////        List<AbstractDynamicMatchAdapter.ControlLinkPair> lnkLeft = adapterLeft.getAllLinks(nodeLeft);
////        List<AbstractDynamicMatchAdapter.ControlLinkPair> lnkRight = adapterRight.getAllLinks(nodeRight);
//        //das rechte zusammenfassen für
//        //agent darf edge+outername haben für check
//        //redex darf nur outername haben für check
//
////        Table<Control, String, Integer> incidence2 = HashBasedTable.create();
////        for (AbstractDynamicMatchAdapter.ControlLinkPair eachPairLeft : lnkLeft) {
////            for (AbstractDynamicMatchAdapter.ControlLinkPair eachPairLeft2 : lnkLeft) {
////                EAttribute nameAttr = EMFUtils.findAttribute(eachPairLeft2.getLink().getInstance().eClass(), "name");
////                Object name = eachPairLeft2.getLink().getInstance().eGet(nameAttr);
//////                if (eachPairLeft.getLink().equals(eachPairLeft2.getLink())) { //&& !eachPairRight.getControl().equals(eachPairRight2.getControl())
////                incidence2.put(eachPairLeft.getControl(), String.valueOf(name), 1);
//////                } else {
////                incidence2.put(eachPairLeft.getControl(), String.valueOf(name), 0);
//////                }
////            }
////        }
////        boolean isgood = false;
//        //TODO: also check for distinct names and same names
//        //means: same number of connections for each control = count only outer names (edges are anyway not recorded in the set)
////        if (round == 0) {
////            if (lnkRight.size() == 0) {
////                //hat der agent überhaupt links? - > wichtig
////                int cntMatch = 0;
////                for (Control each : sibLeftControls) {
////                    for (AbstractDynamicMatchAdapter.ControlLinkPair eachPairLeft : lnkLeft) {
////                        if (eachPairLeft.getControl().equals(each)) cntMatch++;
////                    }
////                }
////                if (cntMatch != 0) {
////                    System.out.println("links fehlen im Redex");
////                    return false;
////                }
////
////            } else {
////                boolean found = false;
//////        Table<Control, Control, Integer> incidence = HashBasedTable.create();
////                for (AbstractDynamicMatchAdapter.ControlLinkPair eachPairRight : lnkRight) {
////                    //get the link and control
////                    //check in left, wheter a control exists with a linking
////                    found = false; //reset
////                    for (AbstractDynamicMatchAdapter.ControlLinkPair eachPairLeft : lnkLeft) {
////                        if (eachPairLeft.getControl().equals(eachPairRight.getControl())) {
////                            found = true;
////                            break;
////                        }
////                    }
////                    if (!found) break;
////                }
////                System.out.println("isgood =" + found);
////                if (!found) return false;
////            }
////        }
//
//
//        if (!hasSite) {
////            if (sibLeft.size() != sibRight.size()) return false;
//            if (!sibLeftControls.equals(sibRightControls)) return false;
//        } else {
//            for (BigraphEntity eachUControl : sibRight) {
//                if (eachUControl.getType().equals(BigraphEntityType.SITE)) continue;
//                if (!sibLeftControls.contains(eachUControl.getControl())) {
//                    return false;
//                }
//            }
//        }
//        for (BigraphEntity eachRight : sibRight) {
//            for (BigraphEntity eachLeft : sibLeft) {
//                if (!eachRight.getType().equals(BigraphEntityType.SITE)) {
//                    if (eachLeft.getControl().equals(eachRight.getControl())) {
//                        boolean result = checkSubtreesControl(adapterLeft, eachLeft, adapterRight, eachRight, ++round);
//                        if (!result) return false;
//                    }
//                }
//            }
//        }
//        System.out.println("Geschafft");
//        return true;
//    }
//
//    private static Graph<BigraphEntity, DefaultEdge> createBipartiteGraph(List<BigraphEntity> x, List<BigraphEntity> y) {
//        SimpleGraph<BigraphEntity, DefaultEdge> bg = (SimpleGraph<BigraphEntity, DefaultEdge>) buildEmptySimpleDirectedGraph();
//        for (BigraphEntity eachX : x) {
//            bg.addVertex(eachX);
//        }
//        for (BigraphEntity eachY : y) {
//            bg.addVertex(eachY);
//        }
//        return bg;
//    }
//
//    private static Graph<BigraphEntity, DefaultEdge> buildEmptySimpleDirectedGraph() {
//        return GraphTypeBuilder.<BigraphEntity, DefaultEdge>undirected()
////                .vertexClass()
////                .vertexSupplier(vSupplier)
//                .allowingMultipleEdges(false)
//                .allowingSelfLoops(false)
//                .edgeClass(DefaultEdge.class)
//                .weighted(false).buildGraph();
//    }
//
//
//    public Bigraph createRedex_model_test_3() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
//        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
//        BigraphBuilder<DefaultDynamicSignature> builder = BigraphBuilder.start(signature);
//
//        BigraphEntity.OuterName e0 = builder.createOuterName("e0");
//        BigraphEntity.OuterName a1 = builder.createOuterName("a1");
//        BigraphEntity.OuterName a2 = builder.createOuterName("a2");
//        BigraphEntity.OuterName b2 = builder.createOuterName("b2");
//        BigraphEntity.OuterName b3 = builder.createOuterName("b3");
//        BigraphEntity.OuterName u1 = builder.createOuterName("u1");
//
////        big r = (
////                (Room{e0} . (Printer{a1, b2}.1))
////| (Room{e0} . (Printer{a2, b3}.1))
////| User{b4}.1
////);
//
//        builder.createRoot()
//                .addChild(signature.getControlByName("Room")).connectNodeToOuterName(e0)
//                .withNewHierarchy().addChild(signature.getControlByName("Printer")).connectNodeToOuterName(a1).connectNodeToOuterName(b2)
//                .goBack()
//
//                .addChild(signature.getControlByName("Room")).connectNodeToOuterName(e0)
//                .withNewHierarchy().addChild(signature.getControlByName("Printer")).connectNodeToOuterName(a2).connectNodeToOuterName(b3)
//                .goBack()
//
//                .addChild(signature.getControlByName("User")).connectNodeToOuterName(u1)
//        ;
//
//        return builder.createBigraph();
//    }
//
//    public static Bigraph createAgent_model_test_3() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
//        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
//        BigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder = BigraphBuilder.start(signature);
//
//        BigraphEntity.InnerName roomLink = builder.createInnerName("e0");
//        BigraphEntity.OuterName a1 = builder.createOuterName("a1");
//        BigraphEntity.OuterName a2 = builder.createOuterName("a2");
//        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
//        BigraphEntity.OuterName b2 = builder.createOuterName("b2");
//        BigraphEntity.OuterName u1 = builder.createOuterName("u1");
//
//        builder.createRoot()
//                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
//                .withNewHierarchy().addChild(signature.getControlByName("Printer")).connectNodeToOuterName(a1).connectNodeToOuterName(b1)
//                .goBack()
//
//                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
//                .withNewHierarchy().addChild(signature.getControlByName("Printer")).connectNodeToOuterName(a2).connectNodeToOuterName(b2)
//                .goBack()
//
//                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
//                .withNewHierarchy().addChild(signature.getControlByName("Printer")).connectNodeToOuterName(a2).connectNodeToOuterName(b2)
//                .goBack()
//
//                .addChild(signature.getControlByName("User")).connectNodeToOuterName(u1)
//        ;
//
//        builder.closeAllInnerNames();
//        builder.makeGround();
//        return builder.createBigraph();
//
//    }
//
//    public static Bigraph createAgent_model_test_1() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
//        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
//        BigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder = BigraphBuilder.start(signature);
//        BigraphEntity.OuterName jeff1 = builder.createOuterName("jeff1");
//        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");
//        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
//        BigraphEntity.InnerName e1 = builder.createInnerName("e1");
//
////        big s1 = /e1
////                ((Room{e1} . (Computer{b1}.(Job.1) | User{jeff1}.1 ))
////|
////        (Room{e1} . (Computer{b1}.(Job.1 | User{jeff2}.1))));
//
//        builder.createRoot()
//                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(e1)
//                .withNewHierarchy()
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
//                .withNewHierarchy().addChild(signature.getControlByName("Job")).goBack()
//                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff1)
//                .goBack()
//
//                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(e1)
//                .withNewHierarchy()
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
//                .withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff2)
//                .goBack().goBack();
//
//        builder.closeAllInnerNames();
//        builder.makeGround();
//        return builder.createBigraph();
//    }
//
//    public static Bigraph createRedex_model_test_1() throws LinkTypeNotExistsException, InvalidConnectionException {
//        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
//        BigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder = BigraphBuilder.start(signature);
//        BigraphEntity.OuterName jeff1 = builder.createOuterName("jeff1");
//        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");
//        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
//
//        //(Computer{b1}.(Job.1) | User{jeff2}.1) || Computer{b1}.(Job.1 | User{jeff2}.1);
//
//        builder.createRoot()
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
//                .withNewHierarchy().addChild(signature.getControlByName("Job")).goBack()
//                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff1);
//
//        builder.createRoot()
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
//                .withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff2);
//
//        builder.makeGround();
//        return builder.createBigraph();
//
//    }
//
//    public static Bigraph createRedex_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
//        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
//        BigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder = BigraphBuilder.start(signature);
//        BigraphEntity.OuterName jeff1 = builder.createOuterName("jeff1");
//        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");
//        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
//        BigraphEntity.OuterName b2 = builder.createOuterName("b2");
//        builder.
//                createRoot()
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
//                .withNewHierarchy()
//                .addChild(signature.getControlByName("Job"))
//                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff2)
//                .goBack()
//
//        ;
//        builder.createRoot()
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
//                .withNewHierarchy()
//                .addChild(signature.getControlByName("Job"))
//                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff1)
//                .goBack()
//        ;
//        return builder.createBigraph();
//    }
//
//    public static Bigraph createAgent_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
//        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
//        BigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder = BigraphBuilder.start(signature);
//
//        BigraphEntity.InnerName roomLink = builder.createInnerName("tmp1_room");
//        BigraphEntity.OuterName a = builder.createOuterName("a");
//        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
////        BigraphEntity.OuterName b2 = builder.createOuterName("b2");
//        BigraphEntity.OuterName jeff = builder.createOuterName("jeff");
//        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");
//
//        builder.createRoot()
//                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
//                .withNewHierarchy()
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
//                .withNewHierarchy()
//                .addChild(signature.getControlByName("Job"))
//                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff2)
//                .goBack()
//                .goBack()
//
//                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
//                .withNewHierarchy()
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
//                .withNewHierarchy()
//                .addChild(signature.getControlByName("Job"))
//                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff2)
//                .goBack()
//                .goBack()
//
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
//        ;
//        builder.closeAllInnerNames();
//        builder.makeGround();
//
//        DynamicEcoreBigraph bigraph = builder.createBigraph();
//        return bigraph;
//
//    }
//
//    public static Bigraph createRedex_model_test_2() throws LinkTypeNotExistsException, InvalidConnectionException {
//        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
//        BigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder = BigraphBuilder.start(signature);
//        BigraphEntity.OuterName jeff = builder.createOuterName("jeff1");
//        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
//
//
//        // (Computer{b1}.(id(1)) | Computer{jeff1}.1 | Job.1) || (User{jeff1}.(Job.1 | Job.1));
//        builder.
//                createRoot()
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
//                .withNewHierarchy().addSite().goBack()
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(jeff)
//                .addChild(signature.getControlByName("Job"))
//        ;
//        builder.createRoot()
//                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff)
//                .withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).goBack()
//        ;
//        return builder.createBigraph();
//    }
//
//
//    public static Bigraph createAgent_model_test_2() throws LinkTypeNotExistsException, InvalidConnectionException {
//        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
//        BigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder = BigraphBuilder.start(signature);
//
//        BigraphEntity.InnerName door = builder.createInnerName("door");
//        BigraphEntity.OuterName e1 = builder.createOuterName("eroom");
//        BigraphEntity.OuterName e0 = builder.createOuterName("espool");
//        BigraphEntity.OuterName a = builder.createOuterName("a");
//        BigraphEntity.OuterName b = builder.createOuterName("b");
//        BigraphEntity.OuterName jeff1 = builder.createOuterName("jeff1");
//        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");
//
////        big s1 = /door (
////                (Room{door} . (Computer{a}.1 | Computer{a}.(Job.1 | Job.(Job.1) | Job.1) | Computer{a}.1 | Computer{jeff}.1 | Job.1 ))
////                | (Spool{e0}.1)
////                | (Room{e1} . (User{jeff}.(Job.1 | Job.1) | Job.1 | Job.1))
////                );
//
//        builder.createRoot()
//                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(door)
//                .withNewHierarchy()
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
//                .withNewHierarchy()
//                .addChild(signature.getControlByName("Job"))
//                .addChild(signature.getControlByName("Job")).withNewHierarchy().addChild(signature.getControlByName("Job")).goBack()
//                .addChild(signature.getControlByName("Job")).goBack()
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(jeff1)
//                .addChild(signature.getControlByName("Job"))
//                .goBack()
//
//                .addChild(signature.getControlByName("Spool")).connectNodeToOuterName(e0)
//
//                .addChild(signature.getControlByName("Room")).connectNodeToOuterName(e1)
//                .withNewHierarchy()
//                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff2)
//                .withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).goBack()
//                .addChild(signature.getControlByName("Job"))
//                .addChild(signature.getControlByName("Job"))
//
//        ;
//
////        builder.closeInnerName(roomLink);
////        builder.closeInnerName(printerSpoolLink);
//        builder.closeAllInnerNames();
//        builder.makeGround();
//
//        DynamicEcoreBigraph bigraph = builder.createBigraph();
//        return bigraph;
//    }
//
//    private static <C extends Control<?, ?>> Signature<C> createExampleSignature() {
//        DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> defaultBuilder = new DynamicSignatureBuilder<>();
//        defaultBuilder
//                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
//                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign();
//
//        return (Signature<C>) defaultBuilder.create();
//    }
//
//
//    //    @Test
////    void function_tests() throws Exception {
////        DynamicEcoreBigraph bigraph0 = (DynamicEcoreBigraph) create();
////        FileOutputStream fio = new FileOutputStream("./test.xmi");
//////        FileOutputStream fioMeta = new FileOutputStream("./test_meta.ecore");
////
////        EcoreBigraphAgentAdapter bigraphAdapter = new EcoreBigraphAgentAdapter(bigraph0);
////        for (BigraphEntity next : bigraphAdapter.getAllVertices()) {
////            int i = bigraphAdapter.degreeOf(next);
////            System.out.println(next.getType() + " has degreeOf=" + i + " (" + next.getControl() + ")");
////        }
////
////        System.out.println("Internal vertices");
////        List<BigraphEntity> allInternalVerticesPostOrder = bigraphAdapter.getAllInternalVerticesPostOrder();
////        for (BigraphEntity next : allInternalVerticesPostOrder) {
//////            int i = bigraphAdapter.degreeOf(next);
////            System.out.println(next.getType() + " (" + next.getControl() + ")");
////        }
//////
////        System.out.println("All leaves:");
////        Collection<BigraphEntity> allLeaves = bigraphAdapter.getAllLeaves();
////        for (BigraphEntity each : allLeaves) {
////            System.out.println(each.getControl());
////        }
////
//////        System.out.println("Open neighborhood:");
//////        for (BigraphEntity next : bigraph.getNodesWithRoots()) {
//////            Collection<BigraphEntity> openNeighborhoodOf = bigraph.getOpenNeighborhoodOf(next);
//////            System.out.println("Node " + next.getControl() + " has #neighbors=" + openNeighborhoodOf.size());
//////        }
////
////        //TODO: unterscheidung nötig? leaves/getchildren/heighborhood liefert probably auch edges+outernames (sind in dem
////        //matching moment ja auch knoten
////        //beim maximum matching wird dann geschaut, ob
////
////        BigraphModelFileStore.exportBigraph((DynamicEcoreBigraph) bigraph0, "test.xmi", fio);
////
////    }

}
