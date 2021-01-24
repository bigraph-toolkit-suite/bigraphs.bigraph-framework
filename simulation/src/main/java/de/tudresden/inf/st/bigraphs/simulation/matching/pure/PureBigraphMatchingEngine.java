package de.tudresden.inf.st.bigraphs.simulation.matching.pure;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Table;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ContextIsNotActive;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.simulation.matching.BigraphMatchingEngine;
import de.tudresden.inf.st.bigraphs.simulation.matching.BigraphMatchingSupport;
import de.tudresden.inf.st.bigraphs.simulation.util.Permutations;
import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.BiMaps;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;
import static de.tudresden.inf.st.bigraphs.simulation.matching.AbstractDynamicMatchAdapter.ControlLinkPair;
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
public class PureBigraphMatchingEngine extends BigraphMatchingSupport implements BigraphMatchingEngine<PureBigraph> {

    private final Logger logger = LoggerFactory.getLogger(PureBigraphMatchingEngine.class);

    private final PureBigraphRedexAdapter redexAdapter;
    private final PureBigraphAgentAdapter agentAdapter;
    private Table<BigraphEntity<?>, BigraphEntity<?>, List<BigraphEntity<?>>> S = HashBasedTable.create();
    private AtomicInteger totalHits;
    private Table<BigraphEntity<?>, BigraphEntity<?>, Integer> results;

    private ImmutableList<BigraphEntity<?>> internalVertsG;
    private ImmutableList<BigraphEntity<?>> allVerticesOfH;
    private final MutableList<PureBigraphParametricMatch> matches = org.eclipse.collections.impl.factory.Lists.mutable.empty();
    // Agent -> redex root indices
    private final HashMap<BigraphEntity<?>, List<Integer>> hitsVIx = new HashMap<>();

    SubHypergraphIsoSearch search;
    Map<BigraphEntity.NodeEntity<?>, List<BigraphEntity.NodeEntity<?>>> candidatesHyperIso;
    //TODO make local (?)
    com.google.common.collect.BiMap<BigraphEntity<?>, LinkedList<BigraphEntity<?>>> crossingsA = HashBiMap.create();
    private Stopwatch matchingTimer;
    private FutureTask<Map<BigraphEntity.NodeEntity<?>, List<BigraphEntity.NodeEntity<?>>>> linkGraphIsoTask;

    PureBigraphMatchingEngine(PureBigraph agent, PureBigraph redex) {
        //signature, ground agent
        Stopwatch timer = logger.isDebugEnabled() ? Stopwatch.createStarted() : null;
        this.redexAdapter = new PureBigraphRedexAdapter(redex);
        this.agentAdapter = new PureBigraphAgentAdapter(agent);
        this.init();
        if (logger.isDebugEnabled() && Objects.nonNull(timer))
            logger.debug("Initialization time: {}", (timer.stop().elapsed(TimeUnit.NANOSECONDS) / 1e+6f));
    }

    @Override
    public List<PureBigraphParametricMatch> getMatches() {
        return matches;
    }

    private void init() {
        S = HashBasedTable.create();
        allVerticesOfH = redexAdapter.getAllVertices(); //new ArrayList<>(redexAdapter.getAllVertices());
        for (BigraphEntity<?> gVert : agentAdapter.getAllVertices()) {
            for (BigraphEntity<?> hVert : allVerticesOfH) {
                if (agentAdapter.degreeOf(gVert) <= 1 && !BigraphEntityType.isRoot(gVert) &&
                        redexAdapter.degreeOf(hVert) <= 1 && !BigraphEntityType.isRoot(hVert)) {
                    S.put(gVert, hVert, redexAdapter.getOpenNeighborhoodOfVertex(hVert));
                } else {
                    S.put(gVert, hVert, new ArrayList<>());
                }
            }
        }

        internalVertsG = agentAdapter.getAllInternalVerticesPostOrder();//TODO return as stream...
        totalHits = new AtomicInteger(0);
        results = HashBasedTable.create();

        search = new SubHypergraphIsoSearch(redexAdapter.getBigraphDelegate(), agentAdapter.getBigraphDelegate());
        linkGraphIsoTask = new FutureTask<>(() -> {
            search.embeddings();
            candidatesHyperIso = search.getCandidates();
            return candidatesHyperIso;
        });
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

//        search.embeddings();
//        candidatesHyperIso = search.getCandidates();
//        Set<SubHypergraphIsoSearch.Embedding> embeddingSet = search.getEmbeddingSet();
        executorService.submit(linkGraphIsoTask);

        List<List<BigraphEntity<?>>> partitionSets = new ArrayList<>();
        for (BigraphEntity<?> eachV : internalVertsG) {//TODO: create this in a stream-filter
            List<BigraphEntity<?>> childrenOfV = agentAdapter.getChildren(eachV);
//            List<BigraphEntity> u_vertsOfH = new ArrayList<>(allVerticesOfH);//TODO: create this in a stream-filter
            MutableList<BigraphEntity<?>> u_vertsOfH = org.eclipse.collections.api.factory.Lists.mutable.ofAll(allVerticesOfH);
            //d(u) <= t + 1
            int t = childrenOfV.size();
            for (int i = u_vertsOfH.size() - 1; i >= 0; i--) {
                BigraphEntity<?> each = u_vertsOfH.get(i);
                if (redexAdapter.degreeOf(each) > t + 1)
                    u_vertsOfH.remove(each); //TODO getdegree in bigraph vorspeichern
            }

            for (BigraphEntity<?> eachU : u_vertsOfH) {
                boolean cs = true; //eachU.getControl().equals(eachV.getControl());
                if (eachU.getControl() != null && eachV.getControl() != null) {
                    cs = isSameControl(eachU, eachV);
                }
                if (!cs) continue;

                List<BigraphEntity<?>> neighborsOfU = redexAdapter.getOpenNeighborhoodOfVertex(eachU);
//                neighborsOfU = neighborsOfU.stream().filter(x -> !x.getType().equals(BigraphEntityType.SITE)).collect(Collectors.toList());
                Graph<BigraphEntity<?>, DefaultEdge> bipartiteGraph = BigraphMatchingSupport.createBipartiteGraph(neighborsOfU, childrenOfV);

                // Additional Conditions to check in the following as well:
                // C1: has eachU some sites als sibling? If not, then eachV must have same sibling count
                // C2: control check
                // Do controls of childrenOfV and neighborsOfU match? (root is a placeholder)
                // eachU and eachV must match


                // Connect nodes by edges of the bipartite graph
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
                    List<BigraphEntity<?>> tmp = new ArrayList<>(neighborsOfU);
                    tmp.remove(neighborsOfU.get(i - 1));
                    partitionSets.add(tmp);
                }

                // To check:
                // 1) Has eachU a site as children? ->
                //      -> children are allowed 
                //      -> it's possible now that eachV can have more siblings than eachU
                boolean hasSite = false;
                // if the current element is a root then we automatically interpret it is a "site"
                if (redexAdapter.isBRoot(eachU.getInstance())) {
                    hasSite = true;
                } else {
//                    hasSite = redexAdapter.getChildrenWithSites(eachU).stream().anyMatch(BigraphEntityType::isSite);
                    for (BigraphEntity<?> eachSibOfU : redexAdapter.getChildrenWithSites(eachU)) {
                        if (BigraphEntityType.isSite(eachSibOfU)) {
                            hasSite = true;
                            break;
                        }
                    }
                }

                // Compute size of maximum matching of bipartite graph for all partitions
                List<BigraphEntity<?>> uSetAfterMatching = new ArrayList<>();
                for (int ic = 0; ic < partitionSets.size(); ic++) {
                    List<BigraphEntity<?>> eachPartitionX = partitionSets.get(ic);
                    try {
                        HKMCBM2 alg =
                                new HKMCBM2(bipartiteGraph,
                                        new HashSet<>(eachPartitionX), new HashSet<>(childrenOfV)
                                );
                        alg.setHasSite(hasSite);
                        MatchingAlgorithm.Matching<BigraphEntity<?>, DefaultEdge> matching = alg.getMatching();
                        int m = matching.getEdges().size();
                        if (m == eachPartitionX.size()) {
                            boolean m3 = alg.areControlsSame();
                            if (m3) {
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
                        // Save last eachU and eachV
                        // Agent node is mapped to redex root index
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

    /**
     * This methods builds the actual bigraphs determined by the matching algorithm (see {@link #beginMatch()}).
     */
    public void createMatchResult() {
        if (logger.isDebugEnabled()) {
            logger.debug("Matching took: {} ms", (matchingTimer.stop().elapsed(TimeUnit.NANOSECONDS) / 1e+6f));
            matchingTimer.reset().start();
        }

        try {
            candidatesHyperIso = linkGraphIsoTask.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // generic loop for unique and non-unique matchings of the redex root to agent's node
        int validCounter = 0;
//        boolean b1 = candidatesHyperIso.values().stream().allMatch(x -> x.size() > 0);
        if (!search.allCandidatesFound()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Number of valid match combinations: {}", validCounter);
                logger.debug("Time to build the match result: {} ms", (matchingTimer.stop().elapsed(TimeUnit.NANOSECONDS) / 1e+6f));
            }
            return;
        }
        // This is needed when there is not a distinct match of the redices roots to a agent's subtree
        // Anzahl an occurrence-bedingter "mehr"-transitionen

        final int numOfRoots = redexAdapter.getRoots().size();
        final boolean redexRootMatchIsUnique = numOfRoots == totalHits.get();
        LinkedList<BigraphEntity<?>> collect = new LinkedList<>(hitsVIx.keySet());
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
            Permutations.of(nums2).forEachOrdered(p -> {
                combination.add(p.toArray(Integer[]::new));
            });
        }


        // for every combination
        for (Integer[] eachCombination : combination) {
            // redex root to agent node
//            BiMap<Integer, BigraphEntity> hitsV_new = HashBiMap.create();
            MutableMap<Integer, BigraphEntity<?>> hitsV_new = org.eclipse.collections.api.factory.Maps.mutable.empty();
            for (int i = 0; i < eachCombination.length; i++) {
                int tmpRootIx = eachCombination[i];
                BigraphEntity<?> agentNode = collect.get(i);
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
//            final List<Table<Integer, BigraphEntity<?>, BigraphEntity<?>>> hitsV_newChildrenStack2 = new LinkedList<>();
            List<MatchPairing> mapPairings = new LinkedList<>();
            // The structure of the Map.Entry: redex root index -> matched agent node
            for (Map.Entry<Integer, BigraphEntity<?>> eachEntry : hitsV_new.entrySet()) {
                int redexRootIx = eachEntry.getKey();
                BigraphEntity<?> agentMatch = eachEntry.getValue();
                List<BigraphEntity<?>> childrenOfAgent = agentAdapter.getChildren(agentMatch);
                List<BigraphEntity<?>> childrenOfRedex = redexAdapter.getChildren(redexAdapter.getRoots().get(redexRootIx));
                Map<BigraphEntity<?>, LinkedList<BigraphEntity<?>>> nodeDiffByControlCheck = findOccurrences(childrenOfAgent, childrenOfRedex, true);
                // Check that not one entry is empty, otherwise return and no match could be found
                boolean incompleteMatch = nodeDiffByControlCheck.values().stream().filter(x -> x.size() == 0).anyMatch(obj -> true);
                if (incompleteMatch) return;

                //current impl: select the first possible agent node
                for (Map.Entry<BigraphEntity<?>, LinkedList<BigraphEntity<?>>> eachMapping : nodeDiffByControlCheck.entrySet()) {
//                    if(eachMapping.getValue().size() <= 1) continue;
                    //the agent-redex match is here already "good" (w/o considering the children yet - this is done later)
//                    MatchPairing q = new MatchPairing(redexRootIx, eachMapping.getKey(), eachMapping.getValue());
//                    q.getAgentMatches().addAll(eachMapping.getValue());
                    mapPairings.add(new MatchPairing(redexRootIx, eachMapping.getKey(), eachMapping.getValue()));
                }
            }

//            logger.debug("mapPairings={}", mapPairings);

            List<Map<MatchPairing, BigraphEntity<?>>> combinations = new LinkedList<>();
            HashMap<MatchPairing, List<BigraphEntity<?>>> collect4 = mapPairings.stream()
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
                if (elementRepeats(combinations.get(i).values())) {
                    continue;
                }
                final Table<Integer, BigraphEntity<?>, BigraphEntity<?>> matchTable = HashBasedTable.create();
                combinations.get(i)
                        .forEach((key, value) -> matchTable.put(key.getRootIndex(), value, key.getRedexNode()));

                // TODO: make the following faster: this step takes most of the time
                try {
                    // Structure of each: {redex root index -> {agentNode -> redexNode}} (are unique mappings here)
//                    logger.debug("Final redex<->agent node matching: {}", matchTable);
                    PureBigraphParametricMatch m = buildMatch(hitsV_new, matchTable, new LinkedList<>(), 0);
                    matches.add(m);
                } catch (ContextIsNotActive contextIsNotActive) {
                    contextIsNotActive.printStackTrace();
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Time to build the match result: {} ms", (matchingTimer.stop().elapsed(TimeUnit.NANOSECONDS) / 1e+6f));
            logger.debug("Number of valid match combinations: {}", validCounter);
        }
    }

    boolean elementRepeats(Collection<BigraphEntity<?>> nodes) {
        long count = nodes.stream().map(x -> ((BigraphEntity.NodeEntity<Control>) x).getName()).distinct().count();
        return count < nodes.size();
    }

    /**
     * Checks if any match could be found and also if <emph>_all_</emph> redex roots could be matched.
     *
     * @return {@code true}, if a correct match could be found, otherwise {@code false}
     */
    public boolean hasMatched() {
        return totalHits.get() > 0 && totalHits.get() >= redexAdapter.getRoots().size() && (totalHits.get() % redexAdapter.getRoots().size() == 0);
    }

    /**
     * Constructs the context and the parameters of the redex to build the "redex image".
     * <p>
     * This method may be called recursively to build other matches because of the "multiple occurrences" dilemma.
     * Therefore, the use of the {@code rekCnt} variable.
     *
     * @param hitsV_newChildren redex root ix, agent node, redex node
     */
    private PureBigraphParametricMatch buildMatch(
            Map<Integer, BigraphEntity<?>> hitsV,
            Table<Integer, BigraphEntity<?>, BigraphEntity<?>> hitsV_newChildren,
            LinkedList<BigraphEntity<?>> discards, int rekCnt) throws ContextIsNotActive {

        logger.debug("[Start] Build Match Result ...");

        MutableBuilder<DefaultDynamicSignature> builder = PureBigraphBuilder.newMutableBuilder(agentAdapter.getSignature(), agentAdapter.getModelPackage());
        MutableBuilder<DefaultDynamicSignature> builder2 = PureBigraphBuilder.newMutableBuilder(agentAdapter.getSignature(), agentAdapter.getModelPackage());
//        PureBigraphFactory pureBigraphFactory = pure();
        Placings<DefaultDynamicSignature> placingsFactory = purePlacings(agentAdapter.getSignature());
        Linkings<DefaultDynamicSignature> linkingsFactory = pureLinkings(agentAdapter.getSignature());

        // agent->redex
        final MutableBiMap<BigraphEntity.NodeEntity, BigraphEntity.NodeEntity> matchDict = BiMaps.mutable.empty();

        // context: replace the agent with sites at eachV and if edge exists, make inner name
        // Compute the context, and the parameters
        Map<Integer, Bigraph<DefaultDynamicSignature>> parameters = new LinkedHashMap<>();

        //when no params are necessary then a barren is all that is needed
        boolean needsParameters = redexAdapter.getSites().size() >= 1;
        if (needsParameters) {
            redexAdapter.getSites().forEach(x -> parameters.put(x.getIndex(), placingsFactory.barren()));
        }


        // First: create roots: should only have one root!
        final BigraphEntity.RootEntity newRootCtx = (BigraphEntity.RootEntity) builder.createNewRoot(0); //first.get().getIndex());
        builder.availableRoots().put(newRootCtx.getIndex(), newRootCtx);
        final Map<Integer, BigraphEntity.SiteEntity> newSites = org.eclipse.collections.impl.factory.Maps.mutable.empty(); //new HashMap<>();
        final Map<String, BigraphEntity.NodeEntity> newNodes = org.eclipse.collections.impl.factory.Maps.mutable.empty(); //new HashMap<>();

        // recreate nodes that are in V exclusive U
        // where first u node is found replace with site
        // this should build the node hierarchy and put places under those nodes where the redex matched

        Set<BigraphEntity<?>> blockNodesForContext = org.eclipse.collections.impl.factory.Sets.mutable.empty(); // new HashSet<>();

        // Recreate idle outer names in the agent for the context: the redex don't need to be checked, as it
        // cannot have idle outer names. It is "simple" (this constraint is checked when creating a RR)
        if (!agentAdapter.isEpimorphic()) {
            agentAdapter.getOuterNames().stream().filter(x -> agentAdapter.getPointsFromLink(x).size() == 0).forEach(link -> {
                builder.createOuterName(link.getName());
//                newOuterNames.put(newOuterName.getName(), newOuterName);
            });
        }

//        for (BigraphEntity eachNodeV : agentAdapter.getAllVerticesBfsOrder()) {
        agentAdapter.getAllVerticesBfsOrderStream().forEachOrdered(eachNodeV -> {

                    // skip blocked children first (children that are in the redex match
                    // this part will be executed after the top-level nodes of the redex were matched with the corresponding agent nodes
                    if (blockNodesForContext.contains(eachNodeV)) {
                        //put all their children inside
//                        blockNodesForContext.addAll(agentAdapter.getChildrenOf(eachNodeV));
                        blockNodesForContext.addAll(agentAdapter.getSubtreeOfNode(eachNodeV));
                        return;
                    }

                    final BigraphEntity<?> newNode;
                    BigraphEntity<?> newParent = agentAdapter.getParent(eachNodeV);
                    if (newParent == null || BigraphEntityType.isRoot(newParent)) {
                        newParent = newRootCtx;
                    } else { // else find or create a new parent
                        //find the parent or create a new one first
                        final String theParentName = ((BigraphEntity.NodeEntity) newParent).getName();
                        final BigraphEntity<?> _newParent0 = agentAdapter.getParent(eachNodeV);
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
                    boolean hasAmatch = hitsV.containsValue(eachNodeV);
                    if (hasAmatch) {
                        Integer rootIx = hitsV.entrySet().stream().filter(x -> x.getValue().equals(eachNodeV)).findFirst().get().getKey();
//                        (Integer) ((UnifiedMap) hitsV).flip().get(eachNodeV).getOnly(); //
                        MutableList<BigraphEntity<?>> savedRedexNodes = org.eclipse.collections.impl.factory.Lists.mutable.empty(); //new ArrayList<>(); //all agent nodes that shouldn't be considered anymore

                        for (BigraphEntity<?> agentNext : hitsV_newChildren.columnMap().keySet()) {
                            BigraphEntity<?> redexEntityCorrespondence = hitsV_newChildren.column(agentNext).get(rootIx);
//                            Map.Entry<Integer, BigraphEntity<?>> integerBigraphEntityEntry = hitsV_newChildren.column(agentNext)
//                                    .entrySet()
//                                    .stream().findFirst().get();
//                            Integer rootIx0 = integerBigraphEntityEntry.getKey();
//                            BigraphEntity<?> redexEntityCorrespondence = hitsV_newChildren.get(rootIx0, agentNext);
                            try {
                                //TODO put?
                                matchDict.forcePut((BigraphEntity.NodeEntity) agentNext, (BigraphEntity.NodeEntity) redexEntityCorrespondence);
                            } catch (java.lang.IllegalArgumentException e) {
                                e.printStackTrace();
                            }
                            // redexEntityCorrespondence "is equal to" agentNext
                            savedRedexNodes.add(agentNext);
                            // Rest of the available nodes remains and the savedRedexNodes goes into the blocking list
                            // and all their children
                            blockNodesForContext.add(agentNext);
                            blockNodesForContext.addAll(agentAdapter.getChildrenOf(agentNext));
                        }
//                        logger.debug("iterateThroughChildren: start");
//                        logger.debug("hitsV_newChildren.columnMap().keySet()={}", hitsV_newChildren.columnMap().keySet());
                        // two-times iteration: because we need the matchDict to be complete for the current level
                        for (BigraphEntity<?> agentNext : hitsV_newChildren.columnMap().keySet()) {
                            Map.Entry<Integer, BigraphEntity<?>> integerBigraphEntityEntry = hitsV_newChildren.column(agentNext)
                                    .entrySet()
                                    .stream().findFirst().get();
                            Integer rootIx0 = integerBigraphEntityEntry.getKey();
                            BigraphEntity<?> redexEntityCorrespondence = hitsV_newChildren.get(rootIx0, agentNext);
//                            logger.debug("redexEntityCorrespondence={}", redexEntityCorrespondence);
                            iterateThroughChildren(redexEntityCorrespondence, agentNext, parameters, matchDict, builder2,
                                    hitsV, hitsV_newChildren, discards, rekCnt);
                        }
//                        logger.debug("iterateThroughChildren: finished");
//                        logger.debug("while iterator finished");
//                        logger.debug("BlockedNodes: {}", savedRedexNodes);


                        // childrenOfCurrentAgentParent: contains only contain agent nodes which couldn't be match
                        // (i.e., which shall be preserved)
                        // now we are left only with the remaining children precluding the redex match at the current level (height of the tree)
                        agentAdapter.getChildrenOf(eachNodeV).stream()
                                .filter(x -> !savedRedexNodes.contains(x))
                                .forEachOrdered(x -> {
                                    BigraphEntity.NodeEntity newNode1 = (BigraphEntity.NodeEntity) builder.createNewNode(x.getControl(), ((BigraphEntity.NodeEntity) x).getName());
                                    newNodes.put(newNode1.getName(), newNode1);
                                    setParentOfNode(newNode1, newNode);
                                });

                        // create site procedure
                        if (BigraphEntityType.isRoot(eachNodeV) && redexAdapter.getRoots().size() > 1) {
                            for (int rix = 0; rix < redexAdapter.getRoots().size(); rix++) {
                                BigraphEntity.SiteEntity newSite = (BigraphEntity.SiteEntity) builder.createNewSite(rix);
                                newSites.put(rix, newSite);
                                setParentOfNode(newSite, newNode);
                            }
//                            IntStream.range(0, redexAdapter.getRoots().size()).boxed()
//                                    .forEachOrdered(rix -> {
//                                        BigraphEntity.SiteEntity newSite = (BigraphEntity.SiteEntity) builder.createNewSite(rix);
//                                        newSites.put(rix, newSite);
//                                        setParentOfNode(newSite, newNode);
//                                    });
                        } else {
                            //get the root index of the corresponding redex match node eachU
                            BigraphEntity.SiteEntity newSite = (BigraphEntity.SiteEntity) builder.createNewSite(rootIx);
                            newSites.put(newSite.getIndex(), newSite);
                            setParentOfNode(newSite, newNode);
                        }
                    }

                    List<ControlLinkPair> linksOfNode = agentAdapter.getLinksOfNode(eachNodeV);
                    for (ControlLinkPair eachPair : linksOfNode) {
                        String linkName;
                        if (BigraphEntityType.isEdge(eachPair.getLink())) {
                            linkName = eachPair.getLink().getName();
                            BigraphEntity.InnerName innerName = builder.availableInnerNames().entrySet().stream()
                                    .filter(e -> e.getValue().getName().equals(linkName))
                                    .map(Map.Entry::getValue)
                                    .findFirst()
                                    .orElse(builder.createInnerName(linkName + "_innername"));
                            try {
                                builder.connectNodeToInnerName((BigraphEntity.NodeEntity) newNode, innerName);
//                                BigraphEntity.InnerName orig = builder.createInnerName(linkName);
//                                builder.addInnerNameTo(innerName, orig);  //TODO here: add also e0??
                            } catch (LinkTypeNotExistsException | InvalidConnectionException e) {
                                e.printStackTrace();
                            }
                        } else if (BigraphEntityType.isOuterName(eachPair.getLink())) {
                            final BigraphEntity.OuterName link = (BigraphEntity.OuterName) eachPair.getLink();
                            BigraphEntity.OuterName newOuterName = builder.availableOuterNames().values().stream()
                                    .filter(outerName -> outerName.getName().equals(link.getName()))
                                    .findFirst()
                                    .orElse(builder.createOuterName(link.getName()));
                            builder.connectNodeToOuterName((BigraphEntity.NodeEntity) newNode, newOuterName);
                        }
                    }
                }
        );

        // outer names in d must not included in outernames of R: die names kommen direkt aus dem agent und dürfen gleichen namen haben

        PureBigraph context = new PureBigraph(builder.new InstanceParameter(
                builder.getLoadedEPackage(),
                agentAdapter.getSignature(),
                builder.availableRoots(), // roots
                newSites,
                newNodes,
                builder.availableInnerNames(), builder.availableOuterNames(), builder.getCreatedEdges()));
        builder.reset();

        //TODO: add to identityForContext: the artificial innernames to be closed.
//        Linkings<DefaultDynamicSignature> linkings = pureBigraphFactory.createLinkings(agentAdapter.getSignature());
        // Build the identity link graph for the context and the redex
        // to build the identity graph later for the redex
        HashMap<String, List<String>> substitutionLinkingGraph = new HashMap<>();
        HashMap<String, List<String>> substitutionLinkingGraphForRedex = new HashMap<>();
        Bigraph<DefaultDynamicSignature> identityForContext = linkingsFactory.identity_e();
        MutableList<String> alreadyUsedNamesOfNode = org.eclipse.collections.impl.factory.Lists.mutable.empty();

        for (BigraphEntity.Link eachAgentLink : agentAdapter.getAllLinks()) {
            Collection<BigraphEntity.NodeEntity<?>> pointsFromLink = agentAdapter.getPointsFromLink(eachAgentLink)
                    .stream()
                    .filter(BigraphEntityType::isPort)
                    .map(x -> agentAdapter.getNodeOfPort((BigraphEntity.Port) x))
                    .filter(x -> Objects.nonNull(matchDict.get(x)))
                    .collect(toList());
            if (BigraphEntityType.isEdge(eachAgentLink)) {
                substitutionLinkingGraphForRedex.putIfAbsent(eachAgentLink.getName() + "_innername", new ArrayList<>());//todo here
                substitutionLinkingGraph.putIfAbsent(eachAgentLink.getName() + "_innername", new ArrayList<>());//todo here
            } else {
                substitutionLinkingGraph.putIfAbsent(eachAgentLink.getName(), new ArrayList<>());
            }
            for (BigraphEntity.NodeEntity<?> each : pointsFromLink) {
                BigraphEntity.NodeEntity<?> matchedRedex = matchDict.get(each);
//                    if (Objects.isNull(matchedRedex)) continue;
                assert Objects.nonNull(matchedRedex);
                LinkedList<ControlLinkPair> linksOfNodeR = redexAdapter.getLinksOfNode(matchedRedex);
                LinkedList<ControlLinkPair> linksOfNodeA = agentAdapter.getLinksOfNode(each);
                assert linksOfNodeR.size() == linksOfNodeA.size();
                for (int i = 0; i < linksOfNodeR.size(); i++) { //ControlLinkPair pairR : linksOfNodeR) {
                    ControlLinkPair pairR = linksOfNodeR.get(i);
                    ControlLinkPair pairA = linksOfNodeA.get(i);
                    String linkNameR = pairR.getLink().getName();
                    String linkNameA = pairA.getLink().getName();
                    if (!alreadyUsedNamesOfNode.contains(linkNameR)) {
                        if (BigraphEntityType.isEdge(pairA.getLink()) && BigraphEntityType.isEdge(eachAgentLink)) { //&& BigraphEntityType.isEdge(eachAgentLink)
                            substitutionLinkingGraphForRedex.get(eachAgentLink.getName() + "_innername").add(linkNameR);
                            substitutionLinkingGraph.get(eachAgentLink.getName() + "_innername").add(linkNameR);
                            alreadyUsedNamesOfNode.add(linkNameR);
                        } else if (!BigraphEntityType.isEdge(pairA.getLink())) {
                            substitutionLinkingGraph.get(eachAgentLink.getName()).add(linkNameR);
                            alreadyUsedNamesOfNode.add(linkNameR);
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, List<String>> each : substitutionLinkingGraph.entrySet()) {
            List<StringTypedName> tmp = each.getValue().stream().map(StringTypedName::of).collect(toList());

            if (each.getKey().endsWith("_innername")) continue;
            if (needsParameters || each.getValue().size() == 0) { //!each.getKey().endsWith("_innername") ||
                tmp.add(StringTypedName.of(each.getKey()));
            }
//                if (each.getKey().endsWith("_innername"))
            Linkings<DefaultDynamicSignature>.Substitution tmpSub = linkingsFactory.substitution(StringTypedName.of(each.getKey()), (List) tmp);
            try {
                identityForContext = ops(identityForContext)
                        .parallelProduct(tmpSub).getOuterBigraph();

//                if (!needsParameters) {
//                    for (Map.Entry<String, List<String>> each2 : substitutionLinkingGraphForRedex.entrySet()) {
//                        if (each2.getValue().size() == 0) {
//                            PureBigraphBuilder<DefaultDynamicSignature> b = pureBigraphFactory.createBigraphBuilder(agentAdapter.getSignature());
//                            b.createOuterName(each.getKey());
//                            PureBigraph idleOuter = b.createBigraph();
//                            identityForContext = pureBigraphFactory.asBigraphOperator(identityForContext).parallelProduct(idleOuter).getOuterBigraph();
//                        }
//                    }
//                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        try {


            Bigraph<DefaultDynamicSignature> identityForParams;
            List<StringTypedName> namesTmp = parameters.values().stream().map(Bigraph::getOuterNames)
                    .flatMap(Collection::stream)
                    .map(x -> StringTypedName.of(x.getName()))
                    .collect(toList());
            if (namesTmp.size() == 0 || parameters.size() == 0) {
                identityForParams = linkingsFactory.identity_e();
                if (substitutionLinkingGraphForRedex.size() != 0) {
                    // CHANGED TO:
                    Set<StringTypedName> namesRedex = redexAdapter.getOuterNames().stream().map(x -> StringTypedName.of(x.getName())).collect(Collectors.toSet());
                    identityForParams = namesRedex.size() != 0 ?
                            linkingsFactory.identity(namesRedex.toArray(new NamedType[0])) : // as array
                            identityForParams;
//                    identityForParams = identityForContext; // COMMENTED
                    Placings<DefaultDynamicSignature>.Permutation permutation = placingsFactory.permutation(redexAdapter.getRoots().size());
                    identityForParams = ops(permutation).parallelProduct(identityForParams).getOuterBigraph();
                    for (Map.Entry<String, List<String>> each : substitutionLinkingGraphForRedex.entrySet()) {
                        if (each.getValue().size() == 0) continue;
                        List<StringTypedName> tmp = each.getValue().stream().map(StringTypedName::of).collect(toList());
//                        tmp.add(StringTypedName.of(each.getKey()));
                        Linkings<DefaultDynamicSignature>.Substitution tmpSub = linkingsFactory.substitution(StringTypedName.of(each.getKey()), (List) tmp);
                        identityForParams = ops(identityForParams).parallelProduct(tmpSub).getOuterBigraph();
                    }
                }
            } else {
                identityForParams = linkingsFactory.identity(namesTmp.toArray(new StringTypedName[0]));
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


            List<Integer> collect = context.getSites().stream().collect(Collectors.toMap(BigraphEntity.SiteEntity::getIndex, s -> context.isActiveAtSite(s.getIndex())))
                    .entrySet().stream().filter(k -> !k.getValue()).map(k -> k.getKey()).collect(toList());
            if (collect.size() != 0) {
                throw new ContextIsNotActive(collect.stream().mapToInt(i -> i).toArray());
            }

            PureBigraph redexImage = ops(redexAdapter.getBigraphDelegate())
                    .parallelProduct(identityForParams)
                    .getOuterBigraph();
            // parameters are only needed when RR contains sites, otherwise they contain just a barren or are empty
            return new PureBigraphParametricMatch(
                    context,
                    redexAdapter.getBigraphDelegate(),
                    redexImage,
                    parameters.values(),
                    identityForParams,
                    identityForContext
            );
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void createNamesForNodeInParam(BigraphEntity.NodeEntity newNode,
                                           BigraphEntity<?> agentNode,
                                           Map<String, BigraphEntity.OuterName> outerNames,
                                           MutableBuilder<DefaultDynamicSignature> builder2) {
        LinkedList<ControlLinkPair> linksOfNode = agentAdapter.getLinksOfNode(agentNode);
        for (ControlLinkPair each : linksOfNode) {
            if (BigraphEntityType.isOuterName(each.getLink())) {
                BigraphEntity.OuterName newOuterName = builder2.createOuterName(each.getLink().getName());
                builder2.connectNodeToOuterName(newNode, newOuterName);
                outerNames.put(newOuterName.getName(), newOuterName);
            } else if (BigraphEntityType.isEdge(each.getLink())) {
                BigraphEntity.OuterName newOuterName = builder2.createOuterName(each.getLink().getName() + "_innername");
                builder2.connectNodeToOuterName(newNode, newOuterName);
                outerNames.put(newOuterName.getName(), newOuterName);
            }
        }
    }

    /**
     * Submethod of the {@link #buildMatch(Map, Table, LinkedList, int)} method.
     * It is recursively called, to collect the parts for the match results such as parameters, etc.
     * for each sublevel in the bigraph.
     */
    private void iterateThroughChildren(
            BigraphEntity<?> redex, BigraphEntity<?> agent,
            Map<Integer, Bigraph<DefaultDynamicSignature>> parameters,
            MutableBiMap<BigraphEntity.NodeEntity, BigraphEntity.NodeEntity> matchDict,
            MutableBuilder<DefaultDynamicSignature> builder2,
            Map<Integer, BigraphEntity<?>> hitsV,
            Table<Integer, BigraphEntity<?>, BigraphEntity<?>> hitsV_newChildren,
            LinkedList<BigraphEntity<?>> discards, int rekCnt
    ) {
        boolean hasSite = redexAdapter.getChildrenWithSites(redex).stream().anyMatch(BigraphEntityType::isSite);
        List<BigraphEntity<?>> redexChildren = redexAdapter.getChildren(redex);

        Map<BigraphEntity<?>, BigraphEntity<?>> distinctMatch = new ConcurrentHashMap<>();
        List<BigraphEntity<?>> agentChildren = agentAdapter.getChildren(agent);

//        logger.debug("iterateThroughChildren:findOccurrences");
        Map<BigraphEntity<?>, LinkedList<BigraphEntity<?>>> occurrences = findOccurrences(agentChildren, redexChildren, hasSite);
        LinkedList<BigraphEntity<?>> discardAgents = new LinkedList<>();
        discardAgents.addAll(discards);
        LinkedList<BigraphEntity<?>> agentChildrenKeep = new LinkedList<>();//SortedSets.mutable.empty(); //ofAll(agentChildren);
        int calcRekCnt = 0;
        for (Map.Entry<BigraphEntity<?>, LinkedList<BigraphEntity<?>>> redexAgentMapping : occurrences.entrySet()) {
            // 1. Normally, get any value from the value list: here we just take the first one (order of the list)
            // 2. However, check if one of the child has a link to another redex match. If so, find out the correspondence

            // take special care of "cross-linking" nodes
            discardAgents.addAll(findCrossLinkingNodes(redexAgentMapping, matchDict));
            calcRekCnt += redexAgentMapping.getValue().size();
            for (BigraphEntity<?> correspondence : redexAgentMapping.getValue()) {
                if (agentChildren.contains(correspondence) && // is still available
                        !discardAgents.contains(correspondence) && // should be ignored
                        distinctMatch.get(redexAgentMapping.getKey()) == null // no match found yet for this redex node
                ) {
//                    calcRekCnt = redexAgentMapping.getValue().size();
                    calcRekCnt--;
                    agentChildren.remove(correspondence);
                    distinctMatch.put(redexAgentMapping.getKey(), correspondence);
                    agentChildrenKeep.add(correspondence);
                    try {
//                        matchDict.forcePut((BigraphEntity.NodeEntity) correspondence, (BigraphEntity.NodeEntity) redexAgentMapping.getKey());
                        matchDict.put((BigraphEntity.NodeEntity) correspondence, (BigraphEntity.NodeEntity) redexAgentMapping.getKey());
                    } catch (java.lang.IllegalArgumentException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }


        if (agentChildrenKeep.size() != 0) {
            if (rekCnt < calcRekCnt) {

                for (BigraphEntity<?> each : agentChildrenKeep) {
                    try {
                        int reduce = agentAdapter.getChildren(each).size();
                        LinkedHashSet<BigraphEntity<?>> newDiscards = new LinkedHashSet<>(discards);
                        boolean b = agentAdapter.getSiblingsOfNode(each).stream().allMatch(n -> {
                            return n.getControl().equals(each.getControl());
                        });
                        boolean siblingsHaveChildren = agentAdapter.getSiblingsOfNode(each).stream()
                                .anyMatch(n -> {
                                    return agentAdapter.getChildren(n).size() > 0;
                                });
                        if (b && !siblingsHaveChildren && reduce == 0) continue;
                        if (b && siblingsHaveChildren)
                            newDiscards.add(each);
                        PureBigraphParametricMatch pureBigraphParametricMatch =
                                buildMatch(hitsV, hitsV_newChildren, new LinkedList<>(newDiscards), rekCnt + 1);
                        matches.add(pureBigraphParametricMatch);
                    } catch (ContextIsNotActive e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }


        if (hasSite) {
            //Everything in this level can be extracted for the "parameter bigraph"
            // Every branch is traversed again with the "correspondences" now

            BigraphEntity.SiteEntity entity = (BigraphEntity.SiteEntity) redexAdapter.getChildrenWithSites(redex).stream()
                    .filter(BigraphEntityType::isSite).findFirst().get();
            int siteIndex = entity.getIndex();

            BigraphEntity.RootEntity rootParam = (BigraphEntity.RootEntity) builder2.createNewRoot(siteIndex);
            LinkedHashMap<String, BigraphEntity.NodeEntity> paramNodes = new LinkedHashMap<>(); // map of all created param nodes so far
            LinkedHashMap<String, BigraphEntity.OuterName> paramOuterNames = new LinkedHashMap<>();
            Traverser<BigraphEntity<?>> paramTraverser = Traverser.forTree(currentAgentNode -> {
                BigraphEntity.NodeEntity np;
                List<BigraphEntity<?>> children = agentAdapter.getChildren(currentAgentNode);
                if ((np = paramNodes.get(((BigraphEntity.NodeEntity) currentAgentNode).getName())) == null) {
                    np = (BigraphEntity.NodeEntity) builder2
                            .createNewNode(currentAgentNode.getControl(), ((BigraphEntity.NodeEntity) currentAgentNode).getName());
                    setParentOfNode(np, rootParam);
                    paramNodes.put(np.getName(), np);
                    createNamesForNodeInParam(np, currentAgentNode, paramOuterNames, builder2);
                }
                for (BigraphEntity entity2 : children) {
                    if (paramNodes.get(((BigraphEntity.NodeEntity) entity2).getName()) == null) {
                        BigraphEntity.NodeEntity newNode1 = (BigraphEntity.NodeEntity) builder2
                                .createNewNode(entity2.getControl(), ((BigraphEntity.NodeEntity) entity2).getName());
                        setParentOfNode(newNode1, np);
                        createNamesForNodeInParam(newNode1, entity2, paramOuterNames, builder2);
                        paramNodes.put(newNode1.getName(), newNode1);
                    }
                }
                return children;
            });
//            Lists.newArrayList(paramTraverser.breadthFirst(agentChildren)); // fills the paramNodes map
            paramTraverser.breadthFirst(agentChildren).forEach(x -> {
            });
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

        // Process the rest recursively in the same fashion
        for (Map.Entry<BigraphEntity<?>, BigraphEntity<?>> eachEntry : distinctMatch.entrySet()) {
            iterateThroughChildren(eachEntry.getKey(), eachEntry.getValue(), parameters, matchDict, builder2, hitsV, hitsV_newChildren, discards, rekCnt);
        }
    }


    /**
     * Take special care of cross linking nodes
     */
    private LinkedList<BigraphEntity<?>> findCrossLinkingNodes(
            Map.Entry<BigraphEntity<?>, LinkedList<BigraphEntity<?>>> redexAgentMapping,
            MutableBiMap<BigraphEntity.NodeEntity, BigraphEntity.NodeEntity> matchDict
    ) {
        LinkedList<BigraphEntity<?>> removeEntities = new LinkedList<>();
        for (BigraphEntity<?> matchingAgent : redexAgentMapping.getValue()) {
            for (Map.Entry<BigraphEntity<?>, LinkedList<BigraphEntity<?>>> cross : crossingsA.entrySet()) {
                if (cross.getValue().contains(matchingAgent)) { // this is only valid for cross-linking nodes
                    BigraphEntity.NodeEntity correspondingAgent = matchDict.inverse().get(cross.getKey());
                    if (Objects.nonNull(correspondingAgent) && !agentAdapter.areConnected(correspondingAgent, (BigraphEntity.NodeEntity) matchingAgent)) {
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
    public Map<BigraphEntity<?>, LinkedList<BigraphEntity<?>>> findOccurrences
    (Collection<BigraphEntity<?>> agentNodes, Collection<BigraphEntity<?>> redexNodes,
     boolean withSitesNoExactMatch) {
        Map<BigraphEntity<?>, LinkedList<BigraphEntity<?>>> mapping = new HashMap<>();
        List<BigraphEntity<?>> agents = new LinkedList<>(agentNodes);
        //Liste durchgehen von redexNodes
        //in 2. schleifen die agent nodes die noch übrig sind
        //prüfen: control, degree (eins abziehen wegen root, wenn site in children vorhanden dann darf mehr vorhanden sein, ansonsten gleich)
        for (BigraphEntity<?> eachRedex : redexNodes) { //degree checking is done in hasSameSpatialStructure() method
//            logger.debug("Checking Redex: {}", ((BigraphEntity.NodeEntity<Control>) eachRedex).getName());
            for (int i = agents.size() - 1; i >= 0; i--) {
                BigraphEntity<?> eachAgent = agents.get(i);
                if (isSameControl(eachAgent, eachRedex)) {
                    boolean hasSite = redexAdapter.getChildrenWithSites(eachRedex).stream().anyMatch(BigraphEntityType::isSite);
                    boolean sameLinks1 = (!Objects.nonNull(candidatesHyperIso.get(eachRedex)) || candidatesHyperIso.get(eachRedex).contains(eachAgent));
                    if (sameLinks1) {
                        boolean sameLinks2 = checkLinksForNode(eachRedex, eachAgent);
                        if (sameLinks2) {
                            boolean sameStructure = hasSameSpatialStructure(eachAgent, eachRedex, hasSite);
                            if (sameStructure) { //
//                                logger.debug("\tKeep agent: {}", ((BigraphEntity.NodeEntity<Control>) eachAgent).getName());
                                mapping.putIfAbsent(eachRedex, new LinkedList<>());
                                mapping.get(eachRedex).add(eachAgent);
                            } else {
//                                logger.debug("\tDiscard agent: {}", ((BigraphEntity.NodeEntity<Control>) eachAgent).getName());
                            }
                        }
                    }
                }
            }
        }
        // reduce space here further: check if one redex has only one match: remove this from all the other with multiple ones (if it occurs in it as well)
        com.google.common.collect.BiMap<BigraphEntity<?>, List<CrossPairLink>> crossings2 = HashBiMap.create();
        if (redexNodes.size() > 1) {
            for (Map.Entry<BigraphEntity<?>, LinkedList<BigraphEntity<?>>> each : mapping.entrySet()) {
                // only for those redexes which have multiple possible  matchings
                if (each.getValue().size() <= 1) continue;
                BigraphEntity<?> currentRedex = each.getKey();
                List<BigraphEntity<?>> rest = mapping.keySet().stream()
                        .filter(bigraphEntities -> !bigraphEntities.equals(each.getKey())).collect(toList());

                // First get all linked nodes under the current redex (a subbigraph)
                // For that we get _all_ children of the current redex first, and then gather all their links.
                // they are somehow "grouped" through this 2D list structure
                List<List<BigraphEntity<?>>> redexLinks = getSubBigraphFrom(currentRedex, redexAdapter)
                        .stream().flatMap(x -> redexAdapter.getLinksOfNode(x).stream())
                        .map(x -> redexAdapter.getNodesOfLink((BigraphEntity.Link) x.getLink()))
                        .collect(toList());

                // Ist alles was ich unter der Liste finde, der parent "currentRedex"?
                for (List<BigraphEntity<?>> nodes : redexLinks) {
                    List<Map.Entry<? extends BigraphEntity<?>, Boolean>> result = nodes.stream()
                            .collect(Collectors.toMap(p -> p, p -> redexAdapter.isParentOf(p, currentRedex)))
                            .entrySet().stream()
                            .filter(x -> !x.getValue())
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
            for (Map.Entry<BigraphEntity<?>, List<CrossPairLink>> eachRedexCrossing : crossings2.entrySet()) {
                HashSet<BigraphEntity<?>> removeThis = new HashSet<>();
                BigraphEntity<?> redexInQuestion = eachRedexCrossing.getKey();
                LinkedList<BigraphEntity<?>> possibleAgentNodes = mapping.get(redexInQuestion);
//                logger.debug("Checking for current redex: {}", redexInQuestion);
//                logger.debug("\twith the possibleAgentNodes: {}", possibleAgentNodes);

                // check here all possible agent matches
                // for all possibleAgentNodes do:
                //      for all eachRedex.getValue() do:
                for (BigraphEntity<?> eachPossibleAgent : possibleAgentNodes) {
                    // the current eachPossibleAgent must fit for all other "redex crossings" in relation to the "redexInQuestion"
//                    logger.debug("Observing agent entity: {}", eachPossibleAgent);
                    for (CrossPairLink otherRedexPair : eachRedexCrossing.getValue()) {
                        LinkedList<BigraphEntity<?>> otherRedexAgents = mapping.get(otherRedexPair.getRedex());//for which redex?
//                        logger.debug("\t::Redex {} with parent={} ", otherRedexPair.getOther(), otherRedexPair.getRedex());
                        //now: find the corresponding agent nodes of otherRedexPair.getOther()
                        // check if redexInQuestion is connected to the corresponding findings of otherRedexPair.getOther()
                        if (otherRedexAgents.size() == 0) continue;
                        List<BigraphEntity<?>> allChildrenOf = (List) getSubBigraphFrom(otherRedexAgents.get(0), agentAdapter)
                                .stream()
                                .filter(x -> agentAdapter.getPortCount((BigraphEntity.NodeEntity) x) > 0)
                                .filter(x -> S.get(x, otherRedexPair.getOther()) != null && S.get(x, otherRedexPair.getOther()).size() > 0)
                                .filter(x -> agentAdapter.areConnected((BigraphEntity.NodeEntity) eachPossibleAgent, (BigraphEntity.NodeEntity) x))
                                .collect(toList());
//                        logger.debug("\t::Are connected: {}", allChildrenOf);
                        if (allChildrenOf.size() == 0) {
//                            logger.debug("\t::Removing agent {} because it and its children have no connection to the redex", eachPossibleAgent);
                            removeThis.add(eachPossibleAgent);
                        } else {
                            crossingsA.putIfAbsent(eachRedexCrossing.getKey(), new LinkedList<>());
                            crossingsA.get(eachRedexCrossing.getKey()).addAll(allChildrenOf);
//                            logger.debug("\t::Keeping agent {}", eachPossibleAgent);
                        }
                    }
                }
                // Remove the previously matched agent that doesn't fit because of the redex node relationship to other redex nodes
                if (removeThis.size() > 0) {
                    boolean b = mapping.get(redexInQuestion).removeAll(removeThis);
                    assert b;
//                    logger.debug("Remaining agent nodes suitable for replacement: {}", mapping.get(redexInQuestion));
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
    private boolean hasSameSpatialStructure(BigraphEntity<?> agent,
                                            BigraphEntity<?> redex,
                                            boolean noExactMatch) {
        List<BigraphEntity<?>> bigraphEntities = S.get(agent, redex);
        if (Objects.isNull(bigraphEntities)) return false;
        List<BigraphEntity<?>> redexChildren = redexAdapter.getChildren(redex);
        if (noExactMatch && redexChildren.size() == 0 && bigraphEntities.size() != 0) {
            return true;
        }

        //no site is assumed: the degree has to match then
        if (!noExactMatch && redexChildren.size() != agentAdapter.getChildren(agent).size() && bigraphEntities.size() != 0)
            return false;

        for (BigraphEntity<?> eachChild : redexChildren) {
            boolean hasSite = redexAdapter.getChildrenWithSites(eachChild).stream().anyMatch(BigraphEntityType::isSite);
            if (!hasSameSpatialStructure(agent, eachChild, hasSite) && !checkLinkIdentityOfNodes(agent, eachChild)) {  //&& !checkLinkIdentityOfNodes(agent, eachChild))
                return false;
            }
        }
        return true;
    }

    // TODO: UTIL MACHEN: wird häufig verwendet!
    private void setParentOfNode(final BigraphEntity<?> node, final BigraphEntity<?> parent) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        node.getInstance().eSet(prntRef, parent.getInstance()); // child is automatically added to the parent according to the ecore model
    }

    private boolean linkMatching() {
        Set<BigraphEntity<?>> bigraphEntities2 = results.rowMap().keySet();
        Set<BigraphEntity<?>> bigraphEntities = results.columnMap().keySet();
        MutableList<BigraphEntity<?>> allChildrenFromNodeU = Lists.mutable.empty();
        MutableList<BigraphEntity<?>> allChildrenFromNodeV = Lists.mutable.empty();
        for (BigraphEntity<?> eachV : bigraphEntities2) {
            allChildrenFromNodeV.addAll(agentAdapter.getSubtreeOfNode(eachV));
        }
        for (BigraphEntity<?> eachU : bigraphEntities) {
            allChildrenFromNodeU.addAll(redexAdapter.getSubtreeOfNode(eachU));
            allChildrenFromNodeU.add(eachU);
        }
        List<BigraphEntity<?>> uColl = allChildrenFromNodeU.stream().filter(BigraphEntityType::isNode).collect(toList());
        List<BigraphEntity<?>> vColl = allChildrenFromNodeV.stream().filter(BigraphEntityType::isNode).collect(toList());
        return areLinksOK(uColl, vColl);
    }

    private boolean areLinksOK(List<BigraphEntity<?>> redexPartition, List<BigraphEntity<?>> agentPartition) {
        MutableMap<BigraphEntity<?>, Boolean> lnk = Maps.mutable.empty(); //new HashMap<>();
//        Table<BigraphEntity<?>, BigraphEntity<?>, Boolean> lnkTab = HashBasedTable.create();
        for (BigraphEntity<?> v : agentPartition) {
            for (BigraphEntity<?> u : redexPartition) {
//                if (!redexAdapter.isBRoot(u.getInstance()) &&
//                        !agentAdapter.isBRoot(v.getInstance()) &&
//                        isSameControl(u, v)) {
                if (!BigraphEntityType.isRoot(u) && // !redexAdapter.isBRoot(u.getInstance()) &&
                        !BigraphEntityType.isRoot(v) && //!agentAdapter.isBRoot(v.getInstance()) &&
                        isSameControl(u, v)) {

                    boolean linksAreGood = checkLinksForNode(u, v);
                    lnk.putIfAbsent(u, linksAreGood);
                    if (!lnk.get(u) && linksAreGood) {
                        lnk.put(u, true);
                    }
//                    lnkTab.put(u, v, linksAreGood);
                }
            }
        }

        boolean allMatchesAreGood = false;
        if (redexPartition.size() == lnk.size()) {
            allMatchesAreGood = lnk.values().stream().allMatch(x -> x);
        }
        return allMatchesAreGood;
    }

    private boolean checkLinkIdentityOfNodes(BigraphEntity<?> v, BigraphEntity<?> u) {
        List<BigraphEntity<?>> bigraphEntities = S.get(v, u); // redex node list is returned
        if (Objects.isNull(bigraphEntities) || bigraphEntities.size() == 0) return false;

        List<ControlLinkPair> lnkRedex = redexAdapter.getLinksOfNode(u);
        List<ControlLinkPair> lnkAgent = agentAdapter.getLinksOfNode(v);
        //Die Anzahl muss auch stimmen
        if (lnkRedex.size() == 0 && lnkAgent.size() == 0) return true;
        if (lnkRedex.size() == lnkAgent.size()) {
            // inner names are not present, thus they are not checked and 'getNodesOfLink()' is enough
            List<BigraphEntity<?>> collectR = lnkRedex.stream().map(x -> redexAdapter.getNodesOfLink(x.getLink())).flatMap(Collection::stream).collect(toList());
            List<BigraphEntity<?>> collectA = lnkAgent.stream().map(x -> agentAdapter.getNodesOfLink(x.getLink())).flatMap(Collection::stream).collect(toList());
            if (collectA.size() < collectR.size()) {
                return false;
            }
            for (BigraphEntity<?> a : collectR) {
                for (BigraphEntity<?> b : collectA) {
                    List<BigraphEntity<?>> childrenA = agentAdapter.getChildren(b);
                    List<BigraphEntity<?>> childrenR = redexAdapter.getChildren(a);
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

    /**
     * This method checks merely the correct number of links both agent and redex must have in order to be considered equal
     * with respect to their links.
     *
     * @param u redex
     * @param v agent
     * @return true if nodes are equal w.r.t. links
     */
    private boolean checkLinksForNode(BigraphEntity<?> u, BigraphEntity<?> v) {
        List<BigraphEntity<?>> bigraphEntities = S.get(v, u);// corresponding?
        if (Objects.nonNull(bigraphEntities) && bigraphEntities.size() != 0) {

            // Edges mean "closed links" (reaction "denied"), outer are "open links" (reaction permitted)
            // Both outer and edges are considered by method "getLinksOfNode" (same as in bigraphER)
            List<ControlLinkPair> lnkAgent = agentAdapter.getLinksOfNode(v);
            List<ControlLinkPair> lnkRedex = redexAdapter.getLinksOfNode(u);

            //Die Anzahl muss auch stimmen in der angegebenen Reihenfolge, aber bezeichner vom namen ist nicht relevant, muss bei der modellierung beachtet werden
            if ((lnkRedex.size() != 0) == (lnkAgent.size() != 0)) {
//            if (lnkRedex.size() != 0 && lnkAgent.size() != 0) {
                if (lnkRedex.size() != lnkAgent.size()) return false;
                for (int i = 0, n = lnkRedex.size(); i < n; i++) {
                    Collection<BigraphEntity<?>> redexLinksOfEachU = redexAdapter.getPointsFromLink(lnkRedex.get(i).getLink());
                    Collection<BigraphEntity<?>> agentLinksOfEachV = agentAdapter.getPointsFromLink(lnkAgent.get(i).getLink());
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
            return (lnkRedex.size() == 0) == (lnkAgent.size() == 0);
        }
        return false;
    }
}
