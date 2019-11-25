package de.tudresden.inf.st.bigraphs.converter.bigmc;

import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidArityOfControlException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.impl.SimpleReactiveSystem;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Dominik Grzelak
 */
public class BigMcTransformationUnitTest {
    private static PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();

    /**
     *
     * bigmc -p ./couting.bgm
     */
    @Test
    void name() throws InvalidConnectionException, LinkTypeNotExistsException, InvalidReactionRuleException, IOException {
        SimpleReactiveSystem reactiveSystem = new SimpleReactiveSystem();

        PureBigraph agent_a = createAgent_A(3, 4);
        ReactionRule<PureBigraph> rr_1 = createReactionRule_1();

        reactiveSystem.setAgent(agent_a);
        reactiveSystem.addReactionRule(rr_1);
        reactiveSystem.addReactionRule(createReactionRule_2());
        reactiveSystem.addReactionRule(createReactionRule_3());

        BigMcTransformator prettyPrinter = new BigMcTransformator();
        String s = prettyPrinter.toString(reactiveSystem);
        System.out.println(s);

        FileOutputStream fout = new FileOutputStream(new File("/home/dominik/git/BigraphFramework/converter/src/test/resources/dump/couting.bgm"));
        prettyPrinter.toOutputStream(reactiveSystem, fout);
        fout.close();
    }

    public static PureBigraph createAgent_A(final int left, final int right) throws ControlIsAtomicException, InvalidArityOfControlException, LinkTypeNotExistsException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy leftNode =
                builder.newHierarchy(signature.getControlByName("Left"))
                        .addChild("S");
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy rightNode =
                builder.newHierarchy(signature.getControlByName("Right"))
                        .addChild("S");
        for (int i = 0; i < left - 1; i++) {
            leftNode = leftNode.withNewHierarchy().addChild("S");
        }
        leftNode = leftNode.withNewHierarchy().addChild("Z").top();
        for (int i = 0; i < right - 1; i++) {
            rightNode = rightNode.withNewHierarchy().addChild("S");
        }
        rightNode = rightNode.withNewHierarchy().addChild("Z").top();

        builder.createRoot()
                .addChild("Age")
                .withNewHierarchy()
                .addChild(leftNode)
                .addChild(rightNode)
//                .addChild(s.top())
        ;
        builder.makeGround();
        return builder.createBigraph();
    }

    public static ReactionRule<PureBigraph> createReactionRule_1() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild("Left").withNewHierarchy().addChild("S").withNewHierarchy().addSite()
                .top()
                .addChild("Right").withNewHierarchy().addChild("S").withNewHierarchy().addSite()
        ;
        builder2.createRoot()
                .addChild("Left").withNewHierarchy().addSite()
                .top()
                .addChild("Right").withNewHierarchy().addSite()
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
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild("Left").withNewHierarchy().addChild("Z")
                .top()
                .addChild("Right").withNewHierarchy().addChild("S").withNewHierarchy().addSite()
        ;
        builder2.createRoot()
                .addSite()
                .addSite()
                .addChild("True").withNewHierarchy().addSite()
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
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild("Left").withNewHierarchy().addSite()
                .top()
                .addChild("Right").withNewHierarchy().addChild("Z")
        ;
        builder2.createRoot()
                .addChild("False").withNewHierarchy().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }


    private static <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder defaultBuilder = factory.createSignatureBuilder();
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
