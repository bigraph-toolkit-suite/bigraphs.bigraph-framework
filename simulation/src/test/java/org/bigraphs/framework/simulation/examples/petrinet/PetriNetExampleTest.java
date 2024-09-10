package org.bigraphs.framework.simulation.examples.petrinet;

import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.reactivesystem.AbstractReactionRule;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.TrackingMap;
import org.bigraphs.framework.simulation.BigraphUnitTestSupport;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

public class PetriNetExampleTest implements BigraphUnitTestSupport {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/petrinet/";

    @BeforeAll
    static void beforeAll() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    @Test
    void simpleTokenFiring_portSorting() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, ReactiveSystemException, BigraphSimulationException {
        DefaultDynamicSignature sig = sig();
        PureBigraph agent = petriNet(sig);
        ReactionRule<PureBigraph> rule1 = petriNetFireRule(sig);

        eb(agent, TARGET_DUMP_PATH + "agent");
        eb(rule1.getRedex(), TARGET_DUMP_PATH + "rule1_LHS");
        eb(rule1.getReactum(), TARGET_DUMP_PATH + "rule1_RHS");

        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .doMeasureTime(true)
                .setReactionGraphWithCycles(true)
                .and(transitionOpts()
                        .setMaximumTransitions(150)
                        .setMaximumTime(60)
                        .allowReducibleClasses(false)
                        .create()
                )
                .and(ModelCheckingOptions.exportOpts()
                                .setReactionGraphFile(new File(completePath.toUri()))
                                .setPrintCanonicalStateLabel(false)
                                .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .setFormatsEnabled(List.of(ModelCheckingOptions.ExportOptions.Format.XMI, ModelCheckingOptions.ExportOptions.Format.PNG))
//                        .disableAllFormats()
                                .create()
                )
        ;

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(rule1);
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.execute();
    }


    @Test
    void simpleTokenFiring_noPortSorting() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, ReactiveSystemException, BigraphSimulationException {
        DefaultDynamicSignature sig = sig_withOuterNames();
//        DefaultDynamicSignature sig = sig();
        PureBigraph agent = petriNet_withOuterNames(sig);
        ReactionRule<PureBigraph> rule1 = petriNetFireRule_withOuterNames(sig);

        eb(agent, TARGET_DUMP_PATH + "agent");
        eb(rule1.getRedex(), TARGET_DUMP_PATH + "rule1_LHS");
        eb(rule1.getReactum(), TARGET_DUMP_PATH + "rule1_RHS");

        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .doMeasureTime(true)
                .setReactionGraphWithCycles(true)
                .and(transitionOpts()
                        .setMaximumTransitions(20)
                        .setMaximumTime(60)
                        .allowReducibleClasses(true)
                        .create()
                )
                .and(ModelCheckingOptions.exportOpts()
                                .setReactionGraphFile(new File(completePath.toUri()))
                                .setPrintCanonicalStateLabel(false)
                                .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                                .setFormatsEnabled(List.of(ModelCheckingOptions.ExportOptions.Format.XMI, ModelCheckingOptions.ExportOptions.Format.PNG))
//                        .disableAllFormats()
                                .create()
                )
        ;

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(rule1);
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts);
        modelChecker.execute();
    }

    public static DefaultDynamicSignature sig() {
        DefaultDynamicSignature signature = pureSignatureBuilder()
                .addControl("Place", 1)
                .addControl("Transition", 2)
                .addControl("Token", 0)
                .create();
        return signature;
    }

    public static DefaultDynamicSignature sig_withOuterNames() {
        DefaultDynamicSignature signature = pureSignatureBuilder()
                .addControl("Place", 1)
                .addControl("Transition", 1)
                .addControl("Token", 0)
                .create();
        return signature;
    }

    public static PureBigraph petriNet(DefaultDynamicSignature signature) throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        builder.createRoot()
                .addChild("Place").linkToInner("tmp").down().addChild("Token").addChild("Token").up()
                .addChild("Transition").linkToInner("tmp").linkToInner("tmp2")
                .addChild("Place").linkToInner("tmp2")
        ;
        builder.closeAllInnerNames();

        return builder.createBigraph();
    }

    public static PureBigraph petriNet_withOuterNames(DefaultDynamicSignature signature) throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

//        builder.createOuterName("idleOuter");
        builder.createRoot()
                .addChild("Place").linkToOuter("y").down().addChild("Token").addChild("Token").up()
                .addChild("Transition").linkToOuter("y")
                .addChild("Place").linkToOuter("y")
        ;

        return builder.createBigraph();
    }

    public static ReactionRule<PureBigraph> petriNetFireRule(DefaultDynamicSignature signature) throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> b2 = pureBuilder(signature);

        b1.createRoot()
                .addChild("Place").linkToInner("tmp").down().addChild("Token").addSite().top()
                .addChild("Transition").linkToInner("tmp").linkToInner("tmp2")
                .addChild("Place").linkToInner("tmp2").down().addSite().top()
        ;
        b1.closeAllInnerNames();

        b2.createRoot()
                .addChild("Place").linkToInner("tmp").down().addSite().top()
                .addChild("Transition").linkToInner("tmp").linkToInner("tmp2")
                .addChild("Place").linkToInner("tmp2").down().addSite().addChild("Token").top()
        ;
        b2.closeAllInnerNames();

        AbstractReactionRule<PureBigraph> rule = new ParametricReactionRule<>(b1.createBigraph(), b2.createBigraph()).withLabel("petriNetFireRule");

        TrackingMap trackingMap = new TrackingMap();
        trackingMap.put("v0", "v0"); // left-place of transition in redex
        trackingMap.put("v3", "v1"); // left token of left-place in redex
        trackingMap.put("v1", "v2"); // transition in redex
        trackingMap.put("v2", "v3"); // right-place of transition in redex
        trackingMap.put("e0", "e0"); // edge from left-place to transition
        trackingMap.put("e1", "e1"); // edges from transition to right-place
        trackingMap.addLinkNames("e0", "e1"); // and possible outer names
        rule.withTrackingMap(trackingMap);

        return rule;
    }

    public static ReactionRule<PureBigraph> petriNetFireRule_withOuterNames(DefaultDynamicSignature signature) throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> b2 = pureBuilder(signature);

        b1.createRoot()
                .addChild("Place", "y").down().addChild("Token").addSite().top()
                .addChild("Transition", "y")
                .addChild("Place", "y").down().addSite().top()
        ;
        b1.closeAllInnerNames();

        b2.createRoot()
                .addChild("Place", "y").down().addSite().top()
                .addChild("Transition", "y")
                .addChild("Place", "y").down().addSite().addChild("Token").top()
        ;
        b2.closeAllInnerNames();
        AbstractReactionRule<PureBigraph> rule = new ParametricReactionRule<>(b1.createBigraph(), b2.createBigraph()).withLabel("petriNetFireRule_withOuterNames");
        TrackingMap map = new TrackingMap();
        map.put("v0", "v0");
        map.put("v1", "v2");
        map.put("v2", "v3");
        map.put("v3", "v1");
        map.addLinkNames("y");
        rule.withTrackingMap(map);
        return rule;
    }

    public static ReactionRule<PureBigraph> petriNetAddRule(DefaultDynamicSignature signature) throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> b2 = pureBuilder(signature);

        b1.createRoot()
                .addChild("Place", "x").down().addSite().top()
        ;

        b2.createRoot()
                .addChild("Place", "x").down().addSite().addChild("Token").top()
        ;

        return new ParametricReactionRule<>(b1.createBigraph(), b2.createBigraph()).withLabel("petriNetAddTokenRule");
    }

}
