package org.bigraphs.framework.simulation.matching;

import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Matching2UnitTests {
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
        MatchIterable match = matcher.match(agent_model_test_0, rr2);
        Iterator<BigraphMatch<?>> iterator = match.iterator();
        int cnt = 0;
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            System.out.println("Match next=" + next);
            cnt++;
        }
        assertEquals(2, cnt);

        cnt = 0;
        MatchIterable match2 = matcher.match(agent_model_test_0, rr);
        Iterator<BigraphMatch<?>> iterator2 = match2.iterator();
        while (iterator2.hasNext()) {
            BigraphMatch<?> next2 = iterator2.next();
            System.out.println("Match next=" + next2);
            cnt++;
        }
        assertEquals(2, cnt);
    }


    public Bigraph createAgent_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

//        BigraphEntity.InnerName roomLink = builder.createInnerName("tmp1_room");
//        BigraphEntity.OuterName a = builder.createOuterName("a");
//        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
//        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");

        builder.root()
                .child(signature.getControlByName("A"))
                .child(signature.getControlByName("B"))
                .down()
                .child(signature.getControlByName("D"))
                .child(signature.getControlByName("E"))
                .up()
                .child(signature.getControlByName("B"))
                .down()
                .child(signature.getControlByName("D"))
                .child(signature.getControlByName("E"))
                .child(signature.getControlByName("F"))
                .child(signature.getControlByName("G"))
                .up()

//                .child(signature.getControlByName("Computer")).connectNodeToOuterName(a)
        ;
        builder.makeGround();

        PureBigraph bigraph = builder.create();
        return bigraph;

    }


    public Bigraph createRedex_model_test_0() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        builder.root()
                .child(signature.getControlByName("A"))
                .child(signature.getControlByName("B"))
                .down()
                .child(signature.getControlByName("D"))
                .child(signature.getControlByName("E"))
                .site()
        ;
//        builder.makeGround();

        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    //roots are separated
    public Bigraph createRedex_model_test_0v2() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        builder.root()
                .child(signature.getControlByName("A"));
        builder.root()
                .child(signature.getControlByName("B"))
                .down()
                .child(signature.getControlByName("D"))
                .child(signature.getControlByName("E"))
                .site()
        ;
        PureBigraph bigraph = builder.create();
        return bigraph;
    }


    private DynamicSignature createExampleSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("C")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("D")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("E")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("F")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("G")).arity(FiniteOrdinal.ofInteger(1)).assign();

        return defaultBuilder.create();
    }

}
