package de.tudresden.inf.st.bigraphs.simulation.examples;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.ReactiveSystemException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionGraphStats;
import de.tudresden.inf.st.bigraphs.simulation.matching.pure.PureReactiveSystem;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.BigraphModelChecker;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.PureBigraphModelChecker;
import de.tudresden.inf.st.bigraphs.simulation.exceptions.BigraphSimulationException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;
import static de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

/**
 * @author Dominik Grzelak
 */
public class MultipleOccurrencesExample {
    //    private static PureBigraphFactory factory = pure();
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/multiple/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    @Test
    void simulate() throws TypeNotExistsException, InvalidConnectionException, InvalidReactionRuleException, BigraphSimulationException, ReactiveSystemException {

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

        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(100)
                        .setMaximumTime(30)
                        .allowReducibleClasses(true)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(TARGET_DUMP_PATH, "transition_graph.png"))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.execute();
        ReactionGraphStats<PureBigraph> graphStats = modelChecker.getReactionGraph().getGraphStats();
        System.out.println("Transitions: " + graphStats.getTransitionCount());
        System.out.println("States: " + graphStats.getStateCount());
        System.out.println("Occurrences: " + graphStats.getOccurrenceCount());
    }


    PureBigraph createAgent() {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());
        builder.createRoot()
                .addChild("Room")
                .addChild("Room")
        ;
        return builder.createBigraph();
    }

    public ReactionRule<PureBigraph> createReactionRuleJA() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(createSignature());

        builder.createRoot()
                .addChild("Room"); //.withNewHierarchy().addSite().top()
        ;
        builder2.createRoot()
                .addChild("Room").down().addChild("JobA").top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }

    public ReactionRule<PureBigraph> createReactionRuleJB() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(createSignature());

        builder.createRoot()
                .addChild("Room"); //.withNewHierarchy().addSite().top()
        ;
        builder2.createRoot()
                .addChild("Room").down().addChild("JobB").top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }

    public ReactionRule<PureBigraph> createReactionRuleJAC() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(createSignature());

        builder.createRoot()
                .addChild("Room").down().addChild("JobA").top()
        ;
        builder2.createRoot()
                .addChild("Room").down().addChild("JobC").top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }

    public ReactionRule<PureBigraph> createReactionRuleJBD() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(createSignature());

        builder.createRoot()
                .addChild("Room").down().addChild("JobB").top()
        ;
        builder2.createRoot()
                .addChild("Room").down().addChild("JobD").top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }


    private static DefaultDynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("JobA")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("JobB")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("JobC")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("JobD")).arity(FiniteOrdinal.ofInteger(1)).assign()
        ;
        return defaultBuilder.create();
    }
}
