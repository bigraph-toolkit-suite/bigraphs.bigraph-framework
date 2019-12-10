package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class BigraphCompositionUnitTests {

    private final static String TARGET_TEST_PATH = "src/test/resources/dump/exported-models/";

    private static PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();

    @BeforeEach
    void setUp() {
    }

    @Test
    void compose_test_0() throws InvalidConnectionException, TypeNotExistsException, IncompatibleSignatureException, IncompatibleInterfaceException {
        PureBigraphBuilder<DefaultDynamicSignature> builderReactum = factory.createBigraphBuilder(createSignature_compose_test_0());
        BigraphEntity.OuterName fromD2 = builderReactum.createOuterName("fromD");
        BigraphEntity.OuterName fromS2 = builderReactum.createOuterName("fromS");
        BigraphEntity.OuterName target2 = builderReactum.createOuterName("target");
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy car2 = builderReactum.newHierarchy("Car").linkToOuter(target2).addSite();
        builderReactum.createRoot()
                .addChild("Place").linkToOuter(fromD2).withNewHierarchy().addSite().addChild(car2).top()
                .addChild("Place", "fromS").withNewHierarchy().addChild("Road").linkToOuter(fromD2).addSite().top()
//                .addChild("Place").connectNodeToOuterName(fromS2).withNewHierarchy().addChild("Road").connectNodeToOuterName(fromD2).addSite().top()
        ;
        PureBigraph reactum = builderReactum.createBigraph();

        PureBigraphBuilder<DefaultDynamicSignature> builderParams = factory.createBigraphBuilder(createSignature_compose_test_0());

        builderParams.createRoot().addChild("Fuel").addChild("Fuel").addChild("Fuel").addChild("Fuel").addChild("Fuel").addChild("Fuel").addChild("Fuel");
        BigraphEntity.OuterName p4 = builderParams.createOuterName("p4");
        BigraphEntity.OuterName p7 = builderParams.createOuterName("p7");
        builderParams.createRoot().addChild("Road").linkToOuter(p4).addChild("Road").linkToOuter(p7);
        BigraphEntity.OuterName p1 = builderParams.createOuterName("p1");
        builderParams.createRoot().addChild("Road").linkToOuter(p1);
        PureBigraph parameters = builderParams.createBigraph();

        PureBigraphBuilder<DefaultDynamicSignature> builderRenaming = factory.createBigraphBuilder(createSignature_compose_test_0());
        Linkings<DefaultDynamicSignature>.Identity identity = factory.createLinkings(createSignature_compose_test_0()).identity(StringTypedName.of("p1"), StringTypedName.of("p4"), StringTypedName.of("p7"));

        Bigraph<DefaultDynamicSignature> reactumImage = factory.asBigraphOperator(identity).parallelProduct(reactum).getOuterBigraph();

        Bigraph<DefaultDynamicSignature> reacted = factory.asBigraphOperator(reactumImage).compose(parameters).getOuterBigraph();

        assertEquals(0, reacted.getInnerNames().size());
        assertEquals(0, reacted.getSites().size());
        assertEquals(14, reacted.getNodes().size());
        assertEquals(1, reacted.getRoots().size());
        assertEquals(6, reacted.getOuterNames().size());
        long count = reacted.getOuterNames().stream().filter(x -> reacted.getPointsFromLink(x).size() == 0).count();
        assertEquals(0, count);

        // Check whether outer names are connected to the correct nodes
        BigraphEntity.OuterName fromS = reacted.getOuterNames().stream().filter(x -> x.getName().equals("fromS")).findFirst().get();
        assertEquals(1, reacted.getPointsFromLink(fromS).size());
        assertEquals("Place", reacted.getNodeOfPort((BigraphEntity.Port) new ArrayList<>(reacted.getPointsFromLink(fromS)).get(0)).getControl().getNamedType().stringValue());

        BigraphEntity.OuterName target = reacted.getOuterNames().stream().filter(x -> x.getName().equals("target")).findFirst().get();
        assertEquals(1, reacted.getPointsFromLink(target).size());
        assertEquals("Car", reacted.getNodeOfPort((BigraphEntity.Port) new ArrayList<>(reacted.getPointsFromLink(target)).get(0)).getControl().getNamedType().stringValue());

        BigraphEntity.OuterName fromD = reacted.getOuterNames().stream().filter(x -> x.getName().equals("fromD")).findFirst().get();
        assertEquals(2, reacted.getPointsFromLink(fromD).size());
//        assertEquals("Place", reacted.getNodeOfPort((BigraphEntity.Port) new ArrayList<>(reacted.getPointsFromLink(fromD)).get(0)).getControl().getNamedType().stringValue());
//        assertEquals("Road", reacted.getNodeOfPort((BigraphEntity.Port) new ArrayList<>(reacted.getPointsFromLink(fromD)).get(1)).getControl().getNamedType().stringValue());

    }

    private static <C extends Control<?, ?>, S extends Signature<C>> S createSignature_compose_test_0() {
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

    @Test
    void compose_test() throws InvalidConnectionException, TypeNotExistsException, IncompatibleSignatureException, IncompatibleInterfaceException, ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builderForF = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builderForG = factory.createBigraphBuilder(signature);

        BigraphEntity.OuterName jeff = builderForF.createOuterName("jeff");
        BigraphEntity.InnerName jeffG = builderForG.createInnerName("jeff");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room =
                builderForF.newHierarchy(signature.getControlByName("Room"));
        room.addChild(signature.getControlByName("User")).linkToOuter(jeff).addChild(signature.getControlByName("Job"));
        builderForF.createRoot()
                .addChild(room);

        builderForG.createRoot()
                .addChild(signature.getControlByName("Job")).withNewHierarchy().addSite().goBack()
                .addChild(signature.getControlByName("User")).linkToInner(jeffG);


        PureBigraph F = builderForF.createBigraph();
        PureBigraph G = builderForG.createBigraph();


        BigraphComposite<DefaultDynamicSignature> compositor = factory.asBigraphOperator(G);
//        PureBigraphComposite<DefaultDynamicSignature> compositor = (PureBigraphComposite<DefaultDynamicSignature>) factory.asBigraphOperator(G);
        BigraphComposite<DefaultDynamicSignature> composedBigraph = compositor.compose(F);

        compositor.juxtapose(F);

//        Bigraph<DefaultDynamicSignature> outerBigraph = composedBigraph.getOuterBigraph();

//        BigraphEntity.NodeEntity<DefaultDynamicControl> next = F.getNodes().iterator().next();
//        BigraphEntity parent = F.getParent(next);
//        BigraphEntity parent0 = F.getParent(parent);
//        int index = ((BigraphEntity.RootEntity) parent0).getIndex();
//        System.out.println("index=" + index + " __ equals= " + parent0.equals(parent));
    }

    @Test
    void parallelProduct() throws InvalidConnectionException, TypeNotExistsException, IncompatibleSignatureException, IncompatibleInterfaceException, IOException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builderForF = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builderForG = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builderForH = factory.createBigraphBuilder(signature);

        BigraphEntity.OuterName networkF = builderForF.createOuterName("wifi");
        BigraphEntity.OuterName networkG = builderForG.createOuterName("wifi");
        BigraphEntity.InnerName ethernetF = builderForF.createInnerName("ethernet");
        BigraphEntity.InnerName ethernetF2 = builderForF.createInnerName("ethernet2");
        BigraphEntity.InnerName ethernetG = builderForG.createInnerName("ethernet");
//        BigraphEntity.InnerName ethernetH = builderForH.createInnerName("ethernet");
//        BigraphEntity.InnerName networkH = builderForH.createInnerName("wifi");
        if (true) {
            builderForF.createRoot().addChild(signature.getControlByName("Room")).withNewHierarchy()
                    .addChild(signature.getControlByName("Computer")).linkToOuter(networkF);//.connectNodeToInnerName(ethernetF);
            builderForG.createRoot().addChild(signature.getControlByName("Room")).withNewHierarchy()
                    .addChild(signature.getControlByName("Computer")).linkToOuter(networkG);//.connectNodeToInnerName(ethernetG);

//            builderForF.connectInnerToOuterName(ethernetF, networkF);
//            builderForG.connectInnerToOuterName(ethernetG, networkG);
        } else {
            builderForF.createRoot().addChild(signature.getControlByName("Room")).withNewHierarchy()
                    .addChild(signature.getControlByName("Computer")).connectInnerNamesToNode(ethernetF, ethernetF2);
            builderForG.createRoot().addChild(signature.getControlByName("Room")).withNewHierarchy()
                    .addChild(signature.getControlByName("Computer")).linkToInner(ethernetG);
            builderForH.createRoot().addChild(signature.getControlByName("Room")).withNewHierarchy()
                    .addChild(signature.getControlByName("Printer"));
        }

        PureBigraph F = builderForF.createBigraph();
        PureBigraph G = builderForG.createBigraph();
        PureBigraph H = builderForH.createBigraph();
        BigraphArtifacts.exportAsInstanceModel((PureBigraph) F,
                new FileOutputStream(TARGET_TEST_PATH + "F.xmi"));
        BigraphArtifacts.exportAsInstanceModel((PureBigraph) G,
                new FileOutputStream(TARGET_TEST_PATH + "G.xmi"));

        BigraphComposite<DefaultDynamicSignature> juxtapose = factory.asBigraphOperator(F).parallelProduct(G);
        Bigraph<DefaultDynamicSignature> result = juxtapose.getOuterBigraph();
        BigraphArtifacts.exportAsInstanceModel(result,
                new FileOutputStream(TARGET_TEST_PATH + "result.xmi"));
//        System.out.println(result.getSupport());

    }

    @Test
    void elementary_bigraph_composition_test() {
        //we want to check whether: merge_m+1 = join(id1 ⊗ merge_m).
        int m = 3;
        Placings<DefaultDynamicSignature> placings = factory.createPlacings();
        Placings<DefaultDynamicSignature>.Merge merge_MplusOne = placings.merge(m + 1);

        Placings<DefaultDynamicSignature>.Join aJoin = placings.join();
        Placings<DefaultDynamicSignature>.Merge merge_1 = placings.merge(1); //id_1 = merge_1
        Placings<DefaultDynamicSignature>.Merge merge_M = placings.merge(m);

        BigraphComposite<DefaultDynamicSignature> a = factory.asBigraphOperator(merge_1);
        BigraphComposite<DefaultDynamicSignature> b = factory.asBigraphOperator(aJoin);

        assertAll(() -> {
            Bigraph<DefaultDynamicSignature> outerBigraph = b.compose(a.juxtapose(merge_M).getOuterBigraph()).getOuterBigraph();
            assertEquals(merge_MplusOne.getRoots().size(), outerBigraph.getRoots().size());
            assertEquals(merge_MplusOne.getSites().size(), outerBigraph.getSites().size());
            assertEquals(merge_MplusOne.getOuterNames().size() == 0, outerBigraph.getOuterNames().size() == 0);
            assertEquals(merge_MplusOne.getOuterNames().size() == 0, outerBigraph.getInnerNames().size() == 0);

            // do the sites have the same and only one parent?
            BigraphEntity.RootEntity rootA = outerBigraph.getRoots().iterator().next();
            List<Boolean> collectA = outerBigraph.getSites().stream().map(x -> outerBigraph.getParent(x) == rootA).collect(Collectors.toList());
            BigraphEntity.RootEntity rootB = merge_MplusOne.getRoots().iterator().next();
            List<Boolean> collectB = merge_MplusOne.getSites().stream().map(x -> merge_MplusOne.getParent(x) == rootB).collect(Collectors.toList());

            assertEquals(collectA.stream().allMatch(y -> y), collectB.stream().allMatch(y -> y));
        });
    }

    @Test
    void elementary_bigraph_linkings_test() {
        Linkings<DefaultDynamicSignature> linkings = new Linkings<>(factory);
        Linkings<DefaultDynamicSignature>.Closure x1 = linkings.closure(StringTypedName.of("x"));
        final Linkings<DefaultDynamicSignature>.Closure x2 = linkings.closure(StringTypedName.of("x"));

        BigraphComposite<DefaultDynamicSignature> x1Op = factory.asBigraphOperator(x1);
        assertThrows(IncompatibleInterfaceException.class, () -> x1Op.juxtapose(x2));

        final Linkings<DefaultDynamicSignature>.Closure x3 = linkings.closure(StringTypedName.of("y"));
        assertAll(() -> {
            BigraphComposite<DefaultDynamicSignature> juxtapose = x1Op.juxtapose(x3);
            assertEquals(2, juxtapose.getOuterBigraph().getInnerNames().size());
            assertEquals(2, juxtapose.getOuterBigraph().getInnerFace().getValue().size());
        });


        assertAll(() -> {

            //a bigraph is composed with a closure resulting in a inner name rewriting of that bigraph
            Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builderForG = factory.createBigraphBuilder(signature);
            BigraphEntity.InnerName zInner = builderForG.createInnerName("z");
            builderForG.createRoot().addChild(signature.getControlByName("User")).linkToInner(zInner);
            PureBigraph simpleBigraph = builderForG.createBigraph();
            Linkings<DefaultDynamicSignature>.Substitution substitution = linkings.substitution(StringTypedName.of("z"), StringTypedName.of("y"));
            BigraphComposite<DefaultDynamicSignature> compose = factory.asBigraphOperator(simpleBigraph);
            BigraphComposite<DefaultDynamicSignature> compose1 = compose.compose(substitution);

            assertFalse(compose1.getOuterBigraph().isGround());
            assertEquals(1, compose1.getOuterBigraph().getInnerNames().size());
            // name was overwritten
            assertEquals("y", compose1.getOuterBigraph().getInnerNames().iterator().next().getName());
            assertEquals(0, compose1.getOuterBigraph().getOuterNames().size());
        });


        assertAll(() -> {
            Linkings<DefaultDynamicSignature>.Substitution a1 = linkings.substitution(StringTypedName.of("a"), StringTypedName.of("b"), StringTypedName.of("c"));
            Linkings<DefaultDynamicSignature>.Substitution a2 = linkings.substitution(StringTypedName.of("x"), StringTypedName.of("a"));
            Bigraph<DefaultDynamicSignature> outerBigraph = factory.asBigraphOperator(a2).compose(a1).getOuterBigraph();
            assertEquals(outerBigraph.getOuterNames().size(), 1);
            assertEquals(outerBigraph.getInnerNames().size(), 2);
            assertEquals(outerBigraph.getNodes().size(), 0);
            assertEquals(outerBigraph.getRoots().size(), 0);
            assertEquals(outerBigraph.getSites().size(), 0);
            assertFalse(outerBigraph.isGround());
        });
    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder signatureBuilder = factory.createSignatureBuilder();
        signatureBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign();

        return (S) signatureBuilder.create();
    }
}
