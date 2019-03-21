package de.tudresden.inf.st.bigraphs.matching;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultControl;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DefaultSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.EcoreBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import de.tudresden.inf.st.bigraphs.store.BigraphModelFileStore;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlaceGraphMatching {

//    public static void main(String[] args) {
//        try {
//            new PlaceGraphMatching().simple_match_test();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Test
    void simple_match_test() throws Exception {
        DynamicEcoreBigraph bigraph0 = (DynamicEcoreBigraph) create();
        DynamicEcoreBigraph redex = (DynamicEcoreBigraph) createRedex();
        FileOutputStream fos = new FileOutputStream("./redex.xmi");
        FileOutputStream fagent = new FileOutputStream("./agent.xmi");
        BigraphModelFileStore.exportBigraph(redex, "redex.xmi", fos);
        BigraphModelFileStore.exportBigraph(bigraph0, "agent.xmi", fagent);

        EcoreBigraphAgentAdapter bigraphAdapter = new EcoreBigraphAgentAdapter(bigraph0);
        EcoreBigraphRedexAdapter redexAdapter = new EcoreBigraphRedexAdapter(redex);
        // everything under the root level of the redex is also considered as a site
        boolean isConform = redexAdapter.checkRedexConform();
        System.out.println("Redex is partial conform: " + isConform);
        if (!isConform) throw new RuntimeException("Redex is not conform");

//        List<AbstractMatchAdapter.ControlLinkPair> allLinksAgent = bigraphAdapter.getAllLinks();
//        List<AbstractMatchAdapter.ControlLinkPair> allLinksRedex = redexAdapter.getAllLinks();
//        throw new RuntimeException("stio");
        Table<BigraphEntity, BigraphEntity, List<BigraphEntity>> S = HashBasedTable.<BigraphEntity, BigraphEntity, List<BigraphEntity>>create();
        for (BigraphEntity gVert : bigraphAdapter.getAllVertices()) {
            for (BigraphEntity hVert : redexAdapter.getAllVertices()) {
                S.put(gVert, hVert, new ArrayList<>());
            }
        }

        Iterable<BigraphEntity> leavesG = bigraphAdapter.getAllLeaves();
        Iterable<BigraphEntity> leavesH = redexAdapter.getAllLeaves();
        for (BigraphEntity gVert : leavesG) {
            for (BigraphEntity hVert : leavesH) {
                S.put(gVert, hVert, redexAdapter.getOpenNeighborhoodOfVertex(hVert));
            }
        }

        System.out.println(S.size());

        //get all internal vertices in postorder
        List<BigraphEntity> internalVertsG = bigraphAdapter.getAllInternalVerticesPostOrder();//TODO ROOT AS LAST
        // restrict search space because of redex - only roots are needed
        List<BigraphEntity> allVerticesOfH = new ArrayList<>(redexAdapter.getAllVertices()); //new ArrayList<>(redexAdapter.getRoots()); // // redexAdapter.getAllInternalVerticesPostOrder(); //
        System.out.println("n = " + bigraphAdapter.getAllVertices().size());
        System.out.println("k = " + allVerticesOfH.size());
//        System.out.println("Complexity: " + Math.pow(allVerticesOfH.size(), 1.5) * bigraphAdapter.getAllVertices().size());
        int[] rootsFound = new int[allVerticesOfH.size()];
        int itcnt = 0;
        AtomicInteger treffer = new AtomicInteger(0);
        for (BigraphEntity eachV : internalVertsG) {
            itcnt++;
            List<BigraphEntity> childrenOfV = bigraphAdapter.getChildren(eachV);
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
                //zwei sachen überprüfen:
                // 1) hat das aktuelle eachU eine Site als Children? -> dann durfen auch childs vorhanden sein
                // 2) hat das aktuelle eachU eine Site als Sibling? -> dann dürfen auch mehr siblings eachV vorhanden sein


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
                List<BigraphEntity> siblingsU = redexAdapter.getChildrenWithSites(eachU);
                boolean hasSite = false;
                for (BigraphEntity eachSibOfU : siblingsU) {
                    if (eachSibOfU.getType().equals(BigraphEntityType.SITE)) hasSite = true;
                }
                if (redexAdapter.isRoot(eachU.getInstance()))
                    hasSite = true;

                // compute size of maximum matching of bipartite graph for all partitions
                List<Integer> matchings = new ArrayList<>();
                List<Integer> matchings2 = new ArrayList<>();
                List<Boolean> matchings3 = new ArrayList<>();
                List<BigraphEntity> uSetAfterMatching = new ArrayList<>();
                int ic = 0;
                for (List<BigraphEntity> eachPartitionX : partitionSets) {
                    try {
                        HKMCBM2 alg =
                                new HKMCBM2(bipartiteGraph,
                                        new HashSet<>(eachPartitionX), new HashSet<>(childrenOfV),
                                        redexAdapter, bigraphAdapter
                                );
                        alg.setHasSite(hasSite);
                        MatchingAlgorithm.Matching<BigraphEntity, DefaultEdge> matching = alg.getMatching();
//                        System.out.println(matching);
                        matchings2.add(alg.getMatchCount());
                        int m = matching.getEdges().size();
                        matchings.add(m);
                        matchings3.add(alg.areControlsSame());
                        boolean m3 = alg.areControlsSame();
                        System.out.println((m == eachPartitionX.size()) + " <> " + m3);
                        if (m == eachPartitionX.size()) { // || (redexAdapter.isRoot(eachU.getInstance()))) { //&& ic == 0
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
                    bigraphAdapter.getChildren(eachV).forEach(x -> System.out.println(x.getControl()));
//                    System.out.println();
//                    return;
                }
            }
        }
        System.out.println(itcnt);
        System.out.println(treffer.get());
    }

    public boolean checkSubtreesControl(EcoreBigraphAgentAdapter adapterLeft, BigraphEntity nodeLeft,
                                        EcoreBigraphRedexAdapter adapterRight,
                                        BigraphEntity nodeRight, int round) {
//        if(adapterLeft.getSiblings(nodeLeft).size() == 0 || adapterRight.getSiblings(nodeRight).size() == 0) {
//            return false;
//        }
        List<BigraphEntity> sibLeft = adapterLeft.getChildren(nodeLeft);
        List<BigraphEntity> sibRight = adapterRight.getChildrenWithSites(nodeRight);
        if (sibLeft.size() == 0 && sibRight.size() == 0) return true;
        boolean hasSite = false;
        for (BigraphEntity eachRight : sibRight) {
            if (eachRight.getType().equals(BigraphEntityType.SITE)) hasSite = true;
        }
        if (round == 0) hasSite = true;
        //TODO control-linkname pair?
        List<Control> sibLeftControls = sibLeft.stream().map(x -> x.getControl()).filter(Objects::nonNull).collect(Collectors.toList());
        List<Control> sibRightControls = sibRight.stream().map(x -> x.getControl()).filter(Objects::nonNull).collect(Collectors.toList());

//        List<AbstractMatchAdapter.ControlLinkPair> lnkLeft = adapterLeft.getAllLinks(nodeLeft);
//        List<AbstractMatchAdapter.ControlLinkPair> lnkRight = adapterRight.getAllLinks(nodeRight);
        //das rechte zusammenfassen für
        //agent darf edge+outername haben für check
        //redex darf nur outername haben für check

//        Table<Control, String, Integer> incidence2 = HashBasedTable.create();
//        for (AbstractMatchAdapter.ControlLinkPair eachPairLeft : lnkLeft) {
//            for (AbstractMatchAdapter.ControlLinkPair eachPairLeft2 : lnkLeft) {
//                EAttribute nameAttr = EMFUtils.findAttribute(eachPairLeft2.getLink().getInstance().eClass(), "name");
//                Object name = eachPairLeft2.getLink().getInstance().eGet(nameAttr);
////                if (eachPairLeft.getLink().equals(eachPairLeft2.getLink())) { //&& !eachPairRight.getControl().equals(eachPairRight2.getControl())
//                incidence2.put(eachPairLeft.getControl(), String.valueOf(name), 1);
////                } else {
//                incidence2.put(eachPairLeft.getControl(), String.valueOf(name), 0);
////                }
//            }
//        }
//        boolean isgood = false;
        //TODO: also check for distinct names and same names
        //means: same number of connections for each control = count only outer names (edges are anyway not recorded in the set)
//        if (round == 0) {
//            if (lnkRight.size() == 0) {
//                //hat der agent überhaupt links? - > wichtig
//                int cntMatch = 0;
//                for (Control each : sibLeftControls) {
//                    for (AbstractMatchAdapter.ControlLinkPair eachPairLeft : lnkLeft) {
//                        if (eachPairLeft.getControl().equals(each)) cntMatch++;
//                    }
//                }
//                if (cntMatch != 0) {
//                    System.out.println("links fehlen im Redex");
//                    return false;
//                }
//
//            } else {
//                boolean found = false;
////        Table<Control, Control, Integer> incidence = HashBasedTable.create();
//                for (AbstractMatchAdapter.ControlLinkPair eachPairRight : lnkRight) {
//                    //get the link and control
//                    //check in left, wheter a control exists with a linking
//                    found = false; //reset
//                    for (AbstractMatchAdapter.ControlLinkPair eachPairLeft : lnkLeft) {
//                        if (eachPairLeft.getControl().equals(eachPairRight.getControl())) {
//                            found = true;
//                            break;
//                        }
//                    }
//                    if (!found) break;
//                }
//                System.out.println("isgood =" + found);
//                if (!found) return false;
//            }
//        }


        if (!hasSite) {
//            if (sibLeft.size() != sibRight.size()) return false;
            if (!sibLeftControls.equals(sibRightControls)) return false;
        } else {
            for (BigraphEntity eachUControl : sibRight) {
                if (eachUControl.getType().equals(BigraphEntityType.SITE)) continue;
                if (!sibLeftControls.contains(eachUControl.getControl())) {
                    return false;
                }
            }
        }
        for (BigraphEntity eachRight : sibRight) {
            for (BigraphEntity eachLeft : sibLeft) {
                if (!eachRight.getType().equals(BigraphEntityType.SITE)) {
                    if (eachLeft.getControl().equals(eachRight.getControl())) {
                        boolean result = checkSubtreesControl(adapterLeft, eachLeft, adapterRight, eachRight, ++round);
                        if (!result) return false;
                    }
                }
            }
        }
        System.out.println("Geschafft");
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

    //    @Test
    void function_tests() throws Exception {
        DynamicEcoreBigraph bigraph0 = (DynamicEcoreBigraph) create();
        FileOutputStream fio = new FileOutputStream("./test.xmi");
//        FileOutputStream fioMeta = new FileOutputStream("./test_meta.ecore");

        EcoreBigraphAgentAdapter bigraphAdapter = new EcoreBigraphAgentAdapter(bigraph0);
        for (BigraphEntity next : bigraphAdapter.getAllVertices()) {
            int i = bigraphAdapter.degreeOf(next);
            System.out.println(next.getType() + " has degreeOf=" + i + " (" + next.getControl() + ")");
        }

        System.out.println("Internal vertices");
        List<BigraphEntity> allInternalVerticesPostOrder = bigraphAdapter.getAllInternalVerticesPostOrder();
        for (BigraphEntity next : allInternalVerticesPostOrder) {
//            int i = bigraphAdapter.degreeOf(next);
            System.out.println(next.getType() + " (" + next.getControl() + ")");
        }
//
        System.out.println("All leaves:");
        Collection<BigraphEntity> allLeaves = bigraphAdapter.getAllLeaves();
        for (BigraphEntity each : allLeaves) {
            System.out.println(each.getControl());
        }

//        System.out.println("Open neighborhood:");
//        for (BigraphEntity next : bigraph.getNodesWithRoots()) {
//            Collection<BigraphEntity> openNeighborhoodOf = bigraph.getOpenNeighborhoodOf(next);
//            System.out.println("Node " + next.getControl() + " has #neighbors=" + openNeighborhoodOf.size());
//        }

        //TODO: unterscheidung nötig? leaves/getchildren/heighborhood liefert probably auch edges+outernames (sind in dem
        //matching moment ja auch knoten
        //beim maximum matching wird dann geschaut, ob

        BigraphModelFileStore.exportBigraph((DynamicEcoreBigraph) bigraph0, "test.xmi", fio);

    }


    public static Bigraph createRedex() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder = EcoreBigraphBuilder.start(signature);
        BigraphEntity.OuterName jeff = builder.createOuterName("jeff1");
        BigraphEntity.OuterName a1 = builder.createOuterName("a1");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
        BigraphEntity.OuterName e1 = builder.createOuterName("e1");
        builder.
                createRoot()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
                    .withNewHierarchy()
//                .addChild(signature.getControlByName("Printer")).connectNodeToOuterName(e1)//.addChild(signature.getControlByName("Job")) //.withNewHierarchy().addSite().goBack()
                        .addSite().goBack()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1).withNewHierarchy().addChild(signature.getControlByName("Job")).goBack()
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
                .addChild(signature.getControlByName("Job"))//.withNewHierarchy().addSite()

        ;
        builder.createRoot()
//                .addChild(signature.getControlByName("User")).withNewHierarchy().addSite().goBack()//.addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff)
//                .withNewHierarchy().addSite().goBack()//.addChild(signature.getControlByName("Job"))
                .withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).goBack()
//                .addSite()
//                .addChild(signature.getControlByName("Job"))
        ;
        return builder.createBigraph();
    }


    public static Bigraph create() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder = EcoreBigraphBuilder.start(signature);

        BigraphEntity.InnerName roomLink = builder.createInnerName("tmp1_room");
        BigraphEntity.InnerName printerSpoolLink = builder.createInnerName("printerSpoolLink");
        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b = builder.createOuterName("b");
        BigraphEntity.OuterName jeff = builder.createOuterName("jeff");


//        EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>>.Hierarchy room = builder.newHierarchy(signature.getControlByName("Room"));
//        room.addChild(signature.getControlByName("User"))
//                .withNewHierarchy()
//                .connectNodeToOuterName(jeff)
//                .addChild(signature.getControlByName("Job"));

//        EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>>.Hierarchy roomLeft = builder.newHierarchy(signature.getControlByName("Room"));
//        roomLeft.addChild(signature.getControlByName("Computer"))
//                .addChild(signature.getControlByName("Printer"));

        builder.createRoot()
                .addChild(signature.getControlByName("Room")).withNewHierarchy()
//                .connectByEdge(signature.getControlByName("Computer"), signature.getControlByName("Printer"))
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
                .withNewHierarchy().addChild(signature.getControlByName("Job"))
                    .addChild(signature.getControlByName("Job")).withNewHierarchy().addChild(signature.getControlByName("Job")).goBack().addChild(signature.getControlByName("Job")).goBack()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
                //add a child to the computer
//                .withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).goBack()

//                .addChild(signature.getControlByName("Printer")).connectNodeToOuterName(a).connectNodeToInnerName(printerSpoolLink)

                .addChild(signature.getControlByName("Job"))
                .goBack()
                .addChild(signature.getControlByName("Spool")).connectNodeToInnerName(printerSpoolLink)

                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
                .withNewHierarchy().addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff)
                .withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job"))
                .goBack()
                .addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("Job"))
//                .goBack()
//                .goBack()
//                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
        ;

//        builder.closeInnerName(roomLink);
//        builder.closeInnerName(printerSpoolLink);
        builder.closeAllInnerNames();
        builder.makeGround();

        DynamicEcoreBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    private static <C extends Control<?, ?>> Signature<C> createExampleSignature() {
        DefaultSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> defaultBuilder = new DefaultSignatureBuilder<>();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign();

        return (Signature<C>) defaultBuilder.create();
    }

}
