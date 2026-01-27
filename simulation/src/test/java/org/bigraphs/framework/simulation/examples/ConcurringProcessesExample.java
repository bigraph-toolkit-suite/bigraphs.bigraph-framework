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
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.analysis.ReactionGraphAnalysis;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * The concurring processes example demonstrates how to simulate concurrent processes interacting with resources
 * using bigraphs and reaction rules.
 * It demonstrates how to set up a model checking environment, define reaction rules, and execute a model checking process
 * to create the transition system (reaction graph), showcasing the lifecycle
 * phases of resource registration, process working, and resource deregistration.
 * <p>
 * Key Methods:
 * <ul>
 * <li>{@link #setUp()}: Initializes the necessary directories and cleans up any existing data before the simulation.</li>
 * <li>{@link #sig()}: Creates a dynamic signature for the bigraph model representing the simulation entities.</li>
 * <li>{@link #simulate()}: Executes the main simulation, involving setting up the agent, defining reaction rules, running the model checker, and analyzing the resulting reaction graph for state paths (traces).</li>
 * <li>{@link #createAgent()}: Constructs the initial agent state for the simulation.</li>
 * <li>{@link #createAgentRegistered()} and {@link #createAgentWorking()}: Generate specific states of the agent under different phases of the simulation lifecycle.</li>
 * <li>{@link #createRule_ResourceRegistrationPhase()} and {@link #createRule_ResourceDeregistrationPhase()}: Define the rules for the phases involving resource management.</li>
 * </ul>
 *
 * @author Dominik Grzelak
 */
@Disabled
public class ConcurringProcessesExample {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/processes/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    private DynamicSignature sig() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add("Process", 1)
                .add("Token", 1)
                .add("Working", 1)
                .add("Resource", 1)
        ;
        return defaultBuilder.create();
    }

    @Test
    void simulate() throws InvalidConnectionException, IOException, InvalidReactionRuleException, BigraphSimulationException, ReactiveSystemException {
        PureBigraph agent = createAgent();
        ReactionRule<PureBigraph> rule_resourceRegistrationPhase = createRule_ResourceRegistrationPhase();
        ReactionRule<PureBigraph> rule_processWorkingPhase = createRule_ProcessWorkingPhase();
        ReactionRule<PureBigraph> rule_resourceDeregistrationPhase = createRule_ResourceDeregistrationPhase();

//        BigraphFileModelManagement.exportAsInstanceModel(agent, new FileOutputStream(new File(TARGET_DUMP_PATH + "agent.xmi")));
        BigraphGraphvizExporter.toPNG(agent,
                true,
                new File(TARGET_DUMP_PATH + "agent")
        );
        BigraphGraphvizExporter.toPNG(rule_resourceRegistrationPhase.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "rule_0_redex")
        );
        BigraphGraphvizExporter.toPNG(rule_resourceRegistrationPhase.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "rule_0_reactum")
        );
        BigraphGraphvizExporter.toPNG(rule_processWorkingPhase.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "rule_1_redex")
        );
        BigraphGraphvizExporter.toPNG(rule_processWorkingPhase.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "rule_1_reactum")
        );
        BigraphGraphvizExporter.toPNG(rule_resourceDeregistrationPhase.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "rule_2_redex")
        );
        BigraphGraphvizExporter.toPNG(rule_resourceDeregistrationPhase.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "rule_2_reactum")
        );


        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(rule_resourceRegistrationPhase);
        reactiveSystem.addReactionRule(rule_processWorkingPhase);
        reactiveSystem.addReactionRule(rule_resourceDeregistrationPhase);

        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .doMeasureTime(true)
                .setReactionGraphWithCycles(true)
                .and(transitionOpts()
                        .setMaximumTransitions(20)
                        .setMaximumTime(60)
                        .allowReducibleClasses(true) // use symmetries to make the transition graph smaller?
                        .create()
                )
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(TARGET_DUMP_PATH, "transition_graph.png"))
                        .setPrintCanonicalStateLabel(true)
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .setFormatsEnabled(List.of(ModelCheckingOptions.ExportOptions.Format.PNG, ModelCheckingOptions.ExportOptions.Format.XMI))
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.execute();
        assertTrue(Files.exists(Paths.get(TARGET_DUMP_PATH, "transition_graph.png")));

        ReactionGraphAnalysis<PureBigraph> analysis = ReactionGraphAnalysis.createInstance();
        List<ReactionGraphAnalysis.StateTrace<PureBigraph>> pathsToLeaves = analysis.findAllPathsInGraphToLeaves(modelChecker.getReactionGraph());
        Assertions.assertEquals(2, pathsToLeaves.size());
        pathsToLeaves.forEach(x -> {
            System.out.println("Path has length: " + x.getPath().size());
            System.out.println("\t" + x.getStateLabels().toString());
            Assertions.assertEquals(4, x.getPath().size());
        });

    }

    PureBigraph createAgent() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());

        builder.root()
                .child("Process", "access1")
                .child("Process", "access2")
                .child("Resource").down().child("Token")
        ;
        PureBigraph bigraph = builder.create();
        bigraph.getNodes().forEach(x -> {
            Map<String, Object> attributes = x.getAttributes();
            attributes.put("_id", x.getName());
            attributes.put("_eobject", x.getInstance());
            x.setAttributes(attributes);
        });
        return bigraph;
    }

    PureBigraph createAgentRegistered() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());

        builder.root()
                .child("Process", "access2")
                .child("Process", "access1")
                .child("Resource").down().child("Token", "access1")
        ;
        return builder.create();
    }

    PureBigraph createAgentWorking() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());
        builder.root()
                .child("Process", "access2")
                .child("Process", "access1").down().child("Working").up()
                .child("Resource").down().child("Token", "access2").up()
        ;
        return builder.create();
    }

    ReactionRule<PureBigraph> createRule_ResourceRegistrationPhase() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(sig());

        builderRedex.root().child("Process", "access");
        builderRedex.root().child("Resource").down().child("Token");

        builderReactum.root().child("Process", "access");
        builderReactum.root().child("Resource").down().child("Token", "access");


        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        redex.getNodes().forEach(x -> {
            Map<String, Object> attributes = x.getAttributes();
            attributes.put("_id", x.getName());
            attributes.put("_eobject", x.getInstance());
            x.setAttributes(attributes);
        });
        reactum.getNodes().forEach(x -> {
            Map<String, Object> attributes = x.getAttributes();
            attributes.put("_id", x.getName());
            attributes.put("_eobject", x.getInstance());
            x.setAttributes(attributes);
        });
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    ReactionRule<PureBigraph> createRule_ResourceDeregistrationPhase() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(sig());

        builderRedex.root().child("Process", "access").down().child("Working").top();
        builderRedex.root().child("Resource").down().child("Token", "access");

        builderReactum.root().child("Process", "access");
        builderReactum.root().child("Resource").down().child("Token");

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    ReactionRule<PureBigraph> createRule_ProcessWorkingPhase() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(sig());

        builderRedex.root().child("Process", "access");
        builderRedex.root().child("Resource").down().child("Token", "access");

        builderReactum.root().child("Process", "access").down().child("Working").top();
        builderReactum.root().child("Resource").down().child("Token", "access");

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }
}
