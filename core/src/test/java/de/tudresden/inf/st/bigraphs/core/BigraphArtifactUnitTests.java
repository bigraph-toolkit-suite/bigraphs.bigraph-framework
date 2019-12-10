package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BigraphArtifactUnitTests {

    private PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();
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
    @DisplayName("Write the meta-model and instance model of a bigraph")
    void write_model_test() {
        assertAll(() -> {
            PureBigraph bigraph = (PureBigraph) createSampleBigraph();

            BigraphArtifacts.exportAsMetaModel(bigraph,
                    new FileOutputStream(TARGET_TEST_PATH + "test_meta-model.ecore"));
            BigraphArtifacts.exportAsInstanceModel(bigraph,
                    new FileOutputStream(TARGET_TEST_PATH + "test_instance-model.xmi"));
        });
    }

    @Test
    @DisplayName("Load an external model and build a bigraph instance")
    void load_external_model_test() {
        assertAll(() -> {
            String fileName = "/home/dominik/git/BigraphFramework/core/src/test/resources/ecore-test-models/test-1.ecore";
            String fileName2 = "/home/dominik/git/BigraphFramework/core/src/test/resources/ecore-test-models/test-1.xmi";
            EPackage metaModel = BigraphArtifacts.loadBigraphMetaModel(fileName);

            List<EObject> eObjects = BigraphArtifacts.loadBigraphInstanceModel(metaModel, fileName2);
            assertEquals(1, eObjects.size());
            List<EObject> eObjects2 = BigraphArtifacts.loadBigraphInstanceModel(fileName2);
            assertEquals(1, eObjects2.size());

            Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
            PureBigraphBuilder builder = PureBigraphBuilder.create(signature, fileName, fileName2);
            System.out.println(builder);
            PureBigraph bigraph = builder.createBigraph();
            BigraphArtifacts.exportAsInstanceModel(bigraph, new FileOutputStream(TARGET_TEST_PATH + "test-1_reloaded.xmi"));
        });
    }

    @Test
    void compose_output() {
        assertAll(() -> {

            Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
            EMetaModelData modelData = EMetaModelData.builder().setName("F").setNsUri("http://www.example.org").setNsPrefix("sample").create();
            PureBigraphBuilder<DefaultDynamicSignature> builderForF = factory.createBigraphBuilder(signature, modelData);
            PureBigraphBuilder<DefaultDynamicSignature> builderForG = factory.createBigraphBuilder(signature);
//
            BigraphEntity.OuterName jeff = builderForF.createOuterName("jeff");
            BigraphEntity.InnerName jeffG = builderForG.createInnerName("jeff");
            BigraphEntity.InnerName f1 = builderForF.createInnerName("x_f");
            BigraphEntity.InnerName f2 = builderForF.createInnerName("y_f");

            PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room =
                    builderForF.newHierarchy(signature.getControlByName("Room"));
            room.addChild(signature.getControlByName("User")).linkToOuter(jeff).addChild(signature.getControlByName("Job"))
                    .addChild(signature.getControlByName("Printer")).linkToInner(f1).linkToInner(f2)
            ;
            builderForF.createRoot()
                    .addChild(room);

            builderForG.createRoot()
                    .addChild(signature.getControlByName("Job")).withNewHierarchy().addSite().goBack()
                    .addChild(signature.getControlByName("User")).linkToInner(jeffG);


            builderForF.createBigraph();
            PureBigraph F = builderForF.createBigraph();
            PureBigraph G = builderForG.createBigraph();

            BigraphArtifacts.exportAsInstanceModel(F, new FileOutputStream(TARGET_TEST_PATH + "f.xmi"));
            BigraphComposite<DefaultDynamicSignature> compositor = factory.asBigraphOperator(G);
            BigraphComposite<DefaultDynamicSignature> composedBigraph = compositor.compose(F);
            BigraphArtifacts.exportAsInstanceModel((PureBigraph) composedBigraph.getOuterBigraph(),
                    new FileOutputStream(TARGET_TEST_PATH + "composetest.xmi"));
            BigraphArtifacts.exportAsMetaModel(composedBigraph.getOuterBigraph(),
                    new FileOutputStream(TARGET_TEST_PATH + "composetest.ecore"));

            BigraphComposite<DefaultDynamicSignature> juxtapose = compositor.juxtapose(F);
            BigraphArtifacts.exportAsInstanceModel((PureBigraph) juxtapose.getOuterBigraph(),
                    new FileOutputStream(TARGET_TEST_PATH + "juxtatest.xmi"));

        });
    }

    // change nsURI in to "http:///ecore_file_name.ecore" and nsPrefix into "ecore_file_name" after
    @Test
    void export_sample_model() throws IOException, InvalidConnectionException, TypeNotExistsException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        EMetaModelData modelData = EMetaModelData.builder().setName("F").setNsUri("http://www.example.org").setNsPrefix("sample").create();
        PureBigraphBuilder<DefaultDynamicSignature> builderForF = factory.createBigraphBuilder(signature, modelData);
        BigraphEntity.OuterName jeff = builderForF.createOuterName("jeff");
        BigraphEntity.InnerName f1 = builderForF.createInnerName("x_f");
        BigraphEntity.InnerName f2 = builderForF.createInnerName("y_f");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room =
                builderForF.newHierarchy(signature.getControlByName("Room"));
        room.addChild(signature.getControlByName("User")).linkToOuter(jeff).addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("Printer")).linkToInner(f1).linkToInner(f2).withNewHierarchy().addSite()
                .goBack()
                .addSite()
        ;
        builderForF.createRoot()
                .addChild(room);

        PureBigraph F = builderForF.createBigraph();

        BigraphArtifacts.exportAsInstanceModel(F,
                new FileOutputStream(TARGET_TEST_EXPORT_PATH + "test-1.xmi"), "test-1.ecore");
        BigraphArtifacts.exportAsMetaModel(F,
                new FileOutputStream(TARGET_TEST_EXPORT_PATH + "test-1.ecore"));
    }

    @Test
    void compose_output_elementary_composition() {
        int m = 3;
        Placings<DefaultDynamicSignature> placings = factory.createPlacings();
        Placings<DefaultDynamicSignature>.Merge merge_MplusOne = placings.merge(m + 1);

        Placings<DefaultDynamicSignature>.Join aJoin = placings.join();
        Placings<DefaultDynamicSignature>.Merge merge_1 = placings.merge(1); //id_1 = merge_1
        Placings<DefaultDynamicSignature>.Merge merge_M = placings.merge(m);

        BigraphComposite<DefaultDynamicSignature> a = factory.asBigraphOperator(merge_1);
        BigraphComposite<DefaultDynamicSignature> b = factory.asBigraphOperator(aJoin);
        try {
//            Bigraph<DefaultDynamicSignature> outerBigraph1 = a.juxtapose(merge_M).getOuterBigraph();
//            Bigraph<DefaultDynamicSignature> outerBigraph = b.compose(outerBigraph1).getOuterBigraph();
//            BigraphModelFileStore.exportBigraph((PureBigraph) outerBigraph, "compose_test", new FileOutputStream(TARGET_TEST_FOLDER + "compose_test.xmi"));
//

            Linkings<DefaultDynamicSignature> linkings = factory.createLinkings();
            Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builderForG = factory.createBigraphBuilder(signature);
            BigraphEntity.InnerName zInner = builderForG.createInnerName("z");
            builderForG.createRoot().addChild(signature.getControlByName("User")).linkToInner(zInner);
            PureBigraph simpleBigraph = builderForG.createBigraph();
            BigraphArtifacts.exportAsInstanceModel((PureBigraph) simpleBigraph,
                    new FileOutputStream(TARGET_TEST_PATH + "compose_test_2a.xmi"));
            Linkings<DefaultDynamicSignature>.Substitution substitution = linkings.substitution(StringTypedName.of("z"), StringTypedName.of("y"));
            BigraphComposite<DefaultDynamicSignature> compose = factory.asBigraphOperator(simpleBigraph);
            BigraphComposite<DefaultDynamicSignature> compose1 = compose.compose(substitution);
            BigraphArtifacts.exportAsInstanceModel((PureBigraph) compose1.getOuterBigraph(),
                    new FileOutputStream(TARGET_TEST_PATH + "compose_test_2b.xmi"));

            Linkings<DefaultDynamicSignature>.Substitution a1 = linkings.substitution(StringTypedName.of("a"), StringTypedName.of("b"), StringTypedName.of("c"));
            Linkings<DefaultDynamicSignature>.Substitution a2 = linkings.substitution(StringTypedName.of("x"), StringTypedName.of("a"));
            Bigraph<DefaultDynamicSignature> a3 = factory.asBigraphOperator(a2).compose(a1).getOuterBigraph();
            BigraphArtifacts.exportAsInstanceModel((PureBigraph) a3,
                    new FileOutputStream(TARGET_TEST_PATH + "compose_test_3.xmi"));

        } catch (LinkTypeNotExistsException | IOException | IncompatibleSignatureException | IncompatibleInterfaceException | InvalidConnectionException | ControlIsAtomicException e) {
            e.printStackTrace();
        }
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

    public Bigraph createSampleBigraph() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        EMetaModelData metaModelData = new EMetaModelData.MetaModelDataBuilder()
                .setName("sampleModel")
                .setNsPrefix("bigraphSampleModel")
                .setNsUri("http://sample.bigraph")
                .create();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature, metaModelData);

        BigraphEntity.InnerName tmp1 = builder.createInnerName("tmp1");
        BigraphEntity.OuterName jeff = builder.createOuterName("jeff");


        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room = builder.newHierarchy(signature.getControlByName("Room"));
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
