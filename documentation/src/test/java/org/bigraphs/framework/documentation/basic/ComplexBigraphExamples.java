package org.bigraphs.framework.documentation.basic;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.factory.BigraphFactory;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.documentation.BaseDocumentationGeneratorSupport;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

/**
 * Bigraphs that are used for the user manual are created here and code samples exported
 *
 * @author Dominik Grzelak
 */
public class ComplexBigraphExamples extends BaseDocumentationGeneratorSupport {
    private final static String TARGET_TEST_PATH = "src/test/resources/dump/exported-models/";

    @BeforeEach
    void setUp() throws IOException {
        create_meta_model();
    }

    @Override
    public List<String> acceptedMethods() {
        return Lists.mutable.of("building_and_combining_hierarchies");
    }


    void create_meta_model() throws IOException {
        createOrGetBigraphMetaModel(
                createExampleSignature(),
                EMetaModelData.builder().setName("myMetaModel").setNsPrefix("example").setNsUri("http://example.org").create()
        );

        PureBigraphBuilder<DefaultDynamicSignature> bigraphBuilder = pureBuilder(
                // it does not matter if we re-create the signature object because we registered it already
                createExampleSignature()
        );

        PureBigraph bigraph = bigraphBuilder.createBigraph();
        BigraphFileModelManagement.Store.exportAsMetaModel(
                bigraph,
                new FileOutputStream(TARGET_TEST_PATH + "my-meta-model.ecore")
        );
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
//        BigraphFileModelManagement.exportAsInstanceModel(bigraph, new FileOutputStream(new File("test.xmi")));
    }

    private DefaultDynamicSignature createExampleSignature() {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder
                .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("Laptop")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign();

        return signatureBuilder.create();
    }

    @Override
    public void generateCodeBlockOutput(List<CodeBlock> codeBlocks, MethodDeclaration methodDeclaration) {

    }
}
