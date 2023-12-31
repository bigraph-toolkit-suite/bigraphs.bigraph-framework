package org.bigraphs.framework.simulation.examples;

import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

/**
 * @author Dominik Grzelak
 */
public class ContextAwarePrinting extends BaseExampleTestSupport {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/cas-printing/";
    private final static boolean AUTO_CLEAN_BEFORE = true;

    public ContextAwarePrinting() {
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

    @Test
    void name() throws Exception {
        DynamicSignatureBuilder assign = pureSignatureBuilder()
                .newControl("loc", 1).assign()
                .newControl("in", 0).assign() // input node
                .newControl("out", 0).assign() // output node
                .newControl("f", 0).assign() // controls find-all query
                .newControl("f'", 1).assign() // answer node
                .newControl("g", 0).assign() // dummy
                .newControl("s", 0).assign() // collect searched nodes
//                .newControl("prt", 1).assign()
//                .newControl("pcl", 0).assign()
//                .newControl("raw", 0).assign()
//                .newControl("dat", 1).assign()
                .newControl("dev", 1).assign();

        DefaultDynamicSignature defaultDynamicSignature = assign.create();

        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(defaultDynamicSignature);

        //loc(loc(loc(loc(dev1) | loc(dev2 | dev3))) | loc() | loc(dev 4))
        BigraphEntity.OuterName z = builder.createOuterName("z");
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy first = builder.hierarchy("loc").linkToOuter(z)
                .addChild("loc", "a")
                .down()
                .addChild("loc", "b").down().addChild("dev", "1").up()
                .addChild("loc", "c").down().addChild("dev", "2").addChild("dev", "3").up();

        builder.createRoot()
                .addChild("in").down().addChild("f").up()
                .addChild("out")
                .addChild("loc", "top")
                .down()
                .addChild(first.top())
                .addChild("loc", "d")
                .addChild("loc", "e").down().addChild("dev", "4").up();
        PureBigraph agent = builder.createBigraph();


        ParametricReactionRule<PureBigraph> rr0 = createReactionRule0(defaultDynamicSignature);
        ParametricReactionRule<PureBigraph> rr1 = initialization_Rule(defaultDynamicSignature);
        ParametricReactionRule<PureBigraph> rrCollectDev = collectDevices_Rule(defaultDynamicSignature);
//        BigraphFileModelManagement.exportAsInstanceModel(bigraph, System.out);

        eb(agent, "plato_tree");
        eb(rr0.getRedex(), "rr0-redex");
        eb(rr0.getReactum(), "rr0-reactum");
        eb(rr1.getRedex(), "rr1-redex");
        eb(rr1.getReactum(), "rr1-reactum");
        eb(rrCollectDev.getReactum(), "rr2-redex");
        eb(rrCollectDev.getReactum(), "rr2-reactum");

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
//        reactiveSystem.addReactionRule(rr0);
        reactiveSystem.addReactionRule(rr1);
//        reactiveSystem.addReactionRule(rrCollectDev);
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(100)
                        .setMaximumTime(30)
                        .allowReducibleClasses(false)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(TARGET_DUMP_PATH + "transition_graph_agent.png"))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .setPrintCanonicalStateLabel(true)
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.execute();

    }

    private static ParametricReactionRule<PureBigraph> createReactionRule0(DefaultDynamicSignature signature) throws InvalidConnectionException, IOException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builderRedex = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builderReactum = pureBuilder(signature);

        builderRedex.createRoot()
                .addChild("loc", "top").down().addSite()
//                .addSite()
        ;
        PureBigraph redex = builderRedex.createBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex, System.out);

        builderReactum.createRoot()
//                .addSite()
                .addChild("loc", "top").down().addSite().top()
                .addChild("in").down().addChild("f").top()
                .addChild("out");
        PureBigraph reactum = builderReactum.createBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum, System.out);
        ParametricReactionRule<PureBigraph> pureBigraphParametricReactionRule = new ParametricReactionRule<>(redex, reactum);
        return pureBigraphParametricReactionRule;
    }


    private static ParametricReactionRule<PureBigraph> initialization_Rule(DefaultDynamicSignature signature) throws InvalidConnectionException, IOException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builderRedex = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builderReactum = pureBuilder(signature);

        builderRedex.createRoot()
                .addChild("in").down().addChild("f").top()
                .addChild("loc", "top").down().addSite().addChild("loc", "x").down().addSite().top()
                .addChild("out");
        PureBigraph redex = builderRedex.createBigraph();

        builderReactum.createRoot()
                .addChild("in").down().addChild("g").top()
                .addChild("loc", "top").down().addSite().addChild("loc", "x").down().addChild("f").addChild("s").addSite().top()
                .addChild("out");
        PureBigraph reactum = builderReactum.createBigraph();

        ParametricReactionRule<PureBigraph> pureBigraphParametricReactionRule = new ParametricReactionRule<>(redex, reactum);
        return pureBigraphParametricReactionRule;
    }

    /**
     * Collects devices under a location, and adds them under 's', further adding a "reference" f' under 'out'
     */
    private static ParametricReactionRule<PureBigraph> collectDevices_Rule(DefaultDynamicSignature signature) throws InvalidConnectionException, IOException, InvalidReactionRuleException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builderRedex = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builderReactum = pureBuilder(signature);

        builderRedex.createRoot()
                .addChild("loc", "x").down()
                .addChild("f").addSite().addChild("dev", "y").addChild("s").down().addSite().top()
                .addChild("out").down().addSite().top();
        PureBigraph redex = builderRedex.createBigraph();


        BigraphEntity.OuterName y1 = builderReactum.createOuterName("y1");
        builderReactum.createRoot()
                .addChild("loc", "x").down()
                .addChild("f").addSite().addChild("s").down().addSite().addChild("dev", y1).top()
                .addChild("out").down().addSite().addChild("f'", "y").top();

        builderReactum.closeOuterName(y1);
        PureBigraph reactum = builderReactum.createBigraph();

        ParametricReactionRule<PureBigraph> pureBigraphParametricReactionRule = new ParametricReactionRule<>(redex, reactum);
        return pureBigraphParametricReactionRule;
    }
}
