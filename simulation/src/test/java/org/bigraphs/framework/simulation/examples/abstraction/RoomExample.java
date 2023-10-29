package org.bigraphs.framework.simulation.examples.abstraction;

import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphDecoder;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.InstantiationMap;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.simulation.examples.BaseExampleTestSupport;
import org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.matching.pure.PureBigraphParametricMatch;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.apache.commons.io.FileUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

public class RoomExample extends BaseExampleTestSupport {

    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/room-example/";
    private final static boolean AUTO_CLEAN_BEFORE = false;

    public RoomExample() {
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
    void room_001() throws Exception {
//        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig());

//        PureBigraph agent0 = agent0();
//        eb(agent0, "agent0", true);

        PureBigraph agent1 = agent1();
        eb(agent1, "agent1", true);

        ParametricReactionRule<PureBigraph> abstractionRule1 = alphaUserGroup();
        eb(abstractionRule1.getRedex(), "a1_lhs", true);
        eb(abstractionRule1.getReactum(), "a1_rhs", true);
//
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agent1, abstractionRule1);
        Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
        int cnt = 0;
        final JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
        PureReactiveSystem system = new PureReactiveSystem();
        system.setAgent(agent1);
        system.addReactionRule(abstractionRule1);
        while (iterator.hasNext()) {
            PureBigraphParametricMatch next = (PureBigraphParametricMatch) iterator.next();
            System.out.println("Match next=" + next);
            PureBigraph ctxDecoded = decoder.decode(next.getJLibMatchResult().getContext(), sig());
            PureBigraph redexImg = decoder.decode(next.getJLibMatchResult().getRedexImage(), sig());
            PureBigraph redexId = decoder.decode(next.getJLibMatchResult().getRedexId(), sig());
            PureBigraph redex = decoder.decode(next.getJLibMatchResult().getRedex(), sig());
            PureBigraph param = decoder.decode(next.getJLibMatchResult().getParam(), sig());
            eb(ctxDecoded, "context_"+ cnt);
            eb(redexImg, "rdxImage_"+ cnt);
            eb(redexId, "redexId_"+ cnt);
            eb(redex, "redex_"+ cnt);
            eb(param, "param_"+ cnt);
            cnt++;
        }



        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(system,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts());
        modelChecker.execute();
    }

    @Test
    void room_002() throws Exception {
        PureBigraph agent2 = loadBigraphFromFS("/home/dominik/git/BigraphFramework/simulation/src/test/resources/dump/room-example/states/state-4.png.xmi");
        eb(agent2, "agent2");

        ParametricReactionRule<PureBigraph> abstractionRule1 = alphaPCGroup();
        eb(abstractionRule1.getRedex(), "2a1_lhs", true);
        eb(abstractionRule1.getReactum(), "2a1_rhs", true);
//
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agent2, abstractionRule1);
        Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
        int cnt = 0;
        final JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
//        PureReactiveSystem system = new PureReactiveSystem();
//        system.setAgent(agent2);
//        system.addReactionRule(abstractionRule1);
        while (iterator.hasNext()) {
            PureBigraphParametricMatch next = (PureBigraphParametricMatch) iterator.next();
            System.out.println("Match next=" + next);
            PureBigraph ctxDecoded = decoder.decode(next.getJLibMatchResult().getContext(), sig());
            PureBigraph redexImg = decoder.decode(next.getJLibMatchResult().getRedexImage(), sig());
            PureBigraph redexId = decoder.decode(next.getJLibMatchResult().getRedexId(), sig());
            PureBigraph redex = decoder.decode(next.getJLibMatchResult().getRedex(), sig());
            PureBigraph param = decoder.decode(next.getJLibMatchResult().getParam(), sig());
            eb(ctxDecoded, "2context_"+ cnt);
            eb(redexImg, "2rdxImage_"+ cnt);
            eb(redexId, "2redexId_"+ cnt);
            eb(redex, "2redex_"+ cnt);
            eb(param, "2param_"+ cnt);
            cnt++;
        }


        PureReactiveSystem system = new PureReactiveSystem();
        system.setAgent(agent2);
        system.addReactionRule(abstractionRule1);
        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(system,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts());
        modelChecker.execute();
    }

    private PureBigraph loadBigraphFromFS(String path) throws IOException {
        EPackage metaModel = createOrGetBigraphMetaModel(sig());
        List<EObject> eObjects = BigraphFileModelManagement.Load.bigraphInstanceModel(metaModel,
                path);

        PureBigraphBuilder<DefaultDynamicSignature> b = PureBigraphBuilder.create(sig(), metaModel, eObjects.get(0));
        PureBigraph bigraph = b.createBigraph();
        return bigraph;
    }

    public ModelCheckingOptions opts() {
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(50)
                        .setMaximumTime(30)
                        .allowReducibleClasses(true) // use symmetries to make the transition graph smaller?
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                                .setReactionGraphFile(new File(TARGET_DUMP_PATH, "transition_graph.png"))
                                .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                                .setPrintCanonicalStateLabel(true)
//                                .setPrintCanonicalStateLabel(false)
                                .create()
                )
        ;
        return opts;
    }

    // With open links only
    public PureBigraph agent1() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig());

        BigraphEntity.OuterName tmpDoor1 = builder.createOuterName("Door1");
        BigraphEntity.OuterName tmpDoor2 = builder.createOuterName("Door2");
        builder.createRoot()
                .addChild("Room", tmpDoor1)
                .down().addChild("MPC", "u0").addChild("User", "u0")
                .addChild("Spool").linkToOuter("lan1").top()
                //
                .addChild("Door", tmpDoor1).linkToOuter(tmpDoor2).down().addChild("Unlocked").top()
                //
                .addChild("Room", tmpDoor2).down()
                .addChild("Printer", "lan1").linkToOuter("lan2")
                .addChild("PC", "u1").linkToOuter("lan2")
                .addChild("PC", "u2").linkToOuter("lan2")
                .addChild("PC", "u3").linkToOuter("lan2")
                .addChild("User").linkToOuter("u1")
                .addChild("User").linkToOuter("u2")
                .addChild("User").linkToOuter("u3")
        ;

        builder.closeAllInnerNames();

        PureBigraph b = builder.createBigraph();
        return b;
    }

    // With closed links only
    public PureBigraph agent0() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig());

        BigraphEntity.InnerName tmpSpPr = builder.createInnerName("tmpSpPr");
        BigraphEntity.InnerName tmpPrPc = builder.createInnerName("tmpPrPc");
        BigraphEntity.InnerName tmpDoor1 = builder.createInnerName("Door1");
        BigraphEntity.InnerName tmpDoor2 = builder.createInnerName("Door2");
        builder.createRoot()
                .addChild("Room").linkToInner(tmpDoor1)
                .down().connectByEdge("MPC", "User").addChild("Spool").linkToInner(tmpSpPr).top()
                .addChild("Door").linkToInner(tmpDoor1).linkToInner(tmpDoor2).down().addChild("Unlocked").top()
                .addChild("Room").linkToInner(tmpDoor2).down()
                .addChild("Printer").linkToInner(tmpSpPr).linkToInner(tmpPrPc)
                .connectByEdge("User", "PC").linkToInner(tmpPrPc)
                .connectByEdge("User", "PC").linkToInner(tmpPrPc)
                .connectByEdge("User", "PC").linkToInner(tmpPrPc)

//                .addChild("PC").linkToInner(tmpPrPc)
//                .addChild("PC").linkToInner(tmpPrPc)
        ;

        PureBigraph b = builder.createBigraph();
        return b;
    }

    // Abstraction rule 1
    public ParametricReactionRule<PureBigraph> alphaUserGroup() throws InvalidReactionRuleException, InvalidConnectionException, LinkTypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> bRedex = pureBuilder(sig());
        PureBigraphBuilder<DefaultDynamicSignature> bReactum = pureBuilder(sig());

        bRedex.createRoot().addChild("Room", "door")
                .down()
                .addSite()
                .addChild("User", "y1").down().addSite().up()
                .addChild("User", "y2").down().addSite().up()
                .addChild("User", "y3").down().addSite().up()
        ;


        bReactum.createOuterName("y2");
        bReactum.createOuterName("y3");
        bReactum.createRoot().addChild("Room", "door")
                .down()
                .addSite()
                .addChild("UserGroup", "y1")
        ;
        bReactum.closeAllInnerNames();

        PureBigraph redex = bRedex.createBigraph();
        PureBigraph reactum = bReactum.createBigraph();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum, InstantiationMap.create(1));
        return rr;
    }

    public ParametricReactionRule<PureBigraph> alphaPCGroup() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> bRedex = pureBuilder(sig());
        PureBigraphBuilder<DefaultDynamicSignature> bReactum = pureBuilder(sig());

        bRedex.createRoot().addChild("Room", "door")
                .down()
                .addSite()
                .addChild("PC", "y1").linkToOuter("l2").down().addSite().up()
                .addChild("PC", "y2").linkToOuter("l2").down().addSite().up()
                .addChild("PC", "y3").linkToOuter("l2").down().addSite().up()
        ;


        bReactum.createOuterName("y2");
//        bReactum.createOuterName("l2");
        bReactum.createOuterName("y3");
        bReactum.createRoot().addChild("Room", "door")
                .down()
                .addSite()
                .addChild("PCGroup", "y1").linkToOuter("l2")
        ;
        bReactum.closeAllInnerNames();

        PureBigraph redex = bRedex.createBigraph();
        PureBigraph reactum = bReactum.createBigraph();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum, InstantiationMap.create(1));
        return rr;
    }

    public static DefaultDynamicSignature sig() {
        DefaultDynamicSignature signature = pureSignatureBuilder()
                .newControl("Room", 1).assign()
                .newControl("Door", 2).assign()
                .newControl("Unlocked", 0).status(ControlStatus.ATOMIC).assign()
                .newControl("PC", 2).assign()
                .newControl("MPC", 1).assign()
                .newControl("Spool", 1).assign()
                .newControl("Printer", 2).assign()
                .newControl("User", 1).assign()
                .newControl("UserGroup", 1).assign()
                .newControl("PCGroup", 2).assign()
                .create();
        return signature;
    }
}
