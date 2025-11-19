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
package org.bigraphs.framework.simulation.examples;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.TrackingMap;
import org.bigraphs.framework.simulation.BigraphUnitTestSupport;
import org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AttributeReactionUnitTest implements BigraphUnitTestSupport {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/attributes/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }
    @Test
    void attribute_preservation_reaction() throws InvalidReactionRuleException {
        DynamicSignature sig = pureSignatureBuilder()
                .add("Place", 0)
                .add("Token", 0)
                .create();
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(sig);
        PureBigraph bigraph = b.root()
                .child("Place").down().child("Token").up()
                .child("Place")
                .create();
        eb(bigraph, TARGET_DUMP_PATH + "s_0");
        BigraphEntity.NodeEntity<DynamicControl> v1 = bigraph.getNodes().stream()
                .filter(x -> x.getName().equals("v1")).findAny().get();
        Map<String, Object> attributes = v1.getAttributes();
        attributes.put("ip", "192.168.0.1");
        v1.setAttributes(attributes);
        System.out.println(attributes);

        PureBigraphBuilder<DynamicSignature> bRedex = pureBuilder(sig);
        PureBigraphBuilder<DynamicSignature> bReactum = pureBuilder(sig);
        bRedex.root().child("Place").down().child("Token").up()
                .child("Place");
        bReactum.root().child("Place")
                .child("Place").down().child("Token").up();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(bRedex.create(), bReactum.create())
                .withLabel("swapRule");
        // important for tracing nodes through reactions, thus, to correctly preserve attributes
        TrackingMap eta = new TrackingMap();
        eta.put("v0", "v0");
        eta.put("v1", "v2");
        eta.put("v2", "v1");
        // assign the tracking map to the rule
        rr.withTrackingMap(eta);
        eb(rr.getRedex(), TARGET_DUMP_PATH + "rr_LHS");
        eb(rr.getReactum(), TARGET_DUMP_PATH + "rr_RHS");

        // build a reactive system
        PureReactiveSystem rs = new PureReactiveSystem();
        rs.setAgent(bigraph);
        rs.addReactionRule(rr);
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(bigraph, rr);
        Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
        int transition = 1;
        while (iterator.hasNext()) {
            BigraphMatch<PureBigraph> next = iterator.next();
            PureBigraph result = rs.buildParametricReaction(bigraph, next, rr);
            eb(result, TARGET_DUMP_PATH + "s_" + transition);
            transition++;
            Map<String, Object> attr = result.getNodes().stream()
                    .filter(x -> x.getName().equals("v1")).findAny().get().getAttributes();
            System.out.println(attr);
        }


//        ModelCheckingOptions modOpts = ModelCheckingOptions.create().and(transitionOpts().setMaximumTransitions(10).create());
//        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
//                reactiveSystem,
//                BigraphModelChecker.SimulationStrategy.Type.BFS,
//                modOpts);
//        modelChecker.setReactiveSystemListener(somewhereModality);
//        modelChecker.execute();
    }

    private static DynamicSignature createExampleSignature() {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder
                .add("Place", 0)
                .add("Token", 0)
        ;
        return signatureBuilder.create();
    }
}
