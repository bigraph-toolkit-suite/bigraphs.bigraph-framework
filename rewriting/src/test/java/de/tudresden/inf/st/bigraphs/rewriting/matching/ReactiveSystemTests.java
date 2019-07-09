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
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystemOptions;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.impl.SimpleReactiveSystem;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.predicates.MatchPredicate;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystemOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dominik Grzelak
 */
public class ReactiveSystemTests {
    private static PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = AbstractBigraphFactory.createPureBigraphFactory();

    @Test
    void simulation_test_wip() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, InvalidReactionRuleException {
        // Create reaction rules
        PureBigraph agent = (PureBigraph) createAgent_model_test_0();
        ReactionRule<PureBigraph> rr = createReactionRule();

        SimpleReactiveSystem reactiveSystem = new SimpleReactiveSystem();
        reactiveSystem.addReactionRule(rr);
        reactiveSystem.addReactionRule(rr);
        assertEquals(1, reactiveSystem.getReactionRules().size());
        assertTrue(reactiveSystem.isSimple());

        ReactiveSystemOptions opts = ReactiveSystemOptions.create();
        reactiveSystem.simulate(agent, opts);
    }

    @Test
    void create_reactionsystem_options_test() {
        ReactiveSystemOptions opts = ReactiveSystemOptions.create();
        opts.and(transitionOpts()
                .setMaximumTransitions(4)
                .setMaximumTime(TimeUnit.SECONDS)
                .create()
        ).and(ReactiveSystemOptions.exportOpts()
                .setOutputStatesFolder(new File(""))
                .setTraceFile(new File(""))
                .create()
        );


        opts.and(transitionOpts()
                .setMaximumTransitions(4)
                .setMaximumTime(TimeUnit.SECONDS)
                .create());

        ReactiveSystemOptions.TransitionOptions opts1 = opts.get(ReactiveSystemOptions.Options.TRANSITION);
        assertEquals(opts1.getMaximumTransitions(), 4);

    }

    @Test
    void create_transition_system_test() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, InvalidReactionRuleException {
        // Create reaction rulesname
        SimpleReactiveSystem reactiveSystem = new SimpleReactiveSystem();
        PureBigraph agent = (PureBigraph) createAgent_A();
        ReactionRule<PureBigraph> rr = createReactionRule_A();
        ReactionRule<PureBigraph> rrsame = createReactionRule_A();
        ReactionRule<PureBigraph> rrSelf = createReactionRule_A_SelfApply();
        ReactionRule<PureBigraph> rr2 = createReactionRule_A2();

        reactiveSystem.addReactionRule(rr);
        reactiveSystem.addReactionRule(rrSelf);
        reactiveSystem.addReactionRule(rr2);
        reactiveSystem.addReactionRule(rrsame);
        assertTrue(reactiveSystem.isSimple());
        Path currentRelativePath = Paths.get("");
        Path completePath = Paths.get(currentRelativePath.toAbsolutePath().toString(), "transition_graph.png");
        ReactiveSystemOptions opts = ReactiveSystemOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(4)
                        .setMaximumTime(TimeUnit.SECONDS)
                        .create()
                )
                .setMeasureTime(true)
                .and(ReactiveSystemOptions.exportOpts()
                        .setTraceFile(new File(completePath.toUri()))
                        .create()
                )
        ;

        MatchPredicate<PureBigraph> pred1 = MatchPredicate.create((PureBigraph) createAgent_A_Final());

        reactiveSystem.computeTransitionSystem(agent, opts, Arrays.asList(pred1));
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

    public static Bigraph createAgent_A() throws ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild("Room")
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer"))
        ;
        builder.makeGround();
        return builder.createBigraph();
    }

    public static Bigraph createAgent_A_Final() throws ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job"))
        ;
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

    public static ReactionRule<PureBigraph> createReactionRule_A_SelfApply() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer"))
        ;
        builder2.createRoot()
                .addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer"))
        ;
//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }


    public static ReactionRule<PureBigraph> createReactionRule_A() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer"))
        ;
        builder2.createRoot()
                .addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Job"))
        ;

//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    public static ReactionRule<PureBigraph> createReactionRule_A2() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Job"))
        ;
        builder2.createRoot()
                .addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job"))
        ;

//        builder.closeAllInnerNames();
        builder.makeGround();
        builder2.makeGround();
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
