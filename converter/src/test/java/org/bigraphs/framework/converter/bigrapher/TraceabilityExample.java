/*
 * Copyright (c) 2020-2025 Bigraph Toolkit Suite Developers
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

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

import java.io.File;
import java.io.IOException;
import org.bigraphs.framework.converter.PureReactiveSystemStub;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class TraceabilityExample {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/traceability/";

    private DynamicSignature sig() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add("Person", 1)
                .add("Room", 1)
                .add("User", 1)
                .add("Computer", 1)
        ;

        return defaultBuilder.create();
    }

    @Test
    void convert() throws InvalidConnectionException, IOException, InvalidReactionRuleException {
        PureBigraph agent = createAgent();
        BigraphGraphvizExporter.toPNG(agent,
                true,
                new File(TARGET_DUMP_PATH + "agent.png")
        );

        ReactionRule<PureBigraph> rr1 = moveRoom();
        ReactionRule<PureBigraph> rr2 = connectToComputer();
        BigraphGraphvizExporter.toPNG(rr1.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "redex1.png")
        );
        BigraphGraphvizExporter.toPNG(rr1.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "reactum1.png")
        );
        BigraphGraphvizExporter.toPNG(rr2.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "redex2.png")
        );
        BigraphGraphvizExporter.toPNG(rr2.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "reactum2.png")
        );

        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(rr1);
        reactiveSystem.addReactionRule(rr2);

        BigrapherTransformator prettyPrinter = new BigrapherTransformator();
        String s = prettyPrinter.toString(reactiveSystem);
        System.out.println(s);
    }

    private PureBigraph createAgent() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());
        builder.root()
                .child("Room").down().child("Computer", "link").up()
                .child("Person")
        ;
        return builder.create();
    }

    private ReactionRule<PureBigraph> moveRoom() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(sig());

        builderRedex.root().child("Room").down().site().up().child("Person");

        builderReactum.root().child("Room").down().site().child("Person");

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    private ReactionRule<PureBigraph> connectToComputer() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(sig());

        builderRedex.root().child("Room").down().child("Computer", "link").child("Person");

        builderReactum.root().child("Room").down().child("Computer", "link").child("Person", "link");

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        return new ParametricReactionRule<>(redex, reactum);
    }
}
