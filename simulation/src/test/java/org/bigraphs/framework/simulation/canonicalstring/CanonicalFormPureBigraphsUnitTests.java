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
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
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
        DefaultDynamicSignature sig = createAlphabeticSignature();
        BigraphCanonicalForm instance = BigraphCanonicalForm.createInstance();
        PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(sig);
        BigraphEntity.InnerName tmp = b.createInnerName("tmp");
        b.createRoot()
                .addChild("A")
                .addChild("B").down().addChild("C").linkToInner(tmp).top();
        b.createRoot().addChild("B").down().addChild("D").linkToInner(tmp).up().addChild("A").top();
        b.closeInnerName(tmp);
        PureBigraph bigraphA = b.createBigraph();
//        BigraphFileModelManagement.exportAsInstanceModel(bigraphA, System.out);
        String bfcsA = instance.bfcs(bigraphA);
        System.out.println(bfcsA);

        // same bigraph as above, but only roots swapped (technically)
        PureBigraphBuilder<DefaultDynamicSignature> b2 = pureBuilder(sig);
        BigraphEntity.InnerName tmp2 = b2.createInnerName("tmp");
        b2.createRoot().addChild("B").down().addChild("D").linkToInner(tmp2).up().addChild("A").top();
        b2.createRoot().addChild("A").addChild("B").down().addChild("C").linkToInner(tmp2).top();
        b2.closeInnerName(tmp2);
        PureBigraph bigraph2 = b2.createBigraph();
//        BigraphFileModelManagement.exportAsInstanceModel(bigraph2, System.out);

        String bfcs2 = instance.bfcs(bigraph2);
        System.out.println(bfcs2);

        PureBigraphBuilder<DefaultDynamicSignature> b3 = pureBuilder(sig);
        b3.createRoot().addChild("G").down().addSite().up().addChild("H").down().addSite();
        PureBigraph big3 = b3.createBigraph();
//        BigraphFileModelManagement.exportAsInstanceModel(big3, System.out);
        String bfcs = instance.bfcs(big3);
        System.out.println(bfcs);

        Bigraph<DefaultDynamicSignature> outerBigraph = ops(big3).compose(bigraphA).getOuterBigraph();
        String outerBigraphSE = instance.bfcs(outerBigraph);
        System.out.println(outerBigraphSE);

        Bigraph<DefaultDynamicSignature> outerBigraph2 = ops(big3).compose(bigraph2).getOuterBigraph();
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
        DefaultDynamicSignature carMapSignature = RouteFinding.createSignature();

        PureBigraphBuilder<DefaultDynamicSignature> builder_9 = PureBigraphBuilder.create(carMapSignature, metaModelFile, instanceModelFile_9);
        PureBigraphBuilder<DefaultDynamicSignature> builder_17 = PureBigraphBuilder.create(carMapSignature, metaModelFile, instanceModelFile_17);

        PureBigraph bigraph_9 = builder_9.createBigraph();
        PureBigraph bigraph_17 = builder_17.createBigraph();
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
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignatureProcess());

        builder.createRoot()
                .addChild("Process", "access2")
                .addChild("Process", "access1")
                .addChild("Resource").down().addChild("Token").up()
        ;
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    private DefaultDynamicSignature createSignatureProcess() {
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
        DefaultDynamicSignature signature = createAlphabeticSignature();
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);
        BigraphEntity.InnerName e0 = b1.createInnerName("e0");
//        BigraphEntity.OuterName y1 = b1.createOuterName("y1");

        b1.createRoot().addChild("B")
                .down()
                .addChild("D", "y1").linkToInner(e0)
                .addChild("F", "y1").down().addChild("H").addChild("G").down().addSite().up().up()
                .addChild("E", "y1").linkToInner(e0)
        ;
        b1.closeAllInnerNames();
        return b1.createBigraph();
    }

    public PureBigraph createBigraph_a() throws InvalidConnectionException, TypeNotExistsException {
        DefaultDynamicSignature signature = createAlphabeticSignature();
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);
        BigraphEntity.InnerName b = b1.createInnerName("b");
        BigraphEntity.OuterName y1 = b1.createOuterName("y1");
        BigraphEntity.OuterName y2 = b1.createOuterName("y2");

        PureBigraphBuilder.Hierarchy left = b1.hierarchy("A").connectByEdge("D", "E").addChild("F").linkToOuter(y1).down().addChild("G").addChild("H").down().addSite().top();
        PureBigraphBuilder.Hierarchy middle = b1.hierarchy("B").addChild("A").addChild("B").down().addSite().top();
        PureBigraphBuilder.Hierarchy right = b1.hierarchy("C").connectByEdge("D", "E").addChild("F").linkToOuter(y2).top();
        b1.connectInnerToOuterName(b, y2);
        b1.createRoot().addChild("Q").down().addChild(left.top()).addChild(middle.top()).addChild(right.top());
        return b1.createBigraph();
    }

    public PureBigraph createBigraph_b() throws InvalidConnectionException, TypeNotExistsException {
        DefaultDynamicSignature signature = createAlphabeticSignature();
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);
        BigraphEntity.OuterName y1 = b1.createOuterName("y1");

        PureBigraphBuilder.Hierarchy left = b1.hierarchy("A").connectByEdge("D", "E").addChild("F").linkToOuter(y1).down().addChild("H").addChild("G").down().addSite().top();
        PureBigraphBuilder.Hierarchy middle = b1.hierarchy("C").addChild("A").addChild("B").top();
        PureBigraphBuilder.Hierarchy right = b1.hierarchy("B").connectByEdge("D", "E").addChild("F").linkToOuter(y1).top();

        b1.createRoot().addChild("Q").down().addChild(left).addChild(middle).addChild(right);
        PureBigraph bigraph = b1.createBigraph();
        return bigraph;
    }

    public PureBigraph createBigraph_c() throws InvalidConnectionException, TypeNotExistsException {
        DefaultDynamicSignature signature = createAlphabeticSignature();
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);

        BigraphEntity.InnerName e0 = b1.createInnerName("e0");
        BigraphEntity.InnerName e1 = b1.createInnerName("e1");
        BigraphEntity.InnerName e2 = b1.createInnerName("e2");
        BigraphEntity.InnerName e3 = b1.createInnerName("e3");

        b1.createRoot()
                .addChild("B").down()
                .addChild("D").linkToInner(e0).down().addChild("G").up().addChild("F").linkToInner(e0).linkToInner(e1).down().addChild("H").up().addChild("E").linkToInner(e1).up()
                .addChild("B").down()
                .addChild("D").linkToInner(e2).down().addChild("G").up().addChild("E").linkToInner(e2).linkToInner(e3).addChild("F").linkToInner(e3).down().addChild("H").up().up()
        ;
        b1.closeInnerNames(e0, e1, e2, e3);
        return b1.createBigraph();
    }


    public PureBigraph createBigraph_d() throws InvalidConnectionException, TypeNotExistsException {
        DefaultDynamicSignature signature = createAlphabeticSignature();
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);
        BigraphEntity.InnerName e0 = b1.createInnerName("e0");
        BigraphEntity.InnerName e1 = b1.createInnerName("e1");
        BigraphEntity.InnerName e2 = b1.createInnerName("e2");
        BigraphEntity.InnerName e3 = b1.createInnerName("e3");
        b1.createRoot()
                .addChild("B").down().addChild("D").linkToInner(e0).linkToInner(e1).down().addChild("G").up().addChild("E").linkToInner(e1).addChild("F").linkToInner(e0).down().addChild("H").up().up()
                .addChild("B").down().addChild("D").linkToInner(e2).down().addChild("G").up().addChild("E").linkToInner(e2).linkToInner(e3).addChild("F").linkToInner(e3).down().addChild("H").up().up()
        ;
        b1.closeInnerNames(e0, e1, e2, e3);
        return b1.createBigraph();
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
        DefaultDynamicSignature randomSignature = createRandomSignature(26, 1f);
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
            DefaultDynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

            builder.createRoot()
                    .addChild(signature.getControlByName("A"))
                    .down()
                    .addChild(signature.getControlByName("B")).down()
                    .addChild("E").down().addChild("A").up().up()
                    .addChild(signature.getControlByName("C")).down()
                    .addChild("F").down().addChild("B").addChild("D").up().addChild("A").up()
                    .addChild(signature.getControlByName("D")).down()
                    .addChild("F").down().addChild("C")
            ;
            return builder.createBigraph();
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
            DefaultDynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

            builder.createRoot()
                    .addChild(signature.getControlByName("A"))
                    .down()
                    .addChild(signature.getControlByName("B"))
                    .down()
                    .addChild(signature.getControlByName("E"))
                    .down()
                    .addChild(signature.getControlByName("E")).up().up()
                    .addChild(signature.getControlByName("B"))
                    .down()
                    .addChild(signature.getControlByName("D"))
                    .down()
                    .addChild(signature.getControlByName("G"))
                    .addChild(signature.getControlByName("F"))
                    .up()
                    .addChild(signature.getControlByName("C"));
            return builder.createBigraph();
        }

        public Bigraph createNonMatchingBiesingerSampleBigraph() throws ControlIsAtomicException {
            DefaultDynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

            builder.createRoot()
                    .addChild(signature.getControlByName("A"))
                    .down()
                    .addChild(signature.getControlByName("B"))
                    .down()
                    .addChild(signature.getControlByName("E"))
                    .down()
                    .addChild(signature.getControlByName("E")).up().up()
                    .addChild(signature.getControlByName("B"))
                    .down()
                    .addChild(signature.getControlByName("C"))
                    .down()
                    .addChild(signature.getControlByName("G"))
                    .addChild(signature.getControlByName("F"))
                    .up()
                    .addChild(signature.getControlByName("D"));
            return builder.createBigraph();
        }

        public Bigraph createSampleBigraphA1() throws ControlIsAtomicException {
            DefaultDynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

            builder.createRoot()
                    .addChild(signature.getControlByName("A"))
                    .down()
                    .addChild(signature.getControlByName("B"))
                    .addChild(signature.getControlByName("B"))
                    .down()
                    .addChild(signature.getControlByName("C"))
                    .down()
                    .addChild(signature.getControlByName("H"))
                    .addChild(signature.getControlByName("G"))
                    .up()
                    .addChild(signature.getControlByName("D"))
                    .up()
                    .addChild(signature.getControlByName("B"))
                    .down()
                    .addChild(signature.getControlByName("E"))
                    .addChild(signature.getControlByName("C"))
                    .down()
                    .addChild(signature.getControlByName("H"))
                    .addChild(signature.getControlByName("F"))
                    .up()
                    .up()
                    .up()
            ;

            return builder.createBigraph();
        }

        public Bigraph createSampleBigraphA2() throws ControlIsAtomicException {
            DefaultDynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

            builder.createRoot()
                    .addChild(signature.getControlByName("A"))
                    .down()
                    .addChild(signature.getControlByName("B"))
                    .down()
                    .addChild(signature.getControlByName("C"))
                    .down()
                    .addChild(signature.getControlByName("F"))
                    .addChild(signature.getControlByName("H"))
                    .up()
                    .addChild(signature.getControlByName("E"))
                    .up()
                    .addChild(signature.getControlByName("B"))
                    .down()
                    .addChild(signature.getControlByName("C"))
                    .down()
                    .addChild(signature.getControlByName("G"))
                    .addChild(signature.getControlByName("H"))
                    .up()
                    .addChild(signature.getControlByName("D"))
                    .up()
                    .addChild(signature.getControlByName("B"))
                    .up()
            ;

            return builder.createBigraph();
        }

        /**
         * Creates a list of basic place graphs which are all equal but each has a different ordering in terms of the
         * build procedure (i.e., node indexes).
         */
        public List<Bigraph> createSampleGraphs() {
            DefaultDynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);
            PureBigraphBuilder<DefaultDynamicSignature> b2 = pureBuilder(signature);
            PureBigraphBuilder<DefaultDynamicSignature> b3 = pureBuilder(signature);
            PureBigraphBuilder<DefaultDynamicSignature> b4 = pureBuilder(signature);

            b1.createRoot()
                    .addChild(signature.getControlByName("A"))
                    .down()
                    .addChild(signature.getControlByName("B")).down()
                    .addChild(signature.getControlByName("C")).up()
                    .addChild(signature.getControlByName("B"))
                    .down()
                    .addChild(signature.getControlByName("D")).addChild(signature.getControlByName("C"));

            b2.createRoot().addChild(signature.getControlByName("A")).down()
                    .addChild(signature.getControlByName("B")).down().addChild(signature.getControlByName("C"))
                    .up()
                    .addChild(signature.getControlByName("B")).down()
                    .addChild(signature.getControlByName("C")).addChild(signature.getControlByName("D"));

            b3.createRoot().addChild(signature.getControlByName("A")).down()
                    .addChild(signature.getControlByName("B")).down()
                    .addChild(signature.getControlByName("D")).addChild(signature.getControlByName("C"))
                    .up()
                    .addChild(signature.getControlByName("B")).down().addChild(signature.getControlByName("C"))
            ;
            b4.createRoot().addChild(signature.getControlByName("A")).down()
                    .addChild(signature.getControlByName("B")).down()
                    .addChild(signature.getControlByName("C")).addChild(signature.getControlByName("D"))
                    .up()
                    .addChild(signature.getControlByName("B")).down().addChild(signature.getControlByName("C"))
            ;

            return Lists.newArrayList(b1.createBigraph(), b2.createBigraph(), b3.createBigraph(), b4.createBigraph());
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
            DefaultDynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);
            b1.createRoot().addChild("A").down()
                    .addChild("B").down().addChild("E").down().addChild("G").up().addChild("D").addChild("F").down().addChild("H").up().up()
                    .addChild("B").down().addChild("D").down().addChild("G").up().addChild("E").addChild("F").down().addChild("H").up().up()
                    .addChild("B").down().addChild("R").addChild("Q").top()
            ;
            return b1.createBigraph();
        }

        private PureBigraph createB() throws Exception {
            DefaultDynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);

            b1.createRoot().addChild("A").down()
                    .addChild("B").down().addChild("D").down().addChild("G").up().addChild("E").addChild("F").down().addChild("H").up().up()
                    .addChild("B").down().addChild("D").down().addChild("G").up().addChild("E").addChild("F").down().addChild("H").up().up()
                    .addChild("B").down().addChild("R").addChild("Q").top()
            ;
            return b1.createBigraph();
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
            DefaultDynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
            BigraphEntity.InnerName inner = builder.createInnerName("inner");
            builder.createRoot()
                    .addChild(signature.getControlByName("A"))
                    .down()
                    .addChild(signature.getControlByName("B")).linkToInner(inner)
                    .addChild(signature.getControlByName("C")).linkToInner(inner)
                    .up()
                    .addChild(signature.getControlByName("A"))
                    .down()
                    .addChild(signature.getControlByName("B"))
                    .addChild(signature.getControlByName("C"))
                    .up()
            ;
            return builder.closeAllInnerNames().createBigraph();
        }

        public Bigraph createSampleBigraphB2() throws ControlIsAtomicException, InvalidConnectionException, LinkTypeNotExistsException {
            DefaultDynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
            BigraphEntity.InnerName inner = builder.createInnerName("inner");
            builder.createRoot()
                    .addChild(signature.getControlByName("A"))
                    .down()
                    .addChild(signature.getControlByName("B"))
                    .addChild(signature.getControlByName("C"))
                    .up()
                    .addChild(signature.getControlByName("A"))
                    .down()
                    .addChild(signature.getControlByName("C")).linkToInner(inner)
                    .addChild(signature.getControlByName("B")).linkToInner(inner)
                    .up()
            ;
            return builder.closeAllInnerNames().createBigraph();
        }

        public Bigraph createSampleBigraph_with_Links() throws InvalidConnectionException, TypeNotExistsException {
            DefaultDynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);

            BigraphEntity.InnerName edge = b1.createInnerName("edge");
            BigraphEntity.InnerName edge2 = b1.createInnerName("edge2");
            BigraphEntity.OuterName o1 = b1.createOuterName("o1");
            BigraphEntity.InnerName x1 = b1.createInnerName("x1");

            b1.createRoot().addChild("R")
                    .down()
                    .addChild("J").linkToInner(edge2)
                    .addChild("A").linkToInner(edge2).linkToOuter(o1)
                    .addChild("A")
                    .addSite()
                    .addChild("J").linkToInner(x1)
                    .up()
                    .addChild("R")
                    .down()
                    .addChild("A").linkToInner(edge)
                    .addChild("J").linkToInner(edge2)
//                .addChild("C")
            ;
            b1.closeInnerName(edge);
            b1.closeInnerName(edge2);
            return b1.createBigraph();
        }

        public Bigraph createSampleBigraph_with_Links_v2() throws InvalidConnectionException, TypeNotExistsException {
            DefaultDynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);

            BigraphEntity.InnerName edge = b1.createInnerName("e1");
            BigraphEntity.InnerName edge2 = b1.createInnerName("e2");
            BigraphEntity.OuterName o1 = b1.createOuterName("o1");
            BigraphEntity.InnerName x1 = b1.createInnerName("x1");

            b1.createRoot()
                    .addChild("R")
                    .down()
                    .addChild("A").linkToInner(edge)
                    .addChild("J").linkToInner(edge2)
                    .top()
                    .addChild("R")
                    .down()
                    .addChild("A")
                    .addChild("A").linkToInner(edge2).linkToOuter(o1)
                    .addChild("J").linkToInner(edge2)
                    .addChild("J").linkToInner(x1)
                    .addSite()
            ;
            b1.closeInnerName(edge);
            b1.closeInnerName(edge2);
            return b1.createBigraph();
        }


        // for the paper some example bigraphs
        @Test
        void name() throws InvalidConnectionException, LinkTypeNotExistsException {
            DefaultDynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);
            PureBigraphBuilder<DefaultDynamicSignature> b2 = pureBuilder(signature);
            BigraphEntity.InnerName x1 = b1.createInnerName("x1");
            BigraphEntity.InnerName x2 = b1.createInnerName("x2");
            BigraphEntity.InnerName x3 = b1.createInnerName("x3");
            BigraphEntity.InnerName x4 = b1.createInnerName("x4");
            BigraphEntity.InnerName x11 = b2.createInnerName("x1");
            BigraphEntity.InnerName x21 = b2.createInnerName("x2");
            BigraphEntity.InnerName x31 = b2.createInnerName("x3");
            BigraphEntity.InnerName x41 = b2.createInnerName("x4");
            b1.createRoot()
                    .addChild("B").down().addChild("F").linkToInner(x1).linkToInner(x2).addChild("D").linkToInner(x1).addChild("E").linkToInner(x2).up()
                    .addChild("B").down().addChild("D").linkToInner(x3).addChild("E").linkToInner(x3).linkToInner(x4).addChild("F").linkToInner(x4).up()
            ;
            b1.closeInnerNames(x1, x2, x3, x4);

            b2.createRoot()
                    .addChild("B").down().addChild("F").linkToInner(x11).addChild("D").linkToInner(x11).linkToInner(x21).addChild("E").linkToInner(x21).up()
                    .addChild("B").down().addChild("D").linkToInner(x31).addChild("E").linkToInner(x31).linkToInner(x41).addChild("F").linkToInner(x41).up()
            ;
            b2.closeInnerNames(x11, x21, x31, x41);

            PureBigraph bigraph = b1.createBigraph();
            PureBigraph bigraph1 = b2.createBigraph();

            String bfcs = BigraphCanonicalForm.createInstance().bfcs(bigraph);
            String bfcs1 = BigraphCanonicalForm.createInstance().bfcs(bigraph1);
            System.out.println(bfcs);
            System.out.println(bfcs1);

            assertNotEquals(bfcs, bfcs1);
        }

        public PureBigraph createFirstLG() throws Exception {
            DefaultDynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);
            BigraphEntity.InnerName x1 = b1.createInnerName("x1");
            BigraphEntity.InnerName x2 = b1.createInnerName("x2");
            BigraphEntity.InnerName e0 = b1.createInnerName("e0");
            BigraphEntity.InnerName e1 = b1.createInnerName("e1");
//        BigraphEntity.InnerName e2 = b1.createInnerName("e2");
            BigraphEntity.OuterName y1 = b1.createOuterName("y1");
            BigraphEntity.OuterName y2 = b1.createOuterName("y2");

            b1.createRoot().addChild("A").down()
                    .addChild("B").down().addChild("D").linkToInner(e0).linkToOuter(y1).addChild("E").linkToInner(e0).linkToOuter(y1).addChild("F").linkToOuter(y1).down().addChild("G").addChild("H").up().up()
                    .addChild("B").down().addChild("D").linkToInner(e1).down().addChild("G").up().addChild("E").linkToInner(e1).addChild("F").linkToOuter(y1).down().addChild("H").up().up()
                    .addChild("B").down().addChild("R").linkToInner(x1).addChild("Q").linkToInner(x1).top()
            ;
            b1.closeInnerNames(e0, e1);
            b1.connectInnerToOuterName(x2, y2);
            return b1.createBigraph();
        }

        public PureBigraph createSecondLG() throws Exception {
            DefaultDynamicSignature signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);
            BigraphEntity.InnerName x1 = b1.createInnerName("x1");
            BigraphEntity.InnerName x2 = b1.createInnerName("x2");
//        BigraphEntity.InnerName e0 = b1.createInnerName("e0");
            BigraphEntity.InnerName e1 = b1.createInnerName("e1");
//        BigraphEntity.InnerName e2 = b1.createInnerName("e2");
            BigraphEntity.OuterName y1 = b1.createOuterName("y1");
            BigraphEntity.OuterName y2 = b1.createOuterName("y2");

            b1.createRoot().addChild("A").down()
                    .addChild("B").down().addChild("D").linkToInner(x1).linkToOuter(y1).addChild("E").linkToInner(x1).linkToOuter(y1).addChild("F").linkToOuter(y1).down().addChild("G").addChild("H").up().up()
                    .addChild("B").down().addChild("D").linkToInner(e1).down().addChild("G").up().addChild("E").linkToInner(e1).addChild("F").linkToOuter(y1).down().addChild("H").up().up()
                    .addChild("B").down().addChild("R").addChild("Q").top()
            ;
            b1.closeInnerNames(e1);
            b1.connectInnerToOuterName(x2, y2);
            return b1.createBigraph();
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
