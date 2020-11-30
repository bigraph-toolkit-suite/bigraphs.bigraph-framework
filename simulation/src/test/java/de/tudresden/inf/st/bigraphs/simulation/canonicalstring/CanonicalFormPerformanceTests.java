package de.tudresden.inf.st.bigraphs.simulation.canonicalstring;

import com.google.common.base.Stopwatch;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.generators.PureBigraphGenerator;
import de.tudresden.inf.st.bigraphs.core.generators.RandomBigraphGeneratorSupport;
import de.tudresden.inf.st.bigraphs.simulation.encoding.BigraphCanonicalForm;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pure;

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

    @Test
    @DisplayName("Time complexity test for a bigraph without a link graph")
    void multiParam_timeComplexity_test() throws IOException {
        String filePath = "/home/dominik/Documents/PhD/Papers/Concept/Canonical-Bigraphs/analysis/data/results0.data";
        // Hängt die komplexität mit der anzahl der links ab?
        String suffix = "";
        float p = 0.0f;
        int maxArity = 2;

        StringBuilder results = new StringBuilder();
        for (int a = 1; a < maxArity; a++) {
            suffix = "ar" + a;
            DefaultDynamicSignature signature = createRandomSignatureFixedArity(6, 1f, a);
//            RandomBigraphGeneratorSupport.LinkStrategy linkStrategy = RandomBigraphGeneratorSupport.LinkStrategy.MAXIMAL_DEGREE_ASSORTATIVE;
            RandomBigraphGeneratorSupport.LinkStrategy linkStrategy = RandomBigraphGeneratorSupport.LinkStrategy.NONE;
            String s = measureTimeComplexity(new float[]{1000f, 10000f, 100f, p}, signature, linkStrategy, suffix);
            results.append(s).append("\n");
            System.out.println("Created results for ar=" + a);
        }

        if (Objects.nonNull(filePath)) {
            Files.write(
                    Paths.get(filePath),
                    results.toString().getBytes());
        }
    }

    /**
     * This test performs some measurement to answer the question whether the complexity of the BFSE computation depends
     * on the number of connection among the nodes, i.e., on the link graph.
     *
     * @throws IOException if the results could not be written on the file system
     */
    @Test
    @DisplayName("Time complexity test for a bigraph with links")
    void multiParam_timeComplexity_test_withLinkGraph() throws IOException {
        String filePath = "/home/dominik/Documents/PhD/Papers/Concept/Canonical-Bigraphs/analysis/data/results_withLinkGraph2.csv";
        int maxArity = 10;
        float[] probs = {0.1f, 0.25f, 0.5f, 0.75f, 1.f};
        int[] nodeSize = {1000, 2500, 5000};
//        int[] aritySizes = {10, 20, 40};
        int[] aritySizes = {5, 10, 20};
        int numOfNodes = 1000;
        String suffix = "";

        RandomBigraphGeneratorSupport.LinkStrategy linkStrategy = RandomBigraphGeneratorSupport.LinkStrategy.MAXIMAL_DEGREE_ASSORTATIVE;
        StringBuilder results = new StringBuilder();
        for (int p = 0; p < probs.length; p++) {
            System.out.println("Start measurements for p=" + probs[p]);
            for (int a = 0; a < aritySizes.length; a++) {

                suffix = "" + aritySizes[a];
                DefaultDynamicSignature signature = createRandomSignatureFixedArity(1, 1f, aritySizes[a]);
                PureBigraph generate = new PureBigraphGenerator(signature)
                        .setLinkStrategy(linkStrategy)
                        .generate(1, numOfNodes, probs[p]);
                System.out.println("\tBigraph was created with maximal arity=" + aritySizes[a] + " and #edges=" + generate.getEdges().size());
                Stopwatch stopwatch = Stopwatch.createStarted();
                String bfcs = BigraphCanonicalForm.createInstance().bfcs(generate);
//            System.out.println(bfcs);
                long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                results.append(probs[p]).append(',').append(elapsed).append(',').append(suffix).append("\n");
                System.out.println("\tMeasurements completed for arity=" + aritySizes[a]);
            }
            System.out.println("Measurements completed for p=" + probs[p]);
        }

        if (Objects.nonNull(filePath)) {
            Files.write(
                    Paths.get(filePath),
                    results.toString().getBytes());
        }
    }

    public void measureTimeComplexity(
            float[] params, String filePath, DefaultDynamicSignature randomSignature, RandomBigraphGeneratorSupport.LinkStrategy linkStrategy, String suffix
    ) throws IOException {
        if (suffix == null) suffix = "";
        String values = measureTimeComplexity(params, randomSignature, linkStrategy, suffix);

        if (Objects.nonNull(filePath)) {
            Files.write(
                    Paths.get(filePath),
                    values.toString().getBytes());
        }
    }

    public String measureTimeComplexity(
            float[] params, DefaultDynamicSignature randomSignature, RandomBigraphGeneratorSupport.LinkStrategy linkStrategy, String suffix
    ) {
        Objects.requireNonNull(params, "Parameters must not be null");
        if (params.length != 4) {
            throw new RuntimeException("Length of parameter array is not correct.");
        }

        int start = (int) params[0];
        int end = (int) params[1];
        int stepSize = (int) params[2];
        float p = params[2];
        StringBuilder values = new StringBuilder();
        for (int numOfNodes = start; numOfNodes < end; numOfNodes += stepSize) {
            PureBigraph generate = new PureBigraphGenerator(randomSignature)
                    .setLinkStrategy(linkStrategy)
                    .generate(1, numOfNodes, p);
//        O(k² c log c): k = num of vertices, c maximal degree of vertices
            Stopwatch stopwatch = Stopwatch.createStarted();
            String bfcs = BigraphCanonicalForm.createInstance().bfcs(generate);
            long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
//            System.out.println("Time taken: " + elapsed + ", nodes=" + generate.getNodes().size());
//            System.out.println(generate.getEdges().size());
//            System.out.println(bfcs);
            values.append(numOfNodes).append(',').append(elapsed).append(',').append(suffix).append("\n");
            if (stepSize != 0 && (numOfNodes % (stepSize * 5) == 0))
                System.out.println("\tCreated values for n=" + numOfNodes + "/" + end);
        }
        if (values.charAt(values.length() - 1) == ',') {
            values.deleteCharAt(values.length() - 1);
        }
//        values.append("\n");
//        System.out.println(values.toString());

        return values.toString();
    }


    private <C extends Control<?, ?>, S extends Signature<C>> S createRandomSignature(int n, float probOfPositiveArity, int maxArity) {
        ;
        DynamicSignatureBuilder signatureBuilder = pure().createSignatureBuilder();

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

    private <C extends Control<?, ?>, S extends Signature<C>> S createRandomSignatureFixedArity(int n, float probOfPositiveArity, int arity) {
        ;
        DynamicSignatureBuilder signatureBuilder = pure().createSignatureBuilder();

        char[] chars = IntStream.rangeClosed('A', 'Z')
                .mapToObj(c -> "" + (char) c).collect(Collectors.joining()).toCharArray();

        int floorNum = (int) Math.ceil(n * probOfPositiveArity);
        for (int i = 0; i < floorNum; i++) {
            signatureBuilder = (DynamicSignatureBuilder) signatureBuilder.newControl().identifier(StringTypedName.of(String.valueOf(chars[i]))).arity(FiniteOrdinal.ofInteger(arity)).assign();
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
