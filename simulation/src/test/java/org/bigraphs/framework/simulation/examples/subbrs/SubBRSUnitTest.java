package org.bigraphs.framework.simulation.examples.subbrs;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphFileModelManagement;
import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.core.EcoreBigraph;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ReactiveSystemException;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionGraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionRule;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactiveSystemPredicate;
import de.tudresden.inf.st.bigraphs.core.utils.BigraphUtil;
import org.bigraphs.framework.simulation.examples.BaseExampleTestSupport;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.apache.commons.io.FileUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

public class SubBRSUnitTest extends BaseExampleTestSupport {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/subbrs/";
    private final static boolean AUTO_CLEAN_BEFORE = true;

    public SubBRSUnitTest() {
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
    void simulate() throws Exception {
        MainBRS system = new MainBRS(createMainModel());
        system.execute();
    }

    public static class MainBRS extends PureReactiveSystem implements BigraphModelChecker.ReactiveSystemListener<PureBigraph> {

        public MainBRS(PureBigraph mainModel) throws Exception {
            setAgent(mainModel);
            eb(getAgent(), "mainModel");
            ReactionRule<PureBigraph> mainR1 = mainR1();
            ReactionRule<PureBigraph> mainR2 = mainR2();
            ReactionRule<PureBigraph> mainR3 = mainR3();
            addReactionRule(mainR1);
            addReactionRule(mainR2);
            addReactionRule(mainR3);

            eb(mainR1.getRedex(), "rrM1_lhs");
            eb(mainR1.getReactum(), "rrM1_rhs");
            eb(mainR2.getRedex(), "rrM2_lhs");
            eb(mainR2.getReactum(), "rrM2_rhs");
            eb(mainR3.getRedex(), "rrM3_lhs");
            eb(mainR3.getReactum(), "rrM3_rhs");

            SubBigraphMatchPredicate<PureBigraph> predicate = createPredicate();
            addPredicate(predicate);
            eb(predicate.getBigraph(), "predicate");

            SubBigraphMatchPredicate<PureBigraph> pred2 = createPredicateEmpty();
            addPredicate(pred2);
            eb(pred2.getBigraph(), "predicateEmpty");
        }

        @Override
        public void onPredicateMatched(PureBigraph currentAgent, ReactiveSystemPredicate<PureBigraph> predicate) {
            BigraphModelChecker.ReactiveSystemListener.super.onPredicateMatched(currentAgent, predicate);
        }

        @Override
        public void onSubPredicateMatched(PureBigraph currentAgent, ReactiveSystemPredicate<PureBigraph> predicate, PureBigraph context, PureBigraph subBigraph, PureBigraph redex, PureBigraph param) {
            if (predicate.equals(getPredicateMap().get("p0"))) {
                eb(context, "subContext");
                eb(subBigraph, "subBigraph");
                eb(redex, "subRedex");
                eb(param, "subParam");

                try {
                    SubBRS subBRS = new SubBRS(param);
                    PureBigraph result = subBRS.execute();
                    if (result != null) {
                        result = liftSigOfAgent(createSignatureMain(), result);
                        eb(result, "resultSubBRS");
                        Bigraph<DefaultDynamicSignature> newAgent = ops(context).compose(redex).compose(result).getOuterBigraph();
                        eb(newAgent, "newAgent");

                        MainBRS m2 = new MainBRS((PureBigraph) newAgent);
                        m2.execute();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void execute() throws Exception {
            PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                    this,
                    BigraphModelChecker.SimulationStrategy.Type.BFS,
                    options());
            modelChecker.setReactiveSystemListener(this);
            modelChecker.execute();

            System.out.println(modelChecker.getReactionGraph().getGraph().vertexSet().size());
        }

        void eb(Bigraph<?> bigraph, String name) {
            try {
                BigraphGraphvizExporter.toPNG(bigraph, true, new File(TARGET_DUMP_PATH + name + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static class SubBRS extends PureReactiveSystem {

        // particular sub-structure of the main-model used for computing the sum of two integer bigraphs
        public SubBRS(PureBigraph subAgent) throws Exception {

            //We re-load the subAgent instance but with a different signature for the sub-BRS
            PureBigraph bigraph = liftSigOfAgent(createSignatureSub(), subAgent);
            setAgent(bigraph);
            addReactionRule(subR1());
            addReactionRule(subR2());
            addReactionRule(subR3());

            eb(getAgent(), "subModel");
            eb(getReactionRulesMap().get("r0").getRedex(), "rrM1_lhs");
            eb(getReactionRulesMap().get("r0").getReactum(), "rrM1_rhs");
            eb(getReactionRulesMap().get("r1").getRedex(), "rrM2_lhs");
            eb(getReactionRulesMap().get("r1").getReactum(), "rrM2_rhs");
            eb(getReactionRulesMap().get("r2").getRedex(), "rrM3_lhs");
            eb(getReactionRulesMap().get("r2").getReactum(), "rrM3_rhs");
        }

        void eb(Bigraph<?> bigraph, String name) {
            try {
                BigraphGraphvizExporter.toPNG(bigraph, true, new File(TARGET_DUMP_PATH + name + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public PureBigraph execute() throws ReactiveSystemException, BigraphSimulationException {
            PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                    this,
                    BigraphModelChecker.SimulationStrategy.Type.BFS,
                    options());
//        modelChecker.setReactiveSystemListener(this);
            modelChecker.execute();

            // Get last state
            int l = modelChecker.getReactionGraph().getGraph().vertexSet().size() - 1;
            Optional<ReactionGraph.LabeledNode> first = modelChecker.getReactionGraph().getGraph().vertexSet().stream().filter(x -> x.getLabel().equals("a:" + l)).findFirst();
            if (first.isPresent()) {
                PureBigraph pureBigraph = modelChecker.getReactionGraph().getStateMap().get(first.get().getCanonicalForm());
                return pureBigraph;
            } else
                return null;
        }
    }

    // TODO bigraphUtil
    public static PureBigraph liftSigOfAgent(DefaultDynamicSignature sig, PureBigraph agent) throws Exception {
        EcoreBigraph.Stub<DefaultDynamicSignature> stub = new EcoreBigraph.Stub<>(agent);
        EPackage bMetaModel = createOrGetBigraphMetaModel(sig);

        List<EObject> eObjects = BigraphFileModelManagement.Load.bigraphInstanceModel(
                bMetaModel,
                stub.getInputStreamOfInstanceModel()
        );

        PureBigraph bigraph = PureBigraphBuilder.create(
                        sig.getInstanceModel(),
                        bMetaModel,
                        eObjects.get(0)
                )
                .createBigraph();
        return bigraph;
    }

    public static ModelCheckingOptions options() {
        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(10)
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
        return opts;
    }


    public PureBigraph createMainModel() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignatureMain());

        builder.createRoot()
                .addChild("Store")
                .down()
                .addChild("Item").down().addChild("Waiting").addSite()
//                .addChild("Item").down().addSite()
//                .addChild("Item").down().addSite()
                .top().addChild("Queue")
        ;
        PureBigraph raw = builder.createBigraph();
        Bigraph<DefaultDynamicSignature> composed = ops(raw).compose(createAddCompAgent(1, 1)).getOuterBigraph();
        return (PureBigraph) composed;
    }

    public PureBigraph createSubModel() {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignatureSub());

        return builder.createBigraph();
    }

    public static PureBigraph createAddCompAgent(final int left, final int right) throws Exception {
        DefaultDynamicSignature signature = createSignatureMain();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy leftNode =
                builder.hierarchy(signature.getControlByName("Left"))
                        .addChild("S");
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy rightNode =
                builder.hierarchy(signature.getControlByName("Right"))
                        .addChild("S");

        for (int i = 0; i < left - 1; i++) {
            leftNode = leftNode.down().addChild("S");
        }
        leftNode = leftNode.down().addChild("Z").top();
        for (int i = 0; i < right - 1; i++) {
            rightNode = rightNode.down().addChild("S");
        }
        rightNode = rightNode.down().addChild("Z").top();

        builder.createRoot()
                .addChild("Plus")
                .down()
                .addChild(leftNode)
                .addChild(rightNode)
        ;
        builder.makeGround();
        return builder.createBigraph();
    }

    public static ReactionRule<PureBigraph> mainR1() throws Exception {
        DefaultDynamicSignature signature = createSignatureMain();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("Store").down()
                .addChild("Item").down().addChild("Waiting").addSite().up()
                .addSite();

        builder2.createRoot()
                .addChild("Store")
                .down()
                .addChild("Item").down().addChild("Ready").addSite().up()
                .addSite();

        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum); //instantiationMap
        return rr;
    }

    public static SubBigraphMatchPredicate<PureBigraph> createPredicate() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignatureMain());

        builder.createRoot()
                .addChild("Item").down()
                .addChild("Ready")
                .addSite()
        ;
        PureBigraph bigraph = builder.createBigraph();
        return SubBigraphMatchPredicate.create(bigraph);
    }

    public static SubBigraphMatchPredicate<PureBigraph> createPredicateEmpty() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignatureMain());

        builder.createRoot().addChild("Item").down().addChild("Waiting").addSite();
//        builder.createRoot().addChild("Queue").down().addSite();
        PureBigraph bigraph = builder.createBigraph();
        return SubBigraphMatchPredicate.create(bigraph);
    }

    public static ReactionRule<PureBigraph> mainR2() throws Exception {
        DefaultDynamicSignature signature = createSignatureMain();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("Store")
                .down()
                .addChild("Item").down()
                .addChild("Ready").addChild("Result").down().addSite().up().up()
                .addSite();

        builder2.createRoot()
                .addChild("Store")
                .down()
                .addChild("Item").down()
                .addChild("Finished").addChild("Result").down().addSite().up().up()
                .addSite();

        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum); //instantiationMap
        return rr;
    }

    public static ReactionRule<PureBigraph> mainR3() throws Exception {
        DefaultDynamicSignature signature = createSignatureMain();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("Store")
                .down()
                .addChild("Item").down()
                .addChild("Finished").addChild("Result").down().addSite().up().up()
                .addSite().top()
        ;
        builder.createRoot().addChild("Queue").down().addSite();

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy item = builder2.hierarchy("Item").addSite();
        builder2.createRoot()
                .addChild("Store").down().addSite().top();
        builder2.createRoot().addChild("Queue").down().addChild(item).addSite();

        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum); //instantiationMap
        return rr;
    }

    /**
     * react r1 = Left.S | Right.S -> Left | Right;
     */
    public static ReactionRule<PureBigraph> subR1() throws Exception {
        DefaultDynamicSignature signature = createSignatureMain();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("Left").down().addChild("S").down().addSite()
                .top()
                .addChild("Right").down().addSite()
        ;
        builder2.createRoot()
                .addChild("Left").down().addSite()
                .top()
                .addChild("Right").down().addChild("S").down().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum); //instantiationMap
        return rr;
    }

    public static ReactionRule<PureBigraph> subR2() throws Exception {
        DefaultDynamicSignature signature = createSignatureSub();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("Left").down().addChild("Z")
                .top()
                .addChild("Right").down().addChild("S").down().addSite()
        ;
        builder2.createRoot()
                .addChild("S").down().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }

    public static ReactionRule<PureBigraph> subR3() throws Exception {
        DefaultDynamicSignature signature = createSignatureMain();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("Plus")
                .down().addChild("S").down().addSite()
                .top()
        ;
        builder2.createRoot()
                .addChild("Result")
                .down().addChild("S").down().addSite()
                .top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }

    public static DefaultDynamicSignature createSignatureMain() {
        DefaultDynamicSignature sigM = pureSignatureBuilder()
                .addControl("Item", 0)
                .addControl("Queue", 0)
                .addControl("Store", 0)
                .addControl("Waiting", 0, ControlStatus.PASSIVE)
                .addControl("Ready", 0, ControlStatus.PASSIVE)
                .addControl("Finished", 0, ControlStatus.PASSIVE)
                .create();


        DefaultDynamicSignature merged = BigraphUtil.mergeSignatures(sigM, createSignatureSub());
        return merged;
    }

    public static DefaultDynamicSignature createSignatureSub() {
        return pureSignatureBuilder()
                .newControl().identifier(StringTypedName.of("Plus")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Sum")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("S")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Z")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Result")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Left")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Right")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .create();
    }
}
