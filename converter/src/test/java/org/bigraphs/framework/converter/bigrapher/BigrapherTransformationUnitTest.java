package org.bigraphs.framework.converter.bigrapher;

import org.bigraphs.framework.converter.PureReactiveSystemStub;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * @author Dominik Grzelak
 */
public class BigrapherTransformationUnitTest {
//    private static PureBigraphFactory factory = pure();

    private static final String DUMP_TARGET = "src/test/resources/dump/";

    /**
     * bigrapher full -d ./test -f svg -s states -M 10 -t trans.svg -v test.big
     */
    @Test
    void name() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, IOException {
        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();

        PureBigraph agent_a = createAgent_A();
        ReactionRule<PureBigraph> rr_1 = createReactionRule_1();

        reactiveSystem.setAgent(agent_a);
        reactiveSystem.addReactionRule(rr_1);
//        reactiveSystem.addPredicate(createPredicate());
//        reactiveSystem.addPredicate(createPredicate());

        BigrapherTransformator prettyPrinter = new BigrapherTransformator();
        String s = prettyPrinter.toString(reactiveSystem);
        System.out.println(s);

        FileOutputStream fout = new FileOutputStream(new File(DUMP_TARGET + "test.big"));
        prettyPrinter.toOutputStream(reactiveSystem, fout);
        fout.close();
    }

    public PureBigraph createAgent_A() throws ControlIsAtomicException, InvalidConnectionException, TypeNotExistsException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName jeff1 = builder.createOuterName("jeff1");
        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b = builder.createOuterName("b");
        BigraphEntity.InnerName e1 = builder.createInnerName("e1");

        builder.createRoot()
                .addChild("Printer").linkToOuter(a).linkToOuter(b)
                .addChild(signature.getControlByName("Room")).linkToInner(e1)
                .down()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down().addChild(signature.getControlByName("Job")).up()
                .addChild(signature.getControlByName("User")).linkToOuter(jeff1)
                .up()

                .addChild(signature.getControlByName("Room")).linkToInner(e1)
                .down()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("User")).linkToOuter(jeff2)
                .up().up();

        builder.closeAllInnerNames();
        builder.makeGround();
        return builder.createBigraph();
    }

    public ReactionRule<PureBigraph> createReactionRule_1() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName jeff1 = builder.createOuterName("jeff1");
        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b = builder.createOuterName("b");

        //(Computer{b1}.(Job.1) | User{jeff2}.1) || Computer{b1}.(Job.1 | User{jeff2}.1);

        builder.createRoot()
                .addChild("Printer").linkToOuter(a).linkToOuter(b)
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down().addChild(signature.getControlByName("Job")).up()
                .addChild(signature.getControlByName("User")).linkToOuter(jeff1);

        builder.createRoot()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("User")).linkToOuter(jeff2);

//        builder.makeGround();
        PureBigraph redex = builder.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, redex);
        return rr;
    }

//    public ReactiveSystemPredicate<PureBigraph> createPredicate() throws InvalidConnectionException, TypeNotExistsException {
//        DefaultDynamicSignature signature = createExampleSignature();
//        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
//
//        BigraphEntity.OuterName door = builder.createOuterName("door");
//        BigraphEntity.OuterName user = builder.createOuterName("name:jeff");
//
//        builder.createRoot()
//                .addChild("Room").linkToOuter(door)
//                .down().addSite().addChild("User", user);
//        PureBigraph bigraph = builder.createBigraph();
//        return SubBigraphMatchPredicate.create(bigraph);
//    }


    private static <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(1)).assign();

        return (S) defaultBuilder.create();
    }
}
