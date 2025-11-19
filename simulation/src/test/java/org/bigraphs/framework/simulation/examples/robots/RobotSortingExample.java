/*
 * Copyright (c) 2022-2025 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.simulation.examples.robots;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.converter.dot.DOTReactionGraphExporter;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystemPredicate;
import org.bigraphs.framework.simulation.examples.BaseExampleTestSupport;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
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

    private DynamicSignature sig() {
        DynamicSignatureBuilder sb = pureSignatureBuilder();
        DynamicSignature sig = sb
                .add("Robot", 0)
                .add("Gripper", 1)
                .add("Table", 0)
                .add("Item", 0)
                .add("Bin", 0)
                .add("ref", 0)
                .add("Lock", 1, ControlStatus.ATOMIC)
                .create();
        return sig;
    }

    private PureBigraph agent() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());
        builder
                .root()
                .child("Robot").down()
                .child("Gripper", "canGrab").down().child("ref").up().up()
                .child("Robot").down()
                .child("Gripper", "canGrab2").down().child("ref").up().up()
                .child("Table").down()
                /**/.child("Item").down().child("ref").up()
                /**/.child("Item").down().child("ref").up()
                /**/.child("Item").down().child("ref").up()
                /**/.child("Bin").down()
                /*    */.child("ref").up();

        return builder.create();
    }

    private ReactionRule<PureBigraph> reserve() throws Exception {
        PureBigraphBuilder<DynamicSignature> bRed = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> bRec = pureBuilder(sig());

        bRed.root().child("Gripper", "canGrab").down().child("ref");
        bRed.root().child("Table").down().site().child("Item").down().child("ref");

//        bRec.createOuterName("grabMe");
        bRec.root().child("Gripper", "canGrab").down()
                /**/.child("ref").down()
                /*    */.child("Lock", "canGrab").up();
        bRec.root().child("Table").down().site()
                .child("Item").down()
                /**/.child("ref").down().
                /*    */child("Lock", "canGrab");

        PureBigraph redex = bRed.create();
        PureBigraph reactum = bRec.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private ReactionRule<PureBigraph> pick() throws Exception {
        PureBigraphBuilder<DynamicSignature> bRed = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> bRec = pureBuilder(sig());

        bRed.root().child("Gripper", "canGrab").down()
                .child("ref").down()
                .child("Lock", "canGrab").top();
        bRed.root()
                .child("Table").down()
                .site()
                .child("Item").down()
                /**/.child("ref").down()
                /*    */.child("Lock", "canGrab");

        bRec.root().child("Gripper", "canGrab").down()
                .child("ref").down()
                .child("Lock", "canGrab").up()
                .child("Item").down()
                /**/.child("ref").down().child("Lock", "canGrab").top();
        bRec.root()
                .child("Table").down()
                .site();

        PureBigraph redex = bRed.create();
        PureBigraph reactum = bRec.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private ReactionRule<PureBigraph> reserveBin() throws Exception {
        PureBigraphBuilder<DynamicSignature> bRed = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> bRec = pureBuilder(sig());

        bRed.root()
                .child("Gripper", "canGrab").down()
                .child("ref").down().child("Lock", "canGrab").up()
                .child("Item").down().child("ref").down().child("Lock", "canGrab").up();
        bRed.root().child("Bin").down()
                .site()
                .child("ref")
        ;

        bRec.root()
                .child("Gripper", "canGrab").down()
                .child("ref").down().child("Lock", "canGrab").up()
                .child("Item").down().child("ref").down().child("Lock", "canGrab").up();
        bRec.root().child("Bin").down()
                .site()
                .child("ref").down().child("Lock", "canGrab")
        ;

        PureBigraph redex = bRed.create();
        PureBigraph reactum = bRec.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private ReactionRule<PureBigraph> placeItemInBin() throws Exception {
        PureBigraphBuilder<DynamicSignature> bRed = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> bRec = pureBuilder(sig());

        bRed.root()
                .child("Gripper", "canGrab").down()
                .child("ref").down().child("Lock", "canGrab").up()
                .child("Item").down().child("ref").down()
                .child("Lock", "canGrab").up();
        bRed.root().child("Bin").down()
                .site()
                .child("ref").down().child("Lock", "canGrab")
        ;

        bRec.root()
                .child("Gripper", "canGrab").down()
                .child("ref").down().child("Lock", "canGrab").up();
        bRec.root().child("Bin").down()
                .site()
                .child("ref").down().child("Lock", "canGrab").up()
                .child("Item").down().child("ref").down().child("Lock", "canGrab").up()
        ;

        PureBigraph redex = bRed.create();
        PureBigraph reactum = bRec.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private ReactionRule<PureBigraph> releaseBinLock() throws Exception {
        PureBigraphBuilder<DynamicSignature> bRed = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> bRec = pureBuilder(sig());

        bRed.root()
                .child("Gripper", "canGrab").down()
                .child("ref").down().child("Lock", "canGrab").top()
        ;
        bRed.root()
                .child("Bin").down()
                .site()
                .child("ref").down().child("Lock", "canGrab").up()
                .child("Item").down()
                .child("ref").down().child("Lock", "canGrab").up()
        ;


        bRec.root()
                .child("Gripper", "canGrab").down().child("ref")
        ;
        bRec.root().child("Bin").down()
                .site()
                .child("ref")
                .child("Item").down().child("ref").up()
        ;

        PureBigraph redex = bRed.create();
        PureBigraph reactum = bRec.create();
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
