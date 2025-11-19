/*
 * Copyright (c) 2020-2025 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.converter.bigred;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.bigraph.model.SignatureAdapter;
import org.bigraph.model.savers.SignatureXMLSaver;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.junit.jupiter.api.Test;

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
            URL resource = org.bigraph.model.Signature.class.getClassLoader().getResource("schema/signature.xsd");
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
                .add("Person", 3, ControlStatus.ATOMIC)
                .add("Room", 2, ControlStatus.PASSIVE)
                .add("User", 1, ControlStatus.ACTIVE)
                .add("Computer", 0)
        ;

        return defaultBuilder.create();
    }
}
