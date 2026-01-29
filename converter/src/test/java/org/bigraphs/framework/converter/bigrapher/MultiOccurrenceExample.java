/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.converter.bigrapher;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

import org.bigraphs.framework.converter.PureReactiveSystemStub;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class MultiOccurrenceExample {

    private static DynamicSignature sig() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add("Building", 1)
                .add("Room", 1)
                .add("Computer", 1)
                .add("JobA", 1)
                .add("JobB", 1)
                .add("JobC", 1)
                .add("JobD", 1)
        ;
        return defaultBuilder.create();
    }

    /**
     * Save output of conversion to {@code model.big}.
     * Command to simulate model with BigraphER:
     * <p>
     * bigrapher full -d ./model -f svg -s states -M 50 -t trans.svg -v model.big
     *
     */
    @Test
    void convert() throws InvalidReactionRuleException {

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
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());
        builder.root()
                .child("Room")
                .child("Room")
        ;
        return builder.create();
    }

    private ReactionRule<PureBigraph> createReactionRuleJA() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(sig());

        builder.root()
                .child("Room");
        ;
        builder2.root()
                .child("Room").down().child("JobA").top()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    private ReactionRule<PureBigraph> createReactionRuleJB() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(sig());

        builder.root()
                .child("Room");
        ;
        builder2.root()
                .child("Room").down().child("JobB").top()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    private ReactionRule<PureBigraph> createReactionRuleJAC() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(sig());

        builder.root()
                .child("Room").down().child("JobA").top()
        ;
        builder2.root()
                .child("Room").down().child("JobC").top()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    private ReactionRule<PureBigraph> createReactionRuleJBD() throws ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(sig());

        builder.root()
                .child("Room").down().child("JobB").top()
        ;
        builder2.root()
                .child("Room").down().child("JobD").top()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }
}
