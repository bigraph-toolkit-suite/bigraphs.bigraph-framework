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
package org.bigraphs.framework.simulation.examples.subbrs;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.EcoreBigraph;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystemPredicate;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.bigraphs.framework.simulation.examples.BaseExampleTestSupport;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
@Disabled
public class SubBRSUnitTest extends BaseExampleTestSupport {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/subbrs/";
    private final static boolean AUTO_CLEAN_BEFORE = true;

    public SubBRSUnitTest() {
        super(TARGET_DUMP_PATH, AUTO_CLEAN_BEFORE);
    }

    @BeforeAll
    static void setUp() throws IOException {
        if (AUTO_CLEAN_BEFORE) {
            File dump = new File(TARGET_DUMP_PATH);
            dump.mkdirs();
            FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
            new File(TARGET_DUMP_PATH + "states/").mkdir();
            new File(TARGET_DUMP_PATH + "states2/").mkdir();
        }
    }

    private DynamicSignature sigMain() {
        DynamicSignature sigM = pureSignatureBuilder()
                .add("Item", 0)
                .add("Queue", 0)
                .add("Store", 0)
                .add("Waiting", 0, ControlStatus.PASSIVE)
                .add("Ready", 0, ControlStatus.PASSIVE)
                .add("Finished", 0, ControlStatus.PASSIVE)
                .create();


        return BigraphUtil.mergeSignatures(sigM, sigSub());
    }

    private DynamicSignature sigSub() {
        return pureSignatureBuilder()
                .add("Plus", 0)
                .add("Sum", 0)
                .add("S", 0)
                .add("Z", 0)
                .add("Result", 0)
                .add("Left", 0)
                .add("Right", 0)
                .create();
    }

    @Test
    void simulate() throws Exception {
        MainBRS system = new MainBRS(createMainModel());
        system.execute();
    }

    private class MainBRS extends PureReactiveSystem implements BigraphModelChecker.ReactiveSystemListener<PureBigraph> {

        public MainBRS(PureBigraph mainModel) throws Exception {
            setAgent(mainModel);
            toPNG(getAgent(), "mainModel", TARGET_DUMP_PATH);
            ReactionRule<PureBigraph> mainR1 = mainR1();
            ReactionRule<PureBigraph> mainR2 = mainR2();
            ReactionRule<PureBigraph> mainR3 = mainR3();
            addReactionRule(mainR1);
            addReactionRule(mainR2);
            addReactionRule(mainR3);

            toPNG(mainR1.getRedex(), "rrM1_lhs", TARGET_DUMP_PATH);
            toPNG(mainR1.getReactum(), "rrM1_rhs", TARGET_DUMP_PATH);
            toPNG(mainR2.getRedex(), "rrM2_lhs", TARGET_DUMP_PATH);
            toPNG(mainR2.getReactum(), "rrM2_rhs", TARGET_DUMP_PATH);
            toPNG(mainR3.getRedex(), "rrM3_lhs", TARGET_DUMP_PATH);
            toPNG(mainR3.getReactum(), "rrM3_rhs", TARGET_DUMP_PATH);

            SubBigraphMatchPredicate<PureBigraph> predicate = createPredicate();
            addPredicate(predicate);
            toPNG(predicate.getBigraph(), "predicate", TARGET_DUMP_PATH);

            SubBigraphMatchPredicate<PureBigraph> pred2 = createPredicateEmpty();
            addPredicate(pred2);
            toPNG(pred2.getBigraph(), "predicateEmpty", TARGET_DUMP_PATH);
        }

        @Override
        public void onPredicateMatched(PureBigraph currentAgent, ReactiveSystemPredicate<PureBigraph> predicate) {
            BigraphModelChecker.ReactiveSystemListener.super.onPredicateMatched(currentAgent, predicate);
        }

        @Override
        public void onSubPredicateMatched(PureBigraph currentAgent, ReactiveSystemPredicate<PureBigraph> predicate, PureBigraph context, PureBigraph subBigraph, PureBigraph redex, PureBigraph param) {
            if (predicate.equals(getPredicateMap().get("ItemReady"))) {

                toPNG(context, "subContext", TARGET_DUMP_PATH);
                toPNG(subBigraph, "subBigraph", TARGET_DUMP_PATH);
                toPNG(redex, "subRedex", TARGET_DUMP_PATH);
                toPNG(param, "subParam", TARGET_DUMP_PATH);

                try {
                    SubBRS subBRS = new SubBRS(param);
                    PureBigraph result = subBRS.execute();
                    if (result != null) {
                        result = liftSigOfAgent(sigMain(), result);
                        toPNG(result, "resultSubBRS", TARGET_DUMP_PATH);
                        Bigraph<DynamicSignature> newAgent = ops(context).compose(redex).compose(result).getOuterBigraph();
                        toPNG(newAgent, "newAgent", TARGET_DUMP_PATH);

                        MainBRS m2 = new MainBRS((PureBigraph) newAgent);
                        m2.execute();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void execute() throws Exception {
            PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                    this,
                    BigraphModelChecker.SimulationStrategy.Type.BFS,
                    options("states/"));
            modelChecker.setReactiveSystemListener(this);
            modelChecker.execute();

            System.out.println("Number of States = " + modelChecker.getReactionGraph().getGraph().vertexSet().size());
        }
    }

    private class SubBRS extends PureReactiveSystem {

        // particular sub-structure of the main-model used for computing the sum of two integer bigraphs
        public SubBRS(PureBigraph subAgent) throws Exception {

            //We re-load the subAgent instance but with a different signature for the sub-BRS
            PureBigraph bigraph = liftSigOfAgent(sigSub(), subAgent);
            setAgent(bigraph);
            addReactionRule(subR1());
            addReactionRule(subR2());
            addReactionRule(subR3());

            eb(getAgent(), "subModel");
            try {
                eb(getReactionRulesMap().get("r0").getRedex(), "rrM1_lhs");
                eb(getReactionRulesMap().get("r0").getReactum(), "rrM1_rhs");
                eb(getReactionRulesMap().get("r1").getRedex(), "rrM2_lhs");
                eb(getReactionRulesMap().get("r1").getReactum(), "rrM2_rhs");
                eb(getReactionRulesMap().get("r2").getRedex(), "rrM3_lhs");
                eb(getReactionRulesMap().get("r2").getReactum(), "rrM3_rhs");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void eb(Bigraph<?> bigraph, String name) {
            try {
                BigraphGraphvizExporter.toPNG(bigraph, true, new File(TARGET_DUMP_PATH + name + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public PureBigraph execute() throws ReactiveSystemException, BigraphSimulationException {
            PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                    this,
                    BigraphModelChecker.SimulationStrategy.Type.BFS,
                    options("states2/"));
            modelChecker.execute();

            // Get last state
            int l = modelChecker.getReactionGraph().getGraph().vertexSet().size() - 1;
            Optional<ReactionGraph.LabeledNode> first = modelChecker.getReactionGraph().getGraph().vertexSet().stream().filter(x -> x.getLabel().equals("a:" + l)).findFirst();
            if (first.isPresent()) {
                PureBigraph pureBigraph = modelChecker.getReactionGraph().getStateMap().get(first.get().getCanonicalForm());
                return pureBigraph;
            } else
                return null;
        }
    }

    // TODO bigraphUtil
    private PureBigraph liftSigOfAgent(DynamicSignature sig, PureBigraph agent) throws Exception {
        EcoreBigraph.Stub<DynamicSignature> stub = new EcoreBigraph.Stub<>(agent);
        EPackage bMetaModel = createOrGetBigraphMetaModel(sig);

        List<EObject> eObjects = BigraphFileModelManagement.Load.bigraphInstanceModel(
                bMetaModel,
                stub.getInputStreamOfInstanceModel()
        );

        return PureBigraphBuilder.create(
                        sig.getInstanceModel(),
                        bMetaModel,
                        eObjects.getFirst()
                )
                .create();
    }

    private ModelCheckingOptions options(String folder) {
        Path completePath = Paths.get(TARGET_DUMP_PATH, folder, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(10)
                        .setMaximumTime(60)
                        .allowReducibleClasses(false)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(completePath.toUri()))
                        .setPrintCanonicalStateLabel(false)
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + folder))
                        .setFormatsEnabled(List.of(ModelCheckingOptions.ExportOptions.Format.PNG))
                        .create()
                )
        ;
        return opts;
    }

    private PureBigraph createMainModel() throws Exception {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sigMain());

        builder.root()
                .child("Store")
                .down()
                .child("Item").down().child("Waiting").site()
//                .child("Item").down().site()
//                .child("Item").down().site()
                .top().child("Queue")
        ;
        PureBigraph raw = builder.create();
        Bigraph<DynamicSignature> composed = ops(raw).compose(createAddCompAgent(2, 1)).getOuterBigraph();
        return (PureBigraph) composed;
    }

    private PureBigraph createAddCompAgent(final int left, final int right) {
        DynamicSignature signature = sigMain();
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
                .child("Plus")
                .down()
                .child(leftNode)
                .child(rightNode)
        ;
        builder.makeGround();
        return builder.create();
    }

    private ReactionRule<PureBigraph> mainR1() throws Exception {
        DynamicSignature signature = sigMain();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root()
                .child("Store").down()
                .child("Item").down().child("Waiting").site().up()
                .site();

        builder2.root()
                .child("Store")
                .down()
                .child("Item").down().child("Ready").site().up()
                .site();

        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    private SubBigraphMatchPredicate<PureBigraph> createPredicate() throws Exception {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sigMain());

        builder.root()
                .child("Item").down()
                .child("Ready")
                .site()
        ;
        PureBigraph bigraph = builder.create();
        return SubBigraphMatchPredicate.create(bigraph).withLabel("ItemReady");
    }

    private SubBigraphMatchPredicate<PureBigraph> createPredicateEmpty() throws Exception {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sigMain());
        builder.root().child("Item").down().child("Waiting").site();
        PureBigraph bigraph = builder.create();
        return SubBigraphMatchPredicate.create(bigraph).withLabel("ItemWaiting");
    }

    private ReactionRule<PureBigraph> mainR2() throws Exception {
        DynamicSignature signature = sigMain();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root()
                .child("Store")
                .down()
                .child("Item").down()
                .child("Ready").child("Result").down().site().up().up()
                .site();

        builder2.root()
                .child("Store")
                .down()
                .child("Item").down()
                .child("Finished").child("Result").down().site().up().up()
                .site();

        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum); //instantiationMap
        return rr;
    }

    private ReactionRule<PureBigraph> mainR3() throws Exception {
        DynamicSignature signature = sigMain();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root()
                .child("Store")
                .down()
                .child("Item").down()
                .child("Finished").child("Result").down().site().up().up()
                .site().top()
        ;
        builder.root().child("Queue").down().site();

        PureBigraphBuilder<DynamicSignature>.Hierarchy item = builder2.hierarchy("Item").site();
        builder2.root()
                .child("Store").down().site().top();
        builder2.root().child("Queue").down().child(item).site();

        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    /**
     * react r1 = Left.S | Right.S -> Left | Right;
     */
    private ReactionRule<PureBigraph> subR1() throws Exception {
        DynamicSignature signature = sigMain();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root()
                .child("Left").down().child("S").down().site()
                .top()
                .child("Right").down().site()
        ;
        builder2.root()
                .child("Left").down().site()
                .top()
                .child("Right").down().child("S").down().site()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    private ReactionRule<PureBigraph> subR2() throws Exception {
        DynamicSignature signature = sigSub();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root()
                .child("Left").down().child("Z")
                .top()
                .child("Right").down().child("S").down().site()
        ;
        builder2.root()
                .child("S").down().site()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    private ReactionRule<PureBigraph> subR3() throws Exception {
        DynamicSignature signature = sigMain();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root()
                .child("Plus")
                .down().child("S").down().site()
                .top()
        ;
        builder2.root()
                .child("Result")
                .down().child("S").down().site()
                .top()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }
}
