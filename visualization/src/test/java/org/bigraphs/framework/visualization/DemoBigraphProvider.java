package org.bigraphs.framework.visualization;

import org.bigraphs.framework.core.alg.generators.PureBigraphGenerator;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

public class DemoBigraphProvider {

    public PureBigraph getRandomBigraphMultipleRoots(int root, int nodes) {
        return new PureBigraphGenerator(createRandomSignature()).generate(root, nodes, 1.f);
    }

    public PureBigraph getRandomBigraphMultipleRoots(int root, int nodes, int sites) {
        return new PureBigraphGenerator(createRandomSignature()).generate(root, nodes, sites, 1.f);
    }

    public PureBigraph getRandomBigraphSingleRoot(int n, int s) {
        return new PureBigraphGenerator(createRandomSignature()).generate(1, n, s, 1.f);
    }

    public PureBigraph getRandomBigraphSingleRoot(int n, int s, DefaultDynamicSignature signature) {
        return new PureBigraphGenerator(signature).generate(1, n, s, 1.f);
    }

    public PureBigraph getRandomBigraphSingleRootOnlyOuternames(int n, int s) {
        return new PureBigraphGenerator(createRandomSignature()).generate(1, n, s, 1.f, 1.f, 0.f);
    }

    public PureBigraph getRandomBigraphSingleRootOnlyEdges(int n, int s) {
        return new PureBigraphGenerator(createRandomSignature()).generate(1, n, s, 1.f, 0.f, 1.f);
    }

    private DefaultDynamicSignature createRandomSignature() {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign();

        return signatureBuilder.create();
    }

    public DefaultDynamicSignature createAlphabetSignature() {
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
                .newControl().identifier(StringTypedName.of("K")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("Q")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("R")).arity(FiniteOrdinal.ofInteger(5)).assign()
        ;

        return defaultBuilder.create();
    }
}
