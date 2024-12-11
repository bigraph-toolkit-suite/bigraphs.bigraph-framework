package org.bigraphs.framework.simulation.examples;

import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphDecoder;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphEncoder;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.*;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

@Disabled
public class FruitBasketExampleTest {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/fruitbasket/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    @Test
    void simulate() throws InvalidConnectionException, IOException, InvalidReactionRuleException, ReactiveSystemException, BigraphSimulationException {
        DefaultDynamicSignature sig = createSignature();
        PureBigraph agent = createAgent();
        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
        agent = decoder.decode(encoder.encode(agent), sig);
        BigraphGraphvizExporter.toPNG(agent, true, new File(TARGET_DUMP_PATH + "agent.png"));

        ParametricReactionRule<PureBigraph> rr1 = createRR1();
        BigraphGraphvizExporter.toPNG(rr1.getRedex(), true, new File(TARGET_DUMP_PATH + "rr1_LHS.png"));
        BigraphGraphvizExporter.toPNG(rr1.getReactum(), true, new File(TARGET_DUMP_PATH + "rr1_RHS.png"));

        ParametricReactionRule<PureBigraph> rr2 = createRR2();
        BigraphGraphvizExporter.toPNG(rr2.getRedex(), true, new File(TARGET_DUMP_PATH + "rr2_LHS.png"));
        BigraphGraphvizExporter.toPNG(rr2.getReactum(), true, new File(TARGET_DUMP_PATH + "rr2_RHS.png"));

        ParametricReactionRule<PureBigraph> rr3 = createRR3();
        BigraphGraphvizExporter.toPNG(rr3.getRedex(), true, new File(TARGET_DUMP_PATH + "rr3_LHS.png"));
        BigraphGraphvizExporter.toPNG(rr3.getReactum(), true, new File(TARGET_DUMP_PATH + "rr3_RHS.png"));

        ParametricReactionRule<PureBigraph> rr4 = createRR4();
        BigraphGraphvizExporter.toPNG(rr4.getRedex(), true, new File(TARGET_DUMP_PATH + "rr4_LHS.png"));
        BigraphGraphvizExporter.toPNG(rr4.getReactum(), true, new File(TARGET_DUMP_PATH + "rr4_RHS.png"));

        PureReactiveSystem rs = new PureReactiveSystem();
        rs.setAgent(createAgent());
        rs.addReactionRule(createRR1());
        rs.addReactionRule(createRR2());
        rs.addReactionRule(createRR3());
        rs.addReactionRule(createRR4());
        assert rs.isSimple();

        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(100)
                        .setMaximumTime(60)
                        .allowReducibleClasses(true)
                        .rewriteOpenLinks(false)
                        .create()
                )
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(Paths.get(TARGET_DUMP_PATH, "transition_graph.png").toFile())
                        .setPrintCanonicalStateLabel(false)
                        .setOutputStatesFolder(Paths.get(TARGET_DUMP_PATH, "states/").toFile())
                        .setFormatsEnabled(List.of(ModelCheckingOptions.ExportOptions.Format.PNG, ModelCheckingOptions.ExportOptions.Format.XMI))
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                rs,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.execute();

        ReactionGraph<PureBigraph> reactionGraph = modelChecker.getReactionGraph();

//        Set<AbstractTransitionSystem<PureBigraph, BMatchResult<PureBigraph>>.Transition> transitions = reactionGraph.getTransitions(reactionGraph.getStates().iterator().next());
//        System.out.println(reactionGraph.getLabelByState(transitions.stream().toList().get(0).getSource()));
//        System.out.println(transitions.stream().toList().get(0).getLabel());
//        System.out.println(reactionGraph.getLabelByState(transitions.stream().toList().get(0).getTarget()));
//        System.out.println(reactionGraph);

//        ParametricReactionRule<PureBigraph> rr11 = createRR1();
//        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
//        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agent, rr11);
//        Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
//        while (iterator.hasNext()) {
//            BigraphMatch<PureBigraph> next = iterator.next();
//            PureBigraph result = rs.buildParametricReaction(agent, next, rr11);
//            // do something with `result`
//            System.out.println(result);
//            assert result != null;
//        }

    }

    public static DefaultDynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .addControl("User", 1, ControlStatus.ATOMIC)
                .addControl("Basket", 1)
                .addControl("Fruit", 1, ControlStatus.ATOMIC)
                .addControl("Table", 0)
        ;
        return defaultBuilder.create();
    }

    public static PureBigraph createAgent() throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(createSignature());

        return b.createRoot()
                .addChild("User")
                .addChild("Table").down()
                .addChild("Basket")
                .addChild("Fruit", "f")
                .addChild("Fruit", "f")
                .addChild("Fruit", "f")
                .createBigraph();

    }

    public static PureBigraph createAgent2() throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(createSignature());

        return b.createRoot()
                .addChild("User")
                .addChild("Table").down()
                .addChild("Basket")
                .addChild("Fruit", "f")
                .addChild("Fruit", "f")
                .addChild("Fruit", "f")
                .addChild("Fruit", "f")
                .addChild("Fruit", "f")
                .createBigraph();

    }

    public static ParametricReactionRule<PureBigraph> createRR1() throws InvalidReactionRuleException, InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> b2 = pureBuilder(createSignature());
        b1.createRoot()
                .addChild("User")
                .addChild("Table").down()
                .addChild("Fruit", "f").addSite()
        ;
        b2.createRoot()
                .addChild("User", "f")
                .addChild("Table").down()
                .addChild("Fruit", "f").addSite()
        ;
        PureBigraph redex = b1.createBigraph();
        PureBigraph reactum = b2.createBigraph();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("rr1");
        return rr;
    }

    public static ParametricReactionRule<PureBigraph> createRR2() throws InvalidReactionRuleException, InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> b2 = pureBuilder(createSignature());
        b1.createRoot()
                .addChild("User", "f")
                .addChild("Table").down()
                .addChild("Fruit", "f").addSite()
                .addChild("Basket").down().addSite()
        ;
        b2.createRoot()
                .addChild("User", "f")
                .addChild("Table").down()
                .addChild("Fruit", "f").addSite()
                .addChild("Basket", "f").down().addSite()
        ;
        PureBigraph redex = b1.createBigraph();
        PureBigraph reactum = b2.createBigraph();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("rr2");
        return rr;
    }

    public static ParametricReactionRule<PureBigraph> createRR3() throws InvalidReactionRuleException, InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> b2 = pureBuilder(createSignature());
        b1.createRoot()
                .addChild("User", "f")
                .addChild("Table").down()
                .addChild("Fruit", "f").addSite()
                .addChild("Basket", "f").down().addSite()
        ;
        b2.createRoot()
                .addChild("User", "f")
                .addChild("Table").down()
                .addSite()
                .addChild("Basket", "f").down().addChild("Fruit", "f").addSite()
        ;
        PureBigraph redex = b1.createBigraph();
        PureBigraph reactum = b2.createBigraph();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("rr3");
        return rr;
    }

    public static ParametricReactionRule<PureBigraph> createRR4() throws InvalidReactionRuleException, InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> b2 = pureBuilder(createSignature());
        b1.createRoot()
                .addChild("User", "f")
                .addChild("Table").down().addSite()
                .addChild("Basket", "f").down().addChild("Fruit", "f").addSite()
        ;
        b2.createOuterName("f");
        b2.createRoot()
                .addChild("User")
                .addChild("Table").down().addSite()
                .addChild("Basket").down().addChild("Fruit").addSite()
        ;
        PureBigraph redex = b1.createBigraph();
        PureBigraph reactum = b2.createBigraph();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("rr4");
        return rr;
    }

    //every state a self-loop
    public static ParametricReactionRule<PureBigraph> createRR5_noop() throws InvalidReactionRuleException, InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> b2 = pureBuilder(createSignature());
        b1.createRoot()
                .addChild("User", "f")
        ;
        b2.createRoot()
                .addChild("User", "f")
        ;
        PureBigraph redex = b1.createBigraph();
        PureBigraph reactum = b2.createBigraph();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("rr5");
        return rr;
    }

    public static ParametricReactionRule<PureBigraph> createRR6_noop() throws InvalidReactionRuleException, InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> b2 = pureBuilder(createSignature());
        b1.createRoot()
                .addChild("Table").down().addSite()
                .addChild("Basket", "f").down().addChild("Fruit", "f").addSite()
        ;
        b2.createRoot()
                .addChild("Table").down().addSite()
                .addChild("Basket", "f").down().addChild("Fruit", "f").addSite()
        ;
        PureBigraph redex = b1.createBigraph();
        PureBigraph reactum = b2.createBigraph();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("rr6");
        return rr;
    }
}
