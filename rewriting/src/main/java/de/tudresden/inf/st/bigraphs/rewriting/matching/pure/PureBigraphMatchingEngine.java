package de.tudresden.inf.st.bigraphs.rewriting.matching.pure;

import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ContextIsNotActive;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.matching.AbstractDynamicMatchAdapter;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.rewriting.matching.PureBigraphParametricMatch;
import de.tudresden.inf.st.bigraphs.rewriting.util.Combination;
import de.tudresden.inf.st.bigraphs.rewriting.util.Permutations;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PureBigraphMatchingEngine<B extends PureBigraph> {

    private PureBigraphRedexAdapter redexAdapter;
    private PureBigraphAgentAdapter agentAdapter;
    private Table<BigraphEntity, BigraphEntity, List<BigraphEntity>> S = HashBasedTable.create();
    private AtomicInteger treffer;
    private Table<BigraphEntity, BigraphEntity, Integer> results;
    private int itcnt; //not necessary
    private List<BigraphEntity> internalVertsG;
    private List<BigraphEntity> allVerticesOfH;

    private List<BigraphMatch<B>> matches = new LinkedList<>();

//    @Deprecated
//    private HashMap<Integer, BigraphEntity> hitsU = new HashMap<>();
    /**
     * The agent's match (node) is stored along the corresponding redex' root index of the match
     * (if multiple occurrences were possible)
     * <p>
     * Describes which root index of the redex matches which agent node
     */
//    @Deprecated
//    private HashMap<Integer, BigraphEntity> hitsV = new HashMap<>();


//    @Deprecated
    private HashMap<BigraphEntity, List<Integer>> hitsVIx = new HashMap<>();

    //Integer: redex ix | redex (root) <-> agent nodes that corresponds to the redex nodes at redex root ix
    //<root ix, map<Redexnode under root, list<agent nodes>>
    @Deprecated
    Map<Integer, Map<BigraphEntity, List<BigraphEntity>>> occurrenceTable = new HashMap<>();
//    private Table<Integer, > occurrenceTable;

    private final MutableBuilder<DefaultDynamicSignature> builder;
    private final MutableBuilder<DefaultDynamicSignature> builder2;
    private final MutableBuilder<DefaultDynamicSignature> bLinking;

    PureBigraphMatchingEngine(B agent, B redex) throws IncompatibleSignatureException {
//        this.agent = agent;
//        this.redex = redex;
        //TODO: validate
        //TODO: check if sites are active
        //signature, ground agent
        this.builder = PureBigraphBuilder.newMutableBuilder(agent.getSignature());
        this.builder2 = PureBigraphBuilder.newMutableBuilder(agent.getSignature());
        this.bLinking = PureBigraphBuilder.newMutableBuilder(agent.getSignature());
        this.redexAdapter = new PureBigraphRedexAdapter(redex);
        this.agentAdapter = new PureBigraphAgentAdapter(agent);
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
        itcnt = 0;
        treffer = new AtomicInteger(0);
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
//            System.out.println("V: ");
//            printName(eachV);
//            System.out.println("Children: ");
//            childrenOfV.forEach(this::printName);
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
//                List<BigraphEntity> childrenWithSitesOfU = redexAdapter.getChildrenWithSites(eachU);
                boolean hasSite = false;
                if (redexAdapter.isRoot(eachU.getInstance())) { //if the current element is a root then automatically a "site" is inferred
                    hasSite = true;
                } else {
                    hasSite = redexAdapter.getChildrenWithSites(eachU).stream().anyMatch(BigraphEntityType::isSite);
//                    for (BigraphEntity eachSibOfU : redexAdapter.getChildrenWithSites(eachU)) {
//                        if (eachSibOfU.getType().equals(BigraphEntityType.SITE)) {
//                            hasSite = true;
//                            break;
//                        }
//                    }
                }

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

                    int i = redexAdapter.getRoots().indexOf(eachU);
                    if (!eachU.getType().equals(BigraphEntityType.ROOT)) {
//                        System.out.println("Kein root...");
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
                        // im model_0 beispiel, könnten wir hier schon aufhören: D.h: how to detect symmetries??


                        int i1 = treffer.incrementAndGet();

                        //<root ix, map<Redexnode under root, list<agent nodes>>
//                        Map<BigraphEntity, List<BigraphEntity>> nodeDiffByControlCheck = findOccurrences(childrenOfV, neighborsOfU, true);
////                        Map<Integer, List<BigraphEntity>> occs = new HashMap<>();
//                        occurrenceTable.putIfAbsent(i, new HashMap<>());
////                        occurrenceTable.get(i).putAll(nodeDiffByControlCheck); //this doesn't work if ambiguity in node matching exists (e.g., a symmetric bigraph)
//                        for (Map.Entry<BigraphEntity, List<BigraphEntity>> eachEntry : nodeDiffByControlCheck.entrySet()) {
//                            if (occurrenceTable.get(i).get(eachEntry.getKey()) == null) {
//                                occurrenceTable.get(i).putAll(nodeDiffByControlCheck);
//                                break;
//                            } else {
//                                occurrenceTable.get(i).get(eachEntry.getKey()).addAll(eachEntry.getValue());
//                            }
//                        }

                        // mapped redex root index zu agent node
                        hitsVIx.putIfAbsent(eachV, new LinkedList<>());
                        hitsVIx.get(eachV).add(i);

                        //save last eachU and eachV
                        // check if we found a matching across all roots of the redex
                        // both following statements are equivalent
                        boolean wereAllRootsVisited = true; //i == redexAdapter.getRoots().size() - 1;
                        boolean wereAllRootsVisited2 = true; //(i1) % redexAdapter.getRoots().size() == 0;
                        if (wereAllRootsVisited2 && wereAllRootsVisited) {

                            System.out.println("FOUND A MATCHING: Agent=" + eachV.getControl() + " and Redex=" + eachU.getControl() + " // Root_ix = " + i);
//                            System.out.println("Children of U");
//                            redexAdapter.getChildren(eachU).forEach(x -> System.out.println(x.getControl()));
//                            System.out.println("Children of V");
//                            agentAdapter.getChildren(eachV).forEach(x -> System.out.println(x.getControl()));

                            // TODO for iterator style: save a breakpoint here somehow... aber nur wenn für alle redex roots
                            // ein distinct agent node zugewiesen werden konnte
                            // save the current child of internalVertsG and u_vertsOfH and all intermediate results
                            // do not call init() again
                            // create a new method beginMatch() which accepts arguments to begin at these stored positions
                            //  and is called later from the iterator after the first match
//                            hitsU.clear();
//                            hitsV.clear();
                        }
                    }
                }
            }
        }

        System.out.println("itcnt = " + itcnt);
        System.out.println("Treffer=" + treffer.get());
        if (treffer.get() == 0) {
            return;
        }

        // This is needed when there is not a distinct match of the redices roots to a agent's subtree
        // Überprüfen ob die indexlisten disjoint sind dann ist das eindeutig (bzw. es sollte nur ein index dort drin stehn)
        // ansonsten nicht und es gibt mehrere Möglichkeiten die Redex' roots auf die gematchten agent nodes
        // zu legen
        // Dann kann man permutieren: agent node einem root aus dem redex zuweisen


        // ich muss zwischen allen paaren testen.
//        Permutations.of(Arrays.asList(0, 1, 0, 1)).forEach(p -> { p.forEach(System.out::print); System.out.print(" "); });

        //TODO: mit der occurrence map umschreiben: da sind nun alle kombinationen enthalten

        //hier beginnt schon die kombination: ab hier weiß man wie viele occurrences-bedingte "mehr"-transitionen es gibt
//            hitsV.clear();
//        HashMap<Integer, BigraphEntity> hitsV = new HashMap<>();
        final int numOfRoots = redexAdapter.getRoots().size();
//        hitsV.clear();

//            areDisjoint = redexAdapter.getRoots().size() == collect.size();
//            System.out.println("Disjoint2=" + areDisjoint);

        //parent nodes should be in order corresponding to the redex indices order
//        List<BigraphEntity> collectChildren = IntStream.range(0, numOfRoots).boxed().map(ix -> occurrenceTable.get(ix))
//                .flatMap(x -> x.values().stream())
//                .flatMap(Collection::stream)
//                .collect(Collectors.toList());
//        LinkedList<BigraphEntity> collect = collectChildren.stream()
//                .map(x -> agentAdapter.getParent(x))
//                .distinct()
//                .collect(Collectors.toCollection(LinkedList::new));//
        LinkedList<BigraphEntity> collect = new LinkedList<>(hitsVIx.keySet());// collectChildren.stream()
//                .map(x -> agentAdapter.getParent(x))
//                .distinct()
//                .collect(Collectors.toCollection(LinkedList::new));

//        List<BigraphEntity> childMatches = occurrenceTable.values().stream()
//                .flatMap(x -> x.keySet().stream())
//                .collect(Collectors.toList());

//        boolean childMatchesAreUnique = new HashSet<>(collectChildren).size() == childMatches.size();

//        boolean areDisjoint =
//                occurrenceTable.values().stream()
//                        .allMatch(bigraphEntityListMap -> bigraphEntityListMap.entrySet().stream()
//                                .allMatch(entry2 -> entry2.getValue().size() == 1));
        boolean redexRootMatchIsUnique = numOfRoots == treffer.get(); //collect.size();
        System.out.println("Disjoint redex root matches=" + redexRootMatchIsUnique);
//        System.out.println("Unique redex<-> agent child matches=" + childMatchesAreUnique);

        //First, build all possible combinations of redex index to agent node matching
        List<Integer[]> combination = new LinkedList<>();

        if (redexRootMatchIsUnique) { //identity mapping
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

        int validCounter = 0;
        for (Integer[] eachCombination : combination) { // for every combination
            //redex root to agent node
            BiMap<Integer, BigraphEntity> hitsV_new = HashBiMap.create();
//            HashMap<Integer, BigraphEntity> hitsV_new = new HashMap<>();

            for (int i = 0; i < eachCombination.length; i++) {
                int tmpRootIx = eachCombination[i];
                BigraphEntity agentNode = collect.get(i);
                if (!hitsVIx.get(agentNode).contains(tmpRootIx)) {
                    break;
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

//            if (true) continue;

            //redex root ix, agent node, redex node
            Table<Integer, BigraphEntity, BigraphEntity> hitsV_newChildren = HashBasedTable.create();
//            List<Table<Integer, BigraphEntity, BigraphEntity>> allCombinations = new LinkedList<>();
            boolean childMatchesAreUnique = false;
            System.out.println("create child combination's for given root constellation ...");

            // iterate through the current redex root ix -> agent match
            for (Map.Entry<Integer, BigraphEntity> eachEntry : hitsV_new.entrySet()) {
                int redexRootIx = eachEntry.getKey();
                BigraphEntity agentMatch = eachEntry.getValue();
                List<BigraphEntity> childrenOfAgent = agentAdapter.getChildren(agentMatch);
                List<BigraphEntity> childrenOfRedex = redexAdapter.getChildren(redexAdapter.getRoots().get(redexRootIx));
                Map<BigraphEntity, List<BigraphEntity>> nodeDiffByControlCheck = findOccurrences(childrenOfAgent, childrenOfRedex, true);
                Map<Integer, List<BigraphEntity>> occs = new HashMap<>();
                childMatchesAreUnique = nodeDiffByControlCheck.values().size() == childrenOfRedex.size();
                for (Map.Entry<BigraphEntity, List<BigraphEntity>> eachMapping : nodeDiffByControlCheck.entrySet()) {
                    hitsV_newChildren.put(redexRootIx, eachMapping.getKey(), eachMapping.getValue().get(0));
//                    if (occurrenceTable.get(i).get(eachEntry.getKey()) == null) {
//                        occurrenceTable.get(i).putAll(nodeDiffByControlCheck);
//                        break;
//                    } else {
//                        occurrenceTable.get(i).get(eachEntry.getKey()).addAll(eachEntry.getValue());
//                    }
                }
                //TODO find occurrences: new combinatorial problem
                // buildmatch must then be called in a new loop
                //if(multiple) {
//            }

                //add result to allCombinations later

//                    occurrenceTable.putIfAbsent(i, new HashMap<>());
////                        occurrenceTable.get(i).putAll(nodeDiffByControlCheck); //this doesn't work if ambiguity in node matching exists (e.g., a symmetric bigraph)
//                    for (Map.Entry<BigraphEntity, List<BigraphEntity>> eachEntry : nodeDiffByControlCheck.entrySet()) {
//                        if (occurrenceTable.get(i).get(eachEntry.getKey()) == null) {
//                            occurrenceTable.get(i).putAll(nodeDiffByControlCheck);
//                            break;
//                        } else {
//                            occurrenceTable.get(i).get(eachEntry.getKey()).addAll(eachEntry.getValue());
//                        }
//                    }
            }

//            if(!childMatchesAreUnique) {
//            }

            try {
                buildMatch(hitsV_new, hitsV_newChildren);
            } catch (ContextIsNotActive contextIsNotActive) {
                contextIsNotActive.printStackTrace();
            }


        }
        System.out.println("Valid combinations: " + validCounter);
    }

    /**
     * After a match is found, we need to compute the occurrences of the redex inside the match
     * <p>
     * It can be possible that not a distinct match is possible but multiple ones.
     * (we then need to use all these when a rewrite is done!)
     * <p>
     * Beginning from a redex node, the whole subtree + links will searched and checked to find possible ones.
     * The search space is here reduces since we only look up into the provided agent nodes.
     * //TODO here is space for improvement, maybe the S table can be used here.
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
        for (int i = agents.size() - 1; i >= 0; i--) {
            BigraphEntity eachAgent = agents.get(i);
            for (BigraphEntity eachRedex : redexNodes) { //degree checking is done in theSame() method
                boolean b = theSame(eachAgent, eachRedex, withSitesNoExactMatch) && isSameControl(eachAgent, eachRedex);
                boolean linksAreMatching = checkLinkIdentityByNodes(eachAgent, eachRedex);
                if (b && linksAreMatching) {
                    mapping.putIfAbsent(eachRedex, new ArrayList<>());
                    mapping.get(eachRedex).add(eachAgent);
                }
            }
        }
        return mapping;
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
    private boolean theSame(BigraphEntity agent, BigraphEntity redex, boolean noExactMatch) {
        List<BigraphEntity> bigraphEntities = S.get(agent, redex);
        if (Objects.isNull(bigraphEntities)) return false;
        List<BigraphEntity> children = redexAdapter.getChildren(redex);
        if (noExactMatch && children.size() == 0 && bigraphEntities.size() != 0) return true;
        //no site is assumed: the degree has to match then
        if (!noExactMatch && children.size() != agentAdapter.getChildren(agent).size() && bigraphEntities.size() != 0)
            return false;
        if (bigraphEntities.size() > 0) {
            for (BigraphEntity eachChild : children) {
                if (!theSame(agent, eachChild, noExactMatch) && !checkLinkIdentityByNodes(agent, eachChild)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }


    private boolean isSameControl(BigraphEntity node1, BigraphEntity node2) {
        return node1.getControl().equals(node2.getControl());
    }

    /**
     * Constructs the context and the parameters of the redex to build the "redex image".
     */
    private void buildMatch(BiMap<Integer, BigraphEntity> hitsV, Table<Integer, BigraphEntity, BigraphEntity> hitsV_newChildren) throws ContextIsNotActive {
        //calculate the context
        //the parameters etc.
        Map<Integer, Bigraph> parameters = new LinkedHashMap<>();
        PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> pureBigraphFactory =
                AbstractBigraphFactory.createPureBigraphFactory();
        boolean needsParameters = redexAdapter.getSites().size() >= 1;
        //when no params are necessary then a barren is all that is needed
        if (needsParameters) {
            redexAdapter.getSites().forEach(x -> parameters.put(x.getIndex(), pureBigraphFactory.createPlacings().barren()));
        }

        Supplier<String> paramOuterNameSupplier = new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                System.out.println("id is " + id);
                return "d_" + id++;
            }
        };

        //context: replace the agent with sites at eachV and if edge exists, make inner name

        //first: create roots
        //should only have one root!
//        Optional<BigraphEntity.RootEntity> first = agentAdapter.getRoots().stream().findFirst();
//        assert first.isPresent();
        final BigraphEntity.RootEntity newRootCtx = (BigraphEntity.RootEntity) builder.createNewRoot(0); //first.get().getIndex());
        final Map<Integer, BigraphEntity.SiteEntity> newSites = new HashMap<>();
        final Map<String, BigraphEntity.NodeEntity> newNodes = new HashMap<>();
        final Map<String, BigraphEntity.OuterName> newOuterNames = new HashMap<>();
        final Map<String, BigraphEntity.InnerName> newInnerNames = new HashMap<>();


        final Map<String, BigraphEntity.OuterName> newOuterNamesL = new HashMap<>(); // für link graph
        final Map<String, BigraphEntity.InnerName> newInnerNamesL = new HashMap<>(); // für place graph

        // to build the identity graph later for the redex
        HashMap<BigraphEntity.OuterName, List<String>> substitutionLinkingGraph = new HashMap<>();
        //recreate nodes that are in V exclusive U
        //where first u node is found replace with site
        //this should build the node hierarchy and put places under those nodes where the redex matched


        //TODO: use hitsV_newChildren
        Set<BigraphEntity> availableAgentNodes = new ArrayList<>(occurrenceTable.values())
                .stream().map(Map::values)
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        // nodes that are not anymore available when a redex is matched to an agent node
        Set<BigraphEntity> blockNodes = new HashSet<>();
        //redex -> agent
        Map<BigraphEntity, BigraphEntity> correspondences = new LinkedHashMap<>();

        // Recreate idle outer names in the agent for the context: the redex don't need to be checked, as it
        // cannot have idle outer names. It is "simple".
        if (!agentAdapter.isEpimorphic()) {
            agentAdapter.getOuterNames().stream().filter(x -> agentAdapter.getPointsFromLink(x).size() == 0).forEach(link -> {
                BigraphEntity.OuterName newOuterName = builder.createOuterName(link.getName());
                newOuterNames.put(newOuterName.getName(), newOuterName);
            });
        }

        for (BigraphEntity eachNodeV : agentAdapter.getAllVerticesBfsOrder()) {

            // skip blocked children first (children that are in the redex match
            //this part will be executed after the top-level nodes of the redex were matched with the corresponding agent nodes
            if (blockNodes.contains(eachNodeV)) {
                //put all their children inside
                blockNodes.addAll(agentAdapter.getChildrenOf(eachNodeV));
//                blockNodes.addAll(agentAdapter.getAllChildrenFromNode(eachNodeV));
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

            //abstract the rest away and put children into blocked list
            boolean hasAmatch = hitsV.values().contains(eachNodeV);
            if (hasAmatch) { // && agentAdapter.getChildrenOf(eachNodeV).size() >= 1) {

                //TODO: that part below must be supplied from outside: use hitsV_newChildren
                //  don't use occurrenceTable

                Integer rootIx = hitsV.entrySet().stream().filter(x -> x.getValue().equals(eachNodeV)).findFirst().get().getKey();
                Collection<BigraphEntity> childrenOfCurrentAgentParent = agentAdapter.getChildrenOf(eachNodeV);

                List<BigraphEntity> savedRedexNodes = new ArrayList<>();
                // it's possible that a redex node can be matched to multipe agent nodes. Sometimes there is no
                // exact match possible because the redex is not "tight" enough
                // if multiple occurrences are found, then the transition system must span a new branch
                //TODO:start Permutations bilden (oder anfangs nur 1. index wählen: das sind alles verschiedene states beim rewriting später
                //  How to count the number of possible combiniation which equals to the number of new branches in the LTS
                //  we have the rootIx: every

                Map<BigraphEntity, List<BigraphEntity>> redexToAgentMapping = occurrenceTable.get(rootIx); //key: redex nodes -> list of possible agent nodes
                //Re-collect all agent nodes first
//                Set<BigraphEntity> availableAgentNodes = redexToAgentMapping.values().stream()
//                        .flatMap(Collection::stream).collect(Collectors.toSet());
                List<BigraphEntity> allChildrenOfAgent = new LinkedList<>();
                for (Map.Entry<BigraphEntity, List<BigraphEntity>> redexAgentMapping : redexToAgentMapping.entrySet()) {
                    //get any value from the value list: here we just take the first one (order of the list)
                    for (BigraphEntity correspondence : redexAgentMapping.getValue()) {
                        if (availableAgentNodes.contains(correspondence)) {
                            savedRedexNodes.add(correspondence);
                            correspondences.put(redexAgentMapping.getKey(), correspondence);
                            allChildrenOfAgent.addAll(agentAdapter.getAllChildrenFromNode(correspondence));
                            //remove the set of available nodes to take
                            availableAgentNodes.remove(correspondence);
                            childrenOfCurrentAgentParent.remove(correspondence); //and reduce also the set of agent's children at this step
                            // hier ist es "egal" wie die parameter hineingelegt werden, das ändert die transitionen nicht
                            // nur die "erste ebene", da kann es mehr occurrences geben
                            if (needsParameters)
                                iterateThroughChild(redexAgentMapping.getKey(), correspondence, parameters, paramOuterNameSupplier);
                            break;
                        }
                    }
                }
                // TODO:end

                //rest of the available nodes remains and the savedRedexNodes goes into the blocking list
                //and all their children
                savedRedexNodes.forEach(x -> {
                    blockNodes.add(x);
                    blockNodes.addAll(agentAdapter.getChildrenOf(x)); //these are the parameters for the current root index..
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

                // create the parameter bigraph for the redex at the site
                System.out.println("create parameters ...");
                //find the first site at this level
                //the rest

//                TODO: do something with: getAllChildrenFrom.... and built this structure as parameter for the site
//                createParameter(allChildrenOfAgent, correspondences, parameters, paramOuterNameSupplier);
            }

            //TODO collect also outernames
            //  28.5.19:
            //  Make EDGES TO INNERNAMES (OK) BUT
            //  make alle links to innernames (?)


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
                Collections.singletonMap(0, newRootCtx),
                newSites,
                newNodes,
                newInnerNames, newOuterNames, builder.getCreatedEdges()));
        builder.reset();

//        if (!context.isActive()) {
        List<Integer> collect = context.getSites().stream().collect(Collectors.toMap(BigraphEntity.SiteEntity::getIndex, s -> context.isActiveAtSite(s.getIndex())))
                .entrySet().stream().filter(k -> !k.getValue()).map(k -> k.getKey()).collect(Collectors.toList());
        if (collect.size() != 0) {
            throw new ContextIsNotActive(collect.stream().mapToInt(i -> i).toArray());

        }
//        }

        //This takes a lot if time!
        System.out.println("Create png's");
        Stopwatch timer = Stopwatch.createStarted();
        try {
            String convert = GraphvizConverter.toPNG(context,
                    true,
                    new File("src/test/resources/graphviz/context.png")
            );
//            System.out.println(convert);
            GraphvizConverter.toPNG(agentAdapter,
                    true,
                    new File("src/test/resources/graphviz/agent.png")
            );
            GraphvizConverter.toPNG(redexAdapter,
                    true,
                    new File("src/test/resources/graphviz/redex.png")
            );
            parameters.keySet().forEach(x -> {
                try {
                    GraphvizConverter.toPNG(parameters.get(x),
                            true,
                            new File("src/test/resources/graphviz/param_" + x + ".png")
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            long elapsed = timer.stop().elapsed(TimeUnit.MILLISECONDS);
            System.out.println("Create png's took (millisecs) " + elapsed);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // parameters are only needed when RR contains sites, otherwise they contain just a barren
        //see e.g., \cite[p.75]{elsborg_bigraphs_2009}
        PureBigraphParametricMatch m = new PureBigraphParametricMatch(
                context,
                redexAdapter.getBigraphDelegate(),
                parameters.values(),
                null
        );
        matches.add((BigraphMatch<B>) m);
    }

    private void createNamesForNodeInParam(BigraphEntity.NodeEntity newNode,
                                           BigraphEntity agentNode,
                                           Map<String, BigraphEntity.OuterName> outerNames,
                                           Supplier<String> outerNameSupplier) {
        LinkedList<AbstractDynamicMatchAdapter.ControlLinkPair> linksOfNode = agentAdapter.getLinksOfNode(agentNode);
        for (AbstractDynamicMatchAdapter.ControlLinkPair each : linksOfNode) {
            if (BigraphEntityType.isOuterName(each.getLink())) {
//                String name = ((BigraphEntity.OuterName) each.getLink()).getName();
                BigraphEntity.OuterName newOuterName = builder2.createOuterName(outerNameSupplier.get() + "_o");
                builder2.connectNodeToOuterName(newNode, newOuterName);
                outerNames.put(newOuterName.getName(), newOuterName);
            } else if (BigraphEntityType.isEdge(each.getLink())) {
//                String name = ((BigraphEntity.Edge) each.getLink()).getName();
                BigraphEntity.OuterName newOuterName = builder2.createOuterName(outerNameSupplier.get() + "_e");
                builder2.connectNodeToOuterName(newNode, newOuterName);
                outerNames.put(newOuterName.getName(), newOuterName);
            }
        }
    }

    // corresponding parts
    private void iterateThroughChild(BigraphEntity redex, BigraphEntity agent, Map<Integer, Bigraph> parameters, Supplier<String> outerNameSupplier) {
        List<BigraphEntity> redexChildren = redexAdapter.getChildren(redex);
        boolean hasSite = redexAdapter.getChildrenWithSites(redex).stream().anyMatch(BigraphEntityType::isSite);

        Map<BigraphEntity, BigraphEntity> distinctMatch = new ConcurrentHashMap<>();
        List<BigraphEntity> agentChildren = agentAdapter.getChildren(agent);
        Map<BigraphEntity, List<BigraphEntity>> occurrences = findOccurrences(agentChildren, redexChildren, hasSite);
        for (Map.Entry<BigraphEntity, List<BigraphEntity>> redexAgentMapping : occurrences.entrySet()) {
            //get any value from the value list: here we just take the first one (order of the list)
            for (BigraphEntity correspondence : redexAgentMapping.getValue()) {
                if (agentChildren.contains(correspondence)) {
                    agentChildren.remove(correspondence);
                    distinctMatch.put(redexAgentMapping.getKey(), correspondence);
                    continue;
                }
            }
        }
        System.out.println("Rest: " + agentChildren);
        if (hasSite) {
            //alles in dieser ebene als parameter wegpacken
            // jeder ast wird dann noch einmal durchlaufen: mit den correspondences

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
                    createNamesForNodeInParam(np, xx, paramOuterNames, outerNameSupplier);
                }
                List<BigraphEntity> children = agentAdapter.getChildren(xx);
                for (BigraphEntity entity2 : children) {
                    if (paramNodes.get(((BigraphEntity.NodeEntity) entity2).getName()) == null) {
                        BigraphEntity.NodeEntity newNode1 = (BigraphEntity.NodeEntity) builder2
                                .createNewNode(entity2.getControl(), ((BigraphEntity.NodeEntity) entity2).getName());
                        setParentOfNode(newNode1, np);
                        createNamesForNodeInParam(newNode1, entity2, paramOuterNames, outerNameSupplier);
                        paramNodes.put(newNode1.getName(), newNode1);
                    }
                }
                return children;
            });
            Lists.newArrayList(paramTraverser.breadthFirst(agentChildren));
            PureBigraph paramBigraph = new PureBigraph(
                    builder2.new InstanceParameter(
                            builder2.getLoadedEPackage(),
                            agentAdapter.getSignature(),
                            Collections.singletonMap(0, rootParam),
                            Collections.EMPTY_MAP,
                            paramNodes,
                            Collections.EMPTY_MAP,
                            paramOuterNames,
                            builder2.getCreatedEdges()
                    )
            );
            builder2.reset();
            parameters.put(siteIndex, paramBigraph);
        }

        // wenn keine site da ist, dann die anderen treffer durchgehen, bis alles fertig ist
        //dann gibt es keine parameter
        for (Map.Entry<BigraphEntity, BigraphEntity> eachEntry : distinctMatch.entrySet()) {
            iterateThroughChild(eachEntry.getKey(), eachEntry.getValue(), parameters, outerNameSupplier);
        }


    }

    private void setParentOfNode(final BigraphEntity node, final BigraphEntity parent) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        node.getInstance().eSet(prntRef, parent.getInstance()); // child is automatically added to the parent according to the ecore model
    }

    public List<BigraphMatch<B>> getMatches() {
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
//            allChildrenFromNodeV.remove(eachV);
        }
        for (BigraphEntity eachU : bigraphEntities) {
            allChildrenFromNodeU.addAll(redexAdapter.getAllChildrenFromNode(eachU));
            allChildrenFromNodeU.add(eachU);
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
        for (BigraphEntity v : paritionAgent) {
            for (BigraphEntity u : paritionRedex) {
                if (!redexAdapter.isRoot(u.getInstance()) && !agentAdapter.isRoot(v.getInstance()) &&
                        u.getControl().equals(v.getControl())) {
                    boolean linksAreGood = checkLinksForNode(u, v);
                    lnk.put(u, linksAreGood);
                }
            }
        }
//        System.out.println(paritionAgent.size() * paritionRedex.size());
        return !lnk.containsValue(false);
    }

    //inner names gibt es nicht und müssen auch nicht betrachtet werden (thus, getNodesOfLink is enough)
    private boolean checkLinkIdentityByNodes(BigraphEntity v, BigraphEntity u) {
        List<BigraphEntity> bigraphEntities = S.get(v, u);//corresponding?
        if (Objects.isNull(bigraphEntities)) return false;
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

    /**
     * helper function
     *
     * @param x
     * @param y
     * @return
     */
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

    /**
     * helper function
     *
     * @return
     */
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
