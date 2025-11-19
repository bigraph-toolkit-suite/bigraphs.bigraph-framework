/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.examples;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphEncoder;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class RouteFinding implements BigraphModelChecker.ReactiveSystemListener<PureBigraph> {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/cars/framework/";
    boolean carArrivedAtTarget = false;
    long startTime = System.currentTimeMillis();
    long finishTime = System.currentTimeMillis();

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    @Override
    public void onAllPredicateMatched(PureBigraph currentAgent, String label) {
        System.out.println("Car arrived at the target");
        System.out.println(label);
        finishTime = System.currentTimeMillis();
        System.out.println("Time elapsed: " + (finishTime - startTime) + " ms");
        carArrivedAtTarget = true;
    }

    @Test
    void simulate() throws InvalidConnectionException, TypeNotExistsException, IOException, InvalidReactionRuleException, BigraphSimulationException, ReactiveSystemException {
        SubBigraphMatchPredicate<PureBigraph> predicate = createPredicate();
        BigraphGraphvizExporter.toPNG(predicate.getBigraphToMatch(),
                true,
                new File(TARGET_DUMP_PATH + "predicate_car.png")
        );

//        PureBigraph map = createMapSimple(8);
        PureBigraph map = createMap(8);
        BigraphGraphvizExporter.toPNG(map,
                true,
                new File(TARGET_DUMP_PATH + "map_car.png")
        );

        ReactionRule<PureBigraph> reactionRule = createReactionRule();
        BigraphGraphvizExporter.toPNG(reactionRule.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "redex_car.png")
        );
        BigraphGraphvizExporter.toPNG(reactionRule.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "reactum_car.png")
        );
        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
        System.out.println(encoder.encode(reactionRule.getRedex()));
        System.out.println(encoder.encode(reactionRule.getReactum()));

        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .doMeasureTime(true)
                .setReactionGraphWithCycles(false)
                .and(transitionOpts()
                        .setMaximumTransitions(150)
                        .setMaximumTime(60)
                        .allowReducibleClasses(false)
                        .create()
                )
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(completePath.toUri()))
                        .setPrintCanonicalStateLabel(false)
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .setFormatsEnabled(List.of(ModelCheckingOptions.ExportOptions.Format.XMI, ModelCheckingOptions.ExportOptions.Format.PNG))
//                        .disableAllFormats()
                        .create()
                )
        ;

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.addReactionRule(reactionRule);
        reactiveSystem.setAgent(map);
        reactiveSystem.addPredicate(predicate);
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.setReactiveSystemListener(this);
        modelChecker.execute();
        assertTrue(Files.exists(completePath));
        assertTrue(carArrivedAtTarget);
    }

    private PureBigraph createMap(int fuelLevel) throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignature());

        BigraphEntity.OuterName p0 = builder.createOuter("p0");
        BigraphEntity.OuterName p1 = builder.createOuter("p1");
        BigraphEntity.OuterName p2 = builder.createOuter("p2");
        BigraphEntity.OuterName p3 = builder.createOuter("p3");
        BigraphEntity.OuterName p4 = builder.createOuter("p4");
        BigraphEntity.OuterName p5 = builder.createOuter("p5");
        BigraphEntity.OuterName p7 = builder.createOuter("p7");
        BigraphEntity.OuterName target = builder.createOuter("target");

        PureBigraphBuilder<DynamicSignature>.Hierarchy car = builder.hierarchy("Car").linkOuter(target);
        for (int i = 0; i < fuelLevel; i++) {
            car = car.child("Fuel");
        }
        builder.root()
                .child("Place").linkOuter(p0).down().child(car).child("Road").linkOuter(p1).child("Road").linkOuter(p3).up()
                .child("Place").linkOuter(p1).down().child("Road").linkOuter(p2).child("Road").linkOuter(p4).up()
                .child("Place").linkOuter(p2).down().child("Road").linkOuter(p5).up()
                .child("Place").linkOuter(p3).down().child("Road").linkOuter(p4).child("Road").linkOuter(p7).up()
                .child("Place").linkOuter(p4).down().child("Road").linkOuter(p5).child("Road").linkOuter(p1).up()
                .child("Place").linkOuter(p7).down().child("Road").linkOuter(p2).child("Target").linkOuter(target).up()
        ;
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    private PureBigraph createMapSimple(int fuelLevel) throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignature());

        BigraphEntity.OuterName p0 = builder.createOuter("p0");
        BigraphEntity.OuterName p1 = builder.createOuter("p1");
        BigraphEntity.OuterName p3 = builder.createOuter("p3");
        BigraphEntity.OuterName p4 = builder.createOuter("p4");
        BigraphEntity.OuterName target = builder.createOuter("target");

        PureBigraphBuilder<DynamicSignature>.Hierarchy car = builder.hierarchy("Car").linkOuter(target);
        for (int i = 0; i < fuelLevel; i++) {
            car = car.child("Fuel");
        }
        builder.root()
                .child("Place").linkOuter(p0).down().child(car).child("Road").linkOuter(p1).child("Road").linkOuter(p3).up()
                .child("Place").linkOuter(p1).down().child("Road").child("Road").linkOuter(p4).up()
                .child("Place").linkOuter(p3).down().child("Road").linkOuter(p4).child("Road").up()
        ;
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    /**
     * react r1 = Left.S | Right.S -> Left | Right;
     */
    public static ReactionRule<PureBigraph> createReactionRule() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(createSignature());

        BigraphEntity.OuterName fromD = builder.createOuter("fromD");
        BigraphEntity.OuterName fromS = builder.createOuter("fromS");
        BigraphEntity.OuterName target = builder.createOuter("target");

        Supplier<PureBigraphBuilder.Hierarchy> car = () -> {
            PureBigraphBuilder<DynamicSignature>.Hierarchy car1 = null;
            try {
                car1 = builder.hierarchy("Car").linkOuter(target).site().child("Fuel");
            } catch (TypeNotExistsException | InvalidConnectionException e) {
                e.printStackTrace();
            }
            return car1;
        };

        builder.root()
                .child("Place").linkOuter(fromD).down().site().top()
                .child("Place").linkOuter(fromS).down().child(car.get()).site().child("Road").linkOuter(fromD).top()
        ;

        BigraphEntity.OuterName fromD2 = builder2.createOuter("fromD");
        BigraphEntity.OuterName fromS2 = builder2.createOuter("fromS");
        BigraphEntity.OuterName target2 = builder2.createOuter("target");
        Supplier<PureBigraphBuilder.Hierarchy> car2 = () -> {
            PureBigraphBuilder<DynamicSignature>.Hierarchy car1 = null;
            try {
                car1 = builder2.hierarchy("Car").linkOuter(target2).site();
            } catch (TypeNotExistsException | InvalidConnectionException e) {
                e.printStackTrace();
            }
            return car1;
        };

        builder2.root()
                .child("Place").linkOuter(fromD2).down().site().child(car2.get()).top()
                .child("Place").linkOuter(fromS2).down().child("Road").linkOuter(fromD2).site().top()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    private SubBigraphMatchPredicate<PureBigraph> createPredicate() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignature());

        BigraphEntity.OuterName from = builder.createOuter("from");

        // links of car and target must be connected via an outer name otherwise the predicate is not matched
        builder.root()
                .child("Place").linkOuter(from)
                .down().site().child("Target", "target").child("Car", "target").down().site();
        PureBigraph bigraph = builder.create();
        return SubBigraphMatchPredicate.create(bigraph);
    }

    public static DynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add("Car", 1)
                .add("Fuel", 0)
                .add("Place", 1)
                .add("Road", 1)
                .add("Target", 1)
        ;
        return defaultBuilder.create();
    }
}
