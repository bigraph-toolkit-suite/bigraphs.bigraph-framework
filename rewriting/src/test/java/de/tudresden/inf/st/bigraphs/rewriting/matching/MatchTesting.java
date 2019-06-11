package de.tudresden.inf.st.bigraphs.rewriting.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.matching.pure.PureBigraphMatcher;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Iterator;

public class MatchTesting {
    private PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = AbstractBigraphFactory.createPureBigraphFactory();

    @Test
    void model_test_0() throws Exception {
        PureBigraph agent_model_test_0 = (PureBigraph) createAgent_model_test_0();
        PureBigraph redex_model_test_0 = (PureBigraph) createRedex_model_test_0();
        ReactionRule<DefaultDynamicSignature> rr = new ParametricReactionRule<>(redex_model_test_0, redex_model_test_0);

//        PureBigraphMatchingEngine<PureBigraph> matchingEngine = new PureBigraphMatchingEngine<>(agent_model_test_0, redex_model_test_0);

//        matchingEngine.beginMatch();
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class); //new PureBigraphMatcher();
        MatchIterable match = matcher.match(agent_model_test_0, (PureBigraph) rr.getRedex());
        Iterator<BigraphMatch<?>> iterator = match.iterator();
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            System.out.println(next);
        }

    }

    @Test
    void model_test_1() throws Exception {
        PureBigraph agent_model_test_1 = (PureBigraph) createAgent_model_test_1();
        PureBigraph redex_model_test_1 = (PureBigraph) createRedex_model_test_1();

//        PureBigraphMatchingEngine<PureBigraph> matchingEngine = new PureBigraphMatchingEngine<>(agent_model_test_1, redex_model_test_1);
//        matchingEngine.beginMatch();
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

//        PureBigraphMatchingEngine<PureBigraph> matchingEngine = new PureBigraphMatchingEngine<>(agent_model_test_2, redex_model_test_2);
//
//        matchingEngine.beginMatch();
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
//        MatchIterable match = matcher.match(agent_model_test_2, redex_model_test_2a);
//        Iterator<BigraphMatch> iterator = match.iterator();
//        while (iterator.hasNext()) {
//            BigraphMatch next = iterator.next();
//            System.out.println(next);
//        }

        MatchIterable match = matcher.match(agent_model_test_2, redex_model_test_2b);
        Iterator<BigraphMatch<?>> iterator = match.iterator();
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            System.out.println(next);
        }

    }

    public Bigraph createRedex_model_test_3() throws LinkTypeNotExistsException, InvalidConnectionException {
        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        BigraphEntity.OuterName e0 = builder.createOuterName("e0");
        BigraphEntity.OuterName a1 = builder.createOuterName("a1");
        BigraphEntity.OuterName a2 = builder.createOuterName("a2");
        BigraphEntity.OuterName b2 = builder.createOuterName("b2");
        BigraphEntity.OuterName b3 = builder.createOuterName("b3");
        BigraphEntity.OuterName u1 = builder.createOuterName("u1");

//        big r = (
//                (Room{e0} . (Printer{a1, b2}.1))
//| (Room{e0} . (Printer{a2, b3}.1))
//| User{b4}.1
//);

        builder.createRoot()
                .addChild(signature.getControlByName("Room")).connectNodeToOuterName(e0)
                .withNewHierarchy().addChild(signature.getControlByName("Printer")).connectNodeToOuterName(a1).connectNodeToOuterName(b2)
                .goBack()

                .addChild(signature.getControlByName("Room")).connectNodeToOuterName(e0)
                .withNewHierarchy().addChild(signature.getControlByName("Printer")).connectNodeToOuterName(a2).connectNodeToOuterName(b3)
                .goBack()

                .addChild(signature.getControlByName("User")).connectNodeToOuterName(u1)
        ;

        return builder.createBigraph();
    }

    public Bigraph createAgent_model_test_3() throws LinkTypeNotExistsException, InvalidConnectionException {
        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        BigraphEntity.InnerName roomLink = builder.createInnerName("e0");
        BigraphEntity.OuterName a1 = builder.createOuterName("a1");
        BigraphEntity.OuterName a2 = builder.createOuterName("a2");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
        BigraphEntity.OuterName b2 = builder.createOuterName("b2");
        BigraphEntity.OuterName u1 = builder.createOuterName("u1");

        builder.createRoot()
                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
                .withNewHierarchy().addChild(signature.getControlByName("Printer")).connectNodeToOuterName(a1).connectNodeToOuterName(b1)
                .goBack()

                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
                .withNewHierarchy().addChild(signature.getControlByName("Printer")).connectNodeToOuterName(a2).connectNodeToOuterName(b2)
                .goBack()

                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
                .withNewHierarchy().addChild(signature.getControlByName("Printer")).connectNodeToOuterName(a2).connectNodeToOuterName(b2)
                .goBack()

                .addChild(signature.getControlByName("User")).connectNodeToOuterName(u1)
        ;

        builder.closeAllInnerNames();
        builder.makeGround();
        return builder.createBigraph();

    }

    public Bigraph createAgent_model_test_1() throws LinkTypeNotExistsException, InvalidConnectionException {
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

    public Bigraph createRedex_model_test_1() throws LinkTypeNotExistsException, InvalidConnectionException {
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

    public Bigraph createRedex_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException {
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

    public Bigraph createAgent_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
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
    public Bigraph createRedex_model_test_2a() throws LinkTypeNotExistsException, InvalidConnectionException {
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
    public Bigraph createRedex_model_test_2b() throws LinkTypeNotExistsException, InvalidConnectionException {
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


    public Bigraph createAgent_model_test_2() throws LinkTypeNotExistsException, InvalidConnectionException {
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
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign();

        return (S) defaultBuilder.create();
    }

}
