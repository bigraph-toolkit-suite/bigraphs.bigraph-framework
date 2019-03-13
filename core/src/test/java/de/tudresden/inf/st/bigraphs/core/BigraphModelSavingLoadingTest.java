package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultControl;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DefaultSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.EcoreBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphModelFileStore;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertAll;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BigraphModelSavingLoadingTest {

    private static <C extends Control<?, ?>> Signature<C> createExampleSignature() {
        DefaultSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> defaultBuilder = new DefaultSignatureBuilder<>();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign();

        return (Signature<C>) defaultBuilder.create();
    }

    public static Bigraph create() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder = EcoreBigraphBuilder.start(signature);

        BigraphEntity.InnerName tmp1 = builder.createInnerName("tmp1");
        BigraphEntity.OuterName jeff = builder.createOuterName("jeff");


        EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>>.Hierarchy room = builder.newHierarchy(signature.getControlByName("Room"));
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

    @Test
    void load_instance_model_test() {
        assertAll(() -> {

            DynamicEcoreBigraph bigraph = (DynamicEcoreBigraph) create();

            BigraphModelFileStore.exportMetaModel(bigraph, "test_meta", new FileOutputStream("./test_meta.ecore"));
            BigraphModelFileStore.exportBigraph(bigraph, "test", new FileOutputStream("./test.xmi"));

            EPackage ePackage = BigraphModelFileStore.loadEcoreMetaModel(URI.create("file:///home/dominik/git/BigraphFramework/core/test_meta.ecore"));

            EList<EObject> eObjects = BigraphModelFileStore.loadInstanceModel(ePackage, URI.create("file:///home/dominik/git/BigraphFramework/core/test.xmi"));
            System.out.println(eObjects);
            EList<EObject> eObjects2 = BigraphModelFileStore.loadInstanceModel(ePackage, URI.create("file:///home/dominik/git/BigraphFramework/core/src/test/resources/ecore-test-models/printer-example-1.xmi"));
        });
    }
}
