/*
 * Copyright (c) 2022-2025 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.simulation.matching;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystemPredicate;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.junit.jupiter.api.Test;

public class TemporalOperatorUnitTest implements AbstractUnitTestSupport {

    @Test
    void somewhereModality_test_00() throws ReactiveSystemException, BigraphSimulationException, InvalidReactionRuleException {
        // Create Pred.
        PureBigraph agent = createAgent();
        ReactionRule<PureBigraph> add = addItemRR();
        SubBigraphMatchPredicate<PureBigraph> predicate = SubBigraphMatchPredicate.create(createPredicate());
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(add);
        reactiveSystem.addPredicate(predicate);
        SomewhereModalityImpl somewhereModality = new SomewhereModalityImpl(predicate);

        ModelCheckingOptions modOpts = ModelCheckingOptions.create()
                .and(transitionOpts().setMaximumTransitions(10).create());

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                modOpts);
        modelChecker.setReactiveSystemListener(somewhereModality);
        modelChecker.execute();

        assert somewhereModality.predicateMatched;

    }

    private PureBigraph createPredicate() {
        return pureBuilder(createSignature()).root()
                .child("Container").down()
                .child("Item").child("Item").child("Item").create();
    }

    private ReactionRule<PureBigraph> addItemRR() throws InvalidReactionRuleException {
        PureBigraph redex = pureBuilder(createSignature())
                .root().child("Container").down().site().create();
        PureBigraph reactum = pureBuilder(createSignature())
                .root().child("Container").down().child("Item").site().create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    private PureBigraph createAgent() {
        return pureBuilder(createSignature())
                .root()
                .child("Container").create();
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

    private static DynamicSignature createSignature() {
        return pureSignatureBuilder()
                .add("Container", 0)
                .add("Item", 0)
                .create();
    }
}
