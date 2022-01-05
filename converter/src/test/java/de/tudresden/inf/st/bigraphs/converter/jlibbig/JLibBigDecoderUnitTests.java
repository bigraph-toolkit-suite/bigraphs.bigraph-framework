package de.tudresden.inf.st.bigraphs.converter.jlibbig;

import de.tudresden.inf.st.bigraphs.core.BigraphFileModelManagement;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureBuilder;
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureSignatureBuilder;

public class JLibBigDecoderUnitTests {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/jlibbig/";

    @Test
    void decode_01() throws InvalidConnectionException, TypeNotExistsException, IOException {
        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
        PureBigraph big_00 = big_00();

        Bigraph encoded = encoder.encode(big_00);
        System.out.println(encoded.toString());

        PureBigraph decoded = decoder.decode(encoded);
        BigraphFileModelManagement.Store.exportAsInstanceModel(decoded, System.out);

        BigraphGraphvizExporter.toPNG(big_00,
                true,
                new File(TARGET_DUMP_PATH + "original.png")
        );
        BigraphGraphvizExporter.toPNG(decoded,
                true,
                new File(TARGET_DUMP_PATH + "decoded.png")
        );
    }

    @Test
    void decode_02() throws InvalidConnectionException, TypeNotExistsException, IOException {
        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();

        PureBigraph big_02 = big_02();

        Bigraph encoded = encoder.encode(big_02);
        System.out.println(encoded.toString());

        PureBigraph decoded = decoder.decode(encoded);
        BigraphFileModelManagement.Store.exportAsInstanceModel(decoded, System.out);

        BigraphGraphvizExporter.toPNG(big_02,
                true,
                new File(TARGET_DUMP_PATH + "original.png")
        );
        BigraphGraphvizExporter.toPNG(decoded,
                true,
                new File(TARGET_DUMP_PATH + "decoded.png")
        );
    }

    @Test
    void decode_03() throws InvalidConnectionException, TypeNotExistsException, IOException {
        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();

        PureBigraph big_03 = big_03();
        Bigraph encoded = encoder.encode(big_03);
        System.out.println(encoded.toString());

        PureBigraph decoded = decoder.decode(encoded);
        BigraphFileModelManagement.Store.exportAsInstanceModel(decoded, System.out);

        BigraphGraphvizExporter.toPNG(big_03,
                true,
                new File(TARGET_DUMP_PATH + "original.png")
        );
        BigraphGraphvizExporter.toPNG(decoded,
                true,
                new File(TARGET_DUMP_PATH + "decoded.png")
        );
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
