/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.core.random;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.alg.generators.PureBigraphGenerator;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.MutableBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraphComposite;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class RandomBigraphUnitTests {
    private final static String TARGET_TEST_PATH = "src/test/resources/dump/exported-models/";

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
        end();
    }

    @Test
    void name() {
        DynamicSignature exampleSignature = createExampleSignature();
        PureBigraph generated = new PureBigraphGenerator(exampleSignature).generate(1, 10, 1.f);
    }

    @Test
    void ecoremodel_composition() throws InvalidConnectionException, TypeNotExistsException, IOException, IncompatibleSignatureException, IncompatibleInterfaceException {
        DynamicSignature exampleSignature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(exampleSignature);
        PureBigraphGenerator pureBigraphGenerator = pureRandomBuilder(exampleSignature);
        PureBigraph randomBigraph = pureBigraphGenerator.generate(1, 10, 0f);

        BigraphEntity.OuterName network = builder.createOuter("network");
        BigraphEntity.InnerName login = builder.createInner("login");
        builder.root()
                .child("User").linkOuter(network)
                .child("User").linkInner(login).down().site();
        PureBigraph left = builder.create();
        //TODO: add to docu: spawnNewOne() - important for ecore operations
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(exampleSignature);
//        PureBigraphBuilder<DefaultDynamicSignature> builder2 = builder.spawnNewOne();
        BigraphEntity.OuterName login2 = builder2.createOuter("login");
        BigraphEntity.InnerName login2User = builder2.createInner("nextLogin");
        BigraphEntity.InnerName abc = builder2.createInner("abc");
        BigraphEntity.OuterName xyz = builder2.createOuter("network");//xyz
        builder2.root()
                .child("User").linkOuter(login2)
                .child("User").linkInner(login2User);
        builder2.linkInnerToOuter(abc, xyz);

        PureBigraph right = builder2.create();


        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) left, new FileOutputStream("src/test/resources/dump/exported-models/random_left_03_before.xmi"));
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) right, new FileOutputStream("src/test/resources/dump/exported-models/random_right_03_before.xmi"));
        PureBigraphComposite<DynamicSignature> comp = new PureBigraphComposite<>(left);
        Assertions.assertThrows(IncompatibleInterfaceException.class, () -> {
            BigraphComposite<DynamicSignature> result = comp.compose(right);
        });
//        BigraphComposite<DefaultDynamicSignature> result = comp.parallelProductV2(right);
//        BigraphComposite<DefaultDynamicSignature> result = comp.parallelProduct(right);
//        BigraphComposite<DefaultDynamicSignature> juxtapose = ((PureBigraphComposite) result).juxtapose(randomBigraph);
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) result.getOuterBigraph(), new FileOutputStream("src/test/resources/dump/exported-models/random_comp_03_after.xmi"));
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) juxtapose.getOuterBigraph(), new FileOutputStream("src/test/resources/dump/exported-models/random_juxta_03_after.xmi"));
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) left, new FileOutputStream("src/test/resources/dump/exported-models/random_left_03_after.xmi"));
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) right, new FileOutputStream("src/test/resources/dump/exported-models/random_right_03_after.xmi"));

//        Assertions.assertNotNull(result);
    }

    @Test
    void performance() throws IOException, CloneNotSupportedException, IncompatibleSignatureException, IncompatibleInterfaceException {
        DynamicSignature exampleSignature = createExampleSignature();
        List<Double> resultsTime = new ArrayList<Double>();
        List<Double> resultsTimeCloneOp = new ArrayList<Double>();

        for (int i = 1000; i < 10000; i += 1000) {
//        for (int i = 10; i < 11; i += 1) {
            PureBigraph generated = new PureBigraphGenerator(exampleSignature).generate(1, i, 0.5f, 0f, 1f);
            MutableBuilder<DynamicSignature> mutableBuilder = new MutableBuilder<>(exampleSignature, generated.getMetaModel(), generated.getInstanceModel());
            Integer o = (Integer) mutableBuilder.availableNodes().keySet()
                    .stream()
                    .map(x -> Integer.parseInt(((String) x).replace("v", "")))
                    .map(x -> (Integer) x)
                    .max(Comparator.comparing(Function.identity()))
                    .get();
            System.out.println(o);
            String key = "v" + o;
            BigraphEntity.NodeEntity nodeEntity = mutableBuilder.availableNodes().get(key);
            BigraphEntity newSite = mutableBuilder.createNewSite(0);
            mutableBuilder.setParentOfNode(newSite, nodeEntity); // this notifies the EContentAdapter
            mutableBuilder.availableSites().put(0, (BigraphEntity.SiteEntity) newSite);
            PureBigraph bigraph = mutableBuilder.create();
            Assertions.assertNotNull(bigraph);
            long tclone1 = System.nanoTime();
            EcoreBigraph.Stub clone = new EcoreBigraph.Stub(bigraph).clone();
            PureBigraph cloned = PureBigraphBuilder.create(exampleSignature, clone.getMetaModel(), clone.getInstanceModel()).create();
            long tclone2 = System.nanoTime();
            double secsClone = (tclone2 - tclone1) * 1.f / 1e+6;
            resultsTimeCloneOp.add(secsClone);
            Assertions.assertNotNull(cloned);
            PureBigraphComposite<DynamicSignature> comp = new PureBigraphComposite<>(bigraph);
//            BigraphFileModelManagement.exportAsInstanceModel(comp.getOuterBigraph(), new FileOutputStream(TARGET_TEST_PATH + "random_left_01.xmi"));
            long l = System.nanoTime();
            BigraphComposite<DynamicSignature> result = comp.compose(cloned);
//            BigraphComposite<DefaultDynamicSignature> result = comp.compose(cloned);
            long l2 = System.nanoTime();
//            double secs = (l2 - l) * 1.f / 1e+9;
            double millisecs = (l2 - l) * 1.f / 1e+6;
//            System.out.println("Time: " + secs + " / ns " + (l2 - l));
            resultsTime.add(millisecs);
        }
        System.out.println(resultsTime);
        double total = resultsTime.stream().reduce(0d, (a, b) -> a + b).doubleValue();
        System.out.println("Avg: " + (total / resultsTime.size() - 1));
        System.out.println(resultsTimeCloneOp);
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) result, new FileOutputStream(TARGET_TEST_PATH + "random_compose_result.xmi"));
    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder
                .add("Printer", 2)
                .add("User", 1)
                .add("Room", 1)
                .add("Spool", 1)
                .add("Computer", 1)
                .add("Job", 0);

        return (S) signatureBuilder.create();
    }
}
