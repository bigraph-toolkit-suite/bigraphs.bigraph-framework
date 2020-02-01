package de.tudresden.inf.st.bigraphs.simulation.canonicalstring;

import com.google.common.collect.Lists;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphArtifacts;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.generators.PureBigraphGenerator;
import de.tudresden.inf.st.bigraphs.simulation.encoding.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.predicates.BigraphIsoPredicate;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Dominik Grzelak
 */
public class CanonicalFormRepresentationUnitTests {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/canonicform/";

    private PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    @Test
    void symmetricProcesses() throws InvalidConnectionException {
        PureBigraph agentProcess = createAgentProcess();
//        String bfcs0 = BigraphCanonicalForm.createInstance().bfcs(agentProcess);
        String bfcs1 = BigraphCanonicalForm.createInstance(true).bfcs(agentProcess);
//        System.out.println(bfcs0);
        System.out.println(bfcs1);

    }

    PureBigraph createAgentProcess() throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(createSignatureProcess());

        builder.createRoot()
                .addChild("Process", "access2")
                .addChild("Process", "access1")
                .addChild("Resource").down().addChild("Token").up()
        ;
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }
    private <C extends Control<?, ?>, S extends Signature<C>> S createSignatureProcess() {
        DynamicSignatureBuilder defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Process")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Token")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Working")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Resource")).arity(FiniteOrdinal.ofInteger(1)).assign()
        ;
        return (S) defaultBuilder.create();
    }

    @Test
    void paper_bigraphs() throws InvalidConnectionException, TypeNotExistsException, IOException {
        PureBigraph bigraph_a = createBigraph_a();
        PureBigraph bigraph_b = createBigraph_b();
        PureBigraph bigraph_c = createBigraph_c();
        PureBigraph bigraph_d = createBigraph_d();

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

        assertEquals(bfcs0, "r0$Q$ABC$D{e0}E{e0}F{y0}$D{e1}E{e1}F{y1}$AB$$$GH$$$$$1$0#x0y1#");
        assertEquals(bfcs1, "r0$Q$ABC$D{e0}E{e0}F{y0}$D{e1}E{e1}F{y0}$AB$$$GH$$$$$$0#");
        assertEquals(bfcs2, "r0$BB$D{e0}E{e0e1}F{e1}$D{e2}E{e3}F{e2e3}$G$$H$G$$H#");
        assertEquals(bfcs3, "r0$BB$D{e0}E{e0e1}F{e1}$D{e2e3}E{e3}F{e2}$G$$H$G$$H#");

        PureBigraph subbigraph_b = createSubbigraph_b();
        String s1 = BigraphCanonicalForm.createInstance().bfcs(subbigraph_b);
        System.out.println(s1);
    }

    public PureBigraph createSubbigraph_b() throws InvalidConnectionException, TypeNotExistsException {
        Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
        PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);
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
        Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
        PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);
        BigraphEntity.InnerName b = b1.createInnerName("b");
        BigraphEntity.OuterName y1 = b1.createOuterName("y1");
        BigraphEntity.OuterName y2 = b1.createOuterName("y2");

        PureBigraphBuilder.Hierarchy left = b1.hierarchy("A").connectByEdge("D", "E").addChild("F").linkToOuter(y1).down().addChild("H").addChild("G").down().addSite().top();
        PureBigraphBuilder.Hierarchy middle = b1.hierarchy("C").addChild("A").addChild("B").down().addSite().top();
        PureBigraphBuilder.Hierarchy right = b1.hierarchy("B").connectByEdge("D", "E").addChild("F").linkToOuter(y2).top();
        b1.connectInnerToOuterName(b, y2);
        b1.createRoot().addChild("Q").down().addChild(left).addChild(middle).addChild(right);
        return b1.createBigraph();
    }

    public PureBigraph createBigraph_b() throws InvalidConnectionException, TypeNotExistsException {
        Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
        PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName y1 = b1.createOuterName("y1");

        PureBigraphBuilder.Hierarchy left = b1.hierarchy("A").connectByEdge("D", "E").addChild("F").linkToOuter(y1).down().addChild("H").addChild("G").down().addSite().top();
        PureBigraphBuilder.Hierarchy middle = b1.hierarchy("C").addChild("A").addChild("B").top();
        PureBigraphBuilder.Hierarchy right = b1.hierarchy("B").connectByEdge("D", "E").addChild("F").linkToOuter(y1).top();

        b1.createRoot().addChild("Q").down().addChild(left).addChild(middle).addChild(right);
        PureBigraph bigraph = b1.createBigraph();
        return bigraph;
    }

    public PureBigraph createBigraph_c() throws InvalidConnectionException, TypeNotExistsException {
        Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
        PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);

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
        Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
        PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);
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
        BigraphGraphvizExporter.toPNG(bigraph2,
                true,
                new File(TARGET_DUMP_PATH + "paperbigraph2.png")
        );
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
        PureBigraph g0 = new PureBigraphGenerator().generate(randomSignature, 1, 30, 1f);

        BigraphIsoPredicate<PureBigraph> pureBigraphBigraphIsoPredicate = BigraphIsoPredicate.create(g0);
        boolean test = pureBigraphBigraphIsoPredicate.test(g0);
        Assertions.assertTrue(test);

        PureBigraph g1 = new PureBigraphGenerator().generate(randomSignature, 1, 30, 1f);
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
            Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

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
            Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

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
            Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

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
            Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

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
            Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

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
            Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);
            PureBigraphBuilder<DefaultDynamicSignature> b2 = factory.createBigraphBuilder(signature);
            PureBigraphBuilder<DefaultDynamicSignature> b3 = factory.createBigraphBuilder(signature);
            PureBigraphBuilder<DefaultDynamicSignature> b4 = factory.createBigraphBuilder(signature);

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
            BigraphArtifacts.exportAsInstanceModel(bigraph2, new FileOutputStream(new File(TARGET_DUMP_PATH + "sampleBigraph_canonicalFormTest1.xmi")));


            String bfcs = BigraphCanonicalForm.createInstance().bfcs(bigraph);
            String bfcs2 = BigraphCanonicalForm.createInstance().bfcs(bigraph2);
            System.out.println(bfcs);
            System.out.println(bfcs2);
            assertNotEquals(bfcs, bfcs2);
        }

        private PureBigraph createA() throws Exception {
            Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);
            b1.createRoot().addChild("A").down()
                    .addChild("B").down().addChild("E").down().addChild("G").up().addChild("D").addChild("F").down().addChild("H").up().up()
                    .addChild("B").down().addChild("D").down().addChild("G").up().addChild("E").addChild("F").down().addChild("H").up().up()
                    .addChild("B").down().addChild("R").addChild("Q").top()
            ;
            return b1.createBigraph();
        }

        private PureBigraph createB() throws Exception {
            Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);

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
            assertEquals(count2, 1);
        }

        public Bigraph createSampleBigraphB1() throws ControlIsAtomicException, InvalidConnectionException, LinkTypeNotExistsException {
            Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
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
            Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
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
            Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);

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
            Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);

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
            Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);
            PureBigraphBuilder<DefaultDynamicSignature> b2 = factory.createBigraphBuilder(signature);
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
            Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);
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
            Signature<DefaultDynamicControl> signature = createAlphabeticSignature();
            PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);
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
        DynamicSignatureBuilder defaultBuilder = factory.createSignatureBuilder();
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
        DynamicSignatureBuilder signatureBuilder = factory.createSignatureBuilder();

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
