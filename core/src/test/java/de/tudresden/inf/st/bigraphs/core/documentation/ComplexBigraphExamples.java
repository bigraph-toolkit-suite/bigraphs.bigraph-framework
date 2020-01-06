package de.tudresden.inf.st.bigraphs.core.documentation;

import de.tudresden.inf.st.bigraphs.core.BigraphArtifacts;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pure;
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * Bigraphs that are used for the user manual are created here.
 *
 * @author Dominik Grzelak
 */
public class ComplexBigraphExamples {
    private final static String TARGET_TEST_PATH = "src/test/resources/dump/exported-models/";
    private static PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();

    @BeforeEach
    void setUp() throws IOException {
        create_meta_model();
    }

    void create_meta_model() throws IOException {
        PureBigraphBuilder<DefaultDynamicSignature> bigraphBuilder = factory.createBigraphBuilder(
                createExampleSignature(),
                EMetaModelData.builder().setName("myMetaModel").setNsPrefix("example").setNsUri("http://example.org").create()
        );
        PureBigraph bigraph = bigraphBuilder.createBigraph();
        BigraphArtifacts.exportAsMetaModel(bigraph,
                new FileOutputStream(new File(TARGET_TEST_PATH + "my-meta-model.ecore")));

    }

    @Test
    void building_and_combining_hierarchies() throws InvalidConnectionException, TypeNotExistsException, IOException {
        DefaultDynamicSignature signature = //createExampleSignature();
                BigraphFactory.pureSignatureBuilder()
                .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Laptop")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .create();

        PureBigraphBuilder<DefaultDynamicSignature> builder =
                BigraphFactory.pureBuilder(signature, TARGET_TEST_PATH + "my-meta-model.ecore");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy buildingRight =
                builder.hierarchy("Building")
                        .addChild("Room").down()
                        .addChild("Laptop").addChild("Laptop").addChild("Laptop").addChild("Laptop");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy roomLeft = builder.hierarchy("Room");
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy roomRight = builder.hierarchy("Room");

        BigraphEntity.InnerName login = builder.createInnerName("login");
        roomLeft.addChild("User").linkToInner(login)
                .addChild("Laptop", "network").linkToInner(login).down().addChild("Job");
        builder.closeInnerName(login);

        roomRight.addChild("Printer", "network");

        builder.createRoot().addChild("Building").down().addChild(roomLeft.top()).addChild(roomRight.top());
        builder.createRoot().addChild(buildingRight.top());

        PureBigraph bigraph = builder.createBigraph();
//        System.out.println(bigraph.getRoots().size());
//        BigraphArtifacts.exportAsInstanceModel(bigraph, new FileOutputStream(new File("test.xmi")));
    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder signatureBuilder = factory.createSignatureBuilder();
        signatureBuilder
                .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("Laptop")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign();

        return (S) signatureBuilder.create();
    }
}
