package de.tudresden.inf.st.bigraphs.simulation.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.ReactionRule;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Matching2UnitTests {
    private PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/matching/framework/testforpaper/";

    void exportGraph(Bigraph<?> big, String path) {
        try {
            BigraphGraphvizExporter.toPNG((PureBigraph) big,
                    true,
                    new File(path)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void model_test_0() throws Exception {
        PureBigraph agent_model_test_0 = (PureBigraph) createAgent_model_test_0();
        PureBigraph redex_model_test_0 = (PureBigraph) createRedex_model_test_0();
        PureBigraph redex_model_test_0v2 = (PureBigraph) createRedex_model_test_0v2();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex_model_test_0, redex_model_test_0);
        ReactionRule<PureBigraph> rr2 = new ParametricReactionRule<>(redex_model_test_0v2, redex_model_test_0v2);
        exportGraph(agent_model_test_0, TARGET_DUMP_PATH + "agent.png");
        exportGraph(redex_model_test_0, TARGET_DUMP_PATH + "redex.png");
        exportGraph(redex_model_test_0v2, TARGET_DUMP_PATH + "redexv2.png");

        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable match = matcher.match(agent_model_test_0, rr2.getRedex());
        Iterator<BigraphMatch<?>> iterator = match.iterator();
        int cnt = 0;
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            System.out.println("Match next=" + next);
            cnt++;
        }
        assertEquals(2, cnt);

        cnt = 0;
        MatchIterable match2 = matcher.match(agent_model_test_0, rr.getRedex());
        Iterator<BigraphMatch<?>> iterator2 = match2.iterator();
        while (iterator2.hasNext()) {
            BigraphMatch<?> next2 = iterator2.next();
            System.out.println("Match next=" + next2);
            cnt++;
        }
        assertEquals(2, cnt);
    }


    public Bigraph createAgent_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, ControlIsAtomicException {
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

//        BigraphEntity.InnerName roomLink = builder.createInnerName("tmp1_room");
//        BigraphEntity.OuterName a = builder.createOuterName("a");
//        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
//        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");

        builder.createRoot()
                .addChild(signature.getControlByName("A"))
                .addChild(signature.getControlByName("B"))
                .down()
                .addChild(signature.getControlByName("D"))
                .addChild(signature.getControlByName("E"))
                .up()
                .addChild(signature.getControlByName("B"))
                .down()
                .addChild(signature.getControlByName("D"))
                .addChild(signature.getControlByName("E"))
                .addChild(signature.getControlByName("F"))
                .addChild(signature.getControlByName("G"))
                .up()

//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
        ;
        builder.makeGround();

        PureBigraph bigraph = builder.createBigraph();
        return bigraph;

    }


    public Bigraph createRedex_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild(signature.getControlByName("A"))
                .addChild(signature.getControlByName("B"))
                .down()
                .addChild(signature.getControlByName("D"))
                .addChild(signature.getControlByName("E"))
                .addSite()
        ;
//        builder.makeGround();

        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    //roots are separated
    public Bigraph createRedex_model_test_0v2() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild(signature.getControlByName("A"));
        builder.createRoot()
                .addChild(signature.getControlByName("B"))
                .down()
                .addChild(signature.getControlByName("D"))
                .addChild(signature.getControlByName("E"))
                .addSite()
        ;
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }


    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("C")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("D")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("E")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("F")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("G")).arity(FiniteOrdinal.ofInteger(1)).assign();

        return (S) defaultBuilder.create();
    }

}
