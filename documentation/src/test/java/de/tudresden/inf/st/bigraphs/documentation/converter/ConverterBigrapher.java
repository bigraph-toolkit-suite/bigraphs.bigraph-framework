package de.tudresden.inf.st.bigraphs.documentation.converter;

import com.github.javaparser.ast.body.MethodDeclaration;
import de.tudresden.inf.st.bigraphs.converter.bigrapher.BigrapherTransformator;
import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.InstantiationMap;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactiveSystemPredicate;
import de.tudresden.inf.st.bigraphs.documentation.BaseDocumentationGeneratorSupport;
import de.tudresden.inf.st.bigraphs.documentation.MainDocGenerationRunner;
import de.tudresden.inf.st.bigraphs.simulation.matching.pure.PureReactiveSystem;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.apache.commons.io.FileUtils;
import org.eclipse.collections.api.factory.Lists;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;

public class ConverterBigrapher extends BaseDocumentationGeneratorSupport {

    String firstPrefix = "import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;";

    @Override
    public List<String> acceptedMethods() {
        return Lists.mutable.of("createSignature", "bigrapher_test01");
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
                    String format = String.format(MainDocGenerationRunner.BASE_EXPORT_PATH + "/converter/%s-%d.java", methodNameSimple, cnt);
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

    public void bigrapher_test01() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, IOException {
        // (1) start
        DefaultDynamicSignature sig = createSignature();
        createOrGetBigraphMetaModel(sig);
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig);

        // Initial state
        PureBigraph s0 = builder.createRoot()
                //A{a}.Snd.(M{a, v_a} | Ready.Fun.1);
                .addChild("A", "a").down().addChild("Snd").down()
                .addChild("M", "a").linkToOuter("v_a").addChild("Ready").down().addChild("Fun")
                .top()
                // A{b}.Snd.(M{a, v_b});
                .addChild("A", "b").down().addChild("Snd").down().addChild("M", "a").linkToOuter("v_b")
                .top()
                // Mail.1
                .addChild("Mail")
                .createBigraph();
        // (1) end

        // (2) start
        PureBigraph sndRedex = builder.spawnNewOne().createRoot()
                // A{a0}.Snd.(M{a1, v} | id) | Mail
                .addChild("A", "a0").down().addChild("Snd").down()
                .addChild("M", "a1").linkToOuter("v").addSite()
                .top().addChild("Mail")
                .createBigraph();
        PureBigraph sndReactum = builder.spawnNewOne().createRoot()
                // A{a0} | Mail.(M{a1, v} | id);
                .addChild("A", "a0").addChild("Mail").down()
                .addChild("M", "a1").linkToOuter("v").addSite()
                .createBigraph();
        ParametricReactionRule<PureBigraph> snd = new ParametricReactionRule<>(sndRedex, sndReactum).withLabel("snd");

        PureBigraph readyRedex = builder.spawnNewOne().createRoot()
                // A{a}.Ready | Mail.(M{a, v} | id)
                .addChild("A", "a").down().addChild("Ready").up()
                .addChild("Mail").down().addChild("M", "a").linkToOuter("v").addSite()
                .createBigraph();
        PureBigraphBuilder<DefaultDynamicSignature> readyBuilder = builder.spawnNewOne();
        //A{a} | Mail | {v};
        readyBuilder.createOuterName("v");
        PureBigraph readyReactum = readyBuilder.createRoot().addChild("A", "a").addChild("Mail")
                .createBigraph();
        ParametricReactionRule<PureBigraph> ready = new ParametricReactionRule<>(readyRedex, readyReactum)
                .withLabel("ready");

        PureBigraph lambdaRedex = builder.spawnNewOne().createRoot().addChild("A", "a").down().addChild("Fun").createBigraph();
        PureBigraph lambdaReactum = builder.spawnNewOne().createRoot().addChild("A", "a").createBigraph();
        ParametricReactionRule<PureBigraph> lambda = new ParametricReactionRule<>(lambdaRedex, lambdaReactum).withLabel("lambda");

        PureBigraph newRedex = builder.spawnNewOne().createRoot()
                //A{a0}.(New.(A'{a1} | id) | id)
                .addChild("A", "a0").down()
                .addChild("New").down()
                .addChild("A'", "a1").down().addSite().up() // (!) explicitly adding a site here, ohterwise BigraphER encoding will be not correct
                .addSite().up().addSite().createBigraph();
        PureBigraph newReactum = builder.spawnNewOne().createRoot()
                // A{a0}.(id | id) | A{a1}.(id | id)
                .addChild("A", "a0").down().addSite().addSite().up()
                .addChild("A", "a1").down().addSite().addSite().createBigraph();
        InstantiationMap instMap = InstantiationMap.create(newReactum.getSites().size())
                .map(0, 1).map(1, 2).map(2, 0).map(3, 2);
        ParametricReactionRule<PureBigraph> newRR = new ParametricReactionRule<>(newRedex, newReactum, instMap).withLabel("new");
        // (2) end

        // (3) start
        ReactiveSystemPredicate<PureBigraph> phi = SubBigraphMatchPredicate.create(
                builder.spawnNewOne().createRoot()
                        .addChild("Mail").down().addChild("M", "a").linkToOuter("v").addSite().top().createBigraph()
        ).withLabel("phi");
        // (3) end

        // (4) start
        PureReactiveSystem rs = new PureReactiveSystem();
        rs.setAgent(s0);
        rs.addReactionRule(snd);
        rs.addReactionRule(ready);
        rs.addReactionRule(lambda);
        rs.addReactionRule(newRR);
        rs.addPredicate(phi);

        BigrapherTransformator transformator = new BigrapherTransformator();
        transformator.toOutputStream(rs, System.out);
        // (4) end
    }

    private static DefaultDynamicSignature createSignature() {
        // (1) start
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl("A", 1).assign()
                .newControl("A'", 1).assign()
                .newControl("Mail", 0).assign()
                .newControl("M", 2).status(ControlStatus.ATOMIC).assign()
                .newControl("Snd", 0).assign()
                .newControl("Ready", 0).assign()
                .newControl("New", 0).assign()
                .newControl("Fun", 0).assign();
        return defaultBuilder.create();
        // (1) end
    }
}
