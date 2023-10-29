package org.bigraphs.framework.simulation.examples.bigrid;

import de.tudresden.inf.st.bigraphs.converter.bigrapher.BigrapherTransformator;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.AbstractReactionRule;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionRule;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactiveSystemPredicate;
import org.bigraphs.framework.simulation.BigraphUnitTestSupport;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static de.tudresden.inf.st.bigraphs.core.alg.generators.BigridGenerator.DiscreteIons.NodeType.*;
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleCircularGridTest implements BigraphUnitTestSupport {

    private static String BASEPATH = "src/test/resources/dump/bigrid/circular/";

    //Trace = Action Plan = regelanwendung
    //bei exception: wm wird verändert -> ursprungsregel kann nicht ausgefuehrt werden
    //man kann immer aktuellen zustand mit trace vergleichen und den abstand messen

    @Test
    void test_01() throws Exception {
        DefaultDynamicSignature sig = signature();

        int numOfCols = 4;
        int numOfRows = 4;
        BigridObject bigridObj = new BigridObject(sig, numOfCols, numOfRows);
        createOrGetBigraphMetaModel(bigridObj.getSignature());
//        BigraphGraphvizExporter.toPNG(bigridObj.getBigraph(), false, new File(BASEPATH + "circular-grid.png"));
        eb(bigridObj.getBigraph(), BASEPATH + "circular-grid");
        print(bigridObj.getBigraph());
//        printMetaModel(bigridObj.getBigraph());


        Bigraph contextSource = bigridObj.prepareSourceNodeAt(0, "turtle1", "T", true);
        Bigraph bot1 = bigridObj.prepareItemAtNode(0, bigridObj.getSignature().getControlByName("bot"), "turtle1", true);
        Bigraph sourceInstance = ops(contextSource).nesting(bot1).getOuterBigraph();
        eb(sourceInstance, BASEPATH + "sourceInstance");
        Bigraph contextTarget = bigridObj.prepareTargetNodeAt(9, "turtle1", "F", false);
        eb(contextTarget, BASEPATH + "contextTarget");

        List<Integer> blockedIndices = new ArrayList<>();
        blockedIndices.add(0);
        blockedIndices.add(9);
        Bigraph rest = purePlacings(bigridObj.getSignature()).permutation(numOfCols * numOfRows);
        for (int i = 0; i < numOfCols * numOfRows; i++) {
            if (!blockedIndices.contains(i)) {
                Bigraph t1 = bigridObj.prepareOccupiedBlockedNodeAt(i, "F", true);
                rest = ops(rest).nesting(t1).getOuterBigraph();
            }
        }
        eb(rest, BASEPATH + "rest");



        Bigraph outerBigraph = ops(rest).nesting(sourceInstance).nesting(contextTarget).getOuterBigraph();
        eb(outerBigraph, BASEPATH + "outerBigraph");
        PureBigraph agent = bigridObj.makeAgentCompatible(bigridObj.getBigraph(), outerBigraph);
        eb(agent, BASEPATH + "agent-final");
        print(agent);
        ReactiveSystemPredicate<PureBigraph> predTargetReached = bigridObj.targetReached();
        eb(predTargetReached.getBigraph(), BASEPATH + "predTargetReached");
//  BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) bigridObj.getBigraph(), System.out);

// ClockwiseMovement
        ReactionRule<PureBigraph> rr = bigridObj.constructRule(TOP_LEFT, TOP_EDGE).get(0);
        BigraphGraphvizExporter.toPNG(rr.getRedex(), true, new File(BASEPATH + "rr1-LHS.png"));
        BigraphGraphvizExporter.toPNG(rr.getReactum(), true, new File(BASEPATH + "rr1-RHS.png"));
        ReactionRule<PureBigraph> rr2 = bigridObj.constructRule(TOP_EDGE, TOP_EDGE).get(0);
        BigraphGraphvizExporter.toPNG(rr2.getRedex(), true, new File(BASEPATH + "rr2-LHS.png"));
        BigraphGraphvizExporter.toPNG(rr2.getReactum(), true, new File(BASEPATH + "rr2-RHS.png"));
        ReactionRule<PureBigraph> rr3 = bigridObj.constructRule(TOP_EDGE, CENTER).get(0);
        BigraphGraphvizExporter.toPNG(rr3.getRedex(), true, new File(BASEPATH + "rr3-LHS.png"));
        BigraphGraphvizExporter.toPNG(rr3.getReactum(), true, new File(BASEPATH + "rr3-RHS.png"));

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        List<ReactionRule<PureBigraph>> rrC4 = bigridObj.constructRule(CENTER, CENTER);
        for (ReactionRule<PureBigraph> eachCenterRule : rrC4) {
            String lbl = ((AbstractReactionRule) eachCenterRule).getLabel();
            BigraphGraphvizExporter.toPNG(eachCenterRule.getRedex(), true, new File(BASEPATH + lbl + "-LHS.png"));
            BigraphGraphvizExporter.toPNG(eachCenterRule.getReactum(), true, new File(BASEPATH + lbl + "-RHS.png"));
            reactiveSystem.addReactionRule(eachCenterRule);
        }


//
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(rr);
        reactiveSystem.addReactionRule(rr2);
        reactiveSystem.addReactionRule(rr3);
        reactiveSystem.addPredicate(predTargetReached);
//

        BigrapherTransformator encoder = new BigrapherTransformator();
        encoder.toOutputStream(reactiveSystem, new FileOutputStream(BASEPATH + "model.big"));

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts());
        modelChecker.execute();

        assertTrue(Files.exists(Paths.get(BASEPATH, "transition_graph_agent-2.png")));

        System.out.println(modelChecker.getReactionGraph().getGraph().vertexSet().size());
        System.out.println(modelChecker.getReactionGraph().getGraph().edgeSet().size());

//        ReactionGraphAnalysis<PureBigraph> analysis = ReactionGraphAnalysis.createInstance();
//        List<ReactionGraphAnalysis.PathList<PureBigraph>> pathsToLeaves = analysis.findAllPathsInGraphToLeaves(modelChecker.getReactionGraph());
//        System.out.println(pathsToLeaves.size());
//        System.out.println(pathsToLeaves.get(0).getStateLabels());
    }

    private ReactionRule<PureBigraph> createInverseRule(ReactionRule<PureBigraph> rule) throws InvalidReactionRuleException {
        if (rule.isReversible()) {
            if (rule.getInstantationMap().isIdentity()) {
                return new ParametricReactionRule<>(rule.getReactum(), rule.getRedex());
            } else {
                return new ParametricReactionRule<>(rule.getReactum(), rule.getRedex(), rule.getInstantationMap());
            }
        }
        return rule;
    }

    public ModelCheckingOptions opts() {
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(10)
//                        .setMaximumTime(10)
//                        .allowReducibleClasses(false)
                        .allowReducibleClasses(false)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(BASEPATH, "transition_graph_agent-2.png"))
                        .setOutputStatesFolder(new File(BASEPATH + "states/"))
                        .setPrintCanonicalStateLabel(false)
                        .create()
                )
        ;
        return opts;
    }

    public DefaultDynamicSignature signature() {
        return pureSignatureBuilder()
                .newControl("bot", 1).status(ControlStatus.ACTIVE).assign()
                .newControl("blocked", 0).status(ControlStatus.ACTIVE).assign()
                .newControl("T", 0).status(ControlStatus.ATOMIC).assign()
                .newControl("F", 0).status(ControlStatus.ATOMIC).assign()
                .newControl("occupiedBy", 0).status(ControlStatus.ACTIVE).assign()
                .newControl("source", 1).status(ControlStatus.ACTIVE).assign()
                .newControl("target", 1).status(ControlStatus.ACTIVE).assign()
                .newControl("dirClockWise", 0).status(ControlStatus.ATOMIC).assign()
                .newControl("dirAntiClockWise", 0).status(ControlStatus.ATOMIC).assign()
//                .newControl("TopLeftCorner", 4).assign()
////                .newControl("TopEdge", 4).assign()
//                .newControl("TopRightCorner", 4).assign()
////                .newControl("LeftEdge", 4).assign()
////                .newControl("Center", 4).assign()
////                .newControl("RightEdge", 4).assign()
//                .newControl("BottomLeftCorner", 4).assign()
////                .newControl("BottomEdge", 4).assign()
//                .newControl("BottomRightCorner", 4).assign()
                .create();
    }
}
