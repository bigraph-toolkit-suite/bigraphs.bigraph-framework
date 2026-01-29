/*
 * Copyright (c) 2021-2025 Bigraph Toolkit Suite Developers
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
import java.io.File;
import java.io.IOException;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class JLibBigDecoderUnitTests {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/jlibbig/";

    @Test
    void decode_01() throws InvalidConnectionException, TypeNotExistsException, IOException {
        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
        PureBigraph big_00 = big_00();

        Bigraph encoded = encoder.encode(big_00);
        System.out.println(encoded.toString());

        PureBigraph decoded = decoder.decode(encoded);
        BigraphFileModelManagement.Store.exportAsInstanceModel(decoded, System.out);

        BigraphGraphvizExporter.toPNG(big_00,
                true,
                new File(TARGET_DUMP_PATH + "original.png")
        );
        BigraphGraphvizExporter.toPNG(decoded,
                true,
                new File(TARGET_DUMP_PATH + "decoded.png")
        );
    }

    @Test
    void decode_02() throws InvalidConnectionException, TypeNotExistsException, IOException {
        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();

        PureBigraph big_02 = big_02();

        Bigraph encoded = encoder.encode(big_02);
        System.out.println(encoded.toString());

        PureBigraph decoded = decoder.decode(encoded);
        BigraphFileModelManagement.Store.exportAsInstanceModel(decoded, System.out);

        BigraphGraphvizExporter.toPNG(big_02,
                true,
                new File(TARGET_DUMP_PATH + "original.png")
        );
        BigraphGraphvizExporter.toPNG(decoded,
                true,
                new File(TARGET_DUMP_PATH + "decoded.png")
        );
    }

    @Test
    void decode_03() throws InvalidConnectionException, TypeNotExistsException, IOException {
        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();

        PureBigraph big_03 = big_03();
        Bigraph encoded = encoder.encode(big_03);
        System.out.println(encoded.toString());

        PureBigraph decoded = decoder.decode(encoded);
        BigraphFileModelManagement.Store.exportAsInstanceModel(decoded, System.out);

        BigraphGraphvizExporter.toPNG(big_03,
                true,
                new File(TARGET_DUMP_PATH + "original.png")
        );
        BigraphGraphvizExporter.toPNG(decoded,
                true,
                new File(TARGET_DUMP_PATH + "decoded.png")
        );
    }

    /**
     * A simple bigraph without sites, no links
     */
    private PureBigraph big_00() {
        DynamicSignature signature = signature00();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        builder.root()
                .child("Building")
                .down().child("Room").down().child("Room").down().child("User").up().up()
                .child("Room").down().child("User");
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    /**
     * A simple bigraph with 2 sites, no links
     */
    private PureBigraph big_01() {
        DynamicSignature signature = signature00();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        builder.root()
                .child("Building")
                .down().child("Room").down().child("Room").down().site().child("User").site().up().up()
                .child("Room").down().child("User").site();
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    /**
     * A simple bigraph with 3 sites, top-most rooms are connected via outer name
     * User nodes are connected via inner name
     */
    private PureBigraph big_02() throws InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = signature00();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName door = builder.createOuter("door");
        BigraphEntity.InnerName network = builder.createInner("network");
        builder.root()
                .child("Building")
                .down()
                .child("Printer")
                .child("Room").linkOuter(door).down().child("Room").down().site().child("User").linkInner(network).site().up().up()
                .child("Room").linkOuter(door).down().child("User").linkInner(network).site();
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    /**
     * A simple bigraph with 3 sites, top-most rooms are connected via outer name
     * User nodes are connected via an edge
     */
    private PureBigraph big_03() throws InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = signature00();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName door = builder.createOuter("door");
        BigraphEntity.InnerName tmp = builder.createInner("network");
        builder.root()
                .child("Building")
                .down()
                .child("Printer")
                .child("Room").linkOuter(door).down().child("Room").down().site().child("User").linkInner(tmp).site().up().up()
                .child("Room").linkOuter(door).down().child("User").linkInner(tmp).site();
        builder.closeInner(tmp);
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    //mit 1 root und 2 Rooms, no links
    private PureBigraph redex_01() {
        DynamicSignature signature = signature00();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        builder.root().child("Room").down().site()
                .up().child("Room").down().site();
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    //mit 2 roots, 2 rooms, no links
    private PureBigraph redex_02() {
        DynamicSignature signature = signature00();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        builder.root().child("Room").down().site();
        builder.root().child("Room").down().site();
        PureBigraph bigraph = builder.create();
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
