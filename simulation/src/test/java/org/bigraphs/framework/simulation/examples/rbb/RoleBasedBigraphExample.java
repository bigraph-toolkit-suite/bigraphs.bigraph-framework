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
package org.bigraphs.framework.simulation.examples.rbb;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

import it.uniud.mads.jlibbig.core.std.Bigraph;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphDecoder;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphEncoder;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.examples.BaseExampleTestSupport;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class RoleBasedBigraphExample extends BaseExampleTestSupport {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/rbb/";
    private final static boolean AUTO_CLEAN_BEFORE = true;

    public RoleBasedBigraphExample() {
        super(TARGET_DUMP_PATH, AUTO_CLEAN_BEFORE);
    }

    @BeforeAll
    static void setUp() throws IOException {
        System.setProperty("it.uniud.mads.jlibbig.debug", "false");
        if (AUTO_CLEAN_BEFORE) {
            File dump = new File(TARGET_DUMP_PATH);
            dump.mkdirs();
            FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
            new File(TARGET_DUMP_PATH + "states/").mkdir();
        }
    }


    @Test
    void test() throws Exception {
        PureBigraph agent0 = createAgent();
//        eb(agent0, "agent");
        PureBigraph agent = finalizeAgent(agent0);
        eb(agent, "agent");

        List<ReactionRule<PureBigraph>> incDec_Balance = increaseDecrease_Balance();
        eb(incDec_Balance.get(0).getRedex(), "incBalanceLHS");
        eb(incDec_Balance.get(0).getReactum(), "incBalanceRHS");
        eb(incDec_Balance.get(1).getRedex(), "decBalanceLHS");
        eb(incDec_Balance.get(1).getReactum(), "decBalanceRHS");

        ReactionRule<PureBigraph> bindSourceRole = bindSourceRole(false);
        ReactionRule<PureBigraph> bindTargetRole = bindTargetRole(false);
        ReactionRule<PureBigraph> fixedbindSourceRole = bindSourceRole(true);
        ReactionRule<PureBigraph> fixedbindTargetRole = bindTargetRole(true);
        eb(bindSourceRole.getRedex(), "bindSourceLHS");
        eb(bindSourceRole.getReactum(), "bindSourceRHS");
        eb(bindTargetRole.getRedex(), "bindTargetLHS");
        eb(bindTargetRole.getReactum(), "bindTargetRHS");
        eb(fixedbindSourceRole.getRedex(), "fixedbindSourceLHS");
        eb(fixedbindSourceRole.getReactum(), "fixedbindSourceRHS");
        eb(fixedbindTargetRole.getRedex(), "fixedbindTargetLHS");
        eb(fixedbindTargetRole.getReactum(), "fixedbindTargetRHS");

        ReactionRule<PureBigraph> transaction = transaction();
        eb(transaction.getRedex(), "transactionLHS");
        eb(transaction.getReactum(), "transactionRHS");

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
//        reactiveSystem.addReactionRule(incDec_Balance.get(0));
//        reactiveSystem.addReactionRule(incDec_Balance.get(1));
//        reactiveSystem.addReactionRule(bindSourceRole);
//        reactiveSystem.addReactionRule(bindTargetRole);
        reactiveSystem.addReactionRule(fixedbindSourceRole);
        reactiveSystem.addReactionRule(fixedbindTargetRole);

        ModelCheckingOptions modOpts = opts();
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                modOpts);
//        modelChecker.setReactiveSystemListener(this);
        modelChecker.execute();

        //states=51, transitions=80
        System.out.println("Edges: " + modelChecker.getReactionGraph().getGraph().edgeSet().size());
        System.out.println("Vertices: " + modelChecker.getReactionGraph().getGraph().vertexSet().size());
//
//        ReactionGraphAnalysis<PureBigraph> analysis = ReactionGraphAnalysis.createInstance();
//        List<ReactionGraphAnalysis.PathList<PureBigraph>> pathsToLeaves = analysis.findAllPathsInGraphToLeaves(modelChecker.getReactionGraph());
//        System.out.println(pathsToLeaves.size());
    }

    private static PureBigraph finalizeAgent(PureBigraph bigraph) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        Placings<DynamicSignature>.Merge merge = purePlacings(createSignature()).merge(2);
        return ops(merge).compose(bigraph).getOuterBigraph();
    }

    private static PureBigraph createAgent() throws InvalidConnectionException, LinkTypeNotExistsException {
        DynamicSignature signature = createSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        PureBigraphBuilder<DynamicSignature>.Hierarchy root1 = builder.root();
        root1
                .child("AccountUnbound").down().child("Balance").down().child("i0").top()
                .child("AccountUnbound").down().child("Balance").down().child("i0").top()
        ;
//        builder.closeInnerName(tmp);

        PureBigraphBuilder<DynamicSignature>.Hierarchy root2 = builder.root();
        root2.child("Transaction").down().child("Source", "RoleSource").child("Target", "RoleTarget");

        PureBigraph bigraph = builder.create();


        PureBigraph decoded = makeIdleEdges(bigraph);
        return decoded;
    }

    private ReactionRule<PureBigraph> bindSourceRole(boolean withFix) throws Exception {
        DynamicSignature signature = createSignature();
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(signature);

        builderRedex.root()
                .child("AccountUnbound").down().site().top()
        ;
        builderRedex.root()
                .child("Source", "RoleSource")
        ;

        builderReactum.root()
                .child("AccountBound", "RoleSource").down().site().top()
        ;
        if (!withFix) {
            builderReactum.root()
                    .child("Source", "RoleSource")
            ;
        } else {
            builderReactum.root()
                    .child("Source", "RoleSource").down().child("i1")
            ;
        }

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(makeIdleEdges(redex), makeIdleEdges(reactum));
        return rr;
    }

    private ReactionRule<PureBigraph> bindTargetRole(boolean withFix) throws Exception {
        DynamicSignature signature = createSignature();
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(signature);

        builderRedex.root()
                .child("AccountUnbound").down().site().top()
        ;
        builderRedex.root()
                .child("Target", "RoleTarget")
        ;

        builderReactum.root()
                .child("AccountBound", "RoleTarget").down().site().top()
        ;
        if (!withFix) {
            builderReactum.root()
                    .child("Target", "RoleTarget")
            ;
        } else {
            builderReactum.root()
                    .child("Target", "RoleTarget").down().child("i1")
            ;
        }

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(makeIdleEdges(redex), makeIdleEdges(reactum));
        return rr;
    }

    private ReactionRule<PureBigraph> transaction() throws Exception {
        DynamicSignature signature = createSignature();
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(signature);

        builderRedex.root()
                .child("AccountBound", "RoleSource").down().child("Balance").down().site().top()
                .child("AccountBound", "RoleTarget").down().child("Balance").down().site().top()
        ;
        builderRedex.root()
                .child("Source", "RoleSource")
                .child("Target", "RoleTarget")
        ;

        builderReactum.root()
                .child("AccountBound", "RoleSource").down().child("Balance").down().site().top()
                .child("AccountBound", "RoleTarget").down().child("Balance").down().site().top()
        ;
        builderReactum.root()
                .child("Source", "RoleSource")
                .child("Target", "RoleTarget")
        ;

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private List<ReactionRule<PureBigraph>> increaseDecrease_Balance() throws Exception {
        DynamicSignature signature = createSignature();
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(signature);

        builderRedex.root()
//                .child("AccountUnbound")
                .child("Balance").down().child("i0").top();

        builderReactum.root()
//                .child("AccountUnbound")
                .child("Balance").down().child("i1").top();

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        ReactionRule<PureBigraph> rrInverse = new ParametricReactionRule<>(reactum, redex);
        return Arrays.asList(rr, rrInverse);
    }


    private ModelCheckingOptions opts() {
        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(100)
                        .setMaximumTime(60)
                        .allowReducibleClasses(true)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(completePath.toUri()))
                        .setPrintCanonicalStateLabel(false)
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .create()
                )
        ;
        return opts;
    }


    private static DynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("AccountUnbound")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("AccountBound")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Transaction")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Bank")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Source")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Target")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Savings")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Checkings")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Balance")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("i0")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("i1")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;

        return defaultBuilder.create();
    }

    // A quick and dirty way to have loose edges connected to unused ports
    private static PureBigraph makeIdleEdges(PureBigraph bigraph) {
        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
        Bigraph jLibEncoded = encoder.encode(bigraph);
        PureBigraph decoded = decoder.decode(jLibEncoded, bigraph.getSignature());
        return decoded;
    }
}
