package de.tudresden.inf.st.bigraphs.rewriting.matching;

import com.google.common.collect.Lists;
import com.google.common.graph.Traverser;
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
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.visualization.GraphvizConverter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dominik Grzelak
 */
public class CanonicalFormRepresentation {
    private PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = AbstractBigraphFactory.createPureBigraphFactory();

    @Test
    void name() throws IOException {
        Bigraph sampleBigraph = createSampleBigraph();
        Bigraph sampleBigraph2 = createSampleBigraph2();
        GraphvizConverter.toPNG(sampleBigraph2,
                true,
                new File("sampleBigraph.png")
        );
        String bfcf = BigraphCanonicalForm.bfcf(sampleBigraph);
        String bfcf1 = BigraphCanonicalForm.bfcf(sampleBigraph);
        assertEquals(bfcf, bfcf1);
    }

    public Bigraph createSampleBigraph() throws ControlIsAtomicException {
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

    public Bigraph createSampleBigraph2() throws ControlIsAtomicException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        builder.createRoot()
                .addChild(signature.getControlByName("A"))
                .withNewHierarchy()
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
                .withNewHierarchy()
                .addChild(signature.getControlByName("C"))
                .addChild(signature.getControlByName("E"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("F"))
                .addChild(signature.getControlByName("H"))
                .goBack()
                .goBack()
                .addChild(signature.getControlByName("B"))
                .goBack()
        ;

        return builder.createBigraph();
    }


    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("C")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("D")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("E")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("F")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("G")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("H")).arity(FiniteOrdinal.ofInteger(1)).assign();

        return (S) defaultBuilder.create();
    }
}
