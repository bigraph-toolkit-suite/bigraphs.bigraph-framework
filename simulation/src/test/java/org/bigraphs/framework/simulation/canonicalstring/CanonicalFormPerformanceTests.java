package org.bigraphs.framework.simulation.canonicalstring;

import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.alg.generators.RandomBigraphGeneratorSupport;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * @author Dominik Grzelak
 */
@Disabled
public class CanonicalFormPerformanceTests {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/canonicform/";

    @Test
    @DisplayName("Time complexity measurement based on random bigraphs: n=[1000,10000], assortative mixing")
    void time_complexity_test() throws IOException {
        DefaultDynamicSignature randomSignature = createRandomSignature(5, 1f, 5);
        RandomBigraphGeneratorSupport.LinkStrategy linkStrategy = RandomBigraphGeneratorSupport.LinkStrategy.MAXIMAL_DEGREE_ASSORTATIVE;

        String p = "/home/dominik/Documents/PhD/Papers/Concept/Canonical-Bigraphs/analysis/data/A2time-" + linkStrategy + ".data";
        measureTimeComplexity(new float[]{100f, 1000f, 10f, 0.1f}, p, randomSignature, linkStrategy, null);
    }

    @Test
    @DisplayName("Time complexity measurement based on random bigraphs: n=[1000,10000], disassortative mixing")
    void time_complexity_test2() throws IOException {
        DefaultDynamicSignature randomSignature = createRandomSignature(5, 1f, 5);
        RandomBigraphGeneratorSupport.LinkStrategy linkStrategy = RandomBigraphGeneratorSupport.LinkStrategy.MAXIMAL_DEGREE_DISASSORTATIVE;

        // export an example of the graph we are going to create
//        PureBigraph g0 = new PureBigraphGenerator().setLinkStrategy(linkStrategy).generate(randomSignature, 1, 10, 1f);
//        BigraphGraphvizExporter.toPNG(g0,
//                true,
//                new File(TARGET_DUMP_PATH + "randomBigraph.png")
//        );
//        System.out.println(BigraphCanonicalForm.createInstance().bfcs(g0));

        String filePath = "/home/dominik/Documents/PhD/Papers/Concept/Canonical-Bigraphs/analysis/data/D2time-" + linkStrategy + ".data";
        measureTimeComplexity(new float[]{100f, 1000f, 10f, 0.1f}, filePath, randomSignature, linkStrategy, null);
    }


    public void measureTimeComplexity(
            float[] params, String filePath, DefaultDynamicSignature randomSignature, RandomBigraphGeneratorSupport.LinkStrategy linkStrategy, String suffix
    ) throws IOException {
        if (suffix == null) suffix = "";
        String values = CanonicalFormPaperBenchmarks.measureTimeComplexity(params, randomSignature, linkStrategy, suffix);

        if (Objects.nonNull(filePath)) {
            Files.write(
                    Paths.get(filePath),
                    values.toString().getBytes());
        }
    }


    private <C extends Control<?, ?>, S extends Signature<C>> S createRandomSignature(int n, float probOfPositiveArity, int maxArity) {
        ;
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();

        char[] chars = IntStream.rangeClosed('A', 'Z')
                .mapToObj(c -> "" + (char) c).collect(Collectors.joining()).toCharArray();

        int floorNum = (int) Math.ceil(n * probOfPositiveArity);
        for (int i = 0; i < floorNum; i++) {
            int ar = new Random().nextInt(maxArity) + 1;
            signatureBuilder = (DynamicSignatureBuilder) signatureBuilder.newControl().identifier(StringTypedName.of(String.valueOf(chars[i]))).arity(FiniteOrdinal.ofInteger(ar)).assign();
        }
        for (int i = floorNum; i < n; i++) {
            signatureBuilder = (DynamicSignatureBuilder) signatureBuilder.newControl().identifier(StringTypedName.of(String.valueOf(chars[i]))).arity(FiniteOrdinal.ofInteger(0)).assign();
        }
        S s = (S) signatureBuilder.create();
        ArrayList<C> cs = new ArrayList<>(s.getControls());
        Collections.shuffle(cs);
        return (S) signatureBuilder.createWith((Iterable<? extends Control<StringTypedName, FiniteOrdinal<Integer>>>) new LinkedHashSet<C>(cs));
    }

}
