package org.bigraphs.framework.documentation.basic;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.bigraphs.framework.core.BigraphComposite;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.factory.BigraphFactory;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.documentation.BaseDocumentationGeneratorSupport;
import org.bigraphs.framework.documentation.MainDocGenerationRunner;
import org.apache.commons.io.FileUtils;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

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

        DefaultDynamicSignature signature = sigBuilder
                .addControl("User", 1, ControlStatus.ATOMIC)
                .addControl("Computer", 2)
                .create();
        // (1) end

        // (2) start
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        builder.createRoot()
                .addChild("User", "login").addChild("Computer", "login");
        PureBigraph bigraph = builder.createRoot()
                .addChild("User", "login").addChild("Computer", "login")
                .createBigraph();
        // (2) end

        // (3) start
        Placings<DefaultDynamicSignature> placings = purePlacings(signature);
        Placings<DefaultDynamicSignature>.Merge merge = placings.merge(2);
        Linkings<DefaultDynamicSignature> linkings = pureLinkings(signature);
        Linkings<DefaultDynamicSignature>.Identity login = linkings.identity(StringTypedName.of("login"));
        // (3) end

        // (4) start
        BigraphComposite<DefaultDynamicSignature> composed = BigraphFactory.ops(merge).parallelProduct(login).compose(bigraph);
        assert composed.getOuterBigraph().getRoots().size() == 1;
        assert composed.getOuterBigraph().getOuterNames().size() == 1;
        assert composed.getOuterBigraph().getChildrenOf(new ArrayList<>(composed.getOuterBigraph().getRoots()).get(0)).size() == 4;
        // (4) end
    }
}
