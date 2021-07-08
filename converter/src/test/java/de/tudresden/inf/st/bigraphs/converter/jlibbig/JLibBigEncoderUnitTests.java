package de.tudresden.inf.st.bigraphs.converter.jlibbig;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import org.junit.jupiter.api.Test;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureBuilder;
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureSignatureBuilder;

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
        DefaultDynamicSignature signature = signature00();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        builder.createRoot()
                .addChild("Building")
                .down().addChild("Room").down().addChild("Room").down().addChild("User").up().up()
                .addChild("Room").down().addChild("User");
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    /**
     * A simple bigraph with 2 sites, no links
     */
    private PureBigraph big_01() {
        DefaultDynamicSignature signature = signature00();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        builder.createRoot()
                .addChild("Building")
                .down().addChild("Room").down().addChild("Room").down().addSite().addChild("User").addSite().up().up()
                .addChild("Room").down().addChild("User").addSite();
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    /**
     * A simple bigraph with 3 sites, top-most rooms are connected via outer name
     * User nodes are connected via inner name
     */
    private PureBigraph big_02() throws InvalidConnectionException, TypeNotExistsException {
        DefaultDynamicSignature signature = signature00();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName door = builder.createOuterName("door");
        BigraphEntity.InnerName network = builder.createInnerName("network");
        builder.createRoot()
                .addChild("Building")
                .down()
                .addChild("Printer")
                .addChild("Room").linkToOuter(door).down().addChild("Room").down().addSite().addChild("User").linkToInner(network).addSite().up().up()
                .addChild("Room").linkToOuter(door).down().addChild("User").linkToInner(network).addSite();
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    /**
     * A simple bigraph with 3 sites, top-most rooms are connected via outer name
     * User nodes are connected via an edge
     */
    private PureBigraph big_03() throws InvalidConnectionException, TypeNotExistsException {
        DefaultDynamicSignature signature = signature00();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName door = builder.createOuterName("door");
        BigraphEntity.InnerName tmp = builder.createInnerName("network");
        builder.createRoot()
                .addChild("Building")
                .down()
                .addChild("Printer")
                .addChild("Room").linkToOuter(door).down().addChild("Room").down().addSite().addChild("User").linkToInner(tmp).addSite().up().up()
                .addChild("Room").linkToOuter(door).down().addChild("User").linkToInner(tmp).addSite();
        builder.closeInnerName(tmp);
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    //mit 1 root und 2 Rooms, no links
    private PureBigraph redex_01() {
        DefaultDynamicSignature signature = signature00();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        builder.createRoot().addChild("Room").down().addSite()
                .up().addChild("Room").down().addSite();
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    //mit 2 roots, 2 rooms, no links
    private PureBigraph redex_02() {
        DefaultDynamicSignature signature = signature00();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        builder.createRoot().addChild("Room").down().addSite();
        builder.createRoot().addChild("Room").down().addSite();
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    private static DefaultDynamicSignature signature00() {
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
