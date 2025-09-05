package org.bigraphs.framework.converter.bigrapher;

import org.bigraphs.framework.converter.PureReactiveSystemStub;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.InstantiationMap;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

//Example from: Christoph Luckeneder
public class RoomExample {

    @Test
    public void test() throws InvalidConnectionException, InvalidReactionRuleException, IncompatibleInterfaceException, IOException, TypeNotExistsException {
        DynamicSignature signature = pureSignatureBuilder()
                .newControl("Room", 1).assign()
                .newControl("Door", 1).assign()
                .newControl("Attributes", 0).assign()
                .newControl("Agent", 1).status(ControlStatus.ATOMIC).assign()
                .newControl("Dangerous", 0).status(ControlStatus.ATOMIC).assign()
                .create();

        EMetaModelData metamodelData = EMetaModelData.builder().setName("bigraphMetamodel")
                .setNsPrefix("bigraph").setNsUri("at.ac.tuwien.ict.bigraphs").create();

        createOrGetBigraphMetaModel(signature, metamodelData);
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        PureBigraph roomPlan = builder
                .root()
                .child("Room", "room1").down()
                .child("Agent", "alice").child("Door", "door1").up()
                .child("Room", "room2").down()
                .child("Door", "door1").child("Door", "door2").up()
                .child("Room", "room3").down()
                .child("Door", "door2").child("Door", "door3").up()
                .child("Room", "room4").down()
                .child("Door", "door3").child("Attributes").down()
                .child("Dangerous").up()
                .up()
                .create();

        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();
        reactiveSystem.setAgent(roomPlan);


        PureBigraphBuilder<DynamicSignature> builder2 = builder.spawn();

        PureBigraph redex = builder2.root()
                .child("Room", "r1").down()
                .child("Agent", "name").child("Door", "x").site().up()
                .child("Room", "r2").down()
                .child("Door", "x").site().up()
                .create();


        builder2 = builder.spawn();

        PureBigraph reactum = builder2.root()
                .child("Room", "r1").down()
                .child("Door", "x").site().up()
                .child("Room", "r2").down()
                .child("Agent", "name").child("Door", "x").site().up()
                .create();

        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);

        reactiveSystem.addReactionRule(rr);

        BigrapherTransformator encoder = new BigrapherTransformator();
        String output = encoder.toString(reactiveSystem);
        System.out.println(output);


    }

    // bigrapher full -v -s ./states -t trans.svg -f svg,json -M 20 model2.big
    @Test
    void test_02() throws InvalidReactionRuleException, InvalidConnectionException {
        DynamicSignature signature = pureSignatureBuilder()
                .newControl("Room", 1).assign()
                .newControl("Door", 1).assign()
                .newControl("Attributes", 0).assign()
                .newControl("Agent", 1).status(ControlStatus.ATOMIC).assign()
                .newControl("Dangerous", 0).status(ControlStatus.ATOMIC).assign()
                .create();

        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);


        PureBigraph roomPlan = builder
                .root()
                .child("Room", "room1").down()
                .child("Agent", "alice").child("Door", "door1").up()
                .child("Room", "room2").down()
                .child("Door", "door1").child("Door", "door2").up()
                .child("Room", "room3").down()
                .child("Door", "door2").child("Door", "door3").up()
                .child("Room", "room4").down()
                .child("Door", "door3").child("Attributes").down()
                .child("Dangerous").up()
                .up()
                .create();


        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();
        reactiveSystem.setAgent(roomPlan);


        PureBigraphBuilder<DynamicSignature> builder2 = builder.spawn();

        PureBigraph redex = builder2.root()
                .child("Room", "r1").down()
                .child("Agent", "name").child("Door", "x").site().up()
                .child("Room", "r2").down()
                .child("Door", "x").site().up()
                .create();


        builder2 = builder.spawn();

        PureBigraph reactum = builder2.root()
                .child("Room", "r1").down()
                .child("Door", "x").site().up()
                .child("Room", "r2").down()
                .child("Agent", "name").child("Door", "x").site().up()
                .create();

		InstantiationMap instMap = InstantiationMap.create(reactum.getSites().size())
				.map(0, 0)
				.map(1, 1);
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum, instMap).withLabel("rule1");

        reactiveSystem.addReactionRule(rr);


        BigrapherTransformator encoder = new BigrapherTransformator();
        String output = encoder.toString(reactiveSystem);
        System.out.println(output);
    }
}
