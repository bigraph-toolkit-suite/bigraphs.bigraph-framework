package org.bigraphs.framework.core;

import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidArityOfControlException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.elementary.DiscreteIon;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.junit.jupiter.api.Assertions.*;

public class BigraphCompositionUnitTests {

    private final static String TARGET_TEST_PATH = "src/test/resources/dump/exported-models/";
    private final static String TARGET_TEST_MODEL_PATH = "src/test/resources/ecore-test-models/";
//    private static PureBigraphFactory factory = pure();

    @BeforeAll
    static void beforeAll() {
        EPackage exampleMetaModel = getExampleMetaModel();
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    void test_01() throws IncompatibleSignatureException, IncompatibleInterfaceException, IOException {
        DynamicSignature sig = pureSignatureBuilder()
                .newControl("K", 1).assign()
                .newControl("L", 1).assign()
                .create();
        DiscreteIon<DynamicSignature> K_x =
                pureDiscreteIon(sig, "K", "x");
        DiscreteIon<DynamicSignature> L_x =
                pureDiscreteIon(sig, "L", "x");
        Linkings<DynamicSignature>.Closure x = pureLinkings(sig).closure("x");
        BigraphComposite<DynamicSignature> G = ops(K_x).merge(L_x);
        ops(x).compose(G);
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) G, System.out);
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) (ops(x).compose(G).getOuterBigraph()), System.out);
    }

    @Test
    void test_02() {
        DynamicSignature sig = pureSignatureBuilder()
                .add("K", 1, ControlStatus.ATOMIC)
                .add("L", 1, ControlStatus.ATOMIC)
                .create();
        try {
            pureBuilder(sig)
                    .root()
                    .child("K").down();
        } catch (ControlIsAtomicException e) {
            // Exception handling
            e.printStackTrace();
        }
    }

    @Test
    void test_03() {
        DynamicSignature sig = pureSignatureBuilder()
                .add("K", 0, ControlStatus.ATOMIC)
                .add("L", 1, ControlStatus.ATOMIC)
                .create();
        try {
            pureBuilder(sig)
                    .root()
                    .connectByEdge("K", "L");
        } catch (InvalidArityOfControlException e) {
            // Exception handling
            e.printStackTrace();
        }
    }

    @Test
    void test_04() {
        DynamicSignature sig = pureSignatureBuilder()
                .add("K", 1, ControlStatus.ATOMIC)
                .add("L", 1, ControlStatus.ATOMIC)
                .create();

        AbstractEcoreSignature<? extends Control<?, ?>> signatureFromMetaModel
                = BigraphBuilderSupport.getSignatureFromMetaModel(sig.getInstanceModel());
//        System.out.println(signatureFromMetaModel);
    }

    @Test
    void parallelproduct_test() throws Exception {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(createExampleSignature());
        PureBigraph bigraph = b.root().child("Computer").linkInner("eref").create();
        Linkings<DynamicSignature>.Identity identity = pureLinkings(createExampleSignature()).identity("eref");

        Bigraph<DynamicSignature> result = ops(bigraph).parallelProduct(identity).getOuterBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) result, System.out);


        PureBigraphBuilder<DynamicSignature> b2 = pureBuilder(createExampleSignature());
        PureBigraph bigraph2 = b2.root().child("Computer").linkInner("eref").create();
        Linkings<DynamicSignature>.Substitution substitution = pureLinkings(createExampleSignature()).substitution("eref", "eref", "a");
        Bigraph<DynamicSignature> result2 = ops(bigraph2).parallelProduct(substitution).getOuterBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) result2, System.out);
    }

    @Test
    void bigraphNesting_test_withoutLinks() throws InvalidConnectionException, TypeNotExistsException, IOException, IncompatibleSignatureException, IncompatibleInterfaceException {
        EPackage exampleMetaModel = getExampleMetaModel();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createExampleSignature());
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(createExampleSignature());
        PureBigraph bigraph = builder.root()
                .child("Room").down().child("User")
                .child("Computer")
                .site()
                .top()
                .create();
        PureBigraph userBigraph = builder2.root()
                .child("User")
                .top()
                .create();
//        BigraphFileModelManagement.exportAsMetaModel(userBigraph, new FileOutputStream(userBigraph.getEMetaModelData().getName() + ".ecore"));
//        BigraphFileModelManagement.loadBigraphMetaModel(userBigraph.getEMetaModelData().getName() + ".ecore");
        BigraphComposite<DynamicSignature> nesting = ops(bigraph).nesting(userBigraph);
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) nesting.getOuterBigraph(), System.out);
    }

    @Test
    void bigraphNesting_test() throws InvalidConnectionException, TypeNotExistsException, IOException, IncompatibleSignatureException, IncompatibleInterfaceException {
        EPackage exampleMetaModel = getExampleMetaModel();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createExampleSignature());
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(createExampleSignature());
        BigraphEntity.OuterName jeff = builder.createOuter("jeff");
        PureBigraph bigraph = builder.root()
                .child("Room").down().child("User", "jeff")
                .child("Computer").linkOuter(jeff)
                .site()
                .top()
                .create();
        PureBigraph userBigraph = builder2.root()
                .child("User", "jeff")
                .top()
                .create();
        System.out.println("-----------------------------------------");
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, System.out);
        System.out.println("-----------------------------------------");
        BigraphFileModelManagement.Store.exportAsInstanceModel(userBigraph, System.out);
        PureBigraph nesting = ops(bigraph).nesting(userBigraph).getOuterBigraph();
//        System.out.println("-----------------------------------------");
        BigraphFileModelManagement.Store.exportAsInstanceModel(nesting, System.out);
        assertEquals(1, nesting.getRoots().size());
        assertEquals(4, nesting.getNodes().size());
        assertEquals(0, nesting.getSites().size());
        assertEquals(1, nesting.getOuterNames().size());
        assertEquals("jeff", nesting.getOuterNames().get(0).getName());
        assertEquals(3, nesting.getPointsFromLink(nesting.getOuterNames().stream().filter(x -> x.getName().equals("jeff")).findFirst().get()).size());
    }

    @Test
    @DisplayName("Closure test: Close open link by transforming it to a closed link")
    void closure_test_01() throws InvalidConnectionException, IOException, IncompatibleSignatureException, IncompatibleInterfaceException {
        // (id(1) | /("x")) * (a["x"] | a["x"])
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createExampleSignature());
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(createExampleSignature());

        // (Computer["x"] | Computer["x"])
        builder.root().child("Computer", "x").child("Computer", "x");

        PureBigraph bigraph = builder.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, System.out);

        // (id(1) | /("x"))
        builder2.createInner("x");
        builder2.root().site();
        PureBigraph left = builder2.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel(left, System.out);

        Bigraph<DynamicSignature> result = ops(left).compose(bigraph).getOuterBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) result, System.out);
        assertNotNull(result);
        assertEquals(3, result.getAllPlaces().size());
        assertEquals(1, result.getEdges().size());
        assertEquals(0, result.getOuterNames().size());
        assertEquals(0, result.getInnerNames().size());
        assertEquals(2, result.getPointsFromLink(result.getEdges().iterator().next()).size());
//        Bigraph<DefaultDynamicSignature> result2 = new PureBigraphComposite<>(left).composeV2(bigraph).getOuterBigraph();
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) result2, System.out);
    }

    @Test
    @DisplayName("Compose two bigraphs, which are the same instance")
    void composition_of_sameBigraphInstance() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException, LinkTypeNotExistsException, IOException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createExampleSignature());
        BigraphEntity.InnerName jeff = builder.createInner("jeff");
        PureBigraph bigraph = builder.root()
                .child("Room").down().child("User", "jeff")
                .child("Computer").linkInner(jeff)
                .site()
                .create();

        BigraphComposite<DynamicSignature> comp = ops(bigraph).parallelProduct(bigraph);
        Bigraph<DynamicSignature> outerBigraph = comp.getOuterBigraph();
//        BigraphFileModelManagement.exportAsInstanceModel((PureBigraph) outerBigraph, new FileOutputStream(TARGET_TEST_PATH + "same-instance.xmi"));

        Bigraph<DynamicSignature> comp2 = ops(bigraph).compose(bigraph).getOuterBigraph();
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) comp2, new FileOutputStream(TARGET_TEST_PATH + "same-instance2.xmi"));
    }

//    @Test
//    @DisplayName("Compose two bigraphs and check their EMetaModel data")
//    void compose_test_with_emetamodel_check() throws IncompatibleSignatureException, IncompatibleInterfaceException, IOException {
//        Signature<DefaultDynamicControl> signature = createExampleSignature();
//        EMetaModelData metaData = EMetaModelData.builder().setName("model-check").setNsPrefix("model").setNsUri("org.example.check").create();
////        EMetaModelData metaData2 = EMetaModelData.builder().setName("model-check2").setNsPrefix("model2").setNsUri("org.example.check2").create();
//        EPackage bMM1 = getExampleMetaModel();
////        EPackage bMM2 = createOrGetMetaModel(signature, metaData2);
//
//        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature, bMM1);
//        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature, bMM1);
//
//        PureBigraph building = builder.root().child("Room").child("Room").down().site().createBigraph();
//        PureBigraph user = builder2.root().child("User").createBigraph();
//
//        BigraphComposite<DefaultDynamicSignature> composed = ops(building).compose(user);
//        BigraphComposite<DefaultDynamicSignature> composed2 = ops(building).juxtapose(user);
//        assertNotNull(composed);
//        assertNotNull(composed2);
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) composed, System.out);
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) composed2, System.out);
//
////        assertEquals(metaData.getName(), ((PureBigraph) composed.getOuterBigraph()).getModelPackage().getName());
////        assertEquals(metaData.getNsPrefix(), ((PureBigraph) composed.getOuterBigraph()).getModelPackage().getNsPrefix());
////        assertEquals(metaData.getNsUri(), ((PureBigraph) composed.getOuterBigraph()).getModelPackage().getNsURI());
//    }

    @Test
    void compose_test_0() throws InvalidConnectionException, TypeNotExistsException, IncompatibleSignatureException, IncompatibleInterfaceException {
        EPackage exampleMetaModel = createOrGetBigraphMetaModel(createSignature_compose_test_0());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(createSignature_compose_test_0());
        BigraphEntity.OuterName fromD2 = builderReactum.createOuter("fromD");
        BigraphEntity.OuterName fromS2 = builderReactum.createOuter("fromS");
        BigraphEntity.OuterName target2 = builderReactum.createOuter("target");
        PureBigraphBuilder<DynamicSignature>.Hierarchy car2 = builderReactum.hierarchy("Car").linkOuter(target2).site();
        builderReactum.root()
                .child("Place").linkOuter(fromD2).down().site().child(car2).top()
                .child("Place", "fromS").down().child("Road").linkOuter(fromD2).site().top()
//                .child("Place").connectNodeToOuterName(fromS2).withNewHierarchy().child("Road").connectNodeToOuterName(fromD2).site().top()
        ;
        PureBigraph reactum = builderReactum.create();

        PureBigraphBuilder<DynamicSignature> builderParams = pureBuilder(createSignature_compose_test_0());

        builderParams.root().child("Fuel").child("Fuel").child("Fuel").child("Fuel").child("Fuel").child("Fuel").child("Fuel");
        BigraphEntity.OuterName p4 = builderParams.createOuter("p4");
        BigraphEntity.OuterName p7 = builderParams.createOuter("p7");
        builderParams.root().child("Road").linkOuter(p4).child("Road").linkOuter(p7);
        BigraphEntity.OuterName p1 = builderParams.createOuter("p1");
        builderParams.root().child("Road").linkOuter(p1);
        PureBigraph parameters = builderParams.create();

        PureBigraphBuilder<DynamicSignature> builderRenaming = pureBuilder(createSignature_compose_test_0());
        Linkings<DynamicSignature>.Identity identity = pureLinkings(createSignature_compose_test_0()).identity(StringTypedName.of("p1"), StringTypedName.of("p4"), StringTypedName.of("p7"));

        Bigraph<DynamicSignature> reactumImage = ops(identity).parallelProduct(reactum).getOuterBigraph();

        Bigraph<DynamicSignature> reacted = ops(reactumImage).compose(parameters).getOuterBigraph();

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

    private static DynamicSignature createSignature_compose_test_0() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add("Car", 1)
                .add("Fuel", 0)
                .add("Place", 1)
                .add("Road", 1)
                .add("Target", 1)
        ;
        return defaultBuilder.create();
    }

    @Test
    void compose_test() throws InvalidConnectionException, TypeNotExistsException, IncompatibleSignatureException, IncompatibleInterfaceException, ControlIsAtomicException {
        EPackage exampleMetaModel = getExampleMetaModel();
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builderForF = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builderForG = pureBuilder(signature);

        BigraphEntity.OuterName jeff = builderForF.createOuter("jeff");
        BigraphEntity.InnerName jeffG = builderForG.createInner("jeff");

        PureBigraphBuilder<DynamicSignature>.Hierarchy room =
                builderForF.hierarchy(signature.getControlByName("Room"));
        room.child(signature.getControlByName("User")).linkOuter(jeff).child(signature.getControlByName("Job"));
        builderForF.root()
                .child(room);

        builderForG.root()
                .child(signature.getControlByName("Job")).down().site().up()
                .child(signature.getControlByName("User")).linkInner(jeffG);


        PureBigraph F = builderForF.create();
        PureBigraph G = builderForG.create();

        EPackage modelPackage = F.getMetaModel();


        BigraphComposite<DynamicSignature> compositor = ops(G);
//        PureBigraphComposite<DefaultDynamicSignature> compositor = (PureBigraphComposite<DefaultDynamicSignature>) factory.asBigraphOperator(G);
        BigraphComposite<DynamicSignature> composedBigraph = compositor.compose(F);

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
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builderForF = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builderForG = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builderForH = pureBuilder(signature);

        BigraphEntity.OuterName networkF = builderForF.createOuter("wifi");
        BigraphEntity.OuterName networkG = builderForG.createOuter("wifi");
        BigraphEntity.InnerName ethernetF = builderForF.createInner("ethernet");
        BigraphEntity.InnerName ethernetF2 = builderForF.createInner("ethernet2");
        BigraphEntity.InnerName ethernetG = builderForG.createInner("ethernet");
//        BigraphEntity.InnerName ethernetH = builderForH.createInnerName("ethernet");
//        BigraphEntity.InnerName networkH = builderForH.createInnerName("wifi");
        if (true) {
            builderForF.root().child(signature.getControlByName("Room")).down()
                    .child(signature.getControlByName("Computer")).linkOuter(networkF);//.connectNodeToInnerName(ethernetF);
            builderForG.root().child(signature.getControlByName("Room")).down()
                    .child(signature.getControlByName("Computer")).linkOuter(networkG);//.connectNodeToInnerName(ethernetG);

//            builderForF.connectInnerToOuterName(ethernetF, networkF);
//            builderForG.connectInnerToOuterName(ethernetG, networkG);
        } else {
            builderForF.root().child(signature.getControlByName("Room")).down()
                    .child(signature.getControlByName("Computer")).connectInnerNamesToNode(ethernetF, ethernetF2);
            builderForG.root().child(signature.getControlByName("Room")).down()
                    .child(signature.getControlByName("Computer")).linkInner(ethernetG);
            builderForH.root().child(signature.getControlByName("Room")).down()
                    .child(signature.getControlByName("Printer"));
        }

        PureBigraph F = builderForF.create();
        PureBigraph G = builderForG.create();
        PureBigraph H = builderForH.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel((PureBigraph) F,
                new FileOutputStream(TARGET_TEST_PATH + "F.xmi"));
        BigraphFileModelManagement.Store.exportAsInstanceModel((PureBigraph) G,
                new FileOutputStream(TARGET_TEST_PATH + "G.xmi"));

        BigraphComposite<DynamicSignature> juxtapose = ops(F).parallelProduct(G);
        Bigraph<DynamicSignature> result = juxtapose.getOuterBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) result,
                new FileOutputStream(TARGET_TEST_PATH + "result.xmi"));
    }

    @Test
    void elementary_bigraph_composition_test() {
        //we want to check whether: merge_m+1 = join(id1 ⊗ merge_m).
        int m = 3;
        DynamicSignature empty = pureSignatureBuilder().createEmpty();
        Placings<DynamicSignature> placings = purePlacings(empty);
        Placings<DynamicSignature>.Merge merge_MplusOne = placings.merge(m + 1);

        Placings<DynamicSignature>.Join aJoin = placings.join();
        Placings<DynamicSignature>.Merge merge_1 = placings.merge(1); //id_1 = merge_1
        Placings<DynamicSignature>.Merge merge_M = placings.merge(m);

        BigraphComposite<DynamicSignature> a = ops(merge_1);
        BigraphComposite<DynamicSignature> b = ops(aJoin);

        assertAll(() -> {
            Bigraph<DynamicSignature> tmp = a.juxtapose(merge_M).getOuterBigraph();
            assertEquals(2, tmp.getRoots().size());
            assertEquals(4, tmp.getSites().size());
            Bigraph<DynamicSignature> outerBigraph = b.compose(tmp).getOuterBigraph();
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
    void more_elementary_linkings_tests() {
        final DynamicSignature mainSignature = createExampleSignature();
        final EPackage bMetaModel = createOrGetBigraphMetaModel(mainSignature);
        final Linkings<DynamicSignature> linkings = pureLinkings(mainSignature);
        final Placings<DynamicSignature> placings = purePlacings(mainSignature);

        assertAll(() -> {
            Linkings<DynamicSignature>.Identity a = linkings.identity(StringTypedName.of("a"));
            PureBigraphBuilder<DynamicSignature> builder = pureBuilder(mainSignature);
            builder.createOuter("a");
            PureBigraph bigraph = builder.create();

            Bigraph<DynamicSignature> compose = ops(a).compose(bigraph).getOuterBigraph();
            assertEquals(1, compose.getOuterNames().size());
            assertEquals(0, compose.getInnerNames().size());

            assertThrows(IncompatibleInterfaceException.class, () -> {
                ops(bigraph).compose(a).getOuterBigraph();
            });

            PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(mainSignature);
            builder2.linkInnerToOuter(builder2.createInner("a"), builder2.createOuter("a"));
            PureBigraph bigraph2 = builder2.create();
            Bigraph<DynamicSignature> compose2 = ops(a).compose(bigraph2).getOuterBigraph();
            assertEquals(1, compose2.getOuterNames().size());
            assertEquals(1, compose2.getInnerNames().size());
            assertEquals("a", compose2.getLinkOfPoint(compose2.getInnerNames().iterator().next()).getName());
        });

        assertAll(() -> {
            Linkings<DynamicSignature>.Substitution s1 = linkings.substitution(StringTypedName.of("y"), StringTypedName.of("y"));
            Linkings<DynamicSignature>.Closure c1 = linkings.closure(StringTypedName.of("x"));
            Linkings<DynamicSignature>.Closure c2 = linkings.closure(StringTypedName.of("a"));
            Linkings<DynamicSignature>.Closure y = linkings.closure(StringTypedName.of("y"));
            DiscreteIon<DynamicSignature> ionC = pureDiscreteIon(mainSignature, "Printer", "x", "y");

            // id(2) * (/x || /y): <2,{}> and <0,{}> doesn't works
            assertThrows(IncompatibleInterfaceException.class, () -> {
                Placings<DynamicSignature>.Permutation permutation = placings.permutation(2);
                BigraphComposite<DynamicSignature> tmp = ops(c1).parallelProduct(y);
                ops(permutation).compose(tmp).getOuterBigraph();
            });

            ///x * User{x}
            Bigraph<DynamicSignature> outerBigraph3 = ops(placings.identity1()).juxtapose(c1).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) outerBigraph3, System.out);

            DiscreteIon<DynamicSignature> discreteIon = pureDiscreteIon(mainSignature, "User", "x");
            Bigraph<DynamicSignature> result0 = ops(outerBigraph3).compose(discreteIon).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) result0, System.out);
            assertEquals(0, result0.getOuterNames().size());
            assertEquals(0, result0.getInnerNames().size());
            assertEquals(3, result0.getAllPlaces().size());
            assertEquals(1, result0.getRoots().size());
            assertEquals(1, result0.getNodes().size());
            assertEquals(1, result0.getSites().size());

            // parallel product between linkings : shall not create root
            Bigraph<DynamicSignature> composed1 = ops(c1).parallelProduct(s1).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) composed1, System.out);
            assertEquals(1, composed1.getOuterNames().size());
            assertEquals(2, composed1.getInnerNames().size());
            assertEquals(0, composed1.getAllPlaces().size());
            assertEquals(0, composed1.getRoots().size());
            assertTrue(BigraphUtil.isBigraphElementaryLinking(composed1));

            // parallel product between linkings: shall not create root
            Bigraph<DynamicSignature> composed2 = ops(c1).parallelProductOf(c1, s1).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) composed2, System.out);
            assertEquals(1, composed2.getOuterNames().size());
            assertEquals(2, composed2.getInnerNames().size());
            assertEquals(0, composed2.getAllPlaces().size());
            assertEquals(0, composed2.getRoots().size());
            assertTrue(BigraphUtil.isBigraphElementaryLinking(composed2));

            // merge product between linkings
            Bigraph<DynamicSignature> merged = ops(c1).merge(s1).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) merged, System.out);
            assertEquals(1, merged.getOuterNames().size());
            assertEquals(2, merged.getInnerNames().size());
            assertEquals(1, merged.getAllPlaces().size());
            assertEquals(1, merged.getRoots().size());

            // mutliple merges
            Bigraph<DynamicSignature> merged2 = ops(c1).merge(c1).merge(c1).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) merged2, System.out);
            assertEquals(0, merged2.getOuterNames().size());
            assertEquals(1, merged2.getInnerNames().size());
            assertEquals(1, merged2.getAllPlaces().size());
            assertEquals(1, merged2.getRoots().size());

            // merge product of two different linkings
            Bigraph<DynamicSignature> merged3 = ops(c1).merge(c1).merge(c2).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) merged3, System.out);
            assertEquals(0, merged3.getOuterNames().size());
            assertEquals(2, merged3.getInnerNames().size());
            assertEquals(1, merged3.getAllPlaces().size());
            assertEquals(1, merged3.getRoots().size());

            // (/x || (/x | /a))
            Bigraph<DynamicSignature> result = ops(c1).parallelProduct(ops(c1).merge(c2)).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) result, System.out);
            assertEquals(0, result.getOuterNames().size());
            assertEquals(2, result.getInnerNames().size());
            assertEquals(1, result.getAllPlaces().size());
            assertEquals(1, result.getRoots().size());
            assertEquals(0, result.getSites().size());


            // 1 * (/x || /y): <0,{}> and <0,{}> works
            Placings<DynamicSignature>.Barren barren = placings.barren();
            Bigraph<DynamicSignature> result2 = ops(barren).compose(ops(c1).parallelProduct(y)).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) result2, System.out);
            assertEquals(0, result2.getOuterNames().size());
            assertEquals(2, result2.getInnerNames().size());
            assertEquals(1, result2.getAllPlaces().size());
            assertEquals(1, result2.getRoots().size());
            assertEquals(0, result2.getSites().size());

            //1 * (/x | /y): <0, {}> and <1, {}> doesn't work
            assertThrows(IncompatibleInterfaceException.class, () -> {
                ops(placings.barren()).compose(ops(c1).merge(y)).getOuterBigraph();
            });

            // /x * /y * /a: <0, {x}> and <0, {}> doesn't work
            assertThrows(IncompatibleInterfaceException.class, () -> {
                ops(c1).compose(y).compose(c2).getOuterBigraph();
            });

            //(((x/{x}) * x/{y,x})||id(1)) * C{x,y}
            Placings<DynamicSignature>.Identity1 id_1 = placings.identity1();
            Linkings<DynamicSignature>.Identity id_x = linkings.identity(StringTypedName.of("x"), StringTypedName.of("x"));
            Linkings<DynamicSignature>.Substitution sigma_yx = linkings.substitution(StringTypedName.of("x"), StringTypedName.of("y"), StringTypedName.of("x"));
            Bigraph<DynamicSignature> outerBigraph = ops(ops(id_x).compose(sigma_yx).getOuterBigraph()).parallelProduct(id_1).getOuterBigraph();
            assertEquals(1, outerBigraph.getOuterNames().size());
            assertEquals(2, outerBigraph.getInnerNames().size());
            assertEquals(2, outerBigraph.getAllPlaces().size());
            assertEquals(1, outerBigraph.getRoots().size());
            assertEquals(0, outerBigraph.getNodes().size());
            assertEquals(1, outerBigraph.getSites().size());
            Bigraph<DynamicSignature> outerBigraph1 = ops(outerBigraph).compose(ionC).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) outerBigraph1, System.out);
            assertEquals(1, outerBigraph1.getOuterNames().size());
            assertEquals(0, outerBigraph1.getEdges().size());
            assertEquals(0, outerBigraph1.getInnerNames().size());
            assertEquals(3, outerBigraph1.getAllPlaces().size());
            assertEquals(1, outerBigraph1.getRoots().size());
            assertEquals(1, outerBigraph1.getNodes().size());
            assertEquals(1, outerBigraph1.getSites().size());

            //(id(1) || /x || y/{y}) * C{x,y}: closes one open link 'x' but keeps the idle edge
            Linkings<DynamicSignature>.Substitution sigma_y = linkings.substitution(StringTypedName.of("y"), StringTypedName.of("y"));
            Bigraph<DynamicSignature> outerBigraph2 = ops(placings.identity1()).parallelProduct(c1).parallelProduct(sigma_y).getOuterBigraph();
            Bigraph<DynamicSignature> composed3 = ops(outerBigraph2).compose(ionC).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) composed3, System.out);
            assertEquals(1, composed3.getOuterNames().size());
            assertEquals("y", composed3.getOuterNames().iterator().next().getName());
            assertEquals(1, composed3.getEdges().size());
            assertEquals(0, composed3.getInnerNames().size());
            assertEquals(3, composed3.getAllPlaces().size());
            assertEquals(1, composed3.getRoots().size());
            assertEquals(1, composed3.getNodes().size());
            assertEquals(1, composed3.getSites().size());
        });
    }

    @Test
    void test_example_3_14() {
        DynamicSignature sig = pureSignatureBuilder().newControl("K", 3).assign()
                .newControl("L", 2).assign().create();

        DiscreteIon<DynamicSignature> K_xyz = pureDiscreteIon(sig, "K", "x", "y", "z");
        DiscreteIon<DynamicSignature> L_yz = pureDiscreteIon(sig, "L", "y", "z");

        assertAll(() -> {
            Bigraph<DynamicSignature> outerBigraph = ops(K_xyz).nesting(L_yz).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) outerBigraph, System.out);
        });
    }

    @Test
    void elementary_bigraph_linkings_test() {
//        DefaultDynamicSignature empty = pureSignatureBuilder().createEmpty();
        DynamicSignature exampleSignature = createExampleSignature();
        EPackage exampleMetaModel = getExampleMetaModel();
        Linkings linkings = pureLinkings(exampleSignature);

        assertAll(() -> {
            Linkings<DynamicSignature>.Closure xyz = linkings.closure(
                    Sets.mutable.of(StringTypedName.of("a"), StringTypedName.of("x"), StringTypedName.of("y"))
            );
            BigraphFileModelManagement.Store.exportAsInstanceModel(xyz, System.out);
            assertEquals(3, xyz.getInnerNames().size());
            assertEquals(0, xyz.getOuterNames().size());

            Linkings<DynamicSignature>.Closure x = linkings.closure(StringTypedName.of("x"));
            DiscreteIon<DynamicSignature> discreteIon = pureDiscreteIon(
                    exampleSignature,
                    "User",
                    "x"
            );
            assertEquals(1, discreteIon.getRoots().size());
            assertEquals(1, discreteIon.getNodes().size());
            assertEquals(1, discreteIon.getSites().size());
            assertEquals(3, discreteIon.getAllPlaces().size());


//            BigraphFileModelManagement.exportAsInstanceModel(x, System.out);
            Placings.Identity1 id = purePlacings(exampleSignature).identity1();
            System.out.println("Discrete Ion:");
            BigraphFileModelManagement.Store.exportAsInstanceModel(discreteIon, System.out);
            BigraphComposite<DynamicSignature> compose = ops(ops(x).juxtapose(id).getOuterBigraph()).compose(discreteIon);
            assertEquals(0, compose.getOuterBigraph().getInnerNames().size());
            assertEquals(0, compose.getOuterBigraph().getOuterNames().size());
            System.out.println("Composition of Closure /x and Discrete Ion:");
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) compose.getOuterBigraph(), System.out);
//            BigraphComposite<DefaultDynamicSignature> compose = ops(x).compose(discreteIon);
//            factory.asBigraphOperator(x).compose(discreteIon)
            Bigraph<DynamicSignature> xyzWithPGIdentity = ops(id).parallelProduct(xyz).getOuterBigraph();
            System.out.println("xyzWithPGIdentity: ");
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) xyzWithPGIdentity, System.out);
//            Bigraph<DefaultDynamicSignature> nesting = factory.asBigraphOperator(xyzWithPGIdentity).nesting(discreteIon).getOuterBigraph();
//            BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) nesting, System.out);
//            assertEquals(2, nesting.getInnerNames().size());
//            assertEquals(1, nesting.getOuterNames().size());
//            assertEquals(1, nesting.getRoots().size());
//            assertEquals(1, nesting.getNodes().size());
//            assertEquals(1, nesting.getSites().size());
        });


        Linkings<DynamicSignature>.Closure x1 = linkings.closure(StringTypedName.of("x"));
        final Linkings<DynamicSignature>.Closure x2 = linkings.closure(StringTypedName.of("x"));

        BigraphComposite<DynamicSignature> x1Op = ops(x1);
        assertThrows(IncompatibleInterfaceException.class, () -> x1Op.juxtapose(x2));

        final Linkings<DynamicSignature>.Closure x3 = linkings.closure(StringTypedName.of("y"));
        assertAll(() -> {
            BigraphComposite<DynamicSignature> juxtapose = x1Op.juxtapose(x3);
            assertEquals(2, juxtapose.getOuterBigraph().getInnerNames().size());
            assertEquals(2, juxtapose.getOuterBigraph().getInnerFace().getValue().size());
        });


        assertAll(() -> {

            //a bigraph is composed with a closure resulting in a inner name rewriting of that bigraph
//            Signature<DefaultDynamicControl> signature = (Signature<DefaultDynamicControl>) exampleSignature;
            PureBigraphBuilder<DynamicSignature> builderForG = pureBuilder(exampleSignature);
            BigraphEntity.InnerName zInner = builderForG.createInner("z");
            builderForG.root().child(exampleSignature.getControlByName("User")).linkInner(zInner);
            PureBigraph simpleBigraph = builderForG.create();
            Linkings<DynamicSignature>.Substitution substitution = linkings.substitution(StringTypedName.of("z"), StringTypedName.of("y"));
            BigraphComposite<DynamicSignature> compose = ops(simpleBigraph);
            BigraphComposite<DynamicSignature> compose1 = compose.compose(substitution);

            assertFalse(compose1.getOuterBigraph().isGround());
            assertEquals(1, compose1.getOuterBigraph().getInnerNames().size());
            // name was overwritten
            assertEquals("y", compose1.getOuterBigraph().getInnerNames().iterator().next().getName());
            assertEquals(0, compose1.getOuterBigraph().getOuterNames().size());
        });


        assertAll(() -> {
            Linkings<DynamicSignature>.Substitution a1 = linkings.substitution(StringTypedName.of("a"), StringTypedName.of("b"), StringTypedName.of("c"));
            Linkings<DynamicSignature>.Substitution a2 = linkings.substitution(StringTypedName.of("x"), StringTypedName.of("a"));
            Bigraph<DynamicSignature> outerBigraph = ops(a2).compose(a1).getOuterBigraph();
            assertEquals(1, outerBigraph.getOuterNames().size());
            assertEquals(2, outerBigraph.getInnerNames().size());
            assertEquals(0, outerBigraph.getNodes().size());
            assertEquals(0, outerBigraph.getRoots().size());
            assertEquals(0, outerBigraph.getSites().size());
            assertFalse(outerBigraph.isGround());
        });
    }

    @Test
    void composition_test_02() throws IncompatibleSignatureException, IncompatibleInterfaceException, IOException {
//        TARGET_TEST_PATH
//        String metaModel = TARGET_TEST_MODEL_PATH + "composition_test_02.ecore";
//        String instanceModela = TARGET_TEST_MODEL_PATH + "composition_test_02a.xmi"; // context with identity links
//        String instanceModelb = TARGET_TEST_MODEL_PATH + "composition_test_02b.xmi"; // reactum image  (composition result)
//        String instanceModelc = TARGET_TEST_MODEL_PATH + "composition_test_02c.xmi"; // identity links
//        String instanceModeld = TARGET_TEST_MODEL_PATH + "composition_test_02d.xmi"; // bigraph
//        BigraphBaseModelPackageImpl.init();
//        PureBigraphBuilder<Signature> b1 = PureBigraphBuilder.create(createSingleControlSignature(), metaModel, instanceModela);
//        PureBigraph lhs = b1.createBigraph();
//        BigraphFileModelManagement.exportAsMetaModel(lhs, new FileOutputStream(TARGET_TEST_MODEL_PATH + "F.ecore"));
//
//        b1 = PureBigraphBuilder.create(createSingleControlSignature(), b1.getLoadedEPackage(), BigraphFileModelManagement.loadBigraphInstanceModel(instanceModelb).get(0));
//        PureBigraph rhs = b1.createBigraph();
//
//        b1 = PureBigraphBuilder.create(createSingleControlSignature(), b1.getLoadedEPackage(), BigraphFileModelManagement.loadBigraphInstanceModel(instanceModelc).get(0));
//        PureBigraph bc = b1.createBigraph();
//        b1 = PureBigraphBuilder.create(createSingleControlSignature(), b1.getLoadedEPackage(), BigraphFileModelManagement.loadBigraphInstanceModel(instanceModeld).get(0));
//        PureBigraph bd = b1.createBigraph();
//
//        BigraphFileModelManagement.exportAsInstanceModel(bc, System.out);
//        BigraphFileModelManagement.exportAsInstanceModel(bd, System.out);
//        PureBigraph reactumImage = factory.asBigraphOperator(bc).nesting(bd).getOuterBigraph();
//        BigraphFileModelManagement.exportAsInstanceModel(reactumImage, System.out);
//        assertEquals(1, reactumImage.getRoots().size());
//        assertEquals(2, reactumImage.getNodes().size());
//        assertEquals(3, reactumImage.getOuterNames().size());
//        assertEquals(0, reactumImage.getInnerNames().size());
//        assertEquals(2, reactumImage.getPointsFromLink(reactumImage.getOuterNames().stream().filter(x -> x.getName().equals("e0_innername")).findFirst().get()).size());
//        assertEquals(2, reactumImage.getPointsFromLink(reactumImage.getOuterNames().stream().filter(x -> x.getName().equals("y1")).findFirst().get()).size());
//        assertEquals(0, reactumImage.getPointsFromLink(reactumImage.getOuterNames().stream().filter(x -> x.getName().equals("y2")).findFirst().get()).size());
//
//        Bigraph<DefaultDynamicSignature> result = factory.asBigraphOperator(lhs).compose(rhs).getOuterBigraph();
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) result, System.out);
//        assertEquals(1, result.getRoots().size());
//        assertEquals(4, result.getNodes().size());
//        assertEquals(0, result.getSites().size());
//        assertEquals(3, result.getAllLinks().size());
//        assertEquals(1, result.getEdges().size());
//        assertEquals(2, result.getOuterNames().size());
//        assertEquals(3, result.getPointsFromLink(result.getOuterNames().stream().filter(x -> x.getName().equals("y1")).findFirst().get()).size());
//        assertEquals(3, result.getPointsFromLink(result.getEdges().stream().filter(x -> x.getName().equals("e0")).findFirst().get()).size());
//        assertEquals(2, result.getPointsFromLink(result.getOuterNames().stream().filter(x -> x.getName().equals("y2")).findFirst().get()).size());
    }

    public static DynamicSignature createSingleControlSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier("A").arity(2).assign()
        ;
        return defaultBuilder.create();
    }

    private static EPackage getExampleMetaModel() {
        return createOrGetBigraphMetaModel(createExampleSignature());
    }

    private static DynamicSignature createExampleSignature() {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder
                .add("Printer", 2)
                .add("User", 1)
                .add("Room", 1)
                .add("Spool", 1)
                .add("Computer", 1)
                .add("Job", 0);

        return signatureBuilder.create();
    }
}
