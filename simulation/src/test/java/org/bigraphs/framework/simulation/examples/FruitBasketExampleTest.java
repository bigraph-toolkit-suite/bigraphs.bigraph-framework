/*
 * Copyright (c) 2024-2025 Bigraph Toolkit Suite Developers
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

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphDecoder;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphEncoder;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.*;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class FruitBasketExampleTest {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/fruitbasket/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    @Test
    void simulate() throws InvalidConnectionException, IOException, InvalidReactionRuleException, ReactiveSystemException, BigraphSimulationException {
        DynamicSignature sig = createSignature();
        PureBigraph agent = createAgent();
        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
        agent = decoder.decode(encoder.encode(agent), sig);
        BigraphGraphvizExporter.toPNG(agent, true, new File(TARGET_DUMP_PATH + "agent.png"));

        ParametricReactionRule<PureBigraph> rr1 = createRR1();
        BigraphGraphvizExporter.toPNG(rr1.getRedex(), true, new File(TARGET_DUMP_PATH + "rr1_LHS.png"));
        BigraphGraphvizExporter.toPNG(rr1.getReactum(), true, new File(TARGET_DUMP_PATH + "rr1_RHS.png"));

        ParametricReactionRule<PureBigraph> rr2 = createRR2();
        BigraphGraphvizExporter.toPNG(rr2.getRedex(), true, new File(TARGET_DUMP_PATH + "rr2_LHS.png"));
        BigraphGraphvizExporter.toPNG(rr2.getReactum(), true, new File(TARGET_DUMP_PATH + "rr2_RHS.png"));

        ParametricReactionRule<PureBigraph> rr3 = createRR3();
        BigraphGraphvizExporter.toPNG(rr3.getRedex(), true, new File(TARGET_DUMP_PATH + "rr3_LHS.png"));
        BigraphGraphvizExporter.toPNG(rr3.getReactum(), true, new File(TARGET_DUMP_PATH + "rr3_RHS.png"));

        ParametricReactionRule<PureBigraph> rr4 = createRR4();
        BigraphGraphvizExporter.toPNG(rr4.getRedex(), true, new File(TARGET_DUMP_PATH + "rr4_LHS.png"));
        BigraphGraphvizExporter.toPNG(rr4.getReactum(), true, new File(TARGET_DUMP_PATH + "rr4_RHS.png"));

        PureReactiveSystem rs = new PureReactiveSystem();
        rs.setAgent(createAgent());
        rs.addReactionRule(createRR1());
        rs.addReactionRule(createRR2());
        rs.addReactionRule(createRR3());
        rs.addReactionRule(createRR4());
        assert rs.isSimple();

        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(100)
                        .setMaximumTime(60)
                        .allowReducibleClasses(true)
                        .rewriteOpenLinks(false)
                        .create()
                )
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(Paths.get(TARGET_DUMP_PATH, "transition_graph.png").toFile())
                        .setPrintCanonicalStateLabel(false)
                        .setOutputStatesFolder(Paths.get(TARGET_DUMP_PATH, "states/").toFile())
                        .setFormatsEnabled(List.of(ModelCheckingOptions.ExportOptions.Format.PNG, ModelCheckingOptions.ExportOptions.Format.XMI))
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                rs,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.execute();

//        ParametricReactionRule<PureBigraph> rr11 = createRR1();
//        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
//        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agent, rr11);
//        Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
//        while (iterator.hasNext()) {
//            BigraphMatch<PureBigraph> next = iterator.next();
//            PureBigraph result = rs.buildParametricReaction(agent, next, rr11);
//            // do something with `result`
//            System.out.println(result);
//            assert result != null;
//        }

    }

    public static DynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add("User", 1, ControlStatus.ATOMIC)
                .add("Basket", 1)
                .add("Fruit", 1, ControlStatus.ATOMIC)
                .add("Table", 0)
        ;
        return defaultBuilder.create();
    }

    public static PureBigraph createAgent() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(createSignature());

        return b.root()
                .child("User")
                .child("Table").down()
                .child("Basket")
                .child("Fruit", "f")
                .child("Fruit", "f")
                .child("Fruit", "f")
                .create();

    }

    public static PureBigraph createAgent2() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(createSignature());

        return b.root()
                .child("User")
                .child("Table").down()
                .child("Basket")
                .child("Fruit", "f")
                .child("Fruit", "f")
                .child("Fruit", "f")
                .child("Fruit", "f")
                .child("Fruit", "f")
                .create();

    }

    public static ParametricReactionRule<PureBigraph> createRR1() throws InvalidReactionRuleException, InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> b2 = pureBuilder(createSignature());
        b1.root()
                .child("User")
                .child("Table").down()
                .child("Fruit", "f").site()
        ;
        b2.root()
                .child("User", "f")
                .child("Table").down()
                .child("Fruit", "f").site()
        ;
        PureBigraph redex = b1.create();
        PureBigraph reactum = b2.create();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("rr1");
        return rr;
    }

    public static ParametricReactionRule<PureBigraph> createRR2() throws InvalidReactionRuleException, InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> b2 = pureBuilder(createSignature());
        b1.root()
                .child("User", "f")
                .child("Table").down()
                .child("Fruit", "f").site()
                .child("Basket").down().site()
        ;
        b2.root()
                .child("User", "f")
                .child("Table").down()
                .child("Fruit", "f").site()
                .child("Basket", "f").down().site()
        ;
        PureBigraph redex = b1.create();
        PureBigraph reactum = b2.create();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("rr2");
        return rr;
    }

    public static ParametricReactionRule<PureBigraph> createRR3() throws InvalidReactionRuleException, InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> b2 = pureBuilder(createSignature());
        b1.root()
                .child("User", "f")
                .child("Table").down()
                .child("Fruit", "f").site()
                .child("Basket", "f").down().site()
        ;
        b2.root()
                .child("User", "f")
                .child("Table").down()
                .site()
                .child("Basket", "f").down().child("Fruit", "f").site()
        ;
        PureBigraph redex = b1.create();
        PureBigraph reactum = b2.create();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("rr3");
        return rr;
    }

    public static ParametricReactionRule<PureBigraph> createRR4() throws InvalidReactionRuleException, InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> b2 = pureBuilder(createSignature());
        b1.root()
                .child("User", "f")
                .child("Table").down().site()
                .child("Basket", "f").down().child("Fruit", "f").site()
        ;
        b2.createOuter("f");
        b2.root()
                .child("User")
                .child("Table").down().site()
                .child("Basket").down().child("Fruit").site()
        ;
        PureBigraph redex = b1.create();
        PureBigraph reactum = b2.create();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("rr4");
        return rr;
    }

    //every state a self-loop
    public static ParametricReactionRule<PureBigraph> createRR5_noop() throws InvalidReactionRuleException, InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> b2 = pureBuilder(createSignature());
        b1.root()
                .child("User", "f")
        ;
        b2.root()
                .child("User", "f")
        ;
        PureBigraph redex = b1.create();
        PureBigraph reactum = b2.create();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("rr5");
        return rr;
    }

    public static ParametricReactionRule<PureBigraph> createRR6_noop() throws InvalidReactionRuleException, InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> b2 = pureBuilder(createSignature());
        b1.root()
                .child("Table").down().site()
                .child("Basket", "f").down().child("Fruit", "f").site()
        ;
        b2.root()
                .child("Table").down().site()
                .child("Basket", "f").down().child("Fruit", "f").site()
        ;
        PureBigraph redex = b1.create();
        PureBigraph reactum = b2.create();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("rr6");
        return rr;
    }
}
