package org.bigraphs.framework.visualization;

import org.bigraphs.framework.core.alg.generators.PureBigraphGenerator;
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
                .addControl("Printer", 2)
                .addControl("User", 1)
                .addControl("Room", 1)
                .addControl("Spool", 1)
                .addControl("Computer", 1)
                .addControl("Job", 0);

        return signatureBuilder.create();
    }

    public DefaultDynamicSignature createAlphabetSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .addControl("A", 5)
                .addControl("B", 5)
                .addControl("C", 5)
                .addControl("D", 5)
                .addControl("E", 5)
                .addControl("F", 5)
                .addControl("G", 5)
                .addControl("H", 5)
                .addControl("I", 5)
                .addControl("J", 5)
                .addControl("K", 5)
                .addControl("Q", 5)
                .addControl("R", 5)
        ;

        return defaultBuilder.create();
    }
}
