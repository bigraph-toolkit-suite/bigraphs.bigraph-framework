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
package org.bigraphs.framework.visualization;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

import org.bigraphs.framework.core.alg.generators.PureBigraphGenerator;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.junit.jupiter.api.Disabled;

@Disabled
public class DemoBigraphProvider {

    public PureBigraph getRandomBigraphMultipleRoots(int root, int nodes) {
        return new PureBigraphGenerator(createRandomSignature()).generate(root, nodes, 1.f);
    }

    public PureBigraph getRandomBigraphMultipleRoots(int root, int nodes, int sites) {
        return new PureBigraphGenerator(createRandomSignature()).generate(root, nodes, sites, 1.f);
    }

    public PureBigraph getRandomBigraphSingleRoot(int n, int s) {
        return new PureBigraphGenerator(createRandomSignature()).generate(1, n, s, 1.f);
    }

    public PureBigraph getRandomBigraphSingleRoot(int n, int s, DynamicSignature signature) {
        return new PureBigraphGenerator(signature).generate(1, n, s, 1.f);
    }

    public PureBigraph getRandomBigraphSingleRootOnlyOuternames(int n, int s) {
        return new PureBigraphGenerator(createRandomSignature()).generate(1, n, s, 1.f, 1.f, 0.f);
    }

    public PureBigraph getRandomBigraphSingleRootOnlyEdges(int n, int s) {
        return new PureBigraphGenerator(createRandomSignature()).generate(1, n, s, 1.f, 0.f, 1.f);
    }

    private DynamicSignature createRandomSignature() {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder
                .add("Printer", 2)
                .add("User", 1)
                .add("Room", 1)
                .add("Spool", 1)
                .add("Computer", 1)
                .add("Job", 0);

        return signatureBuilder.create();
    }

    public DynamicSignature createAlphabetSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add("A", 5)
                .add("B", 5)
                .add("C", 5)
                .add("D", 5)
                .add("E", 5)
                .add("F", 5)
                .add("G", 5)
                .add("H", 5)
                .add("I", 5)
                .add("J", 5)
                .add("K", 5)
                .add("Q", 5)
                .add("R", 5)
        ;

        return defaultBuilder.create();
    }
}
