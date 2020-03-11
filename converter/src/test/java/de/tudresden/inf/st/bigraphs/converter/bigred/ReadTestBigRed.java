package de.tudresden.inf.st.bigraphs.converter.bigred;

import de.tudresden.inf.st.bigraphs.core.BigraphArtifacts;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * @author Dominik Grzelak
 */
public class ReadTestBigRed {

    private static final String DUMP_TARGET = "src/test/resources/dump/";

    @Test
    void read_signature_test() {
        URL resource = getClass().getResource("/bigred/signatures/sampleA.bigraph-signature");
        DefaultSignatureXMLLoader loader = new DefaultSignatureXMLLoader();
        loader.readXml(resource.getPath());
        DefaultDynamicSignature signature2 = loader.importObject();
    }

    @Test
    void read_bigraph_test() throws IOException {
        URL resource = getClass().getResource("/bigred/agents/sampleB.bigraph-agent");
        PureBigraphXMLLoader bxl = new PureBigraphXMLLoader();
        bxl.readXml(resource.getPath());
        PureBigraph bigraph = bxl.importObject();
        BigraphArtifacts.exportAsMetaModel(bigraph, new FileOutputStream(new File(DUMP_TARGET + "./bigred/test-sampleB.ecore")));
        BigraphArtifacts.exportAsInstanceModel(bigraph, new FileOutputStream(new File(DUMP_TARGET + "./bigred/test-sampleB.xmi")));
        String s = BigraphGraphvizExporter.toPNG(bigraph, true, (new File(DUMP_TARGET + "./bigred//test-sampleB.png")));
    }
}
