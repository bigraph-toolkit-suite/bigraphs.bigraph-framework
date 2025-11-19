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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.BigraphComposite;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.documentation.BaseDocumentationGeneratorSupport;
import org.bigraphs.framework.documentation.MainDocGenerationRunner;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Test;

/**
 * This test class generates the code samples for the Getting Started section of the user manual.
 *
 * @author Dominik Grzelak
 */
public class GettingStartedGuide extends BaseDocumentationGeneratorSupport {

    String firstPrefix = "import static org.bigraphs.framework.core.factory.BigraphFactory.*;";

    @Override
    public List<String> acceptedMethods() {
        return Lists.mutable.of("getting_started_guide");
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
                    String format = String.format(MainDocGenerationRunner.BASE_EXPORT_PATH + "/basics/%s-%d.java", methodNameSimple, cnt);
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

    @Test
    void getting_started_guide() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException {
        // (1) start
        DynamicSignatureBuilder sigBuilder = pureSignatureBuilder();

        DynamicSignature signature = sigBuilder
                .add("User", 1, ControlStatus.ATOMIC)
                .add("Computer", 2)
                .create();
        // (1) end

        // (2) start
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        builder.root()
                .child("User", "login").child("Computer", "login");
        PureBigraph bigraph = builder.root()
                .child("User", "login").child("Computer", "login")
                .create();
        // (2) end

        // (3) start
        Placings<DynamicSignature> placings = purePlacings(signature);
        Placings<DynamicSignature>.Merge merge = placings.merge(2);
        Linkings<DynamicSignature> linkings = pureLinkings(signature);
        Linkings<DynamicSignature>.Identity login = linkings.identity("login");
        // (3) end

        // (4) start
        BigraphComposite<DynamicSignature> composed = ops(merge).parallelProduct(login).compose(bigraph);
        assert composed.getOuterBigraph().getRoots().size() == 1;
        assert composed.getOuterBigraph().getOuterNames().size() == 1;
        assert composed.getOuterBigraph().getChildrenOf(new ArrayList<>(composed.getOuterBigraph().getRoots()).get(0)).size() == 4;
        // (4) end
    }
}
