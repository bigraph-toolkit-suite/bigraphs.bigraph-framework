package de.tudresden.inf.st.bigraphs.matching;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.building.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.matching.impl.AbstractDynamicMatchAdapter;
import de.tudresden.inf.st.bigraphs.matching.impl.EcoreBigraphAgentAdapter;
import de.tudresden.inf.st.bigraphs.matching.impl.EcoreBigraphRedexAdapter;
import de.tudresden.inf.st.bigraphs.visualization.GraphvizConverter;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BigraphMatchingEngine<B extends PureBigraph> {

    private EcoreBigraphRedexAdapter redexAdapter;
    private EcoreBigraphAgentAdapter agentAdapter;
    private Table<BigraphEntity, BigraphEntity, List<BigraphEntity>> S = HashBasedTable.create();
    private AtomicInteger treffer;
    private Table<BigraphEntity, BigraphEntity, Integer> results;
    private int[] rootsFound;
    private int itcnt;
    private List<BigraphEntity> internalVertsG;
    private List<BigraphEntity> allVerticesOfH;

    private List<Match<B>> matches = new LinkedList<>();

    @Deprecated
    private HashMap<Integer, BigraphEntity> hitsU = new HashMap<>();
    /**
     * The agent's match (node) is stored along the corresponding redex' root index of the match
     * (if multiple occurrences were possible)
     * <p>
     * Describes which root index of the redex matches which agent node
     */
//    @Deprecated
    private HashMap<Integer, BigraphEntity> hitsV = new HashMap<>();


    private HashMap<BigraphEntity, List<Integer>> hitsVIx = new HashMap<>();

    //Integer: redex ix | redex (root) <-> agent nodes that corresponds to the redex nodes at redex root ix
    //<root ix, map<Redexnode under root, list<agent nodes>>
    Map<Integer, Map<BigraphEntity, List<BigraphEntity>>> occurrenceTable = new HashMap<>();
//    private Table<Integer, > occurrenceTable;

    private final MutableBuilder<DefaultDynamicSignature> builder;
    private final MutableBuilder<DefaultDynamicSignature> bLinking;

    BigraphMatchingEngine(B agent, B redex) throws IncompatibleSignatureException {
//        this.agent = agent;
//        this.redex = redex;
        //TODO: validate
        //signature, ground agent
        this.builder = PureBigraphBuilder.newMutableBuilder(agent.getSignature());
        this.bLinking = PureBigraphBuilder.newMutableBuilder(agent.getSignature());
        this.redexAdapter = new EcoreBigraphRedexAdapter(redex);
        this.agentAdapter = new EcoreBigraphAgentAdapter(agent);
        this.init();
    }

    private void init() {
        S = HashBasedTable.create();
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
//        System.out.println("n = " + agentAdapter.getAllVertices().size());
//        System.out.println("k = " + allVerticesOfH.size());
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
//            int childcnt = 0;
//            System.out.println("For eachV=" + eachV.toString());
            for (BigraphEntity eachU : u_vertsOfH) {
//                System.out.println("\tFor eachU=" + eachU.toString());
                itcnt++;
//                childcnt++;
//                System.out.println("");
//                System.out.println("New Round");
//                System.out.println("itcnt = " + itcnt);
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
                List<List<BigraphEntity>> partitionSets = new ArrayList<>();//TODO refactor: do not create new list
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
                List<HKMCBM2> lastResults = new ArrayList<>();
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
                        lastResults.add(alg);
//                        matchings3.add(alg.areControlsSame());
                        boolean m3 = alg.areControlsSame();
//                        System.out.println((m == eachPartitionX.size()) + " <> " + m3);
                        if (m == eachPartitionX.size()) {
                            if (m3) {
//                                System.out.println("\tDARF SETZEN");

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

                    boolean b = linkMatching();

                    if (b) {
                        //TODO hier kann noch optimiert werden wie durchsucht wird
                        // der algo findet für den gleiche agent noch redex matches anstatt andersherum
                        // in diesem fall müssen wir eben weitersuchen. Wenn match aber "symmatrisch" ist wie
                        // im model_0 beispiel, könnten wir hier schon aufhören


                        int i1 = treffer.incrementAndGet();

                        //<root ix, map<Redexnode under root, list<agent nodes>>
                        Map<BigraphEntity, List<BigraphEntity>> nodeDiffByControlCheck = findOccurrences(childrenOfV, neighborsOfU);
//                        Map<Integer, List<BigraphEntity>> occs = new HashMap<>();
                        occurrenceTable.putIfAbsent(i, new HashMap<>());
                        occurrenceTable.get(i).putAll(nodeDiffByControlCheck);
                        // permutationen kann dann gebildet werden, um die redex nodes auf die agents zu matchen...


                        hitsVIx.putIfAbsent(eachV, new LinkedList<>());
                        hitsVIx.get(eachV).add(i);

//                        hitsU.put(i, eachU);
//                        hitsV.put(i, eachV);
                        //save last eachU and eachV
                        // check if we found a matching across all roots of the redex
                        // both following statements are equivalent
                        boolean wereAllRootsVisited = true; //i == redexAdapter.getRoots().size() - 1;
                        boolean wereAllRootsVisited2 = true; //(i1) % redexAdapter.getRoots().size() == 0;
                        if (wereAllRootsVisited2 && wereAllRootsVisited) {

//                            buildMatch();


                            System.out.println("FOUND A MATCHING: Agent=" + eachV.getControl() + " and Redex=" + eachU.getControl() + " // Root_ix = " + i);
//                            System.out.println("Children of U");
//                            redexAdapter.getChildren(eachU).forEach(x -> System.out.println(x.getControl()));
//                            System.out.println("Children of V");
//                            agentAdapter.getChildren(eachV).forEach(x -> System.out.println(x.getControl()));

                            //TODO for iterator style: save a breakpoint here somehow... aber nur wenn für alle redex roots
                            // ein distinct agent node zugewiesen werden konnte
                            // save the current child of internalVertsG and u_vertsOfH and all intermediate results
                            // do not call init() again
                            // create a new method beginMatch() which accepts arguments to begin at these stored positions
                            //  and is called later from the iterator after the first match
                            hitsU.clear();
                            hitsV.clear();
                        }
                    }
                }
            }
        }


        if (treffer.get() > 0) {

//            List<Integer> firstList = new ArrayList<>();

            // This is needed when there is not a distinct match of the redices roots to a agent's subtree
            // Überprüfen ob die indexlisten disjoint sind dann ist das eindeutig (bzw. es sollte nur ein index dort drin stehn)
            // ansonsten nicht und es gibt mehrere Möglichkeiten die Redex' roots auf die gematchten agent nodes
            // zu legen
            // Dann kann man permutieren: agent node einem root aus dem redex zuweisen

            boolean areDisjoint = true;
            // ich muss zwischen allen paaren testen.
//        Permutations.of(Arrays.asList(0, 1, 0, 1)).forEach(p -> { p.forEach(System.out::print); System.out.print(" "); });

            //TODO: mit der occurrence map umschreiben: da sind nun alle kombinationen enthalten


            for (Map.Entry<BigraphEntity, List<Integer>> each : hitsVIx.entrySet()) {
                if (each.getValue().size() == 1) {
                    hitsV.put(each.getValue().get(0), each.getKey());
                } else {
                    // es reicht wenn es nur für ein redex root mehrere möglichkeiten gibt
                    areDisjoint = false;
                    hitsV.clear();
                    break;
                }
            }
            if (!areDisjoint) {
                // es kann auch im rix = 3 und 4 mehrer möglichkeiten geben
                // wir müssen hier erstmal durch
                for (int i = 0, n = redexAdapter.getRoots().size(); i < n; i++) {
                    final int i0 = i;
                    //finde ein passendes match:
                    List<BigraphEntity> possibleAgentNodeMatches = hitsVIx.entrySet().stream()
                            .filter(x -> x.getValue().contains(i0))
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());
                    //nimm irgendeins, was noch nicht drin ist
                    for (BigraphEntity eachMatchAgent : possibleAgentNodeMatches) {
                        if (!hitsV.values().contains(eachMatchAgent)) {
                            hitsV.put(i, eachMatchAgent);
                            break;
                        }
                    }
                }
            }

            buildMatch();
        }
        System.out.println("itcnt = " + itcnt);
        System.out.println("Treffer=" + treffer.get());
    }

    /**
     * After a match is found, we need to compute the occurrences of the redex inside the match
     * <p>
     * It can be possible that not a distinct match is possible
     * (when then need to use all these when a rewrite is done!)
     *
     * @param agentNodes
     * @param redexNodes
     * @return
     */
    public Map<BigraphEntity, List<BigraphEntity>> findOccurrences(Collection<BigraphEntity> agentNodes, Collection<BigraphEntity> redexNodes) {
        List<BigraphEntity> agentNodesDiffs = new ArrayList<>();
//        List<Control> ctrlsRedex = redexNodes.stream().map(x -> x.getControl()).filter(Objects::nonNull)
//                .collect(Collectors.toList());//availableControlsRedex.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
//        Map<Control, Long> ctrlsAgent = agentNodes.stream().map(x -> x.getControl()).filter(Objects::nonNull)
//                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
//        int mapCnt = 0;
        Map<BigraphEntity, List<BigraphEntity>> mapping = new HashMap<>();
        List<BigraphEntity> agents = new ArrayList<>(agentNodes);
        //Liste durchgehen von redexNodes
        //in 2. schleifen die agent nodes die noch übrig sind
        //prüfen: control, degree (eins abziehen wegen root, wenn site in children vorhanden dann darf mehr vorhanden sein, ansonsten gleich)
        boolean controlsAreGood = false;
        //        List<Integer> ixSame = new ArrayList<>();
        for (int i = agents.size() - 1; i >= 0; i--) {
            BigraphEntity eachAgent = agents.get(i);
            boolean hadMatch = false;
            for (BigraphEntity eachRedex : redexNodes) {
                boolean b = theSame(eachAgent, eachRedex) && iSameControl(eachAgent, eachRedex);
                boolean linksAreMatching = checkLinkIdentity(eachAgent, eachRedex);
                System.out.println(b);
                if (b && linksAreMatching) {
                    hadMatch = true;
                    mapping.putIfAbsent(eachRedex, new ArrayList<>());
                    mapping.get(eachRedex).add(eachAgent);
                }
            }
//            mapCnt++;
//            if (hadMatch) agents.remove(i);
        }
        return mapping;
    }

    /**
     * find the "trace" of the subtree of the redex in the agent
     * No links are checked
     *
     * @param agent
     * @param redex
     * @return
     */
    private boolean theSame(BigraphEntity agent, BigraphEntity redex) {
        List<BigraphEntity> bigraphEntities = S.get(agent, redex);
        List<BigraphEntity> children = redexAdapter.getChildren(redex);
        if (children.size() == 0 && bigraphEntities.size() != 0) return true;
        if (bigraphEntities.size() > 0) {
            for (BigraphEntity eachChild : children) {
                if (!theSame(agent, eachChild)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    //TODO check also degree etc.
    //  only control type check for now
    private boolean iSameControl(BigraphEntity node1, BigraphEntity node2) {
        return node1.getControl().equals(node2.getControl());
    }

    private void buildMatch() {
        //calculate the context
        //the parameters etc.
        Map<Integer, Bigraph> parameters = new HashMap<>();
        PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> pureBigraphFactory =
                AbstractBigraphFactory.createPureBigraphFactory();
        boolean needsParameters = redexAdapter.getSites().size() >= 1;
        //when no params are necessary then a barren is all that is needed
        if (!needsParameters) {
            parameters.put(0, pureBigraphFactory.createPlacings().barren());
        }

        //context: replace the agent with sites at eachV and if edge exists, make inner name

        //first: create roots
        //should only have one root!
//        Optional<BigraphEntity.RootEntity> first = agentAdapter.getRoots().stream().findFirst();
//        assert first.isPresent();
        final BigraphEntity.RootEntity newRoot = (BigraphEntity.RootEntity) builder.createNewRoot(0); //first.get().getIndex());
        final Map<Integer, BigraphEntity.SiteEntity> newSites = new HashMap<>();
        final Map<String, BigraphEntity.NodeEntity> newNodes = new HashMap<>();
        final Map<String, BigraphEntity.OuterName> newOuterNames = new HashMap<>();
        final Map<String, BigraphEntity.InnerName> newInnerNames = new HashMap<>();


        final Map<String, BigraphEntity.OuterName> newOuterNamesL = new HashMap<>(); // für link graph
        final Map<String, BigraphEntity.InnerName> newInnerNamesL = new HashMap<>(); // für place graph


        Set<BigraphEntity> blockNodes = new HashSet<>();
        // to build the identity graph later for the redex
        HashMap<BigraphEntity.OuterName, List<String>> substitutionLinkingGraph = new HashMap<>();
        //recreate nodes that are in V exclusive U
        //where first u node is found replace with site
        //this should build the node hierarchy and put places under those nodes where the redex matched
        int currentRedexRoot = -1;
        for (BigraphEntity eachNodeV : agentAdapter.getAllVerticesBfsOrder()) {
            if (BigraphEntityType.isRoot(eachNodeV)) {
                continue;
            }

            // outer names are used for substitution (linking)
//            boolean b = S.containsColumn(eachNodeV);
//            List<AbstractDynamicMatchAdapter.ControlLinkPair> linksOfNode11 = agentAdapter.getLinksOfNode(eachNodeV);
//            List<String> outernamesOfAgentNode = linksOfNode11.stream()
//                    .filter(x -> BigraphEntityType.isOuterName(x.getLink()))
//                    .map(x -> ((BigraphEntity.OuterName) x.getLink()).getName()).collect(Collectors.toList());
//            List<Table.Cell<BigraphEntity, BigraphEntity, List<BigraphEntity>>> collect = S.cellSet().stream().filter(x -> x.getRowKey().equals(eachNodeV)).collect(Collectors.toList());
////            BigraphEntity correspondingNode = cellOptional.get().getColumnKey();
//            //redexAdapter.getLinksOfNode(redexAdapter.getAllVerticesBfsOrder().get(1)).get(0).getLink().getName()
//            for (BigraphEntity b : redexAdapter.getAllVerticesBfsOrder()) {
//                List<BigraphEntity> bigraphEntities = S.get(eachNodeV, b);
//                System.out.println(bigraphEntities.size());
//            }
            // skip blocked children first (children that are in the redex match
            if (blockNodes.contains(eachNodeV)) {
                //from here: its all in the redex


                // we know the current redex image: the agents nodes are returned in BFS order
                // se we can be sure the iterate through all the childs before the current redex root index changes
                //get the topmost parent of this control where the match between the specific redex root ix and the
                // agent node matches
                if (needsParameters) {
                    BigraphEntity _parent = agentAdapter.getParent(eachNodeV);
                    currentRedexRoot = -1;
                    while (_parent != null) {
                        if (hitsV.values().contains(_parent)) {
                            BigraphEntity _parent0 = _parent;
                            currentRedexRoot = hitsV.entrySet().stream().filter(x -> x.getValue().equals(_parent0)).findFirst().get().getKey();
                            break;
                        } else {
                            _parent = agentAdapter.getParent(_parent);
                        }
                    }
                    System.out.println("Current redex root ix: " + currentRedexRoot);
                    //collect everything under a redex sites that corresponds to the agent
                    // TODO das hier sind alle potentielle parameter für den Redex!
                    // TODO check if the redex hat a site at this point
                    //entweder hier machen und alle kinderknoten analysieren und dann gleich alle in die blocking nodes stecken
                    //Oder nachdem hier alles einfach von vorne anfange und nur den knoten speichern für die parameter ix
                    //index habe ich ja.
                    if (currentRedexRoot != -1) {
                        //TODO siblings are more suitable but we need to check redex nodes with agent nodes at this step
//                        parameters.put(currentRedexRoot, _parent);
                    }
                }


                //put all their children inside
//                blockNodes.addAll(agentAdapter.getChildrenOf(eachNodeV));
                blockNodes.addAll(agentAdapter.getAllChildrenFromNode(eachNodeV));
                continue;
            }


            final BigraphEntity.NodeEntity eachNodeV0 = (BigraphEntity.NodeEntity) eachNodeV;

            BigraphEntity newNode = builder.createNewNode(eachNodeV0.getControl(), eachNodeV0.getName());
            if (newNodes.containsKey(eachNodeV0.getName())) {
                newNode = newNodes.get(eachNodeV0.getName());
            }
            BigraphEntity newParent = agentAdapter.getParent(eachNodeV0);
            if (newParent == null || BigraphEntityType.isRoot(newParent)) {
                newParent = newRoot;
            } else { // else find or create a new parent
                //find the parent or create a new one first
                final String theParentName = ((BigraphEntity.NodeEntity) newParent).getName();
                final BigraphEntity _newParent0 = agentAdapter.getParent(eachNodeV0);
                newParent = newNodes.values().stream()
                        .filter(x -> x.getName().equals(theParentName) &&
                                x.getControl().equals((_newParent0.getControl())))
                        .findFirst()
                        .orElse((BigraphEntity.NodeEntity) builder.createNewNode(newParent.getControl(), theParentName));
                newNodes.put(theParentName,
                        (BigraphEntity.NodeEntity) newParent);
            }
            setParentOfNode(newNode, newParent);
            newNodes.put(((BigraphEntity.NodeEntity) newNode).getName(),
                    (BigraphEntity.NodeEntity) newNode);

            //abstract the rest away and put children into blocked list
            boolean hasAmatch = hitsV.values().contains(eachNodeV0);
            if (hasAmatch && agentAdapter.getChildrenOf(eachNodeV0).size() >= 1) {

                Integer rootIx = hitsV.entrySet().stream().filter(x -> x.getValue().equals(eachNodeV0)).findFirst().get().getKey();
                Collection<BigraphEntity> childrenOf = agentAdapter.getChildrenOf(eachNodeV0);

                //es gibt so viele möglichkeiten wie: listenelemente zählen für jedes unterschiedliche redex element
                //TODO: Permutations bilden (oder anfangs nur 1. index wählen: das sind alles verschiedene states beim rewriting später

                //key: redex nodes -> list of possible agent nodes
                List<BigraphEntity> savedRedexNodes = new ArrayList<>();
                Map<BigraphEntity, List<BigraphEntity>> redexToAgentMapping = occurrenceTable.get(rootIx);
                //Re-collect all agent nodes first
                Set<BigraphEntity> availableAgentNodes = redexToAgentMapping.values().stream()
                        .flatMap(Collection::stream).collect(Collectors.toSet());
                for (Map.Entry<BigraphEntity, List<BigraphEntity>> redexAgentMapping : redexToAgentMapping.entrySet()) {
                    //get any value from the value list
                    for (BigraphEntity correspondence : redexAgentMapping.getValue()) {
                        if (availableAgentNodes.contains(correspondence)) {
                            savedRedexNodes.add(correspondence);
                            //remove the set of available nodes to take
                            availableAgentNodes.remove(correspondence);
                            childrenOf.remove(correspondence); //and reduce also the set of agent's children at this step
                            break;
                        }
                    }
                }

                //rest of the available nodes remains and the savedRedexNodes goes into the blocking list
                //and all their children
                savedRedexNodes.forEach(x -> {
                    blockNodes.add(x);
                    blockNodes.addAll(agentAdapter.getChildrenOf(x));
                });
                for (BigraphEntity eachRemaining : childrenOf) {
                    BigraphEntity.NodeEntity eachRemaining0 = (BigraphEntity.NodeEntity) eachRemaining;
                    BigraphEntity.NodeEntity newNode1 = (BigraphEntity.NodeEntity) builder.createNewNode(eachRemaining0.getControl(), eachRemaining0.getName());
                    newNodes.put(newNode1.getName(), newNode1);
                    setParentOfNode(newNode1, newNode);
                }
                //get the root index of the corresponding redex match node eachU
                BigraphEntity.SiteEntity newSite = (BigraphEntity.SiteEntity) builder.createNewSite(rootIx); //first1.get().getKey());
                newSites.put(newSite.getIndex(), newSite);
                setParentOfNode(newSite, newNode);
            }

            //TODO collect also outernames
            //  28.5.19:
            //  Make EDGES TO INNERNAMES (OK) BUT
            //  make alle links to innernames (?)


            List<AbstractDynamicMatchAdapter.ControlLinkPair> linksOfNode = agentAdapter.getLinksOfNode(eachNodeV0);
            for (AbstractDynamicMatchAdapter.ControlLinkPair eachPair : linksOfNode) {
                String linkName;
                if (BigraphEntityType.isEdge(eachPair.getLink())) {
                    linkName = ((BigraphEntity.Edge) eachPair.getLink()).getName();
//EDIT
                    BigraphEntity.InnerName innerName = newInnerNames.entrySet().stream()
                            .filter(e -> e.getValue().getName().equals(linkName))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(builder.createInnerName(linkName + "_innername"));
//                    BigraphEntity.InnerName innerName = builder.createInnerName(link.getName());
                    newInnerNames.put(innerName.getName(), innerName);
                    try {
                        builder.connectNodeToInnerName((BigraphEntity.NodeEntity) newNode, innerName);
                    } catch (LinkTypeNotExistsException | InvalidConnectionException e) {
                        e.printStackTrace();
                    }
                    //EDIT
                } else if (BigraphEntityType.isOuterName(eachPair.getLink())) {
                    final BigraphEntity.OuterName link = (BigraphEntity.OuterName) eachPair.getLink();
//                    BigraphEntity.OuterName newOuterName = (BigraphEntity.OuterName) builder.createNewOuterName(((BigraphEntity.OuterName) eachPair.getLink()).getName());
                    BigraphEntity.OuterName newOuterName = newOuterNames.values().stream()
                            .filter(outerName -> outerName.getName().equals(link.getName()))
                            .findFirst()
                            .orElse(builder.createOuterName(link.getName()));
                    //EDIT
                    newOuterNames.put(newOuterName.getName(), newOuterName);
                    builder.connectNodeToOuterName((BigraphEntity.NodeEntity) newNode, newOuterName);

                    //TODO find here the nodes of the redex that should be reconnected
                    //if no parameters are defined
//                    BigraphEntity newOuterNameL = bLinking.createNewOuterName(link.getName());
//                    BigraphEntity.InnerName innerNameL = bLinking.createInnerName(newOuterName.getName());
//                    bLinking.connectInnerToOuter(innerNameL, (BigraphEntity.OuterName) newOuterNameL);
//                    newOuterNamesL.put(link.getName(), (BigraphEntity.OuterName) newOuterNameL);
//                    newInnerNamesL.put(innerNameL.getName(), innerNameL);
                    //EDIT

                    //create also inner name with same name (meant to be a substitution - linking)
//                    BigraphEntity.InnerName innerName = builder.createInnerName(newOuterName.getName());
//                    newInnerNames.put(innerName.getName(), innerName);
//                    builder.connectInnerToOuter(innerName, newOuterName);
                }
            }
        }

        //jetzt nochmal die parameter bauen


        //TODO: identity graph: depends if parameters are necessary
        // if not, (?) Make outernames substitution - means that corresponding node must be fetched also
        // loop through all nodes

        // outer names in d must not included in outernames of R


        //TODO falls ein subnode im redex+parameter ein link mit einem node im context hat muss dieser
        // erhalten bleiben. Oder vorher schauen ob der link mit einem node im redex ist, ansonsten kann man diesen schließen
//        builder.closeAllInnerNames();

        PureBigraph context = new PureBigraph(builder.new InstanceParameter(
                builder.getLoadedEPackage(),
                agentAdapter.getSignature(),
                Collections.singletonMap(0, newRoot),
                newSites,
                newNodes,
                newInnerNames, newOuterNames, builder.getCreatedEdges()));
        builder.reset();

        try {
            String convert = GraphvizConverter.toPNG(context,
                    true,
                    new File("src/test/resources/graphviz/context.png")
            );
            System.out.println(convert);
            GraphvizConverter.toPNG(agentAdapter,
                    true,
                    new File("src/test/resources/graphviz/agent.png")
            );
            GraphvizConverter.toPNG(redexAdapter,
                    true,
                    new File("src/test/resources/graphviz/redex.png")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        // parameters are only needed when RR contains sites, otherwise they contain just a barren
        //see e.g., \cite[p.75]{elsborg_bigraphs_2009}
        DefaultParametricMatch m = new DefaultParametricMatch(context, redexAdapter.getBigraph(),
                parameters.values(),
                null);
        matches.add((Match<B>) m);
    }

    private void setParentOfNode(final BigraphEntity node, final BigraphEntity parent) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        node.getInstance().eSet(prntRef, parent.getInstance()); // child is automatically added to the parent according to the ecore model
    }

    public List<Match<B>> getMatches() {
        return matches;
    }

    //TODO: move to linkgraph matching
    private boolean linkMatching() {
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
//        System.out.println(allChildrenFromNodeU.size());
//        System.out.println(allChildrenFromNodeV.size());
        boolean areLinksOK = areLinksOK(allChildrenFromNodeU, allChildrenFromNodeV);
//        System.out.println(areLinksOK);
        return areLinksOK;
    }

    //TODO: move to linkgraph matching
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
//        System.out.println(paritionAgent.size() * paritionRedex.size());
        return !lnk.containsValue(false);
    }

    //inner names gibt es nicht und müssen auch nicht betrachtet werden (thus, getNodesOfLink is enough)
    private boolean checkLinkIdentity(BigraphEntity v, BigraphEntity u) {
        List<BigraphEntity> bigraphEntities = S.get(v, u);//corresponding?
        if (bigraphEntities.size() != 0) {
            List<AbstractDynamicMatchAdapter.ControlLinkPair> lnkRedex = redexAdapter.getLinksOfNode(u);
            List<AbstractDynamicMatchAdapter.ControlLinkPair> lnkAgent = agentAdapter.getLinksOfNode(v);

            //Die Anzahl muss auch stimmen
            if (lnkRedex.size() == 0 && lnkAgent.size() == 0) return true;
            if (lnkRedex.size() == lnkAgent.size()) {
                for (int i = 0, n = lnkRedex.size(); i < n; i++) {
                    List<BigraphEntity> redexLinksOfEachU = redexAdapter.getNodesOfLink((BigraphEntity.OuterName) lnkRedex.get(i).getLink());
                    List<BigraphEntity> agentLinksOfEachV = agentAdapter.getNodesOfLink((BigraphEntity.OuterName) lnkAgent.get(i).getLink());
//                    boolean isDistinctLinkR = redexLinksOfEachU.size() == 1;
//                    boolean isDistinctLinkA = agentLinksOfEachV.size() == 1;
                    if (agentLinksOfEachV.size() < redexLinksOfEachU.size()) {
//                            System.out.println("\tControl " + u.getControl() + " kann NICHT gematcht werden");
                        return false;
                    }
                }
                return true;
            }
            return false;
        } else {
            return true;
        }
    }

    //TODO: move to linkgraph matching
    private boolean checkLinksForNode(BigraphEntity u, BigraphEntity v) {
        List<BigraphEntity> bigraphEntities = S.get(v, u);//corresponding?
        if (bigraphEntities.size() != 0) {
//            System.out.println(v.getControl() + " // " + u.getControl());
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
//                            System.out.println("\tControl " + u.getControl() + " kann gematcht werden");
                        } else {
//                            System.out.println("\tControl " + u.getControl() + " kann NICHT gematcht werden");
                            return false;
                        }
                    } else {
//                        System.out.println("\tControl " + u.getControl() + " kann gematcht werden");
                    }
                }
            }
//            }
//                    System.out.println(bigraphEntities.size());

        }
        return true;

    }

    //TODO: helper function
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

    //TODO: helper function
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
