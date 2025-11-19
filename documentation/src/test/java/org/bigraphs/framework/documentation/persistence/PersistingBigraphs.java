/*
 * Copyright (c) 2022-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.documentation.persistence;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

import com.github.javaparser.ast.body.MethodDeclaration;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.documentation.BaseDocumentationGeneratorSupport;
import org.bigraphs.framework.documentation.MainDocGenerationRunner;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Test;

public class PersistingBigraphs extends BaseDocumentationGeneratorSupport {

    String firstPrefix = "import static org.bigraphs.framework.core.factory.BigraphFactory.*;";

    @Override
    public List<String> acceptedMethods() {
        return Lists.mutable.of("storing_a_metamodel_to_the_filesystem");
    }

    @Test
    void storing_a_metamodel_to_the_filesystem() throws IOException {
        // (1) start
        DynamicSignature signature = pureSignatureBuilder()
                .newControl("Building", 2).assign()
                .newControl("Laptop", 1).assign()
                .newControl("Printer", 2).assign()
                .create();

        // this will register the bigraph metamodel so that every bigraph created are over the "same" signature
        createOrGetBigraphMetaModel(signature);
        PureBigraphBuilder<DynamicSignature> bigraphBuilder = pureBuilder(
                signature
        );
        // do something with the bigraph builder ...
        PureBigraph bigraph = bigraphBuilder.create();

        // Export the metamodel
        BigraphFileModelManagement.Store.exportAsMetaModel(bigraph, new FileOutputStream("meta-model.ecore"));
        // (1) end
    }

    public void code_sample_one() throws IOException {
        DynamicSignature signature = pureSignatureBuilder()
                .add("Building", 2)
                .add("Laptop", 1)
                .add("Printer", 2)
                .create();

        createOrGetBigraphMetaModel(signature, EMetaModelData.builder()
                .setName("myMetaModel")
                .setNsPrefix("example")
                .setNsUri("http://example.org")
                .create());
        PureBigraphBuilder<DynamicSignature> bigraphBuilder = pureBuilder(
                signature
        );
        // do something with the bigraph builder ...
        PureBigraph bigraph = bigraphBuilder.create();

        // Export the metamodel
        BigraphFileModelManagement.Store.exportAsMetaModel(bigraph, new FileOutputStream(new File("meta-model.ecore")));
    }

    @Override
    public void generateCodeBlockOutput(List<CodeBlock> codeBlocks, MethodDeclaration methodDeclaration) {
        int cnt = 0;
        String methodName = methodDeclaration.getDeclarationAsString(true, true).trim();
        String methodNameSimple = methodDeclaration.getName().toString();
        if (codeBlocks.size() > 0) {
            for (CodeBlock each : codeBlocks) {
                StringBuilder sb = new StringBuilder(CODE_FENCE_START);
                sb.append("\n");
                if (cnt == 0) {
                    sb.append(firstPrefix).append("\n\n");
                }
                sb.append(methodName).append(" { ").append("\n");
                if (cnt > 0) {
                    sb.append("\t").append("// ...").append("\n");
                }
                each.getLines().forEach(l -> {
                    sb.append("\t").append(l).append(";").append("\n");
                });
                sb.append("}");
                sb.append("\n");
                sb.append(CODE_FENCE_END);
                try {
                    String format = String.format(MainDocGenerationRunner.BASE_EXPORT_PATH + "/persistence/%s-%d.java", methodNameSimple, cnt);
                    Path exportPath = Paths.get(getMavenModuleRoot(
                                    this.getClass()).toAbsolutePath().toString(),
                            "../",
                            format);
                    System.out.println("Exporting to " + exportPath);
                    FileUtils.writeStringToFile(
                            exportPath.toFile(),
                            sb.toString(),
                            StandardCharsets.UTF_8
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                cnt++;
            }
        }
    }
}
