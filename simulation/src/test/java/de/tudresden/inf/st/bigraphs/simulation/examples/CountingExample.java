package de.tudresden.inf.st.bigraphs.simulation.examples;

import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidArityOfControlException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.simulation.ReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.impl.PureReactiveSystem;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.BigraphModelChecker;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.PureBigraphModelChecker;
import de.tudresden.inf.st.bigraphs.simulation.exceptions.BigraphSimulationException;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dominik Grzelak
 */
public class CountingExample {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/counting/";
    private static PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    @Test
    void simulate_counting_example() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, InvalidReactionRuleException, BigraphSimulationException {
        // Create reaction rulesname
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();

        PureBigraph agent_a = createAgent_A(3, 4);
        BigraphGraphvizExporter.toPNG(agent_a,
                true,
                new File(TARGET_DUMP_PATH + "counting_agent.png")
        );

//        PureBigraph agent = (PureBigraph) createAgent_A();
        ReactionRule<PureBigraph> rr_1 = createReactionRule_1();
        ReactionRule<PureBigraph> rr_2 = createReactionRule_2();
        ReactionRule<PureBigraph> rr_3 = createReactionRule_3();
//        GraphvizConverter.toPNG(rr_1.getRedex(),
//                true,
//                new File("rr_lhs_1.png")
//        );
//        GraphvizConverter.toPNG(rr_1.getReactum(),
//                true,
//                new File("rr_rhs_1.png")
//        );

        ModelCheckingOptions.ExportOptions.Builder builder = ModelCheckingOptions.exportOpts();
        ModelCheckingOptions.TransitionOptions.Builder builder1 = transitionOpts();
        reactiveSystem.addReactionRule(rr_1);
        reactiveSystem.addReactionRule(rr_2);
        reactiveSystem.addReactionRule(rr_3);
        assertTrue(reactiveSystem.isSimple());
        Path currentRelativePath = Paths.get("");
        Path completePath = Paths.get(currentRelativePath.toAbsolutePath().toString(), TARGET_DUMP_PATH, "transition_graph_2.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(8)
                        .setMaximumTime(30)
//                        .allowReducibleClasses(true)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setTraceFile(new File(completePath.toUri()))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .create()
                )
        ;
//
//        MatchPredicate<PureBigraph> pred1 = MatchPredicate.create((PureBigraph) createAgent_A_Final());
//
//        reactiveSystem.computeTransitionSystem(agent_a, opts);
        reactiveSystem.setAgent(agent_a);

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationType.BREADTH_FIRST,
                opts);
        modelChecker.execute();
    }

    /**
     * big numberLeft = Left.S.S.S.S.S.S.Z;
     * big numberRight = Right.S.S.S.S.Z;
     * big start = Age . (numberLeft | numberRight);
     */
    public static PureBigraph createAgent_A(final int left, final int right) throws ControlIsAtomicException, InvalidArityOfControlException, LinkTypeNotExistsException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy leftNode =
                builder.newHierarchy(signature.getControlByName("Left"))
                        .addChild("S");
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy rightNode =
                builder.newHierarchy(signature.getControlByName("Right"))
                        .addChild("S");
//        IntStream.range(0, left).forEach(x -> {
//            leftNode.withNewHierarchy().addChild("S");
//        });
//        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy s = IntStream.range(0, left)
//                .boxed()
//                .map(x -> "S")
//                .reduce((x) -> {
//                    return builder.newHierarchy(signature.getControlByName(x)).addChild(x);
//                }).get();
//        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy s = builder.newHierarchy(signature.getControlByName("S"));
//        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy s1 = builder.newHierarchy(signature.getControlByName("S"));
//        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy s2 = builder.newHierarchy(signature.getControlByName("S"));
//        s = s.addChild(s1);
//        s = s.withNewHierarchy().addChild(s2);
//        s = s.addChild(s2);
        for (int i = 0; i < left - 1; i++) {
            leftNode = leftNode.withNewHierarchy().addChild("S");
        }
        leftNode = leftNode.withNewHierarchy().addChild("Z").top();
        for (int i = 0; i < right - 1; i++) {
            rightNode = rightNode.withNewHierarchy().addChild("S");
        }
        rightNode = rightNode.withNewHierarchy().addChild("Z").top();

        builder.createRoot()
                .addChild("Age")
                .withNewHierarchy()
                .addChild(leftNode)
                .addChild(rightNode)
//                .addChild(s.top())
        ;
        builder.makeGround();
        return builder.createBigraph();
    }

    /**
     * react r1 = Left.S | Right.S -> Left | Right;
     */
    public static ReactionRule<PureBigraph> createReactionRule_1() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild("Left").withNewHierarchy().addChild("S").withNewHierarchy().addSite()
                .top()
                .addChild("Right").withNewHierarchy().addChild("S").withNewHierarchy().addSite()
        ;
        builder2.createRoot()
                .addChild("Left").withNewHierarchy().addSite()
                .top()
                .addChild("Right").withNewHierarchy().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    /**
     * react r2 = Left.Z | Right.S -> True;
     */
    public static ReactionRule<PureBigraph> createReactionRule_2() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild("Left").withNewHierarchy().addChild("Z")
                .top()
                .addChild("Right").withNewHierarchy().addChild("S").withNewHierarchy().addSite()
        ;
        builder2.createRoot()
                .addChild("True").withNewHierarchy().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    /**
     * react r3 = Left | Right.Z -> False;
     */
    public static ReactionRule<PureBigraph> createReactionRule_3() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild("Left").withNewHierarchy().addSite()
                .top()
                .addChild("Right").withNewHierarchy().addChild("Z")
        ;
        builder2.createRoot()
                .addChild("False").withNewHierarchy().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private static <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Age")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("S")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Z")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("True")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("False")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Left")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Right")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;

        return (S) defaultBuilder.create();
    }
}
