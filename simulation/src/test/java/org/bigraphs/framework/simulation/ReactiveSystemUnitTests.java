package org.bigraphs.framework.simulation;

import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.analysis.ReactionGraphAnalysis;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dominik Grzelak
 */
public class ReactiveSystemUnitTests {
    //    private static PureBigraphFactory factory = pure();
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/basic/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    @Test
    void simulate_basic_abbExample() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, BigraphSimulationException, ReactiveSystemException {
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        PureBigraph agent_b = (PureBigraph) createAgent_A2();
        reactiveSystem.setAgent(agent_b);
        ReactionRule<PureBigraph> rr = createReactionRuleForA2();
        reactiveSystem.addReactionRule(rr);

        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph_ABB.png");
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
                        .setPrintCanonicalStateLabel(true)
                        .setReactionGraphFile(new File(completePath.toUri()))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "statesABB/"))
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS, opts);
        modelChecker.execute();
        assertTrue(Files.exists(Paths.get(TARGET_DUMP_PATH, "transition_graph_ABB.png")));

        Set<ReactionGraph.LabeledNode> labeledNodes = modelChecker.getReactionGraph().getGraph().vertexSet();
        assertEquals(2, labeledNodes.size());

        String s0 = new ArrayList<>(labeledNodes).get(0).getCanonicalForm();
        String s1 = new ArrayList<>(labeledNodes).get(1).getCanonicalForm();

        boolean c10 = s0.contains("r0$A{e0}B{e1}B{e2}#") && s1.contains("r0$A{e0}B{e1}#");
        boolean c01 = s0.contains("r0$A{e0}B{e1}#") && s1.contains("r0$A{e0}B{e1}B{e2}#");

        assertTrue(c10 || c01);
    }

    @Test
    @DisplayName("BFS simulation test")
    void simulate_basic_example() throws TypeNotExistsException, InvalidConnectionException, IOException, InvalidReactionRuleException, BigraphSimulationException, ReactiveSystemException {
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
//                        .setPrintCanonicalStateLabel(true)
                                .setPrintCanonicalStateLabel(false)
                                .setReactionGraphFile(new File(completePath.toUri()))
                                .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                                .create()
                )
        ;

        SubBigraphMatchPredicate<PureBigraph> pred1 = SubBigraphMatchPredicate.create((PureBigraph) createAgent_A_Final());
        reactiveSystem.addPredicate(pred1);
//        reactiveSystem.computeTransitionSystem(agent, opts, Arrays.asList(pred1));

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts
        );
        modelChecker.execute();
        assertTrue(Files.exists(Paths.get(TARGET_DUMP_PATH, "transition_graph.png")));

        ReactionGraphAnalysis<PureBigraph> analysis = ReactionGraphAnalysis.createInstance();
        List<ReactionGraphAnalysis.StateTrace<PureBigraph>> pathsToLeaves = analysis.findAllPathsInGraphToLeaves(modelChecker.getReactionGraph());
        Assertions.assertEquals(3, pathsToLeaves.size());
        pathsToLeaves.forEach(x -> {
            System.out.println("Path has length: " + x.getPath().size());
            System.out.println("\t" + x.getStateLabels().toString());
//            Assertions.assertEquals(5, x.getPath().size());
        });
    }

    @Test
    @DisplayName("Random simulation test: Agent is randomly chosen")
    void random_simulation_test() throws BigraphSimulationException, TypeNotExistsException, InvalidConnectionException, InvalidReactionRuleException, ReactiveSystemException {
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
                        .setPrintCanonicalStateLabel(true)
                        .setReactionGraphFile(Paths.get(TARGET_DUMP_PATH, "transition_graph_random.png").toFile())
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.RANDOM,
                opts);
        modelChecker.execute();
        assertTrue(Files.exists(Paths.get(TARGET_DUMP_PATH, "transition_graph_random.png")));
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
                    .setReactionGraphFile(new File(""))
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
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.OuterName network = builder.createOuter("network");

        builder.root()
                .child(signature.getControlByName("Room"))
                .down()
                .child(signature.getControlByName("Computer")).linkOuter(network)
        ;

        builder.closeInner();
        builder.makeGround();

        return builder.create();
    }

    public static Bigraph createAgent_A() throws ControlIsAtomicException, InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
        ;
        builder.makeGround();
        return builder.create();
    }

    public static Bigraph createAgent_A2() throws ControlIsAtomicException, InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = createExampleSignatureABB();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        builder.root()
                .child("A")
                .child("B")
                .child("B")
        ;
//        builder.makeGround();
        return builder.create();
    }

    public static Bigraph createAgent_A_Final() throws ControlIsAtomicException, InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
                .down()
                .child("Job").child("Job")
        ;
        return builder.create();
    }

    /**
     * big rleft = (Room.(Comp{n}.1));
     * big rright = (Room.(Comp{n}.Job.1));
     * <p>
     * react r1 = rleft --> rright;
     */
    public static ReactionRule<PureBigraph> createReactionRule() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        BigraphEntity.OuterName network = builder.createOuter("network");
        builder.root()
                .child(signature.getControlByName("Room"))
                .down()
                .child(signature.getControlByName("Computer")).linkOuter(network)
        ;

        BigraphEntity.OuterName network2 = builder2.createOuter("network");
        builder2.root()
                .child(signature.getControlByName("Room"))
                .down()
                .child(signature.getControlByName("Computer")).linkOuter(network2)
                .down()
                .child(signature.getControlByName("Job"))
        ;

//        builder.closeAllInnerNames();
//        builder.makeGround();
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    public static ReactionRule<PureBigraph> createReactionRule_A_SelfApply() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        BigraphEntity.OuterName network2 = builder2.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
        ;
        builder2.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network2)
        ;
//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    public static ReactionRule<PureBigraph> createReactionRule_AddJob2() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        BigraphEntity.OuterName network2 = builder2.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
        ;
        builder2.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network2)
                .down()
                .child("Job").child("Job")
        ;

//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    /**
     * a | b | b -> a | b
     */
    public static ReactionRule<PureBigraph> createReactionRuleForA2() throws ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignatureABB();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root().child("A").child("B").child("B");
        builder2.root().child("A").child("B");

//        builder.makeGround();
//        builder2.makeGround();
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    public static ReactionRule<PureBigraph> createReactionRule_AddJob() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        BigraphEntity.OuterName network2 = builder2.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
        ;
        builder2.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network2)
                .down()
                .child("Job")
        ;

//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    public static ReactionRule<PureBigraph> createReactionRule_AddOneJob() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        BigraphEntity.OuterName network2 = builder2.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
                .down()
                .child("Job")
        ;
        builder2.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network2)
                .down()
                .child("Job").child("Job")
        ;

//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    public static ReactionRule<PureBigraph> createReactionRule_AddTwoJobs() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        BigraphEntity.OuterName network2 = builder2.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
                .down()
                .child("Job")
        ;
        builder2.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network2)
                .down()
                .child("Job").child("Job").child("Job")
        ;

//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    public static ReactionRule<PureBigraph> createReactionRule_AddThreeJobs() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        BigraphEntity.OuterName network2 = builder2.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
                .down()
                .child("Job")
        ;
        builder2.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network2)
                .down()
                .child("Job").child("Job").child("Job").child("Job")
        ;

//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }


    private static <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;

        return (S) defaultBuilder.create();
    }

    private static <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignatureABB() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(1)).status(ControlStatus.ATOMIC).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(1)).status(ControlStatus.ATOMIC).assign()
        ;

        return (S) defaultBuilder.create();
    }

}
