package org.bigraphs.framework.converter.bigrapher;

import org.bigraphs.framework.converter.PureReactiveSystemStub;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.junit.jupiter.api.Test;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

/**
 * @author Dominik Grzelak
 */
public class MultiOccurrenceExample {
//    private static PureBigraphFactory factory = pure();

    // bigrapher full -d ./model -f svg -s states -M 50 -t trans.svg -v model.big
    @Test
    void simulate() throws TypeNotExistsException, InvalidConnectionException, InvalidReactionRuleException {

        PureBigraph agent = createAgent();
        ReactionRule<PureBigraph> reactionRuleJA = createReactionRuleJA();
        ReactionRule<PureBigraph> reactionRuleJB = createReactionRuleJB();
        ReactionRule<PureBigraph> reactionRuleJAC = createReactionRuleJAC();
        ReactionRule<PureBigraph> reactionRuleJBD = createReactionRuleJBD();
        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(reactionRuleJA);
        reactiveSystem.addReactionRule(reactionRuleJB);
        reactiveSystem.addReactionRule(reactionRuleJAC);
        reactiveSystem.addReactionRule(reactionRuleJBD);

        BigrapherTransformator prettyPrinter = new BigrapherTransformator();
        String s = prettyPrinter.toString(reactiveSystem);
        System.out.println(s);

    }

    PureBigraph createAgent() {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());
        builder.createRoot()
                .addChild("Room")
                .addChild("Room")
        ;
        return builder.createBigraph();
    }

    public ReactionRule<PureBigraph> createReactionRuleJA() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(createSignature());

        builder.createRoot()
                .addChild("Room"); //.withNewHierarchy().addSite().top()
        ;
        builder2.createRoot()
                .addChild("Room").down().addChild("JobA").top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }

    public ReactionRule<PureBigraph> createReactionRuleJB() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(createSignature());

        builder.createRoot()
                .addChild("Room"); //.withNewHierarchy().addSite().top()
        ;
        builder2.createRoot()
                .addChild("Room").down().addChild("JobB").top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }

    public ReactionRule<PureBigraph> createReactionRuleJAC() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(createSignature());

        builder.createRoot()
                .addChild("Room").down().addChild("JobA").top()
        ;
        builder2.createRoot()
                .addChild("Room").down().addChild("JobC").top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }

    public ReactionRule<PureBigraph> createReactionRuleJBD() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(createSignature());

        builder.createRoot()
                .addChild("Room").down().addChild("JobB").top()
        ;
        builder2.createRoot()
                .addChild("Room").down().addChild("JobD").top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }


    private static DefaultDynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("JobA")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("JobB")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("JobC")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("JobD")).arity(FiniteOrdinal.ofInteger(1)).assign()
        ;
        return defaultBuilder.create();
    }
}
