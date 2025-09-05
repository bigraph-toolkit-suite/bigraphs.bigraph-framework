package org.bigraphs.framework.simulation;

import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystemPredicate;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.predicates.BigraphIsoPredicate;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.exportOpts;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
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
        new File(TARGET_DUMP_PATH + "states/").mkdirs();
        new File(TARGET_DUMP_PATH + "states2/").mkdirs();
    }

    @Test
    void test_listeners() throws InvalidReactionRuleException, BigraphSimulationException, ReactiveSystemException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createExampleSignature());

        PureBigraph agent = builder.root()
                .child("Room").child("User").child("Computer").create();

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(createRR1());
        reactiveSystem.addReactionRule(createRR2());
        reactiveSystem.addPredicate(createPredicateIso());
        reactiveSystem.addPredicate(createPredicateSubBigraph());


        ModelCheckingOptions opts = ModelCheckingOptions.create().and(transitionOpts().setMaximumTransitions(10).create())
                .and(exportOpts().setReactionGraphFile(new File(TARGET_DUMP_PATH + "transition_graph.png"))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/")).create());

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
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
            public void onAllPredicateMatched(PureBigraph currentAgent, String label) {
                System.out.println("Matched:");
                allMatched.set(true);
                // Start a new BRS where the agent is the last state of the previous BRS
                ModelCheckingOptions opts = ModelCheckingOptions.create().and(transitionOpts().setMaximumTransitions(10).create())
                        .and(exportOpts().setReactionGraphFile(new File(TARGET_DUMP_PATH + "transition_graph2.png"))
                                .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states2/")).create());
                PureReactiveSystem pureReactiveSystem = new PureReactiveSystem();
                pureReactiveSystem.setAgent(currentAgent);
                try {
                    pureReactiveSystem.addReactionRule(createRR3());
                    PureBigraphModelChecker checker = new PureBigraphModelChecker(pureReactiveSystem, opts);
                    checker.execute();
                } catch (InvalidReactionRuleException | BigraphSimulationException | ReactiveSystemException e) {
                    e.printStackTrace();
                }
            }
        });

        modelChecker.execute();

        assertTrue(hasStarted.get());
        assertTrue(hasFinished.get());
        assertTrue(allMatched.get());
    }

    private ReactionRule<PureBigraph> createRR3() throws InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> redexB = pureBuilder(createExampleSignature());
        PureBigraphBuilder<DynamicSignature> reactumB = pureBuilder(createExampleSignature());

        PureBigraph redex = redexB.root().child("User").down().child("Token").child("Token").create();
        PureBigraph reactum = reactumB.root().child("User").down().child("Token").child("Token").child("Token").create();

        return new ParametricReactionRule<>(redex, reactum);
    }

    ReactiveSystemPredicate<PureBigraph> createPredicateIso() {
        PureBigraph big = pureBuilder(createExampleSignature()).root()
                .child("Room").child("User").down().child("Token").child("Token").up().child("Computer").create();
        return BigraphIsoPredicate.create(big);
    }

    ReactiveSystemPredicate<PureBigraph> createPredicateSubBigraph() throws InvalidReactionRuleException {
        PureBigraph big = createRR2().getReactum();
        return SubBigraphMatchPredicate.create(big);
    }

    ParametricReactionRule<PureBigraph> createRR1() throws InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> redexB = pureBuilder(createExampleSignature());
        PureBigraphBuilder<DynamicSignature> reactumB = pureBuilder(createExampleSignature());

        PureBigraph redex = redexB.root().child("User").create();
        PureBigraph reactum = reactumB.root().child("User").down().child("Token").create();

        return new ParametricReactionRule<>(redex, reactum);
    }

    ParametricReactionRule<PureBigraph> createRR2() throws InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> redexB = pureBuilder(createExampleSignature());
        PureBigraphBuilder<DynamicSignature> reactumB = pureBuilder(createExampleSignature());

        PureBigraph redex = redexB.root().child("User").down().child("Token").create();
        PureBigraph reactum = reactumB.root().child("User").down().child("Token").child("Token").create();

        return new ParametricReactionRule<>(redex, reactum);
    }

    private DynamicSignature createExampleSignature() {
        DynamicSignature signature = pureSignatureBuilder()
                .newControl("User", 1).assign()
                .newControl("Job", 1).assign()
                .newControl("Token", 1).assign()
                .newControl("Computer", 1).assign()
                .newControl("Room", 1).assign()
                .create();
        return signature;
    }
}
