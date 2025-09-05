package org.bigraphs.framework.converter.jlibbig;

import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import org.junit.jupiter.api.Test;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

public class JLibBigEncoderUnitTests {

    @Test
    void encode_01() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraph big_00 = big_00();
        PureBigraph big_01 = big_01();
        PureBigraph big_02 = big_02();
        PureBigraph big_03 = big_03();
        PureBigraph redex_01 = redex_01();
        PureBigraph redex_02 = redex_02();

        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();

        Bigraph encode = encoder.encode(big_00);
        System.out.println(encode.toString());

        Bigraph encode1 = encoder.encode(big_01);
        System.out.println(encode1.toString());

        Bigraph encode2 = encoder.encode(big_02);
        System.out.println(encode2.toString());

        Bigraph encode3 = encoder.encode(big_03);
        System.out.println(encode3.toString());

        Bigraph encodeRedex01 = encoder.encode(redex_01);
        System.out.println(encodeRedex01.toString());

        Bigraph encodeRedex02 = encoder.encode(redex_02);
        System.out.println(encodeRedex02.toString());
    }

    /**
     * A simple bigraph without sites, no links
     */
    private PureBigraph big_00() {
        DynamicSignature signature = signature00();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        builder.root()
                .child("Building")
                .down().child("Room").down().child("Room").down().child("User").up().up()
                .child("Room").down().child("User");
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    /**
     * A simple bigraph with 2 sites, no links
     */
    private PureBigraph big_01() {
        DynamicSignature signature = signature00();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        builder.root()
                .child("Building")
                .down().child("Room").down().child("Room").down().site().child("User").site().up().up()
                .child("Room").down().child("User").site();
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    /**
     * A simple bigraph with 3 sites, top-most rooms are connected via outer name
     * User nodes are connected via inner name
     */
    private PureBigraph big_02() throws InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = signature00();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName door = builder.createOuter("door");
        BigraphEntity.InnerName network = builder.createInner("network");
        builder.root()
                .child("Building")
                .down()
                .child("Printer")
                .child("Room").linkOuter(door).down().child("Room").down().site().child("User").linkInner(network).site().up().up()
                .child("Room").linkOuter(door).down().child("User").linkInner(network).site();
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    /**
     * A simple bigraph with 3 sites, top-most rooms are connected via outer name
     * User nodes are connected via an edge
     */
    private PureBigraph big_03() throws InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = signature00();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName door = builder.createOuter("door");
        BigraphEntity.InnerName tmp = builder.createInner("network");
        builder.root()
                .child("Building")
                .down()
                .child("Printer")
                .child("Room").linkOuter(door).down().child("Room").down().site().child("User").linkInner(tmp).site().up().up()
                .child("Room").linkOuter(door).down().child("User").linkInner(tmp).site();
        builder.closeInner(tmp);
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    //mit 1 root und 2 Rooms, no links
    private PureBigraph redex_01() {
        DynamicSignature signature = signature00();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        builder.root().child("Room").down().site()
                .up().child("Room").down().site();
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    //mit 2 roots, 2 rooms, no links
    private PureBigraph redex_02() {
        DynamicSignature signature = signature00();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        builder.root().child("Room").down().site();
        builder.root().child("Room").down().site();
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    private static DynamicSignature signature00() {
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

        return defaultBuilder.create();
    }
}
