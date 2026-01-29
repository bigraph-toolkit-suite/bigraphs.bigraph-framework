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
package org.bigraphs.framework.converter.bigmc;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.bigraphs.framework.converter.PureReactiveSystemStub;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class BigMcTransformationUnitTest {

    private static final String DUMP_TARGET = "src/test/resources/dump/";

    private static DynamicSignature sig() {
        return pureSignatureBuilder()
                .add("Age", 0)
                .add("S", 0)
                .add("Z", 0)
                .add("True", 1)
                .add("False", 0)
                .add("Left", 0)
                .add("Right", 0)
                .create()
                ;
    }

    /**
     * bigmc -p ./couting.bgm
     */
    @Test
    void convert() throws InvalidReactionRuleException, IOException {
        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();

        PureBigraph agent_a = createAgent_A(3, 4);
        ReactionRule<PureBigraph> rr_1 = createReactionRule_1();

        reactiveSystem.setAgent(agent_a);
        reactiveSystem.addReactionRule(rr_1);
        reactiveSystem.addReactionRule(createReactionRule_2());
        reactiveSystem.addReactionRule(createReactionRule_3());

        BigMcTransformator prettyPrinter = new BigMcTransformator();
        String s = prettyPrinter.toString(reactiveSystem);
        System.out.println(s);

        FileOutputStream fout = new FileOutputStream(new File(DUMP_TARGET + "couting.bgm"));
        prettyPrinter.toOutputStream(reactiveSystem, fout);
        fout.close();
    }

    public static PureBigraph createAgent_A(final int left, final int right) throws ControlIsAtomicException {
        DynamicSignature signature = sig();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        PureBigraphBuilder<DynamicSignature>.Hierarchy leftNode =
                builder.hierarchy(signature.getControlByName("Left"))
                        .child("S");
        PureBigraphBuilder<DynamicSignature>.Hierarchy rightNode =
                builder.hierarchy(signature.getControlByName("Right"))
                        .child("S");
        for (int i = 0; i < left - 1; i++) {
            leftNode = leftNode.down().child("S");
        }
        leftNode = leftNode.down().child("Z").top();
        for (int i = 0; i < right - 1; i++) {
            rightNode = rightNode.down().child("S");
        }
        rightNode = rightNode.down().child("Z").top();

        builder.root()
                .child("Age")
                .down()
                .child(leftNode)
                .child(rightNode)
        ;
        builder.makeGround();
        return builder.create();
    }

    public static ReactionRule<PureBigraph> createReactionRule_1() throws ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = sig();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root()
                .child("Left").down().child("S").down().site()
                .top()
                .child("Right").down().child("S").down().site()
        ;
        builder2.root()
                .child("Left").down().site()
                .top()
                .child("Right").down().site()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    /**
     * react r2 = Left.Z | Right.S -> True;
     */
    public static ReactionRule<PureBigraph> createReactionRule_2() throws ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = sig();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root()
                .child("Left").down().child("Z")
                .top()
                .child("Right").down().child("S").down().site()
        ;
        builder2.root()
                .child("True").down().site()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    /**
     * react r3 = Left | Right.Z -> False;
     */
    public static ReactionRule<PureBigraph> createReactionRule_3() throws ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = sig();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root()
                .child("Left").down().site()
                .top()
                .child("Right").down().child("Z")
        ;
        builder2.root()
                .child("False").down().site()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }
}
