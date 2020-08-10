package de.tudresden.inf.st.bigraphs.converter.bigred;

import de.tudresden.inf.st.bigraphs.core.ControlKind;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import org.bigraph.model.Signature;
import org.bigraph.model.SignatureAdapter;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.loaders.SignatureXMLLoader;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.SignatureXMLSaver;
import org.junit.jupiter.api.Test;

import java.io.*;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pure;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dominik Grzelak
 */
public class BasicBigRedXmlWriteTests {

    @Test
    void write_test() {
        assertAll(() -> {

            DefaultDynamicSignature signature = createSignature();
            SignatureAdapter signatureAdapter = new SignatureAdapter(signature);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            SignatureXMLSaver sx = new SignatureXMLSaver();
            sx.setModel(signatureAdapter)
                    .setOutputStream(bos);
            sx.exportObject();
            String match = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<signature:signature xmlns:signature=\"http://www.itu.dk/research/pls/xmlns/2010/signature\">\n" +
                    "  <signature:control kind=\"active\" name=\"User\">\n" +
                    "    <signature:port name=\"0\"/>\n" +
                    "  </signature:control>\n" +
                    "  <signature:control kind=\"atomic\" name=\"Person\">\n" +
                    "    <signature:port name=\"0\"/>\n" +
                    "    <signature:port name=\"1\"/>\n" +
                    "    <signature:port name=\"2\"/>\n" +
                    "  </signature:control>\n" +
                    "  <signature:control kind=\"passive\" name=\"Room\">\n" +
                    "    <signature:port name=\"0\"/>\n" +
                    "    <signature:port name=\"1\"/>\n" +
                    "  </signature:control>\n" +
                    "  <signature:control kind=\"active\" name=\"Computer\"/>\n" +
                    "</signature:signature>\n";
            String match2 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<signature:signature xmlns:signature=\"http://www.itu.dk/research/pls/xmlns/2010/signature\">\n" +
                    "  <signature:control kind=\"passive\" name=\"Room\">\n" +
                    "    <signature:port name=\"0\"/>\n" +
                    "    <signature:port name=\"1\"/>\n" +
                    "  </signature:control>\n" +
                    "  <signature:control kind=\"atomic\" name=\"Person\">\n" +
                    "    <signature:port name=\"0\"/>\n" +
                    "    <signature:port name=\"1\"/>\n" +
                    "    <signature:port name=\"2\"/>\n" +
                    "  </signature:control>\n" +
                    "  <signature:control kind=\"active\" name=\"User\">\n" +
                    "    <signature:port name=\"0\"/>\n" +
                    "  </signature:control>\n" +
                    "  <signature:control kind=\"active\" name=\"Computer\"/>\n" +
                    "</signature:signature>\n";
            String match3 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<signature:signature xmlns:signature=\"http://www.itu.dk/research/pls/xmlns/2010/signature\">\n" +
                    "  <signature:control kind=\"atomic\" name=\"Person\">\n" +
                    "    <signature:port name=\"0\"/>\n" +
                    "    <signature:port name=\"1\"/>\n" +
                    "    <signature:port name=\"2\"/>\n" +
                    "  </signature:control>\n" +
                    "  <signature:control kind=\"active\" name=\"Computer\"/>\n" +
                    "  <signature:control kind=\"passive\" name=\"Room\">\n" +
                    "    <signature:port name=\"0\"/>\n" +
                    "    <signature:port name=\"1\"/>\n" +
                    "  </signature:control>\n" +
                    "  <signature:control kind=\"active\" name=\"User\">\n" +
                    "    <signature:port name=\"0\"/>\n" +
                    "  </signature:control>\n" +
                    "</signature:signature>\n";
            String match4 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<signature:signature xmlns:signature=\"http://www.itu.dk/research/pls/xmlns/2010/signature\">\n" +
                    "  <signature:control kind=\"active\" name=\"User\">\n" +
                    "    <signature:port name=\"0\"/>\n" +
                    "  </signature:control>\n" +
                    "  <signature:control kind=\"passive\" name=\"Room\">\n" +
                    "    <signature:port name=\"0\"/>\n" +
                    "    <signature:port name=\"1\"/>\n" +
                    "  </signature:control>\n" +
                    "  <signature:control kind=\"atomic\" name=\"Person\">\n" +
                    "    <signature:port name=\"0\"/>\n" +
                    "    <signature:port name=\"1\"/>\n" +
                    "    <signature:port name=\"2\"/>\n" +
                    "  </signature:control>\n" +
                    "  <signature:control kind=\"active\" name=\"Computer\"/>\n" +
                    "</signature:signature>";
            String s = new String(bos.toByteArray());
            System.out.println(s);
//            assertTrue(s.equals(match) || s.equals(match2) || s.equals(match3)|| s.equals(match4));
            bos.close();
        });
    }

    private static DefaultDynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pure().createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Person")).arity(FiniteOrdinal.ofInteger(3)).kind(ControlKind.ATOMIC).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(2)).kind(ControlKind.PASSIVE).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).kind(ControlKind.ACTIVE).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;

        return defaultBuilder.create();
    }
}
