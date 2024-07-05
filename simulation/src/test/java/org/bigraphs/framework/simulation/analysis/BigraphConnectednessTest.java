package org.bigraphs.framework.simulation.analysis;

import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.alg.generators.PureBigraphGenerator;
import org.bigraphs.framework.core.alg.generators.RandomBigraphGeneratorSupport;
import org.bigraphs.framework.core.analysis.BigraphAnalysis;
import org.bigraphs.framework.core.analysis.BigraphDecomposer;
import org.bigraphs.framework.core.analysis.PureBigraphDecomposerImpl;
import org.bigraphs.framework.core.analysis.PureLinkGraphConnectedComponents;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicControl;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.simulation.BigraphUnitTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

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

        PureBigraphDecomposerImpl decomposer = BigraphDecomposer.create(BigraphDecomposer.DEFAULT_DECOMPOSITION_STRATEGY, PureBigraphDecomposerImpl.class);
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
        DefaultDynamicSignature randomSignature = createRandomSignature(8, 1f, 3);
        RandomBigraphGeneratorSupport.LinkStrategy linkStrategy = RandomBigraphGeneratorSupport.LinkStrategy.MAXIMAL_DEGREE_ASSORTATIVE;
        int numOfTrees = 1;
        int numOfNodes = 10;
        float p = 1;
        PureBigraph generate = new PureBigraphGenerator(randomSignature)
                .setLinkStrategy(linkStrategy)
                .generate(numOfTrees, numOfNodes, p);
        eb(generate, TARGET_DUMP_PATH + "random-graph-1");

        BigraphDecomposer<PureBigraph> pureBigraphBigraphDecomposer = BigraphDecomposer.create();
        pureBigraphBigraphDecomposer.decompose(generate);

        BigraphDecomposer<Bigraph<? extends Signature<?>>> bigraphBigraphDecomposer = BigraphDecomposer.create(BigraphDecomposer.DEFAULT_DECOMPOSITION_STRATEGY);
        PureBigraphDecomposerImpl pureBigraphDecomposer = BigraphDecomposer.create(BigraphDecomposer.DEFAULT_DECOMPOSITION_STRATEGY, PureBigraphDecomposerImpl.class);
        pureBigraphDecomposer.decompose(generate);
        System.out.println("Count: " + pureBigraphDecomposer.getUnionFindDataStructure().getCount());
        System.out.println("Partitions: " + pureBigraphDecomposer.getPartitions());

        //TODO build bigraph of every partition
        //TODO build bigraph of every permutation of elements of this partition
    }

    @Test
    void linkGraph_connectedness_LowLevelAPI_test() {
        DefaultDynamicSignature randomSignature = createRandomSignature(8, 1f, 3);
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

    private DefaultDynamicSignature createRandomSignature(int n, float probOfPositiveArity, int maxArity) {
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
        DefaultDynamicSignature s = signatureBuilder.create();
        ArrayList<DefaultDynamicControl> cs = new ArrayList<>(s.getControls());
        Collections.shuffle(cs);
        return signatureBuilder.createWith(new LinkedHashSet<>(cs));
    }

    public static DefaultDynamicSignature createFruitSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .addControl("User", 1, ControlStatus.ATOMIC)
                .addControl("Basket", 1)
                .addControl("Fruit", 1, ControlStatus.ATOMIC)
                .addControl("Table", 0)
                .addControl("Foo", 0)
        ;
        return defaultBuilder.create();
    }

    public static PureBigraph createFruitAgent() throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(createFruitSignature());

        return b.createRoot()
                .addChild("User")
                .addChild("Table").down()
                .addChild("Fruit", "f")
                .addChild("Fruit", "f")
                .addChild("Basket").down().addChild("Fruit", "f").addChild("Foo")
                .top()
                .createBigraph();

    }
}