package org.bigraphs.framework.simulation.matching;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.ReactiveSystemException;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionRule;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactiveSystemPredicate;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.junit.jupiter.api.Test;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureBuilder;
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

public class TemporalOperatorUnitTest extends AbstractUnitTestSupport {

    @Test
    void somewhereModality_test_00() throws ReactiveSystemException, BigraphSimulationException, InvalidReactionRuleException {
        // Create Pred.
        PureBigraph agent = createAgent();
        ReactionRule<PureBigraph> add = addItemRR();
        SubBigraphMatchPredicate<PureBigraph> predicate = SubBigraphMatchPredicate.<PureBigraph>create(createPredicate());
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(add);
        reactiveSystem.addPredicate(predicate);
        SomewhereModalityImpl somewhereModality = new SomewhereModalityImpl(predicate);

        ModelCheckingOptions modOpts = ModelCheckingOptions.create().and(transitionOpts().setMaximumTransitions(10).create());
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                modOpts);
        modelChecker.setReactiveSystemListener(somewhereModality);
        modelChecker.execute();

        assert somewhereModality.predicateMatched;

    }

    private PureBigraph createPredicate() {
        return pureBuilder(createSignature()).createRoot()
                .addChild("Container").down()
                .addChild("Item").addChild("Item").addChild("Item").createBigraph();
    }

    private ReactionRule<PureBigraph> addItemRR() throws InvalidReactionRuleException {
        PureBigraph redex = pureBuilder(createSignature())
                .createRoot().addChild("Container").down().addSite().createBigraph();
        PureBigraph reactum = pureBuilder(createSignature())
                .createRoot().addChild("Container").down().addChild("Item").addSite().createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }

    private PureBigraph createAgent() {
        return pureBuilder(createSignature())
                .createRoot()
                .addChild("Container").createBigraph();
    }

    public static class SomewhereModalityImpl implements BigraphModelChecker.ReactiveSystemListener<PureBigraph> {
        boolean predicateMatched = false;
        ReactiveSystemPredicate<PureBigraph> predicate;

        public SomewhereModalityImpl(ReactiveSystemPredicate<PureBigraph> predicate) {
            this.predicate = predicate;
        }

        @Override
        public void onAllPredicateMatched(PureBigraph currentAgent, String label) {
            this.predicateMatched = true; // in case only one predicate is added to a system
        }

        @Override
        public void onPredicateMatched(PureBigraph currentAgent, ReactiveSystemPredicate<PureBigraph> predicate) {
            if (this.predicate == predicate) {
                this.predicateMatched = true;
            }
        }
    }

    private static DefaultDynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Container")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Item")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;
        return defaultBuilder.create();
    }
}
