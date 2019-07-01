package de.tudresden.inf.st.bigraphs.rewriting.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.Options;
import de.tudresden.inf.st.bigraphs.rewriting.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.impl.SimpleReactiveSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dominik Grzelak
 */
public class RSTest {
    private static PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = AbstractBigraphFactory.createPureBigraphFactory();

    @Test
    void name() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, InvalidReactionRuleException {
        // Create reaction rules
        PureBigraph agent = (PureBigraph) createAgent_model_test_0();
        ReactionRule<PureBigraph> rr = createReactionRule();

        SimpleReactiveSystem reactiveSystem = new SimpleReactiveSystem();
        reactiveSystem.addReactionRule(rr);
        reactiveSystem.addReactionRule(rr);
        assertEquals(1, reactiveSystem.getReactionRules().size());
        assertTrue(reactiveSystem.isSimple());

        Options opts = Options.create(2);
        reactiveSystem.simulate(agent, opts);

    }

    public static Bigraph createAgent_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        BigraphEntity.OuterName network = builder.createOuterName("network");

        builder.createRoot()
                .addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(network)
        ;

        builder.closeAllInnerNames();
        builder.makeGround();

        return builder.createBigraph();
    }

    /**
     * big rleft = (Room.(Comp{n}.1));
     * big rright = (Room.(Comp{n}.Job.1));
     * <p>
     * react r1 = rleft --> rright;
     */
    public static ReactionRule<PureBigraph> createReactionRule() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);

        BigraphEntity.OuterName network = builder.createOuterName("network");
        builder.createRoot()
                .addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(network)
        ;

        BigraphEntity.OuterName network2 = builder2.createOuterName("network");
        builder2.createRoot()
                .addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(network2)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Job"))
        ;

//        builder.closeAllInnerNames();
//        builder.makeGround();
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private static <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;

        return (S) defaultBuilder.create();
    }

}
