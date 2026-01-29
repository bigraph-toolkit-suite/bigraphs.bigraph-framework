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
package org.bigraphs.framework.converter.graphml;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.bigraphs.framework.converter.BigraphPrettyPrinter;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.factory.BigraphFactory;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class Bigraph2GraphMLUnitTest {

    private static final String DUMP_TARGET = "src/test/resources/dump/";

    private static DynamicSignature sigABC() {
        DynamicSignatureBuilder defaultBuilder = BigraphFactory.pureSignatureBuilder();
        defaultBuilder
                .add("A", 4)
                .add("B", 3)
                .add("C", 2)
                .add("D", 1)
                .add("E", 0)
        ;
        return defaultBuilder.create();
    }

    private static DynamicSignature sigRobotZone() {
        return pureSignatureBuilder()
                .newControl("Robot", 1).assign()
                .newControl("Gripper", 1).assign()
                .newControl("Object", 1).assign()
                .newControl("Ownership", 1).assign()
                .newControl("Zone1", 0).assign()
                .newControl("Zone2", 0).assign()
                .newControl("Zone3", 0).assign()
                .create();
    }

    @Test
    void bigraph_2_graphml_test_01() {
        DynamicSignature signature = sigABC();
        PureBigraph generate = BigraphFactory.pureRandomBuilder(signature).generate(1, 10, 0.5f, 1f, 1f);

        PureBigraph2GraphMLPrettyPrinter graphMLPrettyPrinter = new PureBigraph2GraphMLPrettyPrinter();

        String s = graphMLPrettyPrinter.toString(generate);
        assertNotNull(s);
        assertNotEquals("", s);
        assertAll(() -> {
            graphMLPrettyPrinter.toOutputStream(generate, System.out);
        });
    }

    @Test
    @Disabled
    void robotZoneBigraph_2_graphml_test_02() throws InvalidConnectionException {
        PureBigraph pureBigraph = robotZoneBigraph();

        PureBigraph2GraphMLPrettyPrinter graphMLPrettyPrinter = new PureBigraph2GraphMLPrettyPrinter();
        String s = graphMLPrettyPrinter.toString(pureBigraph);
        System.out.println(s);

        assertNotNull(s);
        assertNotEquals("", s);
        assertAll(() -> {
            graphMLPrettyPrinter.toOutputStream(pureBigraph, System.out);
        });

        // XML validation
        assertAll(() -> {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            graphMLPrettyPrinter.toOutputStream(pureBigraph, os);

            URL resource = BigraphPrettyPrinter.class.getClassLoader().getResource("graphml.xsd");
            assertNotNull(resource);
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(resource);
            assertNotNull(schema);
            Validator validator = schema.newValidator();
            Source xmlFile = new StreamSource(new ByteArrayInputStream(os.toByteArray()));
            validator.validate(xmlFile);
        });
    }

    private PureBigraph robotZoneBigraph() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(sigRobotZone());
        PureBigraphBuilder<DynamicSignature>.Hierarchy zone1 = b.hierarchy("Zone1");
        PureBigraphBuilder<DynamicSignature>.Hierarchy zone2 = b.hierarchy("Zone2");
        zone1.child("Robot", "rId").down().child("Gripper", "canGrip");
        zone2.child("Zone3").child("Object", "isFree").down().child("Ownership", "belongsTo");
        b.root()
                .child(zone1).top().child(zone2).top();
        return b.create();
    }
}
