package org.bigraphs.framework.converter.bigrapher;

import org.bigraphs.framework.converter.PureReactiveSystemStub;
import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.InstantiationMap;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionRule;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;

//Example from: Christoph Luckeneder
public class RoomExample {

    @Test
    public void test() throws InvalidConnectionException, InvalidReactionRuleException, IncompatibleInterfaceException, IOException, TypeNotExistsException {
        DefaultDynamicSignature signature = pureSignatureBuilder()
                .newControl("Room", 1).assign()
                .newControl("Door", 1).assign()
                .newControl("Attributes", 0).assign()
                .newControl("Agent", 1).status(ControlStatus.ATOMIC).assign()
                .newControl("Dangerous", 0).status(ControlStatus.ATOMIC).assign()
                .create();

        EMetaModelData metamodelData = EMetaModelData.builder().setName("bigraphMetamodel")
                .setNsPrefix("bigraph").setNsUri("at.ac.tuwien.ict.bigraphs").create();

        createOrGetBigraphMetaModel(signature, metamodelData);
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        PureBigraph roomPlan = builder
                .createRoot()
                .addChild("Room", "room1").down()
                .addChild("Agent", "alice").addChild("Door", "door1").up()
                .addChild("Room", "room2").down()
                .addChild("Door", "door1").addChild("Door", "door2").up()
                .addChild("Room", "room3").down()
                .addChild("Door", "door2").addChild("Door", "door3").up()
                .addChild("Room", "room4").down()
                .addChild("Door", "door3").addChild("Attributes").down()
                .addChild("Dangerous").up()
                .up()
                .createBigraph();

        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();
        reactiveSystem.setAgent(roomPlan);


        PureBigraphBuilder<DefaultDynamicSignature> builder2 = builder.spawnNewOne();

        PureBigraph redex = builder2.createRoot()
                .addChild("Room", "r1").down()
                .addChild("Agent", "name").addChild("Door", "x").addSite().up()
                .addChild("Room", "r2").down()
                .addChild("Door", "x").addSite().up()
                .createBigraph();


        builder2 = builder.spawnNewOne();

        PureBigraph reactum = builder2.createRoot()
                .addChild("Room", "r1").down()
                .addChild("Door", "x").addSite().up()
                .addChild("Room", "r2").down()
                .addChild("Agent", "name").addChild("Door", "x").addSite().up()
                .createBigraph();

        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);

        reactiveSystem.addReactionRule(rr);

        BigrapherTransformator encoder = new BigrapherTransformator();
        String output = encoder.toString(reactiveSystem);
        System.out.println(output);


    }

    // bigrapher full -v -s ./states -t trans.svg -f svg,json -M 20 model2.big
    @Test
    void test_02() throws InvalidReactionRuleException, InvalidConnectionException {
        DefaultDynamicSignature signature = pureSignatureBuilder()
                .newControl("Room", 1).assign()
                .newControl("Door", 1).assign()
                .newControl("Attributes", 0).assign()
                .newControl("Agent", 1).status(ControlStatus.ATOMIC).assign()
                .newControl("Dangerous", 0).status(ControlStatus.ATOMIC).assign()
                .create();

        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);


        PureBigraph roomPlan = builder
                .createRoot()
                .addChild("Room", "room1").down()
                .addChild("Agent", "alice").addChild("Door", "door1").up()
                .addChild("Room", "room2").down()
                .addChild("Door", "door1").addChild("Door", "door2").up()
                .addChild("Room", "room3").down()
                .addChild("Door", "door2").addChild("Door", "door3").up()
                .addChild("Room", "room4").down()
                .addChild("Door", "door3").addChild("Attributes").down()
                .addChild("Dangerous").up()
                .up()
                .createBigraph();


        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();
        reactiveSystem.setAgent(roomPlan);


        PureBigraphBuilder<DefaultDynamicSignature> builder2 = builder.spawnNewOne();

        PureBigraph redex = builder2.createRoot()
                .addChild("Room", "r1").down()
                .addChild("Agent", "name").addChild("Door", "x").addSite().up()
                .addChild("Room", "r2").down()
                .addChild("Door", "x").addSite().up()
                .createBigraph();


        builder2 = builder.spawnNewOne();

        PureBigraph reactum = builder2.createRoot()
                .addChild("Room", "r1").down()
                .addChild("Door", "x").addSite().up()
                .addChild("Room", "r2").down()
                .addChild("Agent", "name").addChild("Door", "x").addSite().up()
                .createBigraph();

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
