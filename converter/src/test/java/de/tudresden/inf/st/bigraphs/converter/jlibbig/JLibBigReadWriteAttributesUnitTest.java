package de.tudresden.inf.st.bigraphs.converter.jlibbig;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DynamicSignatureBuilder;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureBuilder;
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureSignatureBuilder;

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
        DefaultDynamicSignature signature = signature00();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        builder.createRoot()
                .addChild("Building")
                .down().addChild("Room").down().addChild("Room").down().addChild("User").up().up()
                .addChild("Room").down().addChild("User");
        PureBigraph bigraph = builder.createBigraph();
        bigraph.getNodes().forEach(x -> {
            Map<String, Object> attributes = x.getAttributes();
            attributes.put("_id", x.getName());
            attributes.put("_eobject", x.getInstance());
            x.setAttributes(attributes);
        });
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
