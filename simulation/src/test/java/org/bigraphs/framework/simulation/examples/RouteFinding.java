package org.bigraphs.framework.simulation.examples;

import de.tudresden.inf.st.bigraphs.converter.jlibbig.JLibBigBigraphEncoder;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.ReactiveSystemException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.InstantiationMap;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dominik Grzelak
 */
public class RouteFinding implements BigraphModelChecker.ReactiveSystemListener<PureBigraph> {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/cars/framework/";
    boolean carArrivedAtTarget = false;

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
                .and(transitionOpts()
                        .setMaximumTransitions(150)
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
    }

    private PureBigraph createMap(int fuelLevel) throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());

        BigraphEntity.OuterName p0 = builder.createOuterName("p0");
        BigraphEntity.OuterName p1 = builder.createOuterName("p1");
        BigraphEntity.OuterName p2 = builder.createOuterName("p2");
        BigraphEntity.OuterName p3 = builder.createOuterName("p3");
        BigraphEntity.OuterName p4 = builder.createOuterName("p4");
        BigraphEntity.OuterName p5 = builder.createOuterName("p5");
//        BigraphEntity.OuterName p6 = builder.createOuterName("p6");
        BigraphEntity.OuterName p7 = builder.createOuterName("p7");
//        BigraphEntity.OuterName p8 = builder.createOuterName("p8");
        BigraphEntity.OuterName target = builder.createOuterName("target");
//        BigraphEntity.InnerName target = builder.createInnerName("target");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy car = builder.hierarchy("Car").linkToOuter(target);
        for (int i = 0; i < fuelLevel; i++) {
            car = car.addChild("Fuel");
        }
        builder.createRoot()
                .addChild("Place").linkToOuter(p0).down().addChild(car).addChild("Road").linkToOuter(p1).addChild("Road").linkToOuter(p3).up()
                .addChild("Place").linkToOuter(p1).down().addChild("Road").linkToOuter(p2).addChild("Road").linkToOuter(p4).up()
                .addChild("Place").linkToOuter(p2).down().addChild("Road").linkToOuter(p5).up()
                .addChild("Place").linkToOuter(p3).down().addChild("Road").linkToOuter(p4).addChild("Road").linkToOuter(p7).up()
                .addChild("Place").linkToOuter(p4).down().addChild("Road").linkToOuter(p5).addChild("Road").linkToOuter(p1).up()
//                .addChild("Place").connectNodeToOuterName(p5).withNewHierarchy().addChild("Road").connectNodeToOuterName(p6).addChild("Road").connectNodeToOuterName(p7).addChild("Road").connectNodeToOuterName(p8).goBack()
//                .addChild("Place").connectNodeToOuterName(p6).withNewHierarchy().addChild("Road").connectNodeToOuterName(p8).addChild("Road").connectNodeToOuterName(p5).goBack()
                .addChild("Place").linkToOuter(p7).down().addChild("Road").linkToOuter(p2).addChild("Target").linkToOuter(target).up()
//                .addChild("Place")
        ;
//        builder.closeInnerNames(p1, p2, p3, p4, p5, p6, p7, p8, target);
//        builder.closeInnerNames(target);
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    private PureBigraph createMapSimple(int fuelLevel) throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());

        BigraphEntity.OuterName p0 = builder.createOuterName("p0");
        BigraphEntity.OuterName p1 = builder.createOuterName("p1");
//        BigraphEntity.OuterName p2 = builder.createOuterName("p2");
        BigraphEntity.OuterName p3 = builder.createOuterName("p3");
        BigraphEntity.OuterName p4 = builder.createOuterName("p4");
//        BigraphEntity.OuterName p5 = builder.createOuterName("p5");
//        BigraphEntity.OuterName p6 = builder.createOuterName("p6");
//        BigraphEntity.OuterName p7 = builder.createOuterName("p7");
//        BigraphEntity.OuterName p8 = builder.createOuterName("p8");
        BigraphEntity.OuterName target = builder.createOuterName("target");
//        BigraphEntity.InnerName target = builder.createInnerName("target");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy car = builder.hierarchy("Car").linkToOuter(target);
        for (int i = 0; i < fuelLevel; i++) {
            car = car.addChild("Fuel");
        }
        builder.createRoot()
                .addChild("Place").linkToOuter(p0).down().addChild(car).addChild("Road").linkToOuter(p1).addChild("Road").linkToOuter(p3).up()
                .addChild("Place").linkToOuter(p1).down().addChild("Road").addChild("Road").linkToOuter(p4).up()
//                .addChild("Place").linkToOuter(p2).withNewHierarchy().addChild("Road").linkToOuter(p5).goBack()
                .addChild("Place").linkToOuter(p3).down().addChild("Road").linkToOuter(p4).addChild("Road").up()
//                .addChild("Place").linkToOuter(p4).withNewHierarchy().addChild("Road").linkToOuter(p5).addChild("Road").linkToOuter(p1).goBack()
//                .addChild("Place").connectNodeToOuterName(p5).withNewHierarchy().addChild("Road").connectNodeToOuterName(p6).addChild("Road").connectNodeToOuterName(p7).addChild("Road").connectNodeToOuterName(p8).goBack()
//                .addChild("Place").connectNodeToOuterName(p6).withNewHierarchy().addChild("Road").connectNodeToOuterName(p8).addChild("Road").connectNodeToOuterName(p5).goBack()
//                .addChild("Place").linkToOuter(p7).withNewHierarchy().addChild("Road").linkToOuter(p2).addChild("Target").linkToOuter(target).goBack()
//                .addChild("Place")
        ;
//        builder.closeInnerNames(p1, p2, p3, p4, p5, p6, p7, p8, target);
//        builder.closeInnerNames(target);
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    /**
     * react r1 = Left.S | Right.S -> Left | Right;
     */
    public static ReactionRule<PureBigraph> createReactionRule() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(createSignature());

        BigraphEntity.OuterName fromD = builder.createOuterName("fromD");
        BigraphEntity.OuterName fromS = builder.createOuterName("fromS");
        BigraphEntity.OuterName target = builder.createOuterName("target");

        Supplier<PureBigraphBuilder.Hierarchy> car = () -> {
            PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy car1 = null;
            try {
                car1 = builder.hierarchy("Car").linkToOuter(target).addSite().addChild("Fuel");
            } catch (TypeNotExistsException e) {
                e.printStackTrace();
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
            return car1;
        };

        builder.createRoot()
                .addChild("Place").linkToOuter(fromD).down().addSite().top()
                .addChild("Place").linkToOuter(fromS).down().addChild(car.get()).addSite().addChild("Road").linkToOuter(fromD).top()
        ;

        BigraphEntity.OuterName fromD2 = builder2.createOuterName("fromD");
        BigraphEntity.OuterName fromS2 = builder2.createOuterName("fromS");
        BigraphEntity.OuterName target2 = builder2.createOuterName("target");
        Supplier<PureBigraphBuilder.Hierarchy> car2 = () -> {
            PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy car1 = null;
            try {
                car1 = builder2.hierarchy("Car").linkToOuter(target2).addSite();
            } catch (TypeNotExistsException e) {
                e.printStackTrace();
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
            return car1;
        };

        builder2.createRoot()
                .addChild("Place").linkToOuter(fromD2).down().addSite().addChild(car2.get()).top()
                .addChild("Place").linkToOuter(fromS2).down().addChild("Road").linkToOuter(fromD2).addSite().top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        InstantiationMap instantiationMap = InstantiationMap.create(3);
//        instantiationMap.
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private SubBigraphMatchPredicate<PureBigraph> createPredicate() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());

        BigraphEntity.OuterName from = builder.createOuterName("from");

        // links of car and target must be connected via an outer name otherwise the predicate is not matched
        builder.createRoot()
                .addChild("Place").linkToOuter(from)
//                .down().addSite().connectByEdge("Target", "Car").down().addSite();
                .down().addSite().addChild("Target", "target").addChild("Car", "target").down().addSite();
        PureBigraph bigraph = builder.createBigraph();
        return SubBigraphMatchPredicate.create(bigraph);
    }

    public static DefaultDynamicSignature createSignature() {
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
