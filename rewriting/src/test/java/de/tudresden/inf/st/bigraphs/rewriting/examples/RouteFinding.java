package de.tudresden.inf.st.bigraphs.rewriting.examples;

import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystemOptions;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.InstantiationMap;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.impl.PureReactiveSystem;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.BigraphModelChecker;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.PureBigraphModelChecker;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.exceptions.BigraphSimulationException;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.predicates.SubBigraphMatchPredicate;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import static de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystemOptions.transitionOpts;

/**
 * @author Dominik Grzelak
 */
public class RouteFinding {
    private static PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/cars/framework/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    @Test
    void simulate_car_example() throws InvalidConnectionException, TypeNotExistsException, IOException, InvalidReactionRuleException, BigraphSimulationException {
        SubBigraphMatchPredicate<PureBigraph> predicate = createPredicate();
        BigraphGraphvizExporter.toPNG(predicate.getBigraphToMatch(),
                true,
                new File(TARGET_DUMP_PATH + "predicate_car.png")
        );

//        PureBigraph map = createMapSimple(8);
        PureBigraph map = createMap(8);
//        BigraphArtifacts.exportAsMetaModel(map, new FileOutputStream("meta-model.ecore"));
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
//        Path currentRelativePath = Paths.get("");
        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ReactiveSystemOptions opts = ReactiveSystemOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(18)
                        .setMaximumTime(60)
                        .create()
                )
                .doMeasureTime(true)
                .and(ReactiveSystemOptions.exportOpts()
                        .setTraceFile(new File(completePath.toUri()))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .create()
                )
        ;

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.addReactionRule(reactionRule);
        reactiveSystem.setAgent(map);
//        reactiveSystem.addPredicate(predicate);
//        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem, BigraphModelChecker.SimulationType.RANDOM_STATE,
//                opts);
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationType.BREADTH_FIRST,
                opts);
        modelChecker.execute();
    }

    private PureBigraph createMap(int fuelLevel) throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());

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

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy car = builder.newHierarchy("Car").linkToOuter(target);
        for (int i = 0; i < fuelLevel; i++) {
            car = car.addChild("Fuel");
        }
        builder.createRoot()
                .addChild("Place").linkToOuter(p0).withNewHierarchy().addChild(car).addChild("Road").linkToOuter(p1).addChild("Road").linkToOuter(p3).goBack()
                .addChild("Place").linkToOuter(p1).withNewHierarchy().addChild("Road").linkToOuter(p2).addChild("Road").linkToOuter(p4).goBack()
                .addChild("Place").linkToOuter(p2).withNewHierarchy().addChild("Road").linkToOuter(p5).goBack()
                .addChild("Place").linkToOuter(p3).withNewHierarchy().addChild("Road").linkToOuter(p4).addChild("Road").linkToOuter(p7).goBack()
                .addChild("Place").linkToOuter(p4).withNewHierarchy().addChild("Road").linkToOuter(p5).addChild("Road").linkToOuter(p1).goBack()
//                .addChild("Place").connectNodeToOuterName(p5).withNewHierarchy().addChild("Road").connectNodeToOuterName(p6).addChild("Road").connectNodeToOuterName(p7).addChild("Road").connectNodeToOuterName(p8).goBack()
//                .addChild("Place").connectNodeToOuterName(p6).withNewHierarchy().addChild("Road").connectNodeToOuterName(p8).addChild("Road").connectNodeToOuterName(p5).goBack()
                .addChild("Place").linkToOuter(p7).withNewHierarchy().addChild("Road").linkToOuter(p2).addChild("Target").linkToOuter(target).goBack()
//                .addChild("Place")
        ;
//        builder.closeInnerNames(p1, p2, p3, p4, p5, p6, p7, p8, target);
//        builder.closeInnerNames(target);
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    private PureBigraph createMapSimple(int fuelLevel) throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());

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

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy car = builder.newHierarchy("Car").linkToOuter(target);
        for (int i = 0; i < fuelLevel; i++) {
            car = car.addChild("Fuel");
        }
        builder.createRoot()
                .addChild("Place").linkToOuter(p0).withNewHierarchy().addChild(car).addChild("Road").linkToOuter(p1).addChild("Road").linkToOuter(p3).goBack()
                .addChild("Place").linkToOuter(p1).withNewHierarchy().addChild("Road").addChild("Road").linkToOuter(p4).goBack()
//                .addChild("Place").linkToOuter(p2).withNewHierarchy().addChild("Road").linkToOuter(p5).goBack()
                .addChild("Place").linkToOuter(p3).withNewHierarchy().addChild("Road").linkToOuter(p4).addChild("Road").goBack()
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
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(createSignature());

        BigraphEntity.OuterName fromD = builder.createOuterName("fromD");
        BigraphEntity.OuterName fromS = builder.createOuterName("fromS");
        BigraphEntity.OuterName target = builder.createOuterName("target");

        Supplier<PureBigraphBuilder.Hierarchy> car = () -> {
            PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy car1 = null;
            try {
                car1 = builder.newHierarchy("Car").linkToOuter(target).addSite().addChild("Fuel");
            } catch (TypeNotExistsException e) {
                e.printStackTrace();
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
            return car1;
        };

        builder.createRoot()
                .addChild("Place").linkToOuter(fromD).withNewHierarchy().addSite().top()
                .addChild("Place").linkToOuter(fromS).withNewHierarchy().addChild(car.get()).addSite().addChild("Road").linkToOuter(fromD).top()
        ;

        BigraphEntity.OuterName fromD2 = builder2.createOuterName("fromD");
        BigraphEntity.OuterName fromS2 = builder2.createOuterName("fromS");
        BigraphEntity.OuterName target2 = builder2.createOuterName("target");
        Supplier<PureBigraphBuilder.Hierarchy> car2 = () -> {
            PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy car1 = null;
            try {
                car1 = builder2.newHierarchy("Car").linkToOuter(target2).addSite();
            } catch (TypeNotExistsException e) {
                e.printStackTrace();
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
            return car1;
        };

        builder2.createRoot()
                .addChild("Place").linkToOuter(fromD2).withNewHierarchy().addSite().addChild(car2.get()).top()
                .addChild("Place").linkToOuter(fromS2).withNewHierarchy().addChild("Road").linkToOuter(fromD2).addSite().top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        InstantiationMap instantiationMap = InstantiationMap.create(3);
//        instantiationMap.
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private SubBigraphMatchPredicate<PureBigraph> createPredicate() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignature());

        BigraphEntity.OuterName from = builder.createOuterName("from");


        builder.createRoot()
                .addChild("Place").linkToOuter(from)
                .withNewHierarchy().addSite().connectByEdge("Target", "Car").withNewHierarchy().addSite();
        PureBigraph bigraph = builder.createBigraph();
        return SubBigraphMatchPredicate.create(bigraph);
    }

    private static <C extends Control<?, ?>, S extends Signature<C>> S createSignature() {
        DynamicSignatureBuilder defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Car")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Fuel")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Place")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Road")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Target")).arity(FiniteOrdinal.ofInteger(1)).assign()
        ;
        return (S) defaultBuilder.create();
    }
}
