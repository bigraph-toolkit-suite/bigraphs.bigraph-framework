package org.bigraphs.framework.core.tests.performance;

import org.bigraphs.framework.core.BigraphComposite;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.alg.generators.PureBigraphGenerator;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled
public class BigraphOperationPerformanceTest {
    private final static String TARGET_TEST_PATH = "src/test/resources/dump/tests/performance";
//    private final static String TARGET_TEST_MODEL_PATH = "src/test/resources/ecore-test-models/";

    private static final String NL = "\r\n";
    private static final String SEP = ",";

    @Test
    void binaryOperatorTest_placeGraphsOnly() throws IOException, IncompatibleSignatureException, IncompatibleInterfaceException {
        DynamicSignature sig = createRandomSignature(1, 0);
        ConfigSlot cs = ConfigSlot.create(10, 20, 10, 0, 0, 0);
        int numOfRuns = 100;
        PureBigraphGenerator generator = pureRandomBuilder(sig);
        generator.generate(cs.t, cs.n, cs.s, 0);//pre-alloc
        StringBuilder sb = new StringBuilder("n,t,s,time,op").append(NL);
        for (int n = 10; n < 100; n++) {
            cs.n = n;
            cs.t = cs.s = n / 2;
            if (n % 2 != 0) {
                cs.t = cs.s = (int) Math.ceil(n / 2.0f);
            }
            System.out.println("Creating place graphs with roots/sites " + cs.t + " and nodes total " + n);
            for (int r = 0; r < numOfRuns; r++) {
                PureBigraph generate = generator.generate(cs.t, cs.n, cs.s, 0);
//                BigraphFileModelManagement.Store.exportAsInstanceModel(generate, System.out);
                long start1 = System.nanoTime();
                BigraphComposite<DynamicSignature> compose1 = ops(generate).compose(generate);
                long end1 = System.nanoTime();
                long diff1 = (end1 - start1) / 1000000;
                sb.append(cs.n).append(SEP).append(cs.t).append(SEP).append(cs.s).append(SEP)
                        .append(diff1).append(SEP).append("composition").append(NL);

                long start2 = System.nanoTime();
                BigraphComposite<DynamicSignature> compose2 = ops(generate).parallelProduct(generate);
                long end2 = System.nanoTime();
                long diff2 = (end2 - start2) / 1000000;
                sb.append(cs.n).append(SEP).append(cs.t).append(SEP).append(cs.s).append(SEP)
                        .append(diff2).append(SEP).append("parallel").append(NL);

                long start3 = System.nanoTime();
                BigraphComposite<DynamicSignature> compose3 = ops(generate).merge(generate);
                long end3 = System.nanoTime();
                long diff3 = (end3 - start3) / 1000000;
                sb.append(cs.n).append(SEP).append(cs.t).append(SEP).append(cs.s).append(SEP)
                        .append(diff3).append(SEP).append("merge").append(NL);
            }
//            BigraphGraphvizExporter.toPNG(compose.getOuterBigraph(), true, new File("test.png"));
        }
        FileUtils.writeStringToFile(new File("performance-test-operations-place-graph.csv"), sb.toString(), Charset.defaultCharset());
    }

    @Test
    void binaryOperatorTest_bigraphsWithLinks() throws IOException, IncompatibleSignatureException, IncompatibleInterfaceException {
        DynamicSignature sig = createSingletonSignature("A", 100);
        ConfigSlot cs = ConfigSlot.create(250, 500, 250, 0, 0, 0);
        int numOfRuns = 100;
        PureBigraphGenerator generator = pureRandomBuilder(sig);
        generator.generate(cs.t, cs.n, cs.s, 0);//pre-alloc
        Linkings<DynamicSignature> linkings = pureLinkings(sig);
        StringBuilder sb = new StringBuilder("p,pl,pe,time,op").append(NL);
        for (float p = 0.1f; p <= 1.0f; p += 0.1f) {
            cs.p = cs.p_e = cs.p_l = p;
            System.out.println("Creating bigraphs with p= " + cs.p);
            for (int r = 0; r < numOfRuns; r++) {
                PureBigraph generate = generator.generate(cs.t, cs.n, cs.s, cs.p, cs.p_l, cs.p_e);
//                BigraphFileModelManagement.Store.exportAsInstanceModel(generate, System.out);
                long start1 = System.nanoTime();
                if (generate.getOuterNames().size() > 0) {
                    Linkings<DynamicSignature>.Identity id = suitableLinking(linkings, generate.getOuterNames());
//                    BigraphFileModelManagement.Store.exportAsInstanceModel(id, System.out);
                    generate = ops(generate).parallelProduct(id).getOuterBigraph();
//                    BigraphFileModelManagement.Store.exportAsInstanceModel(generate, System.out);
                }
                long end1 = System.nanoTime();
                BigraphComposite<DynamicSignature> compose1 = ops(generate).compose(generate);
                long diff1 = (end1 - start1) / 1000000;
                sb.append(cs.p).append(SEP).append(cs.p_l).append(SEP).append(cs.p_e).append(SEP)
                        .append(diff1).append(SEP).append("composition").append(NL);

                long start2 = System.nanoTime();
                BigraphComposite<DynamicSignature> compose2 = ops(generate).parallelProduct(generate);
                long end2 = System.nanoTime();
                long diff2 = (end2 - start2) / 1000000;
                sb.append(cs.p).append(SEP).append(cs.p_l).append(SEP).append(cs.p_e).append(SEP)
                        .append(diff2).append(SEP).append("parallel").append(NL);
//
                long start3 = System.nanoTime();
                BigraphComposite<DynamicSignature> compose3 = ops(generate).merge(generate);
                long end3 = System.nanoTime();
                long diff3 = (end3 - start3) / 1000000;
                sb.append(cs.p).append(SEP).append(cs.p_l).append(SEP).append(cs.p_e).append(SEP)
                        .append(diff3).append(SEP).append("merge").append(NL);
            }
//            BigraphGraphvizExporter.toPNG(compose.getOuterBigraph(), true, new File("test.png"));
        }
        FileUtils.writeStringToFile(new File("performance-test-operations-bigraph-graph.csv"), sb.toString(), Charset.defaultCharset());
    }


    @Test
    void naryOperatorTest_bigraphsWithLinks() throws IOException, IncompatibleSignatureException, IncompatibleInterfaceException {
        DynamicSignature sig = createSingletonSignature("A", 100);
        ConfigSlot cs = ConfigSlot.create(100, 200, 100, 0.5f, 0.5f, 0.5f);
        int numOfRuns = 50;
        PureBigraphGenerator generator = pureRandomBuilder(sig);
        generator.generate(cs.t, cs.n, cs.s, 0);//pre-alloc
        Linkings<DynamicSignature> linkings = pureLinkings(sig);
        StringBuilder sb = new StringBuilder("steps,time,op").append(NL);
        for (int ops = 1; ops <= 10; ops++) {
            System.out.println("Creating bigraphs with c= " + cs);
            for (int r = 0; r < numOfRuns; r++) {
                PureBigraph generate = generator.generate(cs.t, cs.n, cs.s, cs.p, cs.p_l, cs.p_e);
//                BigraphFileModelManagement.Store.exportAsInstanceModel(generate, System.out);
                Linkings<DynamicSignature>.Identity id = suitableLinking(linkings, generate.getOuterNames());
                generate = ops(generate).parallelProduct(id).getOuterBigraph();
                PureBigraph finalGenerate = generate;
                long start1 = System.nanoTime();
                PureBigraph reduced = IntStream.range(0, ops - 1)
                        .mapToObj(x -> {
                            return finalGenerate;
                        })
                        .reduce(finalGenerate, (pureBigraph, pureBigraph2) -> {
                            try {
                                return ops(pureBigraph).compose(pureBigraph2).getOuterBigraph();
                            } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
                                throw new RuntimeException(e);
                            }
                        });
                long end1 = System.nanoTime();
                long diff1 = (end1 - start1) / 1000000;
                sb.append(ops).append(SEP)
                        .append(diff1).append(SEP).append("composition").append(NL);

                long start2 = System.nanoTime();
                PureBigraph reduced2 = IntStream.range(0, ops - 1)
                        .mapToObj(x -> {
                            return finalGenerate;
                        })
                        .reduce(finalGenerate, (pureBigraph, pureBigraph2) -> {
                            try {
                                return ops(pureBigraph).parallelProduct(pureBigraph2).getOuterBigraph();
                            } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
                                throw new RuntimeException(e);
                            }
                        });
                long end2 = System.nanoTime();
                long diff2 = (end2 - start2) / 1000000;
                sb.append(ops).append(SEP)
                        .append(diff2).append(SEP).append("parallel").append(NL);
////
                long start3 = System.nanoTime();
                PureBigraph reduced3 = IntStream.range(0, ops - 1)
                        .mapToObj(x -> {
                            return finalGenerate;
                        })
                        .reduce(finalGenerate, (pureBigraph, pureBigraph2) -> {
                            try {
                                return ops(pureBigraph).merge(pureBigraph2).getOuterBigraph();
                            } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
                                throw new RuntimeException(e);
                            }
                        });
                long end3 = System.nanoTime();
                long diff3 = (end3 - start3) / 1000000;
                sb.append(ops).append(SEP)
                        .append(diff3).append(SEP).append("merge").append(NL);
            }
        }
        FileUtils.writeStringToFile(new File("time-complexity-bigraph-operations-consecutive.csv"), sb.toString(), Charset.defaultCharset());
    }

    public static Linkings<DynamicSignature>.Identity suitableLinking(Linkings<DynamicSignature> linkings,
                                                                      List<BigraphEntity.OuterName> outerNames) {

        Linkings<DynamicSignature>.Identity identity = linkings.identity(outerNames.stream().map(x -> StringTypedName.of(x.getName())).toArray(StringTypedName[]::new));
        return identity;
    }

    @Test
    void name() throws Exception {
        int numOfSamples = 10;
//        int n = 10;
//        float p = 0;
        StringBuilder sb = new StringBuilder("");
        DynamicSignature sig = createRandomSignature(1, 0);
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

    private DynamicSignature createRandomSignature(int n, float probOfPositiveArity) {
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
        DynamicSignature s = signatureBuilder.create();
        ArrayList<DynamicControl> cs = new ArrayList<>(s.getControls());
        Collections.shuffle(cs);
        return signatureBuilder.createWith(new LinkedHashSet<>(cs));
    }

    private DynamicSignature createSingletonSignature(String control, int arity) {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder.add(control, arity, ControlStatus.ACTIVE);
        return signatureBuilder.create();
    }

    public static void writeData(StringBuilder lines, String filename) throws IOException {
        Files.write(
                Paths.get(filename),
                lines.toString().getBytes(),
                StandardOpenOption.CREATE);
    }
}
