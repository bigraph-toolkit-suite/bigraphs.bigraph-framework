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
package org.bigraphs.framework.simulation;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.analysis.ReactionGraphAnalysis;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.*;

/**
 * @author Dominik Grzelak
 */
@Disabled
public class ReactiveSystemUnitTests {

    private final static String DUMP_PATH = "src/test/resources/dump/basic/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(DUMP_PATH));
        new File(DUMP_PATH + "states/").mkdir();
    }

    private DynamicSignature createExampleSignature() {
        return pureSignatureBuilder()
                .add("Building", 0)
                .add("Room", 1)
                .add("Computer", 1)
                .add("Job", 0)
                .create();
    }

    private DynamicSignature createExampleSignatureABB() {
        return pureSignatureBuilder()
                .add("A", 1, ControlStatus.ATOMIC)
                .add("B", 1, ControlStatus.ATOMIC)
                .create();
    }

    @Test
    void simulate_basic_abbExample() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, BigraphSimulationException, ReactiveSystemException {
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        PureBigraph agent_b = (PureBigraph) createAgent_A2();
        reactiveSystem.setAgent(agent_b);
        ReactionRule<PureBigraph> rr = createReactionRuleForA2();
        reactiveSystem.addReactionRule(rr);

        ModelCheckingOptions opts = getOpts("transition_graph_ABB.png", true);

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS, opts);
        modelChecker.execute();
        assertTrue(Files.exists(Paths.get(DUMP_PATH, "transition_graph_ABB.png")));

        Set<ReactionGraph.LabeledNode> labeledNodes = modelChecker.getReactionGraph().getGraph().vertexSet();
        assertEquals(2, labeledNodes.size());

        String s0 = new ArrayList<>(labeledNodes).get(0).getCanonicalForm();
        String s1 = new ArrayList<>(labeledNodes).get(1).getCanonicalForm();

        boolean c10 = s0.contains("r0$A{e0}B{e1}B{e2}#") && s1.contains("r0$A{e0}B{e1}#");
        boolean c01 = s0.contains("r0$A{e0}B{e1}#") && s1.contains("r0$A{e0}B{e1}B{e2}#");

        assertTrue(c10 || c01);
    }

    @Test
    @DisplayName("BFS simulation test")
    void simulate_basic_bfs() throws TypeNotExistsException, InvalidConnectionException, IOException, InvalidReactionRuleException, BigraphSimulationException, ReactiveSystemException {
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        PureBigraph agent = (PureBigraph) createAgent_A();
        reactiveSystem.setAgent(agent);
        ReactionRule<PureBigraph> rr1 = createReactionRule_AddJob();
        ReactionRule<PureBigraph> rr1_1 = createReactionRule_AddJob();
        ReactionRule<PureBigraph> rr2 = createReactionRule_A_SelfApply();
        ReactionRule<PureBigraph> rr3 = createReactionRule_AddOneJob();

        reactiveSystem.addReactionRule(rr1);
        reactiveSystem.addReactionRule(rr1_1);
        reactiveSystem.addReactionRule(rr2);
        reactiveSystem.addReactionRule(rr3);
        assertTrue(reactiveSystem.isSimple());

        ModelCheckingOptions opts = getOpts("transition_graph.png", false);

        SubBigraphMatchPredicate<PureBigraph> pred1 = SubBigraphMatchPredicate.create((PureBigraph) createAgent_A_Final());
        reactiveSystem.addPredicate(pred1);

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts
        );
        modelChecker.execute();
        assertTrue(Files.exists(Paths.get(DUMP_PATH, "transition_graph.png")));

        ReactionGraphAnalysis<PureBigraph> analysis = ReactionGraphAnalysis.createInstance();
        List<ReactionGraphAnalysis.StateTrace<PureBigraph>> pathsToLeaves = analysis.findAllPathsInGraphToLeaves(modelChecker.getReactionGraph());
        Assertions.assertEquals(3, pathsToLeaves.size());
        pathsToLeaves.forEach(x -> {
            System.out.println("Path has length: " + x.getPath().size());
            System.out.println("\t" + x.getStateLabels().toString());
        });
    }

    @Test
    @DisplayName("Random simulation test: Agent is randomly chosen")
    void simulate_random() throws BigraphSimulationException, TypeNotExistsException, InvalidConnectionException, InvalidReactionRuleException, ReactiveSystemException {
        // Create reaction rulesname
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        PureBigraph agent = (PureBigraph) createAgent_A();
        reactiveSystem.setAgent(agent);
        ReactionRule<PureBigraph> rr = createReactionRule_AddJob();
        ReactionRule<PureBigraph> rr1 = createReactionRule_AddOneJob();
        ReactionRule<PureBigraph> rr2 = createReactionRule_AddTwoJobs();
        ReactionRule<PureBigraph> rr3 = createReactionRule_AddThreeJobs();
        ReactionRule<PureBigraph> rr4 = createReactionRule_AddJob2();

        reactiveSystem.addReactionRule(rr);
        reactiveSystem.addReactionRule(rr1);
        reactiveSystem.addReactionRule(rr2);
        reactiveSystem.addReactionRule(rr3);
        reactiveSystem.addReactionRule(rr4);

        ModelCheckingOptions opts = getOpts("transition_graph_random.png", false);

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.RANDOM,
                opts);
        modelChecker.execute();
        assertTrue(Files.exists(Paths.get(DUMP_PATH, "transition_graph_random.png")));
    }

    private @NonNull ModelCheckingOptions getOpts(String image, boolean flag) {
        Path completePath = Paths.get(DUMP_PATH, image);
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(4)
                        .setMaximumTime(30)
                        .allowReducibleClasses(true)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setPrintCanonicalStateLabel(flag)
                        .setReactionGraphFile(new File(completePath.toUri()))
                        .setOutputStatesFolder(new File(DUMP_PATH + "states/"))
                        .setFormatsEnabled(List.of(ModelCheckingOptions.ExportOptions.Format.PNG))
                        .create()
                )
        ;
        return opts;
    }

    private Bigraph<?> createAgent_A() throws ControlIsAtomicException, InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
        ;
        builder.makeGround();
        return builder.create();
    }

    private Bigraph<?> createAgent_A2() throws ControlIsAtomicException, InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = createExampleSignatureABB();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        builder.root()
                .child("A")
                .child("B")
                .child("B")
        ;
        return builder.create();
    }

    private Bigraph<?> createAgent_A_Final() throws ControlIsAtomicException, InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
                .down()
                .child("Job").child("Job")
        ;
        return builder.create();
    }

    /**
     * big rleft = (Room.(Comp{n}.1));
     * big rright = (Room.(Comp{n}.Job.1));
     * <p>
     * react r1 = rleft --> rright;
     */
    private ReactionRule<PureBigraph> createReactionRule() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        BigraphEntity.OuterName network = builder.createOuter("network");
        builder.root()
                .child(signature.getControlByName("Room"))
                .down()
                .child(signature.getControlByName("Computer")).linkOuter(network)
        ;

        BigraphEntity.OuterName network2 = builder2.createOuter("network");
        builder2.root()
                .child(signature.getControlByName("Room"))
                .down()
                .child(signature.getControlByName("Computer")).linkOuter(network2)
                .down()
                .child(signature.getControlByName("Job"))
        ;

        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private ReactionRule<PureBigraph> createReactionRule_A_SelfApply() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        BigraphEntity.OuterName network2 = builder2.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
        ;
        builder2.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network2)
        ;

        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private ReactionRule<PureBigraph> createReactionRule_AddJob2() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        BigraphEntity.OuterName network2 = builder2.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
        ;
        builder2.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network2)
                .down()
                .child("Job").child("Job")
        ;

//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    /**
     * a | b | b -> a | b
     */
    private ReactionRule<PureBigraph> createReactionRuleForA2() throws ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignatureABB();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root().child("A").child("B").child("B");
        builder2.root().child("A").child("B");

        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    private ReactionRule<PureBigraph> createReactionRule_AddJob() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        BigraphEntity.OuterName network2 = builder2.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
        ;
        builder2.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network2)
                .down()
                .child("Job")
        ;

        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private ReactionRule<PureBigraph> createReactionRule_AddOneJob() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        BigraphEntity.OuterName network2 = builder2.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
                .down()
                .child("Job")
        ;
        builder2.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network2)
                .down()
                .child("Job").child("Job")
        ;

        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private ReactionRule<PureBigraph> createReactionRule_AddTwoJobs() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        BigraphEntity.OuterName network2 = builder2.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
                .down()
                .child("Job")
        ;
        builder2.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network2)
                .down()
                .child("Job").child("Job").child("Job")
        ;

        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private ReactionRule<PureBigraph> createReactionRule_AddThreeJobs() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");
        BigraphEntity.OuterName network2 = builder2.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network)
                .down()
                .child("Job")
        ;
        builder2.root()
                .child("Room")
                .down()
                .child("Computer").linkOuter(network2)
                .down()
                .child("Job").child("Job").child("Job").child("Job")
        ;

        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }
}
