/*
 * Copyright (c) 2024-2025 Bigraph Toolkit Suite Developers
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

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.*;
import org.bigraphs.testing.BigraphUnitTestSupport;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class RuleCompositionTest implements BigraphUnitTestSupport {
    private final static String DUMP = "src/test/resources/dump/rulecomposition/";

    private DynamicSignature sig() {
        return pureSignatureBuilder()
                .add("A", 1)
                .add("B", 1)
                .add("C", 1)
                .add("D", 1)
                .create()
                ;
    }

    @Test
    void test_rule_parallel_product_01() throws InvalidReactionRuleException, IncompatibleInterfaceException {

        ReactionRule<PureBigraph> rr01 = createRR_01();
        toPNG(rr01.getRedex(),  "rr01_LHS", DUMP);
        toPNG(rr01.getReactum(),  "rr01_RHS", DUMP);

        ReactionRule<PureBigraph> rr02 = createRR_02();
        toPNG(rr02.getRedex(),  "rr02_LHS", DUMP);
        toPNG(rr02.getReactum(),  "rr02_RHS", DUMP);

        ReactionRuleComposer<ParametricReactionRule<Bigraph<?>>> rComp = new ReactionRuleComposer<>();
        ParametricReactionRule<Bigraph<?>> product = rComp.parallelProduct(rr01, rr02);
        toPNG(product.getRedex(),  "product_LHS", DUMP);
        toPNG(product.getReactum(),  "product_RHS", DUMP);
        System.out.println("Product label = " + product.getLabel());
        assert product.getLabel().equals("R1_PP_R2");

        System.out.println(product.getTrackingMap());
        product.getInstantationMap().getMappings().forEach((k, v) -> {
            System.out.println(k.getValue() + " --> " + v.getValue());
        });
    }

    @Test
    void test_rule_merge_product_01() throws InvalidReactionRuleException, IncompatibleInterfaceException {

        ReactionRule<PureBigraph> rr01 = createRR_01();
        toPNG(rr01.getRedex(),  "rr01_LHS", DUMP);
        toPNG(rr01.getReactum(),  "rr01_RHS", DUMP);

        ReactionRule<PureBigraph> rr02 = createRR_02();
        toPNG(rr02.getRedex(),  "rr02_LHS", DUMP);
        toPNG(rr02.getReactum(),  "rr02_RHS", DUMP);

        ReactionRuleComposer<ParametricReactionRule<Bigraph<?>>> rComp = new ReactionRuleComposer<>();
        ParametricReactionRule<Bigraph<?>> product = rComp.mergeProduct(rr01, rr02);
        toPNG(product.getRedex(), "product_LHS", DUMP);
        toPNG(product.getReactum(), "product_RHS", DUMP);
        System.out.println("Product label = " + product.getLabel());
        assert product.getLabel().equals("R1_PP_R2");

        System.out.println(product.getTrackingMap());
        product.getInstantationMap().getMappings().forEach((k, v) -> {
            System.out.println(k.getValue() + " --> " + v.getValue());
        });
    }

    private ReactionRule<PureBigraph> createRR_01() throws InvalidReactionRuleException {
        DynamicSignature sig = sig();
        PureBigraphBuilder<DynamicSignature> bRedex = pureBuilder(sig);
        PureBigraphBuilder<DynamicSignature> bReactum = pureBuilder(sig);

        bRedex.root()
                .child("A").down()
                /**/.site().top()
        ;
        bReactum.root()
                .child("A").down()
                /**/.site()
                /**/.child("B")
        ;
        PureBigraph redex = bRedex.create();
        PureBigraph reactum = bReactum.create();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        rr.withLabel("R1");
        TrackingMap map = new TrackingMap();
        map.put("v0", "v0");
        map.put("v1", "");
        rr.withTrackingMap(map);
        InstantiationMap iMap = InstantiationMap.create(1);
        iMap.map(0, 0);
        rr.withInstantiationMap(iMap);
        return rr;
    }

    private ReactionRule<PureBigraph> createRR_02() throws InvalidReactionRuleException {
        DynamicSignature sig = sig();
        PureBigraphBuilder<DynamicSignature> bRedex = pureBuilder(sig);
        PureBigraphBuilder<DynamicSignature> bReactum = pureBuilder(sig);

        bRedex.root()
                .child("C").down()
                /**/.child("B").down()
                /**//**/.site().child("C")
        ;

        bReactum.root()
                .child("C").down()
                /**/.child("B").down()
                /**//**/.site().child("C")
        ;

        PureBigraph redex = bRedex.create();
        PureBigraph reactum = bReactum.create();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        rr.withLabel("R2");
        TrackingMap map = new TrackingMap();
        map.put("v0", "v0");
        map.put("v1", "v1");
        map.put("v2", "v2");
        rr.withTrackingMap(map);
        InstantiationMap iMap = InstantiationMap.create(1);
        iMap.map(0, 0);
        rr.withInstantiationMap(iMap);
        return rr;
    }
}
