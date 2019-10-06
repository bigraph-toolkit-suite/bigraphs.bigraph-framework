package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.utils.PureBigraphGenerator;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BBigraph;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BRoot;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BigraphBaseModelFactory;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class RandomBigraphTests {
    private PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = AbstractBigraphFactory.createPureBigraphFactory();

    @Test
    void apiestt() {
        BBigraph bBigraph = BigraphBaseModelFactory.eINSTANCE.createBBigraph();
        BRoot bRoot = BigraphBaseModelFactory.eINSTANCE.createBRoot();
        bBigraph.getBRoots().add(bRoot);
        System.out.println(bRoot.getBBigraph().hashCode());
        System.out.println(bBigraph.getBRoots().size());
        BBigraph bBigraph2 = BigraphBaseModelFactory.eINSTANCE.createBBigraph();
        bBigraph2.getBRoots().add(bRoot);

        System.out.println(bRoot.getBBigraph().hashCode());
        System.out.println(bBigraph.getBRoots().size());
        System.out.println(bBigraph2.getBRoots().size());
    }

    @Test
    void name() {
        DefaultDynamicSignature exampleSignature = createExampleSignature();
        PureBigraph generated = new PureBigraphGenerator().generate(exampleSignature, 1, 10, 1.f);
    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> signatureBuilder = factory.createSignatureBuilder();
        signatureBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign();

        return (S) signatureBuilder.create();
    }
}
