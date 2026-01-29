/*
 * Copyright (c) 2019-2026 Bigraph Toolkit Suite Developers
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
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.junit.jupiter.api.Test;

/**
 * This class demonstrates the use of BRSs
 * to simulate concurrent processes with various phases such as resource registration, process working,
 * and resource deregistration.
 * <p>
 * The BigraphER toolchain can be used to simulate and visualize the system.
 *
 * @author Dominik Grzelak
 */
public class ConcurringProcessesExample {

    private static DynamicSignature sig() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add("Process", 1)
                .add("Token", 1)
                .add("Working", 1)
                .add("Resource", 1)
        ;

        return defaultBuilder.create();
    }

    /**
     * Save output of conversion to {@code model.big}.
     * Command to simulate model with BigraphER:
     * <p>
     * bigrapher full -d ./model -f svg -s states -M 20 -t trans.svg -v model.big
     */
    @Test
    void convert() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraph agent = createAgent();
        ReactionRule<PureBigraph> rule_resourceRegistrationPhase = createRule_ResourceRegistrationPhase();
        ReactionRule<PureBigraph> rule_processWorkingPhase = createRule_ProcessWorkingPhase();
        ReactionRule<PureBigraph> rule_resourceDeregistrationPhase = createRule_ResourceDeregistrationPhase();


        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(rule_resourceRegistrationPhase);
        reactiveSystem.addReactionRule(rule_processWorkingPhase);
        reactiveSystem.addReactionRule(rule_resourceDeregistrationPhase);

        BigrapherTransformator transformator = new BigrapherTransformator();
        String s = transformator.toString(reactiveSystem);
        System.out.println(s);
    }

    PureBigraph createAgent() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());

        builder.root()
                .child("Process", "access1")
                .child("Process", "access2")
                .child("Resource").down().child("Token")
        ;
        return builder.create();
    }

    ReactionRule<PureBigraph> createRule_ResourceRegistrationPhase() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(sig());

        builderRedex.root().child("Process", "access");
        builderRedex.root().child("Resource").down().child("Token");

        builderReactum.root().child("Process", "access");
        builderReactum.root().child("Resource").down().child("Token", "access");


        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    ReactionRule<PureBigraph> createRule_ResourceDeregistrationPhase() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(sig());

        builderRedex.root().child("Process", "access").down().child("Working").top();
        builderRedex.root().child("Resource").down().child("Token", "access");

        builderReactum.root().child("Process", "access");
        builderReactum.root().child("Resource").down().child("Token");

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    ReactionRule<PureBigraph> createRule_ProcessWorkingPhase() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(sig());

        builderRedex.root().child("Process", "access");
        builderRedex.root().child("Resource").down().child("Token", "access");

        builderReactum.root().child("Process", "access").down().child("Working").top();
        builderReactum.root().child("Resource").down().child("Token", "access");

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }
}
