package de.tudresden.inf.st.bigraphs.rewriting.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Iterator;

public class TestForPaper {
    private PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();

    @Test
    void model_test_0() throws Exception {
        PureBigraph agent_model_test_0 = (PureBigraph) createAgent_model_test_0();
        PureBigraph redex_model_test_0 = (PureBigraph) createRedex_model_test_0();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex_model_test_0, redex_model_test_0);

        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class); //new PureBigraphMatcher();
        MatchIterable match = matcher.match(agent_model_test_0, rr.getRedex());
        Iterator<BigraphMatch<?>> iterator = match.iterator();
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            System.out.println(next);
        }

    }


    public Bigraph createAgent_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

//        BigraphEntity.InnerName roomLink = builder.createInnerName("tmp1_room");
//        BigraphEntity.OuterName a = builder.createOuterName("a");
//        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
//        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");

        builder.createRoot()
                .addChild(signature.getControlByName("A"))
                .addChild(signature.getControlByName("B"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("D"))
                .addChild(signature.getControlByName("E"))
                .goBack()
                .addChild(signature.getControlByName("B"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("D"))
                .addChild(signature.getControlByName("E"))
                .addChild(signature.getControlByName("F"))
                .addChild(signature.getControlByName("G"))
                .goBack()

//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
        ;
        builder.makeGround();

        PureBigraph bigraph = builder.createBigraph();
        return bigraph;

    }


    public Bigraph createRedex_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild(signature.getControlByName("A"))
                .addChild(signature.getControlByName("B"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("D"))
                .addChild(signature.getControlByName("E"))
                .addSite()
        ;
//        builder.makeGround();

        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }


    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("C")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("D")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("E")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("F")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("G")).arity(FiniteOrdinal.ofInteger(1)).assign();

        return (S) defaultBuilder.create();
    }

}
