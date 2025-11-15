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
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
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

/**
 * @author Dominik Grzelak
 */
public class ReactionRuleCreationUnitTest {

    private DynamicSignature createSignature() {
        return pureSignatureBuilder()
                .add("Person", 2)
                .add("Room", 2)
                .add("User", 2)
                .add("Computer", 2)
                .create()
                ;
    }

    @Test
    void name() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, IOException {
        DynamicSignature signature = pureSignatureBuilder()
                .add("Room", 0)
                .add("Computer", 1)
                .add("Job", 0)
                .create();
        // Redex builder
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        // Reactum builder
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);
        // Connect computer over the same channel link
        BigraphEntity.OuterName network = builder.createOuter("network");
        builder.root()
                .child("Room")
                .down()
                .site()
                .child("Computer").linkOuter(network)
                .down()
                .child("Job")
        ;
        builder2.root()
                .child("Room")
                .down()
                .site()
                .child("Computer").linkOuter("network") // or just specify the string
                .down()
                .child("Job").child("Job")
        ;

        // builder.makeGround(); // useful for instances of type GroundReactionRule
        // builder2.makeGround(); // useful for instances of type GroundReactionRule
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        BigraphGraphvizExporter.toPNG(rr.getRedex(), true, new File("redex.png"));
        BigraphGraphvizExporter.toPNG(rr.getReactum(), true, new File("reactum.png"));
    }

    @Test
    @DisplayName("Create a rule whose Redex has 2 Sites and its Reactum has 1 Site")
    void create_rule_01() throws InvalidReactionRuleException, IOException {
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();

        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(createSignature());

        builderRedex.root().child("Room").down().site().child("Person").down().site().up().up().child("Person");

        builderReactum.root().child("Room").down().site().child("Person");

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex, System.out);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum, System.out);
        ParametricReactionRule<PureBigraph> pureBigraphParametricReactionRule = new ParametricReactionRule<>(redex, reactum);
        AbstractReactionRule.ReactiveSystemBoundReactionRule<PureBigraph> rrBounded = pureBigraphParametricReactionRule.withReactiveSystem(reactiveSystem);
        Assertions.assertTrue(rrBounded.isRedexSimple());
    }
}
