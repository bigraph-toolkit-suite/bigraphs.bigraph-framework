package org.bigraphs.framework.simulation.examples;

import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.analysis.ReactionGraphAnalysis;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dominik Grzelak
 */
public class ConcurringProcessesExample {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/processes/";
//    private static PureBigraphFactory factory = pure();

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    //    @Disabled
    @Test
    void simulate_single_step() throws IOException, InvalidConnectionException, InvalidReactionRuleException, BigraphSimulationException, ReactiveSystemException {
//        int num = 2;
//        String fileNameMeta = "/home/dominik/git/BigraphFramework/rewriting/src/test/resources/dump/processes/meta-model_" + num + ".ecore";
//        String fileNameInstance = "/home/dominik/git/BigraphFramework/rewriting/src/test/resources/dump/processes/instance-model_" + num + ".xmi";
//        EPackage metaModel = BigraphFileModelManagement.loadBigraphMetaModel(fileNameMeta);
//        List<EObject> eObjects = BigraphFileModelManagement.loadBigraphInstanceModel(metaModel, fileNameInstance);
//        assertEquals(1, eObjects.size());
//        List<EObject> eObjects2 = BigraphFileModelManagement.loadBigraphInstanceModel(fileNameInstance);
//        assertEquals(1, eObjects2.size());

        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignature());
        builder.root()
                .child("Process", "access2")
                .child("Process", "access1").down().child("Working").up()
                .child("Resource").down().child("Token", "access1").up()
        ;
        PureBigraph agent = createAgent();
        PureBigraph agent2 = createAgentRegistered();
        PureBigraph agent3 = createAgentWorking();
        BigraphGraphvizExporter.toPNG(agent3,
                true,
                new File(TARGET_DUMP_PATH + "partial/agent")
        );

        ReactionRule<PureBigraph> rule_resourceRegistrationPhase = createRule_ResourceRegistrationPhase();
        ReactionRule<PureBigraph> rule_processWorkingPhase = createRule_ProcessWorkingPhase();
        ReactionRule<PureBigraph> rule_resourceDeregistrationPhase = createRule_ResourceDeregistrationPhase();
        BigraphGraphvizExporter.toPNG(rule_processWorkingPhase.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "partial/redex")
        );

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
//        reactiveSystem.setAgent(agent);
//        reactiveSystem.setAgent(agent2);
        reactiveSystem.setAgent(agent3);
//        reactiveSystem.addReactionRule(rule_resourceRegistrationPhase);
        reactiveSystem.addReactionRule(rule_processWorkingPhase);
//        reactiveSystem.addReactionRule(rule_resourceDeregistrationPhase);

        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                                .setMaximumTransitions(20)
                                .setMaximumTime(30)
//                        .allowReducibleClasses(true)
                                .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(TARGET_DUMP_PATH, "partial/transition_graph_agent-2"))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "partial/states/"))
                        .setPrintCanonicalStateLabel(true)
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.execute();
        assertTrue(Files.exists(Paths.get(TARGET_DUMP_PATH, "partial/transition_graph_agent-2")));
    }

    @Test
    void simulate_concurrent_processes() throws InvalidConnectionException, IOException, InvalidReactionRuleException, BigraphSimulationException, ReactiveSystemException {
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
//                        .allowReducibleClasses(false) // for simulation but not used
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
//                BigraphModelChecker.SimulationStrategy.Type.SIMULATION,
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
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignature());

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
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignature());

        builder.root()
                .child("Process", "access2")
                .child("Process", "access1")
                .child("Resource").down().child("Token", "access1")
        ;
        return builder.create();
    }

    PureBigraph createAgentWorking() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignature());
        builder.root()
                .child("Process", "access2")
                .child("Process", "access1").down().child("Working").up()
                .child("Resource").down().child("Token", "access2").up()
        ;
        return builder.create();
    }

    ReactionRule<PureBigraph> createRule_ResourceRegistrationPhase() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(createSignature());

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
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(createSignature());

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
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(createSignature());

        builderRedex.root().child("Process", "access");
        builderRedex.root().child("Resource").down().child("Token", "access");

        builderReactum.root().child("Process", "access").down().child("Working").top();
        builderReactum.root().child("Resource").down().child("Token", "access");

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private DynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add("Process", 1)
                .add("Token", 1)
                .add("Working", 1)
                .add("Resource", 1)
        ;
        return defaultBuilder.create();
    }
}
