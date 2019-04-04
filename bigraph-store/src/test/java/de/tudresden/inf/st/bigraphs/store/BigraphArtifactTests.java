package de.tudresden.inf.st.bigraphs.store;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.building.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DefaultSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.factory.SimpleBigraphFactory;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertAll;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BigraphArtifactTests {
    private SimpleBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = new SimpleBigraphFactory<>();
    private final static String TARGET_TEST_FOLDER = "./gen-test-resources/";

    @Test
    void load_instance_model_test() {
        assertAll(() -> {

            DynamicEcoreBigraph bigraph = (DynamicEcoreBigraph) create();

            BigraphModelFileStore.exportMetaModel(bigraph, "test_meta", new FileOutputStream("./test_meta.ecore"));
            BigraphModelFileStore.exportBigraph(bigraph, "test", new FileOutputStream("./test.xmi"));

//            EPackage ePackage = BigraphModelFileStore.loadEcoreMetaModel(URI.create("file:///home/dominik/git/BigraphFramework/core/test_meta.ecore"));

//            EList<EObject> eObjects = BigraphModelFileStore.loadInstanceModel(ePackage, URI.create("file:///home/dominik/git/BigraphFramework/core/test.xmi"));
//            System.out.println(eObjects);
//            EList<EObject> eObjects2 = BigraphModelFileStore.loadInstanceModel(ePackage, URI.create("file:///home/dominik/git/BigraphFramework/core/src/test/resources/ecore-test-models/printer-example-1.xmi"));
        });
    }

    @Test
    void compose_output() {
        assertAll(() -> {

            Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
            BigraphBuilder<DefaultDynamicSignature> builderForF = factory.createBigraphBuilder(signature);
            BigraphBuilder<DefaultDynamicSignature> builderForG = factory.createBigraphBuilder(signature);
//
            BigraphEntity.OuterName jeff = builderForF.createOuterName("jeff");
            BigraphEntity.InnerName jeffG = builderForG.createInnerName("jeff");
            BigraphEntity.InnerName f1 = builderForF.createInnerName("x_f");
            BigraphEntity.InnerName f2 = builderForF.createInnerName("y_f");

            BigraphBuilder<DefaultDynamicSignature>.Hierarchy room =
                    builderForF.newHierarchy(signature.getControlByName("Room"));
            room.addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff).addChild(signature.getControlByName("Job"))
                    .addChild(signature.getControlByName("Printer")).connectNodeToInnerName(f1).connectNodeToInnerName(f2)
            ;
            builderForF.createRoot()
                    .addHierarchyToParent(room);

            builderForG.createRoot()
                    .addChild(signature.getControlByName("Job")).withNewHierarchy().addSite().goBack()
                    .addChild(signature.getControlByName("User")).connectNodeToInnerName(jeffG);


            DynamicEcoreBigraph F = builderForF.createBigraph();
            DynamicEcoreBigraph G = builderForG.createBigraph();

            BigraphModelFileStore.exportBigraph(F, "f", new FileOutputStream(TARGET_TEST_FOLDER + "f.xmi"));
            BigraphComposite<DefaultDynamicSignature> compositor = factory.asBigraphOperator(G);
            BigraphComposite<DefaultDynamicSignature> composedBigraph = compositor.compose(F);
            BigraphModelFileStore.exportBigraph((DynamicEcoreBigraph) composedBigraph.getOuterBigraph(), "composetest", new FileOutputStream(TARGET_TEST_FOLDER + "composetest.xmi"));

            BigraphComposite<DefaultDynamicSignature> juxtapose = compositor.juxtapose(F);
            BigraphModelFileStore.exportBigraph((DynamicEcoreBigraph) juxtapose.getOuterBigraph(), "juxtatest", new FileOutputStream(TARGET_TEST_FOLDER + "juxtatest.xmi"));

        });
    }


    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> signatureBuilder = factory.createSignatureBuilder();
        signatureBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign();

        return (S) signatureBuilder.create();
    }

    public Bigraph create() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        BigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        BigraphEntity.InnerName tmp1 = builder.createInnerName("tmp1");
        BigraphEntity.OuterName jeff = builder.createOuterName("jeff");


        BigraphBuilder<DefaultDynamicSignature>.Hierarchy room = builder.newHierarchy(signature.getControlByName("Room"));
        room.connectNodeToInnerName(tmp1)
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff)
                .addChild(signature.getControlByName("Job"));

        builder.createRoot()
                .addHierarchyToParent(room)
                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(tmp1);

        builder.closeInnerName(tmp1);

        DynamicEcoreBigraph bigraph = builder.createBigraph();
        FileOutputStream fio = new FileOutputStream("./test.xmi");
        FileOutputStream fioMeta = new FileOutputStream("./test_meta.ecore");

        return bigraph;
    }
}
