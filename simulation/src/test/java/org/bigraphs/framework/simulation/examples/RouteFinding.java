package org.bigraphs.framework.simulation.examples;

import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphEncoder;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.core.reactivesystem.InstantiationMap;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dominik Grzelak
 */
public class RouteFinding implements BigraphModelChecker.ReactiveSystemListener<PureBigraph> {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/cars/framework/";
    boolean carArrivedAtTarget = false;
    long startTime = System.currentTimeMillis();
    long finishTime = System.currentTimeMillis();

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    @Override
    public void onAllPredicateMatched(PureBigraph currentAgent, String label) {
        System.out.println("Car arrived at the target");
        System.out.println(label);
        finishTime = System.currentTimeMillis();
        System.out.println("Time elapsed: " + (finishTime - startTime) + " ms");
        carArrivedAtTarget = true;
    }

    @Test
    void simulate_car_example() throws InvalidConnectionException, TypeNotExistsException, IOException, InvalidReactionRuleException, BigraphSimulationException, ReactiveSystemException {
        SubBigraphMatchPredicate<PureBigraph> predicate = createPredicate();
        BigraphGraphvizExporter.toPNG(predicate.getBigraphToMatch(),
                true,
                new File(TARGET_DUMP_PATH + "predicate_car.png")
        );

//        PureBigraph map = createMapSimple(8);
        PureBigraph map = createMap(8);
//        BigraphFileModelManagement.exportAsMetaModel(map, new FileOutputStream("meta-model.ecore"));
//        PureBigraphBuilder.create(createSignature(),
//                "/home/dominik/git/BigraphFramework/rewriting/meta-model.ecore",
//                "/home/dominik/git/BigraphFramework/rewriting/instance-model_3.xmi");
        BigraphGraphvizExporter.toPNG(map,
                true,
                new File(TARGET_DUMP_PATH + "map_car.png")
        );

        ReactionRule<PureBigraph> reactionRule = createReactionRule();
        BigraphGraphvizExporter.toPNG(reactionRule.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "redex_car.png")
        );
        BigraphGraphvizExporter.toPNG(reactionRule.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "reactum_car.png")
        );
        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
        System.out.println(encoder.encode(reactionRule.getRedex()));
        System.out.println(encoder.encode(reactionRule.getReactum()));
//        BigraphFileModelManagement.exportAsMetaModel(map, new FileOutputStream(TARGET_DUMP_PATH + "meta-model.ecore"));
//        Path currentRelativePath = Paths.get("");
        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .doMeasureTime(true)
                .setReactionGraphWithCycles(false)
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
        reactiveSystem.addReactionRule(reactionRule);
        reactiveSystem.setAgent(map);
        reactiveSystem.addPredicate(predicate);
//        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem, BigraphModelChecker.SimulationType.RANDOM_STATE,
//                opts);
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.setReactiveSystemListener(this);
        modelChecker.execute();
        assertTrue(Files.exists(completePath));
        assertTrue(carArrivedAtTarget);

//        List<BMatchResult<PureBigraph>> rr11 = modelChecker.getReactionGraph().getTransitionMap().get("r0");
//        PureBigraphParametricMatch pureMatch = (PureBigraphParametricMatch) rr11.get(0).getMatch();
//
//        if(pureMatch.wasRewritten()) {
//            PureBigraph param = pureMatch.getParam();
//            PureBigraph ctx = pureMatch.getContext();
//            PureBigraph rdxImg = pureMatch.getRedexImage();
//            BigraphGraphvizExporter.toPNG(param, true, new File("paramRewritten.png"));
//            BigraphGraphvizExporter.toPNG(ctx, true, new File("ctxRewritten.png"));
//            BigraphGraphvizExporter.toPNG(rdxImg, true, new File("rdxImgRewritten.png"));
//        }
//
//        it.uniud.mads.jlibbig.core.std.Bigraph param = (pureMatch).getJLibMatchResult().getParam().clone();
//        System.out.println(param);
//        BigraphGraphvizExporter.toPNG(new JLibBigBigraphDecoder().decode(param), true, new File("decoded.png"));
//        Bigraph context = (pureMatch).getJLibMatchResult().getContext();
//        BigraphGraphvizExporter.toPNG(new JLibBigBigraphDecoder().decode(context), true, new File("context-decoded.png"));
//        Bigraph redexImage = (pureMatch).getJLibMatchResult().getRedexImage();
//        BigraphGraphvizExporter.toPNG(new JLibBigBigraphDecoder().decode(redexImage), true, new File("redexImage-decoded.png"));
    }

    private PureBigraph createMap(int fuelLevel) throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignature());

        BigraphEntity.OuterName p0 = builder.createOuter("p0");
        BigraphEntity.OuterName p1 = builder.createOuter("p1");
        BigraphEntity.OuterName p2 = builder.createOuter("p2");
        BigraphEntity.OuterName p3 = builder.createOuter("p3");
        BigraphEntity.OuterName p4 = builder.createOuter("p4");
        BigraphEntity.OuterName p5 = builder.createOuter("p5");
//        BigraphEntity.OuterName p6 = builder.createOuterName("p6");
        BigraphEntity.OuterName p7 = builder.createOuter("p7");
//        BigraphEntity.OuterName p8 = builder.createOuterName("p8");
        BigraphEntity.OuterName target = builder.createOuter("target");
//        BigraphEntity.InnerName target = builder.createInnerName("target");

        PureBigraphBuilder<DynamicSignature>.Hierarchy car = builder.hierarchy("Car").linkOuter(target);
        for (int i = 0; i < fuelLevel; i++) {
            car = car.child("Fuel");
        }
        builder.root()
                .child("Place").linkOuter(p0).down().child(car).child("Road").linkOuter(p1).child("Road").linkOuter(p3).up()
                .child("Place").linkOuter(p1).down().child("Road").linkOuter(p2).child("Road").linkOuter(p4).up()
                .child("Place").linkOuter(p2).down().child("Road").linkOuter(p5).up()
                .child("Place").linkOuter(p3).down().child("Road").linkOuter(p4).child("Road").linkOuter(p7).up()
                .child("Place").linkOuter(p4).down().child("Road").linkOuter(p5).child("Road").linkOuter(p1).up()
//                .addChild("Place").connectNodeToOuterName(p5).withNewHierarchy().addChild("Road").connectNodeToOuterName(p6).addChild("Road").connectNodeToOuterName(p7).addChild("Road").connectNodeToOuterName(p8).goBack()
//                .addChild("Place").connectNodeToOuterName(p6).withNewHierarchy().addChild("Road").connectNodeToOuterName(p8).addChild("Road").connectNodeToOuterName(p5).goBack()
                .child("Place").linkOuter(p7).down().child("Road").linkOuter(p2).child("Target").linkOuter(target).up()
//                .addChild("Place")
        ;
//        builder.closeInnerNames(p1, p2, p3, p4, p5, p6, p7, p8, target);
//        builder.closeInnerNames(target);
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    private PureBigraph createMapSimple(int fuelLevel) throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignature());

        BigraphEntity.OuterName p0 = builder.createOuter("p0");
        BigraphEntity.OuterName p1 = builder.createOuter("p1");
//        BigraphEntity.OuterName p2 = builder.createOuterName("p2");
        BigraphEntity.OuterName p3 = builder.createOuter("p3");
        BigraphEntity.OuterName p4 = builder.createOuter("p4");
//        BigraphEntity.OuterName p5 = builder.createOuterName("p5");
//        BigraphEntity.OuterName p6 = builder.createOuterName("p6");
//        BigraphEntity.OuterName p7 = builder.createOuterName("p7");
//        BigraphEntity.OuterName p8 = builder.createOuterName("p8");
        BigraphEntity.OuterName target = builder.createOuter("target");
//        BigraphEntity.InnerName target = builder.createInnerName("target");

        PureBigraphBuilder<DynamicSignature>.Hierarchy car = builder.hierarchy("Car").linkOuter(target);
        for (int i = 0; i < fuelLevel; i++) {
            car = car.child("Fuel");
        }
        builder.root()
                .child("Place").linkOuter(p0).down().child(car).child("Road").linkOuter(p1).child("Road").linkOuter(p3).up()
                .child("Place").linkOuter(p1).down().child("Road").child("Road").linkOuter(p4).up()
//                .addChild("Place").linkToOuter(p2).withNewHierarchy().addChild("Road").linkToOuter(p5).goBack()
                .child("Place").linkOuter(p3).down().child("Road").linkOuter(p4).child("Road").up()
//                .addChild("Place").linkToOuter(p4).withNewHierarchy().addChild("Road").linkToOuter(p5).addChild("Road").linkToOuter(p1).goBack()
//                .addChild("Place").connectNodeToOuterName(p5).withNewHierarchy().addChild("Road").connectNodeToOuterName(p6).addChild("Road").connectNodeToOuterName(p7).addChild("Road").connectNodeToOuterName(p8).goBack()
//                .addChild("Place").connectNodeToOuterName(p6).withNewHierarchy().addChild("Road").connectNodeToOuterName(p8).addChild("Road").connectNodeToOuterName(p5).goBack()
//                .addChild("Place").linkToOuter(p7).withNewHierarchy().addChild("Road").linkToOuter(p2).addChild("Target").linkToOuter(target).goBack()
//                .addChild("Place")
        ;
//        builder.closeInnerNames(p1, p2, p3, p4, p5, p6, p7, p8, target);
//        builder.closeInnerNames(target);
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    /**
     * react r1 = Left.S | Right.S -> Left | Right;
     */
    public static ReactionRule<PureBigraph> createReactionRule() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(createSignature());

        BigraphEntity.OuterName fromD = builder.createOuter("fromD");
        BigraphEntity.OuterName fromS = builder.createOuter("fromS");
        BigraphEntity.OuterName target = builder.createOuter("target");

        Supplier<PureBigraphBuilder.Hierarchy> car = () -> {
            PureBigraphBuilder<DynamicSignature>.Hierarchy car1 = null;
            try {
                car1 = builder.hierarchy("Car").linkOuter(target).site().child("Fuel");
            } catch (TypeNotExistsException e) {
                e.printStackTrace();
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
            return car1;
        };

        builder.root()
                .child("Place").linkOuter(fromD).down().site().top()
                .child("Place").linkOuter(fromS).down().child(car.get()).site().child("Road").linkOuter(fromD).top()
        ;

        BigraphEntity.OuterName fromD2 = builder2.createOuter("fromD");
        BigraphEntity.OuterName fromS2 = builder2.createOuter("fromS");
        BigraphEntity.OuterName target2 = builder2.createOuter("target");
        Supplier<PureBigraphBuilder.Hierarchy> car2 = () -> {
            PureBigraphBuilder<DynamicSignature>.Hierarchy car1 = null;
            try {
                car1 = builder2.hierarchy("Car").linkOuter(target2).site();
            } catch (TypeNotExistsException e) {
                e.printStackTrace();
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
            return car1;
        };

        builder2.root()
                .child("Place").linkOuter(fromD2).down().site().child(car2.get()).top()
                .child("Place").linkOuter(fromS2).down().child("Road").linkOuter(fromD2).site().top()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        InstantiationMap instantiationMap = InstantiationMap.create(3);
//        instantiationMap.
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private SubBigraphMatchPredicate<PureBigraph> createPredicate() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignature());

        BigraphEntity.OuterName from = builder.createOuter("from");

        // links of car and target must be connected via an outer name otherwise the predicate is not matched
        builder.root()
                .child("Place").linkOuter(from)
//                .down().addSite().connectByEdge("Target", "Car").down().addSite();
                .down().site().child("Target", "target").child("Car", "target").down().site();
        PureBigraph bigraph = builder.create();
        return SubBigraphMatchPredicate.create(bigraph);
    }

    public static DynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Car")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Fuel")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Place")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Road")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Target")).arity(FiniteOrdinal.ofInteger(1)).assign()
        ;
        return defaultBuilder.create();
    }
}
