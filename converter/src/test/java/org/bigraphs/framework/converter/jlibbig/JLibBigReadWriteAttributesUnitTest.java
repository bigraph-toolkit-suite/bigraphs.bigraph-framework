/*
 * Copyright (c) 2023-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.converter.jlibbig;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

import it.uniud.mads.jlibbig.core.std.Bigraph;
import java.util.Map;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
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
