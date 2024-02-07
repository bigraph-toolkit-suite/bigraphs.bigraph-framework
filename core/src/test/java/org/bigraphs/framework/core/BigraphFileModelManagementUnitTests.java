package org.bigraphs.framework.core;

import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.List;
import java.util.Optional;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BigraphFileModelManagementUnitTests {

    private final static String TARGET_TEST_PATH = "src/test/resources/dump/exported-models/";
    private final static String TARGET_TEST_EXPORT_PATH = "src/test/resources/ecore-test-models/";

    @BeforeAll
    static void setUp() {
        File file = new File(TARGET_TEST_PATH);
        file.mkdirs();
        file = new File(TARGET_TEST_EXPORT_PATH);
        file.mkdirs();
    }

    @Test
    void checkConstraintsAfterExport() throws IOException {
        DefaultDynamicSignature signature = createExampleSignature();
//        PureBigraphGenerator gen = pureRandomBuilder(signature);
//        PureBigraph generate = gen.generate(1, 10, 1f);
//        BigraphFileModelManagement.exportAsInstanceModel(generate, new FileOutputStream(TARGET_TEST_PATH + "_r1.xmi"));
//        BigraphFileModelManagement.exportAsMetaModel(generate, new FileOutputStream(TARGET_TEST_PATH + "_r1.ecore"));
//        EPackage ePackage = BigraphFileModelManagement.loadBigraphMetaModel(TARGET_TEST_PATH + "_r1.ecore");
//        List<EObject> eObjects = BigraphFileModelManagement.loadBigraphInstanceModel(ePackage, TARGET_TEST_PATH + "_r1.xmi");
    }

    @Test
    void load_and_export_Model_Test() throws IOException {
        String baseName = "test-case-X";
        String instanceFilename = TARGET_TEST_PATH + baseName + ".xmi";
        String metaFilename = TARGET_TEST_PATH + baseName + ".ecore";
        String instanceFilenameSig = TARGET_TEST_PATH + baseName + "Sig.xmi";
        String metaFilenameSig = TARGET_TEST_PATH + baseName + "Sig.ecore";
        DefaultDynamicSignature sig = pureSignatureBuilder()
                .addControl("A", 1)
                .addControl("B", 2)
                .addControl("C", 3)
                .create();
//        createOrGetSignatureMetaModel(sig, EMetaModelData.builder()
//                .setName("sigsig")
//                .create());
        System.out.println(sig.getMetaModel().getName());

//        createOrGetBigraphMetaModel(sig, EMetaModelData.builder().setName("bigbig").create());
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig);
        builder.createRoot()
                .addChild("A")
                .addChild("B")
                .addChild("C")
        ;

        BigraphFileModelManagement.Store.exportAsMetaModel(sig, new FileOutputStream(metaFilenameSig));
        EPackage ePackageSigLoaded = BigraphFileModelManagement.Load.signatureMetaModel(metaFilenameSig);
        assertNotNull(ePackageSigLoaded);
        BigraphFileModelManagement.Store.exportAsInstanceModel(sig, new FileOutputStream(instanceFilenameSig));
        assertThrows(Resource.IOWrappedException.class, () -> {
            System.out.println("Class 'A' is not found or is abstract");
            BigraphFileModelManagement.Load.signatureInstanceModel(instanceFilenameSig);
        });
        List<EObject> eObjectsSigInstanceLoaded = BigraphFileModelManagement.Load.signatureInstanceModel(metaFilenameSig, instanceFilenameSig);
        assertNotNull(eObjectsSigInstanceLoaded);
        assertEquals(1, eObjectsSigInstanceLoaded.size());
        DefaultDynamicSignature sigLoaded = createOrGetSignature(eObjectsSigInstanceLoaded.get(0));
        assertEquals(sigLoaded, sig);

        PureBigraph bigraph = builder.createBigraph();
        System.out.println(bigraph.getMetaModel().getNsURI());
        System.out.println(bigraph.getMetaModel().getNsPrefix());
        BigraphFileModelManagement.Store.exportAsMetaModel(bigraph, new FileOutputStream(metaFilename));
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, new FileOutputStream(instanceFilename), baseName + ".ecore");
//
//        //BigraphModelManagement
        List<EObject> eObjects = BigraphFileModelManagement.Load.bigraphInstanceModel(instanceFilename);
        assertEquals(1, eObjects.size());
        System.out.println(eObjects.get(0));
        System.out.println(bigraph.getInstanceModel());
//
        List<EObject> eObjectsReturn = BigraphFileModelManagement.Load.bigraphInstanceModel(metaFilename, instanceFilename);
        assertEquals(1, eObjectsReturn.size());
        System.out.println(eObjectsReturn.get(0));
        System.out.println(eObjectsReturn.get(0).eClass().getEPackage().getNsURI());
        System.out.println(eObjectsReturn.get(0).eClass().getEPackage().getNsPrefix());
    }

    @Test
    void load_signatureBaseModel_test() {
        Assertions.assertAll(() -> {
            EPackage ePackage = BigraphFileModelManagement.Load.internalSignatureMetaMetaModel();
            assertNotNull(ePackage);
            assertEquals(BigraphMetaModelConstants.SignaturePackage.EPACKAGE_NAME, ePackage.getName());
            Optional<EClassifier> bSignature = ePackage.getEClassifiers().stream().filter(x -> x.getName().equals(BigraphMetaModelConstants.SignaturePackage.ECLASS_BDYNAMICSIGNATURE)).findFirst();
            assertTrue(bSignature.isPresent());
            assertNotNull(bSignature.get());
        });
    }

    @Test
    void ecore_bigraph_stub_test() {
        assertAll(() -> {
            PureBigraph bigraph = (PureBigraph) createSampleBigraph();

            EcoreBigraph.Stub stub = new EcoreBigraph.Stub(bigraph);
            assertEquals(stub.getInstanceModel(), bigraph.getInstanceModel());
            assertEquals(stub.getMetaModel(), bigraph.getMetaModel());

            EcoreBigraph.Stub clone = stub.clone();
            assertEquals(stub.getMetaModel(), clone.getMetaModel());
            assertNotEquals(stub, clone);
            assertNotEquals(stub.getInstanceModel(), clone.getInstanceModel());

            InputStream inputStreamOfInstanceModel = stub.getInputStreamOfInstanceModel();
            assertNotNull(inputStreamOfInstanceModel);
            inputStreamOfInstanceModel.close();
        });

    }

    @Test
    @DisplayName("Write the meta-model and instance model of a bigraph")
    void write_model_test() {
        assertAll(() -> {
            PureBigraph bigraph = (PureBigraph) createSampleBigraph();

            BigraphFileModelManagement.Store.exportAsMetaModel(bigraph,
                    new FileOutputStream(TARGET_TEST_PATH + "test_meta-model.ecore"));
            BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph,
                    new FileOutputStream(TARGET_TEST_PATH + "test_instance-model.xmi"));
        });
    }

    @Test
    @DisplayName("Load an external model and build a bigraph instance")
    void load_external_model_test() {
        assertAll(() -> {
//            String fileName = "/home/dominik/git/BigraphFramework/core/src/test/resources/ecore-test-models/test-1.ecore";
//            String fileName2 = "/home/dominik/git/BigraphFramework/core/src/test/resources/ecore-test-models/test-1.xmi";
//            EPackage metaModel = BigraphFileModelManagement.Load.bigraphMetaModel(fileName);
//
//            List<EObject> eObjects = BigraphFileModelManagement.loadBigraphInstanceModel(metaModel, fileName2);
//            assertEquals(1, eObjects.size());
//            List<EObject> eObjects2 = BigraphFileModelManagement.loadBigraphInstanceModel(fileName2);
//            assertEquals(1, eObjects2.size());
//
//            Signature<DefaultDynamicControl> signature = createExampleSignature();
//            PureBigraphBuilder builder = PureBigraphBuilder.create(signature, fileName, fileName2);
//            System.out.println(builder);
//            PureBigraph bigraph = builder.createBigraph();
//            BigraphFileModelManagement.exportAsInstanceModel(bigraph, new FileOutputStream(TARGET_TEST_PATH + "test-1_reloaded.xmi"));
//
//            PureBigraphBuilder<Signature> signaturePureBigraphBuilder = PureBigraphBuilder.create(createExampleSignature(), fileName, fileName2);
//            PureBigraph reloaded = signaturePureBigraphBuilder.createBigraph();
//            System.out.println(reloaded);
//            Collection<BigraphEntity.NodeEntity<DefaultDynamicControl>> nodes = reloaded.getNodes();
        });
    }

    @Test
    void compose_output() {
        assertAll(() -> {

            DefaultDynamicSignature signature = createExampleSignature();
            EMetaModelData modelData = EMetaModelData.builder().setName("F").setNsUri("http://www.example.org").setNsPrefix("sample").create();
            createOrGetBigraphMetaModel(signature, modelData);
            PureBigraphBuilder<DefaultDynamicSignature> builderForF = pureBuilder(signature);
            PureBigraphBuilder<DefaultDynamicSignature> builderForG = pureBuilder(signature);
//
            BigraphEntity.OuterName jeff = builderForF.createOuterName("jeff");
            BigraphEntity.InnerName jeffG = builderForG.createInnerName("jeff");
            BigraphEntity.InnerName f1 = builderForF.createInnerName("x_f");
            BigraphEntity.InnerName f2 = builderForF.createInnerName("y_f");

            PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room =
                    builderForF.hierarchy(signature.getControlByName("Room"));
            room.addChild(signature.getControlByName("User")).linkToOuter(jeff).addChild(signature.getControlByName("Job"))
                    .addChild(signature.getControlByName("Printer")).linkToInner(f1).linkToInner(f2)
            ;
            builderForF.createRoot()
                    .addChild(room);

            builderForG.createRoot()
                    .addChild(signature.getControlByName("Job")).down().addSite().up()
                    .addChild(signature.getControlByName("User")).linkToInner(jeffG);


            builderForF.createBigraph();
            PureBigraph F = builderForF.createBigraph();
            PureBigraph G = builderForG.createBigraph();

            BigraphFileModelManagement.Store.exportAsInstanceModel(F, new FileOutputStream(TARGET_TEST_PATH + "f.xmi"));
            BigraphComposite<DefaultDynamicSignature> compositor = ops(G);
            BigraphComposite<DefaultDynamicSignature> composedBigraph = compositor.compose(F);
            BigraphFileModelManagement.Store.exportAsInstanceModel((PureBigraph) composedBigraph.getOuterBigraph(),
                    new FileOutputStream(TARGET_TEST_PATH + "composetest.xmi"));
            BigraphFileModelManagement.Store.exportAsMetaModel((PureBigraph) composedBigraph.getOuterBigraph(),
                    new FileOutputStream(TARGET_TEST_PATH + "composetest.ecore"));

            BigraphComposite<DefaultDynamicSignature> juxtapose = compositor.juxtapose(F);
            BigraphFileModelManagement.Store.exportAsInstanceModel((PureBigraph) juxtapose.getOuterBigraph(),
                    new FileOutputStream(TARGET_TEST_PATH + "juxtatest.xmi"));

        });
    }

    // change nsURI in to "http:///ecore_file_name.ecore" and nsPrefix into "ecore_file_name" after
    @Test
    void export_sample_model() throws IOException, InvalidConnectionException, TypeNotExistsException {
        DefaultDynamicSignature signature = createExampleSignature();
        EMetaModelData modelData = EMetaModelData.builder().setName("F").setNsUri("http://www.example.org").setNsPrefix("sample").create();
        createOrGetBigraphMetaModel(signature, modelData);
        PureBigraphBuilder<DefaultDynamicSignature> builderForF = pureBuilder(signature);
        BigraphEntity.OuterName jeff = builderForF.createOuterName("jeff");
        BigraphEntity.InnerName f1 = builderForF.createInnerName("x_f");
        BigraphEntity.InnerName f2 = builderForF.createInnerName("y_f");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room =
                builderForF.hierarchy(signature.getControlByName("Room"));
        room.addChild(signature.getControlByName("User")).linkToOuter(jeff).addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("Printer")).linkToInner(f1).linkToInner(f2).down().addSite()
                .up()
                .addSite()
        ;
        builderForF.createRoot()
                .addChild(room);

        PureBigraph F = builderForF.createBigraph();

        BigraphFileModelManagement.Store.exportAsInstanceModel(F,
                new FileOutputStream(TARGET_TEST_EXPORT_PATH + "test-1.xmi"), "test-1.ecore");
        BigraphFileModelManagement.Store.exportAsMetaModel(F,
                new FileOutputStream(TARGET_TEST_EXPORT_PATH + "test-1.ecore"));
    }

    @Test
    void compose_output_elementary_composition() {
        int m = 3;
        DefaultDynamicSignature signature = createExampleSignature();
//        DefaultDynamicSignature empty = pureSignatureBuilder().createEmpty();
        createOrGetBigraphMetaModel(signature);
        Placings<DefaultDynamicSignature> placings = purePlacings(signature);
        Linkings<DefaultDynamicSignature> linkings = pureLinkings(signature);
        Placings<DefaultDynamicSignature>.Merge merge_MplusOne = placings.merge(m + 1);

        Placings<DefaultDynamicSignature>.Join aJoin = placings.join();
        Placings<DefaultDynamicSignature>.Merge merge_1 = placings.merge(1); //id_1 = merge_1
        Placings<DefaultDynamicSignature>.Merge merge_M = placings.merge(m);

        BigraphComposite<DefaultDynamicSignature> a = ops(merge_1);
        BigraphComposite<DefaultDynamicSignature> b = ops(aJoin);
        try {
            PureBigraphBuilder<DefaultDynamicSignature> builderForG = pureBuilder(signature);
            BigraphEntity.InnerName zInner = builderForG.createInnerName("z");
            builderForG.createRoot().addChild(signature.getControlByName("User")).linkToInner(zInner);
            PureBigraph simpleBigraph = builderForG.createBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel(
                    simpleBigraph,
                    new FileOutputStream(TARGET_TEST_PATH + "compose_test_2a.xmi")
            );
            Linkings<DefaultDynamicSignature>.Substitution substitution = linkings.substitution(StringTypedName.of("z"), StringTypedName.of("y"));
            BigraphComposite<DefaultDynamicSignature> compose = ops(simpleBigraph);
            BigraphComposite<DefaultDynamicSignature> compose1 = compose.compose(substitution);
            BigraphFileModelManagement.Store.exportAsInstanceModel((PureBigraph) compose1.getOuterBigraph(),
                    new FileOutputStream(TARGET_TEST_PATH + "compose_test_2b.xmi"));

            Linkings<DefaultDynamicSignature>.Substitution a1 = linkings.substitution(StringTypedName.of("a"), StringTypedName.of("b"), StringTypedName.of("c"));
            Linkings<DefaultDynamicSignature>.Substitution a2 = linkings.substitution(StringTypedName.of("x"), StringTypedName.of("a"));
            Bigraph<DefaultDynamicSignature> a3 = ops(a2).compose(a1).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((PureBigraph) a3,
                    new FileOutputStream(TARGET_TEST_PATH + "compose_test_3.xmi"));

        } catch (LinkTypeNotExistsException | IOException | IncompatibleSignatureException | IncompatibleInterfaceException | InvalidConnectionException | ControlIsAtomicException e) {
            e.printStackTrace();
        }
    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign();

        return (S) signatureBuilder.create();
    }

    public Bigraph createSampleBigraph() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        EMetaModelData metaModelData = new EMetaModelData.MetaModelDataBuilder()
                .setName("sampleModel")
                .setNsPrefix("bigraphSampleModel")
                .setNsUri("http://sample.bigraph")
                .create();
        createOrGetBigraphMetaModel(signature, metaModelData);
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.InnerName tmp1 = builder.createInnerName("tmp1");
        BigraphEntity.OuterName jeff = builder.createOuterName("jeff");


        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room = builder.hierarchy(signature.getControlByName("Room"));
        room.linkToInner(tmp1)
                .addChild(signature.getControlByName("User")).linkToOuter(jeff)
                .addChild(signature.getControlByName("Job"));

        builder.createRoot()
                .addChild(room)
                .addChild(signature.getControlByName("Room")).linkToInner(tmp1);

        builder.closeInnerName(tmp1);

        return builder.createBigraph();
    }
}
