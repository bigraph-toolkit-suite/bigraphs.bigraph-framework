/*
 * Copyright (c) 2024-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.analysis;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.alg.generators.PureBigraphGenerator;
import org.bigraphs.framework.core.alg.generators.RandomBigraphGeneratorSupport;
import org.bigraphs.framework.core.analysis.BigraphDecomposer;
import org.bigraphs.framework.core.analysis.PureBigraphDecomposerImpl;
import org.bigraphs.framework.core.analysis.PureLinkGraphConnectedComponents;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.simulation.BigraphUnitTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class BigraphConnectednessTest implements BigraphUnitTestSupport {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/analysis-connectedness/";
    private final static boolean AUTO_CLEAN_BEFORE = true;


    @BeforeAll
    static void setUp() throws IOException {
        if (AUTO_CLEAN_BEFORE) {
            File dump = new File(TARGET_DUMP_PATH);
            dump.mkdirs();
            FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
//            new File(TARGET_DUMP_PATH + "states/").mkdir();
        }
    }

    @Test
    void bigraph_getConnectedComponentBigraphs_test() throws InvalidConnectionException {
        PureBigraph bigraph = createFruitAgent();
        eb(bigraph, TARGET_DUMP_PATH + "fruitAgent");

        PureBigraphDecomposerImpl decomposer = BigraphDecomposer.create(BigraphDecomposer.DEFAULT_DECOMPOSITION_STRATEGY);
        decomposer.decompose(bigraph);
        System.out.println("Count: " + decomposer.getUnionFindDataStructure().getCount());
        System.out.println("Partitions: " + decomposer.getPartitions());

        List<PureBigraph> components = decomposer.getConnectedComponents();
        AtomicInteger cnt = new AtomicInteger(0);
        components.forEach(c -> {
            eb(c, TARGET_DUMP_PATH + "component_" + cnt.getAndIncrement());
        });
        System.out.println(components);
    }

    @Test
    void linkGraph_bigraphDecomposer_API_test() {
        DynamicSignature randomSignature = createRandomSignature(8, 1f, 3);
        RandomBigraphGeneratorSupport.LinkStrategy linkStrategy = RandomBigraphGeneratorSupport.LinkStrategy.MAXIMAL_DEGREE_ASSORTATIVE;
        int numOfTrees = 1;
        int numOfNodes = 10;
        float p = 1;
        PureBigraph generate = new PureBigraphGenerator(randomSignature)
                .setLinkStrategy(linkStrategy)
                .generate(numOfTrees, numOfNodes, p);
        eb(generate, TARGET_DUMP_PATH + "random-graph-1");

        BigraphDecomposer<PureBigraph> pureBigraphBigraphDecomposer = BigraphDecomposer.create(BigraphDecomposer.DEFAULT_DECOMPOSITION_STRATEGY);
        pureBigraphBigraphDecomposer.decompose(generate);

//        BigraphDecomposer<Bigraph<? extends Signature<?>>> bigraphBigraphDecomposer = BigraphDecomposer.create(BigraphDecomposer.DEFAULT_DECOMPOSITION_STRATEGY);
        PureBigraphDecomposerImpl pureBigraphDecomposer = BigraphDecomposer.create(BigraphDecomposer.DEFAULT_DECOMPOSITION_STRATEGY);
        pureBigraphDecomposer.decompose(generate);
        System.out.println("Count: " + pureBigraphDecomposer.getUnionFindDataStructure().getCount());
        System.out.println("Partitions: " + pureBigraphDecomposer.getPartitions());

        //TODO build bigraph of every partition
        //TODO build bigraph of every permutation of elements of this partition
    }

    @Test
    void linkGraph_connectedness_LowLevelAPI_test() {
        DynamicSignature randomSignature = createRandomSignature(8, 1f, 3);
        RandomBigraphGeneratorSupport.LinkStrategy linkStrategy = RandomBigraphGeneratorSupport.LinkStrategy.MAXIMAL_DEGREE_ASSORTATIVE;
        int numOfTrees = 1;
        int numOfNodes = 10;
        float p = 1;
        PureBigraph generate = new PureBigraphGenerator(randomSignature)
                .setLinkStrategy(linkStrategy)
                .generate(numOfTrees, numOfNodes, p);
        eb(generate, TARGET_DUMP_PATH + "random-graph-2");

//        BigraphAnalysis a = new BigraphAnalysis();
//        boolean b = a.linkGraphIsConnectedGraph(generate);
//        System.out.println("Is Connected: " + b);

        PureLinkGraphConnectedComponents cc = new PureLinkGraphConnectedComponents();
        cc.decompose(generate);
        PureLinkGraphConnectedComponents.UnionFind uf = cc.getUnionFindDataStructure();
        System.out.println("Connected Components: " + uf.getCount());
        System.out.println("# of Partition Roots: " + uf.countRoots(uf.getChildParentMap()));
        Set<Integer> rootsOfPartitions = uf.getRootsOfPartitions(uf.getChildParentMap());
        System.out.println("rootsOfPartitions: " + rootsOfPartitions);
//        System.out.println(uf.getRank());
        Map<Integer, List<BigraphEntity<?>>> partitions = cc.getPartitions();
        System.out.println("partitions: " + partitions);
    }

    private DynamicSignature createRandomSignature(int n, float probOfPositiveArity, int maxArity) {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();

        char[] chars = IntStream.rangeClosed('A', 'Z')
                .mapToObj(c -> "" + (char) c).collect(Collectors.joining()).toCharArray();

        int floorNum = (int) Math.ceil(n * probOfPositiveArity);
        for (int i = 0; i < floorNum; i++) {
            int ar = new Random().nextInt(maxArity) + 1;
            signatureBuilder = signatureBuilder.newControl().identifier(StringTypedName.of(String.valueOf(chars[i]))).arity(FiniteOrdinal.ofInteger(ar)).assign();
        }
        for (int i = floorNum; i < n; i++) {
            signatureBuilder = signatureBuilder.newControl().identifier(StringTypedName.of(String.valueOf(chars[i]))).arity(FiniteOrdinal.ofInteger(0)).assign();
        }
        DynamicSignature s = signatureBuilder.create();
        ArrayList<DynamicControl> cs = new ArrayList<>(s.getControls());
        Collections.shuffle(cs);
        return signatureBuilder.createWith(new LinkedHashSet<>(cs));
    }

    public static DynamicSignature createFruitSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add("User", 1, ControlStatus.ATOMIC)
                .add("Basket", 1)
                .add("Fruit", 1, ControlStatus.ATOMIC)
                .add("Table", 0)
                .add("Foo", 0)
        ;
        return defaultBuilder.create();
    }

    public static PureBigraph createFruitAgent() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(createFruitSignature());

        return b.root()
                .child("User")
                .child("Table").down()
                .child("Fruit", "f")
                .child("Fruit", "f")
                .child("Basket").down().child("Fruit", "f").child("Foo")
                .top()
                .create();

    }
}