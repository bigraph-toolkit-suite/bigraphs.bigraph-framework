package org.bigraphs.framework.converter.bigrapher;

import org.bigraphs.framework.converter.PureReactiveSystemStub;
import org.bigraphs.framework.converter.ReactiveSystemPredicateStub;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.InstantiationMap;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystemPredicate;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

public class ReactionRuleInstantiationMapUnitTests {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/bigrapher/";

    // Example from: https://uog-bigraph.bitbucket.io/actors.html
    // bigrapher full -v -s ./states -t trans.svg -f svg,json -M 20 test-actors.big
    // bigrapher sim -v -s ./states -t trans.svg -f svg,json  actors.big
    @Test
    void instantiation_rule_test() throws InvalidConnectionException, TypeNotExistsException, IOException, InvalidReactionRuleException {
        DefaultDynamicSignature sig = createSignature2();
        createOrGetBigraphMetaModel(sig);
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig);

        // Initial state
        PureBigraph s0 = builder.createRoot()
                //A{a}.Snd.(M{a, v_a} | Ready.Fun.1);
                .addChild("A", "a").down().addChild("Snd").down()
                .addChild("M", "a").linkToOuter("v_a").addChild("Ready").down().addChild("Fun")
                .top()
                // A{b}.Snd.(M{a, v_b});
                .addChild("A", "b").down().addChild("Snd").down().addChild("M", "a").linkToOuter("v_b")
                .top()
                // Mail.1
                .addChild("Mail")
                .createBigraph();
        BigraphGraphvizExporter.toPNG(s0,
                true,
                new File(TARGET_DUMP_PATH + "s0.png")
        );

        PureBigraph sndRedex = builder.spawnNewOne().createRoot()
                // A{a0}.Snd.(M{a1, v} | id) | Mail
                .addChild("A", "a0").down().addChild("Snd").down()
                .addChild("M", "a1").linkToOuter("v").addSite()
                .top().addChild("Mail")
                .createBigraph();
        PureBigraph sndReactum = builder.spawnNewOne().createRoot()
                // A{a0} | Mail.(M{a1, v} | id);
                .addChild("A", "a0").addChild("Mail").down()
                .addChild("M", "a1").linkToOuter("v").addSite()
                .createBigraph();
        ParametricReactionRule<PureBigraph> snd = new ParametricReactionRule<>(sndRedex, sndReactum).withLabel("snd");
        BigraphGraphvizExporter.toPNG(sndRedex, true, new File(TARGET_DUMP_PATH + "sndRedex.png"));
        BigraphGraphvizExporter.toPNG(sndReactum, true, new File(TARGET_DUMP_PATH + "sndReactum.png"));

        PureBigraph readyRedex = builder.spawnNewOne().createRoot()
                // A{a}.Ready | Mail.(M{a, v} | id)
                .addChild("A", "a").down().addChild("Ready").up()
                .addChild("Mail").down().addChild("M", "a").linkToOuter("v").addSite()
                .createBigraph();
        PureBigraphBuilder<DefaultDynamicSignature> readyBuilder = builder.spawnNewOne();
        //A{a} | Mail | {v};
        readyBuilder.createOuterName("v");
        PureBigraph readyReactum = readyBuilder.createRoot().addChild("A", "a").addChild("Mail")
                .createBigraph();
        ParametricReactionRule<PureBigraph> ready = new ParametricReactionRule<>(readyRedex, readyReactum)
                .withLabel("ready");
        BigraphGraphvizExporter.toPNG(readyRedex, true, new File(TARGET_DUMP_PATH + "readyRedex.png"));
        BigraphGraphvizExporter.toPNG(readyReactum, true, new File(TARGET_DUMP_PATH + "readyReactum.png"));


        PureBigraph lambdaRedex = builder.spawnNewOne().createRoot().addChild("A", "a").down().addChild("Fun").createBigraph();
        PureBigraph lambdaReactum = builder.spawnNewOne().createRoot().addChild("A", "a").createBigraph();
        ParametricReactionRule<PureBigraph> lambda = new ParametricReactionRule<>(lambdaRedex, lambdaReactum).withLabel("lambda");
        BigraphGraphvizExporter.toPNG(lambdaRedex, true, new File(TARGET_DUMP_PATH + "lambdaRedex.png"));
        BigraphGraphvizExporter.toPNG(lambdaReactum, true, new File(TARGET_DUMP_PATH + "lambdaReactum.png"));

        PureBigraph newRedex = builder.spawnNewOne().createRoot()
                //A{a0}.(New.(A'{a1} | id) | id)
                .addChild("A", "a0").down()
                .addChild("New").down()
                .addChild("A'", "a1").down().addSite().up() // (!) explicitly adding a site here, ohterwise BigraphER encoding will be not correct
                .addSite().up().addSite().createBigraph();
        PureBigraph newReactum = builder.spawnNewOne().createRoot()
                // A{a0}.(id | id) | A{a1}.(id | id)
                .addChild("A", "a0").down().addSite().addSite().up()
                .addChild("A", "a1").down().addSite().addSite().createBigraph();
        InstantiationMap instMap = InstantiationMap.create(newReactum.getSites().size())
                .map(0, 1).map(1, 2).map(2, 0).map(3, 2);
        ParametricReactionRule<PureBigraph> newRR = new ParametricReactionRule<>(newRedex, newReactum, instMap).withLabel("new");
        BigraphGraphvizExporter.toPNG(newRedex, true, new File(TARGET_DUMP_PATH + "newRedex.png"));
        BigraphGraphvizExporter.toPNG(newReactum, true, new File(TARGET_DUMP_PATH + "newReactum.png"));

        ReactiveSystemPredicate<PureBigraph> phi = new ReactiveSystemPredicateStub(
                builder.spawnNewOne().createRoot()
                        .addChild("Mail").down().addChild("M", "a").linkToOuter("v").addSite().top().createBigraph()
        ).withLabel("phi");


        PureReactiveSystemStub rs = new PureReactiveSystemStub();
        rs.setAgent(s0);
        rs.addReactionRule(snd);
        rs.addReactionRule(ready);
        rs.addReactionRule(lambda);
        rs.addReactionRule(newRR);
        rs.addPredicate(phi);

        BigrapherTransformator transformator = new BigrapherTransformator();
//        transformator.toOutputStream(rs, System.out);
        transformator.toOutputStream(rs, new FileOutputStream(TARGET_DUMP_PATH + "test-actors.big"));
    }

    // bigrapher full -v -s ./states -t trans.svg -f svg,json -M 3 test1.big
    @Test
    void closed_link_test() throws InvalidConnectionException, InvalidReactionRuleException, IOException {
        DefaultDynamicSignature sig = createSignature();
        createOrGetBigraphMetaModel(sig);

        // Create agent
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig);
        builder.createRoot()
                .addChild("Room").down().connectByEdge("User", "Laptop")
                .addChild("User", "openNetwork")
                .addChild("Laptop", "openNetwork");
        PureBigraph big = builder.createBigraph();
        BigraphGraphvizExporter.toPNG(big,
                false,
                new File(TARGET_DUMP_PATH + "big.png")
        );
        // Create Rule 1: Add User
        PureBigraph redex = builder.spawnNewOne()
                .createRoot()
                .addChild("Room").down().addSite()
                .createBigraph();
        PureBigraph reactum = builder.spawnNewOne()
                .createRoot()
                .addChild("Room").down().addSite()
                .addChild("User")
                .createBigraph();
        ParametricReactionRule<PureBigraph> rr1 = new ParametricReactionRule<>(redex, reactum);

        PureBigraph reactum2 = builder.spawnNewOne()
                .createRoot()
                .addChild("Room").down().addSite()
                .addChild("Laptop")
                .createBigraph();
        ParametricReactionRule<PureBigraph> rr2 = new ParametricReactionRule<>(redex, reactum2);

        PureBigraph redex3 = builder.spawnNewOne()
                .createRoot()
                .addChild("User")
                .addChild("Laptop")
                .createBigraph();
        PureBigraph reactum3 = builder.spawnNewOne().createRoot()
                .connectByEdge("User", "Laptop")
                .createBigraph();
        ParametricReactionRule<PureBigraph> rr3 = new ParametricReactionRule<>(redex3, reactum3);

        PureReactiveSystemStub rs = new PureReactiveSystemStub();
        rs.setAgent(big);
        rs.addReactionRule(rr1);
        rs.addReactionRule(rr2);
        rs.addReactionRule(rr3);

        BigrapherTransformator transformator = new BigrapherTransformator();
        transformator.toOutputStream(rs, System.out);
        transformator.toOutputStream(rs, new FileOutputStream(TARGET_DUMP_PATH + "test1.big"));
    }

    private static DefaultDynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl("User", 1).assign()
                .newControl("Room", 1).assign()
                .newControl("Laptop", 1).assign()
        ;
        return defaultBuilder.create();
    }

    private static DefaultDynamicSignature createSignature2() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl("A", 1).assign()
                .newControl("A'", 1).assign()
                .newControl("Mail", 0).assign()
                .newControl("M", 2).status(ControlStatus.ATOMIC).assign()
                .newControl("Snd", 0).assign()
                .newControl("Ready", 0).assign()
                .newControl("New", 0).assign()
                .newControl("Fun", 0).assign()
        ;
        return defaultBuilder.create();
    }
}
