package de.tudresden.inf.st.bigraphs.simulation.reactionrules;

import de.tudresden.inf.st.bigraphs.core.BigraphArtifacts;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.AbstractReactionRule;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.matching.pure.PureReactiveSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureBuilder;
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureSignatureBuilder;

public class ReactionRuleCreationUnitTest {

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
        BigraphArtifacts.exportAsInstanceModel(redex, System.out);
        BigraphArtifacts.exportAsInstanceModel(reactum, System.out);
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
