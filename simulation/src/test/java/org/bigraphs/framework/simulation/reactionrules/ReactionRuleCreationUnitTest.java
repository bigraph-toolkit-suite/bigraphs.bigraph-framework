/*
 * Copyright (c) 2021-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.reactionrules;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

import java.io.IOException;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.AbstractReactionRule;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class ReactionRuleCreationUnitTest {

    private DynamicSignature sig() {
        return pureSignatureBuilder()
                .add("Person", 2)
                .add("Room", 2)
                .add("User", 2)
                .add("Computer", 2)
                .create()
                ;
    }

    @Test
    @DisplayName("Create a rule whose Redex has 2 Sites and its Reactum has 1 Site")
    void create_rule_01() throws InvalidReactionRuleException, IOException {
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();

        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(sig());

        builderRedex.root().child("Room").down().site().child("Person").down().site().up().up().child("Person");

        builderReactum.root().child("Room").down().site().child("Person");

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex, System.out);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum, System.out);
        ParametricReactionRule<PureBigraph> pureBigraphParametricReactionRule = new ParametricReactionRule<>(redex, reactum);
        AbstractReactionRule.ReactiveSystemBoundReactionRule<PureBigraph> rrBounded = pureBigraphParametricReactionRule.withReactiveSystem(reactiveSystem);
        Assertions.assertTrue(rrBounded.isRedexSimple());
    }
}
