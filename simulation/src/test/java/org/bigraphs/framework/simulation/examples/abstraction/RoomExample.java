/*
 * Copyright (c) 2022-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.examples.abstraction;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphDecoder;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
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
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
        PureBigraph agent0 = agent0();
        eb(agent0, "agent0", true);

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

        PureBigraphBuilder<DynamicSignature> b = PureBigraphBuilder.create(sig(), metaModel, eObjects.get(0));
        PureBigraph bigraph = b.create();
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
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());

        BigraphEntity.OuterName tmpDoor1 = builder.createOuter("Door1");
        BigraphEntity.OuterName tmpDoor2 = builder.createOuter("Door2");
        builder.root()
                .child("Room", tmpDoor1)
                .down().child("MPC", "u0").child("User", "u0")
                .child("Spool").linkOuter("lan1").top()
                //
                .child("Door", tmpDoor1).linkOuter(tmpDoor2).down().child("Unlocked").top()
                //
                .child("Room", tmpDoor2).down()
                .child("Printer", "lan1").linkOuter("lan2")
                .child("PC", "u1").linkOuter("lan2")
                .child("PC", "u2").linkOuter("lan2")
                .child("PC", "u3").linkOuter("lan2")
                .child("User").linkOuter("u1")
                .child("User").linkOuter("u2")
                .child("User").linkOuter("u3")
        ;

        builder.closeInner();

        PureBigraph b = builder.create();
        return b;
    }

    // With closed links only
    public PureBigraph agent0() throws Exception {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig());

        BigraphEntity.InnerName tmpSpPr = builder.createInner("tmpSpPr");
        BigraphEntity.InnerName tmpPrPc = builder.createInner("tmpPrPc");
        BigraphEntity.InnerName tmpDoor1 = builder.createInner("Door1");
        BigraphEntity.InnerName tmpDoor2 = builder.createInner("Door2");
        builder.root()
                .child("Room").linkInner(tmpDoor1)
                .down().connectByEdge("MPC", "User").child("Spool").linkInner(tmpSpPr).top()
                .child("Door").linkInner(tmpDoor1).linkInner(tmpDoor2).down().child("Unlocked").top()
                .child("Room").linkInner(tmpDoor2).down()
                .child("Printer").linkInner(tmpSpPr).linkInner(tmpPrPc)
                .connectByEdge("User", "PC").linkInner(tmpPrPc)
                .connectByEdge("User", "PC").linkInner(tmpPrPc)
                .connectByEdge("User", "PC").linkInner(tmpPrPc)

//                .child("PC").linkToInner(tmpPrPc)
//                .child("PC").linkToInner(tmpPrPc)
        ;

        PureBigraph b = builder.create();
        return b;
    }

    // Abstraction rule 1
    public ParametricReactionRule<PureBigraph> alphaUserGroup() throws InvalidReactionRuleException, InvalidConnectionException, LinkTypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> bRedex = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> bReactum = pureBuilder(sig());

        bRedex.root().child("Room", "door")
                .down()
                .site()
                .child("User", "y1").down().site().up()
                .child("User", "y2").down().site().up()
                .child("User", "y3").down().site().up()
        ;


        bReactum.createOuter("y2");
        bReactum.createOuter("y3");
        bReactum.root().child("Room", "door")
                .down()
                .site()
                .child("UserGroup", "y1")
        ;
        bReactum.closeInner();

        PureBigraph redex = bRedex.create();
        PureBigraph reactum = bReactum.create();
        InstantiationMap instMap = InstantiationMap.create(reactum.getSites().size());
        instMap.map(0, 0);
        return new ParametricReactionRule<>(redex, reactum, instMap).withLabel("abstractUser");
    }

    public ParametricReactionRule<PureBigraph> alphaPCGroup() throws Exception {
        PureBigraphBuilder<DynamicSignature> bRedex = pureBuilder(sig());
        PureBigraphBuilder<DynamicSignature> bReactum = pureBuilder(sig());

        bRedex.root().child("Room", "door")
                .down()
                .site()
                .child("PC", "y1").linkOuter("l2").down().site().up()
                .child("PC", "y2").linkOuter("l2").down().site().up()
                .child("PC", "y3").linkOuter("l2").down().site().up()
        ;


        bReactum.createOuter("y2");
//        bReactum.createOuterName("l2");
        bReactum.createOuter("y3");
        bReactum.root().child("Room", "door")
                .down()
                .site()
                .child("PCGroup", "y1").linkOuter("l2")
        ;
        bReactum.closeInner();

        PureBigraph redex = bRedex.create();
        PureBigraph reactum = bReactum.create();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum, InstantiationMap.create(1));
        return rr;
    }

    public static DynamicSignature sig() {
        DynamicSignature signature = pureSignatureBuilder()
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
