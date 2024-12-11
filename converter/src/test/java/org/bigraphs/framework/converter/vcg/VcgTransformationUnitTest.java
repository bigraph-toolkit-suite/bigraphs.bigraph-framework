package org.bigraphs.framework.converter.vcg;

import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
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
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName idleOuter = builder.createOuterName("idle2");
        BigraphEntity.OuterName jeff1 = builder.createOuterName("jeff1");
        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b = builder.createOuterName("b");
        BigraphEntity.InnerName e1 = builder.createInnerName("e1");
        BigraphEntity.InnerName idleInner = builder.createInnerName("idle1");


        builder.createRoot()
                .addChild("Printer").linkToOuter(a).linkToOuter(b)
                .addChild(signature.getControlByName("Room")).linkToInner(e1)
                .down()
                .addSite()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down().addChild(signature.getControlByName("Job")).up()
                .addChild(signature.getControlByName("User")).linkToOuter(jeff1)
                .up()

                .addChild(signature.getControlByName("Room")).linkToInner(e1)
                .down()
                .addSite()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down().addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("User")).linkToOuter(jeff2)
                .up().up();

//        builder.closeAllInnerNames();
//        builder.makeGround();
        return builder.createBigraph();
    }

    private static <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .addControl("Printer", 2)
                .addControl("Building", 0)
                .addControl("User", 1)
                .addControl("Room", 1)
                .addControl("Spool", 1)
                .addControl("Computer", 1)
                .addControl("Job", 0)
                .addControl("A", 1)
                .addControl("B", 1)
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
