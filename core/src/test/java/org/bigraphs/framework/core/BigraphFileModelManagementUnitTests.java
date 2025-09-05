package org.bigraphs.framework.core;

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
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
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
        DynamicSignature signature = createExampleSignature();
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
        DynamicSignature sig = pureSignatureBuilder()
                .add("A", 1)
                .add("B", 2)
                .add("C", 3)
                .create();
//        createOrGetSignatureMetaModel(sig, EMetaModelData.builder()
//                .setName("sigsig")
//                .create());
        System.out.println(sig.getMetaModel().getName());

//        createOrGetBigraphMetaModel(sig, EMetaModelData.builder().setName("bigbig").create());
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig);
        builder.root()
                .child("A")
                .child("B")
                .child("C")
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
        DynamicSignature sigLoaded = createOrGetSignature(eObjectsSigInstanceLoaded.get(0));
        assertEquals(sigLoaded, sig);

        PureBigraph bigraph = builder.create();
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

            DynamicSignature signature = createExampleSignature();
            EMetaModelData modelData = EMetaModelData.builder().setName("F").setNsUri("http://www.example.org").setNsPrefix("sample").create();
            createOrGetBigraphMetaModel(signature, modelData);
            PureBigraphBuilder<DynamicSignature> builderForF = pureBuilder(signature);
            PureBigraphBuilder<DynamicSignature> builderForG = pureBuilder(signature);
//
            BigraphEntity.OuterName jeff = builderForF.createOuter("jeff");
            BigraphEntity.InnerName jeffG = builderForG.createInner("jeff");
            BigraphEntity.InnerName f1 = builderForF.createInner("x_f");
            BigraphEntity.InnerName f2 = builderForF.createInner("y_f");

            PureBigraphBuilder<DynamicSignature>.Hierarchy room =
                    builderForF.hierarchy(signature.getControlByName("Room"));
            room.child(signature.getControlByName("User")).linkOuter(jeff).child(signature.getControlByName("Job"))
                    .child(signature.getControlByName("Printer")).linkInner(f1).linkInner(f2)
            ;
            builderForF.root()
                    .child(room);

            builderForG.root()
                    .child(signature.getControlByName("Job")).down().site().up()
                    .child(signature.getControlByName("User")).linkInner(jeffG);


            builderForF.create();
            PureBigraph F = builderForF.create();
            PureBigraph G = builderForG.create();

            BigraphFileModelManagement.Store.exportAsInstanceModel(F, new FileOutputStream(TARGET_TEST_PATH + "f.xmi"));
            BigraphComposite<DynamicSignature> compositor = ops(G);
            BigraphComposite<DynamicSignature> composedBigraph = compositor.compose(F);
            BigraphFileModelManagement.Store.exportAsInstanceModel((PureBigraph) composedBigraph.getOuterBigraph(),
                    new FileOutputStream(TARGET_TEST_PATH + "composetest.xmi"));
            BigraphFileModelManagement.Store.exportAsMetaModel((PureBigraph) composedBigraph.getOuterBigraph(),
                    new FileOutputStream(TARGET_TEST_PATH + "composetest.ecore"));

            BigraphComposite<DynamicSignature> juxtapose = compositor.juxtapose(F);
            BigraphFileModelManagement.Store.exportAsInstanceModel((PureBigraph) juxtapose.getOuterBigraph(),
                    new FileOutputStream(TARGET_TEST_PATH + "juxtatest.xmi"));

        });
    }

    // change nsURI in to "http:///ecore_file_name.ecore" and nsPrefix into "ecore_file_name" after
    @Test
    void export_sample_model() throws IOException, InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = createExampleSignature();
        EMetaModelData modelData = EMetaModelData.builder().setName("F").setNsUri("http://www.example.org").setNsPrefix("sample").create();
        createOrGetBigraphMetaModel(signature, modelData);
        PureBigraphBuilder<DynamicSignature> builderForF = pureBuilder(signature);
        BigraphEntity.OuterName jeff = builderForF.createOuter("jeff");
        BigraphEntity.InnerName f1 = builderForF.createInner("x_f");
        BigraphEntity.InnerName f2 = builderForF.createInner("y_f");

        PureBigraphBuilder<DynamicSignature>.Hierarchy room =
                builderForF.hierarchy(signature.getControlByName("Room"));
        room.child(signature.getControlByName("User")).linkOuter(jeff).child(signature.getControlByName("Job"))
                .child(signature.getControlByName("Printer")).linkInner(f1).linkInner(f2).down().site()
                .up()
                .site()
        ;
        builderForF.root()
                .child(room);

        PureBigraph F = builderForF.create();

        BigraphFileModelManagement.Store.exportAsInstanceModel(F,
                new FileOutputStream(TARGET_TEST_EXPORT_PATH + "test-1.xmi"), "test-1.ecore");
        BigraphFileModelManagement.Store.exportAsMetaModel(F,
                new FileOutputStream(TARGET_TEST_EXPORT_PATH + "test-1.ecore"));
    }

    @Test
    void compose_output_elementary_composition() {
        int m = 3;
        DynamicSignature signature = createExampleSignature();
//        DefaultDynamicSignature empty = pureSignatureBuilder().createEmpty();
        createOrGetBigraphMetaModel(signature);
        Placings<DynamicSignature> placings = purePlacings(signature);
        Linkings<DynamicSignature> linkings = pureLinkings(signature);
        Placings<DynamicSignature>.Merge merge_MplusOne = placings.merge(m + 1);

        Placings<DynamicSignature>.Join aJoin = placings.join();
        Placings<DynamicSignature>.Merge merge_1 = placings.merge(1); //id_1 = merge_1
        Placings<DynamicSignature>.Merge merge_M = placings.merge(m);

        BigraphComposite<DynamicSignature> a = ops(merge_1);
        BigraphComposite<DynamicSignature> b = ops(aJoin);
        try {
            PureBigraphBuilder<DynamicSignature> builderForG = pureBuilder(signature);
            BigraphEntity.InnerName zInner = builderForG.createInner("z");
            builderForG.root().child(signature.getControlByName("User")).linkInner(zInner);
            PureBigraph simpleBigraph = builderForG.create();
            BigraphFileModelManagement.Store.exportAsInstanceModel(
                    simpleBigraph,
                    new FileOutputStream(TARGET_TEST_PATH + "compose_test_2a.xmi")
            );
            Linkings<DynamicSignature>.Substitution substitution = linkings.substitution(StringTypedName.of("z"), StringTypedName.of("y"));
            BigraphComposite<DynamicSignature> compose = ops(simpleBigraph);
            BigraphComposite<DynamicSignature> compose1 = compose.compose(substitution);
            BigraphFileModelManagement.Store.exportAsInstanceModel((PureBigraph) compose1.getOuterBigraph(),
                    new FileOutputStream(TARGET_TEST_PATH + "compose_test_2b.xmi"));

            Linkings<DynamicSignature>.Substitution a1 = linkings.substitution(StringTypedName.of("a"), StringTypedName.of("b"), StringTypedName.of("c"));
            Linkings<DynamicSignature>.Substitution a2 = linkings.substitution(StringTypedName.of("x"), StringTypedName.of("a"));
            Bigraph<DynamicSignature> a3 = ops(a2).compose(a1).getOuterBigraph();
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
        DynamicSignature signature = createExampleSignature();
        EMetaModelData metaModelData = new EMetaModelData.MetaModelDataBuilder()
                .setName("sampleModel")
                .setNsPrefix("bigraphSampleModel")
                .setNsUri("http://sample.bigraph")
                .create();
        createOrGetBigraphMetaModel(signature, metaModelData);
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.InnerName tmp1 = builder.createInner("tmp1");
        BigraphEntity.OuterName jeff = builder.createOuter("jeff");


        PureBigraphBuilder<DynamicSignature>.Hierarchy room = builder.hierarchy(signature.getControlByName("Room"));
        room.linkInner(tmp1)
                .child(signature.getControlByName("User")).linkOuter(jeff)
                .child(signature.getControlByName("Job"));

        builder.root()
                .child(room)
                .child(signature.getControlByName("Room")).linkInner(tmp1);

        builder.closeInner(tmp1);

        return builder.create();
    }
}
