package de.tudresden.inf.st.bigraphs.rewriting.matching;

import com.google.common.base.Stopwatch;
import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.visualization.GraphvizConverter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//TODO write better tests here: the redex output should conform to the expected output - this makes observing things easier
// to the equivalent bigraphER output result (means, check for num. of outer names etc.)
public class MatchTesting {
    private PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = AbstractBigraphFactory.createPureBigraphFactory();

    void createGraphvizOutput(Bigraph<?> agent, BigraphMatch<?> next, String path) throws IncompatibleSignatureException, IncompatibleInterfaceException, IOException {
        PureBigraph context = (PureBigraph) next.getContext();
        PureBigraph redex = (PureBigraph) next.getRedex();
        PureBigraph contextIdentity = (PureBigraph) next.getContextIdentity();
        ElementaryBigraph identityForParams = next.getRedexIdentity();
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

            BigraphComposite bigraphComposite = factory
                    .asBigraphOperator(identityForParams).parallelProduct(redex);
            GraphvizConverter.toPNG(bigraphComposite.getOuterBigraph(),
                    true,
                    new File(path + "redexImage.png")
            );

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
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
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
            System.out.println(next);
        }

    }

    @Test
    void model_test_2() throws Exception {
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

        MatchIterable match2 = matcher.match(agent_model_test_2, redex_model_test_2b);
        Iterator<BigraphMatch<?>> iterator2 = match2.iterator();
        while (iterator2.hasNext()) {
            BigraphMatch<?> next = iterator2.next();
            System.out.println(next);
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
    void model_test_4() throws Exception {
        PureBigraph agent_model_test_4 = (PureBigraph) createAgent_model_test_4();
        PureBigraph redex_model_test_4 = (PureBigraph) createRedex_model_test_4();
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);

        MatchIterable match = matcher.match(agent_model_test_4, redex_model_test_4);
        Iterator<BigraphMatch<?>> iterator = match.iterator();
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            System.out.println(next);
        }

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

    public Bigraph createRedex_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
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

    public Bigraph createAgent_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, ControlIsAtomicException {
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
        builder.
                createRoot()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
                .withNewHierarchy().addSite().goBack()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(jeff)
                .addChild(signature.getControlByName("Job"))
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

    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(1)).assign();

        return (S) defaultBuilder.create();
    }

}
