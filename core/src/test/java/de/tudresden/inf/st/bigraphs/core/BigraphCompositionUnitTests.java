package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
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
import de.tudresden.inf.st.bigraphs.core.impl.elementary.DiscreteIon;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import de.tudresden.inf.st.bigraphs.core.impl.EcoreBigraph;
import de.tudresden.inf.st.bigraphs.core.utils.BigraphUtil;
import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.ops;
import static org.junit.jupiter.api.Assertions.*;

public class BigraphCompositionUnitTests {

    private final static String TARGET_TEST_PATH = "src/test/resources/dump/exported-models/";
    private final static String TARGET_TEST_MODEL_PATH = "src/test/resources/ecore-test-models/";
    private static PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();

    @BeforeEach
    void setUp() {
    }

    @Test
    void bigraphNesting_test_withoutLinks() throws InvalidConnectionException, TypeNotExistsException, IOException, IncompatibleSignatureException, IncompatibleInterfaceException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createExampleSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(createExampleSignature());
        PureBigraph bigraph = builder.createRoot()
                .addChild("Room").down().addChild("User")
                .addChild("Computer")
                .addSite()
                .top()
                .createBigraph();
        PureBigraph userBigraph = builder2.createRoot()
                .addChild("User")
                .top()
                .createBigraph();
        BigraphComposite<DefaultDynamicSignature> nesting = ops(bigraph).nesting(userBigraph);
        BigraphArtifacts.exportAsInstanceModel(nesting.getOuterBigraph(), System.out);
    }

    @Test
    void bigraphNesting_test() throws InvalidConnectionException, TypeNotExistsException, IOException, IncompatibleSignatureException, IncompatibleInterfaceException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createExampleSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(createExampleSignature());
        BigraphEntity.OuterName jeff = builder.createOuterName("jeff");
        PureBigraph bigraph = builder.createRoot()
                .addChild("Room").down().addChild("User", "jeff")
                .addChild("Computer").linkToOuter(jeff)
                .addSite()
                .top()
                .createBigraph();
        PureBigraph userBigraph = builder2.createRoot()
                .addChild("User", "jeff")
                .top()
                .createBigraph();
        System.out.println("-----------------------------------------");
        BigraphArtifacts.exportAsInstanceModel(bigraph, System.out);
        System.out.println("-----------------------------------------");
        BigraphArtifacts.exportAsInstanceModel(userBigraph, System.out);
        PureBigraph nesting = ops(bigraph).nesting(userBigraph).getOuterBigraph();
//        System.out.println("-----------------------------------------");
        BigraphArtifacts.exportAsInstanceModel(nesting, System.out);
        assertEquals(1, nesting.getRoots().size());
        assertEquals(4, nesting.getNodes().size());
        assertEquals(0, nesting.getSites().size());
        assertEquals(1, nesting.getOuterNames().size());
        assertEquals("jeff", nesting.getOuterNames().get(0).getName());
        assertEquals(3, nesting.getPointsFromLink(nesting.getOuterNames().stream().filter(x -> x.getName().equals("jeff")).findFirst().get()).size());
    }

    @Test
    @DisplayName("Compose two bigraphs, which are the same instance")
    void composition_of_sameBigraphInstance() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException, LinkTypeNotExistsException, IOException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createExampleSignature());
        BigraphEntity.InnerName jeff = builder.createInnerName("jeff");
        PureBigraph bigraph = builder.createRoot()
                .addChild("Room").down().addChild("User", "jeff")
                .addChild("Computer").linkToInner(jeff)
                .addSite()
                .createBigraph();

        BigraphComposite<DefaultDynamicSignature> comp = ops(bigraph).parallelProduct(bigraph);
        Bigraph<DefaultDynamicSignature> outerBigraph = comp.getOuterBigraph();
//        BigraphArtifacts.exportAsInstanceModel((PureBigraph) outerBigraph, new FileOutputStream(TARGET_TEST_PATH + "same-instance.xmi"));

        Bigraph<DefaultDynamicSignature> comp2 = ops(bigraph).compose(bigraph).getOuterBigraph();
//        BigraphArtifacts.exportAsInstanceModel((EcoreBigraph) comp2, new FileOutputStream(TARGET_TEST_PATH + "same-instance2.xmi"));
    }

    @Test
    @DisplayName("Compose two bigraphs and check their EMetaModel data")
    void compose_test_with_emetamodel_check() throws IncompatibleSignatureException, IncompatibleInterfaceException {
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        EMetaModelData metaData = EMetaModelData.builder().setName("model-check").setNsPrefix("model").setNsUri("org.example.check").create();
        EMetaModelData metaData2 = EMetaModelData.builder().setName("model-check2").setNsPrefix("model2").setNsUri("org.example.check2").create();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature, metaData);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature, metaData2);

        PureBigraph building = builder.createRoot().addChild("Room").addChild("Room").down().addSite().createBigraph();
        PureBigraph user = builder2.createRoot().addChild("User").createBigraph();

        BigraphComposite<DefaultDynamicSignature> composed = ops(building).compose(user);
        BigraphComposite<DefaultDynamicSignature> composed2 = ops(building).juxtapose(user);
        assertNotNull(composed);
        assertNotNull(composed2);

        assertEquals(metaData.getName(), ((PureBigraph) composed.getOuterBigraph()).getModelPackage().getName());
        assertEquals(metaData.getNsPrefix(), ((PureBigraph) composed.getOuterBigraph()).getModelPackage().getNsPrefix());
        assertEquals(metaData.getNsUri(), ((PureBigraph) composed.getOuterBigraph()).getModelPackage().getNsURI());
    }

    @Test
    void compose_test_0() throws InvalidConnectionException, TypeNotExistsException, IncompatibleSignatureException, IncompatibleInterfaceException {
        PureBigraphBuilder<DefaultDynamicSignature> builderReactum = factory.createBigraphBuilder(createSignature_compose_test_0());
        BigraphEntity.OuterName fromD2 = builderReactum.createOuterName("fromD");
        BigraphEntity.OuterName fromS2 = builderReactum.createOuterName("fromS");
        BigraphEntity.OuterName target2 = builderReactum.createOuterName("target");
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy car2 = builderReactum.hierarchy("Car").linkToOuter(target2).addSite();
        builderReactum.createRoot()
                .addChild("Place").linkToOuter(fromD2).down().addSite().addChild(car2).top()
                .addChild("Place", "fromS").down().addChild("Road").linkToOuter(fromD2).addSite().top()
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
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builderForF = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builderForG = factory.createBigraphBuilder(signature);

        BigraphEntity.OuterName jeff = builderForF.createOuterName("jeff");
        BigraphEntity.InnerName jeffG = builderForG.createInnerName("jeff");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room =
                builderForF.hierarchy(signature.getControlByName("Room"));
        room.addChild(signature.getControlByName("User")).linkToOuter(jeff).addChild(signature.getControlByName("Job"));
        builderForF.createRoot()
                .addChild(room);

        builderForG.createRoot()
                .addChild(signature.getControlByName("Job")).down().addSite().up()
                .addChild(signature.getControlByName("User")).linkToInner(jeffG);


        PureBigraph F = builderForF.createBigraph();
        PureBigraph G = builderForG.createBigraph();

        EPackage modelPackage = F.getModelPackage();


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
        Signature<DefaultDynamicControl> signature = createExampleSignature();
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
            builderForF.createRoot().addChild(signature.getControlByName("Room")).down()
                    .addChild(signature.getControlByName("Computer")).linkToOuter(networkF);//.connectNodeToInnerName(ethernetF);
            builderForG.createRoot().addChild(signature.getControlByName("Room")).down()
                    .addChild(signature.getControlByName("Computer")).linkToOuter(networkG);//.connectNodeToInnerName(ethernetG);

//            builderForF.connectInnerToOuterName(ethernetF, networkF);
//            builderForG.connectInnerToOuterName(ethernetG, networkG);
        } else {
            builderForF.createRoot().addChild(signature.getControlByName("Room")).down()
                    .addChild(signature.getControlByName("Computer")).connectInnerNamesToNode(ethernetF, ethernetF2);
            builderForG.createRoot().addChild(signature.getControlByName("Room")).down()
                    .addChild(signature.getControlByName("Computer")).linkToInner(ethernetG);
            builderForH.createRoot().addChild(signature.getControlByName("Room")).down()
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
        BigraphArtifacts.exportAsInstanceModel((EcoreBigraph) result,
                new FileOutputStream(TARGET_TEST_PATH + "result.xmi"));
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
            Bigraph<DefaultDynamicSignature> outerBigraph = b.compose((Bigraph<DefaultDynamicSignature>) a.juxtapose(merge_M).getOuterBigraph()).getOuterBigraph();
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
        Linkings<DefaultDynamicSignature> linkings = factory.createLinkings(createExampleSignature());
        Placings<DefaultDynamicSignature> placings = factory.createPlacings(createExampleSignature());

        assertAll(() -> {
            Linkings<DefaultDynamicSignature>.Identity a = linkings.identity(StringTypedName.of("a"));
            PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createExampleSignature());
            builder.createOuterName("a");
            PureBigraph bigraph = builder.createBigraph();

            Bigraph<DefaultDynamicSignature> compose = factory.asBigraphOperator(a).compose(bigraph).getOuterBigraph();
            assertEquals(1, compose.getOuterNames().size());
            assertEquals(0, compose.getInnerNames().size());

            assertThrows(IncompatibleInterfaceException.class, () -> {
                factory.asBigraphOperator(bigraph).compose(a).getOuterBigraph();
            });

            PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(createExampleSignature());
            builder2.connectInnerToOuterName(builder2.createInnerName("a"), builder2.createOuterName("a"));
            PureBigraph bigraph2 = builder2.createBigraph();
            Bigraph<DefaultDynamicSignature> compose2 = factory.asBigraphOperator(a).compose(bigraph2).getOuterBigraph();
            assertEquals(1, compose2.getOuterNames().size());
            assertEquals(1, compose2.getInnerNames().size());
            assertEquals("a", compose2.getLinkOfPoint(compose2.getInnerNames().iterator().next()).getName());
        });

        assertAll(() -> {
            Linkings<DefaultDynamicSignature>.Substitution s1 = linkings.substitution(StringTypedName.of("y"), StringTypedName.of("y"));
            Linkings<DefaultDynamicSignature>.Closure c1 = linkings.closure(StringTypedName.of("x"));
            Linkings<DefaultDynamicSignature>.Closure c2 = linkings.closure(StringTypedName.of("a"));
            Linkings<DefaultDynamicSignature>.Closure y = linkings.closure(StringTypedName.of("y"));
            DiscreteIon<DefaultDynamicSignature> ionC = factory.createDiscreteIon("Printer", new HashSet<>(Arrays.asList("x", "y")), createExampleSignature());

            // id(2) * (/x || /y): <2,{}> and <0,{}> doesn't works
            assertThrows(IncompatibleInterfaceException.class, () -> {
                Placings<DefaultDynamicSignature>.Permutation permutation = placings.permutation(2);
                ops(permutation).compose(ops(c1).parallelProduct(y)).getOuterBigraph();
            });

            ///x * User{x}
            DiscreteIon<DefaultDynamicSignature> discreteIon = factory.createDiscreteIon("User", new HashSet<>(Arrays.asList("x")), createExampleSignature());
            Bigraph<DefaultDynamicSignature> result0 = ops(c1).compose(discreteIon).getOuterBigraph();
            BigraphArtifacts.exportAsInstanceModel((EcoreBigraph) result0, System.out);
            assertEquals(0, result0.getOuterNames().size());
            assertEquals(0, result0.getInnerNames().size());
            assertEquals(3, result0.getAllPlaces().size());
            assertEquals(1, result0.getRoots().size());
            assertEquals(1, result0.getNodes().size());
            assertEquals(1, result0.getSites().size());

            // parallel product between linkings : shall not create root
            Bigraph<DefaultDynamicSignature> composed1 = ops(c1).parallelProduct(s1).getOuterBigraph();
            BigraphArtifacts.exportAsInstanceModel((EcoreBigraph) composed1, System.out);
            assertEquals(1, composed1.getOuterNames().size());
            assertEquals(2, composed1.getInnerNames().size());
            assertEquals(0, composed1.getAllPlaces().size());
            assertEquals(0, composed1.getRoots().size());
            assertTrue(BigraphUtil.isBigraphElementaryLinking(composed1));

            // parallel product between linkings: shall not create root
            Bigraph<DefaultDynamicSignature> composed2 = ops(c1).parallelProductOf(c1, s1).getOuterBigraph();
            BigraphArtifacts.exportAsInstanceModel((EcoreBigraph) composed2, System.out);
            assertEquals(1, composed2.getOuterNames().size());
            assertEquals(2, composed2.getInnerNames().size());
            assertEquals(0, composed2.getAllPlaces().size());
            assertEquals(0, composed2.getRoots().size());
            assertTrue(BigraphUtil.isBigraphElementaryLinking(composed2));

            // merge product between linkings
            Bigraph<DefaultDynamicSignature> merged = ops(c1).merge(s1).getOuterBigraph();
            BigraphArtifacts.exportAsInstanceModel((EcoreBigraph) merged, System.out);
            assertEquals(1, merged.getOuterNames().size());
            assertEquals(2, merged.getInnerNames().size());
            assertEquals(1, merged.getAllPlaces().size());
            assertEquals(1, merged.getRoots().size());

            // mutliple merges
            Bigraph<DefaultDynamicSignature> merged2 = ops(c1).merge(c1).merge(c1).getOuterBigraph();
            BigraphArtifacts.exportAsInstanceModel((EcoreBigraph) merged2, System.out);
            assertEquals(0, merged2.getOuterNames().size());
            assertEquals(1, merged2.getInnerNames().size());
            assertEquals(1, merged2.getAllPlaces().size());
            assertEquals(1, merged2.getRoots().size());

            // merge product of two different linkings
            Bigraph<DefaultDynamicSignature> merged3 = ops(c1).merge(c1).merge(c2).getOuterBigraph();
            BigraphArtifacts.exportAsInstanceModel((EcoreBigraph) merged3, System.out);
            assertEquals(0, merged3.getOuterNames().size());
            assertEquals(2, merged3.getInnerNames().size());
            assertEquals(1, merged3.getAllPlaces().size());
            assertEquals(1, merged3.getRoots().size());

            // (/x || (/x | /a))
            Bigraph<DefaultDynamicSignature> result = ops(c1).parallelProduct(ops(c1).merge(c2)).getOuterBigraph();
            BigraphArtifacts.exportAsInstanceModel((EcoreBigraph) result, System.out);
            assertEquals(0, result.getOuterNames().size());
            assertEquals(2, result.getInnerNames().size());
            assertEquals(1, result.getAllPlaces().size());
            assertEquals(1, result.getRoots().size());
            assertEquals(0, result.getSites().size());


            // 1 * (/x || /y): <0,{}> and <0,{}> works
            Placings<DefaultDynamicSignature>.Barren barren = placings.barren();
            Bigraph<DefaultDynamicSignature> result2 = ops(barren).compose(ops(c1).parallelProduct(y)).getOuterBigraph();
            BigraphArtifacts.exportAsInstanceModel((EcoreBigraph) result2, System.out);
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
            Placings<DefaultDynamicSignature>.Identity1 id_1 = placings.identity1();
            Linkings<DefaultDynamicSignature>.Identity id_x = linkings.identity(StringTypedName.of("x"), StringTypedName.of("x"));
            Linkings<DefaultDynamicSignature>.Substitution sigma_yx = linkings.substitution(StringTypedName.of("x"), StringTypedName.of("y"), StringTypedName.of("x"));
            Bigraph<DefaultDynamicSignature> outerBigraph = ops(ops(id_x).compose(sigma_yx).getOuterBigraph()).parallelProduct(id_1).getOuterBigraph();
            assertEquals(1, outerBigraph.getOuterNames().size());
            assertEquals(2, outerBigraph.getInnerNames().size());
            assertEquals(2, outerBigraph.getAllPlaces().size());
            assertEquals(1, outerBigraph.getRoots().size());
            assertEquals(0, outerBigraph.getNodes().size());
            assertEquals(1, outerBigraph.getSites().size());
            Bigraph<DefaultDynamicSignature> outerBigraph1 = ops(outerBigraph).compose(ionC).getOuterBigraph();
            BigraphArtifacts.exportAsInstanceModel((EcoreBigraph) outerBigraph1, System.out);
            assertEquals(1, outerBigraph1.getOuterNames().size());
            assertEquals(0, outerBigraph1.getEdges().size());
            assertEquals(0, outerBigraph1.getInnerNames().size());
            assertEquals(3, outerBigraph1.getAllPlaces().size());
            assertEquals(1, outerBigraph1.getRoots().size());
            assertEquals(1, outerBigraph1.getNodes().size());
            assertEquals(1, outerBigraph1.getSites().size());

            //(id(1) || /x || y/{y}) * C{x,y}: closes one name 'x'
            Linkings<DefaultDynamicSignature>.Substitution sigma_y = linkings.substitution(StringTypedName.of("y"), StringTypedName.of("y"));
            Bigraph<DefaultDynamicSignature> outerBigraph2 = ops(placings.identity1()).parallelProduct(c1).parallelProduct(sigma_y).getOuterBigraph();
            Bigraph<DefaultDynamicSignature> composed3 = ops(outerBigraph2).compose(ionC).getOuterBigraph();
            BigraphArtifacts.exportAsInstanceModel((EcoreBigraph) composed3, System.out);
            assertEquals(1, composed3.getOuterNames().size());
            assertEquals("y", composed3.getOuterNames().iterator().next().getName());
            assertEquals(0, composed3.getEdges().size());
            assertEquals(0, composed3.getInnerNames().size());
            assertEquals(3, composed3.getAllPlaces().size());
            assertEquals(1, composed3.getRoots().size());
            assertEquals(1, composed3.getNodes().size());
            assertEquals(1, composed3.getSites().size());
        });
    }

    @Test
    void elementary_bigraph_linkings_test() {
        Linkings<DefaultDynamicSignature> linkings = factory.createLinkings();

        assertAll(() -> {
            Linkings<DefaultDynamicSignature>.Closure xyz = linkings.closure(
                    Sets.mutable.of(StringTypedName.of("a"), StringTypedName.of("x"), StringTypedName.of("y"))
            );
            BigraphArtifacts.exportAsInstanceModel(xyz, System.out);
            assertEquals(3, xyz.getInnerNames().size());
            assertEquals(0, xyz.getOuterNames().size());

            Linkings<DefaultDynamicSignature>.Closure x = linkings.closure(StringTypedName.of("x"));
            DiscreteIon<DefaultDynamicSignature> discreteIon = factory.createDiscreteIon(
                    StringTypedName.of("User"),
                    Collections.singleton(StringTypedName.of("x")),
                    createExampleSignature()
            );
            assertEquals(1, discreteIon.getRoots().size());
            assertEquals(1, discreteIon.getNodes().size());
            assertEquals(1, discreteIon.getSites().size());
            assertEquals(3, discreteIon.getAllPlaces().size());


//            BigraphArtifacts.exportAsInstanceModel(x, System.out);
            BigraphArtifacts.exportAsInstanceModel(discreteIon, System.out);
            BigraphComposite<DefaultDynamicSignature> compose = factory.asBigraphOperator(x).compose(discreteIon);
            assertEquals(0, compose.getOuterBigraph().getInnerNames().size());
            assertEquals(0, compose.getOuterBigraph().getOuterNames().size());
            BigraphArtifacts.exportAsInstanceModel(compose.getOuterBigraph(), System.out);
//            BigraphComposite<DefaultDynamicSignature> compose = ops(x).compose(discreteIon);
//            factory.asBigraphOperator(x).compose(discreteIon)
            Bigraph<DefaultDynamicSignature> xyzWithPGIdentity = factory.asBigraphOperator(factory.createPlacings(discreteIon.getSignature()).identity1()).parallelProduct(xyz).getOuterBigraph();
            BigraphArtifacts.exportAsInstanceModel((EcoreBigraph) xyzWithPGIdentity, System.out);
            BigraphComposite<DefaultDynamicSignature> nesting = factory.asBigraphOperator(xyzWithPGIdentity).nesting(discreteIon);
            BigraphArtifacts.exportAsInstanceModel(nesting.getOuterBigraph(), System.out);
            assertEquals(2, nesting.getOuterBigraph().getInnerNames().size());
            assertEquals(1, nesting.getOuterBigraph().getOuterNames().size());
            assertEquals(1, nesting.getOuterBigraph().getRoots().size());
            assertEquals(1, nesting.getOuterBigraph().getNodes().size());
            assertEquals(1, nesting.getOuterBigraph().getSites().size());
        });


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
            Signature<DefaultDynamicControl> signature = createExampleSignature();
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
        String metaModel = TARGET_TEST_MODEL_PATH + "composition_test_02.ecore";
        String instanceModela = TARGET_TEST_MODEL_PATH + "composition_test_02a.xmi"; // context with identity links
        String instanceModelb = TARGET_TEST_MODEL_PATH + "composition_test_02b.xmi"; // reactum image  (composition result)
        String instanceModelc = TARGET_TEST_MODEL_PATH + "composition_test_02c.xmi"; // identity links
        String instanceModeld = TARGET_TEST_MODEL_PATH + "composition_test_02d.xmi"; // bigraph

        PureBigraphBuilder<Signature> b1 = PureBigraphBuilder.create(createSingleControlSignature(), metaModel, instanceModela);
        PureBigraph lhs = b1.createBigraph();

        b1 = PureBigraphBuilder.create(createSingleControlSignature(), metaModel, instanceModelb);
        PureBigraph rhs = b1.createBigraph();

        b1 = PureBigraphBuilder.create(createSingleControlSignature(), metaModel, instanceModelc);
        PureBigraph bc = b1.createBigraph();
        b1 = PureBigraphBuilder.create(createSingleControlSignature(), metaModel, instanceModeld);
        PureBigraph bd = b1.createBigraph();

        BigraphArtifacts.exportAsInstanceModel(bc, System.out);
        BigraphArtifacts.exportAsInstanceModel(bd, System.out);
        PureBigraph reactumImage = factory.asBigraphOperator(bc).nesting(bd).getOuterBigraph();
        BigraphArtifacts.exportAsInstanceModel(reactumImage, System.out);
        assertEquals(1, reactumImage.getRoots().size());
        assertEquals(2, reactumImage.getNodes().size());
        assertEquals(3, reactumImage.getOuterNames().size());
        assertEquals(0, reactumImage.getInnerNames().size());
        assertEquals(2, reactumImage.getPointsFromLink(reactumImage.getOuterNames().stream().filter(x -> x.getName().equals("e0_innername")).findFirst().get()).size());
        assertEquals(2, reactumImage.getPointsFromLink(reactumImage.getOuterNames().stream().filter(x -> x.getName().equals("y1")).findFirst().get()).size());
        assertEquals(0, reactumImage.getPointsFromLink(reactumImage.getOuterNames().stream().filter(x -> x.getName().equals("y2")).findFirst().get()).size());

        Bigraph<DefaultDynamicSignature> result = factory.asBigraphOperator(lhs).compose(rhs).getOuterBigraph();
        BigraphArtifacts.exportAsInstanceModel((EcoreBigraph) result, System.out);
        assertEquals(1, result.getRoots().size());
        assertEquals(4, result.getNodes().size());
        assertEquals(0, result.getSites().size());
        assertEquals(3, result.getAllLinks().size());
        assertEquals(1, result.getEdges().size());
        assertEquals(2, result.getOuterNames().size());
        assertEquals(3, result.getPointsFromLink(result.getOuterNames().stream().filter(x -> x.getName().equals("y1")).findFirst().get()).size());
        assertEquals(3, result.getPointsFromLink(result.getEdges().stream().filter(x -> x.getName().equals("e0")).findFirst().get()).size());
        assertEquals(2, result.getPointsFromLink(result.getOuterNames().stream().filter(x -> x.getName().equals("y2")).findFirst().get()).size());
    }

    public static <C extends Control<?, ?>, S extends Signature<C>> S createSingleControlSignature() {
        DynamicSignatureBuilder defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier("A").arity(2).assign()
        ;
        return (S) defaultBuilder.create();
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
