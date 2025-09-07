package org.bigraphs.framework.simulation.canonicalstring;

import com.google.common.collect.Lists;
import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.alg.generators.PureBigraphGenerator;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.simulation.encoding.BigraphCanonicalForm;
import org.bigraphs.framework.simulation.examples.BaseExampleTestSupport;
import org.bigraphs.framework.simulation.examples.RouteFinding;
import org.bigraphs.framework.simulation.modelchecking.predicates.BigraphIsoPredicate;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Dominik Grzelak
 */
public class CanonicalFormPureBigraphsUnitTests extends BaseExampleTestSupport {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/canonicform/";

    public CanonicalFormPureBigraphsUnitTests() {
        super(TARGET_DUMP_PATH, true);
    }

//    private PureBigraphFactory factory = pure();

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    @Test
    void multiple_roots() throws InvalidConnectionException, TypeNotExistsException, IOException, IncompatibleSignatureException, IncompatibleInterfaceException {
        DynamicSignature sig = createAlphabeticSignature();
        BigraphCanonicalForm instance = BigraphCanonicalForm.createInstance();
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(sig);
        BigraphEntity.InnerName tmp = b.createInner("tmp");
        b.root()
                .child("A")
                .child("B").down().child("C").linkInner(tmp).top();
        b.root().child("B").down().child("D").linkInner(tmp).up().child("A").top();
        b.closeInner(tmp);
        PureBigraph bigraphA = b.create();
//        BigraphFileModelManagement.exportAsInstanceModel(bigraphA, System.out);
        String bfcsA = instance.bfcs(bigraphA);
        System.out.println(bfcsA);

        // same bigraph as above, but only roots swapped (technically)
        PureBigraphBuilder<DynamicSignature> b2 = pureBuilder(sig);
        BigraphEntity.InnerName tmp2 = b2.createInner("tmp");
        b2.root().child("B").down().child("D").linkInner(tmp2).up().child("A").top();
        b2.root().child("A").child("B").down().child("C").linkInner(tmp2).top();
        b2.closeInner(tmp2);
        PureBigraph bigraph2 = b2.create();
//        BigraphFileModelManagement.exportAsInstanceModel(bigraph2, System.out);

        String bfcs2 = instance.bfcs(bigraph2);
        System.out.println(bfcs2);

        PureBigraphBuilder<DynamicSignature> b3 = pureBuilder(sig);
        b3.root().child("G").down().site().up().child("H").down().site();
        PureBigraph big3 = b3.create();
//        BigraphFileModelManagement.exportAsInstanceModel(big3, System.out);
        String bfcs = instance.bfcs(big3);
        System.out.println(bfcs);

        Bigraph<DynamicSignature> outerBigraph = ops(big3).compose(bigraphA).getOuterBigraph();
        String outerBigraphSE = instance.bfcs(outerBigraph);
        System.out.println(outerBigraphSE);

        Bigraph<DynamicSignature> outerBigraph2 = ops(big3).compose(bigraph2).getOuterBigraph();
        String outerBigraphSE2 = instance.bfcs(outerBigraph2);
        System.out.println(outerBigraphSE2);
//        Placings<DefaultDynamicSignature> placings = factory.createPlacings(sig);
//        Placings<DefaultDynamicSignature>.Join join = placings.join();
//        Bigraph<DefaultDynamicSignature> compA = ops(join).nesting(bigraphA).getOuterBigraph();
//        Bigraph<DefaultDynamicSignature> comp2 = ops(join).nesting(bigraph2).getOuterBigraph();
////        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) compA, System.out);
////        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) comp2, System.out);
//
//        String bfcsCompA = instance.bfcs(compA);
//        String bfcsComp2 = instance.bfcs(comp2);
//        System.out.println(bfcsCompA);
//        System.out.println(bfcsComp2);
    }

    @Test
    void car_example() throws IOException {
//        file:///home/dominik/git/BigraphFramework/simulation/src/test/resources/dump/cars/framework/states/a_9.xmi
//        file:///home/dominik/git/BigraphFramework/simulation/src/test/resources/dump/cars/framework/states/a_17.xmi

        //same: 9-17-21
        //same: 5-11
        String metaModelFile = TARGET_DUMP_PATH + "../../bigraphs/cars/meta-model.ecore";
        String instanceModelFile_9 = TARGET_DUMP_PATH + "../../bigraphs/cars/a_9.xmi";
        String instanceModelFile_17 = TARGET_DUMP_PATH + "../../bigraphs/cars/a_17.xmi";
        DynamicSignature carMapSignature = RouteFinding.createSignature();

        PureBigraphBuilder<DynamicSignature> builder_9 = PureBigraphBuilder.create(carMapSignature, metaModelFile, instanceModelFile_9);
        PureBigraphBuilder<DynamicSignature> builder_17 = PureBigraphBuilder.create(carMapSignature, metaModelFile, instanceModelFile_17);

        PureBigraph bigraph_9 = builder_9.create();
        PureBigraph bigraph_17 = builder_17.create();
//                BigraphGraphvizExporter.toPNG(bigraph_9,
//                true,
//                new File(TARGET_DUMP_PATH + "../../bigraphs/cars/b9.png")
//        );
//                BigraphGraphvizExporter.toPNG(bigraph_17,
//                true,
//                new File(TARGET_DUMP_PATH + "../../bigraphs/cars/b17.png")
//        );

        assertEquals(bigraph_9.getAllPlaces().size(), bigraph_17.getAllPlaces().size());
        BigraphCanonicalForm instance = BigraphCanonicalForm.createInstance();
        String bfcs_9 = instance.bfcs(bigraph_9);
        String bfcs_17 = instance.bfcs(bigraph_17);

        System.out.println(bfcs_9);
        System.out.println(bfcs_17);
        assertEquals(bfcs_9, bfcs_17);

//        new PureBigraphBuilder<DefaultDynamicSignature>()
    }

    @Test
    void symmetricProcesses() throws InvalidConnectionException {
        PureBigraph agentProcess = createAgentProcess();
        String bfcs0 = BigraphCanonicalForm.createInstance().bfcs(agentProcess);
        String bfcs1 = BigraphCanonicalForm.createInstance(true).bfcs(agentProcess);
        System.out.println(bfcs0);
        System.out.println(bfcs1);

    }

    PureBigraph createAgentProcess() throws InvalidConnectionException {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignatureProcess());

        builder.root()
                .child("Process", "access2")
                .child("Process", "access1")
                .child("Resource").down().child("Token").up()
        ;
        PureBigraph bigraph = builder.create();
        return bigraph;
    }

    private DynamicSignature createSignatureProcess() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Process")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Token")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Working")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Resource")).arity(FiniteOrdinal.ofInteger(1)).assign()
        ;
        return defaultBuilder.create();
    }

    @Test
    void paper_bigraphs() throws InvalidConnectionException, TypeNotExistsException, IOException {
        PureBigraph bigraph_a = createBigraph_a();
        PureBigraph bigraph_b = createBigraph_b();
        PureBigraph bigraph_c = createBigraph_c();
        PureBigraph bigraph_d = createBigraph_d();
        eb(bigraph_c, "bigraph_c");


        String bfcs0 = BigraphCanonicalForm.createInstance().bfcs(bigraph_a);
        String bfcs1 = BigraphCanonicalForm.createInstance().bfcs(bigraph_b);
        String bfcs2 = BigraphCanonicalForm.createInstance().bfcs(bigraph_c);
        String bfcs3 = BigraphCanonicalForm.createInstance().bfcs(bigraph_d);
        System.out.println(bfcs0);
        System.out.println(bfcs1);
        System.out.println(bfcs2);
        System.out.println(bfcs3);

        assertNotEquals(bfcs0, bfcs1);
        assertNotEquals(bfcs2, bfcs3);
//        r0$Q$ABC$D{e0}E{e0}F{y0}$D{e1}E{e1}F{y1}$AB$$$GH$$$$$1$0#x0y1# // paper
        assertEquals("r0$Q$ABC$D{e0}E{e0}F{y1}$AB$D{e1}E{e1}F{y2}$$$GH$$1$$$$$0#by2#", bfcs0);
        assertEquals("r0$Q$ABC$D{e0}E{e0}F{y1}$D{e1}E{e1}F{y1}$AB$$$GH$$$$$$0#", bfcs1);
        assertEquals("r0$BB$D{e0}E{e0e1}F{e1}$D{e2}E{e3}F{e2e3}$G$$H$G$$H#", bfcs2);
        assertEquals("r0$BB$D{e0}E{e0e1}F{e1}$D{e2e3}E{e3}F{e2}$G$$H$G$$H#", bfcs3);

        PureBigraph subbigraph_b = createSubbigraph_b();
        String s1 = BigraphCanonicalForm.createInstance().bfcs(subbigraph_b);
        System.out.println(s1);
    }

    public PureBigraph createSubbigraph_b() throws InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = createAlphabeticSignature();
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);
        BigraphEntity.InnerName e0 = b1.createInner("e0");
//        BigraphEntity.OuterName y1 = b1.createOuterName("y1");

        b1.root().child("B")
                .down()
                .child("D", "y1").linkInner(e0)
                .child("F", "y1").down().child("H").child("G").down().site().up().up()
                .child("E", "y1").linkInner(e0)
        ;
        b1.closeInner();
        return b1.create();
    }

    public PureBigraph createBigraph_a() throws InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = createAlphabeticSignature();
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);
        BigraphEntity.InnerName b = b1.createInner("b");
        BigraphEntity.OuterName y1 = b1.createOuter("y1");
        BigraphEntity.OuterName y2 = b1.createOuter("y2");

        PureBigraphBuilder.Hierarchy left = b1.hierarchy("A").connectByEdge("D", "E").child("F").linkOuter(y1).down().child("G").child("H").down().site().top();
        PureBigraphBuilder.Hierarchy middle = b1.hierarchy("B").child("A").child("B").down().site().top();
        PureBigraphBuilder.Hierarchy right = b1.hierarchy("C").connectByEdge("D", "E").child("F").linkOuter(y2).top();
        b1.linkInnerToOuter(b, y2);
        b1.root().child("Q").down().child(left.top()).child(middle.top()).child(right.top());
        return b1.create();
    }

    public PureBigraph createBigraph_b() throws InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = createAlphabeticSignature();
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);
        BigraphEntity.OuterName y1 = b1.createOuter("y1");

        PureBigraphBuilder.Hierarchy left = b1.hierarchy("A").connectByEdge("D", "E").child("F").linkOuter(y1).down().child("H").child("G").down().site().top();
        PureBigraphBuilder.Hierarchy middle = b1.hierarchy("C").child("A").child("B").top();
        PureBigraphBuilder.Hierarchy right = b1.hierarchy("B").connectByEdge("D", "E").child("F").linkOuter(y1).top();

        b1.root().child("Q").down().child(left).child(middle).child(right);
        PureBigraph bigraph = b1.create();
        return bigraph;
    }

    public PureBigraph createBigraph_c() throws InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = createAlphabeticSignature();
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);

        BigraphEntity.InnerName e0 = b1.createInner("e0");
        BigraphEntity.InnerName e1 = b1.createInner("e1");
        BigraphEntity.InnerName e2 = b1.createInner("e2");
        BigraphEntity.InnerName e3 = b1.createInner("e3");

        b1.root()
                .child("B").down()
                .child("D").linkInner(e0).down().child("G").up().child("F").linkInner(e0).linkInner(e1).down().child("H").up().child("E").linkInner(e1).up()
                .child("B").down()
                .child("D").linkInner(e2).down().child("G").up().child("E").linkInner(e2).linkInner(e3).child("F").linkInner(e3).down().child("H").up().up()
        ;
        b1.closeInner(e0, e1, e2, e3);
        return b1.create();
    }


    public PureBigraph createBigraph_d() throws InvalidConnectionException, TypeNotExistsException {
        DynamicSignature signature = createAlphabeticSignature();
        PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);
        BigraphEntity.InnerName e0 = b1.createInner("e0");
        BigraphEntity.InnerName e1 = b1.createInner("e1");
        BigraphEntity.InnerName e2 = b1.createInner("e2");
        BigraphEntity.InnerName e3 = b1.createInner("e3");
        b1.root()
                .child("B").down().child("D").linkInner(e0).linkInner(e1).down().child("G").up().child("E").linkInner(e1).child("F").linkInner(e0).down().child("H").up().up()
                .child("B").down().child("D").linkInner(e2).down().child("G").up().child("E").linkInner(e2).linkInner(e3).child("F").linkInner(e3).down().child("H").up().up()
        ;
        b1.closeInner(e0, e1, e2, e3);
        return b1.create();
    }

    @Test
    void test0() throws Exception {

        PureBigraph bigraph = new LinkGraphCanonicalFormTest().createFirstLG();
        PureBigraph bigraph2 = new LinkGraphCanonicalFormTest().createSecondLG();
//        BigraphGraphvizExporter.toPNG(bigraph2,
//                true,
//                new File(TARGET_DUMP_PATH + "paperbigraph2.png")
//        );
//        PureBigraph bigraph2 = createSecond();

        String bfcs = BigraphCanonicalForm.createInstance().bfcs(bigraph);
        String bfcs2 = BigraphCanonicalForm.createInstance().bfcs(bigraph2);
        System.out.println(bfcs);
        System.out.println(bfcs2);

    }


    @Test
    @DisplayName("Using the Bigraph BFCS computation within the 'BigraphIsoPredicate'")
    void using_iso_predicate_test() {
        DynamicSignature randomSignature = createRandomSignature(26, 1f);
        PureBigraph g0 = new PureBigraphGenerator(randomSignature).generate(1, 30, 1f);

        BigraphIsoPredicate<PureBigraph> pureBigraphBigraphIsoPredicate = BigraphIsoPredicate.create(g0);
        boolean test = pureBigraphBigraphIsoPredicate.test(g0);
        Assertions.assertTrue(test);

        PureBigraph g1 = new PureBigraphGenerator(randomSignature).generate(1, 30, 1f);
        boolean test2 = pureBigraphBigraphIsoPredicate.negate().test(g1);
        boolean test3 = pureBigraphBigraphIsoPredicate.test(g1);
        Assertions.assertTrue(test2);
        Assertions.assertFalse(test3);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Test series for place graphs")
    class PlaceGraphCanonicalFormTest {

        @Test
        void basic_placeGraph_canonicalForm_test() throws IOException {
            PureBigraph basicPlaceGraph = createBasicPlaceGraph();
            BigraphGraphvizExporter.toPNG(basicPlaceGraph,
                    true,
                    new File(TARGET_DUMP_PATH + "basicPlaceGraph.png")
            );
            String validBFCS = "r0$A$BCD$E$AF$F$A$$BD$C#";
            String bfcs = BigraphCanonicalForm.createInstance().bfcs(basicPlaceGraph);
            assertEquals(bfcs, validBFCS);
            System.out.printf("%s == %s\n", bfcs, validBFCS);
        }

        private PureBigraph createBasicPlaceGraph() {
            DynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

            builder.root()
                    .child(signature.getControlByName("A"))
                    .down()
                    .child(signature.getControlByName("B")).down()
                    .child("E").down().child("A").up().up()
                    .child(signature.getControlByName("C")).down()
                    .child("F").down().child("B").child("D").up().child("A").up()
                    .child(signature.getControlByName("D")).down()
                    .child("F").down().child("C")
            ;
            return builder.create();
        }

        @Test
        void simple_placeGraph_canonical_form_test() throws IOException, InvalidConnectionException, LinkTypeNotExistsException {

            Bigraph biesingerSampleBigraph = createBiesingerSampleBigraph();
            String bfcs = BigraphCanonicalForm.createInstance().bfcs(biesingerSampleBigraph);
            System.out.println(bfcs);

            String bfcsNonMatch = BigraphCanonicalForm.createInstance().bfcs(createNonMatchingBiesingerSampleBigraph());
            String validBFCS = "r0$A$BB$CD$E$$FG$E#";
            assertEquals(bfcs, validBFCS);
            System.out.printf("%s == %s\n", bfcs, validBFCS);
            assertNotEquals(bfcsNonMatch, validBFCS);
            System.out.printf("%s != %s\n", bfcsNonMatch, validBFCS);
            BigraphGraphvizExporter.toPNG(createNonMatchingBiesingerSampleBigraph(),
                    true,
                    new File(TARGET_DUMP_PATH + "sampleBigraph_biesinger.png")
            );
//
            AtomicInteger cnt0 = new AtomicInteger(0);
            long count = Stream.of(createSampleBigraphA1(), createSampleBigraphA2())
                    .peek(x -> {
                        try {
                            BigraphGraphvizExporter.toPNG(x,
                                    true,
                                    new File(TARGET_DUMP_PATH + "sampleBigraph0_" + cnt0.incrementAndGet() + ".png")
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })
                    .map(x -> BigraphCanonicalForm.createInstance().bfcs(x))
                    .peek(System.out::println)
                    .peek(x -> assertEquals("r0$A$BBB$$CD$CE$GH$$FH#", x))
                    .distinct().count();
            assertEquals(count, 1);

            List<Bigraph> sampleGraphs = createSampleGraphs();
//        AtomicInteger cnt1 = new AtomicInteger(0);
            long num = sampleGraphs.stream()
                    .peek(x -> {
                        try {
                            BigraphGraphvizExporter.toPNG(x,
                                    true,
                                    new File(TARGET_DUMP_PATH + "sampleBigraph1_" + cnt0.incrementAndGet() + ".png")
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })
                    .map(x -> BigraphCanonicalForm.createInstance().bfcs(x))
                    .peek(System.out::println)
                    .peek(x -> assertEquals("r0$A$BB$C$CD#", x))
                    .distinct()
                    .count();
            assertEquals(num, 1);
        }

        /**
         * Example graph within the slides of Markus Biesinger. The BFCS is "A$BB$CD$E$FG$E#".
         * But our approach produces: "r0$A$BB$CD$E$$FG$E#"
         *
         * @return an example graph
         * @throws ControlIsAtomicException
         * @see <a href="https://www.ke.tu-darmstadt.de/lehre/archiv/ws0809/ml-sem/slides/Biesinger_Markus.pdf">https://www.ke.tu-darmstadt.de/lehre/archiv/ws0809/ml-sem/slides/Biesinger_Markus.pdf</a>
         */
        public Bigraph createBiesingerSampleBigraph() throws ControlIsAtomicException {
            DynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

            builder.root()
                    .child(signature.getControlByName("A"))
                    .down()
                    .child(signature.getControlByName("B"))
                    .down()
                    .child(signature.getControlByName("E"))
                    .down()
                    .child(signature.getControlByName("E")).up().up()
                    .child(signature.getControlByName("B"))
                    .down()
                    .child(signature.getControlByName("D"))
                    .down()
                    .child(signature.getControlByName("G"))
                    .child(signature.getControlByName("F"))
                    .up()
                    .child(signature.getControlByName("C"));
            return builder.create();
        }

        public Bigraph createNonMatchingBiesingerSampleBigraph() throws ControlIsAtomicException {
            DynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

            builder.root()
                    .child(signature.getControlByName("A"))
                    .down()
                    .child(signature.getControlByName("B"))
                    .down()
                    .child(signature.getControlByName("E"))
                    .down()
                    .child(signature.getControlByName("E")).up().up()
                    .child(signature.getControlByName("B"))
                    .down()
                    .child(signature.getControlByName("C"))
                    .down()
                    .child(signature.getControlByName("G"))
                    .child(signature.getControlByName("F"))
                    .up()
                    .child(signature.getControlByName("D"));
            return builder.create();
        }

        public Bigraph createSampleBigraphA1() throws ControlIsAtomicException {
            DynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

            builder.root()
                    .child(signature.getControlByName("A"))
                    .down()
                    .child(signature.getControlByName("B"))
                    .child(signature.getControlByName("B"))
                    .down()
                    .child(signature.getControlByName("C"))
                    .down()
                    .child(signature.getControlByName("H"))
                    .child(signature.getControlByName("G"))
                    .up()
                    .child(signature.getControlByName("D"))
                    .up()
                    .child(signature.getControlByName("B"))
                    .down()
                    .child(signature.getControlByName("E"))
                    .child(signature.getControlByName("C"))
                    .down()
                    .child(signature.getControlByName("H"))
                    .child(signature.getControlByName("F"))
                    .up()
                    .up()
                    .up()
            ;

            return builder.create();
        }

        public Bigraph createSampleBigraphA2() throws ControlIsAtomicException {
            DynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

            builder.root()
                    .child(signature.getControlByName("A"))
                    .down()
                    .child(signature.getControlByName("B"))
                    .down()
                    .child(signature.getControlByName("C"))
                    .down()
                    .child(signature.getControlByName("F"))
                    .child(signature.getControlByName("H"))
                    .up()
                    .child(signature.getControlByName("E"))
                    .up()
                    .child(signature.getControlByName("B"))
                    .down()
                    .child(signature.getControlByName("C"))
                    .down()
                    .child(signature.getControlByName("G"))
                    .child(signature.getControlByName("H"))
                    .up()
                    .child(signature.getControlByName("D"))
                    .up()
                    .child(signature.getControlByName("B"))
                    .up()
            ;

            return builder.create();
        }

        /**
         * Creates a list of basic place graphs which are all equal but each has a different ordering in terms of the
         * build procedure (i.e., node indexes).
         */
        public List<Bigraph> createSampleGraphs() {
            DynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);
            PureBigraphBuilder<DynamicSignature> b2 = pureBuilder(signature);
            PureBigraphBuilder<DynamicSignature> b3 = pureBuilder(signature);
            PureBigraphBuilder<DynamicSignature> b4 = pureBuilder(signature);

            b1.root()
                    .child(signature.getControlByName("A"))
                    .down()
                    .child(signature.getControlByName("B")).down()
                    .child(signature.getControlByName("C")).up()
                    .child(signature.getControlByName("B"))
                    .down()
                    .child(signature.getControlByName("D")).child(signature.getControlByName("C"));

            b2.root().child(signature.getControlByName("A")).down()
                    .child(signature.getControlByName("B")).down().child(signature.getControlByName("C"))
                    .up()
                    .child(signature.getControlByName("B")).down()
                    .child(signature.getControlByName("C")).child(signature.getControlByName("D"));

            b3.root().child(signature.getControlByName("A")).down()
                    .child(signature.getControlByName("B")).down()
                    .child(signature.getControlByName("D")).child(signature.getControlByName("C"))
                    .up()
                    .child(signature.getControlByName("B")).down().child(signature.getControlByName("C"))
            ;
            b4.root().child(signature.getControlByName("A")).down()
                    .child(signature.getControlByName("B")).down()
                    .child(signature.getControlByName("C")).child(signature.getControlByName("D"))
                    .up()
                    .child(signature.getControlByName("B")).down().child(signature.getControlByName("C"))
            ;

            return Lists.newArrayList(b1.create(), b2.create(), b3.create(), b4.create());
        }

        @Test
        void placeGraphs_canonicalForm_notEqual() throws Exception {
            PureBigraph bigraph = createA();
            PureBigraph bigraph2 = createB();
            BigraphGraphvizExporter.toPNG(bigraph,
                    true,
                    new File(TARGET_DUMP_PATH + "sampleBigraph_canonicalFormTest1.png")
            );
            BigraphGraphvizExporter.toPNG(bigraph2,
                    true,
                    new File(TARGET_DUMP_PATH + "sampleBigraph_canonicalFormTest2.png")
            );
            BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph2, new FileOutputStream(new File(TARGET_DUMP_PATH + "sampleBigraph_canonicalFormTest1.xmi")));


            String bfcs = BigraphCanonicalForm.createInstance().bfcs(bigraph);
            String bfcs2 = BigraphCanonicalForm.createInstance().bfcs(bigraph2);
            System.out.println(bfcs);
            System.out.println(bfcs2);
            assertNotEquals(bfcs, bfcs2);
        }

        private PureBigraph createA() throws Exception {
            DynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);
            b1.root().child("A").down()
                    .child("B").down().child("E").down().child("G").up().child("D").child("F").down().child("H").up().up()
                    .child("B").down().child("D").down().child("G").up().child("E").child("F").down().child("H").up().up()
                    .child("B").down().child("R").child("Q").top()
            ;
            return b1.create();
        }

        private PureBigraph createB() throws Exception {
            DynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);

            b1.root().child("A").down()
                    .child("B").down().child("D").down().child("G").up().child("E").child("F").down().child("H").up().up()
                    .child("B").down().child("D").down().child("G").up().child("E").child("F").down().child("H").up().up()
                    .child("B").down().child("R").child("Q").top()
            ;
            return b1.create();
        }
    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Test series for link graphs")
    class LinkGraphCanonicalFormTest {

        @Test
        void simple_canonical_form_with_links_test() throws InvalidConnectionException, TypeNotExistsException, IOException {
            Bigraph l1 = createSampleBigraph_with_Links();
            Bigraph l2 = createSampleBigraph_with_Links_v2();
            BigraphGraphvizExporter.toPNG(l1,
                    true,
                    new File(TARGET_DUMP_PATH + "sampleBigraphL_1.png")
            );
            BigraphGraphvizExporter.toPNG(l2,
                    true,
                    new File(TARGET_DUMP_PATH + "sampleBigraphL_2.png")
            );
            String bfcs = BigraphCanonicalForm.createInstance().bfcs(l1);
            String bfcs2 = BigraphCanonicalForm.createInstance().bfcs(l2);
            System.out.println(bfcs);
            System.out.println(bfcs2);
            Assertions.assertEquals(bfcs, bfcs2);

            AtomicInteger cnt2 = new AtomicInteger(0);
            long count2 = Stream.of(createSampleBigraphB1(), createSampleBigraphB2())//
                    .peek(x -> {
                        try {
                            BigraphGraphvizExporter.toPNG(x,
                                    true,
                                    new File(TARGET_DUMP_PATH + "sampleBigraph2_" + cnt2.incrementAndGet() + ".png")
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })
                    .map(x -> BigraphCanonicalForm.createInstance().bfcs(x))
                    .peek(System.out::println)
                    .distinct()
                    .count();
//            assertEquals(count2, 1);
        }

        public Bigraph createSampleBigraphB1() throws ControlIsAtomicException, InvalidConnectionException, LinkTypeNotExistsException {
            DynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
            BigraphEntity.InnerName inner = builder.createInner("inner");
            builder.root()
                    .child(signature.getControlByName("A"))
                    .down()
                    .child(signature.getControlByName("B")).linkInner(inner)
                    .child(signature.getControlByName("C")).linkInner(inner)
                    .up()
                    .child(signature.getControlByName("A"))
                    .down()
                    .child(signature.getControlByName("B"))
                    .child(signature.getControlByName("C"))
                    .up()
            ;
            return builder.closeInner().create();
        }

        public Bigraph createSampleBigraphB2() throws ControlIsAtomicException, InvalidConnectionException, LinkTypeNotExistsException {
            DynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
            BigraphEntity.InnerName inner = builder.createInner("inner");
            builder.root()
                    .child(signature.getControlByName("A"))
                    .down()
                    .child(signature.getControlByName("B"))
                    .child(signature.getControlByName("C"))
                    .up()
                    .child(signature.getControlByName("A"))
                    .down()
                    .child(signature.getControlByName("C")).linkInner(inner)
                    .child(signature.getControlByName("B")).linkInner(inner)
                    .up()
            ;
            return builder.closeInner().create();
        }

        public Bigraph createSampleBigraph_with_Links() throws InvalidConnectionException, TypeNotExistsException {
            DynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);

            BigraphEntity.InnerName edge = b1.createInner("edge");
            BigraphEntity.InnerName edge2 = b1.createInner("edge2");
            BigraphEntity.OuterName o1 = b1.createOuter("o1");
            BigraphEntity.InnerName x1 = b1.createInner("x1");

            b1.root().child("R")
                    .down()
                    .child("J").linkInner(edge2)
                    .child("A").linkInner(edge2).linkOuter(o1)
                    .child("A")
                    .site()
                    .child("J").linkInner(x1)
                    .up()
                    .child("R")
                    .down()
                    .child("A").linkInner(edge)
                    .child("J").linkInner(edge2)
//                .child("C")
            ;
            b1.closeInner(edge);
            b1.closeInner(edge2);
            return b1.create();
        }

        public Bigraph createSampleBigraph_with_Links_v2() throws InvalidConnectionException, TypeNotExistsException {
            DynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);

            BigraphEntity.InnerName edge = b1.createInner("e1");
            BigraphEntity.InnerName edge2 = b1.createInner("e2");
            BigraphEntity.OuterName o1 = b1.createOuter("o1");
            BigraphEntity.InnerName x1 = b1.createInner("x1");

            b1.root()
                    .child("R")
                    .down()
                    .child("A").linkInner(edge)
                    .child("J").linkInner(edge2)
                    .top()
                    .child("R")
                    .down()
                    .child("A")
                    .child("A").linkInner(edge2).linkOuter(o1)
                    .child("J").linkInner(edge2)
                    .child("J").linkInner(x1)
                    .site()
            ;
            b1.closeInner(edge);
            b1.closeInner(edge2);
            return b1.create();
        }


        // for the paper some example bigraphs
        @Test
        void name() throws InvalidConnectionException, LinkTypeNotExistsException {
            DynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);
            PureBigraphBuilder<DynamicSignature> b2 = pureBuilder(signature);
            BigraphEntity.InnerName x1 = b1.createInner("x1");
            BigraphEntity.InnerName x2 = b1.createInner("x2");
            BigraphEntity.InnerName x3 = b1.createInner("x3");
            BigraphEntity.InnerName x4 = b1.createInner("x4");
            BigraphEntity.InnerName x11 = b2.createInner("x1");
            BigraphEntity.InnerName x21 = b2.createInner("x2");
            BigraphEntity.InnerName x31 = b2.createInner("x3");
            BigraphEntity.InnerName x41 = b2.createInner("x4");
            b1.root()
                    .child("B").down().child("F").linkInner(x1).linkInner(x2).child("D").linkInner(x1).child("E").linkInner(x2).up()
                    .child("B").down().child("D").linkInner(x3).child("E").linkInner(x3).linkInner(x4).child("F").linkInner(x4).up()
            ;
            b1.closeInner(x1, x2, x3, x4);

            b2.root()
                    .child("B").down().child("F").linkInner(x11).child("D").linkInner(x11).linkInner(x21).child("E").linkInner(x21).up()
                    .child("B").down().child("D").linkInner(x31).child("E").linkInner(x31).linkInner(x41).child("F").linkInner(x41).up()
            ;
            b2.closeInner(x11, x21, x31, x41);

            PureBigraph bigraph = b1.create();
            PureBigraph bigraph1 = b2.create();

            String bfcs = BigraphCanonicalForm.createInstance().bfcs(bigraph);
            String bfcs1 = BigraphCanonicalForm.createInstance().bfcs(bigraph1);
            System.out.println(bfcs);
            System.out.println(bfcs1);

            assertNotEquals(bfcs, bfcs1);
        }

        public PureBigraph createFirstLG() throws Exception {
            DynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);
            BigraphEntity.InnerName x1 = b1.createInner("x1");
            BigraphEntity.InnerName x2 = b1.createInner("x2");
            BigraphEntity.InnerName e0 = b1.createInner("e0");
            BigraphEntity.InnerName e1 = b1.createInner("e1");
//        BigraphEntity.InnerName e2 = b1.createInnerName("e2");
            BigraphEntity.OuterName y1 = b1.createOuter("y1");
            BigraphEntity.OuterName y2 = b1.createOuter("y2");

            b1.root().child("A").down()
                    .child("B").down().child("D").linkInner(e0).linkOuter(y1).child("E").linkInner(e0).linkOuter(y1).child("F").linkOuter(y1).down().child("G").child("H").up().up()
                    .child("B").down().child("D").linkInner(e1).down().child("G").up().child("E").linkInner(e1).child("F").linkOuter(y1).down().child("H").up().up()
                    .child("B").down().child("R").linkInner(x1).child("Q").linkInner(x1).top()
            ;
            b1.closeInner(e0, e1);
            b1.linkInnerToOuter(x2, y2);
            return b1.create();
        }

        public PureBigraph createSecondLG() throws Exception {
            DynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DynamicSignature> b1 = pureBuilder(signature);
            BigraphEntity.InnerName x1 = b1.createInner("x1");
            BigraphEntity.InnerName x2 = b1.createInner("x2");
//        BigraphEntity.InnerName e0 = b1.createInnerName("e0");
            BigraphEntity.InnerName e1 = b1.createInner("e1");
//        BigraphEntity.InnerName e2 = b1.createInnerName("e2");
            BigraphEntity.OuterName y1 = b1.createOuter("y1");
            BigraphEntity.OuterName y2 = b1.createOuter("y2");

            b1.root().child("A").down()
                    .child("B").down().child("D").linkInner(x1).linkOuter(y1).child("E").linkInner(x1).linkOuter(y1).child("F").linkOuter(y1).down().child("G").child("H").up().up()
                    .child("B").down().child("D").linkInner(e1).down().child("G").up().child("E").linkInner(e1).child("F").linkOuter(y1).down().child("H").up().up()
                    .child("B").down().child("R").child("Q").top()
            ;
            b1.closeInner(e1);
            b1.linkInnerToOuter(x2, y2);
            return b1.create();
        }
    }


    private <C extends Control<?, ?>, S extends Signature<C>> S createAlphabeticSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("C")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("D")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("E")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("F")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("G")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("H")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("I")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("J")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("Q")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("R")).arity(FiniteOrdinal.ofInteger(5)).assign()
        ;

        return (S) defaultBuilder.create();
    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createRandomSignature(int n, float probOfPositiveArity) {
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
        S s = (S) signatureBuilder.create();
        ArrayList<C> cs = new ArrayList<>(s.getControls());
        Collections.shuffle(cs);
        return (S) signatureBuilder.createWith((Iterable<? extends Control<StringTypedName, FiniteOrdinal<Integer>>>) new LinkedHashSet<C>(cs));
    }
}
