/*
 * Copyright (c) 2020-2025 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.documentation.basic;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

import com.github.javaparser.ast.body.MethodDeclaration;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.factory.BigraphFactory;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.documentation.BaseDocumentationGeneratorSupport;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

        PureBigraphBuilder<DynamicSignature> bigraphBuilder = pureBuilder(
                // it does not matter if we re-create the signature object because we registered it already
                createExampleSignature()
        );

        PureBigraph bigraph = bigraphBuilder.create();
        BigraphFileModelManagement.Store.exportAsMetaModel(
                bigraph,
                new FileOutputStream(TARGET_TEST_PATH + "my-meta-model.ecore")
        );
    }

    @Test
    void building_and_combining_hierarchies() throws InvalidConnectionException, TypeNotExistsException, IOException {
        DynamicSignature signature = //createExampleSignature();
                BigraphFactory.pureSignatureBuilder()
                        .add("Building", 0)
                        .add("Room", 1)
                        .add("User", 1)
                        .add("Laptop", 2)
                        .add("Printer", 1)
                        .add("Job", 1)
                        .create();

        PureBigraphBuilder<DynamicSignature> builder =
                BigraphFactory.pureBuilder(signature, TARGET_TEST_PATH + "my-meta-model.ecore");

        PureBigraphBuilder<DynamicSignature>.Hierarchy buildingRight =
                builder.hierarchy("Building")
                        .child("Room").down()
                        .child("Laptop").child("Laptop").child("Laptop").child("Laptop");

        PureBigraphBuilder<DynamicSignature>.Hierarchy roomLeft = builder.hierarchy("Room");
        PureBigraphBuilder<DynamicSignature>.Hierarchy roomRight = builder.hierarchy("Room");

        BigraphEntity.InnerName login = builder.createInner("login");
        roomLeft.child("User").linkInner(login)
                .child("Laptop", "network").linkInner(login).down().child("Job");
        builder.closeInner(login);

        roomRight.child("Printer", "network");

        builder.root().child("Building").down().child(roomLeft.top()).child(roomRight.top());
        builder.root().child(buildingRight.top());

        PureBigraph bigraph = builder.create();
//        System.out.println(bigraph.getRoots().size());
//        BigraphFileModelManagement.exportAsInstanceModel(bigraph, new FileOutputStream(new File("test.xmi")));
    }

    private DynamicSignature createExampleSignature() {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder
                .add("Building", 2)
                .add("Laptop", 2)
                .add("Printer", 2)
                .add("User", 1)
                .add("Room", 1)
                .add("Spool", 1)
                .add("Job", 0);

        return signatureBuilder.create();
    }

    @Override
    public void generateCodeBlockOutput(List<CodeBlock> codeBlocks, MethodDeclaration methodDeclaration) {

    }
}
