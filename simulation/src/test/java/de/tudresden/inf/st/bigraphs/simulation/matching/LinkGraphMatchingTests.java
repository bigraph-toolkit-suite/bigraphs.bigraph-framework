package de.tudresden.inf.st.bigraphs.simulation.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphArtifacts;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.simulation.ReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.examples.BaseExampleTestSupport;
import de.tudresden.inf.st.bigraphs.simulation.exceptions.BigraphSimulationException;
import de.tudresden.inf.st.bigraphs.simulation.matching.pure.IHSFilter;
import de.tudresden.inf.st.bigraphs.simulation.matching.pure.SubHypergraphIsoSearch;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.BigraphModelChecker;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.PureBigraphModelChecker;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.impl.PureReactiveSystem;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dominik Grzelak
 */
public class LinkGraphMatchingTests extends BaseExampleTestSupport implements BigraphModelChecker.ReactiveSystemListener<PureBigraph> {
    private static PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/bpmtest/framework/";

    public LinkGraphMatchingTests() {
        super(TARGET_DUMP_PATH, true);
    }

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    @Override
    public void onAllPredicateMatched(PureBigraph currentAgent, String label) {
    }

    @Test
    void paper_example_test() throws Exception {
        PureBigraph queryLinkGraph = createQueryLinkGraph();
        eb(queryLinkGraph, "query");
        assertEquals(2, queryLinkGraph.getEdges().size());
        BigraphEntity.Edge e0 = queryLinkGraph.getEdges().stream().filter(x -> x.getName().equals("e0")).findFirst().get();
        BigraphEntity.Edge e1 = queryLinkGraph.getEdges().stream().filter(x -> x.getName().equals("e1")).findFirst().get();
        assertEquals(3, queryLinkGraph.getPointsFromLink(e0).size());
        assertEquals(3, queryLinkGraph.getPointsFromLink(e1).size());
        BigraphArtifacts.exportAsInstanceModel(queryLinkGraph, System.out);

        PureBigraph dataLinkGraph = createDataLinkGraph();
        eb(dataLinkGraph, "dataLinkGraph");
        assertEquals(4, dataLinkGraph.getEdges().size());
        e0 = dataLinkGraph.getEdges().stream().filter(x -> x.getName().equals("e0")).findFirst().get();
        e1 = dataLinkGraph.getEdges().stream().filter(x -> x.getName().equals("e1")).findFirst().get();
        BigraphEntity.Edge e2 = dataLinkGraph.getEdges().stream().filter(x -> x.getName().equals("e2")).findFirst().get();
        BigraphEntity.Edge e3 = dataLinkGraph.getEdges().stream().filter(x -> x.getName().equals("e3")).findFirst().get();
        assertEquals(3, dataLinkGraph.getPointsFromLink(e0).size());
        assertEquals(3, dataLinkGraph.getPointsFromLink(e1).size());
        assertEquals(3, dataLinkGraph.getPointsFromLink(e1).size());
        assertEquals(3, dataLinkGraph.getPointsFromLink(e1).size());
        BigraphArtifacts.exportAsInstanceModel(dataLinkGraph, System.out);


        SubHypergraphIsoSearch search = new SubHypergraphIsoSearch(queryLinkGraph, dataLinkGraph);

        search.embeddings();

        System.out.println(search.getCandidates());
        assertEquals(1, search.getEmbeddingSet().size());
        SubHypergraphIsoSearch.Embedding next = search.getEmbeddingSet().iterator().next();
        assertTrue(next.entrySet().stream().allMatch(x -> x.getKey().getName().equals(x.getValue().getName())));
    }

    // compare also with the bigrapher output
    // our occurrence counts are doubled because we do not "remove" symmetries
    @Test
    void subhypergraphisosearch() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraph agent = createAgent();
        List<PureBigraph> redexes = new ArrayList<>();
        redexes.add(createRedex1());
        redexes.add(createRedex2());
        redexes.add(createRedex3());
        redexes.add(createRedex4());
        int[] numOfEmbeddings = {12, 0, 0, 2}; // without
        eb(agent, "agent");
        eb(redexes.get(0), "redex4");
        for (int i = 0; i < redexes.size(); i++) {
            PureBigraph redex = redexes.get(i);
            SubHypergraphIsoSearch search = new SubHypergraphIsoSearch(redex, agent);
            search.embeddings();
            System.out.println(search.getCandidates());
            System.out.println(search.getEmbeddingSet());
            assertEquals(numOfEmbeddings[i], search.getEmbeddingSet().size());
        }
    }

    @Test
    void matchTest_redex4() throws InvalidConnectionException, TypeNotExistsException, IOException, InvalidReactionRuleException, BigraphSimulationException, IncompatibleInterfaceException {
        PureBigraph agent = createAgent();
        PureBigraph redex = createRedex4();
        eb(agent, "agent");
        eb(redex, "redex4");

        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agent, redex);
        Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
        int transition = 0;
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            createGraphvizOutput(agent, next, TARGET_DUMP_PATH + "redex4/" + (transition) + "/");
            transition++;
        }
        assertEquals(2, transition);
    }

    @Test
    void modelCheckerTest_redex4_test() throws InvalidConnectionException, TypeNotExistsException, BigraphSimulationException, InvalidReactionRuleException {
        PureBigraph agent = createAgent();
        PureBigraph redex = createRedex4();
        eb(agent, "agent");
        eb(redex, "redex4");

        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(50)
                        .setMaximumTime(60)
                        .allowReducibleClasses(false)
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

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(createReactionRule4());
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationType.BREADTH_FIRST,
                opts);
//        modelChecker.setReactiveSystemListener(this);
        modelChecker.execute();
        assertTrue(Files.exists(completePath));
    }

    @Test
    void ihsfilter_test() throws Exception {
        PureBigraph agent = createAgent();
        PureBigraph redex = createRedex4();
        eb(agent, "agent");
        eb(redex, "redex4");

        IHSFilter ihsFilter = new IHSFilter(redex, agent);

        ihsFilter.condition4(redex.getNodes().get(0), agent.getNodes().get(0));

    }

    void createGraphvizOutput(Bigraph<?> agent, BigraphMatch<?> next, String path) throws IncompatibleSignatureException, IncompatibleInterfaceException, IOException {
        PureBigraph context = (PureBigraph) next.getContext();
        PureBigraph redex = (PureBigraph) next.getRedex();
        Bigraph contextIdentity = next.getContextIdentity();
        Bigraph<DefaultDynamicSignature> identityForParams = next.getRedexIdentity();
        PureBigraph contextComposed = (PureBigraph) factory.asBigraphOperator(context).parallelProduct(contextIdentity).getOuterBigraph();

//        try {
        BigraphGraphvizExporter.toPNG(contextComposed,
                true,
                new File(path + "contextComposed.png")
        );

        BigraphGraphvizExporter.toPNG(context,
                true,
                new File(path + "context.png")
        );
        BigraphGraphvizExporter.toPNG(agent,
                true,
                new File(path + "agent.png")
        );
        BigraphGraphvizExporter.toPNG(redex,
                true,
                new File(path + "redex.png")
        );
    }

    private PureBigraph createQueryLinkGraph() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature2());

        BigraphEntity.InnerName x1 = builder.createInnerName("x1");
        BigraphEntity.InnerName x2 = builder.createInnerName("x2");
        builder.createRoot()
                .addChild("A").linkToInner(x1)
                .addChild("C").linkToInner(x1).linkToInner(x2)
                .addChild("B").linkToInner(x1).linkToInner(x2)
                .addChild("C").linkToInner(x2)
        ;

        builder.closeInnerName(x1);
        builder.closeInnerName(x2);
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    private PureBigraph createDataLinkGraph() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature2());
        BigraphEntity.InnerName x1 = builder.createInnerName("x1");
        BigraphEntity.InnerName x2 = builder.createInnerName("x2");
        BigraphEntity.InnerName x3 = builder.createInnerName("x3");
        BigraphEntity.InnerName x4 = builder.createInnerName("x4");

        builder.createRoot()
                .addChild("A").linkToInner(x1)
                .addChild("C").linkToInner(x1).linkToInner(x2)
                .addChild("B").linkToInner(x1).linkToInner(x2).linkToInner(x3)
                .addChild("C").linkToInner(x2).linkToInner(x2)
                .addChild("A").linkToInner(x3)
                .addChild("C").linkToInner(x3).linkToInner(x4)
                .addChild("B").linkToInner(x4)
                .addChild("C").linkToInner(x4)
        ;

        builder.closeAllInnerNames();
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    private PureBigraph createAgent() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());

//        /e0 (A{y1,y2}.1 | A{y1,e0}.1 | A{y1,e0}.1 | A{y2,e0}.1);

        BigraphEntity.OuterName y1 = builder.createOuterName("y1");
        BigraphEntity.OuterName y2 = builder.createOuterName("y2");
        BigraphEntity.InnerName e0 = builder.createInnerName("e0");
        builder.createRoot()
                .addChild("A").linkToOuter(y1).linkToOuter(y2)
                .addChild("A").linkToOuter(y1).linkToInner(e0)
                .addChild("A").linkToOuter(y1).linkToInner(e0)
                .addChild("A").linkToOuter(y2).linkToInner(e0)
        ;
        builder.closeInnerName(e0);
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    //(A{y1,e0}.1 | A{y2,e0}.1) -> (A{y1,e0}.1 | A{y2,e0}.1);
    public PureBigraph createRedex1() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());
        BigraphEntity.OuterName y1 = builder.createOuterName("y2");
        BigraphEntity.OuterName y2 = builder.createOuterName("e0");
        BigraphEntity.OuterName e0 = builder.createOuterName("y1");
        builder.createRoot()
                .addChild("A").linkToOuter(y1).linkToOuter(e0)
                .addChild("A").linkToOuter(y2).linkToOuter(e0)
        ;

        return builder.createBigraph();
    }

    ///e0 (A{y1,e0}.1 | A{y1,e0}.1) -> /e0 (A{y1,e0}.1 | A{y1,e0}.1)
    public PureBigraph createRedex3() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());
        BigraphEntity.OuterName y1 = builder.createOuterName("y1");
        BigraphEntity.InnerName e0 = builder.createInnerName("e0");
        builder.createRoot()
                .addChild("A").linkToOuter(y1).linkToInner(e0)
                .addChild("A").linkToOuter(y1).linkToInner(e0)
        ;
        builder.closeInnerName(e0);
        return builder.createBigraph();
    }

    ///e0 (/y1 A{y1,e0}.1 | /y2 A{y2,e0}.1) -> /e0 (/y1 A{y1,e0}.1 | /y2 A{y2,e0}.1);
    public PureBigraph createRedex2() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());
        builder.createRoot()
                .connectByEdge("A", "A")
        ;

        return builder.createBigraph();
    }


    // (A{y1,e0}.1 | A{y1,e0}.1)
    public PureBigraph createRedex4() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());

        BigraphEntity.OuterName y1 = builder.createOuterName("y1");
        BigraphEntity.OuterName e0 = builder.createOuterName("e0");
        builder.createRoot()
                .addChild("A").linkToOuter(y1).linkToOuter(e0)
                .addChild("A").linkToOuter(y1).linkToOuter(e0)
        ;

        return builder.createBigraph();
    }

    public ReactionRule<PureBigraph> createReactionRule4() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
//        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());
//        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(createSignature());

        return new ParametricReactionRule<>(createRedex4(), createRedex4());
    }

    public ReactionRule<PureBigraph> createReactionRule2() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
//        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());
//        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(createSignature());

        return new ParametricReactionRule<>(createRedex2(), createRedex2());
    }

    private SubBigraphMatchPredicate<PureBigraph> createPredicate() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());

        BigraphEntity.OuterName from = builder.createOuterName("from");

        // links of car and target must be connected via an outer name otherwise the predicate is not matched
        builder.createRoot()
                .addChild("Place").linkToOuter(from)
//                .down().addSite().connectByEdge("Target", "Car").down().addSite();
                .down().addSite().addChild("Target", "target").addChild("Car", "target").down().addSite();
        PureBigraph bigraph = builder.createBigraph();
        return SubBigraphMatchPredicate.create(bigraph);
    }

    public static <C extends Control<?, ?>, S extends Signature<C>> S createSignature() {
        DynamicSignatureBuilder defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier("A").arity(2).assign()
        ;
        return (S) defaultBuilder.create();
    }

    public static <C extends Control<?, ?>, S extends Signature<C>> S createSignature2() {
        DynamicSignatureBuilder defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier("A").arity(3).assign()
                .newControl().identifier("B").arity(3).assign()
                .newControl().identifier("C").arity(3).assign()
        ;
        return (S) defaultBuilder.create();
    }
}
