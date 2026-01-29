/*
 * Copyright (c) 2019-2026 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.converter.vcg;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class VcgTransformationUnitTest {

    private static final String DUMP_TARGET = "src/test/resources/dump/";

    private static DynamicSignature createExampleSignature() {
        return pureSignatureBuilder()

                .add("Printer", 2)
                .add("Building", 0)
                .add("User", 1)
                .add("Room", 1)
                .add("Spool", 1)
                .add("Computer", 1)
                .add("Job", 0)
                .add("A", 1)
                .add("B", 1)
                .create();
    }

    /**
     * ycomp ycomp-test.vcg
     */
    @Test
    void convert() throws InvalidConnectionException, TypeNotExistsException, IOException {
        PureBigraph big = createBigraph();
//        BigraphFileModelManagement.Store.exportAsInstanceModel(big, System.out);

        VCGTransformator vcgTransformator = new VCGTransformator();
        String s = vcgTransformator.toString(big);
        System.out.println(s);

        FileOutputStream fout = new FileOutputStream(DUMP_TARGET + "ycomp-test.vcg");
        vcgTransformator.toOutputStream(big, fout);
        fout.close();
    }

    private PureBigraph createBigraph() throws ControlIsAtomicException, InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName idleOuter = builder.createOuter("idle2");
        BigraphEntity.OuterName jeff1 = builder.createOuter("jeff1");
        BigraphEntity.OuterName jeff2 = builder.createOuter("jeff2");
        BigraphEntity.OuterName b1 = builder.createOuter("b1");
        BigraphEntity.OuterName a = builder.createOuter("a");
        BigraphEntity.OuterName b = builder.createOuter("b");
        BigraphEntity.InnerName e1 = builder.createInner("e1");
        BigraphEntity.InnerName idleInner = builder.createInner("idle1");


        builder.root()
                .child("Printer").linkOuter(a).linkOuter(b)
                .child(signature.getControlByName("Room")).linkInner(e1)
                .down()
                .site()
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down().child(signature.getControlByName("Job")).up()
                .child(signature.getControlByName("User")).linkOuter(jeff1)
                .up()

                .child(signature.getControlByName("Room")).linkInner(e1)
                .down()
                .site()
                .child(signature.getControlByName("Computer")).linkOuter(b1)
                .down().child(signature.getControlByName("Job")).child(signature.getControlByName("User")).linkOuter(jeff2)
                .up().up();

        return builder.create();
    }
}
