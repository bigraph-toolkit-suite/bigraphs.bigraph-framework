package de.tudresden.inf.st.bigraphs.rewriting.examples;

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
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystemOptions;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.ReactionGraphStats;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.impl.PureReactiveSystem;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.BigraphModelChecker;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.PureBigraphModelChecker;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.exceptions.BigraphSimulationException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystemOptions.transitionOpts;

/**
 * @author Dominik Grzelak
 */
public class MultipleOccurrencesExample {
    private static PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/multiple/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    @Test
    void simulate() throws TypeNotExistsException, InvalidConnectionException, InvalidReactionRuleException, BigraphSimulationException {

        PureBigraph agent = createAgent();
        ReactionRule<PureBigraph> reactionRuleJA = createReactionRuleJA();
        ReactionRule<PureBigraph> reactionRuleJB = createReactionRuleJB();
        ReactionRule<PureBigraph> reactionRuleJAC = createReactionRuleJAC();
        ReactionRule<PureBigraph> reactionRuleJBD = createReactionRuleJBD();
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(reactionRuleJA);
        reactiveSystem.addReactionRule(reactionRuleJB);
        reactiveSystem.addReactionRule(reactionRuleJAC);
        reactiveSystem.addReactionRule(reactionRuleJBD);

        ReactiveSystemOptions opts = ReactiveSystemOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(50)
                        .setMaximumTime(30)
                        .allowReducibleClasses(true)
                        .create()
                )
                .doMeasureTime(true)
                .and(ReactiveSystemOptions.exportOpts()
                        .setTraceFile(new File(TARGET_DUMP_PATH, "transition_graph.png"))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationType.BREADTH_FIRST,
                opts);
        modelChecker.execute();
        ReactionGraphStats<PureBigraph> graphStats = modelChecker.getReactionGraph().getGraphStats();
        System.out.println("Transitions: " + graphStats.getTransitionCount());
        System.out.println("States: " + graphStats.getStateCount());
        System.out.println("Occurrences: " + graphStats.getOccurrenceCount());
    }


    PureBigraph createAgent() {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());
        builder.createRoot()
                .addChild("Room")
                .addChild("Room")
        ;
        return builder.createBigraph();
    }

    public ReactionRule<PureBigraph> createReactionRuleJA() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(createSignature());

        builder.createRoot()
                .addChild("Room"); //.withNewHierarchy().addSite().top()
        ;
        builder2.createRoot()
                .addChild("Room").withNewHierarchy().addChild("JobA").top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }

    public ReactionRule<PureBigraph> createReactionRuleJB() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(createSignature());

        builder.createRoot()
                .addChild("Room"); //.withNewHierarchy().addSite().top()
        ;
        builder2.createRoot()
                .addChild("Room").withNewHierarchy().addChild("JobB").top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }

    public ReactionRule<PureBigraph> createReactionRuleJAC() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(createSignature());

        builder.createRoot()
                .addChild("Room").withNewHierarchy().addChild("JobA").top()
        ;
        builder2.createRoot()
                .addChild("Room").withNewHierarchy().addChild("JobC").top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }

    public ReactionRule<PureBigraph> createReactionRuleJBD() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(createSignature());

        builder.createRoot()
                .addChild("Room").withNewHierarchy().addChild("JobB").top()
        ;
        builder2.createRoot()
                .addChild("Room").withNewHierarchy().addChild("JobD").top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }


    private static <C extends Control<?, ?>, S extends Signature<C>> S createSignature() {
        DynamicSignatureBuilder defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("JobA")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("JobB")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("JobC")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("JobD")).arity(FiniteOrdinal.ofInteger(1)).assign()
        ;
        return (S) defaultBuilder.create();
    }
}
