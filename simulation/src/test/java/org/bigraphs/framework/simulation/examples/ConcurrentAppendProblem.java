/*
 * Copyright (c) 2020-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.examples;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.converter.bigrapher.BigrapherTransformator;
import org.bigraphs.framework.converter.dot.DOTReactionGraphExporter;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.analysis.ReactionGraphAnalysis;
import org.bigraphs.framework.simulation.encoding.BigraphCanonicalForm;
import org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Bigraph implementation of the concurrent append problem, adapted from Rensink's GROOVE paper.
 *
 * @author Dominik Grzelak
 * @see <a href="https://doi.org/10.1007/978-3-540-25959-6_40">Rensink, A. (2004). The GROOVE Simulator: A Tool for State Space Generation.</a>
 */
@Disabled
public class ConcurrentAppendProblem extends BaseExampleTestSupport {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/append/";
    private final static boolean AUTO_CLEAN_BEFORE = true;

    public ConcurrentAppendProblem() {
        super(TARGET_DUMP_PATH, AUTO_CLEAN_BEFORE);
    }

    @BeforeAll
    static void setUp() throws IOException {
        if (AUTO_CLEAN_BEFORE) {
            File dump = new File(TARGET_DUMP_PATH);
            dump.mkdirs();
            FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
            new File(TARGET_DUMP_PATH + "states/").mkdir();
        }
    }

    private DynamicSignature sig() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add("append", 1) // as much as we have "callers" (processes)
                .add("Root", 0)
                .add("list", 0)
                .add("this", 0) // as much as we have "callers" (processes)
                .add("thisRef", 1)
                .add("Node", 0)
                .add("void", 0)
                .add("val", 0)
                .add("i1", 0)
                .add("i2", 0)
                .add("i3", 0)
                .add("i4", 0)
                .add("i5", 0)
                .add("i6", 0)
                .add("next", 0)
        ;
        return defaultBuilder.create();
    }

    @Test
    void simulate() throws Exception {

        PureBigraph agent = createAgent(); //specify the number of processes here
        ReactionRule<PureBigraph> nextRR = nextRR();
        ReactionRule<PureBigraph> append = appendRR();
        ReactionRule<PureBigraph> returnRR = returnRR();

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(nextRR);
        reactiveSystem.addReactionRule(append);
        reactiveSystem.addReactionRule(returnRR);

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                setUpSimOpts());

        long start = System.nanoTime();

        modelChecker.execute();

        long diff = System.nanoTime() - start;
        System.out.println(diff);

        //states=51, transitions=80
        System.out.println("Edges: " + modelChecker.getReactionGraph().getGraph().edgeSet().size());
        System.out.println("Vertices: " + modelChecker.getReactionGraph().getGraph().vertexSet().size());

        ReactionGraphAnalysis<PureBigraph> analysis = ReactionGraphAnalysis.createInstance();
        List<ReactionGraphAnalysis.StateTrace<PureBigraph>> pathsToLeaves = analysis.findAllPathsInGraphToLeaves(modelChecker.getReactionGraph());
        System.out.println("Number of solutions: " + pathsToLeaves.size());

        DOTReactionGraphExporter exporter = new DOTReactionGraphExporter();
        String dotFile = exporter.toString(modelChecker.getReactionGraph());
        System.out.println(dotFile);
        exporter.toOutputStream(modelChecker.getReactionGraph(), new FileOutputStream(TARGET_DUMP_PATH + "reaction_graph.dot"));
    }

    @Test
    void export_to_bigrapher_specification() throws Exception {
        PureBigraph agent = createAgent(); //specify the number of processes here
        ReactionRule<PureBigraph> nextRR = nextRR();
        ReactionRule<PureBigraph> append = appendRR();
        ReactionRule<PureBigraph> returnRR = returnRR();
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(nextRR);
        reactiveSystem.addReactionRule(append);
        reactiveSystem.addReactionRule(returnRR);

        BigrapherTransformator encoder = new BigrapherTransformator();
        String export = encoder.toString(reactiveSystem);
        System.out.println(export);
    }

    @Test
    void simulate_single_step() throws Exception {
        PureBigraph a = loadBigraphFromFS("./src/test/resources/bigraphs/append/a_1.xmi");
        toPNG(a, "loaded_1", TARGET_DUMP_PATH);
        PureBigraph b = loadBigraphFromFS("./src/test/resources/bigraphs/append/a_2.xmi");
        toPNG(b, "loaded_2", TARGET_DUMP_PATH);

        String bfcs4 = BigraphCanonicalForm.createInstance().bfcs(a);
        System.out.println(bfcs4);
        String bfcs6 = BigraphCanonicalForm.createInstance().bfcs(b);
        System.out.println(bfcs6);

        ReactionRule<PureBigraph> nextRR = nextRR();
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(a, nextRR);
        Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
        while (iterator.hasNext()) {
            BigraphMatch<?> next = iterator.next();
            System.out.println("OK: " + next);
        }
    }

    private ModelCheckingOptions setUpSimOpts() {
        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(5000)
                        .setMaximumTime(-1)
                        .allowReducibleClasses(true)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(completePath.toUri()))
                        .setPrintCanonicalStateLabel(false)
                        .setFormatsEnabled(List.of(ModelCheckingOptions.ExportOptions.Format.PNG, ModelCheckingOptions.ExportOptions.Format.XMI))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .create()
                )
        ;
        return opts;
    }

    PureBigraph createAgent() throws Exception {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());

        BigraphEntity.InnerName tmpA1 = builder.createInner("tmpA1");
        BigraphEntity.InnerName tmpA2 = builder.createInner("tmpA2");
//        BigraphEntity.InnerName tmpA3 = builder.createInnerName("tmpA3");

        PureBigraphBuilder<DynamicSignature>.Hierarchy appendcontrol1 = builder.hierarchy("append");
        appendcontrol1
                .linkInner(tmpA1).child("val").down().child("i5").top();

        PureBigraphBuilder<DynamicSignature>.Hierarchy appendcontrol2 = builder.hierarchy("append");
        appendcontrol2
                .linkInner(tmpA2).child("val").down().child("i4").top();

//        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy appendcontrol3 = builder.hierarchy("append");
//        appendcontrol3
//                .linkToInner(tmpA3).child("val").down().child("i6").top();

        PureBigraphBuilder<DynamicSignature>.Hierarchy rootCell = builder.hierarchy("Root");
        rootCell
                .child("list").down().child("Node")
                .down().child("this").down()
                .child("thisRef").linkInner(tmpA1)
                .child("thisRef").linkInner(tmpA2)
//                .child("thisRef").linkToInner(tmpA3)
                .up()
                .child("val").down().child("i1").up()
                .child("next").down().child("Node").down().child("this")

                .child("val").down().child("i2").up()
                .child("next").down().child("Node").down().child("this")

                .child("val").down().child("i3").up()
                .top();

        builder.root()
                .child(rootCell)
                .child(appendcontrol1)
                .child(appendcontrol2)
//                .addChild(appendcontrol3)
        ;
        builder.closeInner();
        PureBigraph bigraph = builder.create();

//        BigraphFileModelManagement.exportAsInstanceModel(bigraph, System.out);
        toPNG(bigraph, "agent", TARGET_DUMP_PATH);

        return bigraph;
    }

    ReactionRule<PureBigraph> nextRR() throws Exception {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(sig());

        BigraphEntity.InnerName tmp0 = builderRedex.createInner("tmp");

        builderRedex.root()
                .child("this")
                .down().site().child("thisRef").linkInner(tmp0).up()
                .child("next").down().child("Node").down().site().child("this").down()
                .site().up()
                .top()
        ;
        //
        builderRedex.root()
                .child("append").linkInner(tmp0).down()
                .child("val").down().site().top()
        ;
        builderRedex.closeInner();

        BigraphEntity.InnerName tmp21 = builderReactum.createInner("tmp1");
        BigraphEntity.InnerName tmp22 = builderReactum.createInner("tmp2");
        builderReactum.root()
                .child("this").down().site().child("thisRef").linkInner(tmp22).up()
                .child("next").down().child("Node").down().site()
                .child("this").down().child("thisRef").linkInner(tmp21)
                .site()
                .top()
        ;
        //
        builderReactum.root()
                .child("append").linkInner(tmp22)
                .down().child("append").linkInner(tmp21)
                .down()
                .child("val").down().site().up()

        ;
        builderReactum.closeInner();

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();

        toPNG(redex, "next_1", TARGET_DUMP_PATH);
        toPNG(reactum, "next_2", TARGET_DUMP_PATH);

        return new ParametricReactionRule<>(redex, reactum).withLabel("next");
    }

    // create a new cell with the value
    // Only append a new value when last cell is reached, ie, without a next control
    ReactionRule<PureBigraph> appendRR() throws Exception {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(sig());

        BigraphEntity.InnerName tmp = builderRedex.createInner("tmp");
        builderRedex.root()
                .child("Node")
                .down()
                .child("this").down().child("thisRef").linkInner(tmp).site().up()
                .child("val").down().site().top()
        ;
        //
        builderRedex.root()
                .child("append").linkInner(tmp).down()
                .child("val").down().site().up()

        ;
        builderRedex.closeInner();

        builderReactum.root()
                .child("Node")
                .down()
                .child("this").down().site().up()
                .child("val").down().site().up()
                .child("next").down().child("Node").down().child("this").child("val").down().site().top();
        //
        builderReactum.root()
                .child("void")
        ;

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();

        toPNG(redex, "append_1", TARGET_DUMP_PATH);
        toPNG(reactum, "append_2", TARGET_DUMP_PATH);

        return new ParametricReactionRule<>(redex, reactum).withLabel("append");
    }

    ReactionRule<PureBigraph> returnRR() throws Exception {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(sig());

        BigraphEntity.InnerName tmp1 = builderRedex.createInner("tmp");
        builderRedex.root()
                .child("thisRef").linkInner(tmp1)
        ;
        //
        builderRedex.root()
                .child("append").linkInner(tmp1).down().child("void")

        ;
        builderRedex.closeInner();


        builderReactum.root()
        ;

        builderReactum.root()
                .child("void")
        ;
        builderReactum.closeInner();

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();


        toPNG(redex, "return_1", TARGET_DUMP_PATH);
        toPNG(reactum, "return_2", TARGET_DUMP_PATH);

        return new ParametricReactionRule<>(redex, reactum).withLabel("return");
    }

    private PureBigraph loadBigraphFromFS(String path) throws IOException {
        EPackage metaModel = createOrGetBigraphMetaModel(sig());
        List<EObject> eObjects = BigraphFileModelManagement.Load.bigraphInstanceModel(metaModel,
                path);

        PureBigraphBuilder<DynamicSignature> b = PureBigraphBuilder.create(sig(), metaModel, eObjects.get(0));
        PureBigraph bigraph = b.create();
        return bigraph;
    }
}
