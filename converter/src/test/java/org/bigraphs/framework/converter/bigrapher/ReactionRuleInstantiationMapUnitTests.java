package org.bigraphs.framework.converter.bigrapher;

import org.bigraphs.framework.converter.PureReactiveSystemStub;
import org.bigraphs.framework.converter.ReactiveSystemPredicateStub;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
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
        DynamicSignature sig = createSignature2();
        createOrGetBigraphMetaModel(sig);
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig);

        // Initial state
        PureBigraph s0 = builder.root()
                //A{a}.Snd.(M{a, v_a} | Ready.Fun.1);
                .child("A", "a").down().child("Snd").down()
                .child("M", "a").linkOuter("v_a").child("Ready").down().child("Fun")
                .top()
                // A{b}.Snd.(M{a, v_b});
                .child("A", "b").down().child("Snd").down().child("M", "a").linkOuter("v_b")
                .top()
                // Mail.1
                .child("Mail")
                .create();
        BigraphGraphvizExporter.toPNG(s0,
                true,
                new File(TARGET_DUMP_PATH + "s0.png")
        );

        PureBigraph sndRedex = builder.spawn().root()
                // A{a0}.Snd.(M{a1, v} | id) | Mail
                .child("A", "a0").down().child("Snd").down()
                .child("M", "a1").linkOuter("v").site()
                .top().child("Mail")
                .create();
        PureBigraph sndReactum = builder.spawn().root()
                // A{a0} | Mail.(M{a1, v} | id);
                .child("A", "a0").child("Mail").down()
                .child("M", "a1").linkOuter("v").site()
                .create();
        ParametricReactionRule<PureBigraph> snd = new ParametricReactionRule<>(sndRedex, sndReactum).withLabel("snd");
        BigraphGraphvizExporter.toPNG(sndRedex, true, new File(TARGET_DUMP_PATH + "sndRedex.png"));
        BigraphGraphvizExporter.toPNG(sndReactum, true, new File(TARGET_DUMP_PATH + "sndReactum.png"));

        PureBigraph readyRedex = builder.spawn().root()
                // A{a}.Ready | Mail.(M{a, v} | id)
                .child("A", "a").down().child("Ready").up()
                .child("Mail").down().child("M", "a").linkOuter("v").site()
                .create();
        PureBigraphBuilder<DynamicSignature> readyBuilder = builder.spawn();
        //A{a} | Mail | {v};
        readyBuilder.createOuter("v");
        PureBigraph readyReactum = readyBuilder.root().child("A", "a").child("Mail")
                .create();
        ParametricReactionRule<PureBigraph> ready = new ParametricReactionRule<>(readyRedex, readyReactum)
                .withLabel("ready");
        BigraphGraphvizExporter.toPNG(readyRedex, true, new File(TARGET_DUMP_PATH + "readyRedex.png"));
        BigraphGraphvizExporter.toPNG(readyReactum, true, new File(TARGET_DUMP_PATH + "readyReactum.png"));


        PureBigraph lambdaRedex = builder.spawn().root().child("A", "a").down().child("Fun").create();
        PureBigraph lambdaReactum = builder.spawn().root().child("A", "a").create();
        ParametricReactionRule<PureBigraph> lambda = new ParametricReactionRule<>(lambdaRedex, lambdaReactum).withLabel("lambda");
        BigraphGraphvizExporter.toPNG(lambdaRedex, true, new File(TARGET_DUMP_PATH + "lambdaRedex.png"));
        BigraphGraphvizExporter.toPNG(lambdaReactum, true, new File(TARGET_DUMP_PATH + "lambdaReactum.png"));

        PureBigraph newRedex = builder.spawn().root()
                //A{a0}.(New.(A'{a1} | id) | id)
                .child("A", "a0").down()
                .child("New").down()
                .child("A'", "a1").down().site().up() // (!) explicitly adding a site here, ohterwise BigraphER encoding will be not correct
                .site().up().site().create();
        PureBigraph newReactum = builder.spawn().root()
                // A{a0}.(id | id) | A{a1}.(id | id)
                .child("A", "a0").down().site().site().up()
                .child("A", "a1").down().site().site().create();
        InstantiationMap instMap = InstantiationMap.create(newReactum.getSites().size())
                .map(0, 1).map(1, 2).map(2, 0).map(3, 2);
        ParametricReactionRule<PureBigraph> newRR = new ParametricReactionRule<>(newRedex, newReactum, instMap).withLabel("new");
        BigraphGraphvizExporter.toPNG(newRedex, true, new File(TARGET_DUMP_PATH + "newRedex.png"));
        BigraphGraphvizExporter.toPNG(newReactum, true, new File(TARGET_DUMP_PATH + "newReactum.png"));

        ReactiveSystemPredicate<PureBigraph> phi = new ReactiveSystemPredicateStub(
                builder.spawn().root()
                        .child("Mail").down().child("M", "a").linkOuter("v").site().top().create()
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
        DynamicSignature sig = createSignature();
        createOrGetBigraphMetaModel(sig);

        // Create agent
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig);
        builder.root()
                .child("Room").down().connectByEdge("User", "Laptop")
                .child("User", "openNetwork")
                .child("Laptop", "openNetwork");
        PureBigraph big = builder.create();
        BigraphGraphvizExporter.toPNG(big,
                false,
                new File(TARGET_DUMP_PATH + "big.png")
        );
        // Create Rule 1: Add User
        PureBigraph redex = builder.spawn()
                .root()
                .child("Room").down().site()
                .create();
        PureBigraph reactum = builder.spawn()
                .root()
                .child("Room").down().site()
                .child("User")
                .create();
        ParametricReactionRule<PureBigraph> rr1 = new ParametricReactionRule<>(redex, reactum);

        PureBigraph reactum2 = builder.spawn()
                .root()
                .child("Room").down().site()
                .child("Laptop")
                .create();
        ParametricReactionRule<PureBigraph> rr2 = new ParametricReactionRule<>(redex, reactum2);

        PureBigraph redex3 = builder.spawn()
                .root()
                .child("User")
                .child("Laptop")
                .create();
        PureBigraph reactum3 = builder.spawn().root()
                .connectByEdge("User", "Laptop")
                .create();
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

    private static DynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl("User", 1).assign()
                .newControl("Room", 1).assign()
                .newControl("Laptop", 1).assign()
        ;
        return defaultBuilder.create();
    }

    private static DynamicSignature createSignature2() {
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
