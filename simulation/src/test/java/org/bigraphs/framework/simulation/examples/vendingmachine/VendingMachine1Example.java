package org.bigraphs.framework.simulation.examples.vendingmachine;

import com.google.common.cache.LoadingCache;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphDecoder;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystemPredicate;
import org.bigraphs.framework.simulation.examples.BaseExampleTestSupport;
import org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.matching.pure.PureBigraphParametricMatch;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import it.uniud.mads.jlibbig.core.std.Match;
import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.*;
import org.eclipse.emf.compare.match.*;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.EqualityHelper;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VendingMachine1Example extends BaseExampleTestSupport implements BigraphModelChecker.ReactiveSystemListener<PureBigraph> {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/vendingmachine/";

    public VendingMachine1Example() {
        super(TARGET_DUMP_PATH, true);
    }

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
        new File(TARGET_DUMP_PATH + "first/").mkdir();
        new File(TARGET_DUMP_PATH + "second/").mkdir();
    }

    @Test
    @Disabled
    void test_single_rule() throws Exception {
        PureBigraph agent = agent(2, 2, 1);
        printMetaModel(agent);
        eb(agent, "agent", true);

        ReactionRule<PureBigraph> insertCoinRR = insertCoin();
        eb(insertCoinRR.getRedex(), "insertCoinL");
        eb(insertCoinRR.getReactum(), "insertCoinR");

        ReactionRule<PureBigraph> insertCoinRR2 = new ParametricReactionRule<>(insertCoinRR.getReactum(), insertCoinRR.getReactum());


        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(insertCoinRR);

        PureBigraph validReaction = null;
        PureBigraph validReaction2 = null;
        PureBigraph param = null;
        PureBigraph newAgent = null;
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match2 = matcher.match(agent, insertCoinRR);
        Iterator<BigraphMatch<PureBigraph>> iterator2 = match2.iterator();
        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
        if (iterator2.hasNext()) {
            PureBigraphParametricMatch match = (PureBigraphParametricMatch) iterator2.next();
            Match jLibMatchResult = match.getJLibMatchResult();
            PureBigraph context = decoder.decode(jLibMatchResult.getContext(), sig());
            PureBigraph redex = decoder.decode(jLibMatchResult.getRedex(), sig());
            PureBigraph redexImage = decoder.decode(jLibMatchResult.getRedexImage(), sig());
            param = decoder.decode(jLibMatchResult.getParam(), sig());

            eb(context, "context");
            eb(redex, "redex");
            eb(redex, "redexImage");
            eb(param, "param");

            validReaction = ops(redex).compose(param).getOuterBigraph();
            eb(validReaction, "validReaction");

            PureBigraphBuilder<DefaultDynamicSignature> b = PureBigraphBuilder.create(sig(), redex.getMetaModel(), redex.getInstanceModel());
            b.makeGround();
            eb(b.createBigraph(), "redex0");


            newAgent = reactiveSystem.buildParametricReaction(agent, match, insertCoinRR);
            eb(newAgent, "newAgent");
        }
        assert validReaction != null;
        assert newAgent != null;
        assert param != null;

        MatchIterable<BigraphMatch<PureBigraph>> match3 = matcher.match(newAgent, insertCoinRR2);
        Iterator<BigraphMatch<PureBigraph>> iterator3 = match3.iterator();
        if (iterator3.hasNext()) {
            PureBigraphParametricMatch match = (PureBigraphParametricMatch) iterator3.next();
            Match jLibMatchResult = match.getJLibMatchResult();
            PureBigraph context = decoder.decode(jLibMatchResult.getContext(), sig());
            PureBigraph redex = decoder.decode(jLibMatchResult.getRedex(), sig());
            PureBigraph redexImage = decoder.decode(jLibMatchResult.getRedexImage(), sig());
            param = decoder.decode(jLibMatchResult.getParam(), sig());

            eb(context, "context2");
            eb(redex, "redex2");
            eb(param, "param2");

            validReaction2 = ops(redex).compose(param).getOuterBigraph();
            eb(validReaction2, "validReaction2");

            PureBigraphBuilder<DefaultDynamicSignature> b = PureBigraphBuilder.create(sig(), redex.getMetaModel(), redex.getInstanceModel());
            b.makeGround();
            eb(b.createBigraph(), "redex3");

        }

        IEqualityHelperFactory helperFactory = new DefaultEqualityHelperFactory() {
            @Override
            public org.eclipse.emf.compare.utils.IEqualityHelper createEqualityHelper() {
                final LoadingCache<EObject, URI> cache = EqualityHelper.createDefaultCache(getCacheBuilder());
                return new EqualityHelper(cache) {
                    @Override
                    public boolean matchingValues(Object object1, Object object2) {
//                        if (object1 instanceof MyDataType && object2 instanceof MyDataType) {
//                            // custom code
//                        }
                        return super.matchingValues(object1, object2);
                    }
                };
            }
        };

        BigraphFileModelManagement.Store.exportAsInstanceModel(validReaction, new FileOutputStream(TARGET_DUMP_PATH + "first/model.xmi"));
        BigraphFileModelManagement.Store.exportAsInstanceModel(validReaction2, new FileOutputStream(TARGET_DUMP_PATH + "second/model.xmi"));
        // Compare state where match could not be applied and the state where it could be applied
        URI uri1 = URI.createFileURI(TARGET_DUMP_PATH + "first/model.xmi");
        URI uri2 = URI.createFileURI(TARGET_DUMP_PATH + "second/model.xmi");

        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

        ResourceSet resourceSet1 = new ResourceSetImpl();
        ResourceSet resourceSet2 = new ResourceSetImpl();

//        resourceSet1.createResource(uri1);
//        resourceSet2.createResource(uri2);
        resourceSet1.getResource(uri1, true); //.getContents().add(agent.getInstanceModel());
        resourceSet2.getResource(uri2, true); //.getContents().add(validReaction.getInstanceModel());

        IEObjectMatcher matcherEObject = DefaultMatchEngine.createDefaultEObjectMatcher(UseIdentifiers.NEVER);
//        IEObjectMatcher matcherEObject = DefaultMatchEngine.createDefaultEObjectMatcher(UseIdentifiers.WHEN_AVAILABLE);
        IComparisonScope scope = new DefaultComparisonScope(resourceSet2, resourceSet1, null);
        IComparisonFactory comparisonFactory = new DefaultComparisonFactory(helperFactory);
        IMatchEngine.Factory matchEngineFactory = new MatchEngineFactoryImpl(matcherEObject, comparisonFactory);
//        matchEngineFactory.setRanking(10);

        IMatchEngine.Factory.Registry standaloneInstance = MatchEngineFactoryRegistryImpl.createStandaloneInstance();
        standaloneInstance.add(matchEngineFactory);
//        IMatchEngine.Factory.Registry matchEngineRegistry = new MatchEngineFactoryRegistryImpl();
//        matchEngineRegistry.add(matchEngineFactory);


//        Comparison comparison = EMFCompare.builder().build().compare(scope);
        Comparison comparison = EMFCompare.builder()
                .setMatchEngineFactoryRegistry(standaloneInstance)
                .build().compare(scope);


        Map<String, List<org.eclipse.emf.compare.Match>> bfs = bfs(comparison.getMatches().get(0));
        System.out.println(bfs);
        String nodeLabel = "Coin";
        System.out.println(bfs.get(nodeLabel).get(0).eContainer());
        System.out.println(bfs.get(nodeLabel).get(1).eContainer());
        System.out.println("");

        if (bfs.get(nodeLabel).get(0).getLeft() != null && bfs.get(nodeLabel).get(1).getRight() != null) {
            // left is the current state, and right is the previous state
            String parentNow = bfs.get(nodeLabel).get(0).getLeft().eContainer().eClass().getName();
            String parentPrev = bfs.get(nodeLabel).get(1).getRight().eContainer().eClass().getName();
            System.out.println(parentNow + " // " + parentPrev);
            if(parentNow != parentPrev) {
                StringBuilder sb = new StringBuilder("");
                sb.append("In a tree structure, a node with label '").append(parentPrev).append("' has one child node with label '").append(nodeLabel).append("'.");
                sb.append("\r\n");
                sb.append("In a tree structure, a node with label '").append(parentNow).append("' has one child node with label '").append(nodeLabel).append("'.");
                System.out.println(sb.toString());
            }
        }



//        IComparisonScope scope = new DefaultComparisonScope(resourceSet1, resourceSet2, (Notifier)null); //EMFCompare.createDefaultScope(resourceSet1, resourceSet2);
//        Comparison comparison = comparator.compare(scope);
//        System.out.println(comparison);
//        List<Diff> differences = comparison.getDifferences();

//        Predicate<? super Diff> predicate = and(fromSide(DifferenceSource.LEFT), not(hasConflict(ConflictKind.REAL, ConflictKind.PSEUDO)));
// Filter out the differences that do not satisfy the predicate
//        Iterable<Diff> nonConflictingDifferencesFromLeft = filter(comparison.getDifferences(), predicate);
//        Iterator<Diff> iterator = nonConflictingDifferencesFromLeft.iterator();
//        while (iterator.hasNext()) {
//            Diff next = iterator.next();
//            System.out.println(next);
//        }
    }

    public static Map<String, List<org.eclipse.emf.compare.Match>> bfs(org.eclipse.emf.compare.Match match) {
        // Create a queue for BFS
        Queue<org.eclipse.emf.compare.Match> queue = new LinkedList<>();
        Map<String, List<org.eclipse.emf.compare.Match>> map = new HashMap<>();

        List<org.eclipse.emf.compare.Match> significant = new ArrayList<>();

        // Mark the start node as visited and enqueue it
        Set<org.eclipse.emf.compare.Match> visited = new HashSet<>();
        visited.add(match);
        queue.offer(match);

        while (!queue.isEmpty()) {
            // Dequeue a vertex from queue and print it
            org.eclipse.emf.compare.Match vertex = queue.poll();
            System.out.print(vertex + " ");

            // Get all adjacent vertices of the dequeued vertex
            List<org.eclipse.emf.compare.Match> neighbors = StreamSupport.stream(
                    match.getAllSubmatches().spliterator(), false
            ).collect(Collectors.toList());


            // If an adjacent vertex has not been visited, mark it as visited and enqueue it
            for (org.eclipse.emf.compare.Match neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    if (neighbor.getLeft() == null || neighbor.getRight() == null) {
                        if (!neighbor.getAllSubmatches().iterator().hasNext() &&
                                !neighbor.getDifferences().iterator().hasNext()) {
                            if (neighbor.getLeft() != null) {
                                String label = (String) neighbor.getLeft().eClass().getName();
                                map.putIfAbsent(label, new ArrayList<>());
                                map.get(label).add(neighbor);
                            }
                            if (neighbor.getRight() != null) {
                                String label = (String) neighbor.getRight().eClass().getName();
                                map.putIfAbsent(label, new ArrayList<>());
                                map.get(label).add(neighbor);
                            }
                            significant.add(neighbor);
                        }
                    }
                    queue.offer(neighbor);
                }
            }
        }
        return map;
    }

    //TODO: Example path predicate: if rule(1) is executed 2-times coffee was served 2-times, vice versa for tea
    @Test
    void simulate() throws Exception {

        PureBigraph agent = agent(2, 2, 2);
        printMetaModel(agent);
        eb(agent, "agent", false);
        ReactionRule<PureBigraph> insertCoinRR = insertCoin();
        eb(insertCoinRR.getRedex(), "insertCoinL");
        eb(insertCoinRR.getReactum(), "insertCoinR");
//        print(insertCoinRR.getRedex());
//        print(insertCoinRR.getReactum());

        ReactionRule<PureBigraph> pushBtn1 = pushButton1();
        eb(pushBtn1.getRedex(), "pushBtn1L");
        eb(pushBtn1.getReactum(), "pushBtn1R");
//        print(pushBtn1.getRedex());
//        print(pushBtn1.getReactum());
        ReactionRule<PureBigraph> pushBtn2 = pushButton2();
        eb(pushBtn2.getRedex(), "pushBtn2L");
        eb(pushBtn2.getReactum(), "pushBtn2R");
//        print(pushBtn2.getRedex());
//        print(pushBtn2.getReactum());

        ReactionRule<PureBigraph> giveCoffee = giveCoffee();
        eb(giveCoffee.getRedex(), "giveCoffeeL");
        eb(giveCoffee.getReactum(), "giveCoffeeR");
//        print(giveCoffee.getRedex());
//        print(giveCoffee.getReactum());

        ReactionRule<PureBigraph> giveTea = giveTea();
        eb(giveTea.getRedex(), "giveTeaL");
        eb(giveTea.getReactum(), "giveTeaR");
//        print(giveTea.getRedex());
//        print(giveTea.getReactum());

        SubBigraphMatchPredicate<PureBigraph> teaEmpty = teaContainerIsEmpty();
        eb(teaEmpty.getBigraph(), "teaEmpty");
        print(teaEmpty.getBigraph());
        SubBigraphMatchPredicate<PureBigraph> coffeeEmpty = coffeeContainerIsEmpty();
        eb(coffeeEmpty.getBigraph(), "coffeeEmpty");
        print(coffeeEmpty.getBigraph());

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(insertCoinRR);
        reactiveSystem.addReactionRule(pushBtn1);
        reactiveSystem.addReactionRule(pushBtn2);
        reactiveSystem.addReactionRule(giveCoffee);
        reactiveSystem.addReactionRule(giveTea);
        reactiveSystem.addPredicate(coffeeEmpty);
        reactiveSystem.addPredicate(teaEmpty);


        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.DFS,
                opts());
        modelChecker.setReactiveSystemListener(this);
        modelChecker.execute();
//        assertTrue(Files.exists(completePath));
//        assertTrue(carArrivedAtTarget);
    }

    private ModelCheckingOptions opts() {
        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(60)
                        .setMaximumTime(60)
                        .allowReducibleClasses(true)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(completePath.toUri()))
                        .setPrintCanonicalStateLabel(false)
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .create()
                )
        ;
        return opts;
    }

    @Override
    public void onPredicateMatched(PureBigraph currentAgent, ReactiveSystemPredicate<PureBigraph> predicate) {
        System.out.println("pred matched");
    }

    @Override
    public void onAllPredicateMatched(PureBigraph currentAgent, String label) {
        System.out.println("all matched");
    }

    private SubBigraphMatchPredicate<PureBigraph> teaContainerIsEmpty() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig());
        builder.createRoot()
                .addChild("VM").down()
                .addSite()
                .addChild("Container")
                .addChild("Container").down()
                .addChild("Coffee").addSite()
        ;
        PureBigraph bigraph = builder.createBigraph();
        return SubBigraphMatchPredicate.create(bigraph);
    }

    private SubBigraphMatchPredicate<PureBigraph> coffeeContainerIsEmpty() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig());
        builder.createRoot()
                .addChild("VM").down()
                .addSite()
                .addChild("Container")
                .addChild("Container").down()
                .addChild("Tea").addSite()
        ;
        PureBigraph bigraph = builder.createBigraph();
        return SubBigraphMatchPredicate.create(bigraph);
    }

    private PureBigraph agent(int numOfCoffee, int numOfTea, int numOfCoinsPhd) throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> vmB = pureBuilder(sig());
        PureBigraphBuilder<DefaultDynamicSignature> phdB = pureBuilder(sig());

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy containerCoffee = vmB.hierarchy("Container");
        for (int i = 0; i < numOfCoffee; i++) {
            containerCoffee = containerCoffee.addChild("Coffee");
        }
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy containerTea = vmB.hierarchy("Container");
        for (int i = 0; i < numOfTea; i++) {
            containerTea = containerTea.addChild("Tea");
        }
        vmB.createRoot()
                .addChild("VM")
                .down()
                .addChild(containerCoffee.top())
                .addChild(containerTea.top())
                .addChild("Button1")
                .addChild("Button2")
                .addChild("Tresor")
        ;

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy wallet = vmB.hierarchy("Wallet");
        for (int i = 0; i < numOfCoinsPhd; i++) {
            wallet = wallet.addChild("Coin");
        }
        phdB.createRoot().addChild("PHD")
                .down()
                .addChild(wallet.top());


        Placings<DefaultDynamicSignature> placings = purePlacings(sig());
        Placings<DefaultDynamicSignature>.Merge merge2 = placings.merge(2);
        PureBigraph vm = vmB.createBigraph();
        PureBigraph phd = phdB.createBigraph();
        Bigraph<DefaultDynamicSignature> both = ops(vm).parallelProduct(phd).getOuterBigraph();
        Bigraph<DefaultDynamicSignature> result = ops(merge2).compose(both).getOuterBigraph();
        return (PureBigraph) result;
    }

    /**
     * Insert is only possible if no button was pressed
     */
    public ReactionRule<PureBigraph> insertCoin() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DefaultDynamicSignature signature = sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addChild("Coin").addSite()
                .top()
                .addChild("VM").down().addSite().addChild("Button1").addChild("Button2");
        ;
        builder2.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addSite()
                .top()
                .addChild("VM").down().addSite().addChild("Button1").addChild("Button2").addChild("Coin");
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("insertCoin");
        return rr;
    }


    /**
     * TODO: idea show how we can easily change the context (button can only be pressed if PHD has an ID card, or if it is in the safety zone etc.
     * phd must be present; a VM cannot press a button itself
     * For coffee.
     */
    public ReactionRule<PureBigraph> pushButton1() throws Exception {
        DefaultDynamicSignature signature = sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("PHD").down().addSite()
                .top()
                .addChild("VM").down().addChild("Coin").addSite()
                .addChild("Button2")
                .addChild("Button1")
        ;
        builder2.createRoot()
                .addChild("PHD").down().addSite()
                .top()
                .addChild("VM").down().addChild("Coin").addSite()
                .addChild("Button2")
                .addChild("Button1").down().addChild("Pressed");
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    /**
     * phd must be present; a VM cannot press a button itself.
     * for tea.
     */
    public ReactionRule<PureBigraph> pushButton2() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DefaultDynamicSignature signature = sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("PHD").down().addSite()
                .top()
                .addChild("VM").down().addChild("Coin").addSite()
                .addChild("Button1")
                .addChild("Button2");
        ;
        builder2.createRoot()
                .addChild("PHD").down().addSite()
                .top()
                .addChild("VM").down().addChild("Coin").addSite()
                .addChild("Button1")
                .addChild("Button2").down().addChild("Pressed")
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    /**
     * Several things happen:
     * Check that button was pressed;
     * check that enough money was inserted <- customization opportunity for user
     * check if coffee is available
     * <p>
     * give the rest of the money back
     * put the rest in the tresor
     * release button
     */
    public ReactionRule<PureBigraph> giveCoffee() throws Exception {
        DefaultDynamicSignature signature = sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addSite()
                .top()
                .addChild("VM").down()
                .addChild("Coin").addSite()
                .addChild("Container").down().addChild("Coffee").addSite().up()
                .addChild("Button1").down().addChild("Pressed").up()
                .addChild("Tresor").down().addSite();
        ;
        builder2.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addChild("Coffee").addSite()
                .top()
                .addChild("VM").down()
                .addSite()
                .addChild("Container").down().addSite().up()
                .addChild("Button1")
                .addChild("Tresor").down().addChild("Coin").addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    public ReactionRule<PureBigraph> giveTea() throws Exception {
        DefaultDynamicSignature signature = sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addSite()
                .top()
                .addChild("VM").down()
                .addChild("Coin").addSite()
                .addChild("Container").down().addChild("Tea").addSite().up()
                .addChild("Button2").down().addChild("Pressed").up()
                .addChild("Tresor").down().addSite();
        ;
        builder2.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addChild("Tea").addSite()
                .top()
                .addChild("VM").down()
                .addSite()
                .addChild("Container").down().addSite().up()
                .addChild("Button2")
                .addChild("Tresor").down().addChild("Coin").addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        ((ParametricReactionRule)rr).withPriority(0);
        return rr;
    }

    private DefaultDynamicSignature sig() {
        DynamicSignatureBuilder sb = pureSignatureBuilder();
        DefaultDynamicSignature sig = sb
                .addControl("Coin", 0)
                .addControl("VM", 0)
                .addControl("Button1", 0)
                .addControl("Button2", 0)
                .addControl("Pressed", 0)
                .addControl("Coffee", 0)
                .addControl("Container", 0)
                .addControl("Tea", 0)
                .addControl("PHD", 0)
                .addControl("Wallet", 0)
                .addControl("Tresor", 0)
                .create();
        return sig;
    }
}
