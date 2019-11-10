package de.tudresden.inf.st.bigraphs.rewriting;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.utils.PureBigraphGenerator;
import de.tudresden.inf.st.bigraphs.core.utils.RandomBigraphGenerator;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.predicates.BigraphIsoPredicate;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dominik Grzelak
 */
public class CanonicalFormRepresentation {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/canonicform/";

    private PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = AbstractBigraphFactory.createPureBigraphFactory();

    @BeforeAll
    static void setUp() {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
    }

    @Test
    void with_predicate_test() {
        DefaultDynamicSignature randomSignature = createRandomSignature(26, 1f);
        PureBigraph g0 = new PureBigraphGenerator().generate(randomSignature, 1, 30, 1f);
        BigraphIsoPredicate<PureBigraph> pureBigraphBigraphIsoPredicate = BigraphIsoPredicate.create(g0);
        boolean test = pureBigraphBigraphIsoPredicate.test(g0);
        Assertions.assertTrue(test);

        PureBigraph g1 = new PureBigraphGenerator().generate(randomSignature, 1, 30, 1f);
        boolean test2 = pureBigraphBigraphIsoPredicate.negate().test(g1);
        Assertions.assertTrue(test2);

    }

    @Test
    void simple_canonical_form_test() throws IOException, InvalidConnectionException, LinkTypeNotExistsException {

        Bigraph biesingerSampleBigraph = createBiesingerSampleBigraph();
        String bfcs = BigraphCanonicalForm.getInstance().bfcs(biesingerSampleBigraph);
        assertEquals(bfcs, "r0$" + "A$BB$CD$E$FG$E#");
        System.out.println(bfcs);
        BigraphGraphvizExporter.toPNG(biesingerSampleBigraph,
                true,
                new File(TARGET_DUMP_PATH + "sampleBigraph_biesinger.png")
        );

        AtomicInteger cnt2 = new AtomicInteger(0);
        long count2 = Stream.of(createSampleBigraphB1(), createSampleBigraphB2())
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
                .map(x -> BigraphCanonicalForm.getInstance().bfcs(x))
                .peek(System.out::println)
                .distinct()
                .count();
        assertEquals(count2, 1);
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
                .map(x -> BigraphCanonicalForm.getInstance().bfcs(x))
                .peek(System.out::println)
                .distinct()
                .count();
        assertEquals(count, 1);

        List<Bigraph> sampleGraphs = createSampleGraphs();
        AtomicInteger cnt1 = new AtomicInteger(0);
        long num = sampleGraphs.stream()
                .peek(x -> {
                    try {
                        BigraphGraphvizExporter.toPNG(x,
                                true,
                                new File(TARGET_DUMP_PATH + "sampleBigraph1_" + cnt1.incrementAndGet() + ".png")
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .map(x -> BigraphCanonicalForm.getInstance().bfcs(x))
                .peek(System.out::println)
                .distinct()
                .count();
//                .allMatch(x -> BigraphCanonicalForm.getInstance().bfcs(sampleGraphs.get(0)))
//                .reduce((s1, s21) -> s1.compareTo(s2) == 0 ? s1 : "").get();
//        assert !s3.isEmpty();
        assertEquals(num, 1);
    }

    @Test
    void compute_canonical_of_random_bigraph() throws IOException {
//        RandomBigraphGenerator.split()
        DefaultDynamicSignature randomSignature = createRandomSignature(26, 1f);
        PureBigraph g0 = new PureBigraphGenerator().generate(randomSignature, 1, 30, 1f);
        BigraphGraphvizExporter.toPNG(g0,
                true,
                new File(TARGET_DUMP_PATH + "randomBigraph.png")
        );
        System.out.println(BigraphCanonicalForm.getInstance().bfcs(g0));
        StringBuilder values = new StringBuilder();
        for (int i = 1000; i < 10000; i += 100) {
            PureBigraph generate = new PureBigraphGenerator()
                    .setLinkStrategy(RandomBigraphGenerator.LinkStrategy.MAXIMAL_DEGREE_ASSORTATIVE)
                    .generate(randomSignature, 1, i, 1f);
//        O(k² c log c): k = num of vertices, c maximal degree of vertices
            Stopwatch stopwatch = Stopwatch.createStarted();
            String bfcs = BigraphCanonicalForm.getInstance().bfcs(generate);
            long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
//            System.out.println("Time taken: " + elapsed + ", nodes=" + generate.getNodes().size());
            values.append(elapsed).append(",");
        }
        if (values.charAt(values.length() - 1) == ',') {
            values.deleteCharAt(values.length() - 1);
        }
//        System.out.println(values.toString());

        Files.write(
//                                        Paths.get("/home/dominik/Documents/PhD/Papers/Concept/RandomBigraphGeneration/analysis/node_dissa.data"),
                Paths.get("/home/dominik/Documents/PhD/Papers/Concept/Canonical-Bigraphs/analysis/data/time.data"),
                values.toString().getBytes());
//        System.out.println(generate.getEdges().size());
//        System.out.println(bfcs);
    }

    @Test
    void simple_canonical_form_with_links_test() throws InvalidConnectionException, LinkTypeNotExistsException, IOException {
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
        String bfcs = BigraphCanonicalForm.getInstance().bfcs(l1);
        String bfcs2 = BigraphCanonicalForm.getInstance().bfcs(l2);
        System.out.println(bfcs);
        System.out.println(bfcs2);
        Assertions.assertEquals(bfcs, bfcs2);
    }

    /**
     * Example graph within the slides of Markus Biesinger. The BFCS is "A$BB$CD$E$FG$E#".
     *
     * @return an example graph
     * @throws ControlIsAtomicException
     * @see <a href="https://www.ke.tu-darmstadt.de/lehre/archiv/ws0809/ml-sem/slides/Biesinger_Markus.pdf">https://www.ke.tu-darmstadt.de/lehre/archiv/ws0809/ml-sem/slides/Biesinger_Markus.pdf</a>
     */
    public Bigraph createBiesingerSampleBigraph() throws ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild(signature.getControlByName("A"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("B"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("E"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("E")).goBack().goBack()
                .addChild(signature.getControlByName("B"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("D"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("G"))
                .addChild(signature.getControlByName("F"))
                .goBack()
                .addChild(signature.getControlByName("C"));


        return builder.createBigraph();
    }

    public Bigraph createSampleBigraphA1() throws ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild(signature.getControlByName("A"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("B"))
                .addChild(signature.getControlByName("B"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("C"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("H"))
                .addChild(signature.getControlByName("G"))
                .goBack()
                .addChild(signature.getControlByName("D"))
                .goBack()
                .addChild(signature.getControlByName("B"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("E"))
                .addChild(signature.getControlByName("C"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("H"))
                .addChild(signature.getControlByName("F"))
                .goBack()
                .goBack()
                .goBack()
        ;

        return builder.createBigraph();
    }

    public Bigraph createSampleBigraphA2() throws ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild(signature.getControlByName("A"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("B"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("C"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("F"))
                .addChild(signature.getControlByName("H"))
                .goBack()
                .addChild(signature.getControlByName("E"))
                .goBack()
                .addChild(signature.getControlByName("B"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("C"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("G"))
                .addChild(signature.getControlByName("H"))
                .goBack()
                .addChild(signature.getControlByName("D"))
                .goBack()
                .addChild(signature.getControlByName("B"))
                .goBack()
        ;

        return builder.createBigraph();
    }

    public Bigraph createSampleBigraphB1() throws ControlIsAtomicException, InvalidConnectionException, LinkTypeNotExistsException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        BigraphEntity.InnerName inner = builder.createInnerName("inner");
        builder.createRoot()
                .addChild(signature.getControlByName("A"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("B")).linkToInner(inner)
                .addChild(signature.getControlByName("C")).linkToInner(inner)
                .goBack()
                .addChild(signature.getControlByName("A"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("B"))
                .addChild(signature.getControlByName("C"))
                .goBack()
        ;
        return builder.closeAllInnerNames().createBigraph();
    }

    public Bigraph createSampleBigraphB2() throws ControlIsAtomicException, InvalidConnectionException, LinkTypeNotExistsException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        BigraphEntity.InnerName inner = builder.createInnerName("inner");
        builder.createRoot()
                .addChild(signature.getControlByName("A"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("B"))
                .addChild(signature.getControlByName("C"))
                .goBack()
                .addChild(signature.getControlByName("A"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("C")).linkToInner(inner)
                .addChild(signature.getControlByName("B")).linkToInner(inner)
                .goBack()
        ;
        return builder.closeAllInnerNames().createBigraph();
    }

    public Bigraph createSampleBigraph_with_Links() throws InvalidConnectionException, LinkTypeNotExistsException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);

        BigraphEntity.InnerName edge = b1.createInnerName("edge");
        BigraphEntity.InnerName edge2 = b1.createInnerName("edge2");
        BigraphEntity.OuterName o1 = b1.createOuterName("o1");
        BigraphEntity.InnerName x1 = b1.createInnerName("x1");

        b1.createRoot().addChild("R")
                .withNewHierarchy()
                .addChild("J").linkToInner(edge2)
                .addChild("A").linkToInner(edge2).linkToOuter(o1)
                .addChild("A")
                .addSite()
                .addChild("J").linkToInner(x1)
                .goBack()
                .addChild("R")
                .withNewHierarchy()
                .addChild("A").linkToInner(edge)
                .addChild("J").linkToInner(edge2)
//                .addChild("C")
        ;
        b1.closeInnerName(edge);
        b1.closeInnerName(edge2);
        return b1.createBigraph();
    }

    public Bigraph createSampleBigraph_with_Links_v2() throws InvalidConnectionException, LinkTypeNotExistsException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);

        BigraphEntity.InnerName edge = b1.createInnerName("e1");
        BigraphEntity.InnerName edge2 = b1.createInnerName("e2");
        BigraphEntity.OuterName o1 = b1.createOuterName("o1");
        BigraphEntity.InnerName x1 = b1.createInnerName("x1");

        b1.createRoot()
                .addChild("R")
                .withNewHierarchy()
                .addChild("A").linkToInner(edge)
                .addChild("J").linkToInner(edge2)
                .top()
                .addChild("R")
                .withNewHierarchy()
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

    public List<Bigraph> createSampleGraphs() {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> b1 = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> b2 = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> b3 = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> b4 = factory.createBigraphBuilder(signature);

        b1.createRoot()
                .addChild(signature.getControlByName("A"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("B")).withNewHierarchy()
                .addChild(signature.getControlByName("C")).goBack()
                .addChild(signature.getControlByName("B"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("D")).addChild(signature.getControlByName("C"));

        b2.createRoot().addChild(signature.getControlByName("A")).withNewHierarchy()
                .addChild(signature.getControlByName("B")).withNewHierarchy().addChild(signature.getControlByName("C"))
                .goBack()
                .addChild(signature.getControlByName("B")).withNewHierarchy()
                .addChild(signature.getControlByName("C")).addChild(signature.getControlByName("D"));

        b3.createRoot().addChild(signature.getControlByName("A")).withNewHierarchy()
                .addChild(signature.getControlByName("B")).withNewHierarchy()
                .addChild(signature.getControlByName("D")).addChild(signature.getControlByName("C"))
                .goBack()
                .addChild(signature.getControlByName("B")).withNewHierarchy().addChild(signature.getControlByName("C"))
        ;
        b4.createRoot().addChild(signature.getControlByName("A")).withNewHierarchy()
                .addChild(signature.getControlByName("B")).withNewHierarchy()
                .addChild(signature.getControlByName("C")).addChild(signature.getControlByName("D"))
                .goBack()
                .addChild(signature.getControlByName("B")).withNewHierarchy().addChild(signature.getControlByName("C"))
        ;

        return Lists.newArrayList(b1.createBigraph(), b2.createBigraph(), b3.createBigraph(), b4.createBigraph());
    }


    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("C")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("D")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("E")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("F")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("G")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("H")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("I")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("J")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("Q")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("R")).arity(FiniteOrdinal.ofInteger(1)).assign()
        ;

        return (S) defaultBuilder.create();
    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createRandomSignature(int n, float probOfPositiveArity) {
        DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> signatureBuilder = factory.createSignatureBuilder();

        char[] chars = IntStream.rangeClosed('A', 'Z')
                .mapToObj(c -> "" + (char) c).collect(Collectors.joining()).toCharArray();

        int floorNum = (int) Math.ceil(n * probOfPositiveArity);
        for (int i = 0; i < floorNum; i++) {
            signatureBuilder = (DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>>) signatureBuilder.newControl().identifier(StringTypedName.of(String.valueOf(chars[i]))).arity(FiniteOrdinal.ofInteger(1)).assign();
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
