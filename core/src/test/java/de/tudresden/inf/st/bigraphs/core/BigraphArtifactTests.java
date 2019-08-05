package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.BigraphModelFileStore;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertAll;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BigraphArtifactTests {
    private PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = AbstractBigraphFactory.createPureBigraphFactory();
    private final static String TARGET_TEST_PATH = "src/test/exported-models/";

    @Test
    void load_instance_model_test() {
        assertAll(() -> {

            PureBigraph bigraph = (PureBigraph) createSampleBigraph();

            BigraphModelFileStore.exportAsMetaModel(bigraph, "test_meta",
                    new FileOutputStream("src/test/exported-models/test_meta.ecore"));
            BigraphModelFileStore.exportAsInstanceModel(bigraph, "test",
                    new FileOutputStream("src/test/exported-models/test.xmi"));

        });
    }

    @Test
    void compose_output() {
        assertAll(() -> {

            Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builderForF = factory.createBigraphBuilder(signature);
            PureBigraphBuilder<DefaultDynamicSignature> builderForG = factory.createBigraphBuilder(signature);
//
            BigraphEntity.OuterName jeff = builderForF.createOuterName("jeff");
            BigraphEntity.InnerName jeffG = builderForG.createInnerName("jeff");
            BigraphEntity.InnerName f1 = builderForF.createInnerName("x_f");
            BigraphEntity.InnerName f2 = builderForF.createInnerName("y_f");

            PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room =
                    builderForF.newHierarchy(signature.getControlByName("Room"));
            room.addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff).addChild(signature.getControlByName("Job"))
                    .addChild(signature.getControlByName("Printer")).connectNodeToInnerName(f1).connectNodeToInnerName(f2)
            ;
            builderForF.createRoot()
                    .addChild(room);

            builderForG.createRoot()
                    .addChild(signature.getControlByName("Job")).withNewHierarchy().addSite().goBack()
                    .addChild(signature.getControlByName("User")).connectNodeToInnerName(jeffG);


            builderForF.createBigraph();
            PureBigraph F = builderForF.createBigraph();
            PureBigraph G = builderForG.createBigraph();

            BigraphModelFileStore.exportAsInstanceModel(F, "f", new FileOutputStream(TARGET_TEST_PATH + "f.xmi"));
            BigraphComposite<DefaultDynamicSignature> compositor = factory.asBigraphOperator(G);
            BigraphComposite<DefaultDynamicSignature> composedBigraph = compositor.compose(F);
            BigraphModelFileStore.exportAsInstanceModel((PureBigraph) composedBigraph.getOuterBigraph(), "composetest",
                    new FileOutputStream(TARGET_TEST_PATH + "composetest.xmi"));

            BigraphComposite<DefaultDynamicSignature> juxtapose = compositor.juxtapose(F);
            BigraphModelFileStore.exportAsInstanceModel((PureBigraph) juxtapose.getOuterBigraph(), "juxtatest",
                    new FileOutputStream(TARGET_TEST_PATH + "juxtatest.xmi"));

        });
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
            builderForG.createRoot().addChild(signature.getControlByName("User")).connectNodeToInnerName(zInner);
            PureBigraph simpleBigraph = builderForG.createBigraph();
            BigraphModelFileStore.exportAsInstanceModel((PureBigraph) simpleBigraph, "compose_test_2a",
                    new FileOutputStream(TARGET_TEST_PATH + "compose_test_2a.xmi"));
            Linkings<DefaultDynamicSignature>.Substitution substitution = linkings.substitution(StringTypedName.of("z"), StringTypedName.of("y"));
            BigraphComposite<DefaultDynamicSignature> compose = factory.asBigraphOperator(simpleBigraph);
            BigraphComposite<DefaultDynamicSignature> compose1 = compose.compose(substitution);
            BigraphModelFileStore.exportAsInstanceModel((PureBigraph) compose1.getOuterBigraph(), "compose_test_2b",
                    new FileOutputStream(TARGET_TEST_PATH + "compose_test_2b.xmi"));

            Linkings<DefaultDynamicSignature>.Substitution a1 = linkings.substitution(StringTypedName.of("a"), StringTypedName.of("b"), StringTypedName.of("c"));
            Linkings<DefaultDynamicSignature>.Substitution a2 = linkings.substitution(StringTypedName.of("x"), StringTypedName.of("a"));
            Bigraph<DefaultDynamicSignature> a3 = factory.asBigraphOperator(a2).compose(a1).getOuterBigraph();
            BigraphModelFileStore.exportAsInstanceModel((PureBigraph) a3, "compose_test_3",
                    new FileOutputStream(TARGET_TEST_PATH + "compose_test_3.xmi"));

        } catch (LinkTypeNotExistsException | IOException | IncompatibleSignatureException | IncompatibleInterfaceException | InvalidConnectionException | ControlIsAtomicException e) {
            e.printStackTrace();
        }
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

    public Bigraph createSampleBigraph() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
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
        room.connectNodeToInnerName(tmp1)
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff)
                .addChild(signature.getControlByName("Job"));

        builder.createRoot()
                .addChild(room)
                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(tmp1);

        builder.closeInnerName(tmp1);

        return builder.createBigraph();
    }
}
