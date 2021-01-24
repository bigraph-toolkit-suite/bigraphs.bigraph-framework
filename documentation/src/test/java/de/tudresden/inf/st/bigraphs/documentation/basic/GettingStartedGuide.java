package de.tudresden.inf.st.bigraphs.documentation.basic;

import com.github.javaparser.ast.body.MethodDeclaration;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.documentation.BaseDocumentationGeneratorSupport;
import org.apache.commons.io.FileUtils;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;

/**
 * This test class generates the code samples for the Getting Started section of the user manual.
 *
 * @author Dominik Grzelak
 */
public class GettingStartedGuide extends BaseDocumentationGeneratorSupport {

    String firstPrefix = "import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;";

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
                    String format = String.format(BASE_EXPORT_PATH + "/basics/%s-%d.java", methodNameSimple, cnt);
                    Path exportPath = Paths.get(getMavenModuleRoot(
                            this.getClass()).toAbsolutePath().toString(),
                            "../",
                            format);
                    System.out.println("Exporting to " + exportPath);
                    FileUtils.writeStringToFile(
                            exportPath.toFile(),
                            sb.toString(),
                            Charset.forName("UTF-8")
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
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();

        DefaultDynamicSignature signature = signatureBuilder
                .newControl().identifier("User").arity(1).status(ControlStatus.ATOMIC).assign()
                .newControl(StringTypedName.of("Computer"), FiniteOrdinal.ofInteger(2)).assign()
                .create();
        // (1) end

        // (2) start
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        builder.createRoot()
                .addChild("User", "login").addChild("Computer", "login");
        PureBigraph bigraph = builder.createRoot()
                .addChild("User", "login2").addChild("Computer", "login")
                .createBigraph();
        // (2) end

        // (3) start
        Placings<DefaultDynamicSignature> placings = pure().createPlacings(signature);
        Placings<DefaultDynamicSignature>.Merge merge = placings.merge(2);
        Linkings<DefaultDynamicSignature> linkings = pure().createLinkings(signature);
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
