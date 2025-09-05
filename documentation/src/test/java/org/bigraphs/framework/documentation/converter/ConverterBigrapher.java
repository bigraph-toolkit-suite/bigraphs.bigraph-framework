package org.bigraphs.framework.documentation.converter;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.bigraphs.framework.converter.bigrapher.BigrapherTransformator;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.InstantiationMap;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystemPredicate;
import org.bigraphs.framework.documentation.BaseDocumentationGeneratorSupport;
import org.bigraphs.framework.documentation.MainDocGenerationRunner;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.apache.commons.io.FileUtils;
import org.eclipse.collections.api.factory.Lists;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

public class ConverterBigrapher extends BaseDocumentationGeneratorSupport {

    String firstPrefix = "import static org.bigraphs.framework.core.factory.BigraphFactory.*;";

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
        DynamicSignature sig = createSignature();
        createOrGetBigraphMetaModel(sig);
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig);

        // Initial state
        PureBigraph s0 = builder.root()
                //A{a}.Snd.(M{a, v_a} | Ready.Fun.1);
                .child("A", "a").down().child("Snd").down()
                .child("M", "a").linkOuter("v_a").child("Ready").down().child("Fun")
                .top()
                // A{b}.Snd.(M{a, v_b});
                .child("A", "b").down().child("Snd").down().child("M", "a").linkOuter("v_b")
                .top()
                // Mail.1
                .child("Mail")
                .create();
        // (1) end

        // (2) start
        PureBigraph sndRedex = builder.spawn().root()
                // A{a0}.Snd.(M{a1, v} | id) | Mail
                .child("A", "a0").down().child("Snd").down()
                .child("M", "a1").linkOuter("v").site()
                .top().child("Mail")
                .create();
        PureBigraph sndReactum = builder.spawn().root()
                // A{a0} | Mail.(M{a1, v} | id);
                .child("A", "a0").child("Mail").down()
                .child("M", "a1").linkOuter("v").site()
                .create();
        ParametricReactionRule<PureBigraph> snd = new ParametricReactionRule<>(sndRedex, sndReactum).withLabel("snd");

        PureBigraph readyRedex = builder.spawn().root()
                // A{a}.Ready | Mail.(M{a, v} | id)
                .child("A", "a").down().child("Ready").up()
                .child("Mail").down().child("M", "a").linkOuter("v").site()
                .create();
        PureBigraphBuilder<DynamicSignature> readyBuilder = builder.spawn();
        //A{a} | Mail | {v};
        readyBuilder.createOuter("v");
        PureBigraph readyReactum = readyBuilder.root().child("A", "a").child("Mail")
                .create();
        ParametricReactionRule<PureBigraph> ready = new ParametricReactionRule<>(readyRedex, readyReactum)
                .withLabel("ready");

        PureBigraph lambdaRedex = builder.spawn().root().child("A", "a").down().child("Fun").create();
        PureBigraph lambdaReactum = builder.spawn().root().child("A", "a").create();
        ParametricReactionRule<PureBigraph> lambda = new ParametricReactionRule<>(lambdaRedex, lambdaReactum).withLabel("lambda");

        PureBigraph newRedex = builder.spawn().root()
                //A{a0}.(New.(A'{a1} | id) | id)
                .child("A", "a0").down()
                .child("New").down()
                .child("A'", "a1").down().site().up() // (!) explicitly adding a site here, ohterwise BigraphER encoding will be not correct
                .site().up().site().create();
        PureBigraph newReactum = builder.spawn().root()
                // A{a0}.(id | id) | A{a1}.(id | id)
                .child("A", "a0").down().site().site().up()
                .child("A", "a1").down().site().site().create();
        InstantiationMap instMap = InstantiationMap.create(newReactum.getSites().size())
                .map(0, 1).map(1, 2).map(2, 0).map(3, 2);
        ParametricReactionRule<PureBigraph> newRR = new ParametricReactionRule<>(newRedex, newReactum, instMap).withLabel("new");
        // (2) end

        // (3) start
        ReactiveSystemPredicate<PureBigraph> phi = SubBigraphMatchPredicate.create(
                builder.spawn().root()
                        .child("Mail").down().child("M", "a").linkOuter("v").site().top().create()
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

    private static DynamicSignature createSignature() {
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
