package org.bigraphs.framework.simulation.matching;

import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

public class OccurenceUnitTest extends AbstractUnitTestSupport {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/occurrence/";

    @Test
    @DisplayName("")
    void test_00() throws InvalidReactionRuleException, IOException, IncompatibleInterfaceException {
        PureBigraph agent_00 = agent_00();
        exportGraph(agent_00, TARGET_DUMP_PATH + "agent_00.png");

        PureBigraph redex_00 = redex_00();
        exportGraph(redex_00, TARGET_DUMP_PATH + "redex_00.png");

        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex_00, redex_00);
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agent_00, rr);
        int transition = 0;
        for (BigraphMatch<?> next : match) {
            createGraphvizOutput(agent_00, next, TARGET_DUMP_PATH + "model00-" + (transition++) + "/");
//            createGraphvizOutput(agent_00, next, TARGET_DUMP_PATH + "model1/");
            System.out.println("NEXT: " + next);
        }
    }

    private PureBigraph agent_00() {
        DefaultDynamicSignature signature = signature00();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        builder.createRoot()
                .addChild("Building")
                .down().addChild("Room").down().addChild("Room").down().addChild("User").up().up()
                .addChild("Room").down().addChild("User");
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    private PureBigraph redex_00() {
        DefaultDynamicSignature signature = signature00();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        builder.createRoot().addChild("Room").down().addSite();
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    @Test
    @DisplayName("")
    void test_01() throws InvalidReactionRuleException, IOException, IncompatibleInterfaceException {
        PureBigraph agent_00 = agent_00();
        exportGraph(agent_00, TARGET_DUMP_PATH + "agent_00.png");

        PureBigraph redex_01 = redex_01();
        exportGraph(redex_01, TARGET_DUMP_PATH + "redex_01.png");

        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex_01, redex_01);
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agent_00, rr);
        int transition = 0;
        for (BigraphMatch<?> next : match) {
            createGraphvizOutput(agent_00, next, TARGET_DUMP_PATH + "model01-" + (transition++) + "/");
//            createGraphvizOutput(agent_00, next, TARGET_DUMP_PATH + "model1/");
            System.out.println("NEXT: " + next);
        }
    }

    //mit 1 root und 2 Rooms
    private PureBigraph redex_01() {
        DefaultDynamicSignature signature = signature00();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        builder.createRoot().addChild("Room").down().addSite()
                .up().addChild("Room").down().addSite();
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }


    @Test
    @DisplayName("2 roots ohne links")
    void test_02() throws InvalidReactionRuleException, IOException, IncompatibleInterfaceException {
        PureBigraph agent_00 = agent_00();
        exportGraph(agent_00, TARGET_DUMP_PATH + "agent_00.png");

        PureBigraph redex_02 = redex_02();
        exportGraph(redex_02, TARGET_DUMP_PATH + "redex_02.png");

        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex_02, redex_02);
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agent_00, rr);
        int transition = 0;
        for (BigraphMatch<?> next : match) {
            createGraphvizOutput(agent_00, next, TARGET_DUMP_PATH + "model02-" + (transition++) + "/");
//            createGraphvizOutput(agent_00, next, TARGET_DUMP_PATH + "model1/");
            System.out.println("NEXT: " + next);
        }
    }

    //mit 2 roots
    private PureBigraph redex_02() {
        DefaultDynamicSignature signature = signature00();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        builder.createRoot().addChild("Room").down().addSite();
        builder.createRoot().addChild("Room").down().addSite();
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

//    //mit links
//    private PureBigraph agent_03() {
//        DefaultDynamicSignature signature = signature00();
//        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
//
//        builder.createRoot()
//                .addChild("Building")
//                .down().addChild("Room").down().addChild("Room").down().addChild("User").up().up()
//                .addChild("Room").down().addChild("User");
//        PureBigraph bigraph = builder.createBigraph();
//        return bigraph;
//    }
//
//    //mit links, 2 roots
//    private PureBigraph redex_03() {
//        DefaultDynamicSignature signature = signature00();
//        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
//        builder.createRoot().addChild("Room").down().addChild("User");
//        builder.createRoot().addChild("Room").down().addSite();
//        PureBigraph bigraph = builder.createBigraph();
//        return bigraph;
//    }


    private static DefaultDynamicSignature signature00() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(1)).assign();

        return defaultBuilder.create();
    }
}
