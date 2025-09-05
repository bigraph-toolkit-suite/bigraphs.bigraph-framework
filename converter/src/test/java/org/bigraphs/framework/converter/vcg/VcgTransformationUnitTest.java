package org.bigraphs.framework.converter.vcg;

import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

public class VcgTransformationUnitTest {
    private static final String DUMP_TARGET = "src/test/resources/dump/";

    /**
     * ycomp ycomp-test.vcg
     */
    @Test
    void name() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, IOException {
        PureBigraph big = createBigraph();
//        BigraphFileModelManagement.Store.exportAsInstanceModel(big, System.out);

        VCGTransformator vcgTransformator = new VCGTransformator();
        String s = vcgTransformator.toString(big);
        System.out.println(s);

        FileOutputStream fout = new FileOutputStream(DUMP_TARGET + "ycomp-test.vcg");
        vcgTransformator.toOutputStream(big, fout);
        fout.close();
    }

    public PureBigraph createBigraph() throws ControlIsAtomicException, InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName idleOuter = builder.createOuter("idle2");
        BigraphEntity.OuterName jeff1 = builder.createOuter("jeff1");
        BigraphEntity.OuterName jeff2 = builder.createOuter("jeff2");
        BigraphEntity.OuterName b1 = builder.createOuter("b1");
        BigraphEntity.OuterName a = builder.createOuter("a");
        BigraphEntity.OuterName b = builder.createOuter("b");
        BigraphEntity.InnerName e1 = builder.createInner("e1");
        BigraphEntity.InnerName idleInner = builder.createInner("idle1");


        builder.root()
                .child("Printer").linkOuter(a).linkOuter(b)
                .child(signature.getControlByName("Room")).linkInner(e1)
                .down()
                .site()
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down().child(signature.getControlByName("Job")).up()
                .child(signature.getControlByName("User")).linkOuter(jeff1)
                .up()

                .child(signature.getControlByName("Room")).linkInner(e1)
                .down()
                .site()
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down().child(signature.getControlByName("Job")).child(signature.getControlByName("User")).linkOuter(jeff2)
                .up().up();

//        builder.closeAllInnerNames();
//        builder.makeGround();
        return builder.create();
    }

    private static <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add("Printer", 2)
                .add("Building", 0)
                .add("User", 1)
                .add("Room", 1)
                .add("Spool", 1)
                .add("Computer", 1)
                .add("Job", 0)
                .add("A", 1)
                .add("B", 1)
//                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
//                .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(0)).assign()
//                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign()
//                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(1)).assign();
        ;
        return (S) defaultBuilder.create();
    }
}
