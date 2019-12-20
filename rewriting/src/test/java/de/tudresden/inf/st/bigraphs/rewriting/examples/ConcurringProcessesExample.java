package de.tudresden.inf.st.bigraphs.rewriting.examples;

import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystemOptions;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.impl.PureReactiveSystem;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.BigraphModelChecker;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.PureBigraphModelChecker;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.exceptions.BigraphSimulationException;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystemOptions.transitionOpts;

/**
 * @author Dominik Grzelak
 */
public class ConcurringProcessesExample {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/processes/";
    private static PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
//        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    @Test
    void simulate_single_step() throws IOException, InvalidConnectionException, InvalidReactionRuleException, BigraphSimulationException {
//        int num = 2;
//        String fileNameMeta = "/home/dominik/git/BigraphFramework/rewriting/src/test/resources/dump/processes/meta-model_" + num + ".ecore";
//        String fileNameInstance = "/home/dominik/git/BigraphFramework/rewriting/src/test/resources/dump/processes/instance-model_" + num + ".xmi";
//        EPackage metaModel = BigraphArtifacts.loadBigraphMetaModel(fileNameMeta);
//        List<EObject> eObjects = BigraphArtifacts.loadBigraphInstanceModel(metaModel, fileNameInstance);
//        assertEquals(1, eObjects.size());
//        List<EObject> eObjects2 = BigraphArtifacts.loadBigraphInstanceModel(fileNameInstance);
//        assertEquals(1, eObjects2.size());

        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());
        builder.createRoot()
                .addChild("Process", "access2")
                .addChild("Process", "access1").withNewHierarchy().addChild("Working").goBack()
                .addChild("Resource").withNewHierarchy().addChild("Token", "access1").goBack()
        ;
        PureBigraph agent = createAgent();
        PureBigraph agent2 = createAgentRegistered();
        PureBigraph agent3 = createAgentWorking();
        BigraphGraphvizExporter.toPNG(agent3,
                true,
                new File(TARGET_DUMP_PATH + "partial/agent.png")
        );

        ReactionRule<PureBigraph> rule_resourceRegistrationPhase = createRule_ResourceRegistrationPhase();
        ReactionRule<PureBigraph> rule_processWorkingPhase = createRule_ProcessWorkingPhase();
        ReactionRule<PureBigraph> rule_resourceDeregistrationPhase = createRule_ResourceDeregistrationPhase();
        BigraphGraphvizExporter.toPNG(rule_processWorkingPhase.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "partial/redex.png")
        );

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
//        reactiveSystem.setAgent(agent);
//        reactiveSystem.setAgent(agent2);
        reactiveSystem.setAgent(agent3);
//        reactiveSystem.addReactionRule(rule_resourceRegistrationPhase);
        reactiveSystem.addReactionRule(rule_processWorkingPhase);
//        reactiveSystem.addReactionRule(rule_resourceDeregistrationPhase);

        ReactiveSystemOptions opts = ReactiveSystemOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(20)
                        .setMaximumTime(30)
                        .create()
                )
                .doMeasureTime(true)
                .and(ReactiveSystemOptions.exportOpts()
                        .setTraceFile(new File(TARGET_DUMP_PATH, "partial/transition_graph_agent-2.png"))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "partial/states/"))
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationType.BREADTH_FIRST,
                opts);
        modelChecker.execute();
    }

    @Test
    void simulate_concurrent_processes() throws InvalidConnectionException, IOException, InvalidReactionRuleException, BigraphSimulationException {
        PureBigraph agent = createAgent();
        ReactionRule<PureBigraph> rule_resourceRegistrationPhase = createRule_ResourceRegistrationPhase();
        ReactionRule<PureBigraph> rule_processWorkingPhase = createRule_ProcessWorkingPhase();
        ReactionRule<PureBigraph> rule_resourceDeregistrationPhase = createRule_ResourceDeregistrationPhase();

//        BigraphArtifacts.exportAsInstanceModel(agent, new FileOutputStream(new File(TARGET_DUMP_PATH + "agent.xmi")));
        BigraphGraphvizExporter.toPNG(agent,
                true,
                new File(TARGET_DUMP_PATH + "agent.png")
        );
        BigraphGraphvizExporter.toPNG(rule_resourceRegistrationPhase.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "rule_0_redex.png")
        );
        BigraphGraphvizExporter.toPNG(rule_resourceRegistrationPhase.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "rule_0_reactum.png")
        );
        BigraphGraphvizExporter.toPNG(rule_processWorkingPhase.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "rule_1_redex.png")
        );
        BigraphGraphvizExporter.toPNG(rule_processWorkingPhase.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "rule_1_reactum.png")
        );
        BigraphGraphvizExporter.toPNG(rule_resourceDeregistrationPhase.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "rule_2_redex.png")
        );
        BigraphGraphvizExporter.toPNG(rule_resourceDeregistrationPhase.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "rule_2_reactum.png")
        );


        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(rule_resourceRegistrationPhase);
        reactiveSystem.addReactionRule(rule_processWorkingPhase);
        reactiveSystem.addReactionRule(rule_resourceDeregistrationPhase);

        ReactiveSystemOptions opts = ReactiveSystemOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(20)
                        .setMaximumTime(30)
                        .create()
                )
                .doMeasureTime(true)
                .and(ReactiveSystemOptions.exportOpts()
                        .setTraceFile(new File(TARGET_DUMP_PATH, "transition_graph.png"))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationType.BREADTH_FIRST,
                opts);
        modelChecker.execute();

    }

    PureBigraph createAgent() throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());

        builder.createRoot()
                .addChild("Process", "access1")
                .addChild("Process", "access2")
                .addChild("Resource").withNewHierarchy().addChild("Token")
        ;
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    PureBigraph createAgentRegistered() throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());

        builder.createRoot()
                .addChild("Process", "access2")
                .addChild("Process", "access1")
                .addChild("Resource").withNewHierarchy().addChild("Token", "access1")
        ;
        return builder.createBigraph();
    }

    PureBigraph createAgentWorking() throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());
        builder.createRoot()
                .addChild("Process", "access2")
                .addChild("Process", "access1").withNewHierarchy().addChild("Working").goBack()
                .addChild("Resource").withNewHierarchy().addChild("Token", "access2").goBack()
        ;
        return builder.createBigraph();
    }

    ReactionRule<PureBigraph> createRule_ResourceRegistrationPhase() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builderRedex = factory.createBigraphBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builderReactum = factory.createBigraphBuilder(createSignature());

        builderRedex.createRoot().addChild("Process", "access");
        builderRedex.createRoot().addChild("Resource").withNewHierarchy().addChild("Token");

        builderReactum.createRoot().addChild("Process", "access");
        builderReactum.createRoot().addChild("Resource").withNewHierarchy().addChild("Token", "access");


        PureBigraph redex = builderRedex.createBigraph();
        PureBigraph reactum = builderReactum.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    ReactionRule<PureBigraph> createRule_ResourceDeregistrationPhase() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builderRedex = factory.createBigraphBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builderReactum = factory.createBigraphBuilder(createSignature());

        builderRedex.createRoot().addChild("Process", "access").withNewHierarchy().addChild("Working").top();
        builderRedex.createRoot().addChild("Resource").withNewHierarchy().addChild("Token", "access");

        builderReactum.createRoot().addChild("Process", "access");
        builderReactum.createRoot().addChild("Resource").withNewHierarchy().addChild("Token");

        PureBigraph redex = builderRedex.createBigraph();
        PureBigraph reactum = builderReactum.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    ReactionRule<PureBigraph> createRule_ProcessWorkingPhase() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builderRedex = factory.createBigraphBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builderReactum = factory.createBigraphBuilder(createSignature());

        builderRedex.createRoot().addChild("Process", "access");
        builderRedex.createRoot().addChild("Resource").withNewHierarchy().addChild("Token", "access");

        builderReactum.createRoot().addChild("Process", "access").withNewHierarchy().addChild("Working").top();
        builderReactum.createRoot().addChild("Resource").withNewHierarchy().addChild("Token", "access");

        PureBigraph redex = builderRedex.createBigraph();
        PureBigraph reactum = builderReactum.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createSignature() {
        DynamicSignatureBuilder defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Process")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Token")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Working")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Resource")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("True")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("False")).arity(FiniteOrdinal.ofInteger(0)).assign()
//                .newControl().identifier(StringTypedName.of("Left")).arity(FiniteOrdinal.ofInteger(0)).assign()
//                .newControl().identifier(StringTypedName.of("Right")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;

        return (S) defaultBuilder.create();
    }
}
