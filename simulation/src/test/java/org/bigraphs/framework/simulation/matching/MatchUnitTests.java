package org.bigraphs.framework.simulation.matching;

import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.*;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.matching.pure.PureBigraphParametricMatch;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

//import org.openjdk.jmh.annotations.Benchmark;
//import org.openjdk.jmh.annotations.BenchmarkMode;
//import org.openjdk.jmh.annotations.Fork;
//import org.openjdk.jmh.annotations.Mode;

//TODO write better tests here: the redex output should conform to the expected output - this makes observing things easier
// to the equivalent bigraphER output result (means, check for num. of outer names etc.)
public class MatchUnitTests extends AbstractUnitTestSupport {
    //    private static PureBigraphFactory factory = pure();
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/matching/framework/";

//    public static void main(String[] args) throws Exception {
////        org.openjdk.jmh.Main.main(args);
//    }


    @Test
    void model_test_0() throws Exception {
        PureBigraph agent_model_test_0 = (PureBigraph) createAgent_model_test_0();
        PureBigraph redex_model_test_0 = (PureBigraph) createRedex_model_test_0();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex_model_test_0, redex_model_test_0);

        BigraphGraphvizExporter.toPNG(agent_model_test_0,
                true,
                new File(TARGET_DUMP_PATH + "model0/agent.png")
        );
        BigraphGraphvizExporter.toPNG(redex_model_test_0,
                true,
                new File(TARGET_DUMP_PATH + "model0/redex.png")
        );

        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agent_model_test_0, rr);
        Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
        int transition = 0;
        assertTrue(iterator.hasNext());
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
//            createGraphvizOutput(agent_model_test_0, next, TARGET_DUMP_PATH + "model0/" + (transition++) + "/");
            System.out.println("NEXT: " + next);
        }

    }

    @Test
    void model_test_1() throws Exception {
        PureBigraph agent_model_test_1 = (PureBigraph) createAgent_model_test_1();
        PureBigraph redex_model_test_1 = (PureBigraph) createRedex_model_test_1();

        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agent_model_test_1, new ParametricReactionRule<>(redex_model_test_1,redex_model_test_1));
        Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
        assertTrue(iterator.hasNext());
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            createGraphvizOutput(agent_model_test_1, next, TARGET_DUMP_PATH + "model1/");
            System.out.println("Match found: " + next);
        }

    }

    @Test
//    @Benchmark
//    @Fork(value = 1, warmups = 2)
//    @BenchmarkMode(Mode.AverageTime)
    public void model_test_2() throws Exception {
//        PureBigraph agent_model_test_2 = (PureBigraph) createAgent_model_test_2();
//        PureBigraph redex_model_test_2a = (PureBigraph) createRedex_model_test_2a();
//        exportGraph(redex_model_test_2a, TARGET_DUMP_PATH + "model2/redex_model_test_2a.png");
//        exportGraph(agent_model_test_2, TARGET_DUMP_PATH + "model2/agent_model_test_2.png");
        //the second root of the redex will create many occurrences because a distinct match isn't possible
//        PureBigraph redex_model_test_2b = (PureBigraph) createRedex_model_test_2b();

//        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
//        MatchIterable<PureBigraphParametricMatch> match = matcher.match(agent_model_test_2, redex_model_test_2a);
//        Iterator<PureBigraphParametricMatch> iterator = match.iterator();
//        while (iterator.hasNext()) {
//            BigraphMatch next = iterator.next();
//            System.out.println(next);
//            createGraphvizOutput(agent_model_test_2, next, TARGET_DUMP_PATH + "model2/");
//        }


//        Stopwatch timer0 = Stopwatch.createStarted();
//        MatchIterable<BigraphMatch<PureBigraph>> match2 = matcher.match(agent_model_test_2, redex_model_test_2b);
//        Iterator<BigraphMatch<PureBigraph>> iterator2 = match2.iterator();
//        while (iterator2.hasNext()) {
//            BigraphMatch<?> next = iterator2.next();
//            long elapsed0 = timer0.stop().elapsed(TimeUnit.NANOSECONDS);
//            System.out.println("Match time FULL (millisecs) " + (elapsed0 / 1e+6f));
//            createGraphvizOutput(agent_model_test_2, next, "src/test/resources/graphviz/model2/");
////                System.out.println(next);
//        }


    }

    @Test
    void model_test_3() throws Exception {
        PureBigraph agent_model_test_3 = (PureBigraph) createAgent_model_test_3();
        PureBigraph redex_model_test_3 = (PureBigraph) createRedex_model_test_3();
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);

        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agent_model_test_3, new ParametricReactionRule<>(redex_model_test_3,redex_model_test_3));
        Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            createGraphvizOutput(agent_model_test_3, next, TARGET_DUMP_PATH + "model3/");
            System.out.println(next);
        }

    }

    @Test
    @DisplayName("No matches are expected")
    void model_test_4() throws Exception {
        PureBigraph agent_model_test_4 = (PureBigraph) createAgent_model_test_4();
        PureBigraph redex_model_test_4 = (PureBigraph) createRedex_model_test_4();
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        BigraphGraphvizExporter.toPNG(agent_model_test_4,
                true,
                new File(TARGET_DUMP_PATH + "model4/agent.png")
        );
        BigraphGraphvizExporter.toPNG(redex_model_test_4,
                true,
                new File(TARGET_DUMP_PATH + "model4/redex.png")
        );
        MatchIterable<PureBigraphParametricMatch> match = matcher.match(agent_model_test_4, new ParametricReactionRule<>(redex_model_test_4,redex_model_test_4));
        Iterator<PureBigraphParametricMatch> iterator = match.iterator();
        assertTrue(iterator.hasNext());
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
//            createGraphvizOutput(agent_model_test_4, next, TARGET_DUMP_PATH + "model4/");
            System.out.println("MATCH: " + next);
        }

    }

    @Test
    @DisplayName("no matches should occur")
    void model_test_5() throws InvalidConnectionException, LinkTypeNotExistsException, InvalidReactionRuleException {
        PureBigraph agent_model_test_5 = (PureBigraph) createAgent_model_test_5();
        PureBigraph redex_model_test_5 = (PureBigraph) createRedex_model_test_5();
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);

        MatchIterable match = matcher.match(agent_model_test_5, new ParametricReactionRule<>(redex_model_test_5,redex_model_test_5));
        Iterator<BigraphMatch<?>> iterator = match.iterator();
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
//            createGraphvizOutput(agent_model_test_4, next, TARGET_DUMP_PATH + "model4/");
            System.out.println("MATCH: " + next);
        }
    }

    @Test
    @DisplayName("No matches are expected")
    void match_test_6_counting() throws InvalidConnectionException, LinkTypeNotExistsException, IncompatibleInterfaceException, InvalidReactionRuleException, IOException {
        PureBigraph agent = createAgent_A(-1, 1);
//        createGraphvizOutput(agent_a, null, "src/test/resources/graphviz/model6/");
        BigraphGraphvizExporter.toPNG(agent,
                true,
                new File(TARGET_DUMP_PATH + "model6/agent.png")
        );
        ReactionRule<PureBigraph> reactionRule_3 = createReactionRule_3();
        PureBigraph redex = reactionRule_3.getRedex();
        BigraphGraphvizExporter.toPNG(redex,
                true,
                new File(TARGET_DUMP_PATH + "model6/redex.png")
        );

        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<PureBigraphParametricMatch> match = matcher.match(agent, new ParametricReactionRule<>(redex,redex));
        Iterator<PureBigraphParametricMatch> iterator = match.iterator();
        assertFalse(iterator.hasNext());
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
//            createGraphvizOutput(agent, next, TARGET_DUMP_PATH + "model6/");
            System.out.println("MATCH: " + next);
        }
    }

    @Test
    @DisplayName("Only Place Graph Matching: Successful match is expected")
    void model_test_7() throws InvalidConnectionException, LinkTypeNotExistsException, IOException, InvalidReactionRuleException, IncompatibleInterfaceException {
        PureBigraph agent_model_test_7 = createAgent_model_test_7();
        PureBigraph redex_7 = createRedex_7();
//        BigraphGraphvizExporter.toPNG(agent_model_test_7,
//                true,
//                new File(TARGET_DUMP_PATH + "model7/agent.png")
//        );
//        BigraphGraphvizExporter.toPNG(redex_7,
//                true,
//                new File(TARGET_DUMP_PATH + "model7/redex_7.png")
//        );

        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);

        MatchIterable<PureBigraphParametricMatch> match = matcher.match(agent_model_test_7, new ParametricReactionRule<>(redex_7,redex_7));
        Iterator<PureBigraphParametricMatch> iterator = match.iterator();
        while (iterator.hasNext()) {
            PureBigraphParametricMatch next = iterator.next();
//            createGraphvizOutput(agent_model_test_7, next, TARGET_DUMP_PATH + "model7/");
            System.out.println("MATCH: " + next);
        }
    }

    @Test
    @DisplayName("Agent has no idle names: idle name in redex doesn't yield a match, but the redex w/o idle names.")
    void idle_edge_test() throws InvalidConnectionException, TypeNotExistsException, IOException, InvalidReactionRuleException, IncompatibleInterfaceException {
        PureBigraph agent = createAgent_idle_edge_test();
        BigraphGraphvizExporter.toPNG(agent,
                true,
                new File(TARGET_DUMP_PATH + "model_idleedge/agent.png")
        );
        PureBigraph redexMatch = createRedex_idle_edge_test(false);
        BigraphGraphvizExporter.toPNG(redexMatch,
                true,
                new File(TARGET_DUMP_PATH + "model_idleedge/redex-matches.png")
        );

////        GroundReactionRule<PureBigraph> rr = new GroundReactionRule<>(redexMatch, redexMatch);
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<PureBigraphParametricMatch> match = matcher.match(agent, new ParametricReactionRule<>(redexMatch,redexMatch));
        Iterator<PureBigraphParametricMatch> iterator = match.iterator();
        assertTrue(match.iterator().hasNext());
        while (iterator.hasNext()) {
            PureBigraphParametricMatch next = iterator.next();
//            createGraphvizOutput(agent, next, TARGET_DUMP_PATH + "model_idleedge/");
            System.out.println(next);
        }


        PureBigraph redexNoMatch = createRedex_idle_edge_test(true);
        BigraphGraphvizExporter.toPNG(redexNoMatch,
                true,
                new File(TARGET_DUMP_PATH + "model_idleedge/redex-no-match.png")
        );
        MatchIterable<PureBigraphParametricMatch> noMatchIterable = matcher.match(agent, new ParametricReactionRule<>(redexNoMatch,redexNoMatch));
        assertFalse(noMatchIterable.iterator().hasNext());
    }

    //        big s0 = /a Building . (Room.(Printer{a,b}.1));
    public PureBigraph createAgent_idle_edge_test() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.OuterName a = builder.createOuter("a");
        BigraphEntity.OuterName b = builder.createOuter("b");
        builder.root()
                .child("Building").down().child("Room")
                .down().child("Printer").linkOuter(a)
                .linkOuter(b)
        ;

        return builder.create();
    }

    public PureBigraph createRedex_idle_edge_test(boolean closeName) throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.OuterName a = builder.createOuter("a");
        BigraphEntity.OuterName b = builder.createOuter("b");
        builder.root()
                .child("Room").down()
                .child("Printer").linkOuter(a)
                .linkOuter(b)
        ;
        if (closeName)
            builder.closeOuter(a, true);
        return builder.create();
    }

    public PureBigraph createAgent_model_test_7() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createSignatureABC();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        PureBigraphBuilder<DynamicSignature>.Hierarchy a = builder.hierarchy(signature.getControlByName("A"));
        PureBigraphBuilder<DynamicSignature>.Hierarchy b = builder.hierarchy(signature.getControlByName("B"));

        a.child("B")
                .child("C")
                .down().child("D").child("E");
        b.child("F").child("G").down().child("H").child("I");

        builder.root()
                .child(a.top())
                .child(b.top())
        ;
        return builder.create();
    }

    public PureBigraph createRedex_7() {
        DynamicSignature signature = createSignatureABC();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        builder.root().child("G").down().child("H").child("I");

//        builder.createRoot().addChild("C").withNewHierarchy().addChild("A").withNewHierarchy().addChild("D").addChild("E");
        return builder.create();
    }

    public Bigraph createAgent_model_test_5() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        builder.root()
                .child(signature.getControlByName("Computer"))
                .down()
                .child(signature.getControlByName("Room"))
                .child(signature.getControlByName("Job"));
        return builder.create();
    }

    public Bigraph createRedex_model_test_5() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        builder.root()
                .child(signature.getControlByName("Room"))
                .down()
                .child(signature.getControlByName("Computer"))
                .child(signature.getControlByName("Job"));
        return builder.create();
    }

    public Bigraph createAgent_model_test_4() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.InnerName innerLink = builder.createInner("inner_link");
        BigraphEntity.OuterName network = builder.createOuter("network");
//        BigraphEntity.OuterName network1 = builder.createOuterName("network1");
        BigraphEntity.OuterName a = builder.createOuter("a");
        BigraphEntity.OuterName b = builder.createOuter("b");

        PureBigraphBuilder<DynamicSignature>.Hierarchy room = builder.hierarchy(signature.getControlByName("Room"));
        room.child(signature.getControlByName("Computer")).linkOuter(network).down()
                .child(signature.getControlByName("A"))
                .child(signature.getControlByName("A")).linkOuter(b)
                .child(signature.getControlByName("A")).linkOuter(b);

        PureBigraphBuilder<DynamicSignature>.Hierarchy room2 = builder.hierarchy(signature.getControlByName("Room"));
        room2.child(signature.getControlByName("Computer")).linkOuter(network).down()
                .child(signature.getControlByName("A")).linkOuter(a)
                .child(signature.getControlByName("B")).linkInner(innerLink)
                .down().child(signature.getControlByName("Job")).child(signature.getControlByName("Job")).up()
                .child(signature.getControlByName("A")).linkInner(innerLink);

        builder.root().child(room).child(room2);
        builder.closeInner();
        builder.makeGround();
        return builder.create();
    }

    public Bigraph createRedex_model_test_4() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuter("network");

        PureBigraphBuilder<DynamicSignature>.Hierarchy room = builder.hierarchy(signature.getControlByName("Room"));
        room.child(signature.getControlByName("Computer")).linkOuter(network).down()
                .site();

        PureBigraphBuilder<DynamicSignature>.Hierarchy room2 = builder.hierarchy(signature.getControlByName("Room"));
        room2.child(signature.getControlByName("Computer")).linkOuter(network).down()
                .site();

        builder.root().child(room).child(room2);
        return builder.create();
    }


    public Bigraph createAgent_model_test_3() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.InnerName roomLink = builder.createInner("door");
        BigraphEntity.OuterName network = builder.createOuter("network");
        BigraphEntity.OuterName jeff = builder.createOuter("jeff");
        BigraphEntity.OuterName bob = builder.createOuter("bob");
        BigraphEntity.OuterName a = builder.createOuter("a");
        BigraphEntity.OuterName b = builder.createOuter("b");

        builder.root()
                .child(signature.getControlByName("Room")).linkInner(roomLink)
                .down().child(signature.getControlByName("User")).linkOuter(bob)
                .child(signature.getControlByName("Computer")).linkOuter(network)
                .down().child(signature.getControlByName("Job")).down().child(signature.getControlByName("Job")).child(signature.getControlByName("Job")).up()
                .child(signature.getControlByName("User")).linkOuter(a)
                .down().child(signature.getControlByName("User")).linkOuter(b)
                .up()
                .up()
                .up()

                .child(signature.getControlByName("Room")).linkInner(roomLink)
                .down().child(signature.getControlByName("User")).linkOuter(jeff)
                .up()
        ;

        builder.closeInner();
        builder.makeGround();
        return builder.create();

    }

    public Bigraph createRedex_model_test_3() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.OuterName network = builder.createOuter("network");

        builder.root()
                .child(signature.getControlByName("Computer")).linkOuter(network)
                .down().site().child(signature.getControlByName("Job")).down().site();
        return builder.create();
    }

    public Bigraph createAgent_model_test_1() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName jeff1 = builder.createOuter("jeff1");
        BigraphEntity.OuterName jeff2 = builder.createOuter("jeff2");
        BigraphEntity.OuterName b1 = builder.createOuter("b1");
        BigraphEntity.InnerName e1 = builder.createInner("e1");

//        big s1 = /e1
//                ((Room{e1} . (Computer{b1}.(Job.1) | User{jeff1}.1 ))
//|
//        (Room{e1} . (Computer{b1}.(Job.1 | User{jeff2}.1))));

        builder.root()
                .child(signature.getControlByName("Room")).linkInner(e1)
                .down()
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down().child(signature.getControlByName("Job")).up()
                .child(signature.getControlByName("User")).linkOuter(jeff1)
                .up()

                .child(signature.getControlByName("Room")).linkInner(e1)
                .down()
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down().child(signature.getControlByName("Job")).child(signature.getControlByName("User")).linkOuter(jeff2)
                .up().up();

        builder.closeInner();
        builder.makeGround();
        return builder.create();
    }

    public Bigraph createRedex_model_test_1() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName jeff1 = builder.createOuter("jeff1");
        BigraphEntity.OuterName jeff2 = builder.createOuter("jeff2");
        BigraphEntity.OuterName b1 = builder.createOuter("b1");

        //(Computer{b1}.(Job.1) | User{jeff2}.1) || Computer{b1}.(Job.1 | User{jeff2}.1);

        builder.root()
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down().child(signature.getControlByName("Job")).up()
                .child(signature.getControlByName("User")).linkOuter(jeff1);

        builder.root()
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down().child(signature.getControlByName("Job")).child(signature.getControlByName("User")).linkOuter(jeff2);

        builder.makeGround();
        return builder.create();

    }

    public static Bigraph createRedex_model_test_0() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName a = builder.createOuter("a");
        BigraphEntity.OuterName b = builder.createOuter("b");
        BigraphEntity.OuterName c = builder.createOuter("c");
        BigraphEntity.OuterName d = builder.createOuter("d");
        builder.
                root()
                .child(signature.getControlByName("Computer")).linkOuter(a)
                .down()
                .child(signature.getControlByName("User")).linkOuter(d)
                .child(signature.getControlByName("Job"))
//                .goBack()
        ;
        builder.root()
                .child(signature.getControlByName("Computer")).linkOuter(c)
                .down()
                .child(signature.getControlByName("User")).linkOuter(b)
                .child(signature.getControlByName("Job"))
//                .goBack()
        ;
        return builder.create();
    }

    public static Bigraph createAgent_model_test_0() throws TypeNotExistsException, InvalidConnectionException, IOException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.InnerName roomLink = builder.createInner("tmp1_room");
//        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b1 = builder.createOuter("b1");
        BigraphEntity.OuterName jeff2 = builder.createOuter("jeff2");

        builder.root()
                .child(signature.getControlByName("Room")).linkInner(roomLink)
                .down()
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down()
                .child(signature.getControlByName("Job"))
                .child(signature.getControlByName("User")).linkOuter(jeff2)
                .up()
                .up()

                .child(signature.getControlByName("Room")).linkInner(roomLink)
                .down()
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down()
                .child(signature.getControlByName("Job"))
                .child(signature.getControlByName("User")).linkOuter(jeff2)
                .up()
                .up()

//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
        ;
        builder.closeInner(roomLink);
        builder.closeInner();
        builder.makeGround();

        PureBigraph bigraph = builder.create();
        return bigraph;

    }

    /**
     * Ground reaction rule: find a user with two jobs.
     * RR: User has a link and also an idle outer to match.
     *
     * @return
     * @throws LinkTypeNotExistsException
     * @throws InvalidConnectionException
     */
    public Bigraph createRedex_model_test_2a() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName jeff = builder.createOuter("jeff1");
        BigraphEntity.OuterName b1 = builder.createOuter("b1");


        // (Computer{b1}.(id(1)) | Computer{jeff1}.1 | Job.1) || (User{jeff1}.(Job.1 | Job.1));
        builder.
                root()
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down().site().up()
                .child(signature.getControlByName("Computer")).linkOuter(jeff)
                .child(signature.getControlByName("Job"))
        ;
//        builder.createRoot()
//                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff)
//                .withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).goBack()
//        ;
        return builder.create();
    }

    /**
     * Parametric Reaction Rule
     * Redex will need parameters to be built.
     *
     * @return
     * @throws LinkTypeNotExistsException
     * @throws InvalidConnectionException
     */
    public Bigraph createRedex_model_test_2b() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName b1 = builder.createOuter("b1");
        BigraphEntity.OuterName c = builder.createOuter("c");
        BigraphEntity.OuterName jeff1 = builder.createOuter("jeff1");


        // (Computer{b1}.(id(1)) | Computer{jeff1}.1 | Job.1) || (User{jeff1}.(Job.1 | Job.1));
        builder.
                root()
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down().site().up()
                .child(signature.getControlByName("Computer")).linkOuter(c)
                .child(signature.getControlByName("Job"))
        ;
        builder.root()
                .child(signature.getControlByName("User")).linkOuter(jeff1)
                .down().child(signature.getControlByName("Job")).child(signature.getControlByName("Job")).up()
        ;
        return builder.create();
    }


    public Bigraph createAgent_model_test_2() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.InnerName door = builder.createInner("door");
        BigraphEntity.OuterName e1 = builder.createOuter("eroom");
        BigraphEntity.OuterName e0 = builder.createOuter("espool");
        BigraphEntity.OuterName a = builder.createOuter("a");
        BigraphEntity.OuterName b = builder.createOuter("b");
        BigraphEntity.OuterName jeff1 = builder.createOuter("jeff1");
        BigraphEntity.OuterName jeff2 = builder.createOuter("jeff2");

//        big s1 = /door (
//                (Room{door} . (Computer{a}.1 | Computer{a}.(Job.1 | Job.(Job.1) | Job.1) | Computer{a}.1 | Computer{jeff}.1 | Job.1 ))
//                | (Spool{e0}.1)
//                | (Room{e1} . (User{jeff}.(Job.1 | Job.1) | Job.1 | Job.1))
//                );

        builder.root()
                .child(signature.getControlByName("Room")).linkInner(door)
                .down()
                .child(signature.getControlByName("Computer")) //.connectNodeToOuterName(a)
                .child(signature.getControlByName("Computer")).linkOuter(a)
                .down()
                .child(signature.getControlByName("Job"))
                .child(signature.getControlByName("Job")).down().child(signature.getControlByName("Job")).up()
                .child(signature.getControlByName("Job")).up()
                .child(signature.getControlByName("Computer")).linkOuter(a)
                .child(signature.getControlByName("Computer")).linkOuter(jeff1)
                .child(signature.getControlByName("Job"))
                .child(signature.getControlByName("User"))
                .up()

                .child(signature.getControlByName("Spool")).linkOuter(e0)

                .child(signature.getControlByName("Room")).linkOuter(e1)
                .down()
                .child(signature.getControlByName("User")).linkOuter(jeff2)
                .down().child(signature.getControlByName("Job")).child(signature.getControlByName("Job")).up()
                .child(signature.getControlByName("Job"))
                .child(signature.getControlByName("Job"))

        ;

//        builder.closeInnerName(roomLink);
//        builder.closeInnerName(printerSpoolLink);
        builder.closeInner();
        builder.makeGround();

        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    private static <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
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

        return (S) defaultBuilder.create();
    }

    private static <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature2() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Age")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("S")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Z")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("True")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("False")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Left")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Right")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;

        return (S) defaultBuilder.create();
    }

    private static <C extends Control<?, ?>, S extends Signature<C>> S createSignatureABC() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("C")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("D")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("E")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("F")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("G")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("H")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("I")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("J")).arity(FiniteOrdinal.ofInteger(1)).assign()
        ;

        return (S) defaultBuilder.create();
    }

    /**
     * big numberLeft = Left.S.S.S.S.S.S.Z;
     * big numberRight = Right.S.S.S.S.Z;
     * big start = Age . (numberLeft | numberRight);
     */
    public static PureBigraph createAgent_A(final int left, final int right) throws ControlIsAtomicException, InvalidArityOfControlException, LinkTypeNotExistsException {
        DynamicSignature signature = createExampleSignature2();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        PureBigraphBuilder<DynamicSignature>.Hierarchy leftNode =
                builder.hierarchy(signature.getControlByName("Left"))
                        .child("Z");
        PureBigraphBuilder<DynamicSignature>.Hierarchy rightNode =
                builder.hierarchy(signature.getControlByName("Right"))
                        .child("S");
//        for (int i = 0; i < left - 1; i++) {
//            leftNode = leftNode.withNewHierarchy().addChild("S");
//        }
//        leftNode = leftNode.withNewHierarchy().addChild("Z").top();
        for (int i = 0; i < right - 1; i++) {
            rightNode = rightNode.down().child("S");
        }
//        rightNode = rightNode.withNewHierarchy().addChild("Z").top();
        rightNode = rightNode.top();

        builder.root()
                .child("Age")
                .down()
                .child(leftNode)
                .child(rightNode)
        ;
        builder.makeGround();
        return builder.create();
    }

    public static ReactionRule<PureBigraph> createReactionRule_3() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature2();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root()
                .child("Left").down().site()
                .top()
                .child("Right").down().child("Z")
        ;
        builder2.root()
                .child("False").down().site()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

}
