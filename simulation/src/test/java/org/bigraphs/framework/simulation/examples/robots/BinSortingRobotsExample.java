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

/**
 * @author Dominik Grzelak
 */
@Disabled
public class BinSortingRobotsExample extends BaseExampleTestSupport implements BigraphModelChecker.ReactiveSystemListener<PureBigraph> {
    private final static String DUMP_PATH = "src/test/resources/dump/robots/sortings/";

    public BinSortingRobotsExample() {
        super(DUMP_PATH, true);
    }

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(DUMP_PATH));
        new File(DUMP_PATH + "states/").mkdir();
    }

    private DynamicSignature sig() {
        return pureSignatureBuilder()
                .add("Robot", 0)
                .add("Gripper", 1)
                .add("Table", 0)
                .add("Item", 0)
                .add("Bin", 0)
                .add("ref", 0)
                .add("Lock", 1, ControlStatus.ATOMIC)
                .create();
    }

    @Test
    void simulate() throws Exception {
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();

        PureBigraph agent = agent();
        ReactionRule<PureBigraph> reserveRR = reserve();
        ReactionRule<PureBigraph> pickRR = pick();
        ReactionRule<PureBigraph> reserveBinRR = reserveBin();
        ReactionRule<PureBigraph> placeItemInBinRR = placeItemInBin();
        ReactionRule<PureBigraph> releaseBinLockRR = releaseBinLock();

        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(reserveRR);
        reactiveSystem.addReactionRule(pickRR);
        reactiveSystem.addReactionRule(reserveBinRR);
        reactiveSystem.addReactionRule(placeItemInBinRR);
        reactiveSystem.addReactionRule(releaseBinLockRR);

        // Export Data for Debugging
        printMetaModel(agent);
        toPNG(agent, "agent", DUMP_PATH, false);
        toPNG(reserveRR.getRedex(), "reserveL", DUMP_PATH);
        toPNG(reserveRR.getReactum(), "reserveR", DUMP_PATH);
        toPNG(pickRR.getRedex(), "pickL", DUMP_PATH);
        toPNG(pickRR.getReactum(), "pickR", DUMP_PATH);
        toPNG(reserveBinRR.getRedex(), "reserveBinL", DUMP_PATH);
        toPNG(reserveBinRR.getReactum(), "reserveBinR", DUMP_PATH);
        toPNG(placeItemInBinRR.getRedex(), "placeItemInBinL", DUMP_PATH);
        toPNG(placeItemInBinRR.getReactum(), "placeItemInBinR", DUMP_PATH);
        toPNG(releaseBinLockRR.getRedex(), "releaseBinLockL", DUMP_PATH);
        toPNG(releaseBinLockRR.getReactum(), "releaseBinLockR", DUMP_PATH);

        // Create the model checker
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts());
        modelChecker.setReactiveSystemListener(this);
        modelChecker.execute();

        DOTReactionGraphExporter exporter = new DOTReactionGraphExporter();
        String dotFile = exporter.toString(modelChecker.getReactionGraph());
        System.out.println(dotFile);
        exporter.toOutputStream(modelChecker.getReactionGraph(), new FileOutputStream(DUMP_PATH + "reaction_graph.dot"));
    }

    private ModelCheckingOptions opts() {
        Path completePath = Paths.get(DUMP_PATH, "transition_graph.png");
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
                        .setOutputStatesFolder(new File(DUMP_PATH + "states/"))
                        .setFormatsEnabled(List.of(ModelCheckingOptions.ExportOptions.Format.PNG))
                        .create()
                )
        ;
        return opts;
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
        return new ParametricReactionRule<>(redex, reactum);
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
        return new ParametricReactionRule<>(redex, reactum);
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
