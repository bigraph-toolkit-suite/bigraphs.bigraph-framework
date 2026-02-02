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
package org.bigraphs.framework.simulation.examples;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
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
public class ContextAwarePrinting extends BaseExampleTestSupport {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/cas-printing/";
    private final static boolean AUTO_CLEAN_BEFORE = true;

    public ContextAwarePrinting() {
        super(TARGET_DUMP_PATH, AUTO_CLEAN_BEFORE);
    }

    @BeforeAll
    static void setUp() throws IOException {
        if (AUTO_CLEAN_BEFORE) {
            File dump = new File(TARGET_DUMP_PATH);
            dump.mkdirs();
            FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
            new File(TARGET_DUMP_PATH + "states/").mkdir();
        }
    }

    @Test
    void name() throws Exception {
        DynamicSignatureBuilder assign = pureSignatureBuilder()
                .newControl("loc", 1).assign()
                .newControl("in", 0).assign() // input node
                .newControl("out", 0).assign() // output node
                .newControl("f", 0).assign() // controls find-all query
                .newControl("f'", 1).assign() // answer node
                .newControl("g", 0).assign() // dummy
                .newControl("s", 0).assign() // collect searched nodes
//                .newControl("prt", 1).assign()
//                .newControl("pcl", 0).assign()
//                .newControl("raw", 0).assign()
//                .newControl("dat", 1).assign()
                .newControl("dev", 1).assign();

        DynamicSignature dynamicSignature = assign.create();

        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(dynamicSignature);

        //loc(loc(loc(loc(dev1) | loc(dev2 | dev3))) | loc() | loc(dev 4))
        BigraphEntity.OuterName z = builder.createOuter("z");
        PureBigraphBuilder<DynamicSignature>.Hierarchy first = builder.hierarchy("loc").linkOuter(z)
                .child("loc", "a")
                .down()
                .child("loc", "b").down().child("dev", "1").up()
                .child("loc", "c").down().child("dev", "2").child("dev", "3").up();

        builder.root()
                .child("in").down().child("f").up()
                .child("out")
                .child("loc", "top")
                .down()
                .child(first.top())
                .child("loc", "d")
                .child("loc", "e").down().child("dev", "4").up();
        PureBigraph agent = builder.create();


        ParametricReactionRule<PureBigraph> rr0 = createReactionRule0(dynamicSignature);
        ParametricReactionRule<PureBigraph> rr1 = initialization_Rule(dynamicSignature);
        ParametricReactionRule<PureBigraph> rrCollectDev = collectDevices_Rule(dynamicSignature);
//        BigraphFileModelManagement.exportAsInstanceModel(bigraph, System.out);

        toPNG(agent, "plato_tree", TARGET_DUMP_PATH);
        toPNG(rr0.getRedex(), "rr0-redex", TARGET_DUMP_PATH);
        toPNG(rr0.getReactum(), "rr0-reactum", TARGET_DUMP_PATH);
        toPNG(rr1.getRedex(), "rr1-redex", TARGET_DUMP_PATH);
        toPNG(rr1.getReactum(), "rr1-reactum", TARGET_DUMP_PATH);
        toPNG(rrCollectDev.getReactum(), "rr2-redex", TARGET_DUMP_PATH);
        toPNG(rrCollectDev.getReactum(), "rr2-reactum", TARGET_DUMP_PATH);

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
//        reactiveSystem.addReactionRule(rr0);
        reactiveSystem.addReactionRule(rr1);
//        reactiveSystem.addReactionRule(rrCollectDev);
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(100)
                        .setMaximumTime(30)
                        .allowReducibleClasses(false)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(TARGET_DUMP_PATH + "transition_graph_agent.png"))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .setPrintCanonicalStateLabel(true)
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.execute();

    }

    private static ParametricReactionRule<PureBigraph> createReactionRule0(DynamicSignature signature) throws InvalidConnectionException, IOException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(signature);

        builderRedex.root()
                .child("loc", "top").down().site()
//                .site()
        ;
        PureBigraph redex = builderRedex.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex, System.out);

        builderReactum.root()
//                .site()
                .child("loc", "top").down().site().top()
                .child("in").down().child("f").top()
                .child("out");
        PureBigraph reactum = builderReactum.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum, System.out);
        ParametricReactionRule<PureBigraph> pureBigraphParametricReactionRule = new ParametricReactionRule<>(redex, reactum);
        return pureBigraphParametricReactionRule;
    }


    private static ParametricReactionRule<PureBigraph> initialization_Rule(DynamicSignature signature) throws InvalidConnectionException, IOException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(signature);

        builderRedex.root()
                .child("in").down().child("f").top()
                .child("loc", "top").down().site().child("loc", "x").down().site().top()
                .child("out");
        PureBigraph redex = builderRedex.create();

        builderReactum.root()
                .child("in").down().child("g").top()
                .child("loc", "top").down().site().child("loc", "x").down().child("f").child("s").site().top()
                .child("out");
        PureBigraph reactum = builderReactum.create();

        ParametricReactionRule<PureBigraph> pureBigraphParametricReactionRule = new ParametricReactionRule<>(redex, reactum);
        return pureBigraphParametricReactionRule;
    }

    /**
     * Collects devices under a location, and adds them under 's', further adding a "reference" f' under 'out'
     */
    private static ParametricReactionRule<PureBigraph> collectDevices_Rule(DynamicSignature signature) throws InvalidConnectionException, IOException, InvalidReactionRuleException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(signature);

        builderRedex.root()
                .child("loc", "x").down()
                .child("f").site().child("dev", "y").child("s").down().site().top()
                .child("out").down().site().top();
        PureBigraph redex = builderRedex.create();


        BigraphEntity.OuterName y1 = builderReactum.createOuter("y1");
        builderReactum.root()
                .child("loc", "x").down()
                .child("f").site().child("s").down().site().child("dev", y1).top()
                .child("out").down().site().child("f'", "y").top();

        builderReactum.closeOuter(y1);
        PureBigraph reactum = builderReactum.create();

        ParametricReactionRule<PureBigraph> pureBigraphParametricReactionRule = new ParametricReactionRule<>(redex, reactum);
        return pureBigraphParametricReactionRule;
    }
}
