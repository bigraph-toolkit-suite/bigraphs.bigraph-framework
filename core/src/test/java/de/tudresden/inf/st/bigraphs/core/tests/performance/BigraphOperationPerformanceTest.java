package de.tudresden.inf.st.bigraphs.core.tests.performance;

import de.tudresden.inf.st.bigraphs.core.alg.generators.PureBigraphGenerator;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.ops;
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureSignatureBuilder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BigraphOperationPerformanceTest {
    private final static String TARGET_TEST_PATH = "src/test/resources/dump/tests/performance";
//    private final static String TARGET_TEST_MODEL_PATH = "src/test/resources/ecore-test-models/";

    private static final String NL = "\r\n";
    private static final String SEP = ",";


    @Test
    void name() throws Exception {
        int numOfSamples = 10;
//        int n = 10;
//        float p = 0;
        StringBuilder sb = new StringBuilder();
        DefaultDynamicSignature sig = createRandomSignature(1, 0);
        PureBigraphGenerator generator = new PureBigraphGenerator(sig);
        List<ConfigSlot> configurations = Arrays.asList(
                ConfigSlot.create(10, 20, 10, 0, 0, 0),
                ConfigSlot.create(100, 200, 100, 0, 0, 0),
                ConfigSlot.create(1000, 2000, 1000, 0, 0, 0),
                ConfigSlot.create(10000, 20000, 1000, 0, 0, 0)
        );
        for (ConfigSlot each : configurations) {
            int ix = configurations.indexOf(each);
            String lblConfig = "c" + ix;
            int n = each.n;
            float p = each.p;
            int t = each.t;
            int s = each.s;

            for (int sIx = 0; sIx < numOfSamples; sIx++) { // sample index
                PureBigraph b1 = generator.generate(t, n + 1, p);
                //start measure
                long t1 = System.nanoTime();
//            ops(b1).compose(b1);
                long t2 = System.nanoTime();
                long diff = (t2 - t1) / 1000000000; //in seconds
                diff = (long) (Math.random() * 100);
                //stop measure
                sb.append(new Date()).append(SEP) //current time
                        .append(sIx).append(SEP) // running sample index for a configuration
                        .append(n).append(SEP)  // number of nodes involved
                        .append(diff).append(SEP)   // time difference, operation in seconds
//                        .append("composition").append(SEP) // the actual performed operation
                        .append("parallel").append(SEP) // the actual performed operation
                        .append(lblConfig).append(SEP)  // the used configuration
                        .append(NL)
                ;
            }
        }
        Files.write(
                Paths.get(TARGET_TEST_PATH, "sample.csv"),
                sb.toString().getBytes(),
                StandardOpenOption.APPEND);
//        System.out.println(sb.toString());
    }

    private static class ConfigSlot {
        public int t;
        public int n;
        public int s;
        public float p;
        public float p_l;
        public float p_e;

        public static ConfigSlot create(int t, int n, int s, float p, float p_l, float p_e) {
            return new ConfigSlot(t, n, s, p, p_l, p_e);
        }

        public ConfigSlot(int t, int n, int s, float p, float p_l, float p_e) {
            this.t = t;
            this.n = n;
            this.s = s;
            this.p = p;
            this.p_l = p_l;
            this.p_e = p_e;
        }
    }

    private DefaultDynamicSignature createRandomSignature(int n, float probOfPositiveArity) {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();

        char[] chars = IntStream.rangeClosed('A', 'Z')
                .mapToObj(c -> "" + (char) c).collect(Collectors.joining()).toCharArray();

        int floorNum = (int) Math.ceil(n * probOfPositiveArity);
        for (int i = 0; i < floorNum; i++) {
            signatureBuilder = (DynamicSignatureBuilder) signatureBuilder.newControl().identifier(StringTypedName.of(String.valueOf(chars[i]))).arity(FiniteOrdinal.ofInteger(1)).assign();
        }
        for (int i = floorNum; i < n; i++) {
            signatureBuilder = (DynamicSignatureBuilder) signatureBuilder.newControl().identifier(StringTypedName.of(String.valueOf(chars[i]))).arity(FiniteOrdinal.ofInteger(0)).assign();
        }
        DefaultDynamicSignature s = signatureBuilder.create();
        ArrayList<DefaultDynamicControl> cs = new ArrayList<>(s.getControls());
        Collections.shuffle(cs);
        return signatureBuilder.createWith(new LinkedHashSet<>(cs));
    }

    public static void writeData(StringBuilder lines, String filename) throws IOException {
        Files.write(
                Paths.get(filename),
                lines.toString().getBytes(),
                StandardOpenOption.CREATE);
    }
}
