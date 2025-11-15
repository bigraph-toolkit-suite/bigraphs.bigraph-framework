package org.bigraphs.framework.converter.jlibbig;

import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

public class JLibBigReadWriteAttributesUnitTest {

    @Test
    void write_and_read_attributes() {
        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();

        PureBigraph pureBigraph = big_00();
        Bigraph jBigraph = encoder.encode(pureBigraph);

        PureBigraph decodedPureBigraph = decoder.decode(jBigraph);
        System.out.println(decodedPureBigraph);
    }

    private PureBigraph big_00() {
        DynamicSignature signature = signature00();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        builder.root()
                .child("Building")
                .down().child("Room").down().child("Room").down().child("User").up().up()
                .child("Room").down().child("User");
        PureBigraph bigraph = builder.create();
        bigraph.getNodes().forEach(x -> {
            Map<String, Object> attributes = x.getAttributes();
            attributes.put("_id", x.getName());
            attributes.put("_eobject", x.getInstance());
            x.setAttributes(attributes);
        });
        return bigraph;
    }

    private static DynamicSignature signature00() {
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
                .add("B", 1);

        return defaultBuilder.create();
    }
}
