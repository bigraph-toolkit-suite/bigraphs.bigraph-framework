package de.tudresden.inf.st.bigraphs.rewriting.canonicalstring;

import com.google.common.base.Stopwatch;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.utils.PureBigraphGenerator;
import de.tudresden.inf.st.bigraphs.core.utils.RandomBigraphGenerator;
import de.tudresden.inf.st.bigraphs.rewriting.encoding.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
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
public class CanonicalPerformanceTests {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/canonicform/";

    @Test
    @DisplayName("Time complexity measurement based on random bigraphs: n=[1000,10000], assortative mixing")
    void time_complexity_test() throws IOException {
        DefaultDynamicSignature randomSignature = createRandomSignature(5, 1f, 5);
        RandomBigraphGenerator.LinkStrategy linkStrategy = RandomBigraphGenerator.LinkStrategy.MAXIMAL_DEGREE_ASSORTATIVE;

        String p = "/home/dominik/Documents/PhD/Papers/Concept/Canonical-Bigraphs/analysis/data/A2time-" + linkStrategy + ".data";
        measureTimeComplexity(new float[]{100f, 1000f, 10f, 0.1f}, p, randomSignature, linkStrategy);
    }

    @Test
    @DisplayName("Time complexity measurement based on random bigraphs: n=[1000,10000], disassortative mixing")
    void time_complexity_test2() throws IOException {
        DefaultDynamicSignature randomSignature = createRandomSignature(5, 1f, 5);
        RandomBigraphGenerator.LinkStrategy linkStrategy = RandomBigraphGenerator.LinkStrategy.MAXIMAL_DEGREE_DISASSORTATIVE;

        // export an example of the graph we are going to create
//        PureBigraph g0 = new PureBigraphGenerator().setLinkStrategy(linkStrategy).generate(randomSignature, 1, 10, 1f);
//        BigraphGraphvizExporter.toPNG(g0,
//                true,
//                new File(TARGET_DUMP_PATH + "randomBigraph.png")
//        );
//        System.out.println(BigraphCanonicalForm.createInstance().bfcs(g0));

        String p = "/home/dominik/Documents/PhD/Papers/Concept/Canonical-Bigraphs/analysis/data/D2time-" + linkStrategy + ".data";
        measureTimeComplexity(new float[]{100f, 1000f, 10f, 0.1f}, p, randomSignature, linkStrategy);
    }

    public void measureTimeComplexity(
            float[] params, String filePath, DefaultDynamicSignature randomSignature, RandomBigraphGenerator.LinkStrategy linkStrategy
    ) throws IOException {
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
            PureBigraph generate = new PureBigraphGenerator()
                    .setLinkStrategy(linkStrategy)
                    .generate(randomSignature, 1, numOfNodes, p);
//        O(k² c log c): k = num of vertices, c maximal degree of vertices
            Stopwatch stopwatch = Stopwatch.createStarted();
            String bfcs = BigraphCanonicalForm.createInstance().bfcs(generate);
            long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
//            System.out.println("Time taken: " + elapsed + ", nodes=" + generate.getNodes().size());
//            System.out.println(generate.getEdges().size());
//            System.out.println(bfcs);
            values.append(numOfNodes).append(',').append(elapsed).append("\n");
        }
        if (values.charAt(values.length() - 1) == ',') {
            values.deleteCharAt(values.length() - 1);
        }
//        values.append("\n");
//        System.out.println(values.toString());

        if (Objects.nonNull(filePath)) {
            Files.write(
                    Paths.get(filePath),
                    values.toString().getBytes());
        }
    }


    private <C extends Control<?, ?>, S extends Signature<C>> S createRandomSignature(int n, float probOfPositiveArity, int maxArity) {
        ;
        DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> signatureBuilder = pure().createSignatureBuilder();

        char[] chars = IntStream.rangeClosed('A', 'Z')
                .mapToObj(c -> "" + (char) c).collect(Collectors.joining()).toCharArray();

        int floorNum = (int) Math.ceil(n * probOfPositiveArity);
        for (int i = 0; i < floorNum; i++) {
            int ar = new Random().nextInt(maxArity) + 1;
            signatureBuilder = (DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>>) signatureBuilder.newControl().identifier(StringTypedName.of(String.valueOf(chars[i]))).arity(FiniteOrdinal.ofInteger(ar)).assign();
        }
        for (int i = floorNum; i < n; i++) {
            signatureBuilder = (DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>>) signatureBuilder.newControl().identifier(StringTypedName.of(String.valueOf(chars[i]))).arity(FiniteOrdinal.ofInteger(0)).assign();
        }
        S s = (S) signatureBuilder.create();
        ArrayList<C> cs = new ArrayList<>(s.getControls());
        Collections.shuffle(cs);
        return signatureBuilder.createSignature(new LinkedHashSet<>(cs));
    }


}
