package org.bigraphs.framework.simulation.examples;

import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphDecoder;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphEncoder;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.encoding.BigraphCanonicalForm;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import org.apache.commons.io.FileUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

/**
 * The concurrent append problem from the Groove Rensink paper in bigraphs
 *
 * @author Dominik Grzelak
 */
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

        PureBigraphBuilder<DefaultDynamicSignature> b = PureBigraphBuilder.create(createSignature(), metaModel, eObjects.get(0));
        PureBigraph bigraph = b.createBigraph();
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

        PureBigraph agent = createAgent();
        ReactionRule<PureBigraph> nextRR = nextRR();
        ReactionRule<PureBigraph> append = appendRR();
        ReactionRule<PureBigraph> returnRR = returnRR();
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(nextRR);
        reactiveSystem.addReactionRule(append);
        reactiveSystem.addReactionRule(returnRR);


        //TODO
        //attributes would make the "value check" simpler

        ModelCheckingOptions modOpts = setUpSimOpts();
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                modOpts);
//        modelChecker.setReactiveSystemListener(this);
        long start= System.nanoTime();
        modelChecker.execute();
        long diff = System.nanoTime() - start;
        System.out.println(diff);


        //states=51, transitions=80
        System.out.println("Edges: " + modelChecker.getReactionGraph().getGraph().edgeSet().size());
        System.out.println("Vertices: " + modelChecker.getReactionGraph().getGraph().vertexSet().size());

//        ReactionGraphAnalysis<PureBigraph> analysis = ReactionGraphAnalysis.createInstance();
//        List<ReactionGraphAnalysis.PathList<PureBigraph>> pathsToLeaves = analysis.findAllPathsInGraphToLeaves(modelChecker.getReactionGraph());
//        System.out.println(pathsToLeaves.size());

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
//                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                                .create()
                )
        ;
        return opts;
    }

    PureBigraph createAgent() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());

//        BigraphEntity.OuterName caller1 = builder.createOuterName("caller1");
//        BigraphEntity.OuterName caller2 = builder.createOuterName("caller2");
//        BigraphEntity.OuterName caller3 = builder.createOuterName("caller3");

        BigraphEntity.InnerName tmpA1 = builder.createInnerName("tmpA1");
        BigraphEntity.InnerName tmpA2 = builder.createInnerName("tmpA2");
        BigraphEntity.InnerName tmpA3 = builder.createInnerName("tmpA3");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy appendcontrol1 = builder.hierarchy("append");
        appendcontrol1
//                .linkToOuter(caller1)
                .linkToInner(tmpA1).addChild("val").down().addChild("i5").top();

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy appendcontrol2 = builder.hierarchy("append");
        appendcontrol2
//                .linkToOuter(caller2)
                .linkToInner(tmpA2).addChild("val").down().addChild("i4").top();

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy appendcontrol3 = builder.hierarchy("append");
        appendcontrol3
//                .linkToOuter(caller3)
                .linkToInner(tmpA3).addChild("val").down().addChild("i6").top();

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy rootCell = builder.hierarchy("Root")
//                .linkToOuter(caller1).linkToOuter(caller2)
                ;
        rootCell
                .addChild("list").down().addChild("Node")
                .down().addChild("this").down()
                .addChild("thisRef").linkToInner(tmpA1)
                .addChild("thisRef").linkToInner(tmpA2)
                .addChild("thisRef").linkToInner(tmpA3)
                .up()
                .addChild("val").down().addChild("i1").up()
                .addChild("next").down().addChild("Node").down().addChild("this")
//                .down().addChild("thisRef").addChild("thisRef").up()
                .addChild("val").down().addChild("i2").up()
                .addChild("next").down().addChild("Node").down().addChild("this")
//                .down().addChild("thisRef").addChild("thisRef").up()
                .addChild("val").down().addChild("i3").up()
                .top();

        builder.createRoot()
                .addChild(rootCell)
                .addChild(appendcontrol1)
                .addChild(appendcontrol2)
                .addChild(appendcontrol3)
        ;
        builder.closeAllInnerNames();
        PureBigraph bigraph = builder.createBigraph();
//        BigraphFileModelManagement.exportAsInstanceModel(bigraph, System.out);
        eb(bigraph, "agent");
        return bigraph;
    }

    ReactionRule<PureBigraph> nextRR() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builderReactum = pureBuilder(createSignature());

        BigraphEntity.InnerName tmp0 = builderRedex.createInnerName("tmp");
//        BigraphEntity.OuterName anyRef = builderRedex.createOuterName("anyRef");
//        BigraphEntity.OuterName openRef = builderRedex.createOuterName("openRef");
        builderRedex.createRoot()
                .addChild("this")
                .down().addSite().addChild("thisRef").linkToInner(tmp0).up()
//                .addSite()
//                .addChild("val").down().addSite().top()
                .addChild("next").down().addChild("Node").down().addSite().addChild("this").down()
//                .addChild("thisRef")
                .addSite().up()
                .top()
        ;
        //
        builderRedex.createRoot()
//                .addChild("appendcontrol", "caller").linkToInner(tmp0).down()
                .addChild("append").linkToInner(tmp0).down()
                .addChild("val").down().addSite().top()
        ;
        builderRedex.closeAllInnerNames();

//        BigraphEntity.OuterName anyRef2 = builderReactum.createOuterName("anyRef");
//        BigraphEntity.OuterName openRef2 = builderReactum.createOuterName("openRef");
        BigraphEntity.InnerName tmp21 = builderReactum.createInnerName("tmp1");
        BigraphEntity.InnerName tmp22 = builderReactum.createInnerName("tmp2");
        builderReactum.createRoot()
                .addChild("this").down().addSite().addChild("thisRef").linkToInner(tmp22).up()
//                .addChild("val").down().addSite().top()
//                .addSite()
                .addChild("next").down().addChild("Node").down().addSite()
                .addChild("this").down().addChild("thisRef").linkToInner(tmp21)
                .addSite()
                .top()
        ;
        //
        builderReactum.createRoot()
//                .addChild("append", "caller").linkToInner(tmp22)
//                .down().addChild("appendcontrol", "caller").linkToInner(tmp21)
                .addChild("append").linkToInner(tmp22)
                .down().addChild("append").linkToInner(tmp21)
                .down()
                .addChild("val").down().addSite().up()

        ;
        builderReactum.closeAllInnerNames();

        PureBigraph redex = builderRedex.createBigraph();
        PureBigraph reactum = builderReactum.createBigraph();
        eb(redex, "next_1");
        eb(reactum, "next_2");

//        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
//        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
//        Bigraph encodedRedex = encoder.encode(redex);
//        Bigraph encodedReactum = encoder.encode(reactum, encodedRedex.getSignature());
//        RewritingRule rewritingRule = new RewritingRule(encodedRedex, encodedReactum, 0, 1, 2, 3, 4);

        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    // create a new cell with the value
    // Only append a new value when last cell is reached, ie, without a next control
    ReactionRule<PureBigraph> appendRR() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builderReactum = pureBuilder(createSignature());

//        BigraphEntity.OuterName thisRefAny = builderRedex.createOuterName("thisRefAny");
//        BigraphEntity.OuterName thisRefA1 = builderRedex.createOuterName("thisRefA1");
        BigraphEntity.InnerName tmp = builderRedex.createInnerName("tmp");
        builderRedex.createRoot()
                .addChild("Node")
                .down()
                .addChild("this").down().addChild("thisRef").linkToInner(tmp).addSite().up()
                .addChild("val").down().addSite().top()
        ;
        //
        builderRedex.createRoot()
//                .addChild("appendcontrol", "caller").linkToInner(tmp).down()
                .addChild("append").linkToInner(tmp).down()
                .addChild("val").down().addSite().up()

        ;
        builderRedex.closeAllInnerNames();

//        BigraphEntity.OuterName thisRefRAny = builderReactum.createOuterName("thisRefAny");
//        BigraphEntity.OuterName thisRefRA1 = builderReactum.createOuterName("thisRefA1");
//        BigraphEntity.InnerName tmp1 = builderReactum.createInnerName("tmp");
        builderReactum.createRoot()
                .addChild("Node")
                .down()
                .addChild("this").down().addSite().up() //.addChild("thisRef")
                .addChild("val").down().addSite().up()
                .addChild("next").down().addChild("Node").down().addChild("this").addChild("val").down().addSite().top();
        //
        builderReactum.createRoot()
//                .addChild("void", "caller")
                .addChild("void")
        ;

        PureBigraph redex = builderRedex.createBigraph();
        PureBigraph reactum = builderReactum.createBigraph();
        eb(redex, "append_1");
        eb(reactum, "append_2");

//        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
//        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
//        Bigraph encodedRedex = encoder.encode(redex);
//        Bigraph encodedReactum = encoder.encode(reactum, encodedRedex.getSignature());
//        RewritingRule rewritingRule = new RewritingRule(encodedRedex, encodedReactum, 0, 1, 2);

        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    //if values are the same...
    ReactionRule<PureBigraph> stopRR() throws Exception {
        return null;
    }

    ReactionRule<PureBigraph> returnRR() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builderReactum = pureBuilder(createSignature());

        BigraphEntity.InnerName tmp1 = builderRedex.createInnerName("tmp");
        builderRedex.createRoot()
                .addChild("thisRef").linkToInner(tmp1)
        ;
        //
        builderRedex.createRoot()
//                .addChild("append", "caller").linkToInner(tmp1).down().addChild("void", "caller")
                .addChild("append").linkToInner(tmp1).down().addChild("void")

        ;
        builderRedex.closeAllInnerNames();


        builderReactum.createRoot()
//                .addChild("thisRef")
        ;
        //
        builderReactum.createRoot()
//                .addChild("void", "caller")
                .addChild("void")
        ;
        builderReactum.closeAllInnerNames();

        PureBigraph redex = builderRedex.createBigraph();
        PureBigraph reactum = builderReactum.createBigraph();
        eb(redex, "return_1");
        eb(reactum, "return_2");
        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
        Bigraph encodedRedex = encoder.encode(redex);
        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
        PureBigraph decode = decoder.decode(encodedRedex, redex.getSignature());
        eb(decode, "return_decoded_1");
//        RewritingRule rewritingRule = new RewritingRule(encodedRedex, encodedRedex, 0, 1, 2);
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private DefaultDynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
//                .addControl("appendcontrol", 1)
                .addControl("append", 1)
                .newControl().identifier(StringTypedName.of("Root")).arity(FiniteOrdinal.ofInteger(0)).assign() // as much as we callers have
                .newControl().identifier(StringTypedName.of("list")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("this")).arity(FiniteOrdinal.ofInteger(0)).assign() // as much as we callers have
//                .newControl().identifier(StringTypedName.of("thisRefCurrent")).arity(FiniteOrdinal.ofInteger(1)).assign()
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

//    private DefaultDynamicSignature createSignature() {
//        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
//        defaultBuilder
//                .addControl("appendcontrol", 2)
//                .addControl("append", 2)
//                .newControl().identifier(StringTypedName.of("Root")).arity(FiniteOrdinal.ofInteger(2)).assign() // as much as we callers have
//                .newControl().identifier(StringTypedName.of("list")).arity(FiniteOrdinal.ofInteger(0)).assign()
//                .newControl().identifier(StringTypedName.of("this")).arity(FiniteOrdinal.ofInteger(0)).assign() // as much as we callers have
////                .newControl().identifier(StringTypedName.of("thisRefCurrent")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("thisRef")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("Node")).arity(FiniteOrdinal.ofInteger(0)).assign()
//                .newControl().identifier(StringTypedName.of("void")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("val")).arity(FiniteOrdinal.ofInteger(0)).assign()
//                .newControl().identifier(StringTypedName.of("i1")).arity(FiniteOrdinal.ofInteger(0)).assign()
//                .newControl().identifier(StringTypedName.of("i2")).arity(FiniteOrdinal.ofInteger(0)).assign()
//                .newControl().identifier(StringTypedName.of("i3")).arity(FiniteOrdinal.ofInteger(0)).assign()
//                .newControl().identifier(StringTypedName.of("i4")).arity(FiniteOrdinal.ofInteger(0)).assign()
//                .newControl().identifier(StringTypedName.of("i5")).arity(FiniteOrdinal.ofInteger(0)).assign()
//                .newControl().identifier(StringTypedName.of("next")).arity(FiniteOrdinal.ofInteger(0)).assign()
//        ;
//        return defaultBuilder.create();
//    }
}
