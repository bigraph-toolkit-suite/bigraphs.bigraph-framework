package de.tudresden.inf.st.bigraphs.rewriting.matching;

import com.google.common.base.Stopwatch;
import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.*;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.GroundReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.visualization.GraphvizConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//TODO write better tests here: the redex output should conform to the expected output - this makes observing things easier
// to the equivalent bigraphER output result (means, check for num. of outer names etc.)
public class MatchTesting {
    private static PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = AbstractBigraphFactory.createPureBigraphFactory();

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    void createGraphvizOutput(Bigraph<?> agent, BigraphMatch<?> next, String path) throws IncompatibleSignatureException, IncompatibleInterfaceException, IOException {
        PureBigraph context = (PureBigraph) next.getContext();
        PureBigraph redex = (PureBigraph) next.getRedex();
        Bigraph contextIdentity = next.getContextIdentity();
        ElementaryBigraph<DefaultDynamicSignature> identityForParams = next.getRedexIdentity();
        PureBigraph contextComposed = (PureBigraph) factory.asBigraphOperator(context).parallelProduct(contextIdentity).getOuterBigraph();
//            BigraphModelFileStore.exportAsInstanceModel(contextComposed, "contextComposed",
//                    new FileOutputStream("src/test/resources/graphviz/contextComposed.xmi"));
        GraphvizConverter.toPNG(contextComposed,
                true,
                new File(path + "contextComposed.png")
        );


        //This takes a lot if time!
        System.out.println("Create png's");
        Stopwatch timer = Stopwatch.createStarted();
        try {
            String convert = GraphvizConverter.toPNG(context,
                    true,
                    new File(path + "context.png")
            );
//            System.out.println(convert);
            GraphvizConverter.toPNG(agent,
                    true,
                    new File(path + "agent.png")
            );
            GraphvizConverter.toPNG(redex,
                    true,
                    new File(path + "redex.png")
            );
            GraphvizConverter.toPNG(contextIdentity,
                    true,
                    new File(path + "identityForContext.png")
            );
            GraphvizConverter.toPNG(identityForParams,
                    true,
                    new File(path + "identityForParams.png")
            );

//            BigraphComposite bigraphComposite = factory
//                    .asBigraphOperator(identityForParams).parallelProduct(redex); //.compose();
//            GraphvizConverter.toPNG(bigraphComposite.getOuterBigraph(),
//                    true,
//                    new File(path + "redexImage.png")
//            );

            AtomicInteger cnt = new AtomicInteger(0);
            next.getParameters().forEach(x -> {
                try {
                    GraphvizConverter.toPNG((PureBigraph) x,
                            true,
                            new File(path + "param_" + cnt.incrementAndGet() + ".png")
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            long elapsed = timer.stop().elapsed(TimeUnit.MILLISECONDS);
            System.out.println("Create png's took (millisecs) " + elapsed);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    void model_test_0() throws Exception {
        PureBigraph agent_model_test_0 = (PureBigraph) createAgent_model_test_0();
        PureBigraph redex_model_test_0 = (PureBigraph) createRedex_model_test_0();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex_model_test_0, redex_model_test_0);

        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class); //new PureBigraphMatcher();
        MatchIterable match = matcher.match(agent_model_test_0, rr.getRedex());
        Iterator<BigraphMatch<?>> iterator = match.iterator();
        int transition = 0;
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            createGraphvizOutput(agent_model_test_0, next, "src/test/resources/graphviz/model0/" + transition++ + "/");
            System.out.println("NEXT: " + next);
        }

    }

    @Test
    void model_test_1() throws Exception {
        PureBigraph agent_model_test_1 = (PureBigraph) createAgent_model_test_1();
        PureBigraph redex_model_test_1 = (PureBigraph) createRedex_model_test_1();

        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable match = matcher.match(agent_model_test_1, redex_model_test_1);
        Iterator<BigraphMatch<?>> iterator = match.iterator();
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            createGraphvizOutput(agent_model_test_1, next, "src/test/resources/graphviz/model1/");
            System.out.println(next);
        }

    }

    @Test
    @Benchmark
    @Fork(value = 1, warmups = 2)
    @BenchmarkMode(Mode.AverageTime)
    public void model_test_2() throws Exception {
        PureBigraph agent_model_test_2 = (PureBigraph) createAgent_model_test_2();
        PureBigraph redex_model_test_2a = (PureBigraph) createRedex_model_test_2a();
        //the second root of the redex will create many occurrences because a distinct match isn't possible
        PureBigraph redex_model_test_2b = (PureBigraph) createRedex_model_test_2b();

        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
//        MatchIterable match = matcher.match(agent_model_test_2, redex_model_test_2a);
//        Iterator<BigraphMatch<?>> iterator = match.iterator();
//        while (iterator.hasNext()) {
//            BigraphMatch next = iterator.next();
//            System.out.println(next);
//        }

        for (int i = 0; i < 1; i++) {
            Stopwatch timer0 = Stopwatch.createStarted();
            MatchIterable<BigraphMatch<PureBigraph>> match2 = matcher.match(agent_model_test_2, redex_model_test_2b);
            Iterator<BigraphMatch<PureBigraph>> iterator2 = match2.iterator();
            while (iterator2.hasNext()) {
                BigraphMatch<?> next = iterator2.next();
                long elapsed0 = timer0.stop().elapsed(TimeUnit.NANOSECONDS);
                System.out.println("Match time FULL (millisecs) " + (elapsed0 / 1e+6f));
                createGraphvizOutput(agent_model_test_2, next, "src/test/resources/graphviz/model2/");
//                System.out.println(next);
            }
        }

    }

    @Test
    void model_test_3() throws Exception {
        PureBigraph agent_model_test_3 = (PureBigraph) createAgent_model_test_3();
        PureBigraph redex_model_test_3 = (PureBigraph) createRedex_model_test_3();
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);

        MatchIterable match = matcher.match(agent_model_test_3, redex_model_test_3);
        Iterator<BigraphMatch<?>> iterator = match.iterator();
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            createGraphvizOutput(agent_model_test_3, next, "src/test/resources/graphviz/model3/");
            System.out.println(next);
        }

    }

    @Test
    @DisplayName("no matches should occur")
    void model_test_4() throws Exception {
        PureBigraph agent_model_test_4 = (PureBigraph) createAgent_model_test_4();
        PureBigraph redex_model_test_4 = (PureBigraph) createRedex_model_test_4();
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);

        MatchIterable match = matcher.match(agent_model_test_4, redex_model_test_4);
        Iterator<BigraphMatch<?>> iterator = match.iterator();
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
//            createGraphvizOutput(agent_model_test_4, next, "src/test/resources/graphviz/model4/");
            System.out.println(next);
        }

    }

    @Test
    @DisplayName("no matches should occur")
    void model_test_5() throws InvalidConnectionException, LinkTypeNotExistsException {
        PureBigraph agent_model_test_5 = (PureBigraph) createAgent_model_test_5();
        PureBigraph redex_model_test_5 = (PureBigraph) createRedex_model_test_5();
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);

        MatchIterable match = matcher.match(agent_model_test_5, redex_model_test_5);
        Iterator<BigraphMatch<?>> iterator = match.iterator();
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
//            createGraphvizOutput(agent_model_test_4, next, "src/test/resources/graphviz/model4/");
            System.out.println(next);
        }
    }

    @Test
    @DisplayName("no matches should occur")
    void match_test_6_counting() throws InvalidConnectionException, LinkTypeNotExistsException, IncompatibleInterfaceException, InvalidReactionRuleException, IOException {
        PureBigraph agent = createAgent_A(-1, 1);
//        createGraphvizOutput(agent_a, null, "src/test/resources/graphviz/model6/");
        GraphvizConverter.toPNG(agent,
                true,
                new File("src/test/resources/graphviz/model6/agent.png")
        );
        ReactionRule<PureBigraph> reactionRule_3 = createReactionRule_3();
        PureBigraph redex = reactionRule_3.getRedex();

        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);

        MatchIterable match = matcher.match(agent, redex);
        Iterator<BigraphMatch<?>> iterator = match.iterator();
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            createGraphvizOutput(agent, next, "src/test/resources/graphviz/model6/");
            System.out.println(next);
        }
    }

    @Test
    @DisplayName("matches should exist")
    void model_test_7() throws InvalidConnectionException, LinkTypeNotExistsException, IOException, IncompatibleSignatureException, IncompatibleInterfaceException {
        PureBigraph agent_model_test_7 = createAgent_model_test_7();
        GraphvizConverter.toPNG(agent_model_test_7,
                true,
                new File("src/test/resources/graphviz/model7/agent_7.png")
        );
        PureBigraph redex_7 = createRedex_7();
        GraphvizConverter.toPNG(redex_7,
                true,
                new File("src/test/resources/graphviz/model7/redex_7.png")
        );

        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);

        MatchIterable match = matcher.match(agent_model_test_7, redex_7);
        Iterator<BigraphMatch<?>> iterator = match.iterator();
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            createGraphvizOutput(agent_model_test_7, next, "src/test/resources/graphviz/model7/");
            System.out.println(next);
        }
    }

    @Test
    void idle_edge_test() throws InvalidConnectionException, TypeNotExistsException, IOException, InvalidReactionRuleException {
        PureBigraph agent = createAgent_idle_edge_test();
        GraphvizConverter.toPNG(agent,
                true,
                new File("src/test/resources/graphviz/model_idleedge/agent.png")
        );
        PureBigraph redex = createRedex_idle_edge_test();
        GraphvizConverter.toPNG(redex,
                true,
                new File("src/test/resources/graphviz/model_idleedge/redex.png")
        );

        GroundReactionRule<PureBigraph> rr = new GroundReactionRule<>(redex, redex);

        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);

        MatchIterable match = matcher.match(agent, redex);
        Iterator<BigraphMatch<?>> iterator = match.iterator();
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
//            createGraphvizOutput(agent_model_test_7, next, "src/test/resources/graphviz/model7/");
            System.out.println(next);
        }
    }

    //        big s0 = /a Building . (Room.(Printer{a,b}.1));
    public PureBigraph createAgent_idle_edge_test() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b = builder.createOuterName("b");
        builder.createRoot()
                .addChild("Building").withNewHierarchy().addChild("Room")
                .withNewHierarchy().addChild("Printer").connectNodeToOuterName(a)
                .connectNodeToOuterName(b)
        ;

        return builder.createBigraph();
    }

    public PureBigraph createRedex_idle_edge_test() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b = builder.createOuterName("b");
        builder.createRoot()
                .addChild("Room").withNewHierarchy()
                .addChild("Printer").connectNodeToOuterName(a)
                .connectNodeToOuterName(b)
        ;
        builder.closeOuterName(a, true);
        return builder.createBigraph();
    }

    public PureBigraph createAgent_model_test_7() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createSignatureABC();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy a = builder.newHierarchy(signature.getControlByName("A"));
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy b = builder.newHierarchy(signature.getControlByName("B"));

        a.addChild("B")
                .addChild("C")
                .withNewHierarchy().addChild("D").addChild("E");
        b.addChild("F").addChild("G").withNewHierarchy().addChild("H").addChild("I");

        builder.createRoot()
                .addChild(a.top())
                .addChild(b.top())
        ;
        return builder.createBigraph();
    }

    public PureBigraph createRedex_7() {
        DefaultDynamicSignature signature = createSignatureABC();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        builder.createRoot().addChild("G").withNewHierarchy().addChild("H").addChild("I");

//        builder.createRoot().addChild("C").withNewHierarchy().addChild("A").withNewHierarchy().addChild("D").addChild("E");
        return builder.createBigraph();
    }

    public Bigraph createAgent_model_test_5() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild(signature.getControlByName("Computer"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Room"))
                .addChild(signature.getControlByName("Job"));
        return builder.createBigraph();
    }

    public Bigraph createRedex_model_test_5() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer"))
                .addChild(signature.getControlByName("Job"));
        return builder.createBigraph();
    }

    public Bigraph createAgent_model_test_4() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        BigraphEntity.InnerName innerLink = builder.createInnerName("inner_link");
        BigraphEntity.OuterName network = builder.createOuterName("network");
        BigraphEntity.OuterName network1 = builder.createOuterName("network1");
        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b = builder.createOuterName("b");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room = builder.newHierarchy(signature.getControlByName("Room"));
        room.addChild(signature.getControlByName("Computer")).connectNodeToOuterName(network).withNewHierarchy()
                .addChild(signature.getControlByName("A"))
                .addChild(signature.getControlByName("A")).connectNodeToOuterName(b)
                .addChild(signature.getControlByName("A")).connectNodeToOuterName(b);

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room2 = builder.newHierarchy(signature.getControlByName("Room"));
        room2.addChild(signature.getControlByName("Computer")).connectNodeToOuterName(network1).withNewHierarchy()
                .addChild(signature.getControlByName("A")).connectNodeToOuterName(a)
                .addChild(signature.getControlByName("B")).connectNodeToInnerName(innerLink)
                .withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).goBack()
                .addChild(signature.getControlByName("A")).connectNodeToInnerName(innerLink);

        builder.createRoot().addChild(room).addChild(room2);
        builder.closeAllInnerNames();
        builder.makeGround();
        return builder.createBigraph();
    }

    public Bigraph createRedex_model_test_4() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuterName("network");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room = builder.newHierarchy(signature.getControlByName("Room"));
        room.addChild(signature.getControlByName("Computer")).connectNodeToOuterName(network).withNewHierarchy()
                .addSite();

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room2 = builder.newHierarchy(signature.getControlByName("Room"));
        room2.addChild(signature.getControlByName("Computer")).connectNodeToOuterName(network).withNewHierarchy()
                .addSite();

        builder.createRoot().addChild(room).addChild(room2);
        return builder.createBigraph();
    }


    public Bigraph createAgent_model_test_3() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        BigraphEntity.InnerName roomLink = builder.createInnerName("door");
        BigraphEntity.OuterName network = builder.createOuterName("network");
        BigraphEntity.OuterName jeff = builder.createOuterName("jeff");
        BigraphEntity.OuterName bob = builder.createOuterName("bob");
        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b = builder.createOuterName("b");

        builder.createRoot()
                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
                .withNewHierarchy().addChild(signature.getControlByName("User")).connectNodeToOuterName(bob)
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(network)
                .withNewHierarchy().addChild(signature.getControlByName("Job")).withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).goBack()
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(a)
                .withNewHierarchy().addChild(signature.getControlByName("User")).connectNodeToOuterName(b)
                .goBack()
                .goBack()
                .goBack()

                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
                .withNewHierarchy().addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff)
                .goBack()
        ;

        builder.closeAllInnerNames();
        builder.makeGround();
        return builder.createBigraph();

    }

    public Bigraph createRedex_model_test_3() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        BigraphEntity.OuterName network = builder.createOuterName("network");

        builder.createRoot()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(network)
                .withNewHierarchy().addSite().addChild(signature.getControlByName("Job")).withNewHierarchy().addSite();
        return builder.createBigraph();
    }

    public Bigraph createAgent_model_test_1() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName jeff1 = builder.createOuterName("jeff1");
        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
        BigraphEntity.InnerName e1 = builder.createInnerName("e1");

//        big s1 = /e1
//                ((Room{e1} . (Computer{b1}.(Job.1) | User{jeff1}.1 ))
//|
//        (Room{e1} . (Computer{b1}.(Job.1 | User{jeff2}.1))));

        builder.createRoot()
                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(e1)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
                .withNewHierarchy().addChild(signature.getControlByName("Job")).goBack()
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff1)
                .goBack()

                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(e1)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
                .withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff2)
                .goBack().goBack();

        builder.closeAllInnerNames();
        builder.makeGround();
        return builder.createBigraph();
    }

    public Bigraph createRedex_model_test_1() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName jeff1 = builder.createOuterName("jeff1");
        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");

        //(Computer{b1}.(Job.1) | User{jeff2}.1) || Computer{b1}.(Job.1 | User{jeff2}.1);

        builder.createRoot()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
                .withNewHierarchy().addChild(signature.getControlByName("Job")).goBack()
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff1);

        builder.createRoot()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
                .withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff2);

        builder.makeGround();
        return builder.createBigraph();

    }

    public static Bigraph createRedex_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b = builder.createOuterName("b");
        BigraphEntity.OuterName c = builder.createOuterName("c");
        BigraphEntity.OuterName d = builder.createOuterName("d");
        builder.
                createRoot()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
                .withNewHierarchy()
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(d)
                .addChild(signature.getControlByName("Job"))
//                .goBack()
        ;
        builder.createRoot()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(c)
                .withNewHierarchy()
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(b)
                .addChild(signature.getControlByName("Job"))
//                .goBack()
        ;
        return builder.createBigraph();
    }

    public static Bigraph createAgent_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        BigraphEntity.InnerName roomLink = builder.createInnerName("tmp1_room");
//        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");

        builder.createRoot()
                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff2)
                .goBack()
                .goBack()

                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff2)
                .goBack()
                .goBack()

//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
        ;
        builder.closeInnerName(roomLink);
        builder.closeAllInnerNames();
        builder.makeGround();

        PureBigraph bigraph = builder.createBigraph();
        return bigraph;

    }

    /**
     * Ground reaction rule that doesn't matches.
     *
     * @return
     * @throws LinkTypeNotExistsException
     * @throws InvalidConnectionException
     */
    public Bigraph createRedex_model_test_2a() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName jeff = builder.createOuterName("jeff1");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");


        // (Computer{b1}.(id(1)) | Computer{jeff1}.1 | Job.1) || (User{jeff1}.(Job.1 | Job.1));
//        builder.
//                createRoot()
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
//                .withNewHierarchy().addSite().goBack()
//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(jeff)
//                .addChild(signature.getControlByName("Job"))
        ;
        builder.createRoot()
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff)
                .withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).goBack()
        ;
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
    public Bigraph createRedex_model_test_2b() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
        BigraphEntity.OuterName c = builder.createOuterName("c");
        BigraphEntity.OuterName jeff1 = builder.createOuterName("jeff1");


        // (Computer{b1}.(id(1)) | Computer{jeff1}.1 | Job.1) || (User{jeff1}.(Job.1 | Job.1));
        builder.
                createRoot()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
                .withNewHierarchy().addSite().goBack()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(c)
                .addChild(signature.getControlByName("Job"))
        ;
        builder.createRoot()
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff1)
                .withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).goBack()
        ;
        return builder.createBigraph();
    }


    public Bigraph createAgent_model_test_2() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

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
                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(door)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer")) //.connectNodeToOuterName(a)
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("Job")).withNewHierarchy().addChild(signature.getControlByName("Job")).goBack()
                .addChild(signature.getControlByName("Job")).goBack()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(jeff1)
                .addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("User"))
                .goBack()

                .addChild(signature.getControlByName("Spool")).connectNodeToOuterName(e0)

                .addChild(signature.getControlByName("Room")).connectNodeToOuterName(e1)
                .withNewHierarchy()
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff2)
                .withNewHierarchy().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).goBack()
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
        DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> defaultBuilder = factory.createSignatureBuilder();
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
        DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> defaultBuilder = factory.createSignatureBuilder();
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
        DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> defaultBuilder = factory.createSignatureBuilder();
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
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature2();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy leftNode =
                builder.newHierarchy(signature.getControlByName("Left"))
                        .addChild("Z");
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy rightNode =
                builder.newHierarchy(signature.getControlByName("Right"))
                        .addChild("S");
//        for (int i = 0; i < left - 1; i++) {
//            leftNode = leftNode.withNewHierarchy().addChild("S");
//        }
//        leftNode = leftNode.withNewHierarchy().addChild("Z").top();
        for (int i = 0; i < right - 1; i++) {
            rightNode = rightNode.withNewHierarchy().addChild("S");
        }
//        rightNode = rightNode.withNewHierarchy().addChild("Z").top();
        rightNode = rightNode.top();

        builder.createRoot()
                .addChild("Age")
                .withNewHierarchy()
                .addChild(leftNode)
                .addChild(rightNode)
        ;
        builder.makeGround();
        return builder.createBigraph();
    }

    public static ReactionRule<PureBigraph> createReactionRule_3() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature2();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild("Left").withNewHierarchy().addSite()
                .top()
                .addChild("Right").withNewHierarchy().addChild("Z")
        ;
        builder2.createRoot()
                .addChild("False").withNewHierarchy().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

}
