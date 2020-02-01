package de.tudresden.inf.st.bigraphs.simulation;

import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.simulation.exceptions.BigraphSimulationException;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.BigraphModelChecker;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.PureBigraphModelChecker;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.impl.PureReactiveSystem;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.predicates.BigraphIsoPredicate;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.predicates.ReactiveSystemPredicates;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.predicates.SubBigraphMatchPredicate;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;
import static de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions.exportOpts;
import static de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This unit test shows how to execute multiple BRSs sequentially where the next one uses the last state of the previous
 * BRS. It can be thought of a cascading simulation.
 * <p>
 * Also useful for multi-scale simulations. Each BRS can be verified individually and combined later.
 *
 * @author Dominik Grzelak
 */
public class PredicateListenerUnitTests {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/predicatelistener/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    @Test
    void test_listeners() throws InvalidReactionRuleException, BigraphSimulationException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createExampleSignature());

        PureBigraph agent = builder.createRoot()
                .addChild("Room").addChild("User").addChild("Computer").createBigraph();

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(createRR1());
        reactiveSystem.addReactionRule(createRR2());
        reactiveSystem.addPredicate(createPredicateIso());
        reactiveSystem.addPredicate(createPredicateSubBigraph());


        ModelCheckingOptions opts = ModelCheckingOptions.create().and(transitionOpts().setMaximumTransitions(10).create())
                .and(exportOpts().setTraceFile(new File(TARGET_DUMP_PATH + "transition_graph.png"))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/")).create());

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationType.BREADTH_FIRST,
                opts
        );

        AtomicBoolean allMatched = new AtomicBoolean(false);
        AtomicBoolean hasStarted = new AtomicBoolean(false);
        AtomicBoolean hasFinished = new AtomicBoolean(false);

        modelChecker.setReactiveSystemListener(new BigraphModelChecker.ReactiveSystemListener<PureBigraph>() {
            @Override
            public void onReactiveSystemStarted() {
                System.out.println("Started");
                hasStarted.set(true);
            }

            @Override
            public void onReactiveSystemFinished() {
                System.out.println("Finished");
                hasFinished.set(true);
            }

            @Override
            public void onAllPredicateMatched(PureBigraph currentAgent) {
                System.out.println("Matched:");
                allMatched.set(true);
                // Start a new BRS where the agent is the last state of the previous BRS
                ModelCheckingOptions opts = ModelCheckingOptions.create().and(transitionOpts().setMaximumTransitions(10).create())
                        .and(exportOpts().setTraceFile(new File(TARGET_DUMP_PATH + "transition_graph2.png"))
                                .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states2/")).create());
                PureReactiveSystem pureReactiveSystem = new PureReactiveSystem();
                pureReactiveSystem.setAgent(currentAgent);
                try {
                    pureReactiveSystem.addReactionRule(createRR3());
                    PureBigraphModelChecker checker = new PureBigraphModelChecker(pureReactiveSystem, opts);
                    checker.execute();
                } catch (InvalidReactionRuleException | BigraphSimulationException e) {
                    e.printStackTrace();
                }
            }
        });

        modelChecker.execute();

        assertTrue(allMatched.get());
        assertTrue(hasStarted.get());
        assertTrue(hasFinished.get());
    }

    private ReactionRule<PureBigraph> createRR3() throws InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> redexB = pureBuilder(createExampleSignature());
        PureBigraphBuilder<DefaultDynamicSignature> reactumB = pureBuilder(createExampleSignature());

        PureBigraph redex = redexB.createRoot().addChild("User").down().addChild("Token").addChild("Token").createBigraph();
        PureBigraph reactum = reactumB.createRoot().addChild("User").down().addChild("Token").addChild("Token").addChild("Token").createBigraph();

        return new ParametricReactionRule<>(redex, reactum);
    }

    ReactiveSystemPredicates<PureBigraph> createPredicateIso() {
        PureBigraph big = pureBuilder(createExampleSignature()).createRoot()
                .addChild("Room").addChild("User").down().addChild("Token").addChild("Token").up().addChild("Computer").createBigraph();
        return BigraphIsoPredicate.create(big);
    }

    ReactiveSystemPredicates<PureBigraph> createPredicateSubBigraph() throws InvalidReactionRuleException {
        PureBigraph big = createRR2().getReactum();
        return SubBigraphMatchPredicate.create(big);
    }

    ParametricReactionRule<PureBigraph> createRR1() throws InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> redexB = pureBuilder(createExampleSignature());
        PureBigraphBuilder<DefaultDynamicSignature> reactumB = pureBuilder(createExampleSignature());

        PureBigraph redex = redexB.createRoot().addChild("User").createBigraph();
        PureBigraph reactum = reactumB.createRoot().addChild("User").down().addChild("Token").createBigraph();

        return new ParametricReactionRule<>(redex, reactum);
    }

    ParametricReactionRule<PureBigraph> createRR2() throws InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> redexB = pureBuilder(createExampleSignature());
        PureBigraphBuilder<DefaultDynamicSignature> reactumB = pureBuilder(createExampleSignature());

        PureBigraph redex = redexB.createRoot().addChild("User").down().addChild("Token").createBigraph();
        PureBigraph reactum = reactumB.createRoot().addChild("User").down().addChild("Token").addChild("Token").createBigraph();

        return new ParametricReactionRule<>(redex, reactum);
    }

    private DefaultDynamicSignature createExampleSignature() {
        DefaultDynamicSignature signature = pure().createSignatureBuilder()
                .newControl("User", 1).assign()
                .newControl("Job", 1).assign()
                .newControl("Token", 1).assign()
                .newControl("Computer", 1).assign()
                .newControl("Room", 1).assign()
                .create();
        return signature;
    }
}
