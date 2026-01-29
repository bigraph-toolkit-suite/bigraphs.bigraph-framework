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

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

import java.io.File;
import java.io.IOException;
import org.bigraphs.framework.converter.PureReactiveSystemStub;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.Test;

/**
 * This class models a generic path-finding problem and converts it to a bigraph for BigraphER.
 * <p>
 * The BigraphER toolchain can be used to simulate and visualize the system.
 *
 * @author Dominik Grzelak
 */
public class RouteFinding {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/cars/";

    private static DynamicSignature sig() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add("Car", 1)
                .add("Fuel", 0)
                .add("Place", 1)
                .add("Road", 1)
                .add("Target", 1)
        ;
        return defaultBuilder.create();
    }

    /**
     * Save output of conversion to {@code model.big}.
     * Command to simulate model with BigraphER:
     * <p>
     * bigrapher full -d ./cars -f svg -s states -M 10 -t trans.svg -v carsconverted.big
     *
     * @throws InvalidConnectionException
     * @throws LinkTypeNotExistsException
     * @throws IOException
     * @throws InvalidReactionRuleException
     */
    @Test
    void convert() throws InvalidConnectionException, TypeNotExistsException, IOException, InvalidReactionRuleException {
        PureBigraph map = createMap(8);

        BigraphGraphvizExporter.toPNG(map,
                true,
                new File(TARGET_DUMP_PATH + "map_car.png")
        );

        ReactionRule<PureBigraph> reactionRule = createReactionRule();
        BigraphGraphvizExporter.toPNG(reactionRule.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "redex_car.png")
        );
        BigraphGraphvizExporter.toPNG(reactionRule.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "reactum_car.png")
        );

        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();
        reactiveSystem.addReactionRule(reactionRule);
        reactiveSystem.setAgent(map);

        BigrapherTransformator prettyPrinter = new BigrapherTransformator();
        String s = prettyPrinter.toString(reactiveSystem);
        System.out.println(s);
    }

    private PureBigraph createMap(int fuelLevel) throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());

        BigraphEntity.OuterName p0 = builder.createOuter("p0");
        BigraphEntity.OuterName p1 = builder.createOuter("p1");
        BigraphEntity.OuterName p2 = builder.createOuter("p2");
        BigraphEntity.OuterName p3 = builder.createOuter("p3");
        BigraphEntity.OuterName p4 = builder.createOuter("p4");
        BigraphEntity.OuterName p5 = builder.createOuter("p5");
        BigraphEntity.OuterName p7 = builder.createOuter("p7");
        BigraphEntity.OuterName target = builder.createOuter("target");

        PureBigraphBuilder<DynamicSignature>.Hierarchy car = builder.hierarchy("Car").linkOuter(target);
        for (int i = 0; i < fuelLevel; i++) {
            car = car.child("Fuel");
        }
        builder.root()
                .child("Place").linkOuter(p0).down().child(car).child("Road").linkOuter(p1).child("Road").linkOuter(p3).up()
                .child("Place").linkOuter(p1).down().child("Road").linkOuter(p2).child("Road").linkOuter(p4).up()
                .child("Place").linkOuter(p2).down().child("Road").linkOuter(p5).up()
                .child("Place").linkOuter(p3).down().child("Road").linkOuter(p4).child("Road").linkOuter(p7).up()
                .child("Place").linkOuter(p4).down().child("Road").linkOuter(p5).child("Road").linkOuter(p1).up()
                .child("Place").linkOuter(p7).down().child("Road").linkOuter(p2).child("Target").linkOuter(target).up()
        ;

        return builder.create();
    }

    /**
     * react r1 = Left.S | Right.S -> Left | Right;
     */
    public static ReactionRule<PureBigraph> createReactionRule() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(sig());

        BigraphEntity.OuterName fromD = builder.createOuter("fromD");
        BigraphEntity.OuterName fromS = builder.createOuter("fromS");
        BigraphEntity.OuterName target = builder.createOuter("target");

        PureBigraphBuilder<DynamicSignature>.Hierarchy car = builder.hierarchy("Car").linkOuter(target).site().child("Fuel");
        builder.root()
                .child("Place").linkOuter(fromD).down().site().top()
                .child("Place").linkOuter(fromS).down().child(car).site().child("Road").linkOuter(fromD).top()
        ;

        BigraphEntity.OuterName fromD2 = builder2.createOuter("fromD");
        BigraphEntity.OuterName fromS2 = builder2.createOuter("fromS");
        BigraphEntity.OuterName target2 = builder2.createOuter("target");
        PureBigraphBuilder<DynamicSignature>.Hierarchy car2 = builder2.hierarchy("Car").linkOuter(target2).site();
        builder2.root()
                .child("Place").linkOuter(fromD2).down().site().child(car2).top()
                .child("Place").linkOuter(fromS2).down().child("Road").linkOuter(fromD2).site().top()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }
}
