package org.bigraphs.framework.simulation.examples.robots;

import org.bigraphs.framework.converter.dot.DOTReactionGraphExporter;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystemPredicate;
import org.bigraphs.framework.simulation.examples.BaseExampleTestSupport;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

public class RobotSortingExample extends BaseExampleTestSupport implements BigraphModelChecker.ReactiveSystemListener<PureBigraph> {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/robots/sortings/";

    public RobotSortingExample() {
        super(TARGET_DUMP_PATH, true);
    }

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    @Test
    void simulate() throws Exception {

        PureBigraph agent = agent();
        printMetaModel(agent);
        eb(agent, "agent", false);

        ReactionRule<PureBigraph> reserveRR = reserve();
        eb(reserveRR.getRedex(), "reserveL");
        eb(reserveRR.getReactum(), "reserveR");
        ReactionRule<PureBigraph> pickRR = pick();
        eb(pickRR.getRedex(), "pickL");
        eb(pickRR.getReactum(), "pickR");
        ReactionRule<PureBigraph> reserveBinRR = reserveBin();
        eb(reserveBinRR.getRedex(), "reserveBinL");
        eb(reserveBinRR.getReactum(), "reserveBinR");
        ReactionRule<PureBigraph> placeItemInBinRR = placeItemInBin();
        eb(placeItemInBinRR.getRedex(), "placeItemInBinL");
        eb(placeItemInBinRR.getReactum(), "placeItemInBinR");
        ReactionRule<PureBigraph> releaseBinLockRR = releaseBinLock();
        eb(releaseBinLockRR.getRedex(), "releaseBinLockL");
        eb(releaseBinLockRR.getReactum(), "releaseBinLockR");

//        print(insertCoinRR.getRedex());
//        print(insertCoinRR.getReactum());


        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(reserveRR);
        reactiveSystem.addReactionRule(pickRR);
        reactiveSystem.addReactionRule(reserveBinRR);
        reactiveSystem.addReactionRule(placeItemInBinRR);
        reactiveSystem.addReactionRule(releaseBinLockRR);

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts());
        modelChecker.setReactiveSystemListener(this);
        modelChecker.execute();

        DOTReactionGraphExporter exporter = new DOTReactionGraphExporter();
        String dotFile = exporter.toString(modelChecker.getReactionGraph());
        System.out.println(dotFile);
        exporter.toOutputStream(modelChecker.getReactionGraph(), new FileOutputStream(TARGET_DUMP_PATH + "reaction_graph.dot"));
    }

    private ModelCheckingOptions opts() {
        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(50)
                        .setMaximumTime(60)
                        .allowReducibleClasses(true)
                        .rewriteOpenLinks(true)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(completePath.toUri()))
                        .setPrintCanonicalStateLabel(false)
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .setFormatsEnabled(List.of(ModelCheckingOptions.ExportOptions.Format.PNG))
                        .create()
                )
        ;
        return opts;
    }

    private DefaultDynamicSignature sig() {
        DynamicSignatureBuilder sb = pureSignatureBuilder();
        DefaultDynamicSignature sig = sb
                .addControl("Robot", 0)
                .addControl("Gripper", 1)
                .addControl("Table", 0)
                .addControl("Item", 0)
                .addControl("Bin", 0)
                .addControl("ref", 0)
                .addControl("Lock", 1, ControlStatus.ATOMIC)
                .create();
        return sig;
    }

    private PureBigraph agent() throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig());
        builder
                .createRoot()
                .addChild("Robot").down()
                .addChild("Gripper", "canGrab").down().addChild("ref").up().up()
                .addChild("Robot").down()
                .addChild("Gripper", "canGrab2").down().addChild("ref").up().up()
                .addChild("Table").down()
                /**/.addChild("Item").down().addChild("ref").up()
                /**/.addChild("Item").down().addChild("ref").up()
                /**/.addChild("Item").down().addChild("ref").up()
                /**/.addChild("Bin").down()
                /*    */.addChild("ref").up();

        return builder.createBigraph();
    }

    private ReactionRule<PureBigraph> reserve() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> bRed = pureBuilder(sig());
        PureBigraphBuilder<DefaultDynamicSignature> bRec = pureBuilder(sig());

        bRed.createRoot().addChild("Gripper", "canGrab").down().addChild("ref");
        bRed.createRoot().addChild("Table").down().addSite().addChild("Item").down().addChild("ref");

//        bRec.createOuterName("grabMe");
        bRec.createRoot().addChild("Gripper", "canGrab").down()
                /**/.addChild("ref").down()
                /*    */.addChild("Lock", "canGrab").up();
        bRec.createRoot().addChild("Table").down().addSite()
                .addChild("Item").down()
                /**/.addChild("ref").down().
                /*    */addChild("Lock", "canGrab");

        PureBigraph redex = bRed.createBigraph();
        PureBigraph reactum = bRec.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private ReactionRule<PureBigraph> pick() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> bRed = pureBuilder(sig());
        PureBigraphBuilder<DefaultDynamicSignature> bRec = pureBuilder(sig());

        bRed.createRoot().addChild("Gripper", "canGrab").down()
                .addChild("ref").down()
                .addChild("Lock", "canGrab").top();
        bRed.createRoot()
                .addChild("Table").down()
                .addSite()
                .addChild("Item").down()
                /**/.addChild("ref").down()
                /*    */.addChild("Lock", "canGrab");

        bRec.createRoot().addChild("Gripper", "canGrab").down()
                .addChild("ref").down()
                .addChild("Lock", "canGrab").up()
                .addChild("Item").down()
                /**/.addChild("ref").down().addChild("Lock", "canGrab").top();
        bRec.createRoot()
                .addChild("Table").down()
                .addSite();

        PureBigraph redex = bRed.createBigraph();
        PureBigraph reactum = bRec.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private ReactionRule<PureBigraph> reserveBin() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> bRed = pureBuilder(sig());
        PureBigraphBuilder<DefaultDynamicSignature> bRec = pureBuilder(sig());

        bRed.createRoot()
                .addChild("Gripper", "canGrab").down()
                .addChild("ref").down().addChild("Lock", "canGrab").up()
                .addChild("Item").down().addChild("ref").down().addChild("Lock", "canGrab").up();
        bRed.createRoot().addChild("Bin").down()
                .addSite()
                .addChild("ref")
        ;

        bRec.createRoot()
                .addChild("Gripper", "canGrab").down()
                .addChild("ref").down().addChild("Lock", "canGrab").up()
                .addChild("Item").down().addChild("ref").down().addChild("Lock", "canGrab").up();
        bRec.createRoot().addChild("Bin").down()
                .addSite()
                .addChild("ref").down().addChild("Lock", "canGrab")
        ;

        PureBigraph redex = bRed.createBigraph();
        PureBigraph reactum = bRec.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private ReactionRule<PureBigraph> placeItemInBin() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> bRed = pureBuilder(sig());
        PureBigraphBuilder<DefaultDynamicSignature> bRec = pureBuilder(sig());

        bRed.createRoot()
                .addChild("Gripper", "canGrab").down()
                .addChild("ref").down().addChild("Lock", "canGrab").up()
                .addChild("Item").down().addChild("ref").down()
                .addChild("Lock", "canGrab").up();
        bRed.createRoot().addChild("Bin").down()
                .addSite()
                .addChild("ref").down().addChild("Lock", "canGrab")
        ;

        bRec.createRoot()
                .addChild("Gripper", "canGrab").down()
                .addChild("ref").down().addChild("Lock", "canGrab").up();
        bRec.createRoot().addChild("Bin").down()
                .addSite()
                .addChild("ref").down().addChild("Lock", "canGrab").up()
                .addChild("Item").down().addChild("ref").down().addChild("Lock", "canGrab").up()
        ;

        PureBigraph redex = bRed.createBigraph();
        PureBigraph reactum = bRec.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private ReactionRule<PureBigraph> releaseBinLock() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> bRed = pureBuilder(sig());
        PureBigraphBuilder<DefaultDynamicSignature> bRec = pureBuilder(sig());

        bRed.createRoot()
                .addChild("Gripper", "canGrab").down()
                .addChild("ref").down().addChild("Lock", "canGrab").top()
        ;
        bRed.createRoot()
                .addChild("Bin").down()
                .addSite()
                .addChild("ref").down().addChild("Lock", "canGrab").up()
                .addChild("Item").down()
                .addChild("ref").down().addChild("Lock", "canGrab").up()
        ;


        bRec.createRoot()
                .addChild("Gripper", "canGrab").down().addChild("ref")
        ;
        bRec.createRoot().addChild("Bin").down()
                .addSite()
                .addChild("ref")
                .addChild("Item").down().addChild("ref").up()
        ;

        PureBigraph redex = bRed.createBigraph();
        PureBigraph reactum = bRec.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    @Override
    public void onPredicateMatched(PureBigraph currentAgent, ReactiveSystemPredicate<PureBigraph> predicate) {
        System.out.println("pred matched");
    }

    @Override
    public void onAllPredicateMatched(PureBigraph currentAgent, String label) {
        System.out.println("all matched");
    }

}
