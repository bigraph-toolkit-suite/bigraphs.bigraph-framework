package org.bigraphs.framework.simulation.equivalence;

import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.bigraphs.framework.simulation.examples.FruitBasketExampleTest;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

public class FruitBasketBisimTest {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/fruitbasketbisim/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    @Test
    void reduction_test_LTS() throws Exception {
        PureReactiveSystem rs1 = new PureReactiveSystem();
        rs1.setAgent(FruitBasketExampleTest.createAgent());
        rs1.addReactionRule(FruitBasketExampleTest.createRR1());
        rs1.addReactionRule(FruitBasketExampleTest.createRR2());
        rs1.addReactionRule(FruitBasketExampleTest.createRR3());
        rs1.addReactionRule(FruitBasketExampleTest.createRR4());
        assert rs1.isSimple();

        ReactionGraph<PureBigraph> transitionSystem1 = simulate(rs1);

//        TSMinimization<ReactionGraph<PureBigraph>> mini = TSMinimization.getInstance();
//        mini.hopcroft(transitionSystem1);
    }

    @Test
    void simu() throws Exception {

        PureReactiveSystem rs1 = new PureReactiveSystem();
        rs1.setAgent(FruitBasketExampleTest.createAgent());
        rs1.addReactionRule(FruitBasketExampleTest.createRR1());
        rs1.addReactionRule(FruitBasketExampleTest.createRR2());
        rs1.addReactionRule(FruitBasketExampleTest.createRR3());
        rs1.addReactionRule(FruitBasketExampleTest.createRR4());
//        rs1.addReactionRule(FruitBasketExampleTest.createRR5_noop());
//        rs1.addReactionRule(FruitBasketExampleTest.createRR6_noop());
        assert rs1.isSimple();

        PureReactiveSystem rs2 = new PureReactiveSystem();
        rs2.setAgent(FruitBasketExampleTest.createAgent());
        rs2.addReactionRule(FruitBasketExampleTest.createRR1());
        rs2.addReactionRule(FruitBasketExampleTest.createRR2());
        rs2.addReactionRule(FruitBasketExampleTest.createRR3());
        rs2.addReactionRule(FruitBasketExampleTest.createRR4());
//        rs2.addReactionRule(FruitBasketExampleTest.createRR5_noop());
        rs2.addReactionRule(FruitBasketExampleTest.createRR6_noop());
        assert rs2.isSimple();

        ReactionGraph<PureBigraph> transitionSystem1 = simulate(rs1);
        ReactionGraph<PureBigraph> transitionSystem2 = simulate(rs2);


        BehavioralEquivalenceMixin<ReactionGraph<PureBigraph>> mixin = new StrongBisimulationMixinImpl<>();
        mixin.attachToObject(transitionSystem1);
        boolean equivalentTo = mixin.isEquivalentTo(transitionSystem2);
        System.out.println(equivalentTo);

    }

    public ReactionGraph<PureBigraph> simulate(PureReactiveSystem rs) throws ReactiveSystemException, BigraphSimulationException {
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
                        .setReactionGraphFile(Paths.get(TARGET_DUMP_PATH, "transition_graph_"+rs.toString()+".png").toFile())
                        .setPrintCanonicalStateLabel(false)
                        .setOutputStatesFolder(Paths.get(TARGET_DUMP_PATH, "states/").toFile())
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                rs,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.execute();

        ReactionGraph<PureBigraph> reactionGraph = modelChecker.getReactionGraph();
        return reactionGraph;
    }
}
