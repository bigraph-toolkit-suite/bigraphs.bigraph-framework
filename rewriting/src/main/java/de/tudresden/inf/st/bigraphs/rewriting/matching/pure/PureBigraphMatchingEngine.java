package de.tudresden.inf.st.bigraphs.rewriting.matching.pure;

import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ContextIsNotActive;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.rewriting.matching.AbstractDynamicMatchAdapter;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatchingEngine;
import de.tudresden.inf.st.bigraphs.rewriting.util.Permutations;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Matching algorithm for pure bigraphs.
 *
 * @author Dominik Grzelak
 */
public class PureBigraphMatchingEngine implements BigraphMatchingEngine<PureBigraph> {

    private PureBigraphRedexAdapter redexAdapter;
    private PureBigraphAgentAdapter agentAdapter;
    private Table<BigraphEntity, BigraphEntity, List<BigraphEntity>> S = HashBasedTable.create();
    private AtomicInteger totalHits;
    private Table<BigraphEntity, BigraphEntity, Integer> results;

    private List<BigraphEntity> internalVertsG;
    private List<BigraphEntity> allVerticesOfH;
    private List<PureBigraphParametricMatch> matches = new LinkedList<>();

    private HashMap<BigraphEntity, List<Integer>> hitsVIx = new HashMap<>();

    private MutableBuilder<DefaultDynamicSignature> builder;
    private MutableBuilder<DefaultDynamicSignature> builder2;


    private Stopwatch timer0;

    PureBigraphMatchingEngine(PureBigraph agent, PureBigraph redex) {
        //signature, ground agent
        this.redexAdapter = new PureBigraphRedexAdapter(redex);
        this.agentAdapter = new PureBigraphAgentAdapter(agent);
        Stopwatch timer = Stopwatch.createStarted();
        this.init();
        long elapsed = timer.stop().elapsed(TimeUnit.NANOSECONDS);
        System.out.println("INITTIME (millisecs) " + (elapsed / 1e+6f));
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
            System.out.println("\tControl: " + entity.getControl() + " @ " + ((BigraphEntity.NodeEntity) entity).getName());
        } else if (BigraphEntityType.isRoot(entity)) {
            System.out.println("\tRoot @ r_" + ((BigraphEntity.RootEntity) entity).getIndex());
        }
    }


    /**
     * Computes all matches
     * <p>
     * First, structural matching, afterwards link matching
     */
    public void beginMatch() {

        timer0 = Stopwatch.createStarted();
        List<List<BigraphEntity>> partitionSets = new ArrayList<>();
//        List<BigraphEntity> uSetAfterMatching = new ArrayList<>();
//        agentAdapter.getAllInternalVerticesPostOrderAsStream().forEach(eachV -> {

//        });
        for (BigraphEntity eachV : internalVertsG) {
            List<BigraphEntity> childrenOfV = agentAdapter.getChildren(eachV);
            List<BigraphEntity> u_vertsOfH = new ArrayList<>(allVerticesOfH);//TODO: create this in a stream-filter
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
                    cs = isSameControl(eachU, eachV); //eachU.getControl().equals(eachV.getControl());
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
                if (redexAdapter.isRoot(eachU.getInstance())) { //if the current element is a root then automatically a "site" is inferred
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
                // update map S
//                if (uSetAfterMatching.size() != 0) {
                S.put(eachV, eachU, uSetAfterMatching);
//                    S.get(eachV, eachU).addAll(uSetAfterMatching);
//                }
                if (S.get(eachV, eachU).contains(eachU)) {

                    if (!eachU.getType().equals(BigraphEntityType.ROOT)) {
//                        System.out.println("Kein root...");
                        continue;
                    }
                    int i = redexAdapter.getRoots().indexOf(eachU);
//                    if (treffer.get() < redexAdapter.getRoots().size())
                    if (results.get(eachV, eachU) == null)
                        results.put(eachV, eachU, i);
//                    boolean isgoodcontrols = checkSubtreesControl(bigraphAdapter, eachV, redexAdapter, eachU, 0); //TODO prüfen ob mit site gemacht werden kann
//                    System.out.println("Is good? => " + isgoodcontrols);
//                    if (!isgoodcontrols) continue;
//                    if (matchings3.contains(false)) continue;

                    boolean b = linkMatching();

                    if (b) {
                        int i1 = totalHits.incrementAndGet();
                        //save last eachU and eachV
                        // mapped redex root index zu agent node
                        hitsVIx.putIfAbsent(eachV, new LinkedList<>());
                        hitsVIx.get(eachV).add(i);

                        System.out.println("FOUND A MATCHING: Agent=" + eachV.getControl() + " and Redex=" + eachU.getControl() + " // Root_ix = " + i);
//                            System.out.println("Children of U");
//                            redexAdapter.getChildren(eachU).forEach(x -> System.out.println(x.getControl()));
//                            System.out.println("Children of V");
//                            agentAdapter.getChildren(eachV).forEach(x -> System.out.println(x.getControl()));
                    }
                }
            }
        }

//        });

        System.out.println("Treffer=" + totalHits.get());
        if (hasMatched()) {
            return;
        }
    }

    public void createMatchResult() {
        long elapsed0 = timer0.stop().elapsed(TimeUnit.NANOSECONDS);
        System.out.println("MATCHING TIME 1 " + (elapsed0 / 1e+6f));
        timer0.start();
        // This is needed when there is not a distinct match of the redices roots to a agent's subtree
        // Überprüfen ob die indexlisten disjoint sind dann ist das eindeutig (bzw. es sollte nur ein index dort drin stehn)
        // ansonsten nicht und es gibt mehrere Möglichkeiten die Redex' roots auf die gematchten agent nodes
        // zu legen
        // Dann kann man permutieren: agent node einem root aus dem redex zuweisen


        // ich muss zwischen allen paaren testen.
//        Permutations.of(Arrays.asList(0, 1, 0, 1)).forEach(p -> { p.forEach(System.out::print); System.out.print(" "); });

        //hier beginnt schon die kombination: ab hier weiß man wie viele occurrences-bedingte "mehr"-transitionen es gibt

        final int numOfRoots = redexAdapter.getRoots().size();

        LinkedList<BigraphEntity> collect = new LinkedList<>(hitsVIx.keySet());

        final boolean redexRootMatchIsUnique = numOfRoots == totalHits.get(); //collect.size();
        System.out.println("Disjoint redex root matches=" + redexRootMatchIsUnique);

        //First, build all possible combinations of redex index to agent node matching
        List<Integer[]> combination = new LinkedList<>();

        if (redexRootMatchIsUnique) { //identity mapping: mapping is distinct and unique
            Integer[] comb = new Integer[numOfRoots];
            for (int i = 0; i < numOfRoots; i++) {
                comb[i] = i;
            }
            combination.add(comb);
        } else {
            // is a combinatorial problem: is a anordnungs problem, ohne wiederholung
            //for every redex root, where are the matches?
//            List<Integer> nums = IntStream.rangeClosed(0, collect.size() - 1).boxed().collect(Collectors.toList());
//            combination = Combination.combination(nums, new HashSet<>(collect).size());

            //hier kann noch verbessert werden (ungueltige kombinationen werden auch aussortiert). der kombinationsraum
            // kann womöglich hier schon verkleinert werden
            List<Integer> nums2 = IntStream.range(0, numOfRoots).boxed().collect(Collectors.toList());
            Permutations.of(nums2).forEach(p -> {
                combination.add(p.toArray(Integer[]::new));
            });
        }

        //generic loop for unique and non-unique matching of the redex root to agent's node
        int validCounter = 0;
        for (Integer[] eachCombination : combination) { // for every combination
            //redex root to agent node
            BiMap<Integer, BigraphEntity> hitsV_new = HashBiMap.create();

            for (int i = 0; i < eachCombination.length; i++) {
                int tmpRootIx = eachCombination[i];
                BigraphEntity agentNode = collect.get(i);
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
                System.out.println("NOT VALID=" + Arrays.toString(eachCombination));
                continue;
            } else {
                System.out.println("IS VALID=" + Arrays.toString(eachCombination));
                validCounter++;
            }

            //Parameter: <redex root index, agent node, redex node>
            Table<Integer, BigraphEntity, BigraphEntity> hitsV_newChildren = HashBasedTable.create();

            // Iterate through the current redex root
            // The structure of the Map.Entry: redex root index -> matched agent node
            for (Map.Entry<Integer, BigraphEntity> eachEntry : hitsV_new.entrySet()) {
                int redexRootIx = eachEntry.getKey();
                BigraphEntity agentMatch = eachEntry.getValue();
                List<BigraphEntity> childrenOfAgent = agentAdapter.getChildren(agentMatch);
                List<BigraphEntity> childrenOfRedex = redexAdapter.getChildren(redexAdapter.getRoots().get(redexRootIx));
                Map<BigraphEntity, List<BigraphEntity>> nodeDiffByControlCheck = findOccurrences(childrenOfAgent, childrenOfRedex, true);

                List<BigraphEntity> availableAgentMatchNodes = new LinkedList<>();//nodeDiffByControlCheck.values().stream().flatMap(x -> x.stream()).collect(Collectors.toList());

                //TODO find occurrences: new combinatorial problem
                // buildmatch method below must then be called in a new loop multiple times to gather all occurrences
                //current impl: select the first possible agent node
                for (Map.Entry<BigraphEntity, List<BigraphEntity>> eachMapping : nodeDiffByControlCheck.entrySet()) {
                    //TODO make selection random and not only the first (or take all at best - clear availableAgentMatchNodes at the end of the outer loop and call buildMatches before)
                    for (BigraphEntity x : eachMapping.getValue()) {
                        if (!availableAgentMatchNodes.contains(x)) {
                            hitsV_newChildren.put(redexRootIx, x, eachMapping.getKey());
                            availableAgentMatchNodes.add(x);
                            break;
                        }
                    }
                }
                //add result to allCombinations later
            }

            //TODO: make the following faster: this step tooks most of the time
            try {
                //structure of hitsV_newChildren: {redex root index -> {agentNode -> redexNode}} (are unique mappings here)
                buildMatch(hitsV_new, hitsV_newChildren);
            } catch (ContextIsNotActive contextIsNotActive) {
                contextIsNotActive.printStackTrace();
            }


        }
        System.out.println("Valid combinations: " + validCounter);

        elapsed0 = timer0.stop().elapsed(TimeUnit.NANOSECONDS);
        System.out.println("MATCHING TIME 2 " + (elapsed0 / 1e+6f));
    }

    /**
     * Checks if any match could be found and also if _all_ redex roots could be matched.
     *
     * @return {@code true}, if a correct match could be found, otherwise {@code false}
     */
    public boolean hasMatched() {
        return totalHits.get() > 0 && totalHits.get() >= redexAdapter.getRoots().size() && (totalHits.get() % redexAdapter.getRoots().size() == 0);
    }

    /**
     * Constructs the context and the parameters of the redex to build the "redex image".
     *
     * @param hitsV_newChildren redex root ix, agent node, redex node
     */
    private void buildMatch(BiMap<Integer, BigraphEntity> hitsV, Table<Integer, BigraphEntity, BigraphEntity> hitsV_newChildren) throws ContextIsNotActive {
//        if (Objects.isNull(this.builder)) {
        this.builder = PureBigraphBuilder.newMutableBuilder(agentAdapter.getSignature());
        this.builder2 = PureBigraphBuilder.newMutableBuilder(agentAdapter.getSignature());
//        }

        // Compute the context, and the parameters
        Map<Integer, Bigraph> parameters = new LinkedHashMap<>();
        PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> pureBigraphFactory =
                AbstractBigraphFactory.createPureBigraphFactory();

        //when no params are necessary then a barren is all that is needed
        boolean needsParameters = redexAdapter.getSites().size() >= 1;
        if (needsParameters) {
            redexAdapter.getSites().forEach(x -> parameters.put(x.getIndex(), pureBigraphFactory.createPlacings(redexAdapter.getSignature()).barren()));
        }

        BiMap<BigraphEntity.NodeEntity, BigraphEntity.NodeEntity> matchDict = HashBiMap.create();

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

            BigraphEntity newNode = null;
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
                for (BigraphEntity agentNext : childrenOfCurrentAgentParent) {
                    if (hitsV_newChildren.containsColumn(agentNext)) {
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
                        iterateThroughChildren(redexEntityCorrespondence, agentNext, parameters, matchDict);
//                        }
                        savedRedexNodes.add(agentNext);
                    }
                }
                // childrenOfCurrentAgentParent: contains only contain agent nodes which couldn't be match (i.e., which shall be preserved)
                while (iterator.hasNext()) {
                    if (savedRedexNodes.contains(iterator.next())) {
                        iterator.remove();
                    }
                }

                //rest of the available nodes remains and the savedRedexNodes goes into the blocking list
                //and all their children
                savedRedexNodes.forEach(x -> {
                    blockNodesForContext.add(x);
                    blockNodesForContext.addAll(agentAdapter.getChildrenOf(x)); //these are the parameters for the current root index..
                });


                // now we are left only with the remaining children precluding the redex match at the current level (height of the tree)
                for (BigraphEntity eachRemaining : childrenOfCurrentAgentParent) {
                    BigraphEntity.NodeEntity eachRemaining0 = (BigraphEntity.NodeEntity) eachRemaining;
                    BigraphEntity.NodeEntity newNode1 = (BigraphEntity.NodeEntity) builder.createNewNode(eachRemaining0.getControl(), eachRemaining0.getName());
                    newNodes.put(newNode1.getName(), newNode1);
                    setParentOfNode(newNode1, newNode);
                }
                //get the root index of the corresponding redex match node eachU
                BigraphEntity.SiteEntity newSite = (BigraphEntity.SiteEntity) builder.createNewSite(rootIx);
                newSites.put(newSite.getIndex(), newSite);
                setParentOfNode(newSite, newNode);

            }

            List<AbstractDynamicMatchAdapter.ControlLinkPair> linksOfNode = agentAdapter.getLinksOfNode(eachNodeV);
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
                    BigraphEntity.OuterName newOuterName = newOuterNames.values().stream()
                            .filter(outerName -> outerName.getName().equals(link.getName()))
                            .findFirst()
                            .orElse(builder.createOuterName(link.getName()));
                    //EDIT
                    newOuterNames.put(newOuterName.getName(), newOuterName);
                    builder.connectNodeToOuterName((BigraphEntity.NodeEntity) newNode, newOuterName);
                }
            }
        }

        // outer names in d must not included in outernames of R: die names kommen direkt aus dem agent und dürfen gleichen namen haben
//        builder.closeAllInnerNames();

        PureBigraph context = new PureBigraph(builder.new InstanceParameter(
                builder.getLoadedEPackage(),
                agentAdapter.getSignature(),
                Collections.singletonMap(0, newRootCtx), // roots
                newSites,
                newNodes,
                newInnerNames, newOuterNames, builder.getCreatedEdges()));
        builder.reset();

        ElementaryBigraph<DefaultDynamicSignature> identityForParams;
        Linkings<DefaultDynamicSignature> linkings = pureBigraphFactory.createLinkings(agentAdapter.getSignature());
        if (parameters.size() == 0) {
            // if we have no parameters the "renaming" link graph is empty
            identityForParams = linkings.identity_e();
        } else {
            // build the "renaming link graph" from the given parameters
            List<StringTypedName> names = (List<StringTypedName>) parameters.values().stream().map(x -> x.getOuterNames())
                    .flatMap(x -> x.stream())
                    .map(x -> StringTypedName.of(((BigraphEntity.OuterName) x).getName()))
                    .collect(Collectors.toList());
//            System.out.println(names);
            if (names.size() > 0) {
                identityForParams = linkings.identity(names.toArray(new StringTypedName[0]));
            } else {
                identityForParams = linkings.identity_e();
            }
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
                        .filter(BigraphEntityType::isPort).map(x -> agentAdapter.getNodeOfPort((BigraphEntity.Port) x)).collect(Collectors.toList());
                substitutionLinkingGraph.putIfAbsent(eachAgentOuterName.getName(), new ArrayList<>());
                for (BigraphEntity.NodeEntity each : pointsFromLink) {
                    BigraphEntity.NodeEntity matchedRedex = matchDict.get(each);
                    if (Objects.isNull(matchedRedex)) continue;
                    LinkedList<AbstractDynamicMatchAdapter.ControlLinkPair> linksOfNode = redexAdapter.getLinksOfNode(matchedRedex);
                    List<String> names = linksOfNode.stream().map(x -> ((BigraphEntity.OuterName) x.getLink()).getName()).collect(Collectors.toList());
                    for (String eachName : names) {
                        if (!alreadyUsedNamesOfNode.contains(eachName)) {
                            substitutionLinkingGraph.get(eachAgentOuterName.getName()).add(eachName);
                            alreadyUsedNamesOfNode.add(eachName);
                        }
                    }
                }
            }

            for (Map.Entry<String, List<String>> each : substitutionLinkingGraph.entrySet()) {
                List<StringTypedName> tmp = each.getValue().stream().map(StringTypedName::of).collect(Collectors.toList());

                if (tmp.size() == 0) {
                    tmp.add(StringTypedName.of(each.getKey()));
                }
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
//        linkings.closure(closeInnerNames.get(0));
        //OHNE parameter ist die 1-1 correspondence einfach herauszufinden: einfach alle kinder matchen

        List<Integer> collect = context.getSites().stream().collect(Collectors.toMap(BigraphEntity.SiteEntity::getIndex, s -> context.isActiveAtSite(s.getIndex())))
                .entrySet().stream().filter(k -> !k.getValue()).map(k -> k.getKey()).collect(Collectors.toList());
        if (collect.size() != 0) {
            throw new ContextIsNotActive(collect.stream().mapToInt(i -> i).toArray());

        }

        // parameters are only needed when RR contains sites, otherwise they contain just a barren or are empty
        //see e.g., \cite[p.75]{elsborg_bigraphs_2009}
        PureBigraphParametricMatch m = new PureBigraphParametricMatch(
                context,
                redexAdapter.getBigraphDelegate(),
                parameters.values(),
                identityForParams,
                identityForContext
        );
        matches.add(m);
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
                                        Map<Integer, Bigraph> parameters,
                                        BiMap<BigraphEntity.NodeEntity, BigraphEntity.NodeEntity> matchDict) {
        boolean hasSite = redexAdapter.getChildrenWithSites(redex).stream().anyMatch(BigraphEntityType::isSite);
        List<BigraphEntity> redexChildren = redexAdapter.getChildren(redex);

        Map<BigraphEntity, BigraphEntity> distinctMatch = new ConcurrentHashMap<>();
        List<BigraphEntity> agentChildren = agentAdapter.getChildren(agent);
        Map<BigraphEntity, List<BigraphEntity>> occurrences = findOccurrences(agentChildren, redexChildren, hasSite);
        for (Map.Entry<BigraphEntity, List<BigraphEntity>> redexAgentMapping : occurrences.entrySet()) {
            //get any value from the value list: here we just take the first one (order of the list)
            for (BigraphEntity correspondence : redexAgentMapping.getValue()) {
                if (agentChildren.contains(correspondence) && distinctMatch.get(redexAgentMapping.getKey()) == null) {
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
        System.out.println("Rest: " + agentChildren);
        if (hasSite) {
            // Alles in dieser ebene als parameter wegpacken
            // Jeder ast wird dann noch einmal durchlaufen: mit den correspondences

            BigraphEntity.SiteEntity entity = (BigraphEntity.SiteEntity) redexAdapter.getChildrenWithSites(redex).stream()
                    .filter(BigraphEntityType::isSite).findFirst().get();
            int siteIndex = entity.getIndex();
            System.out.println(siteIndex);

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
            iterateThroughChildren(eachEntry.getKey(), eachEntry.getValue(), parameters, matchDict);
        }
    }

    /**
     * TODO here is space for improvement, maybe the S table can be used here.
     * <p>
     * After a match is found, we need to compute the occurrences of the redex inside the match.
     * <p>
     * It may happen that not only a distinct match is found but multiple ones.
     * (We then need to use all these when a rewrite is done!)
     * <p>
     * Beginning from a redex node, the whole subtree + links will checked to find possible ones.
     * The search space is here "dramatically" reduced since we only have to look into the provided agent nodes.
     * <p>
     * The method only returns the "first level" nodes of the matches and not the whole subtree.
     *
     * @param agentNodes a collection of agent nodes
     * @param redexNodes a collection of redex nodes
     * @return a map where the key is the redex and the value is a list of possible agent matches
     */
    public Map<BigraphEntity, List<BigraphEntity>> findOccurrences(Collection<BigraphEntity> agentNodes, Collection<BigraphEntity> redexNodes, boolean withSitesNoExactMatch) {
        Map<BigraphEntity, List<BigraphEntity>> mapping = new HashMap<>();
        List<BigraphEntity> agents = new ArrayList<>(agentNodes);
        //Liste durchgehen von redexNodes
        //in 2. schleifen die agent nodes die noch übrig sind
        //prüfen: control, degree (eins abziehen wegen root, wenn site in children vorhanden dann darf mehr vorhanden sein, ansonsten gleich)
        for (BigraphEntity eachRedex : redexNodes) { //degree checking is done in hasSameSpatialStructure() method
            System.out.println("Checking Redex: " + ((BigraphEntity.NodeEntity<Control>) eachRedex).getName());
            for (int i = agents.size() - 1; i >= 0; i--) {
                BigraphEntity eachAgent = agents.get(i);
                System.out.println("\tChecking against agent: " + ((BigraphEntity.NodeEntity<Control>) eachAgent).getName());
//                boolean b = hasSameSpatialStructure(eachAgent, eachRedex, withSitesNoExactMatch) && isSameControl(eachAgent, eachRedex);

                boolean hasSite = redexAdapter.getChildrenWithSites(eachRedex).stream().anyMatch(BigraphEntityType::isSite);
                boolean b = isSameControl(eachAgent, eachRedex) && hasSameSpatialStructure3(eachAgent, eachRedex, hasSite); //hasSameSpatialStructure(eachAgent, eachRedex, true);
//                boolean bigraphEntities1 = checkLinkIdentityOfNodes(eachAgent, eachRedex);
                boolean big1 = checkLinksForNode(eachRedex, eachAgent); // this is a very basic/simply link checking, later we do something more concretely
                //this is just to pre-filter
                if (b) {
                    if (big1) {
                        mapping.putIfAbsent(eachRedex, new ArrayList<>());
                        mapping.get(eachRedex).add(eachAgent);
                    }
                }
            }
        }

        //TODO check if we need to check all links from all children or "first-level" is enough (currently, we check the whole subbigraph)
        // check of cross-links
        BiMap<BigraphEntity, List<CrossPairLink>> crossings2 = HashBiMap.create();
        if (redexNodes.size() > 1) {
            for (Map.Entry<BigraphEntity, List<BigraphEntity>> each : mapping.entrySet()) {
                // only for those redexes which have multiple possible  matchings
                if (each.getValue().size() <= 1) continue;
                BigraphEntity redexCurrent = each.getKey();
                List<BigraphEntity> rest = mapping.entrySet().stream().filter(x -> !x.getKey().equals(each.getKey()))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                // First get all linked nodes under the current redex (a subbigraph)
                // For that we get _all_ children of the current redex first, and then gather all their links.
                // they are somehow "grouped" through this 2D list structure
                List<List<BigraphEntity>> redexLinks = getSubBigraphFrom(redexCurrent, redexAdapter).stream().flatMap(x -> redexAdapter.getLinksOfNode(x).stream())
                        .map(x -> redexAdapter.getNodesOfLink((BigraphEntity.Link) x.getLink()))
                        .collect(Collectors.toList());
                System.out.println(redexLinks);

                //ist alles was ich unter der liste finde, der parent redexCurrent?
                for (List<BigraphEntity> nodes : redexLinks) {
                    System.out.println(nodes);
                    List<Map.Entry<BigraphEntity, Boolean>> result =
                            nodes.stream()
                                    .collect(Collectors.toMap(p -> p, p -> redexAdapter.isParentOf(p, redexCurrent)))
                                    .entrySet().stream()
                                    .filter(x -> x.getValue() == false)
                                    .collect(Collectors.toList());
                    System.out.println(result);
                    if (result.size() == 0) continue;
                    //für "false": finde heraus welcher redex parent node
                    List<CrossPairLink> pairLinks = new ArrayList<>();
                    rest.forEach(x -> result.forEach(y -> {
                        if (redexAdapter.isParentOf(y.getKey(), x)) {
                            pairLinks.add(CrossPairLink.create(x, y.getKey()));
                        }
                    }));
                    for (CrossPairLink eachCross : pairLinks) {
                        crossings2.putIfAbsent(redexCurrent, new ArrayList<>());
                        crossings2.get(redexCurrent).add(eachCross);
                    }
                }
            }

            //key is the redex node, the value(attr: other): both are connected via a link
            for (Map.Entry<BigraphEntity, List<CrossPairLink>> eachRedexCrossing : crossings2.entrySet()) {
                HashSet<BigraphEntity> removeThis = new HashSet<>();
                BigraphEntity redexInQuestion = eachRedexCrossing.getKey();
                System.out.println("Checking for current redex: " + redexInQuestion);
                List<BigraphEntity> possibleAgentNodes = mapping.get(redexInQuestion);
                System.out.println("\twith the possibleAgentNodes: " + possibleAgentNodes);

                // check here all possible agent matches
                // for all possibleAgentNodes do:
                //      for all eachRedex.getValue() do:
                for (BigraphEntity eachPossibleAgent : possibleAgentNodes) {
                    // the current eachPossibleAgent must fit for all other "redex crossings" in relation to the "redexInQuestion"
                    System.out.println("::now observing agent entity: " + eachPossibleAgent);
                    for (CrossPairLink otherRedexPair : eachRedexCrossing.getValue()) {
                        List<BigraphEntity> otherRedexAgents = mapping.get(otherRedexPair.getRedex());//for which redex?
                        System.out.println("\t::redex entity: " + otherRedexPair.getRedex() + " genauer: " + otherRedexPair.getOther());


                        //now: find the corresponding agent nodes of otherRedexPair.getOther()
                        // check if redexInQuestion is connected to the corresponding findings of otherRedexPair.getOther()
                        List<BigraphEntity> allChildrenOf = getSubBigraphFrom(otherRedexAgents.get(0), agentAdapter)
                                .stream().filter(x -> agentAdapter.getPortCount(x) > 0)
                                .filter(x -> S.get(x, otherRedexPair.getOther()) != null && S.get(x, otherRedexPair.getOther()).size() > 0)
                                .filter(x -> agentAdapter.areConnected((BigraphEntity.NodeEntity) eachPossibleAgent, (BigraphEntity.NodeEntity) x))
                                .collect(Collectors.toList());
                        System.out.println("\t::Are connected: " + allChildrenOf);
                        if (allChildrenOf.size() == 0) {
                            System.out.println("\t::removing " + eachPossibleAgent + " because it and its children have no connection");
                            removeThis.add(eachPossibleAgent);
                        } else {
                            System.out.println("\t::keeping " + eachPossibleAgent);
                        }
                    }
                }
                // Remove the previously matched agent that do not fit because of the redex node relationship to other redex nodes
                if (removeThis.size() > 0) {
                    boolean b = mapping.get(redexInQuestion).removeAll(removeThis);
                    assert b;
                    System.out.println("removed: " + b);
                }
            }
        }
        return mapping;
    }

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

    private boolean hasSameSpatialStructure2(BigraphEntity agent, BigraphEntity redex) {
//        if(!S.columnMap().get(redex).containsValue(agent)) {
        if (S.get(agent, redex).size() == 0) {
//        if (!S.rowMap().get(agent).containsKey(redex)) {
            return false;
        }
        return true;
//        List<BigraphEntity> bigraphEntities = S.get(agent, redex);
//        if (Objects.isNull(bigraphEntities)) return false;
//        List<BigraphEntity> children = redexAdapter.getChildren(redex);
////        if (bigraphEntities.size() > 0) {
//        for (BigraphEntity eachChild : children) {
////                boolean hasSite = redexAdapter.getChildrenWithSites(eachChild).stream().anyMatch(BigraphEntityType::isSite);
//            if (!hasSameSpatialStructure2(agent, eachChild)) { // && !checkLinkIdentityByNodes(agent, eachChild)) {
//                return false;
//            }
//        }
//        return true;
//        } else {
//            return false;
//        }
    }

    /**
     * find the "trace" of the subtree of the redex in the agent
     * No links are checked
     *
     * @param agent
     * @param redex
     * @param noExactMatch flag that assumes a site as sibling of the redex if set to {@code true}
     * @return
     */
    private boolean hasSameSpatialStructure(BigraphEntity agent, BigraphEntity redex, boolean noExactMatch) {
        List<BigraphEntity> bigraphEntities = S.get(agent, redex);
        if (Objects.isNull(bigraphEntities)) return false;
        List<BigraphEntity> children = redexAdapter.getChildren(redex);
        if (noExactMatch && children.size() == 0 && bigraphEntities.size() != 0) return true;
        //no site is assumed: the degree has to match then
        if (!noExactMatch && children.size() != agentAdapter.getChildren(agent).size() && bigraphEntities.size() != 0)
            return false;
        if (bigraphEntities.size() > 0) {
            for (BigraphEntity eachChild : children) {
//                TODO: CHANGED: hasSiteVar added and passed..
                boolean hasSite = redexAdapter.getChildrenWithSites(eachChild).stream().anyMatch(BigraphEntityType::isSite);
                if (!hasSameSpatialStructure(agent, eachChild, hasSite) /*&& !checkLinkIdentityOfNodes(agent, eachChild) */) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    //with link checking
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
//        if (bigraphEntities.size() > 0) {
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
            allChildrenFromNodeV.addAll(agentAdapter.getAllChildrenFromNode(eachV));
        }
        for (BigraphEntity eachU : bigraphEntities) {
            allChildrenFromNodeU.addAll(redexAdapter.getAllChildrenFromNode(eachU));
            allChildrenFromNodeU.add(eachU);
        }
        boolean areLinksOK = areLinksOK(allChildrenFromNodeU, allChildrenFromNodeV);
        return areLinksOK;
    }


    private boolean areLinksOK(List<BigraphEntity> redexPartition, List<BigraphEntity> agentPartition) {
        //TODO only arity > 0
        HashMap<BigraphEntity, Boolean> lnk = new HashMap<>();
        Table<BigraphEntity, BigraphEntity, Boolean> lnkTab = HashBasedTable.create();
        for (BigraphEntity v : agentPartition) {
            for (BigraphEntity u : redexPartition) {
                if (!redexAdapter.isRoot(u.getInstance()) &&
                        !agentAdapter.isRoot(v.getInstance()) &&
                        isSameControl(u, v)) {
                    boolean linksAreGood = checkLinksForNode(u, v);
                    lnk.put(u, linksAreGood);
                    lnkTab.put(u, v, linksAreGood);
                }
            }
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
        System.out.println("New link matching result: " + b);
        System.out.println("old link matching result: " + !lnk.containsValue(false));
//        return b;
//        return !lnk.containsValue(false);
        return b || !lnk.containsValue(false);
    }

    //TODO make return more clear
    // checks only the given nodes if they are equal
    //inner names gibt es nicht und müssen auch nicht betrachtet werden (thus, getNodesOfLink is enough)
    private boolean checkLinkIdentityOfNodes(BigraphEntity v, BigraphEntity u) {
//        System.out.println("Node-ID (R): " + ((BigraphEntity.NodeEntity) u).getName());
//        System.out.println("Node-ID (A): " + ((BigraphEntity.NodeEntity) v).getName());
        List<BigraphEntity> klo = new LinkedList<>();
        List<BigraphEntity> bigraphEntities = S.get(v, u); // redex node list is returned
        if (Objects.isNull(bigraphEntities) || bigraphEntities.size() == 0) return false;


        List<AbstractDynamicMatchAdapter.ControlLinkPair> lnkRedex = redexAdapter.getLinksOfNode(u);
        List<AbstractDynamicMatchAdapter.ControlLinkPair> lnkAgent = agentAdapter.getLinksOfNode(v);
        //Die Anzahl muss auch stimmen
        if (lnkRedex.size() == 0 && lnkAgent.size() == 0) return true;
        if (lnkRedex.size() == lnkAgent.size()) {
            List<BigraphEntity> collectR = lnkRedex.stream().map(x -> redexAdapter.getNodesOfLink((BigraphEntity.OuterName) x.getLink())).flatMap(x -> x.stream()).collect(Collectors.toList());
            List<BigraphEntity> collectA = lnkAgent.stream().map(x -> agentAdapter.getNodesOfLink((BigraphEntity.OuterName) x.getLink())).flatMap(x -> x.stream()).collect(Collectors.toList());
//            if (collectR.size() != collectA.size()) return false;
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

            //Die Anzahl muss auch stimmen in der angegebenen Reihenfolge, aber bezeichner v. name ist nicht relevant
            if (lnkRedex.size() != 0 && lnkAgent.size() != 0) {
                if (lnkRedex.size() != lnkAgent.size()) return false;
                for (int i = 0, n = lnkRedex.size(); i < n; i++) {
                    List<BigraphEntity> agentLinksOfEachV = agentAdapter.getNodesOfLink((BigraphEntity.Link) lnkAgent.get(i).getLink());
                    List<BigraphEntity> redexLinksOfEachU = redexAdapter.getNodesOfLink((BigraphEntity.Link) lnkRedex.get(i).getLink());
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
            return true;
        }
        return false;
    }

}
