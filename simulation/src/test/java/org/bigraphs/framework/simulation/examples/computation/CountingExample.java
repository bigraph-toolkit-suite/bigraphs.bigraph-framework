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
package org.bigraphs.framework.simulation.examples.computation;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.exceptions.*;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
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
import org.bigraphs.testing.BigraphUnitTestSupport;
import org.jgrapht.Graph;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
@Disabled
public class CountingExample implements BigraphUnitTestSupport {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/counting/";

    @BeforeAll
    static void setUp() throws IOException {
//        System.setProperty("it.uniud.mads.jlibbig.debug", "false");
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    private DynamicSignature createExampleSignature() {
        return pureSignatureBuilder()
                .add("Age", 0)
                .add("S", 0)
                .add("Z", 0)
                .add("True", 0)
                .add("False", 0)
                .add("Left", 0)
                .add("Right", 0)
                .create();
    }

    @Test
    void simulate() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, InvalidReactionRuleException, BigraphSimulationException, ReactiveSystemException {

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        PureBigraph agent_a = createInitialState(3, 4);
        reactiveSystem.setAgent(agent_a);

        toPNG(agent_a, "counting_agent", TARGET_DUMP_PATH);

        ReactionRule<PureBigraph> rr_1 = createReactionRule_1();
        ReactionRule<PureBigraph> rr_2 = createReactionRule_2();
        ReactionRule<PureBigraph> rr_3 = createReactionRule_3();
        reactiveSystem.addReactionRule(rr_1);
        reactiveSystem.addReactionRule(rr_2);
        reactiveSystem.addReactionRule(rr_3);

        assertTrue(reactiveSystem.isSimple());

        toPNG(rr_1.getRedex(), "r1_lhs", TARGET_DUMP_PATH);
        toPNG(rr_1.getReactum(), "r1_rhs", TARGET_DUMP_PATH);
        toPNG(rr_2.getRedex(), "r2_lhs", TARGET_DUMP_PATH);
        toPNG(rr_2.getReactum(), "r2_rhs", TARGET_DUMP_PATH);
        toPNG(rr_3.getRedex(), "r3_lhs", TARGET_DUMP_PATH);
        toPNG(rr_3.getReactum(), "r3_rhs", TARGET_DUMP_PATH);


        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(8)
                        .setMaximumTime(30)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(Paths.get(TARGET_DUMP_PATH, "transition_graph.png").toFile())
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .setPrintCanonicalStateLabel(false)
                        .setFormatsEnabled(List.of(ModelCheckingOptions.ExportOptions.Format.PNG))
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.execute();

        ReactionGraphAnalysis<PureBigraph> analysis = ReactionGraphAnalysis.createInstance();
        List<ReactionGraphAnalysis.StateTrace<PureBigraph>> pathsToLeaves = analysis.findAllPathsInGraphToLeaves(modelChecker.getReactionGraph());
        pathsToLeaves.forEach(x -> {
            System.out.println("Path has length: " + x.getPath().size());
        });

        Graph<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> graph = modelChecker.getReactionGraph().getGraph();
        assertEquals(5, graph.vertexSet().size());
        assertEquals(4, graph.edgeSet().size());
        System.out.println(new ArrayList<>(graph.vertexSet()).getFirst().getCanonicalForm());
        System.out.println(new ArrayList<>(graph.vertexSet()).get(graph.vertexSet().size() - 1).getCanonicalForm());

        String s1 = new ArrayList<>(graph.vertexSet()).getFirst().getCanonicalForm();
        String s2 = new ArrayList<>(graph.vertexSet()).get(graph.vertexSet().size() - 1).getCanonicalForm();
        boolean c1 = (s1.startsWith("r0$Age$True") && s1.endsWith("$Z#")) ||
                s2.startsWith("r0$Age$True") && s2.endsWith("$Z#");
        assertTrue(c1);
    }

    /**
     * big numberLeft = Left.S.S.S.S.S.S.Z;
     * big numberRight = Right.S.S.S.S.Z;
     * big start = Age . (numberLeft | numberRight);
     */
    private PureBigraph createInitialState(final int left, final int right) throws ControlIsAtomicException, InvalidArityOfControlException, LinkTypeNotExistsException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        PureBigraphBuilder<DynamicSignature>.Hierarchy leftNode =
                builder.hierarchy(signature.getControlByName("Left"))
                        .child("S");
        PureBigraphBuilder<DynamicSignature>.Hierarchy rightNode =
                builder.hierarchy(signature.getControlByName("Right"))
                        .child("S");

        for (int i = 0; i < left - 1; i++) {
            leftNode = leftNode.down().child("S");
        }
        leftNode = leftNode.down().child("Z").top();
        for (int i = 0; i < right - 1; i++) {
            rightNode = rightNode.down().child("S");
        }
        rightNode = rightNode.down().child("Z").top();

        builder.root()
                .child("Age")
                .down()
                .child(leftNode)
                .child(rightNode)
//                .child(s.top())
        ;
        builder.makeGround();
        return builder.create();
    }

    /**
     * react r1 = Left.S | Right.S -> Left | Right;
     */
    private ReactionRule<PureBigraph> createReactionRule_1() throws ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root()
                .child("Left").down().child("S").down().site()
                .top()
                .child("Right").down().child("S").down().site()
        ;
        builder2.root()
                .child("Left").down().site()
                .top()
                .child("Right").down().site()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    /**
     * react r2 = Left.Z | Right.S -> True;
     */
    private ReactionRule<PureBigraph> createReactionRule_2() throws ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root()
                .child("Left").down().child("Z")
                .top()
                .child("Right").down().child("S").down().site()
        ;
        builder2.root()
                .child("True").down().site()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    /**
     * react r3 = Left | Right.Z -> False;
     */
    private ReactionRule<PureBigraph> createReactionRule_3() throws ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root()
                .child("Left").down().site()
                .top()
                .child("Right").down().child("Z")
        ;
        builder2.root()
                .child("False").down().site()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }
}
