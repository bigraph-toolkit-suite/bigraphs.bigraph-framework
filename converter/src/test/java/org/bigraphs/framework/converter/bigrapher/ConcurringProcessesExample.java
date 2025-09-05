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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

/**
 * @author Dominik Grzelak
 */
public class ConcurringProcessesExample {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/processes/";
//    private static PureBigraphFactory factory = pure();

    @BeforeAll
    static void setUp() throws IOException {
//        File dump = new File(TARGET_DUMP_PATH);
//        dump.mkdirs();
//        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    /**
     * bigrapher full -d ./model -f svg -s states -M 20 -t trans.svg -v model.big
     *
     * @throws InvalidConnectionException
     * @throws IOException
     * @throws InvalidReactionRuleException
     */
    @Test
    void convert_process_example() throws InvalidConnectionException, IOException, InvalidReactionRuleException {
        PureBigraph agent = createAgent();
        ReactionRule<PureBigraph> rule_resourceRegistrationPhase = createRule_ResourceRegistrationPhase();
        ReactionRule<PureBigraph> rule_processWorkingPhase = createRule_ProcessWorkingPhase();
        ReactionRule<PureBigraph> rule_resourceDeregistrationPhase = createRule_ResourceDeregistrationPhase();


        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(rule_resourceRegistrationPhase);
        reactiveSystem.addReactionRule(rule_processWorkingPhase);
        reactiveSystem.addReactionRule(rule_resourceDeregistrationPhase);

        BigrapherTransformator transformator = new BigrapherTransformator();
        String s = transformator.toString(reactiveSystem);
        System.out.println(s);

    }

    PureBigraph createAgent() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignature());

        builder.root()
                .child("Process", "access1")
                .child("Process", "access2")
                .child("Resource").down().child("Token")
        ;
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    ReactionRule<PureBigraph> createRule_ResourceRegistrationPhase() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(createSignature());

        builderRedex.root().child("Process", "access");
        builderRedex.root().child("Resource").down().child("Token");

        builderReactum.root().child("Process", "access");
        builderReactum.root().child("Resource").down().child("Token", "access");


        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    ReactionRule<PureBigraph> createRule_ResourceDeregistrationPhase() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(createSignature());

        builderRedex.root().child("Process", "access").down().child("Working").top();
        builderRedex.root().child("Resource").down().child("Token", "access");

        builderReactum.root().child("Process", "access");
        builderReactum.root().child("Resource").down().child("Token");

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    ReactionRule<PureBigraph> createRule_ProcessWorkingPhase() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(createSignature());

        builderRedex.root().child("Process", "access");
        builderRedex.root().child("Resource").down().child("Token", "access");

        builderReactum.root().child("Process", "access").down().child("Working").top();
        builderReactum.root().child("Resource").down().child("Token", "access");

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private DynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Process")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Token")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Working")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Resource")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("True")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("False")).arity(FiniteOrdinal.ofInteger(0)).assign()
//                .newControl().identifier(StringTypedName.of("Left")).arity(FiniteOrdinal.ofInteger(0)).assign()
//                .newControl().identifier(StringTypedName.of("Right")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;

        return defaultBuilder.create();
    }
}
