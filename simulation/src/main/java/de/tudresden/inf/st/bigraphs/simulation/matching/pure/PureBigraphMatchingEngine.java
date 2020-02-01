package de.tudresden.inf.st.bigraphs.simulation.matching.pure;

import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ContextIsNotActive;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.simulation.matching.AbstractDynamicMatchAdapter;
import de.tudresden.inf.st.bigraphs.simulation.matching.BigraphMatchingEngine;
import de.tudresden.inf.st.bigraphs.simulation.util.Permutations;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.tudresden.inf.st.bigraphs.simulation.util.CombinationMaps.combinations;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Matching algorithm for pure bigraphs (see {@link PureBigraph}).
 *
 * @author Dominik Grzelak
 * @see HKMCBM2
 */
public class PureBigraphMatchingEngine implements BigraphMatchingEngine<PureBigraph> {

    private Logger logger = LoggerFactory.getLogger(PureBigraphMatchingEngine.class);

    private PureBigraphRedexAdapter redexAdapter;
    private PureBigraphAgentAdapter agentAdapter;
    private Table<BigraphEntity, BigraphEntity, List<BigraphEntity>> S = HashBasedTable.create();
    private AtomicInteger totalHits;
    private Table<BigraphEntity, BigraphEntity, Integer> results;

    private List<BigraphEntity> internalVertsG;
    private List<BigraphEntity> allVerticesOfH;
    private List<PureBigraphParametricMatch> matches = new LinkedList<>();
    // Agent -> redex root indices
    private HashMap<BigraphEntity, List<Integer>> hitsVIx = new HashMap<>();

    private MutableBuilder<DefaultDynamicSignature> builder;
    private MutableBuilder<DefaultDynamicSignature> builder2;

    BiMap<BigraphEntity, LinkedList<BigraphEntity>> crossingsA = HashBiMap.create();


    private Stopwatch matchingTimer;

    PureBigraphMatchingEngine(PureBigraph agent, PureBigraph redex) {
        //signature, ground agent
        this.redexAdapter = new PureBigraphRedexAdapter(redex);
        this.agentAdapter = new PureBigraphAgentAdapter(agent);
        Stopwatch timer = logger.isDebugEnabled() ? Stopwatch.createStarted() : null;
        this.init();
        if (logger.isDebugEnabled())
            logger.debug("Initialization time: {}", (timer.stop().elapsed(TimeUnit.NANOSECONDS) / 1e+6f));
    }

    @Override
    public List<PureBigraphParametricMatch> getMatches() {
        return matches;
    }

    private void init() {
        S = HashBasedTable.create();
        allVerticesOfH = new ArrayList<>(redexAdapter.getAllVertices());
        for (BigraphEntity gVert : agentAdapter.getAllVertices()) {
            for (BigraphEntity hVert : allVerticesOfH) {
                if (agentAdapter.degreeOf(gVert) <= 1 && !BigraphEntityType.isRoot(gVert) &&
                        redexAdapter.degreeOf(hVert) <= 1 && !BigraphEntityType.isRoot(hVert)) {
//                    leaves.add(each);
                    S.put(gVert, hVert, redexAdapter.getOpenNeighborhoodOfVertex(hVert));
                } else {
                    S.put(gVert, hVert, new ArrayList<>());
                }
            }
        }

        //below commented: because not necessary already set above
//        Iterable<BigraphEntity> leavesG = agentAdapter.getAllLeaves();
//        Iterable<BigraphEntity> leavesH = redexAdapter.getAllLeaves();
//        for (BigraphEntity gVert : leavesG) {
//            for (BigraphEntity hVert : leavesH) {
//                S.put(gVert, hVert, redexAdapter.getOpenNeighborhoodOfVertex(hVert));
//            }
//        }

        internalVertsG = agentAdapter.getAllInternalVerticesPostOrder();//TODO return as stream...
        totalHits = new AtomicInteger(0);
        results = HashBasedTable.create();
    }

    private void printName(BigraphEntity entity) {
        if (BigraphEntityType.isNode(entity)) {
            logger.debug("\tControl={} @ {}", entity.getControl(), ((BigraphEntity.NodeEntity) entity).getName());
        } else if (BigraphEntityType.isRoot(entity)) {
            logger.debug("\tRoot @ r_{}", ((BigraphEntity.RootEntity) entity).getIndex());
        }
    }


    /**
     * Computes all matches
     * <p>
     * First, structural matching, afterwards link matching
     */
    public void beginMatch() {
        if (logger.isDebugEnabled()) {
            matchingTimer = Stopwatch.createStarted();
        }

        List<List<BigraphEntity>> partitionSets = new ArrayList<>();
        for (BigraphEntity eachV : internalVertsG) {//TODO: create this in a stream-filter
            List<BigraphEntity> childrenOfV = agentAdapter.getChildren(eachV);
//            List<BigraphEntity> u_vertsOfH = new ArrayList<>(allVerticesOfH);//TODO: create this in a stream-filter
            MutableList<BigraphEntity> u_vertsOfH = org.eclipse.collections.api.factory.Lists.mutable.ofAll(allVerticesOfH);
            //d(u) <= t + 1
            int t = childrenOfV.size();
            for (int i = u_vertsOfH.size() - 1; i >= 0; i--) {
                BigraphEntity each = u_vertsOfH.get(i);
                if (redexAdapter.degreeOf(each) > t + 1)
                    u_vertsOfH.remove(each); //TODO getdegree in bigraph vorspeichern
            }

//            System.out.println("For eachV=" + eachV.toString());
//            System.out.println("V: ");
//            printName(eachV);
//            System.out.println("Children: ");
//            childrenOfV.forEach(this::printName);
            for (BigraphEntity eachU : u_vertsOfH) {
                boolean cs = true; //eachU.getControl().equals(eachV.getControl());
                if (eachU.getControl() != null && eachV.getControl() != null) {
                    cs = isSameControl(eachU, eachV);
                }
//                System.out.println("Q: " + cs);
                if (!cs) continue;

                List<BigraphEntity> neighborsOfU = redexAdapter.getOpenNeighborhoodOfVertex(eachU);
//                neighborsOfU = neighborsOfU.stream().filter(x -> !x.getType().equals(BigraphEntityType.SITE)).collect(Collectors.toList());
                Graph<BigraphEntity, DefaultEdge> bipartiteGraph = BigraphMatchingEngine.createBipartiteGraph(neighborsOfU, childrenOfV);

//                System.out.println("U: ");
//                printName(eachU);
//                System.out.println("neighborsOfU: ");
//                neighborsOfU.forEach(this::printName);

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
                partitionSets.clear();
                partitionSets.add(neighborsOfU);
                for (int i = 1, un = neighborsOfU.size(); i <= un; i++) {
                    List<BigraphEntity> tmp = new ArrayList<>(neighborsOfU);
                    tmp.remove(neighborsOfU.get(i - 1));
                    partitionSets.add(tmp);
                }

                //zwei sachen überprüfen:
                // 1) hat das aktuelle eachU eine Site als Children? -> dann durfen auch children vorhanden sein
                // 2) hat das aktuelle eachU eine Site als Sibling? -> dann dürfen auch mehr siblings eachV vorhanden sein
//                List<BigraphEntity> childrenWithSitesOfU = redexAdapter.getChildrenWithSites(eachU);
                boolean hasSite = false;
                if (redexAdapter.isBRoot(eachU.getInstance())) { //if the current element is a root then automatically a "site" is inferred
                    hasSite = true;
                } else {
//                    hasSite = redexAdapter.getChildrenWithSites(eachU).stream().anyMatch(BigraphEntityType::isSite);
                    for (BigraphEntity eachSibOfU : redexAdapter.getChildrenWithSites(eachU)) {
                        if (eachSibOfU.getType().equals(BigraphEntityType.SITE)) {
                            hasSite = true;
                            break;
                        }
                    }
                }

                // compute size of maximum matching of bipartite graph for all partitions
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
                        int m = matching.getEdges().size();


                        if (m == eachPartitionX.size()) { //eachPartitionX.size() != 0 && m != 0 &&
                            boolean m3 = alg.areControlsSame();
                            if (m3) {
//                                System.out.println(eachPartitionX.size() + ", " + m + ", " + hasSite);
//                                System.out.println("\tDARF SETZEN: " + ic + " und cs=" + cs);
                                if (ic == 0) {
                                    uSetAfterMatching.add(eachU);
                                } else {
                                    uSetAfterMatching.add(neighborsOfU.get(ic - 1));
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    ic++;
                }
                // Update map S
                S.put(eachV, eachU, uSetAfterMatching);
                if (S.get(eachV, eachU).contains(eachU)) {
                    if (!BigraphEntityType.isRoot(eachU)) {
                        continue;
                    }
                    int i = redexAdapter.getRoots().indexOf(eachU);

                    if (results.get(eachV, eachU) == null)
                        results.put(eachV, eachU, i);

                    boolean b = linkMatching();
                    if (b) {
                        int i1 = totalHits.incrementAndGet();
                        //save last eachU and eachV
                        // agent node is mapped to redex root index
                        hitsVIx.putIfAbsent(eachV, new LinkedList<>());
                        hitsVIx.get(eachV).add(i);
                        logger.debug("A matching was found for agent={} and redex={} with redex root index={}", eachV, eachU, i);
                    }
                }
            }
        }

        logger.debug("Number of hits (matches): {}", totalHits.get());
        logger.debug("Were all matches found?: {}", hasMatched());
    }

    public void createMatchResult() {
        if (logger.isDebugEnabled()) {
            long elapsed0 = matchingTimer.stop().elapsed(TimeUnit.NANOSECONDS);
            logger.debug("Matching took: {} ms", (elapsed0 / 1e+6f));
            matchingTimer.reset().start();
        }
        // This is needed when there is not a distinct match of the redices roots to a agent's subtree
        // Anzahl an occurrence-bedingter "mehr"-transitionen

        final int numOfRoots = redexAdapter.getRoots().size();
        final boolean redexRootMatchIsUnique = numOfRoots == totalHits.get();
        LinkedList<BigraphEntity> collect = new LinkedList<>(hitsVIx.keySet());
        //First, build all possible combinations of redex index to agent node matching
        List<Integer[]> combination = new LinkedList<>();

        if (redexRootMatchIsUnique) { //identity mapping: mapping is distinct and unique
            Integer[] comb = new Integer[numOfRoots];
            for (int i = 0; i < numOfRoots; i++) {
                comb[i] = i;
            }
            combination.add(comb);
        } else {
            // is a combinatorial problem
            // hier kann noch verbessert werden (ungueltige kombinationen werden auch aussortiert). der kombinationsraum
            // kann womöglich hier schon verkleinert werden
            List<Integer> nums2 = IntStream.range(0, numOfRoots).boxed().collect(toList());
            Permutations.of(nums2).forEach(p -> {
                combination.add(p.toArray(Integer[]::new));
            });
        }

        // generic loop for unique and non-unique matchings of the redex root to agent's node
        int validCounter = 0;
        // for every combination
        for (Integer[] eachCombination : combination) {
            // redex root to agent node
//            BiMap<Integer, BigraphEntity> hitsV_new = HashBiMap.create();
            MutableMap<Integer, BigraphEntity> hitsV_new = org.eclipse.collections.api.factory.Maps.mutable.empty();
            for (int i = 0; i < eachCombination.length; i++) {
                int tmpRootIx = eachCombination[i];
                BigraphEntity agentNode = collect.get(i);
                if (BigraphEntityType.isRoot(agentNode) && i + 1 < eachCombination.length) {
                    collect.add(i + 1, agentNode);
                }
                if (redexRootMatchIsUnique) {
                    //this will only executed when combination is of size 1
                    assert combination.size() == 1;
//                    BigraphEntity finalAgentNode = agentNode;
                    agentNode = hitsVIx.entrySet().stream().filter(e -> e.getValue().contains(tmpRootIx)).findFirst().get().getKey();
                } else {
                    if (!hitsVIx.get(agentNode).contains(tmpRootIx)) {
                        break;
                    }
                }
                hitsV_new.put(tmpRootIx, agentNode);
            }

            if (hitsV_new.size() != numOfRoots) {
                logger.debug("This matching is not valid: {}", Arrays.toString(eachCombination));
                continue;
            } else {
                logger.debug("This matching is valid: {}", Arrays.toString(eachCombination));
                validCounter++;
            }

            //Parameter: <redex root index, agent node, redex node>
            final List<Table<Integer, BigraphEntity, BigraphEntity>> hitsV_newChildrenStack2 = new LinkedList<>();
            List<MatchPairing> mapPairings = new LinkedList<>();

            // The structure of the Map.Entry: redex root index -> matched agent node
            for (Map.Entry<Integer, BigraphEntity> eachEntry : hitsV_new.entrySet()) {
                int redexRootIx = eachEntry.getKey();
                BigraphEntity agentMatch = eachEntry.getValue();
                List<BigraphEntity> childrenOfAgent = agentAdapter.getChildren(agentMatch);
                List<BigraphEntity> childrenOfRedex = redexAdapter.getChildren(redexAdapter.getRoots().get(redexRootIx));
                Map<BigraphEntity, LinkedList<BigraphEntity>> nodeDiffByControlCheck = findOccurrences(childrenOfAgent, childrenOfRedex, true);
                // Check that not one entry is empty, otherwise return and no match could be found
                boolean incompleteMatch = nodeDiffByControlCheck.values().stream().filter(x -> x.size() == 0).anyMatch(Objects::nonNull);
                if (incompleteMatch) return;
//                List<BigraphEntity> availableAgentMatchNodes = new LinkedList<>();

                //current impl: select the first possible agent node
                for (Map.Entry<BigraphEntity, LinkedList<BigraphEntity>> eachMapping : nodeDiffByControlCheck.entrySet()) {
                    //the agent-redex match is here already "good" (w/o considering the children yet - this is done later)
                    MatchPairing q = new MatchPairing(redexRootIx, eachMapping.getKey());
                    for (BigraphEntity x : eachMapping.getValue()) {
//                        if (!availableAgentMatchNodes.contains(x)) {
                        q.getAgentMatches().add(x);
//                        availableAgentMatchNodes.add(x);
//                        break;
//                        }
                    }
                    mapPairings.add(q);
                }
            }

            logger.debug("mapPairings={}", mapPairings);

            List<Map<MatchPairing, BigraphEntity>> combinations = new LinkedList<>();
            HashMap<MatchPairing, List<BigraphEntity>> collect4 = mapPairings.stream()
                    .sorted(comparingInt(e -> e.getAgentMatches().size()))
                    .collect(toMap(f -> f,
                            x -> x.getAgentMatches(),
                            (o1, o2) -> {
                                o1.addAll(o2);
                                return o1;
                            },
                            HashMap::new));
            combinations(collect4, combinations);
            for (int i = 0; i < combinations.size(); i++) {
//                hitsV_newChildrenStack2.add()
                final Table<Integer, BigraphEntity, BigraphEntity> matchTable = HashBasedTable.create();
                Map<MatchPairing, BigraphEntity> matchPairingBigraphEntityMap = combinations.get(i);
                matchPairingBigraphEntityMap.entrySet().stream().forEach(x -> {
                    matchTable.put(x.getKey().getRootIndex(), x.getValue(), x.getKey().getRedexNode());
                });
                hitsV_newChildrenStack2.add(matchTable);
            }

//            System.out.println("----------");
//            logger.debug("hitsV_newChildrenStack2={}", hitsV_newChildrenStack2);
//            System.out.println("Size,hitsV_newChildrenStack2=" + hitsV_newChildrenStack2.size());
//            logger.debug("hitsV_new={}", hitsV_new);
//            System.out.println("----------");
//            return;
            for (Table<Integer, BigraphEntity, BigraphEntity> each : hitsV_newChildrenStack2) {
//                //TODO: make the following faster: this step tooks most of the time
                try {
//                    //structure of each: {redex root index -> {agentNode -> redexNode}} (are unique mappings here)
                    logger.debug("Final redex<->agent node matching: {}", each);
                    PureBigraphParametricMatch m = buildMatch(hitsV_new, each);
                    matches.add(m);
                } catch (ContextIsNotActive contextIsNotActive) {
                    contextIsNotActive.printStackTrace();
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Number of valid match combinations: {}", validCounter);
            long elapsed0 = matchingTimer.stop().elapsed(TimeUnit.NANOSECONDS);
            logger.debug("Time to build the match result: {} ms", (elapsed0 / 1e+6f));
        }
    }

    private List<Table<Integer, BigraphEntity, BigraphEntity>> recurs(int ix, HashMap<BigraphEntity, List<BigraphEntity>> collect1) {
        List<Map<BigraphEntity, BigraphEntity>> combinations = new LinkedList<>();
        combinations(collect1, combinations);
        List<Table<Integer, BigraphEntity, BigraphEntity>> result = new LinkedList<>();
//        for (int ix : combi) {
        combinations.stream().forEachOrdered(x -> {
            final Table<Integer, BigraphEntity, BigraphEntity> matchTable = HashBasedTable.create();
            x.entrySet().stream().forEachOrdered(y -> {
                matchTable.put(ix, y.getValue(), y.getKey());
            });
            result.add(matchTable);
        });
        return result;
    }

    /**
     * Checks if any match could be found and also if _all_ redex roots could be matched.
     *
     * @return {@code true}, if a correct match could be found, otherwise {@code false}
     */
    public boolean hasMatched() {
        return totalHits.get() > 0 && totalHits.get() >= redexAdapter.getRoots().size() && (totalHits.get() % redexAdapter.getRoots().size() == 0);
//        return totalHits.get() > 0 && (totalHits.get() % redexAdapter.getRoots().size() == 0);
    }

    /**
     * Constructs the context and the parameters of the redex to build the "redex image".
     *
     * @param hitsV_newChildren redex root ix, agent node, redex node
     */
    private PureBigraphParametricMatch buildMatch(Map<Integer, BigraphEntity> hitsV, Table<Integer, BigraphEntity, BigraphEntity> hitsV_newChildren) throws ContextIsNotActive {
//        if (Objects.isNull(this.builder)) {
        this.builder = PureBigraphBuilder.newMutableBuilder(agentAdapter.getSignature());
        this.builder2 = PureBigraphBuilder.newMutableBuilder(agentAdapter.getSignature());
//        }
        logger.debug("BUILD MATCH");

        // Compute the context, and the parameters
        Map<Integer, Bigraph<DefaultDynamicSignature>> parameters = new LinkedHashMap<>();
        PureBigraphFactory pureBigraphFactory = AbstractBigraphFactory.createPureBigraphFactory();

        //when no params are necessary then a barren is all that is needed
        boolean needsParameters = redexAdapter.getSites().size() >= 1;
        if (needsParameters) {
            redexAdapter.getSites().forEach(x -> parameters.put(x.getIndex(), pureBigraphFactory.createPlacings(redexAdapter.getSignature()).barren()));
        }

        // agent->redex
        BiMap<BigraphEntity.NodeEntity, BigraphEntity.NodeEntity> matchDict = HashBiMap.create();
//        hitsV_newChildren.columnKeySet().forEach(x -> matchDict.put((BigraphEntity.NodeEntity) x, null));

        // context: replace the agent with sites at eachV and if edge exists, make inner name

        // First: create roots: should only have one root!
        final BigraphEntity.RootEntity newRootCtx = (BigraphEntity.RootEntity) builder.createNewRoot(0); //first.get().getIndex());
        final Map<Integer, BigraphEntity.SiteEntity> newSites = new HashMap<>();
        final Map<String, BigraphEntity.NodeEntity> newNodes = new HashMap<>();
        final Map<String, BigraphEntity.OuterName> newOuterNames = new HashMap<>();
        final Map<String, BigraphEntity.InnerName> newInnerNames = new HashMap<>();


        final Map<String, BigraphEntity.OuterName> newOuterNamesL = new HashMap<>(); // für link graph
        final Map<String, BigraphEntity.InnerName> newInnerNamesL = new HashMap<>(); // für place graph

        // recreate nodes that are in V exclusive U
        // where first u node is found replace with site
        // this should build the node hierarchy and put places under those nodes where the redex matched

        Set<BigraphEntity> blockNodesForContext = new HashSet<>();

        // Recreate idle outer names in the agent for the context: the redex don't need to be checked, as it
        // cannot have idle outer names. It is "simple" (this constraint is checked when creating a RR)
        if (!agentAdapter.isEpimorphic()) {
            agentAdapter.getOuterNames().stream().filter(x -> agentAdapter.getPointsFromLink(x).size() == 0).forEach(link -> {
                BigraphEntity.OuterName newOuterName = builder.createOuterName(link.getName());
                newOuterNames.put(newOuterName.getName(), newOuterName);
            });
        }

        for (BigraphEntity eachNodeV : agentAdapter.getAllVerticesBfsOrder()) {

            // skip blocked children first (children that are in the redex match
            // this part will be executed after the top-level nodes of the redex were matched with the corresponding agent nodes
            if (blockNodesForContext.contains(eachNodeV)) {
                //put all their children inside
                blockNodesForContext.addAll(agentAdapter.getChildrenOf(eachNodeV));
                continue;
            }

            final BigraphEntity newNode;
            BigraphEntity newParent = agentAdapter.getParent(eachNodeV);
            if (newParent == null || BigraphEntityType.isRoot(newParent)) {
                newParent = newRootCtx;
            } else { // else find or create a new parent
                //find the parent or create a new one first
                final String theParentName = ((BigraphEntity.NodeEntity) newParent).getName();
                final BigraphEntity _newParent0 = agentAdapter.getParent(eachNodeV);
                newParent = newNodes.values().stream()
                        .filter(x -> x.getName().equals(theParentName) &&
                                x.getControl().equals((_newParent0.getControl())))
                        .findFirst()
                        .orElse((BigraphEntity.NodeEntity) builder.createNewNode(newParent.getControl(), theParentName));
                newNodes.put(theParentName,
                        (BigraphEntity.NodeEntity) newParent);
            }

            if (!BigraphEntityType.isRoot(eachNodeV)) {
                final BigraphEntity.NodeEntity eachNodeV0 = (BigraphEntity.NodeEntity) eachNodeV;
                if (newNodes.containsKey(eachNodeV0.getName())) {
                    newNode = newNodes.get(eachNodeV0.getName());
                } else {
                    newNode = builder.createNewNode(eachNodeV0.getControl(), eachNodeV0.getName());
                }

                setParentOfNode(newNode, newParent);
                newNodes.put(((BigraphEntity.NodeEntity) newNode).getName(),
                        (BigraphEntity.NodeEntity) newNode);
            } else {
                newNode = newRootCtx;
            }

            // abstract the rest away and put children into blocked list
            boolean hasAmatch = hitsV.values().contains(eachNodeV);
            if (hasAmatch) { // && agentAdapter.getChildrenOf(eachNodeV).size() >= 1) {
                Integer rootIx = hitsV.entrySet().stream().filter(x -> x.getValue().equals(eachNodeV)).findFirst().get().getKey();

                List<BigraphEntity> savedRedexNodes = new ArrayList<>(); //all agent nodes that shouldn't be considered anymore


                Collection<BigraphEntity> childrenOfCurrentAgentParent = agentAdapter.getChildrenOf(eachNodeV);
                // in the end: childrenOfCurrentAgentParent will only contain agent nodes which couldn't be match
                Iterator<BigraphEntity> iterator = childrenOfCurrentAgentParent.iterator();
//                for (BigraphEntity agentNext : childrenOfCurrentAgentParent) {
                for (BigraphEntity agentNext : hitsV_newChildren.columnMap().keySet()) {
//                    if (hitsV_newChildren.containsColumn(agentNext)) {
                    Map.Entry<Integer, BigraphEntity> integerBigraphEntityEntry = hitsV_newChildren.column(agentNext)
                            .entrySet()
                            .stream().findFirst().get();
                    Integer rootIx0 = integerBigraphEntityEntry.getKey();
                    BigraphEntity redexEntityCorrespondence = hitsV_newChildren.get(rootIx0, agentNext);
                    try {
                        //TODO put?
                        matchDict.forcePut((BigraphEntity.NodeEntity) agentNext, (BigraphEntity.NodeEntity) redexEntityCorrespondence);
                    } catch (java.lang.IllegalArgumentException e) {
                        e.printStackTrace();
                    }
//                        redexEntityCorrespondence "is equal to" agentNext
//                    iterateThroughChildren(redexEntityCorrespondence, agentNext, parameters, matchDict, hitsV_newChildren);
                    savedRedexNodes.add(agentNext);
                }
                logger.debug("iterateThroughChildren: start");
                logger.debug("hitsV_newChildren.columnMap().keySet()={}", hitsV_newChildren.columnMap().keySet());
                // two-times iteration: because we need the matchDict complete for the current level
                for (BigraphEntity agentNext : hitsV_newChildren.columnMap().keySet()) {
                    Map.Entry<Integer, BigraphEntity> integerBigraphEntityEntry = hitsV_newChildren.column(agentNext)
                            .entrySet()
                            .stream().findFirst().get();
                    Integer rootIx0 = integerBigraphEntityEntry.getKey();
                    BigraphEntity redexEntityCorrespondence = hitsV_newChildren.get(rootIx0, agentNext);
                    logger.debug("redexEntityCorrespondence={}", redexEntityCorrespondence);
                    iterateThroughChildren(redexEntityCorrespondence, agentNext, parameters, matchDict, hitsV_newChildren);
                }
                logger.debug("iterateThroughChildren: finished");
                // childrenOfCurrentAgentParent: contains only contain agent nodes which couldn't be match (i.e., which shall be preserved)
                while (iterator.hasNext()) {
                    if (savedRedexNodes.contains(iterator.next())) {
                        iterator.remove();
                    }
                }
                logger.debug("while iterator finished");

                //rest of the available nodes remains and the savedRedexNodes goes into the blocking list
                //and all their children
                savedRedexNodes.forEach(x -> {
                    blockNodesForContext.add(x);
                    blockNodesForContext.addAll(agentAdapter.getChildrenOf(x)); //these are the parameters for the current root index..
                });
                logger.debug("BlockedNodes: {}", savedRedexNodes);


                // now we are left only with the remaining children precluding the redex match at the current level (height of the tree)
                for (BigraphEntity eachRemaining : childrenOfCurrentAgentParent) {
                    BigraphEntity.NodeEntity eachRemaining0 = (BigraphEntity.NodeEntity) eachRemaining;
                    BigraphEntity.NodeEntity newNode1 = (BigraphEntity.NodeEntity) builder.createNewNode(eachRemaining0.getControl(), eachRemaining0.getName());
                    newNodes.put(newNode1.getName(), newNode1);
                    setParentOfNode(newNode1, newNode);
                }

                // create site procedure
                boolean agentNodeIsRoot = BigraphEntityType.isRoot(eachNodeV);
                if (agentNodeIsRoot && redexAdapter.getRoots().size() > 1) {
                    IntStream.range(0, redexAdapter.getRoots().size()).boxed()
                            .forEachOrdered(rix -> {
                                BigraphEntity.SiteEntity newSite = (BigraphEntity.SiteEntity) builder.createNewSite(rix);
                                newSites.put(rix, newSite);
                                setParentOfNode(newSite, newNode);
                            });
                } else {
                    //get the root index of the corresponding redex match node eachU
                    BigraphEntity.SiteEntity newSite = (BigraphEntity.SiteEntity) builder.createNewSite(rootIx);
                    newSites.put(newSite.getIndex(), newSite);
                    setParentOfNode(newSite, newNode);
                }
            }

            List<AbstractDynamicMatchAdapter.ControlLinkPair> linksOfNode = agentAdapter.getLinksOfNode(eachNodeV);
            for (AbstractDynamicMatchAdapter.ControlLinkPair eachPair : linksOfNode) {
                String linkName;
                if (BigraphEntityType.isEdge(eachPair.getLink())) {
                    linkName = ((BigraphEntity.Edge) eachPair.getLink()).getName();
                    BigraphEntity.InnerName innerName = newInnerNames.entrySet().stream()
                            .filter(e -> e.getValue().getName().equals(linkName))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(builder.createInnerName(linkName + "_innername"));
                    newInnerNames.put(innerName.getName(), innerName);
                    try {
                        builder.connectNodeToInnerName((BigraphEntity.NodeEntity) newNode, innerName);
                    } catch (LinkTypeNotExistsException | InvalidConnectionException e) {
                        e.printStackTrace();
                    }
                } else if (BigraphEntityType.isOuterName(eachPair.getLink())) {
                    final BigraphEntity.OuterName link = (BigraphEntity.OuterName) eachPair.getLink();
                    BigraphEntity.OuterName newOuterName = newOuterNames.values().stream()
                            .filter(outerName -> outerName.getName().equals(link.getName()))
                            .findFirst()
                            .orElse(builder.createOuterName(link.getName()));
                    newOuterNames.put(newOuterName.getName(), newOuterName);
                    builder.connectNodeToOuterName((BigraphEntity.NodeEntity) newNode, newOuterName);
                }
            }
        }

        // outer names in d must not included in outernames of R: die names kommen direkt aus dem agent und dürfen gleichen namen haben

        PureBigraph context = new PureBigraph(builder.new InstanceParameter(
                builder.getLoadedEPackage(),
                agentAdapter.getSignature(),
                Collections.singletonMap(0, newRootCtx), // roots
                newSites,
                newNodes,
                newInnerNames, newOuterNames, builder.getCreatedEdges()));
        builder.reset();

        Bigraph<DefaultDynamicSignature> identityForParams;
        Linkings<DefaultDynamicSignature> linkings = pureBigraphFactory.createLinkings(agentAdapter.getSignature());
        List<StringTypedName> namesTmp = (List<StringTypedName>) parameters.values().stream().map(x -> x.getOuterNames())
                .flatMap(x -> x.stream())
                .map(x -> StringTypedName.of(((BigraphEntity.OuterName) x).getName()))
                .collect(toList());
        if (namesTmp.size() == 0 || parameters.size() == 0) {
            identityForParams = linkings.identity_e();
        } else {
            identityForParams = linkings.identity(namesTmp.toArray(new StringTypedName[0]));
        }

        //TODO: add to identityForContext: the artificial innernames to be closed.

        // Build the identity link graph for the context and the redex
        // to build the identity graph later for the redex
        HashMap<String, List<String>> substitutionLinkingGraph = new HashMap<>();
        Bigraph<DefaultDynamicSignature> identityForContext = linkings.identity_e();
        try {
            List<String> alreadyUsedNamesOfNode = new ArrayList<>();
            for (BigraphEntity.OuterName eachAgentOuterName : agentAdapter.getOuterNames()) {
                Collection<BigraphEntity.NodeEntity> pointsFromLink = agentAdapter.getPointsFromLink(eachAgentOuterName).stream()
                        .filter(BigraphEntityType::isPort).map(x -> agentAdapter.getNodeOfPort((BigraphEntity.Port) x)).collect(toList());
                substitutionLinkingGraph.putIfAbsent(eachAgentOuterName.getName(), new ArrayList<>());
                for (BigraphEntity.NodeEntity each : pointsFromLink) {
                    BigraphEntity.NodeEntity matchedRedex = matchDict.get(each);
                    if (Objects.isNull(matchedRedex)) continue;
                    LinkedList<AbstractDynamicMatchAdapter.ControlLinkPair> linksOfNode = redexAdapter.getLinksOfNode(matchedRedex);
                    List<String> names = linksOfNode.stream().map(x -> ((BigraphEntity.OuterName) x.getLink()).getName()).collect(toList());
                    for (String eachName : names) {
                        if (!alreadyUsedNamesOfNode.contains(eachName)) {
                            substitutionLinkingGraph.get(eachAgentOuterName.getName()).add(eachName);
                            alreadyUsedNamesOfNode.add(eachName);
                        }
                    }
                }
            }

            for (Map.Entry<String, List<String>> each : substitutionLinkingGraph.entrySet()) {
                List<StringTypedName> tmp = each.getValue().stream().map(StringTypedName::of).collect(toList());

//                if (tmp.size() == 0) {
                tmp.add(StringTypedName.of(each.getKey()));
//                }
                Linkings<DefaultDynamicSignature>.Substitution tmpSub = linkings.substitution(StringTypedName.of(each.getKey()), tmp.toArray(new StringTypedName[0]));
                identityForContext = pureBigraphFactory.asBigraphOperator(identityForContext).parallelProduct(tmpSub).getOuterBigraph();
            }

            // bigraph to prepare closing the inner names
//            List<StringTypedName> closeInnerNames = context.getInnerNames().stream().map(x -> StringTypedName.of(x.getName())).collect(Collectors.toList());
//            PureBigraphBuilder<DefaultDynamicSignature> bigraphBuilder = pureBigraphFactory.createBigraphBuilder(agentAdapter.getSignature());
//            bigraphBuilder.createOuterName(closeInnerNames.get(0).getValue());
//            if (closeInnerNames.size() >= 2) {
//                for (int i = 1; i < closeInnerNames.size(); i++) {
//                    bigraphBuilder.createOuterName(closeInnerNames.get(i).getValue());
//                }
//            }
//            identityForContext = pureBigraphFactory.asBigraphOperator(identityForContext)
//                    .parallelProduct(bigraphBuilder.createBigraph())
//                    .getOuterBigraph();
        } catch (Exception e) {
//            e.printStackTrace();
            throw new RuntimeException(e);
        }

        List<Integer> collect = context.getSites().stream().collect(Collectors.toMap(BigraphEntity.SiteEntity::getIndex, s -> context.isActiveAtSite(s.getIndex())))
                .entrySet().stream().filter(k -> !k.getValue()).map(k -> k.getKey()).collect(toList());
        if (collect.size() != 0) {
            throw new ContextIsNotActive(collect.stream().mapToInt(i -> i).toArray());

        }

        PureBigraph redexImage = null;
        try {
            redexImage = pureBigraphFactory.asBigraphOperator(redexAdapter.getBigraphDelegate()).parallelProduct(identityForParams).getOuterBigraph();
        } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
            logger.error(e.getMessage(), e);
        }
        // parameters are only needed when RR contains sites, otherwise they contain just a barren or are empty
        return new PureBigraphParametricMatch(
                context,
                redexAdapter.getBigraphDelegate(),
                redexImage,
                parameters.values(),
                identityForParams,
                identityForContext
        );
    }

    private void createNamesForNodeInParam(BigraphEntity.NodeEntity newNode,
                                           BigraphEntity agentNode,
                                           Map<String, BigraphEntity.OuterName> outerNames) {
        LinkedList<AbstractDynamicMatchAdapter.ControlLinkPair> linksOfNode = agentAdapter.getLinksOfNode(agentNode);
        for (AbstractDynamicMatchAdapter.ControlLinkPair each : linksOfNode) {
            if (BigraphEntityType.isOuterName(each.getLink())) {
                BigraphEntity.OuterName newOuterName = builder2.createOuterName(((BigraphEntity.OuterName) each.getLink()).getName());
                builder2.connectNodeToOuterName(newNode, newOuterName);
                outerNames.put(newOuterName.getName(), newOuterName);
            } else if (BigraphEntityType.isEdge(each.getLink())) {
                BigraphEntity.OuterName newOuterName = builder2.createOuterName(((BigraphEntity.Edge) each.getLink()).getName() + "_innername");//TODO hier mit e_inner machen
                builder2.connectNodeToOuterName(newNode, newOuterName);
                outerNames.put(newOuterName.getName(), newOuterName);
            }
        }
    }

    // corresponding parts
    private void iterateThroughChildren(BigraphEntity redex, BigraphEntity agent,
                                        Map<Integer, Bigraph<DefaultDynamicSignature>> parameters,
                                        BiMap<BigraphEntity.NodeEntity, BigraphEntity.NodeEntity> matchDict,
                                        Table<Integer, BigraphEntity, BigraphEntity> hitsV_newChildren) {
        boolean hasSite = redexAdapter.getChildrenWithSites(redex).stream().anyMatch(BigraphEntityType::isSite);
        List<BigraphEntity> redexChildren = redexAdapter.getChildren(redex);

        Map<BigraphEntity, BigraphEntity> distinctMatch = new ConcurrentHashMap<>();
        List<BigraphEntity> agentChildren = agentAdapter.getChildren(agent);
        Map<BigraphEntity, LinkedList<BigraphEntity>> occurrences = findOccurrences(agentChildren, redexChildren, hasSite);
        for (Map.Entry<BigraphEntity, LinkedList<BigraphEntity>> redexAgentMapping : occurrences.entrySet()) {
            // 1. Normally, get any value from the value list: here we just take the first one (order of the list)
            // 2. However, check if one of the child has a link to another redex match. If so, find out the correspondence

            // take special care of "cross-linking" nodes
            List<BigraphEntity> discardAgents = findCrossLinkingNodes(redexAgentMapping, matchDict);

            for (BigraphEntity correspondence : redexAgentMapping.getValue()) {
                if (agentChildren.contains(correspondence) && // is still available
                        !discardAgents.contains(correspondence) && // should be ignored
                        distinctMatch.get(redexAgentMapping.getKey()) == null) { // no match found yet for this redex node
                    agentChildren.remove(correspondence);
                    distinctMatch.put(redexAgentMapping.getKey(), correspondence);
                    try {
                        //TODO put?
                        matchDict.forcePut((BigraphEntity.NodeEntity) correspondence, (BigraphEntity.NodeEntity) redexAgentMapping.getKey());
                    } catch (java.lang.IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (hasSite) {
            // Alles in dieser ebene als parameter wegpacken
            // Jeder ast wird dann noch einmal durchlaufen: mit den correspondences

            BigraphEntity.SiteEntity entity = (BigraphEntity.SiteEntity) redexAdapter.getChildrenWithSites(redex).stream()
                    .filter(BigraphEntityType::isSite).findFirst().get();
            int siteIndex = entity.getIndex();

            BigraphEntity.RootEntity rootParam = (BigraphEntity.RootEntity) builder2.createNewRoot(siteIndex);
            Map<String, BigraphEntity.NodeEntity> paramNodes = new LinkedHashMap<>();
            Map<String, BigraphEntity.OuterName> paramOuterNames = new LinkedHashMap<>();
            Traverser<BigraphEntity> paramTraverser = Traverser.forTree(xx -> {
                BigraphEntity.NodeEntity np;
                if ((np = paramNodes.get(((BigraphEntity.NodeEntity) xx).getName())) == null) {
                    np = (BigraphEntity.NodeEntity) builder2
                            .createNewNode(xx.getControl(), ((BigraphEntity.NodeEntity) xx).getName());
                    setParentOfNode(np, rootParam);
                    paramNodes.put(np.getName(), np);
                    createNamesForNodeInParam(np, xx, paramOuterNames);
                }
                List<BigraphEntity> children = agentAdapter.getChildren(xx);
                for (BigraphEntity entity2 : children) {
                    if (paramNodes.get(((BigraphEntity.NodeEntity) entity2).getName()) == null) {
                        BigraphEntity.NodeEntity newNode1 = (BigraphEntity.NodeEntity) builder2
                                .createNewNode(entity2.getControl(), ((BigraphEntity.NodeEntity) entity2).getName());
                        setParentOfNode(newNode1, np);
                        createNamesForNodeInParam(newNode1, entity2, paramOuterNames);
                        paramNodes.put(newNode1.getName(), newNode1);
                    }
                }
                return children;
            });
            Lists.newArrayList(paramTraverser.breadthFirst(agentChildren)); // fills the paramNodes map
            PureBigraph paramBigraph = new PureBigraph(
                    builder2.new InstanceParameter(
                            builder2.getLoadedEPackage(),
                            agentAdapter.getSignature(),
                            Collections.singletonMap(0, rootParam),
                            Collections.emptyMap(),
                            paramNodes,
                            Collections.emptyMap(),
                            paramOuterNames,
                            builder2.getCreatedEdges()
                    )
            );
            builder2.reset();
            parameters.put(siteIndex, paramBigraph);
        }

        // wenn keine site da ist, dann die anderen treffer durchgehen, bis alles fertig ist
        // dann gibt es keine parameter
        for (Map.Entry<BigraphEntity, BigraphEntity> eachEntry : distinctMatch.entrySet()) {
            iterateThroughChildren(eachEntry.getKey(), eachEntry.getValue(), parameters, matchDict, hitsV_newChildren);
        }
    }

    // take special care of cross linking nodes
    private List<BigraphEntity> findCrossLinkingNodes(Map.Entry<BigraphEntity, LinkedList<BigraphEntity>> redexAgentMapping,
                                                      BiMap<BigraphEntity.NodeEntity, BigraphEntity.NodeEntity> matchDict) {
        List<BigraphEntity> removeEntities = new LinkedList<>();
        for (BigraphEntity matchingAgent : redexAgentMapping.getValue()) {
            for (Map.Entry<BigraphEntity, LinkedList<BigraphEntity>> cross : crossingsA.entrySet()) {
                if (cross.getValue().contains(matchingAgent)) { // this is only valid for cross-linking nodes
                    BigraphEntity.NodeEntity correspondingAgent = matchDict.inverse().get(cross.getKey());
                    if (Objects.nonNull(correspondingAgent) && !agentAdapter.areConnected(correspondingAgent, (BigraphEntity.NodeEntity) matchingAgent)) {
                        logger.debug("not null");
                        removeEntities.add(matchingAgent);
                    }
                }
            }
        }
        return removeEntities;
    }

    /**
     * TODO here is space for improvement, maybe the S table can be used here.
     * <p>
     * After a match is found, we need to compute the occurrences of the redex inside the agent matched part.
     * <p>
     * It may happen that not only a distinct match is found but multiple ones.
     * (We then need to use all these when a rewrite is done!)
     * <p>
     * Beginning from a redex node, the whole subtree + links are checked to find possible matches.
     * The search space is here "dramatically" reduced since we only have to look into the provided agent nodes.
     * <p>
     * The method only returns the "first level" nodes of the matches and not the whole subtree.
     *
     * @param agentNodes a collection of agent nodes
     * @param redexNodes a collection of redex nodes
     * @return a map where the key is the redex and the value is a list of possible agent matches
     */
    public Map<BigraphEntity, LinkedList<BigraphEntity>> findOccurrences(Collection<BigraphEntity> agentNodes, Collection<BigraphEntity> redexNodes, boolean withSitesNoExactMatch) {
        Map<BigraphEntity, LinkedList<BigraphEntity>> mapping = new HashMap<>();
        List<BigraphEntity> agents = new LinkedList<>(agentNodes);
        //Liste durchgehen von redexNodes
        //in 2. schleifen die agent nodes die noch übrig sind
        //prüfen: control, degree (eins abziehen wegen root, wenn site in children vorhanden dann darf mehr vorhanden sein, ansonsten gleich)
        for (BigraphEntity eachRedex : redexNodes) { //degree checking is done in hasSameSpatialStructure() method
            logger.debug("Checking Redex: {}", ((BigraphEntity.NodeEntity<Control>) eachRedex).getName());
            for (int i = agents.size() - 1; i >= 0; i--) {
                BigraphEntity eachAgent = agents.get(i);
//                boolean b = hasSameSpatialStructure(eachAgent, eachRedex, withSitesNoExactMatch) && isSameControl(eachAgent, eachRedex);
                boolean hasSite = redexAdapter.getChildrenWithSites(eachRedex).stream().anyMatch(BigraphEntityType::isSite);
                boolean b = isSameControl(eachAgent, eachRedex) && hasSameSpatialStructure3(eachAgent, eachRedex, hasSite); //hasSameSpatialStructure(eachAgent, eachRedex, true);
//                boolean bigraphEntities1 = checkLinkIdentityOfNodes(eachAgent, eachRedex);
                boolean big1 = checkLinksForNode(eachRedex, eachAgent); // this is a very basic/simply link checking, later we do something more concretely
                //this is just to pre-filter
                if (b) {
                    if (big1) {
                        logger.debug("\tChecking against agent: {} -> keep", ((BigraphEntity.NodeEntity<Control>) eachAgent).getName());
                        mapping.putIfAbsent(eachRedex, new LinkedList<>());
                        mapping.get(eachRedex).add(eachAgent);
                    } else {
                        logger.debug("\tChecking against agent: {} -> discard", ((BigraphEntity.NodeEntity<Control>) eachAgent).getName());
                    }
                }
            }
        }
        // reduce space here further: check if one redex has only one match: remove this from all the other with multiple ones (if it occurs in it as well)
        BiMap<BigraphEntity, List<CrossPairLink>> crossings2 = HashBiMap.create();
        if (redexNodes.size() > 1) {
            for (Map.Entry<BigraphEntity, LinkedList<BigraphEntity>> each : mapping.entrySet()) {
                // only for those redexes which have multiple possible  matchings
                if (each.getValue().size() <= 1) continue;
                BigraphEntity currentRedex = each.getKey();
                List<BigraphEntity> rest = mapping.entrySet().stream().filter(x -> !x.getKey().equals(each.getKey()))
                        .map(Map.Entry::getKey)
                        .collect(toList());

                // First get all linked nodes under the current redex (a subbigraph)
                // For that we get _all_ children of the current redex first, and then gather all their links.
                // they are somehow "grouped" through this 2D list structure
                List<List<BigraphEntity>> redexLinks = getSubBigraphFrom(currentRedex, redexAdapter)
                        .stream().flatMap(x -> redexAdapter.getLinksOfNode(x).stream())
                        .map(x -> redexAdapter.getNodesOfLink((BigraphEntity.Link) x.getLink()))
                        .collect(toList());

                // Ist alles was ich unter der Liste finde, der parent "currentRedex"?
                for (List<BigraphEntity> nodes : redexLinks) {
                    List<Map.Entry<? extends BigraphEntity, Boolean>> result = nodes.stream()
                            .collect(Collectors.toMap(p -> p, p -> redexAdapter.isParentOf(p, currentRedex)))
                            .entrySet().stream()
                            .filter(x -> x.getValue() == false)
                            .collect(toList());
                    if (result.size() == 0) continue;
                    // for "false": finde heraus welcher redex parent node
                    List<CrossPairLink> pairLinks = new ArrayList<>();
                    // only nodes that are not connected are now in "result"
                    // find out the corresponding parent
                    rest.forEach(x -> result.forEach(y -> {
                        if (redexAdapter.isParentOf(y.getKey(), x)) {
                            pairLinks.add(CrossPairLink.create(x, y.getKey()));
                        }
                    }));
                    for (CrossPairLink eachCross : pairLinks) {
                        crossings2.putIfAbsent(currentRedex, new ArrayList<>());
                        crossings2.get(currentRedex).add(eachCross);
                    }
                }
            }

            //key is the redex node, the value(attr: other): both are connected via a link
            for (Map.Entry<BigraphEntity, List<CrossPairLink>> eachRedexCrossing : crossings2.entrySet()) {
                HashSet<BigraphEntity> removeThis = new HashSet<>();
                BigraphEntity redexInQuestion = eachRedexCrossing.getKey();
                LinkedList<BigraphEntity> possibleAgentNodes = mapping.get(redexInQuestion);
                logger.debug("Checking for current redex: {}", redexInQuestion);
                logger.debug("\twith the possibleAgentNodes: {}", possibleAgentNodes);

                // check here all possible agent matches
                // for all possibleAgentNodes do:
                //      for all eachRedex.getValue() do:
                for (BigraphEntity eachPossibleAgent : possibleAgentNodes) {
                    // the current eachPossibleAgent must fit for all other "redex crossings" in relation to the "redexInQuestion"
                    logger.debug("Observing agent entity: {}", eachPossibleAgent);
                    for (CrossPairLink otherRedexPair : eachRedexCrossing.getValue()) {
                        LinkedList<BigraphEntity> otherRedexAgents = mapping.get(otherRedexPair.getRedex());//for which redex?
                        logger.debug("\t::Redex {} with parent={} ", otherRedexPair.getOther(), otherRedexPair.getRedex());
                        //now: find the corresponding agent nodes of otherRedexPair.getOther()
                        // check if redexInQuestion is connected to the corresponding findings of otherRedexPair.getOther()
                        if (otherRedexAgents.size() == 0) continue;
                        List<BigraphEntity> allChildrenOf = getSubBigraphFrom(otherRedexAgents.get(0), agentAdapter)
                                .stream()
                                .filter(x -> agentAdapter.getPortCount(x) > 0)
                                .filter(x -> S.get(x, otherRedexPair.getOther()) != null && S.get(x, otherRedexPair.getOther()).size() > 0)
                                .filter(x -> agentAdapter.areConnected((BigraphEntity.NodeEntity) eachPossibleAgent, (BigraphEntity.NodeEntity) x))
                                .collect(toList());
                        logger.debug("\t::Are connected: {}", allChildrenOf);
                        if (allChildrenOf.size() == 0) {
                            logger.debug("\t::Removing agent {} because it and its children have no connection to the redex", eachPossibleAgent);
                            removeThis.add(eachPossibleAgent);
                        } else {
                            crossingsA.putIfAbsent(eachRedexCrossing.getKey(), new LinkedList<>());
                            crossingsA.get(eachRedexCrossing.getKey()).addAll(allChildrenOf);
                            logger.debug("\t::Keeping agent {}", eachPossibleAgent);
                        }
                    }
                }
                // Remove the previously matched agent that doesn't fit because of the redex node relationship to other redex nodes
                if (removeThis.size() > 0) {
                    boolean b = mapping.get(redexInQuestion).removeAll(removeThis);
                    assert b;
                    logger.debug("Remaining agent nodes suitable for replacement: {}", mapping.get(redexInQuestion));
                }
            }
        }
        return mapping;
    }

    /**
     * Structure that denotes a connection between two nodes.
     * <p>
     * Used to check a subtree for suitable node matches to another redex subtree, where a connection spans multiple
     * hierarchies (and tree levels).
     */
    public static class CrossPairLink {
        private BigraphEntity redex;
        private BigraphEntity other;

        private CrossPairLink(BigraphEntity redex, BigraphEntity other) {
            this.redex = redex;
            this.other = other;
        }

        public static CrossPairLink create(BigraphEntity redex, BigraphEntity other) {
            return new CrossPairLink(redex, other);
        }

        public BigraphEntity getRedex() {
            return redex;
        }

        public BigraphEntity getOther() {
            return other;
        }
    }

    /**
     * with link checking
     *
     * @param agent
     * @param redex
     * @param noExactMatch
     * @return
     */
    private boolean hasSameSpatialStructure3(BigraphEntity agent, BigraphEntity redex, boolean noExactMatch) {
        List<BigraphEntity> bigraphEntities = S.get(agent, redex);
        if (Objects.isNull(bigraphEntities)) return false;
        List<BigraphEntity> redexChildren = redexAdapter.getChildren(redex);
        if (noExactMatch && redexChildren.size() == 0 && bigraphEntities.size() != 0) {
            return true;
        }

        //no site is assumed: the degree has to match then
        if (!noExactMatch && redexChildren.size() != agentAdapter.getChildren(agent).size() && bigraphEntities.size() != 0)
            return false;

        for (BigraphEntity eachChild : redexChildren) {
            boolean hasSite = redexAdapter.getChildrenWithSites(eachChild).stream().anyMatch(BigraphEntityType::isSite);
            if (!hasSameSpatialStructure3(agent, eachChild, hasSite) && !checkLinkIdentityOfNodes(agent, eachChild)) {  //&& !checkLinkIdentityOfNodes(agent, eachChild))
                return false;
            }
        }
        return true;
    }

    // TODO: UTIL MACHEN: wird häufig verwendet!
    private void setParentOfNode(final BigraphEntity node, final BigraphEntity parent) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        node.getInstance().eSet(prntRef, parent.getInstance()); // child is automatically added to the parent according to the ecore model
    }

    private boolean linkMatching() {
        Set<BigraphEntity> bigraphEntities2 = results.rowMap().keySet();
        Set<BigraphEntity> bigraphEntities = results.columnMap().keySet();
        List<BigraphEntity> allChildrenFromNodeU = new ArrayList<>();
        List<BigraphEntity> allChildrenFromNodeV = new ArrayList<>();
        for (BigraphEntity eachV : bigraphEntities2) {
            allChildrenFromNodeV.addAll(agentAdapter.getSubtreeOfNode(eachV));
        }
        for (BigraphEntity eachU : bigraphEntities) {
            allChildrenFromNodeU.addAll(redexAdapter.getSubtreeOfNode(eachU));
            allChildrenFromNodeU.add(eachU);
        }
        List<BigraphEntity> uColl = allChildrenFromNodeU.stream().filter(x -> BigraphEntityType.isNode(x)).collect(toList());
        List<BigraphEntity> vColl = allChildrenFromNodeV.stream().filter(x -> BigraphEntityType.isNode(x)).collect(toList()); // && agentAdapter.getPortCount(x) > 0
//        if (vColl.size() < uColl.size()) return false;
        boolean areLinksOK = areLinksOK(uColl, vColl);
        return areLinksOK;
    }

    private boolean areLinksOK(List<BigraphEntity> redexPartition, List<BigraphEntity> agentPartition) {
        HashMap<BigraphEntity, Boolean> lnk = new HashMap<>();
        Table<BigraphEntity, BigraphEntity, Boolean> lnkTab = HashBasedTable.create();
        for (BigraphEntity v : agentPartition) {
            for (BigraphEntity u : redexPartition) {
                if (!redexAdapter.isBRoot(u.getInstance()) &&
                        !agentAdapter.isBRoot(v.getInstance()) &&
                        isSameControl(u, v)) {
                    boolean linksAreGood = checkLinksForNode(u, v);
                    lnk.putIfAbsent(u, linksAreGood);
                    if (!lnk.get(u) && linksAreGood) {
                        lnk.put(u, true);
                    }
                    lnkTab.put(u, v, linksAreGood);
                }
            }
        }

        boolean allMatchesAreGood = false;
        if (redexPartition.size() == lnk.size()) {
            allMatchesAreGood = !lnk.values().stream().anyMatch(x -> !x);
            logger.debug("allMatchesAreGood 3: " + allMatchesAreGood);
        }

        // This statement leads to an overflow: "agentPartition.stream().allMatch(x -> lnkTab.columnMap().get(x).containsValue(true));"
        // so we have to do it via a traditional loop
        boolean b = true;
        for (BigraphEntity entity : agentPartition) {
            if (Objects.nonNull(lnkTab.columnMap().get(entity)) && !lnkTab.columnMap().get(entity).containsValue(true)) {
                b = false;
                break;
            }
        }
        logger.debug("NewNew link matching result: {}", allMatchesAreGood);
        logger.debug("New link matching result: {}", b);
        logger.debug("Old link matching result: {}", !lnk.containsValue(false));
//        return b;
//        return !lnk.containsValue(false);
//        if (!b) return false;
//        return !lnk.containsValue(false);
//        return b; // || !lnk.containsValue(false);
//        return allMatchesAreGood || (b && !lnk.containsValue(false));
        return allMatchesAreGood;
    }

    private boolean checkLinkIdentityOfNodes(BigraphEntity v, BigraphEntity u) {
        List<BigraphEntity> bigraphEntities = S.get(v, u); // redex node list is returned
        if (Objects.isNull(bigraphEntities) || bigraphEntities.size() == 0) return false;

        List<AbstractDynamicMatchAdapter.ControlLinkPair> lnkRedex = redexAdapter.getLinksOfNode(u);
        List<AbstractDynamicMatchAdapter.ControlLinkPair> lnkAgent = agentAdapter.getLinksOfNode(v);
        //Die Anzahl muss auch stimmen
        if (lnkRedex.size() == 0 && lnkAgent.size() == 0) return true;
        if (lnkRedex.size() == lnkAgent.size()) {
            // inner names are not present, thus they are not checked and 'getNodesOfLink()' is enough
            List<BigraphEntity> collectR = lnkRedex.stream().map(x -> redexAdapter.getNodesOfLink((BigraphEntity.OuterName) x.getLink())).flatMap(x -> x.stream()).collect(toList());
            List<BigraphEntity> collectA = lnkAgent.stream().map(x -> agentAdapter.getNodesOfLink((BigraphEntity.OuterName) x.getLink())).flatMap(x -> x.stream()).collect(toList());
            if (collectA.size() < collectR.size()) {
                return false;
            }

            for (BigraphEntity a : collectR) {
                for (BigraphEntity b : collectA) {
                    List<BigraphEntity> childrenA = agentAdapter.getChildren(b);
                    List<BigraphEntity> childrenR = redexAdapter.getChildren(a);
                    for (int i = 0; i < childrenA.size(); i++) {
                        for (int j = 0; j < childrenR.size(); j++) {
                            return checkLinksForNode(childrenA.get(i), childrenR.get(j));
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean checkLinksForNode(BigraphEntity u, BigraphEntity v) {
        List<BigraphEntity> bigraphEntities = S.get(v, u);//corresponding?
        if (Objects.nonNull(bigraphEntities) && bigraphEntities.size() != 0) {
//            System.out.println(v.getControl() + " // " + u.getControl());

            // Edges mean "closed links" (reaction denied), outer are "open links" (reaction permitted)
            // However, both outer and edges are considered by method "getLinksOfNode" (same as in bigraphER)
            List<AbstractDynamicMatchAdapter.ControlLinkPair> lnkAgent = agentAdapter.getLinksOfNode(v);
            List<AbstractDynamicMatchAdapter.ControlLinkPair> lnkRedex = redexAdapter.getLinksOfNode(u);

            //Die Anzahl muss auch stimmen in der angegebenen Reihenfolge, aber bezeichner vom namen ist nicht relevant, muss bei der modellierung beachtet werden
            if ((lnkRedex.size() != 0) == (lnkAgent.size() != 0)) {
//            if (lnkRedex.size() != 0 && lnkAgent.size() != 0) {
                if (lnkRedex.size() != lnkAgent.size()) return false;
                for (int i = 0, n = lnkRedex.size(); i < n; i++) {
                    Collection<BigraphEntity> redexLinksOfEachU = redexAdapter.getPointsFromLink(lnkRedex.get(i).getLink());
                    Collection<BigraphEntity> agentLinksOfEachV = agentAdapter.getPointsFromLink((BigraphEntity.Link) lnkAgent.get(i).getLink());
//                    List<BigraphEntity> redexLinksOfEachU = redexAdapter.getPointsFromLink((BigraphEntity.Link) lnkRedex.get(i).getLink());
//                    List<BigraphEntity> agentLinksOfEachV = agentAdapter.getNodesOfLink((BigraphEntity.Link) lnkAgent.get(i).getLink());
                    boolean isDistinctLinkR = redexLinksOfEachU.size() == 1;
                    boolean isDistinctLinkA = agentLinksOfEachV.size() == 1;
                    if (isDistinctLinkA) {
                        if (isDistinctLinkR) {
//                            System.out.println("\tControl " + u.getControl() + " kann gematcht werden");
                        } else {
//                            System.out.println("\tControl " + u.getControl() + " kann NICHT gematcht werden");
                            return false; //eine edge
                        }
                    } else {
//                        System.out.println("\tControl " + u.getControl() + " kann gematcht werden");
                    }
                }
            } // else both have no links => instantly return true
//            return true;
            return (lnkRedex.size() == 0) == (lnkAgent.size() == 0);
        }
        return false;
    }
}
