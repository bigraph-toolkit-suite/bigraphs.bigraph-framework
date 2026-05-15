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
package org.bigraphs.framework.simulation.examples.robots;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.uniud.mads.jlibbig.core.std.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphEncoder;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.testing.BigraphUnitTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
@Disabled
public class RobotZoneMovementExample implements BigraphModelChecker.ReactiveSystemListener<PureBigraph>, BigraphUnitTestSupport {
    private final static String DUMP_PATH = "src/test/resources/dump/robots/zone-mvmt/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(DUMP_PATH));
        new File(DUMP_PATH + "states/").mkdir();
    }

    private DynamicSignature sig() {
        return pureSignatureBuilder()
                .add("Robot", 1)
                .add("Gripper", 1)
                .add("Object", 1)
                .add("Ownership", 1)
                .add("Zone1", 0)
                .add("Zone2", 0)
                .add("Zone3", 0)
                .create();
    }

    @Test
    void simulate() throws InvalidConnectionException, InvalidReactionRuleException, BigraphSimulationException, ReactiveSystemException {
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(8)
                        .setMaximumTime(30)
                        .allowReducibleClasses(true)
                        .create()
                )
                .doMeasureTime(false)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(DUMP_PATH + "transition_system.png"))
                        .setOutputStatesFolder(new File(DUMP_PATH + "states/"))
                        .create()
                )
        ;
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();

        PureBigraph agent_a = agent();
        ReactionRule<PureBigraph> reactionRule_1 = createReactionRule_1();
        ReactionRule<PureBigraph> reactionRule_2 = createReactionRule_2();
        toPNG(agent_a, "agent", DUMP_PATH);
        toPNG(reactionRule_1.getRedex(), "redex1", DUMP_PATH);
        toPNG(reactionRule_1.getReactum(), "reactum1", DUMP_PATH);

        reactiveSystem.addReactionRule(reactionRule_1);
        reactiveSystem.addReactionRule(reactionRule_2);
        reactiveSystem.setAgent(agent_a);

        PureBigraphModelChecker modelChecker = (PureBigraphModelChecker) new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts)
                .setReactiveSystemListener(this);
        modelChecker.execute();
        assertTrue(Files.exists(Paths.get(DUMP_PATH, "transition_system.png")));
    }

    @Test
    void jlibbig_rewriting_test() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraph agent_a = agent();
        ReactionRule<PureBigraph> reactionRule_1 = createReactionRule_1();
        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
        Bigraph jAgent = encoder.encode(agent_a);

        System.out.println(jAgent);

        Signature jSig = jAgent.getSignature();
        Bigraph jRedex = encoder.encode(reactionRule_1.getRedex(), jSig);
        Bigraph jReactum = encoder.encode(reactionRule_1.getReactum(), jSig);

        AgentRewritingRule jArule = new AgentRewritingRule(jRedex, jReactum, 0);
        Iterable<Bigraph> apply = jArule.apply(jAgent);
        System.out.println(apply);
        System.out.println(apply.iterator());
        System.out.println(apply.iterator().hasNext());

    }

    @Override
    public void onUpdateReactionRuleApplies(PureBigraph agent, ReactionRule<PureBigraph> reactionRule, BigraphMatch<PureBigraph> matchResult) {
        try {
            System.out.println("RR: " + reactionRule.getLabel());
            int cnt = 0;
            for (PureBigraph each : matchResult.getParameters()) {
                toPNG(each, "d" + cnt, DUMP_PATH);
                cnt++;
            }
            toPNG(matchResult.getContext(), "context", DUMP_PATH);
            toPNG(matchResult.getRedexImage(), "redexImage", DUMP_PATH);
        } catch (Exception ignored) {
        }
    }

    private PureBigraph agent() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature>.Hierarchy zone1 = b.hierarchy("Zone1");
        PureBigraphBuilder<DynamicSignature>.Hierarchy zone2 = b.hierarchy("Zone2");
        zone1.child("Robot", "rId").down().child("Gripper", "canGrip");
        zone2.child("Zone3").child("Object", "isFree").down().child("Ownership", "belongsTo");
        b.root()
                .child(zone1).top().child(zone2).top();
        return b.create();
    }

    private ReactionRule<PureBigraph> createReactionRule_1() throws InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> b2 = pureBuilder(sig());

        b1.root().child("Zone1").down().child("Robot", "rId").down().site();
        b1.root().child("Zone2").down().child("Zone3").child("Object", "isFree").down().child("Ownership", "belongsTo");

        b2.root().child("Zone1");
        b2.root().child("Zone2").down().child("Zone3").down().child("Robot", "rId").down().site().up().up()
                .child("Object", "isFree").down().child("Ownership", "belongsTo");

        PureBigraph redex = b1.create();
        PureBigraph reactum = b2.create();
        return new ParametricReactionRule<>(redex, reactum).withLabel("move_1");
    }

    private ReactionRule<PureBigraph> createReactionRule_2() throws InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> b2 = pureBuilder(sig());

        b1.root().child("Zone1");
        b1.root().child("Zone2").down().child("Zone3").down().child("Robot", "rId").down().site().up().up()
                .child("Object", "isFree").down().child("Ownership", "belongsTo");

        b2.root().child("Zone1").down().child("Robot", "rId").down().site();
        b2.root().child("Zone2").down().child("Zone3").child("Object", "isFree").down().child("Ownership", "belongsTo");


        PureBigraph redex = b1.create();
        PureBigraph reactum = b2.create();
        return new ParametricReactionRule<>(redex, reactum).withLabel("move_2");
    }
}
