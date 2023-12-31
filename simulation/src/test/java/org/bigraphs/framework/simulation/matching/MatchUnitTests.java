package org.bigraphs.framework.simulation.matching;

import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.*;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
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
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b = builder.createOuterName("b");
        builder.createRoot()
                .addChild("Building").down().addChild("Room")
                .down().addChild("Printer").linkToOuter(a)
                .linkToOuter(b)
        ;

        return builder.createBigraph();
    }

    public PureBigraph createRedex_idle_edge_test(boolean closeName) throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b = builder.createOuterName("b");
        builder.createRoot()
                .addChild("Room").down()
                .addChild("Printer").linkToOuter(a)
                .linkToOuter(b)
        ;
        if (closeName)
            builder.closeOuterName(a, true);
        return builder.createBigraph();
    }

    public PureBigraph createAgent_model_test_7() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createSignatureABC();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy a = builder.hierarchy(signature.getControlByName("A"));
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy b = builder.hierarchy(signature.getControlByName("B"));

        a.addChild("B")
                .addChild("C")
                .down().addChild("D").addChild("E");
        b.addChild("F").addChild("G").down().addChild("H").addChild("I");

        builder.createRoot()
                .addChild(a.top())
                .addChild(b.top())
        ;
        return builder.createBigraph();
    }

    public PureBigraph createRedex_7() {
        DefaultDynamicSignature signature = createSignatureABC();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        builder.createRoot().addChild("G").down().addChild("H").addChild("I");

//        builder.createRoot().addChild("C").withNewHierarchy().addChild("A").withNewHierarchy().addChild("D").addChild("E");
        return builder.createBigraph();
    }

    public Bigraph createAgent_model_test_5() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        builder.createRoot()
                .addChild(signature.getControlByName("Computer"))
                .down()
                .addChild(signature.getControlByName("Room"))
                .addChild(signature.getControlByName("Job"));
        return builder.createBigraph();
    }

    public Bigraph createRedex_model_test_5() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        builder.createRoot()
                .addChild(signature.getControlByName("Room"))
                .down()
                .addChild(signature.getControlByName("Computer"))
                .addChild(signature.getControlByName("Job"));
        return builder.createBigraph();
    }

    public Bigraph createAgent_model_test_4() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.InnerName innerLink = builder.createInnerName("inner_link");
        BigraphEntity.OuterName network = builder.createOuterName("network");
//        BigraphEntity.OuterName network1 = builder.createOuterName("network1");
        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b = builder.createOuterName("b");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room = builder.hierarchy(signature.getControlByName("Room"));
        room.addChild(signature.getControlByName("Computer")).linkToOuter(network).down()
                .addChild(signature.getControlByName("A"))
                .addChild(signature.getControlByName("A")).linkToOuter(b)
                .addChild(signature.getControlByName("A")).linkToOuter(b);

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room2 = builder.hierarchy(signature.getControlByName("Room"));
        room2.addChild(signature.getControlByName("Computer")).linkToOuter(network).down()
                .addChild(signature.getControlByName("A")).linkToOuter(a)
                .addChild(signature.getControlByName("B")).linkToInner(innerLink)
                .down().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).up()
                .addChild(signature.getControlByName("A")).linkToInner(innerLink);

        builder.createRoot().addChild(room).addChild(room2);
        builder.closeAllInnerNames();
        builder.makeGround();
        return builder.createBigraph();
    }

    public Bigraph createRedex_model_test_4() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuterName("network");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room = builder.hierarchy(signature.getControlByName("Room"));
        room.addChild(signature.getControlByName("Computer")).linkToOuter(network).down()
                .addSite();

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room2 = builder.hierarchy(signature.getControlByName("Room"));
        room2.addChild(signature.getControlByName("Computer")).linkToOuter(network).down()
                .addSite();

        builder.createRoot().addChild(room).addChild(room2);
        return builder.createBigraph();
    }


    public Bigraph createAgent_model_test_3() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.InnerName roomLink = builder.createInnerName("door");
        BigraphEntity.OuterName network = builder.createOuterName("network");
        BigraphEntity.OuterName jeff = builder.createOuterName("jeff");
        BigraphEntity.OuterName bob = builder.createOuterName("bob");
        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b = builder.createOuterName("b");

        builder.createRoot()
                .addChild(signature.getControlByName("Room")).linkToInner(roomLink)
                .down().addChild(signature.getControlByName("User")).linkToOuter(bob)
                .addChild(signature.getControlByName("Computer")).linkToOuter(network)
                .down().addChild(signature.getControlByName("Job")).down().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).up()
                .addChild(signature.getControlByName("User")).linkToOuter(a)
                .down().addChild(signature.getControlByName("User")).linkToOuter(b)
                .up()
                .up()
                .up()

                .addChild(signature.getControlByName("Room")).linkToInner(roomLink)
                .down().addChild(signature.getControlByName("User")).linkToOuter(jeff)
                .up()
        ;

        builder.closeAllInnerNames();
        builder.makeGround();
        return builder.createBigraph();

    }

    public Bigraph createRedex_model_test_3() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.OuterName network = builder.createOuterName("network");

        builder.createRoot()
                .addChild(signature.getControlByName("Computer")).linkToOuter(network)
                .down().addSite().addChild(signature.getControlByName("Job")).down().addSite();
        return builder.createBigraph();
    }

    public Bigraph createAgent_model_test_1() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName jeff1 = builder.createOuterName("jeff1");
        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
        BigraphEntity.InnerName e1 = builder.createInnerName("e1");

//        big s1 = /e1
//                ((Room{e1} . (Computer{b1}.(Job.1) | User{jeff1}.1 ))
//|
//        (Room{e1} . (Computer{b1}.(Job.1 | User{jeff2}.1))));

        builder.createRoot()
                .addChild(signature.getControlByName("Room")).linkToInner(e1)
                .down()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down().addChild(signature.getControlByName("Job")).up()
                .addChild(signature.getControlByName("User")).linkToOuter(jeff1)
                .up()

                .addChild(signature.getControlByName("Room")).linkToInner(e1)
                .down()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("User")).linkToOuter(jeff2)
                .up().up();

        builder.closeAllInnerNames();
        builder.makeGround();
        return builder.createBigraph();
    }

    public Bigraph createRedex_model_test_1() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName jeff1 = builder.createOuterName("jeff1");
        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");

        //(Computer{b1}.(Job.1) | User{jeff2}.1) || Computer{b1}.(Job.1 | User{jeff2}.1);

        builder.createRoot()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down().addChild(signature.getControlByName("Job")).up()
                .addChild(signature.getControlByName("User")).linkToOuter(jeff1);

        builder.createRoot()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("User")).linkToOuter(jeff2);

        builder.makeGround();
        return builder.createBigraph();

    }

    public static Bigraph createRedex_model_test_0() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b = builder.createOuterName("b");
        BigraphEntity.OuterName c = builder.createOuterName("c");
        BigraphEntity.OuterName d = builder.createOuterName("d");
        builder.
                createRoot()
                .addChild(signature.getControlByName("Computer")).linkToOuter(a)
                .down()
                .addChild(signature.getControlByName("User")).linkToOuter(d)
                .addChild(signature.getControlByName("Job"))
//                .goBack()
        ;
        builder.createRoot()
                .addChild(signature.getControlByName("Computer")).linkToOuter(c)
                .down()
                .addChild(signature.getControlByName("User")).linkToOuter(b)
                .addChild(signature.getControlByName("Job"))
//                .goBack()
        ;
        return builder.createBigraph();
    }

    public static Bigraph createAgent_model_test_0() throws TypeNotExistsException, InvalidConnectionException, IOException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.InnerName roomLink = builder.createInnerName("tmp1_room");
//        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");

        builder.createRoot()
                .addChild(signature.getControlByName("Room")).linkToInner(roomLink)
                .down()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down()
                .addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("User")).linkToOuter(jeff2)
                .up()
                .up()

                .addChild(signature.getControlByName("Room")).linkToInner(roomLink)
                .down()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down()
                .addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("User")).linkToOuter(jeff2)
                .up()
                .up()

//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
        ;
        builder.closeInnerName(roomLink);
        builder.closeAllInnerNames();
        builder.makeGround();

        PureBigraph bigraph = builder.createBigraph();
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
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName jeff = builder.createOuterName("jeff1");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");


        // (Computer{b1}.(id(1)) | Computer{jeff1}.1 | Job.1) || (User{jeff1}.(Job.1 | Job.1));
        builder.
                createRoot()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down().addSite().up()
                .addChild(signature.getControlByName("Computer")).linkToOuter(jeff)
                .addChild(signature.getControlByName("Job"))
        ;
//        builder.createRoot()
//                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff)
//                .withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).goBack()
//        ;
        return builder.createBigraph();
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
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
        BigraphEntity.OuterName c = builder.createOuterName("c");
        BigraphEntity.OuterName jeff1 = builder.createOuterName("jeff1");


        // (Computer{b1}.(id(1)) | Computer{jeff1}.1 | Job.1) || (User{jeff1}.(Job.1 | Job.1));
        builder.
                createRoot()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down().addSite().up()
                .addChild(signature.getControlByName("Computer")).linkToOuter(c)
                .addChild(signature.getControlByName("Job"))
        ;
        builder.createRoot()
                .addChild(signature.getControlByName("User")).linkToOuter(jeff1)
                .down().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).up()
        ;
        return builder.createBigraph();
    }


    public Bigraph createAgent_model_test_2() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.InnerName door = builder.createInnerName("door");
        BigraphEntity.OuterName e1 = builder.createOuterName("eroom");
        BigraphEntity.OuterName e0 = builder.createOuterName("espool");
        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b = builder.createOuterName("b");
        BigraphEntity.OuterName jeff1 = builder.createOuterName("jeff1");
        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");

//        big s1 = /door (
//                (Room{door} . (Computer{a}.1 | Computer{a}.(Job.1 | Job.(Job.1) | Job.1) | Computer{a}.1 | Computer{jeff}.1 | Job.1 ))
//                | (Spool{e0}.1)
//                | (Room{e1} . (User{jeff}.(Job.1 | Job.1) | Job.1 | Job.1))
//                );

        builder.createRoot()
                .addChild(signature.getControlByName("Room")).linkToInner(door)
                .down()
                .addChild(signature.getControlByName("Computer")) //.connectNodeToOuterName(a)
                .addChild(signature.getControlByName("Computer")).linkToOuter(a)
                .down()
                .addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("Job")).down().addChild(signature.getControlByName("Job")).up()
                .addChild(signature.getControlByName("Job")).up()
                .addChild(signature.getControlByName("Computer")).linkToOuter(a)
                .addChild(signature.getControlByName("Computer")).linkToOuter(jeff1)
                .addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("User"))
                .up()

                .addChild(signature.getControlByName("Spool")).linkToOuter(e0)

                .addChild(signature.getControlByName("Room")).linkToOuter(e1)
                .down()
                .addChild(signature.getControlByName("User")).linkToOuter(jeff2)
                .down().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).up()
                .addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("Job"))

        ;

//        builder.closeInnerName(roomLink);
//        builder.closeInnerName(printerSpoolLink);
        builder.closeAllInnerNames();
        builder.makeGround();

        PureBigraph bigraph = builder.createBigraph();
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
        DefaultDynamicSignature signature = createExampleSignature2();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy leftNode =
                builder.hierarchy(signature.getControlByName("Left"))
                        .addChild("Z");
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy rightNode =
                builder.hierarchy(signature.getControlByName("Right"))
                        .addChild("S");
//        for (int i = 0; i < left - 1; i++) {
//            leftNode = leftNode.withNewHierarchy().addChild("S");
//        }
//        leftNode = leftNode.withNewHierarchy().addChild("Z").top();
        for (int i = 0; i < right - 1; i++) {
            rightNode = rightNode.down().addChild("S");
        }
//        rightNode = rightNode.withNewHierarchy().addChild("Z").top();
        rightNode = rightNode.top();

        builder.createRoot()
                .addChild("Age")
                .down()
                .addChild(leftNode)
                .addChild(rightNode)
        ;
        builder.makeGround();
        return builder.createBigraph();
    }

    public static ReactionRule<PureBigraph> createReactionRule_3() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DefaultDynamicSignature signature = createExampleSignature2();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("Left").down().addSite()
                .top()
                .addChild("Right").down().addChild("Z")
        ;
        builder2.createRoot()
                .addChild("False").down().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

}
