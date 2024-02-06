package org.bigraphs.framework.converter.graphml;

import org.bigraphs.framework.converter.BigraphPrettyPrinter;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.factory.BigraphFactory;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.converter.graphml.PureBigraph2GraphMLPrettyPrinter;
import org.junit.jupiter.api.Disabled;
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

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dominik Grzelak
 */
public class Bigraph2GraphMLUnitTest {
    private static final String DUMP_TARGET = "src/test/resources/dump/";

    @Test
    void bigraph_2_graphml_test_01() {
        DefaultDynamicSignature signature = createSignature();
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
        PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(createRobotZoneSignature());
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy zone1 = b.hierarchy("Zone1");
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy zone2 = b.hierarchy("Zone2");
        zone1.addChild("Robot", "rId").down().addChild("Gripper", "canGrip");
        zone2.addChild("Zone3").addChild("Object", "isFree").down().addChild("Ownership", "belongsTo");
        b.createRoot()
                .addChild(zone1).top().addChild(zone2).top();
        return b.createBigraph();
    }

    private static DefaultDynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = BigraphFactory.pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(4)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(3)).assign()
                .newControl().identifier(StringTypedName.of("C")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("D")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("E")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;
        return defaultBuilder.create();
    }

    private DefaultDynamicSignature createRobotZoneSignature() {
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
}
