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
package org.bigraphs.framework.simulation.examples.petrinet;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.AbstractReactionRule;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.TrackingMap;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.testing.BigraphUnitTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class PetriNetExampleTest implements BigraphUnitTestSupport {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/petrinet/";

    @BeforeAll
    static void beforeAll() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    @Test
    void simpleTokenFiring_portSorting() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, ReactiveSystemException, BigraphSimulationException {
        DynamicSignature sig = sig();
        PureBigraph agent = petriNet(sig);
        ReactionRule<PureBigraph> rule1 = petriNetFireRule(sig);

        toPNG(agent, "agent", TARGET_DUMP_PATH);
        toPNG(rule1.getRedex(), "rule1_LHS", TARGET_DUMP_PATH);
        toPNG(rule1.getReactum(), "rule1_RHS", TARGET_DUMP_PATH);

        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .doMeasureTime(true)
                .setReactionGraphWithCycles(true)
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
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(rule1);
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.execute();
    }


    @Test
    void simpleTokenFiring_noPortSorting() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, ReactiveSystemException, BigraphSimulationException {
        DynamicSignature sig = sig_withOuterNames();
//        DefaultDynamicSignature sig = sig();
        PureBigraph agent = petriNet_withOuterNames(sig);
        ReactionRule<PureBigraph> rule1 = petriNetFireRule_withOuterNames(sig);

        toPNG(agent, "agent", TARGET_DUMP_PATH);
        toPNG(rule1.getRedex(), "rule1_LHS", TARGET_DUMP_PATH);
        toPNG(rule1.getReactum(), "rule1_RHS", TARGET_DUMP_PATH);

        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .doMeasureTime(true)
                .setReactionGraphWithCycles(true)
                .and(transitionOpts()
                        .setMaximumTransitions(20)
                        .setMaximumTime(60)
                        .allowReducibleClasses(true)
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
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(rule1);
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.execute();
    }

    public static DynamicSignature sig() {
        DynamicSignature signature = pureSignatureBuilder()
                .add("Place", 1)
                .add("Transition", 2)
                .add("Token", 0)
                .create();
        return signature;
    }

    public static DynamicSignature sig_withOuterNames() {
        DynamicSignature signature = pureSignatureBuilder()
                .add("Place", 1)
                .add("Transition", 1)
                .add("Token", 0)
                .create();
        return signature;
    }

    public static PureBigraph petriNet(DynamicSignature signature) throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        builder.root()
                .child("Place").linkInner("tmp").down().child("Token").child("Token").up()
                .child("Transition").linkInner("tmp").linkInner("tmp2")
                .child("Place").linkInner("tmp2")
        ;
        builder.closeInner();

        return builder.create();
    }

    public static PureBigraph petriNet_withOuterNames(DynamicSignature signature) throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

//        builder.createOuterName("idleOuter");
        builder.root()
                .child("Place").linkOuter("y").down().child("Token").child("Token").up()
                .child("Transition").linkOuter("y")
                .child("Place").linkOuter("y")
        ;

        return builder.create();
    }

    public static ReactionRule<PureBigraph> petriNetFireRule(DynamicSignature signature) throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> b2 = pureBuilder(signature);

        b1.root()
                .child("Place").linkInner("tmp").down().child("Token").site().top()
                .child("Transition").linkInner("tmp").linkInner("tmp2")
                .child("Place").linkInner("tmp2").down().site().top()
        ;
        b1.closeInner();

        b2.root()
                .child("Place").linkInner("tmp").down().site().top()
                .child("Transition").linkInner("tmp").linkInner("tmp2")
                .child("Place").linkInner("tmp2").down().site().child("Token").top()
        ;
        b2.closeInner();

        AbstractReactionRule<PureBigraph> rule = new ParametricReactionRule<>(b1.create(), b2.create()).withLabel("petriNetFireRule");

        TrackingMap trackingMap = new TrackingMap();
        trackingMap.put("v0", "v0"); // left-place of transition in redex
        trackingMap.put("v3", "v1"); // left token of left-place in redex
        trackingMap.put("v1", "v2"); // transition in redex
        trackingMap.put("v2", "v3"); // right-place of transition in redex
        trackingMap.put("e0", "e0"); // edge from left-place to transition
        trackingMap.put("e1", "e1"); // edges from transition to right-place
        trackingMap.addLinkNames("e0", "e1"); // and possible outer names
        rule.withTrackingMap(trackingMap);

        return rule;
    }

    public static ReactionRule<PureBigraph> petriNetFireRule_withOuterNames(DynamicSignature signature) throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> b2 = pureBuilder(signature);

        b1.root()
                .child("Place", "y").down().child("Token").site().top()
                .child("Transition", "y")
                .child("Place", "y").down().site().top()
        ;
        b1.closeInner();

        b2.root()
                .child("Place", "y").down().site().top()
                .child("Transition", "y")
                .child("Place", "y").down().site().child("Token").top()
        ;
        b2.closeInner();
        AbstractReactionRule<PureBigraph> rule = new ParametricReactionRule<>(b1.create(), b2.create()).withLabel("petriNetFireRule_withOuterNames");
        TrackingMap map = new TrackingMap();
        map.put("v0", "v0");
        map.put("v1", "v2");
        map.put("v2", "v3");
        map.put("v3", "v1");
        map.addLinkNames("y");
        rule.withTrackingMap(map);
        return rule;
    }

    public static ReactionRule<PureBigraph> petriNetAddRule(DynamicSignature signature) throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> b2 = pureBuilder(signature);

        b1.root()
                .child("Place", "x").down().site().top()
        ;

        b2.root()
                .child("Place", "x").down().site().child("Token").top()
        ;

        return new ParametricReactionRule<>(b1.create(), b2.create()).withLabel("petriNetAddTokenRule");
    }

}
