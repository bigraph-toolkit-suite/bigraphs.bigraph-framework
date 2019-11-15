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
import de.tudresden.inf.st.bigraphs.rewriting.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Random;
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
    void time_complexity_test() throws IOException {
        DefaultDynamicSignature randomSignature = createRandomSignature(5, 1f, 5);
        RandomBigraphGenerator.LinkStrategy linkStrategy = RandomBigraphGenerator.LinkStrategy.MAXIMAL_DEGREE_ASSORTATIVE;
//        RandomBigraphGenerator.LinkStrategy linkStrategy = RandomBigraphGenerator.LinkStrategy.MAXIMAL_DEGREE_DISASSORTATIVE;

        // export an example of the graph we are going to create
        PureBigraph g0 = new PureBigraphGenerator().setLinkStrategy(linkStrategy).generate(randomSignature, 1, 10, 1f);
        BigraphGraphvizExporter.toPNG(g0,
                true,
                new File(TARGET_DUMP_PATH + "randomBigraph.png")
        );
        System.out.println(BigraphCanonicalForm.getInstance().bfcs(g0));

        StringBuilder values = new StringBuilder();
        for (int numOfNodes = 1000; numOfNodes < 10000; numOfNodes += 100) {
            PureBigraph generate = new PureBigraphGenerator()
                    .setLinkStrategy(linkStrategy)
                    .generate(randomSignature, 1, numOfNodes, 1f);
//        O(k² c log c): k = num of vertices, c maximal degree of vertices
            Stopwatch stopwatch = Stopwatch.createStarted();
            String bfcs = BigraphCanonicalForm.getInstance().bfcs(generate);
            long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
//            System.out.println("Time taken: " + elapsed + ", nodes=" + generate.getNodes().size());
//            System.out.println(generate.getEdges().size());
//            System.out.println(bfcs);
            values.append(elapsed).append(",");
        }
        if (values.charAt(values.length() - 1) == ',') {
            values.deleteCharAt(values.length() - 1);
        }
        values.append("\n");
//        System.out.println(values.toString());

        Files.write(
                Paths.get("/home/dominik/Documents/PhD/Papers/Concept/Canonical-Bigraphs/analysis/data/time-" + linkStrategy + ".data"),
                values.toString().getBytes());

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
