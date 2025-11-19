/*
 * Copyright (c) 2020-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.matching;

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
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.exceptions.*;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.matching.pure.IHSFilter;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.matching.pure.SubHypergraphIsoSearch;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
@Disabled
public class LinkGraphMatchingTests2 implements org.bigraphs.testing.BigraphUnitTestSupport, BigraphModelChecker.ReactiveSystemListener<PureBigraph> {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/bpmtest/framework/";

    public LinkGraphMatchingTests2() {
    }

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    private DynamicSignature sig() {
        return pureSignatureBuilder().add("A", 2).create();
    }

    // compare also with the bigrapher output
    // our occurrence counts are doubled because we do not "remove" symmetries
    @Test
    void subhypergraphisosearch() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraph agent = createAgent();
        List<PureBigraph> redexes = new ArrayList<>();
        redexes.add(createRedex1());
        redexes.add(createRedex2());
        redexes.add(createRedex3());
        redexes.add(createRedex4());
        int[] numOfEmbeddings = {12, 0, 0, 2}; // without
        toPNG(agent, "agent", TARGET_DUMP_PATH);
        toPNG(redexes.get(0), "redex4", TARGET_DUMP_PATH);
        for (int i = 0; i < redexes.size(); i++) {
            PureBigraph redex = redexes.get(i);
            SubHypergraphIsoSearch search = new SubHypergraphIsoSearch(redex, agent);
            search.embeddings();
            System.out.println(search.getCandidates());
            System.out.println(search.getEmbeddingSet());
            assertEquals(numOfEmbeddings[i], search.getEmbeddingSet().size());
        }
    }

    @Test
    void matchTest_redex4() throws InvalidConnectionException, TypeNotExistsException, IOException, InvalidReactionRuleException, BigraphSimulationException, IncompatibleInterfaceException {
        PureBigraph agent = createAgent();
        PureBigraph redex = createRedex4();
        toPNG(agent, "agent", TARGET_DUMP_PATH);
        toPNG(redex, "redex4", TARGET_DUMP_PATH);

        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agent, new ParametricReactionRule<>(redex, redex));
        Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
        int transition = 0;
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            createGraphvizOutput(agent, next, TARGET_DUMP_PATH + "redex4/" + (transition) + "/");
            transition++;
        }
        assertEquals(2, transition);
    }

    @Test
    void modelCheckerTest_redex4_test() throws InvalidConnectionException, TypeNotExistsException, BigraphSimulationException, InvalidReactionRuleException, ReactiveSystemException {
        PureBigraph agent = createAgent();
        PureBigraph redex = createRedex4();
        toPNG(agent, "agent", TARGET_DUMP_PATH);
        toPNG(redex, "redex4", TARGET_DUMP_PATH);

        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(50)
                        .setMaximumTime(60)
                        .allowReducibleClasses(false)
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

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(createReactionRule4());
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
//        modelChecker.setReactiveSystemListener(this);
        modelChecker.execute();
        assertTrue(Files.exists(completePath));
    }

    @Test
    void modelCheckerTest_redex1_test() throws InvalidConnectionException, TypeNotExistsException, BigraphSimulationException, InvalidReactionRuleException, ReactiveSystemException {
        PureBigraph agent = createAgent();
        PureBigraph redex = createRedex1();
        toPNG(agent, "agent", TARGET_DUMP_PATH);
        toPNG(redex, "redex1", TARGET_DUMP_PATH);

        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph_redex1.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(50)
                        .setMaximumTime(60)
                        .allowReducibleClasses(false)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(completePath.toFile())
                        .setPrintCanonicalStateLabel(false)
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .create()
                )
        ;

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(createReactionRule4());
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.execute();
        assertTrue(Files.exists(completePath));
    }

    @Test
    void ihsfilter_test() throws Exception {
        PureBigraph agent = createAgent();
        PureBigraph redex = createRedex4();
        toPNG(agent, "agent", TARGET_DUMP_PATH);
        toPNG(redex, "redex4", TARGET_DUMP_PATH);

        IHSFilter ihsFilter = new IHSFilter(redex, agent);

        ihsFilter.condition4(redex.getNodes().get(0), agent.getNodes().get(0));

    }

    private PureBigraph createAgent() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());

//        /e0 (A{y1,y2}.1 | A{y1,e0}.1 | A{y1,e0}.1 | A{y2,e0}.1);

        BigraphEntity.OuterName y1 = builder.createOuter("y1");
        BigraphEntity.OuterName y2 = builder.createOuter("y2");
        BigraphEntity.InnerName e0 = builder.createInner("e0");
        builder.root()
                .child("A").linkOuter(y1).linkOuter(y2)
                .child("A").linkOuter(y1).linkInner(e0)
                .child("A").linkOuter(y1).linkInner(e0)
                .child("A").linkOuter(y2).linkInner(e0)
        ;
        builder.closeInner(e0);
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    //(A{y1,e0}.1 | A{y2,e0}.1) -> (A{y1,e0}.1 | A{y2,e0}.1);
    public PureBigraph createRedex1() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());
        BigraphEntity.OuterName y1 = builder.createOuter("y2");
        BigraphEntity.OuterName y2 = builder.createOuter("e0");
        BigraphEntity.OuterName e0 = builder.createOuter("y1");
        builder.root()
                .child("A").linkOuter(y1).linkOuter(e0)
                .child("A").linkOuter(y2).linkOuter(e0)
        ;

        return builder.create();
    }

    /// e0 (A{y1,e0}.1 | A{y1,e0}.1) -> /e0 (A{y1,e0}.1 | A{y1,e0}.1)
    public PureBigraph createRedex3() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());
        BigraphEntity.OuterName y1 = builder.createOuter("y1");
        BigraphEntity.InnerName e0 = builder.createInner("e0");
        builder.root()
                .child("A").linkOuter(y1).linkInner(e0)
                .child("A").linkOuter(y1).linkInner(e0)
        ;
        builder.closeInner(e0);
        return builder.create();
    }

    /// e0 (/y1 A{y1,e0}.1 | /y2 A{y2,e0}.1) -> /e0 (/y1 A{y1,e0}.1 | /y2 A{y2,e0}.1);
    public PureBigraph createRedex2() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());
        builder.root()
                .connectByEdge("A", "A")
        ;

        return builder.create();
    }


    // (A{y1,e0}.1 | A{y1,e0}.1)
    public PureBigraph createRedex4() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());

        BigraphEntity.OuterName y1 = builder.createOuter("y1");
        BigraphEntity.OuterName e0 = builder.createOuter("e0");
        builder.root()
                .child("A").linkOuter(y1).linkOuter(e0)
                .child("A").linkOuter(y1).linkOuter(e0)
        ;

        return builder.create();
    }

    public ReactionRule<PureBigraph> createReactionRule4() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        return new ParametricReactionRule<>(createRedex4(), createRedex4());
    }

    public ReactionRule<PureBigraph> createReactionRule2() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        return new ParametricReactionRule<>(createRedex2(), createRedex2());
    }

    private SubBigraphMatchPredicate<PureBigraph> createPredicate() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());

        BigraphEntity.OuterName from = builder.createOuter("from");

        // links of car and target must be connected via an outer name otherwise the predicate is not matched
        builder.root()
                .child("Place").linkOuter(from)
//                .down().site().connectByEdge("Target", "Car").down().site();
                .down().site().child("Target", "target").child("Car", "target").down().site();
        PureBigraph bigraph = builder.create();
        return SubBigraphMatchPredicate.create(bigraph);
    }

    private void createGraphvizOutput(Bigraph<?> agent, BigraphMatch<?> next, String path) throws IncompatibleSignatureException, IncompatibleInterfaceException, IOException {
        PureBigraph context = (PureBigraph) next.getContext();
        PureBigraph redex = (PureBigraph) next.getRedex();
        Bigraph contextIdentity = next.getContextIdentity();
        if (context != null && contextIdentity != null && redex != null) {
            PureBigraph contextComposed = (PureBigraph) ops(context).parallelProduct(contextIdentity).getOuterBigraph();

            BigraphGraphvizExporter.toPNG(contextComposed,
                    true,
                    new File(path + "contextComposed.png")
            );

            BigraphGraphvizExporter.toPNG(context,
                    true,
                    new File(path + "context.png")
            );
            BigraphGraphvizExporter.toPNG(agent,
                    true,
                    new File(path + "agent.png")
            );
            BigraphGraphvizExporter.toPNG(redex,
                    true,
                    new File(path + "redex.png")
            );
        }
    }

}
