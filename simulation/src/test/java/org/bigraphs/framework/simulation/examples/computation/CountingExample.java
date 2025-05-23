package org.bigraphs.framework.simulation.examples.computation;

import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.*;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.analysis.ReactionGraphAnalysis;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.apache.commons.io.FileUtils;
import org.jgrapht.Graph;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dominik Grzelak
 */
public class CountingExample {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/counting/";

    @BeforeAll
    static void setUp() throws IOException {
//        System.setProperty("it.uniud.mads.jlibbig.debug", "false");
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    @Test
    void simulate() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, InvalidReactionRuleException, BigraphSimulationException, ReactiveSystemException {
        // Create reaction rulesname
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();

        PureBigraph agent_a = createAgent_A(3, 4);
        BigraphGraphvizExporter.toPNG(agent_a,
                true,
                new File(TARGET_DUMP_PATH + "counting_agent.png")
        );

//        PureBigraph agent = (PureBigraph) createAgent_A();
        ReactionRule<PureBigraph> rr_1 = createReactionRule_1();
        ReactionRule<PureBigraph> rr_2 = createReactionRule_2();
        ReactionRule<PureBigraph> rr_3 = createReactionRule_3();
        BigraphGraphvizExporter.toPNG(rr_1.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "rr_lhs_1.png")
        );
        BigraphGraphvizExporter.toPNG(rr_1.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "rr_rhs_1.png")
        );
        BigraphGraphvizExporter.toPNG(rr_2.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "rr_lhs_2.png")
        );
        BigraphGraphvizExporter.toPNG(rr_3.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "rr_lhs_3.png")
        );

//        ModelCheckingOptions.ExportOptions.Builder builder = ModelCheckingOptions.exportOpts();
//        ModelCheckingOptions.TransitionOptions.Builder builder1 = transitionOpts();
        reactiveSystem.addReactionRule(rr_1);
        reactiveSystem.addReactionRule(rr_2);
        reactiveSystem.addReactionRule(rr_3);
        assertTrue(reactiveSystem.isSimple());
        Path currentRelativePath = Paths.get("");
        Path completePath = Paths.get(currentRelativePath.toAbsolutePath().toString(), TARGET_DUMP_PATH, "transition_graph_2.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                                .setMaximumTransitions(8)
                                .setMaximumTime(30)
//                        .allowReducibleClasses(true)
                                .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                                .setReactionGraphFile(new File(completePath.toUri()))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
//                        .setPrintCanonicalStateLabel(true)
                                .setPrintCanonicalStateLabel(false)
                        .setFormatsEnabled(List.of(ModelCheckingOptions.ExportOptions.Format.PNG))
                                .create()
                )
        ;
        reactiveSystem.setAgent(agent_a);

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.execute();

        ReactionGraphAnalysis<PureBigraph> analysis = ReactionGraphAnalysis.createInstance();
        List<ReactionGraphAnalysis.StateTrace<PureBigraph>> pathsToLeaves = analysis.findAllPathsInGraphToLeaves(modelChecker.getReactionGraph());
        pathsToLeaves.forEach(x -> {
            System.out.println("Path has length: " + x.getPath().size());
        });

        Graph<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> graph = modelChecker.getReactionGraph().getGraph();
        assertEquals(5, graph.vertexSet().size());
        assertEquals(4, graph.edgeSet().size());
        System.out.println(new ArrayList<>(graph.vertexSet()).get(0).getCanonicalForm());
        System.out.println(new ArrayList<>(graph.vertexSet()).get(graph.vertexSet().size() - 1).getCanonicalForm());
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) new ArrayList<>(graph.vertexSet()).get(graph.vertexSet().size() - 1), System.out);
//        boolean c1 = "r0$Age$True$Z#".equals(new ArrayList<>(graph.vertexSet()).get(0).getCanonicalForm()) ||
//                "r0$Age$True$Z#".equals(new ArrayList<>(graph.vertexSet()).get(graph.vertexSet().size() - 1)
//                        .getCanonicalForm());
        String s1 = new ArrayList<>(graph.vertexSet()).get(0).getCanonicalForm();
        String s2 = new ArrayList<>(graph.vertexSet()).get(graph.vertexSet().size() - 1).getCanonicalForm();
        boolean c1 = (s1.startsWith("r0$Age$True") && s1.endsWith("$Z#")) ||
                s2.startsWith("r0$Age$True") && s2.endsWith("$Z#");
        assertTrue(c1);
    }

    /**
     * big numberLeft = Left.S.S.S.S.S.S.Z;
     * big numberRight = Right.S.S.S.S.Z;
     * big start = Age . (numberLeft | numberRight);
     */
    public static PureBigraph createAgent_A(final int left, final int right) throws ControlIsAtomicException, InvalidArityOfControlException, LinkTypeNotExistsException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy leftNode =
                builder.hierarchy(signature.getControlByName("Left"))
                        .addChild("S");
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy rightNode =
                builder.hierarchy(signature.getControlByName("Right"))
                        .addChild("S");
//        IntStream.range(0, left).forEach(x -> {
//            leftNode.withNewHierarchy().addChild("S");
//        });
//        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy s = IntStream.range(0, left)
//                .boxed()
//                .map(x -> "S")
//                .reduce((x) -> {
//                    return builder.newHierarchy(signature.getControlByName(x)).addChild(x);
//                }).get();
//        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy s = builder.newHierarchy(signature.getControlByName("S"));
//        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy s1 = builder.newHierarchy(signature.getControlByName("S"));
//        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy s2 = builder.newHierarchy(signature.getControlByName("S"));
//        s = s.addChild(s1);
//        s = s.withNewHierarchy().addChild(s2);
//        s = s.addChild(s2);
        for (int i = 0; i < left - 1; i++) {
            leftNode = leftNode.down().addChild("S");
        }
        leftNode = leftNode.down().addChild("Z").top();
        for (int i = 0; i < right - 1; i++) {
            rightNode = rightNode.down().addChild("S");
        }
        rightNode = rightNode.down().addChild("Z").top();

        builder.createRoot()
                .addChild("Age")
                .down()
                .addChild(leftNode)
                .addChild(rightNode)
//                .addChild(s.top())
        ;
        builder.makeGround();
        return builder.createBigraph();
    }

    /**
     * react r1 = Left.S | Right.S -> Left | Right;
     */
    public static ReactionRule<PureBigraph> createReactionRule_1() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("Left").down().addChild("S").down().addSite()
                .top()
                .addChild("Right").down().addChild("S").down().addSite()
        ;
        builder2.createRoot()
                .addChild("Left").down().addSite()
                .top()
                .addChild("Right").down().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    /**
     * react r2 = Left.Z | Right.S -> True;
     */
    public static ReactionRule<PureBigraph> createReactionRule_2() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("Left").down().addChild("Z")
                .top()
                .addChild("Right").down().addChild("S").down().addSite()
        ;
        builder2.createRoot()
                .addChild("True").down().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    /**
     * react r3 = Left | Right.Z -> False;
     */
    public static ReactionRule<PureBigraph> createReactionRule_3() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("Left").down().addSite()
                .top()
                .addChild("Right").down().addChild("Z")
        ;
        builder2.createRoot()
                .addChild("False").down().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private static <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Age")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("S")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Z")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("True")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("False")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Left")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Right")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;

        return (S) defaultBuilder.create();
    }
}
