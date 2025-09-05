package org.bigraphs.framework.converter.bigred;

import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraph.model.SignatureAdapter;
import org.bigraph.model.savers.SignatureXMLSaver;
import org.junit.jupiter.api.Test;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Dominik Grzelak
 */
public class BasicBigRedXmlWriteTests {

    @Test
    void write_test() {
        assertAll(() -> {

            DynamicSignature signature = createSignature();
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

            // Get the schema file directly from the bigred-core dependency
            URL resource = org.bigraph.model.Signature.class.getClassLoader().getResource("resources/schema/signature.xsd");
            assertNotNull(resource);
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(resource);
            assertNotNull(schema);
            Validator validator = schema.newValidator();
            Source xmlFile = new StreamSource(new ByteArrayInputStream(s.getBytes()));
            validator.validate(xmlFile);
        });
    }

    private static DynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Person")).arity(FiniteOrdinal.ofInteger(3)).status(ControlStatus.ATOMIC).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(2)).status(ControlStatus.PASSIVE).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).status(ControlStatus.ACTIVE).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;

        return defaultBuilder.create();
    }
}
