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
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.converter.bigrapher.BigrapherTransformator;
import org.bigraphs.framework.converter.dot.DOTReactionGraphExporter;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.encoding.BigraphCanonicalForm;
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
 * Implementation of the concurrent append problem in bigraphs, adapted from the GROOVE paper by Rensink.
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

    private PureBigraph loadBigraphFromFS(String path) throws IOException {
        EPackage metaModel = createOrGetBigraphMetaModel(createSignature());
        List<EObject> eObjects = BigraphFileModelManagement.Load.bigraphInstanceModel(metaModel,
                path);

        PureBigraphBuilder<DynamicSignature> b = PureBigraphBuilder.create(createSignature(), metaModel, eObjects.get(0));
        PureBigraph bigraph = b.create();
        return bigraph;
    }

    @Test
    void simulate_single_step() throws Exception {
        PureBigraph a4 = loadBigraphFromFS("/home/dominik/git/BigraphFramework/simulation/src/test/resources/bigraphs/append/v2/a:6.xmi");
        eb(a4, "loaded_6");
        PureBigraph a6 = loadBigraphFromFS("/home/dominik/git/BigraphFramework/simulation/src/test/resources/bigraphs/append/v2/a:8.xmi");
        eb(a6, "loaded_8");

        String bfcs4 = BigraphCanonicalForm.createInstance().bfcs(a4);
        System.out.println(bfcs4);
        String bfcs6 = BigraphCanonicalForm.createInstance().bfcs(a6);
        System.out.println(bfcs6);
//        ReactionRule<PureBigraph> nextRR = nextRR();
//
//        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
//
//        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(bigraph, nextRR);
//        Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
//        while (iterator.hasNext()) {
//            BigraphMatch<?> next = iterator.next();
//            System.out.println("OK:" + next);
//        }
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

        ModelCheckingOptions modOpts = setUpSimOpts();
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                modOpts);
        long start = System.nanoTime();
        modelChecker.execute();
        long diff = System.nanoTime() - start;
        System.out.println(diff);

        //states=51, transitions=80
        System.out.println("Edges: " + modelChecker.getReactionGraph().getGraph().edgeSet().size());
        System.out.println("Vertices: " + modelChecker.getReactionGraph().getGraph().vertexSet().size());

        DOTReactionGraphExporter exporter = new DOTReactionGraphExporter();
        String dotFile = exporter.toString(modelChecker.getReactionGraph());
        System.out.println(dotFile);
        exporter.toOutputStream(modelChecker.getReactionGraph(), new FileOutputStream(TARGET_DUMP_PATH + "reaction_graph.dot"));

//        ReactionGraphAnalysis<PureBigraph> analysis = ReactionGraphAnalysis.createInstance();
//        List<ReactionGraphAnalysis.PathList<PureBigraph>> pathsToLeaves = analysis.findAllPathsInGraphToLeaves(modelChecker.getReactionGraph());
//        System.out.println(pathsToLeaves.size());
    }

    @Test
    void exportBigrapher() throws Exception {
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
                        .setFormatsEnabled(List.of(ModelCheckingOptions.ExportOptions.Format.PNG))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .create()
                )
        ;
        return opts;
    }

    PureBigraph createAgent() throws Exception {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(createSignature());

        BigraphEntity.InnerName tmpA1 = builder.createInner("tmpA1");
        BigraphEntity.InnerName tmpA2 = builder.createInner("tmpA2");
//        BigraphEntity.InnerName tmpA3 = builder.createInnerName("tmpA3");

        PureBigraphBuilder<DynamicSignature>.Hierarchy appendcontrol1 = builder.hierarchy("append");
        appendcontrol1
//                .linkToOuter(caller1)
                .linkInner(tmpA1).child("val").down().child("i5").top();

        PureBigraphBuilder<DynamicSignature>.Hierarchy appendcontrol2 = builder.hierarchy("append");
        appendcontrol2
//                .linkToOuter(caller2)
                .linkInner(tmpA2).child("val").down().child("i4").top();

//        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy appendcontrol3 = builder.hierarchy("append");
//        appendcontrol3
////                .linkToOuter(caller3)
//                .linkToInner(tmpA3).child("val").down().child("i6").top();

        PureBigraphBuilder<DynamicSignature>.Hierarchy rootCell = builder.hierarchy("Root")
//                .linkToOuter(caller1).linkToOuter(caller2)
                ;
        rootCell
                .child("list").down().child("Node")
                .down().child("this").down()
                .child("thisRef").linkInner(tmpA1)
                .child("thisRef").linkInner(tmpA2)
//                .child("thisRef").linkToInner(tmpA3)
                .up()
                .child("val").down().child("i1").up()
                .child("next").down().child("Node").down().child("this")
//                .down().child("thisRef").child("thisRef").up()
                .child("val").down().child("i2").up()
                .child("next").down().child("Node").down().child("this")
//                .down().addChild("thisRef").addChild("thisRef").up()
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
        eb(bigraph, "agent");
        return bigraph;
    }

    ReactionRule<PureBigraph> nextRR() throws Exception {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(createSignature());

        BigraphEntity.InnerName tmp0 = builderRedex.createInner("tmp");
//        BigraphEntity.OuterName anyRef = builderRedex.createOuterName("anyRef");
//        BigraphEntity.OuterName openRef = builderRedex.createOuterName("openRef");
        builderRedex.root()
                .child("this")
                .down().site().child("thisRef").linkInner(tmp0).up()
//                .site()
//                .addChild("val").down().site().top()
                .child("next").down().child("Node").down().site().child("this").down()
//                .addChild("thisRef")
                .site().up()
                .top()
        ;
        //
        builderRedex.root()
                .child("append").linkInner(tmp0).down()
                .child("val").down().site().top()
        ;
        builderRedex.closeInner();

//        BigraphEntity.OuterName anyRef2 = builderReactum.createOuterName("anyRef");
//        BigraphEntity.OuterName openRef2 = builderReactum.createOuterName("openRef");
        BigraphEntity.InnerName tmp21 = builderReactum.createInner("tmp1");
        BigraphEntity.InnerName tmp22 = builderReactum.createInner("tmp2");
        builderReactum.root()
                .child("this").down().site().child("thisRef").linkInner(tmp22).up()
//                .addChild("val").down().site().top()
//                .site()
                .child("next").down().child("Node").down().site()
                .child("this").down().child("thisRef").linkInner(tmp21)
                .site()
                .top()
        ;
        //
        builderReactum.root()
//                .addChild("append", "caller").linkToInner(tmp22)
//                .down().addChild("appendcontrol", "caller").linkToInner(tmp21)
                .child("append").linkInner(tmp22)
                .down().child("append").linkInner(tmp21)
                .down()
                .child("val").down().site().up()

        ;
        builderReactum.closeInner();

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        eb(redex, "next_1");
        eb(reactum, "next_2");

//        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
//        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
//        Bigraph encodedRedex = encoder.encode(redex);
//        Bigraph encodedReactum = encoder.encode(reactum, encodedRedex.getSignature());
//        RewritingRule rewritingRule = new RewritingRule(encodedRedex, encodedReactum, 0, 1, 2, 3, 4);

        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("next");
        return rr;
    }

    // create a new cell with the value
    // Only append a new value when last cell is reached, ie, without a next control
    ReactionRule<PureBigraph> appendRR() throws Exception {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(createSignature());

//        BigraphEntity.OuterName thisRefAny = builderRedex.createOuterName("thisRefAny");
//        BigraphEntity.OuterName thisRefA1 = builderRedex.createOuterName("thisRefA1");
        BigraphEntity.InnerName tmp = builderRedex.createInner("tmp");
        builderRedex.root()
                .child("Node")
                .down()
                .child("this").down().child("thisRef").linkInner(tmp).site().up()
                .child("val").down().site().top()
        ;
        //
        builderRedex.root()
//                .addChild("appendcontrol", "caller").linkToInner(tmp).down()
                .child("append").linkInner(tmp).down()
                .child("val").down().site().up()

        ;
        builderRedex.closeInner();

//        BigraphEntity.OuterName thisRefRAny = builderReactum.createOuterName("thisRefAny");
//        BigraphEntity.OuterName thisRefRA1 = builderReactum.createOuterName("thisRefA1");
//        BigraphEntity.InnerName tmp1 = builderReactum.createInnerName("tmp");
        builderReactum.root()
                .child("Node")
                .down()
                .child("this").down().site().up() //.addChild("thisRef")
                .child("val").down().site().up()
                .child("next").down().child("Node").down().child("this").child("val").down().site().top();
        //
        builderReactum.root()
//                .addChild("void", "caller")
                .child("void")
        ;

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();
        eb(redex, "append_1");
        eb(reactum, "append_2");

//        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
//        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
//        Bigraph encodedRedex = encoder.encode(redex);
//        Bigraph encodedReactum = encoder.encode(reactum, encodedRedex.getSignature());
//        RewritingRule rewritingRule = new RewritingRule(encodedRedex, encodedReactum, 0, 1, 2);

        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("append");
        return rr;
    }

    //if values are the same...
    ReactionRule<PureBigraph> stopRR() throws Exception {
        return null;
    }

    ReactionRule<PureBigraph> returnRR() throws Exception {
        PureBigraphBuilder<DynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DynamicSignature> builderReactum = pureBuilder(createSignature());

        BigraphEntity.InnerName tmp1 = builderRedex.createInner("tmp");
        builderRedex.root()
                .child("thisRef").linkInner(tmp1)
        ;
        //
        builderRedex.root()
//                .addChild("append", "caller").linkToInner(tmp1).down().addChild("void", "caller")
                .child("append").linkInner(tmp1).down().child("void")

        ;
        builderRedex.closeInner();


        builderReactum.root()
//                .addChild("thisRef")
        ;
        //
        builderReactum.root()
//                .addChild("void", "caller")
                .child("void")
        ;
        builderReactum.closeInner();

        PureBigraph redex = builderRedex.create();
        PureBigraph reactum = builderReactum.create();


        eb(redex, "return_1");
        eb(reactum, "return_2");
//        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
//        Bigraph encodedRedex = encoder.encode(redex);
//        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
//        PureBigraph decode = decoder.decode(encodedRedex, redex.getSignature());
//        eb(decode, "return_decoded_1");
//        RewritingRule rewritingRule = new RewritingRule(encodedRedex, encodedRedex, 0, 1, 2);
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum).withLabel("return");
        return rr;
    }

    private DynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
//                .addControl("appendcontrol", 1)
                .add("append", 1)
                .newControl().identifier(StringTypedName.of("Root")).arity(FiniteOrdinal.ofInteger(0)).assign() // as much as we callers have
                .newControl().identifier(StringTypedName.of("list")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("this")).arity(FiniteOrdinal.ofInteger(0)).assign() // as much as we callers have
                .newControl().identifier(StringTypedName.of("thisRef")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Node")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("void")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("val")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("i1")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("i2")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("i3")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("i4")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("i5")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("i6")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("next")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;
        return defaultBuilder.create();
    }
}
