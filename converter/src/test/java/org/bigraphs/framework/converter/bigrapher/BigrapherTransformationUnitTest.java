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

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.bigraphs.framework.converter.PureReactiveSystemStub;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class BigrapherTransformationUnitTest {

    private static final String DUMP_TARGET = "src/test/resources/dump/";

    private static DynamicSignature sig() {
        return pureSignatureBuilder()
                .add("Printer", 2)
                .add("Building", 0)
                .add("User", 1)
                .add("Room", 1)
                .add("Spool", 1)
                .add("Computer", 1)
                .add("Job", 0)
                .add("A", 1)
                .add("B", 1)
                .create()
                ;
    }

    /**
     * bigrapher full -d ./test -f svg -s states -M 10 -t trans.svg -v test.big
     */
    @Test
    void convert() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, IOException {
        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();

        PureBigraph agent_a = createAgent_A();
        ReactionRule<PureBigraph> rr_1 = createReactionRule_1();

        reactiveSystem.setAgent(agent_a);
        reactiveSystem.addReactionRule(rr_1);

        BigrapherTransformator prettyPrinter = new BigrapherTransformator();
        String s = prettyPrinter.toString(reactiveSystem);
        System.out.println(s);

        FileOutputStream fout = new FileOutputStream(new File(DUMP_TARGET + "test.big"));
        prettyPrinter.toOutputStream(reactiveSystem, fout);
        fout.close();
    }

    private PureBigraph createAgent_A() throws ControlIsAtomicException, InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = sig();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName jeff1 = builder.createOuter("jeff1");
        BigraphEntity.OuterName jeff2 = builder.createOuter("jeff2");
        BigraphEntity.OuterName b1 = builder.createOuter("b1");
        BigraphEntity.OuterName a = builder.createOuter("a");
        BigraphEntity.OuterName b = builder.createOuter("b");
        BigraphEntity.InnerName e1 = builder.createInner("e1");

        builder.root()
                .child("Printer").linkOuter(a).linkOuter(b)
                .child(signature.getControlByName("Room")).linkInner(e1)
                .down()
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down().child(signature.getControlByName("Job")).up()
                .child(signature.getControlByName("User")).linkOuter(jeff1)
                .up()

                .child(signature.getControlByName("Room")).linkInner(e1)
                .down()
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down().child(signature.getControlByName("Job")).child(signature.getControlByName("User")).linkOuter(jeff2)
                .up().up();

        builder.closeInner();
        builder.makeGround();
        return builder.create();
    }

    private ReactionRule<PureBigraph> createReactionRule_1() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = sig();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName jeff1 = builder.createOuter("jeff1");
        BigraphEntity.OuterName jeff2 = builder.createOuter("jeff2");
        BigraphEntity.OuterName b1 = builder.createOuter("b1");
        BigraphEntity.OuterName a = builder.createOuter("a");
        BigraphEntity.OuterName b = builder.createOuter("b");

        //(Computer{b1}.(Job.1) | User{jeff2}.1) || Computer{b1}.(Job.1 | User{jeff2}.1);

        builder.root()
                .child("Printer").linkOuter(a).linkOuter(b)
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down().child(signature.getControlByName("Job")).up()
                .child(signature.getControlByName("User")).linkOuter(jeff1);

        builder.root()
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down().child(signature.getControlByName("Job")).child(signature.getControlByName("User")).linkOuter(jeff2);

        PureBigraph redex = builder.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, redex);
        return rr;
    }
}
