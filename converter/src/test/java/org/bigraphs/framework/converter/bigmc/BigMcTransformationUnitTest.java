package org.bigraphs.framework.converter.bigmc;

import org.bigraphs.framework.converter.PureReactiveSystemStub;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

/**
 * @author Dominik Grzelak
 */
public class BigMcTransformationUnitTest {
//    private static PureBigraphFactory factory = pure();
    private static final String DUMP_TARGET = "src/test/resources/dump/";

    /**
     * bigmc -p ./couting.bgm
     */
    @Test
    void name() throws InvalidConnectionException, LinkTypeNotExistsException, InvalidReactionRuleException, IOException {
        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();

        PureBigraph agent_a = createAgent_A(3, 4);
        ReactionRule<PureBigraph> rr_1 = createReactionRule_1();

        reactiveSystem.setAgent(agent_a);
        reactiveSystem.addReactionRule(rr_1);
        reactiveSystem.addReactionRule(createReactionRule_2());
        reactiveSystem.addReactionRule(createReactionRule_3());

        BigMcTransformator prettyPrinter = new BigMcTransformator();
        String s = prettyPrinter.toString(reactiveSystem);
        System.out.println(s);

        FileOutputStream fout = new FileOutputStream(new File(DUMP_TARGET + "couting.bgm"));
        prettyPrinter.toOutputStream(reactiveSystem, fout);
        fout.close();
    }

    public static PureBigraph createAgent_A(final int left, final int right) throws ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy leftNode =
                builder.hierarchy(signature.getControlByName("Left"))
                        .addChild("S");
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy rightNode =
                builder.hierarchy(signature.getControlByName("Right"))
                        .addChild("S");
        for (int i = 0; i < left - 1; i++) {
            leftNode = leftNode.down().addChild("S");
        }
        leftNode = leftNode.down().addChild("Z").top();
        for (int i = 0; i < right - 1; i++) {
            rightNode = rightNode.down().addChild("S");
        }
        rightNode = rightNode.down().addChild("Z").top();

        builder.createRoot()
                .addChild("Age")
                .down()
                .addChild(leftNode)
                .addChild(rightNode)
//                .addChild(s.top())
        ;
        builder.makeGround();
        return builder.createBigraph();
    }

    public static ReactionRule<PureBigraph> createReactionRule_1() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("Left").down().addChild("S").down().addSite()
                .top()
                .addChild("Right").down().addChild("S").down().addSite()
        ;
        builder2.createRoot()
                .addChild("Left").down().addSite()
                .top()
                .addChild("Right").down().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    /**
     * react r2 = Left.Z | Right.S -> True;
     */
    public static ReactionRule<PureBigraph> createReactionRule_2() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("Left").down().addChild("Z")
                .top()
                .addChild("Right").down().addChild("S").down().addSite()
        ;
        builder2.createRoot()
//                .addSite()
//                .addSite()
                .addChild("True").down().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    /**
     * react r3 = Left | Right.Z -> False;
     */
    public static ReactionRule<PureBigraph> createReactionRule_3() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("Left").down().addSite()
                .top()
                .addChild("Right").down().addChild("Z")
        ;
        builder2.createRoot()
                .addChild("False").down().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }


    private static <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Age")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("S")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Z")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("True")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("False")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Left")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Right")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;

        return (S) defaultBuilder.create();
    }
}
