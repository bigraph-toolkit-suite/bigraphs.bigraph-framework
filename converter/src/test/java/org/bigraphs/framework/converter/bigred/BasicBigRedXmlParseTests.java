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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class BasicBigRedXmlParseTests {

    private static final String DUMP_TARGET = "src/test/resources/dump/";

    @Test
    void read_signature_test() {
        URL resource = getClass().getResource("/bigred/misc-examples/signatures/sampleA.bigraph-signature");
        DefaultSignatureXMLLoader loader = new DefaultSignatureXMLLoader();
        loader.readXml(resource.getPath());
        DynamicSignature signature2 = loader.importObject();
        MutableMap<String, Integer> with = Maps.mutable.with("Person", 3, "Computer", 0, "User", 1, "Room", 2);
        signature2.getControls().forEach(x -> {
            assertNotNull(with.get(x.getNamedType().stringValue()));
            assertEquals(with.get(x.getNamedType().stringValue()), x.getArity().getValue());
        });
    }

    @Test
    void read_signature2_test() {
        URL resource = getClass().getResource("/bigred/misc-examples/signatures/sampleB.bigraph-signature");
        DefaultSignatureXMLLoader loader = new DefaultSignatureXMLLoader();
        loader.readXml(resource.getPath());
        DynamicSignature signature2 = loader.importObject();
        MutableMap<String, Integer> with = Maps.mutable.with("Control3", 0, "Control1", 2, "Control2", 0, "Control4", 0);
        signature2.getControls().forEach(x -> {
            assertNotNull(with.get(x.getNamedType().stringValue()));
            assertEquals(with.get(x.getNamedType().stringValue()), x.getArity().getValue());
        });
    }

    @Test
    void read_bigraph_test() throws IOException {
        URL resource = getClass().getResource("/bigred/misc-examples/agents/sampleB.bigraph-agent");
        DefaultBigraphXMLLoader bxl = new DefaultBigraphXMLLoader();
        bxl.readXml(resource.getPath());
        PureBigraph bigraph = bxl.importObject();
        BigraphFileModelManagement.Store.exportAsMetaModel(bigraph, new FileOutputStream(new File(DUMP_TARGET + "./bigred/test-sampleB.ecore")));
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, new FileOutputStream(new File(DUMP_TARGET + "./bigred/test-sampleB.xmi")));
        String s = BigraphGraphvizExporter.toPNG(bigraph, true, (new File(DUMP_TARGET + "./bigred//test-sampleB.png")));
    }
}
