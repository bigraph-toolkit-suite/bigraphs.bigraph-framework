package org.bigraphs.framework.converter.bigrapher;

import org.bigraphs.framework.converter.PureReactiveSystemStub;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

public class TraceabilityExample {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/traceability/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    @Test
    void test() throws InvalidConnectionException, IOException, InvalidReactionRuleException {
        PureBigraph agent = createAgent();
        BigraphGraphvizExporter.toPNG(agent,
                true,
                new File(TARGET_DUMP_PATH + "agent.png")
        );

        ReactionRule<PureBigraph> rr1 = moveRoom();
        ReactionRule<PureBigraph> rr2 = connectToComputer();
        BigraphGraphvizExporter.toPNG(rr1.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "redex1.png")
        );
        BigraphGraphvizExporter.toPNG(rr1.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "reactum1.png")
        );
        BigraphGraphvizExporter.toPNG(rr2.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "redex2.png")
        );
        BigraphGraphvizExporter.toPNG(rr2.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "reactum2.png")
        );

        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(rr1);
        reactiveSystem.addReactionRule(rr2);

        BigrapherTransformator prettyPrinter = new BigrapherTransformator();
        String s = prettyPrinter.toString(reactiveSystem);
        System.out.println(s);
    }

    PureBigraph createAgent() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignature());
        builder.root()
                .child("Room").down().child("Computer", "link").up()
                .child("Person")
        ;
        return builder.create();
    }

    ReactionRule<PureBigraph> moveRoom() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(createSignature());

        builderRedex.root().child("Room").down().site().up().child("Person");

        builderReactum.root().child("Room").down().site().child("Person");

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    ReactionRule<PureBigraph> connectToComputer() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(createSignature());

        builderRedex.root().child("Room").down().child("Computer", "link").child("Person");

        builderReactum.root().child("Room").down().child("Computer", "link").child("Person", "link");

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    private DynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Person")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
        ;

        return defaultBuilder.create();
    }

}
