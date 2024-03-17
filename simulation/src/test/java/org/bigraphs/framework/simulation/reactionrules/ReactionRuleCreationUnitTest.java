package org.bigraphs.framework.simulation.reactionrules;

import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.AbstractReactionRule;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

public class ReactionRuleCreationUnitTest {

    @Test
    void name() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, IOException {
        DefaultDynamicSignature signature = pureSignatureBuilder()
                .addControl("Room", 0)
                .addControl("Computer", 1)
                .addControl("Job", 0)
                .create()
                ;
        // Redex builder
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        // Reactum builder
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);
        // Connect computer over the same channel link
        BigraphEntity.OuterName network = builder.createOuterName("network");
        builder.createRoot()
                .addChild("Room")
                .down()
                .addSite()
                .addChild("Computer").linkToOuter(network)
                .down()
                .addChild("Job")
        ;
        builder2.createRoot()
                .addChild("Room")
                .down()
                .addSite()
                .addChild("Computer").linkToOuter("network") // or just specify the string
                .down()
                .addChild("Job").addChild("Job")
        ;

        // builder.makeGround(); // useful for instances of type GroundReactionRule
        // builder2.makeGround(); // useful for instances of type GroundReactionRule
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        BigraphGraphvizExporter.toPNG(rr.getRedex(), true, new File("redex.png"));
        BigraphGraphvizExporter.toPNG(rr.getReactum(), true, new File("reactum.png"));
    }

    @Test
    @DisplayName("Create a rule whose Redex has 2 Sites and its Reactum has 1 Site")
    void create_rule_01() throws InvalidReactionRuleException, IOException {
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();

        PureBigraphBuilder<DefaultDynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builderReactum = pureBuilder(createSignature());

        builderRedex.createRoot().addChild("Room").down().addSite().addChild("Person").down().addSite().up().up().addChild("Person");

        builderReactum.createRoot().addChild("Room").down().addSite().addChild("Person");

        PureBigraph redex = builderRedex.createBigraph();
        PureBigraph reactum = builderReactum.createBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex, System.out);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum, System.out);
        ParametricReactionRule<PureBigraph> pureBigraphParametricReactionRule = new ParametricReactionRule<>(redex, reactum);
        AbstractReactionRule.ReactiveSystemBoundReactionRule<PureBigraph> rrBounded = pureBigraphParametricReactionRule.withReactiveSystem(reactiveSystem);
        Assertions.assertTrue(rrBounded.isRedexSimple());
    }


    private DefaultDynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Person")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(2)).assign()
        ;

        return defaultBuilder.create();
    }


}
