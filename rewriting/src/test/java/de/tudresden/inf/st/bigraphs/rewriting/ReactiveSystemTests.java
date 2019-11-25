package de.tudresden.inf.st.bigraphs.rewriting;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
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
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.impl.SimpleReactiveSystem;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.predicates.SubBigraphMatchPredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

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
    private static PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();

//    @Test
//    void simulation_test_wip() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, InvalidReactionRuleException {
//        // Create reaction rules
//        PureBigraph agent = (PureBigraph) createAgent_model_test_0();
//        ReactionRule<PureBigraph> rr = createReactionRule();
//
//        SimpleReactiveSystem reactiveSystem = new SimpleReactiveSystem();
//        reactiveSystem.addReactionRule(rr);
//        reactiveSystem.addReactionRule(rr);
//        assertEquals(1, reactiveSystem.getReactionRules().size());
//        assertTrue(reactiveSystem.isSimple());
//
//        ReactiveSystemOptions opts = ReactiveSystemOptions.create();
//        reactiveSystem.simulate(agent, opts);
//    }


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
                        .setMaximumTime(30)
                        .create()
                )
                .setMeasureTime(true)
                .and(ReactiveSystemOptions.exportOpts()
                        .setTraceFile(new File(completePath.toUri()))
                        .create()
                )
        ;

        SubBigraphMatchPredicate<PureBigraph> pred1 = SubBigraphMatchPredicate.create((PureBigraph) createAgent_A_Final());

        reactiveSystem.computeTransitionSystem(agent, opts, Arrays.asList(pred1));
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Create Reactive System options")
    class ReactiveSystemOptionTests {
        @Test
        void reactionsystem_options_test() {
            ReactiveSystemOptions opts = ReactiveSystemOptions.create();
            opts.and(transitionOpts()
                    .setMaximumTransitions(4)
                    .setMaximumTime(60, TimeUnit.SECONDS)
                    .create()
            ).and(ReactiveSystemOptions.exportOpts()
                    .setOutputStatesFolder(new File(""))
                    .setTraceFile(new File(""))
                    .create()
            );

            // overwrite old settings
            opts.and(transitionOpts()
                    .setMaximumTransitions(5)
                    .setMaximumTime(30, TimeUnit.MILLISECONDS)
                    .create());

            ReactiveSystemOptions.TransitionOptions opts1 = opts.get(ReactiveSystemOptions.Options.TRANSITION);
            assertEquals(opts1.getMaximumTransitions(), 5);
            assertEquals(opts1.getMaximumTimeUnit(), TimeUnit.MILLISECONDS);
            assertEquals(opts1.getMaximumTime(), 30);
        }
    }

    public static Bigraph createAgent_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        BigraphEntity.OuterName network = builder.createOuterName("network");

        builder.createRoot()
                .addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer")).linkToOuter(network)
        ;

        builder.closeAllInnerNames();
        builder.makeGround();

        return builder.createBigraph();
    }

    public static Bigraph createAgent_A() throws ControlIsAtomicException, InvalidConnectionException, LinkTypeNotExistsException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuterName("network");
        builder.createRoot()
                .addChild("Room")
                .withNewHierarchy()
                .addChild("Computer").linkToOuter(network)
        ;
        builder.makeGround();
        return builder.createBigraph();
    }

    public static Bigraph createAgent_A_Final() throws ControlIsAtomicException, InvalidConnectionException, LinkTypeNotExistsException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuterName("network");
        builder.createRoot()
                .addChild("Room")
                .withNewHierarchy()
                .addChild("Computer").linkToOuter(network)
                .withNewHierarchy()
                .addChild("Job").addChild("Job")
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
                .addChild(signature.getControlByName("Computer")).linkToOuter(network)
        ;

        BigraphEntity.OuterName network2 = builder2.createOuterName("network");
        builder2.createRoot()
                .addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer")).linkToOuter(network2)
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
        BigraphEntity.OuterName network = builder.createOuterName("network");
        BigraphEntity.OuterName network2 = builder2.createOuterName("network");
        builder.createRoot()
                .addChild("Room")
                .withNewHierarchy()
                .addChild("Computer").linkToOuter(network)
        ;
        builder2.createRoot()
                .addChild("Room")
                .withNewHierarchy()
                .addChild("Computer").linkToOuter(network2)
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
        BigraphEntity.OuterName network = builder.createOuterName("network");
        BigraphEntity.OuterName network2 = builder2.createOuterName("network");
        builder.createRoot()
                .addChild("Room")
                .withNewHierarchy()
                .addChild("Computer").linkToOuter(network)
        ;
        builder2.createRoot()
                .addChild("Room")
                .withNewHierarchy()
                .addChild("Computer").linkToOuter(network2)
                .withNewHierarchy()
                .addChild("Job")
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
        BigraphEntity.OuterName network = builder.createOuterName("network");
        BigraphEntity.OuterName network2 = builder2.createOuterName("network");
        builder.createRoot()
                .addChild("Room")
                .withNewHierarchy()
                .addChild("Computer").linkToOuter(network)
                .withNewHierarchy()
                .addChild("Job")
        ;
        builder2.createRoot()
                .addChild("Room")
                .withNewHierarchy()
                .addChild("Computer").linkToOuter(network2)
                .withNewHierarchy()
                .addChild("Job").addChild("Job")
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
        DynamicSignatureBuilder defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;

        return (S) defaultBuilder.create();
    }

}
