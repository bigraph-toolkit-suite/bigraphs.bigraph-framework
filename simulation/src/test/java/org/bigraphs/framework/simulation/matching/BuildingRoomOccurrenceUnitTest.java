package org.bigraphs.framework.simulation.matching;

import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.testing.BigraphUnitTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * @author Dominik Grzelak
 */
public class BuildingRoomOccurrenceUnitTest implements AbstractUnitTestSupport, BigraphUnitTestSupport {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/occurrence/";

    private DynamicSignature sig() {
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
                .create();
    }

    @Test
    @DisplayName("Matching test (Building Environment): Redex with 1x Root and 1 Room Node")
    void test_1() throws InvalidReactionRuleException, IOException, IncompatibleInterfaceException {
        PureBigraph agent = agent_00();
        toPNG(agent, "agent_1", TARGET_DUMP_PATH);

        PureBigraph redex = redex_1();
        toPNG(redex, "redex_1", TARGET_DUMP_PATH);

        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, redex);
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agent, rr);
        int transition = 0;
        for (BigraphMatch<?> next : match) {
            createGraphvizOutput(agent, next, TARGET_DUMP_PATH + "test-1/", (transition++));
            System.out.println("NEXT: " + next);
        }
    }

    @Test
    @DisplayName("Matching test (Building Environment): Redex with 1x Root and 2x Room Nodes")
    void test_2() throws InvalidReactionRuleException, IOException, IncompatibleInterfaceException {
        PureBigraph agent = agent_00();
        toPNG(agent, "agent_2", TARGET_DUMP_PATH);

        PureBigraph redex = redex_2();
        toPNG(redex, "redex_2", TARGET_DUMP_PATH);

        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, redex);
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agent, rr);
        int transition = 0;
        for (BigraphMatch<?> next : match) {
            createGraphvizOutput(agent, next, TARGET_DUMP_PATH + "test-2/", (transition++));
            System.out.println("NEXT: " + next);
        }
    }

    @Test
    @DisplayName("Matching test (Building Environment): Redex with 2x Root and 2x Room Nodes")
    void test_3() throws InvalidReactionRuleException, IOException, IncompatibleInterfaceException {
        PureBigraph agent = agent_00();
        toPNG(agent, "agent_3", TARGET_DUMP_PATH);

        PureBigraph redex = redex_3();
        toPNG(redex, "redex_3", TARGET_DUMP_PATH);

        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, redex);
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agent, rr);
        int transition = 0;
        for (BigraphMatch<?> next : match) {
            createGraphvizOutput(agent, next, TARGET_DUMP_PATH + "test-3/", (transition++));
            System.out.println("NEXT: " + next);
        }
    }

    /**
     * Create the initial bigraph with three rooms.
     */
    private PureBigraph agent_00() {
        DynamicSignature signature = sig();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        builder.root()
                .child("Building")
                .down().child("Room").down().child("Room").down().child("User").up().up()
                .child("Room").down().child("User");
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    /**
     * A bigraph with one root and 1 room.
     */
    private PureBigraph redex_1() {
        DynamicSignature signature = sig();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        builder.root().child("Room").down().site();
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    /**
     * A bigraph with one root and 2 rooms.
     */
    private PureBigraph redex_2() {
        DynamicSignature signature = sig();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        builder.root().child("Room").down().site()
                .up().child("Room").down().site();
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    /**
     * Create a bigraph with two roots and 2 rooms.
     */
    private PureBigraph redex_3() {
        DynamicSignature signature = sig();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        builder.root().child("Room").down().site();
        builder.root().child("Room").down().site();
        PureBigraph bigraph = builder.create();
        return bigraph;
    }
}
