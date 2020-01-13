package de.tudresden.inf.st.bigraphs.simulation;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.impl.PureReactiveSystem;
import de.tudresden.inf.st.bigraphs.simulation.exceptions.BigraphSimulationException;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.predicates.SubBigraphMatchPredicate;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.BigraphModelChecker;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.PureBigraphModelChecker;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dominik Grzelak
 */
public class ReactiveSystemUnitTests {
    private static PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/basic/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    @Test
    @DisplayName("BFS simulation test")
    void simulate_basic_example() throws TypeNotExistsException, InvalidConnectionException, IOException, InvalidReactionRuleException, BigraphSimulationException {
        // Create reaction rulesname
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        PureBigraph agent = (PureBigraph) createAgent_A();
        reactiveSystem.setAgent(agent);
        ReactionRule<PureBigraph> rr = createReactionRule_AddJob();
        ReactionRule<PureBigraph> rrsame = createReactionRule_AddJob();
        ReactionRule<PureBigraph> rrSelf = createReactionRule_A_SelfApply();
        ReactionRule<PureBigraph> rr2 = createReactionRule_AddOneJob();

        reactiveSystem.addReactionRule(rr);
        reactiveSystem.addReactionRule(rrSelf);
        reactiveSystem.addReactionRule(rr2);
        reactiveSystem.addReactionRule(rrsame);
        assertTrue(reactiveSystem.isSimple());

        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(4)
                        .setMaximumTime(30)
                        .allowReducibleClasses(true)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setTraceFile(new File(completePath.toUri()))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .create()
                )
        ;

        SubBigraphMatchPredicate<PureBigraph> pred1 = SubBigraphMatchPredicate.create((PureBigraph) createAgent_A_Final());
        reactiveSystem.addPredicate(pred1);
//        reactiveSystem.computeTransitionSystem(agent, opts, Arrays.asList(pred1));

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem, BigraphModelChecker.SimulationType.BREADTH_FIRST, opts);
        modelChecker.execute();
    }

    @Test
    @DisplayName("Random simulation test: Agent is randomly chosen")
    void random_simulation_test() throws BigraphSimulationException, TypeNotExistsException, InvalidConnectionException, InvalidReactionRuleException {
        // Create reaction rulesname
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        PureBigraph agent = (PureBigraph) createAgent_A();
        reactiveSystem.setAgent(agent);
        ReactionRule<PureBigraph> rr = createReactionRule_AddJob();
        ReactionRule<PureBigraph> rr1 = createReactionRule_AddOneJob();
        ReactionRule<PureBigraph> rr2 = createReactionRule_AddTwoJobs();
        ReactionRule<PureBigraph> rr3 = createReactionRule_AddThreeJobs();
        ReactionRule<PureBigraph> rr4 = createReactionRule_AddJob2();

        reactiveSystem.addReactionRule(rr);
        reactiveSystem.addReactionRule(rr1);
        reactiveSystem.addReactionRule(rr2);
        reactiveSystem.addReactionRule(rr3);
        reactiveSystem.addReactionRule(rr4);

        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(4)
                        .setMaximumTime(30)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setTraceFile(Paths.get(TARGET_DUMP_PATH, "transition_graph_random.png").toFile())
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationType.RANDOM_STATE,
                opts);
        modelChecker.execute();
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Create Reactive System options")
    class ReactiveSystemOptionTests {
        @Test
        void reactionsystem_options_test() {
            ModelCheckingOptions opts = ModelCheckingOptions.create();
            opts.and(transitionOpts()
                    .setMaximumTransitions(4)
                    .setMaximumTime(60, TimeUnit.SECONDS)
                    .create()
            ).and(ModelCheckingOptions.exportOpts()
                    .setOutputStatesFolder(new File(""))
                    .setTraceFile(new File(""))
                    .create()
            );

            // overwrite old settings
            opts.and(transitionOpts()
                    .setMaximumTransitions(5)
                    .setMaximumTime(30, TimeUnit.MILLISECONDS)
                    .create());

            ModelCheckingOptions.TransitionOptions opts1 = opts.get(ModelCheckingOptions.Options.TRANSITION);
            assertEquals(opts1.getMaximumTransitions(), 5);
            assertEquals(opts1.getMaximumTimeUnit(), TimeUnit.MILLISECONDS);
            assertEquals(opts1.getMaximumTime(), 30);
        }
    }

    public static Bigraph createAgent_model_test_0() throws TypeNotExistsException, InvalidConnectionException, IOException, ControlIsAtomicException {
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        BigraphEntity.OuterName network = builder.createOuterName("network");

        builder.createRoot()
                .addChild(signature.getControlByName("Room"))
                .down()
                .addChild(signature.getControlByName("Computer")).linkToOuter(network)
        ;

        builder.closeAllInnerNames();
        builder.makeGround();

        return builder.createBigraph();
    }

    public static Bigraph createAgent_A() throws ControlIsAtomicException, InvalidConnectionException, TypeNotExistsException {
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuterName("network");
        builder.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network)
        ;
        builder.makeGround();
        return builder.createBigraph();
    }

    public static Bigraph createAgent_A_Final() throws ControlIsAtomicException, InvalidConnectionException, TypeNotExistsException {
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuterName("network");
        builder.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network)
                .down()
                .addChild("Job").addChild("Job")
        ;
        return builder.createBigraph();
    }

    /**
     * big rleft = (Room.(Comp{n}.1));
     * big rright = (Room.(Comp{n}.Job.1));
     * <p>
     * react r1 = rleft --> rright;
     */
    public static ReactionRule<PureBigraph> createReactionRule() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);

        BigraphEntity.OuterName network = builder.createOuterName("network");
        builder.createRoot()
                .addChild(signature.getControlByName("Room"))
                .down()
                .addChild(signature.getControlByName("Computer")).linkToOuter(network)
        ;

        BigraphEntity.OuterName network2 = builder2.createOuterName("network");
        builder2.createRoot()
                .addChild(signature.getControlByName("Room"))
                .down()
                .addChild(signature.getControlByName("Computer")).linkToOuter(network2)
                .down()
                .addChild(signature.getControlByName("Job"))
        ;

//        builder.closeAllInnerNames();
//        builder.makeGround();
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    public static ReactionRule<PureBigraph> createReactionRule_A_SelfApply() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuterName("network");
        BigraphEntity.OuterName network2 = builder2.createOuterName("network");
        builder.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network)
        ;
        builder2.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network2)
        ;
//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    public static ReactionRule<PureBigraph> createReactionRule_AddJob2() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuterName("network");
        BigraphEntity.OuterName network2 = builder2.createOuterName("network");
        builder.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network)
        ;
        builder2.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network2)
                .down()
                .addChild("Job").addChild("Job")
        ;

//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }


    public static ReactionRule<PureBigraph> createReactionRule_AddJob() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuterName("network");
        BigraphEntity.OuterName network2 = builder2.createOuterName("network");
        builder.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network)
        ;
        builder2.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network2)
                .down()
                .addChild("Job")
        ;

//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    public static ReactionRule<PureBigraph> createReactionRule_AddOneJob() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuterName("network");
        BigraphEntity.OuterName network2 = builder2.createOuterName("network");
        builder.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network)
                .down()
                .addChild("Job")
        ;
        builder2.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network2)
                .down()
                .addChild("Job").addChild("Job")
        ;

//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    public static ReactionRule<PureBigraph> createReactionRule_AddTwoJobs() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuterName("network");
        BigraphEntity.OuterName network2 = builder2.createOuterName("network");
        builder.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network)
                .down()
                .addChild("Job")
        ;
        builder2.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network2)
                .down()
                .addChild("Job").addChild("Job").addChild("Job")
        ;

//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    public static ReactionRule<PureBigraph> createReactionRule_AddThreeJobs() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuterName("network");
        BigraphEntity.OuterName network2 = builder2.createOuterName("network");
        builder.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network)
                .down()
                .addChild("Job")
        ;
        builder2.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network2)
                .down()
                .addChild("Job").addChild("Job").addChild("Job").addChild("Job")
        ;

//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }


    private static <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;

        return (S) defaultBuilder.create();
    }

}
