package de.tudresden.inf.st.bigraphs.rewriting.matching;

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
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.rewriting.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.visualization.GraphvizConverter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dominik Grzelak
 */
public class CanonicalFormRepresentation {
    private PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = AbstractBigraphFactory.createPureBigraphFactory();

    @Test
    void simple_canonical_form_test() throws IOException, InvalidConnectionException, LinkTypeNotExistsException {

        Bigraph biesingerSampleBigraph = createBiesingerSampleBigraph();
        String bfcs = BigraphCanonicalForm.getInstance().bfcs(biesingerSampleBigraph);
        assertEquals(bfcs, "r0$" + "A$BB$CD$E$FG$E#");
        System.out.println(bfcs);
        GraphvizConverter.toPNG(biesingerSampleBigraph,
                true,
                new File("sampleBigraph_biesinger.png")
        );

        AtomicInteger cnt2 = new AtomicInteger(0);
        long count2 = Stream.of(createSampleBigraphB1(), createSampleBigraphB2())
                .peek(x -> {
                    try {
                        GraphvizConverter.toPNG(x,
                                true,
                                new File("sampleBigraph2_" + cnt2.incrementAndGet() + ".png")
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
                        GraphvizConverter.toPNG(x,
                                true,
                                new File("sampleBigraph0_" + cnt0.incrementAndGet() + ".png")
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
                        GraphvizConverter.toPNG(x,
                                true,
                                new File("sampleBigraph1_" + cnt1.incrementAndGet() + ".png")
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
    void simple_canonical_form_with_links_test() throws InvalidConnectionException, LinkTypeNotExistsException, IOException {
        Bigraph l1 = createSampleBigraph_with_Links();
        GraphvizConverter.toPNG(l1,
                true,
                new File("sampleBigraph_L_1.png")
        );
        String bfcs = BigraphCanonicalForm.getInstance().bfcs(l1);
        System.out.println(bfcs);
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
                .addChild(signature.getControlByName("B")).connectNodeToInnerName(inner)
                .addChild(signature.getControlByName("C")).connectNodeToInnerName(inner)
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
                .addChild(signature.getControlByName("C")).connectNodeToInnerName(inner)
                .addChild(signature.getControlByName("B")).connectNodeToInnerName(inner)
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

        b1.createRoot().addChild("R")
                .withNewHierarchy()
                .addChild("J").connectNodeToInnerName(edge2)
                .addChild("A").connectNodeToInnerName(edge2).connectNodeToOuterName(o1)
                .addChild("A")
                .goBack()
                .addChild("R")
                .withNewHierarchy()
                .addChild("A").connectNodeToInnerName(edge)
                .addChild("J").connectNodeToInnerName(edge2)
//                .addChild("C")
        ;
        b1.closeAllInnerNames();
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
}
