package org.bigraphs.framework.simulation.examples;

import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphEncoder;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import it.uniud.mads.jlibbig.core.std.*;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dominik Grzelak
 */
public class RobotZoneMovementExample implements BigraphModelChecker.ReactiveSystemListener<PureBigraph> {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/robot-mvmt/";
//    private static PureBigraphFactory factory = pure();

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    private Bigraph createJBigraph() {
        Signature signature = JLibBigBigraphEncoder.parseSignature(createSignature());
        BigraphBuilder b = new BigraphBuilder(signature);

        OuterName belongsTo = b.addOuterName("belongsTo");
        OuterName isFree = b.addOuterName("isFree");
        OuterName rId = b.addOuterName("rId");
        OuterName canGrip = b.addOuterName("canGrip");
        Root root = b.addRoot();
        Node zone1 = b.addNode("Zone1", root);
        Node robot = b.addNode("Robot", zone1, rId);
        Node gripper = b.addNode("Gripper", robot, canGrip);

        Node zone2 = b.addNode("Zone2", root);
        Node zone3 = b.addNode("Zone3", zone2);
        Node object = b.addNode("Object", zone2, isFree);
        Node ownership = b.addNode("Ownership", object, belongsTo);
        b.ground();
        Bigraph bigraph = b.makeBigraph(true);

        return bigraph;

    }

    @Test
    void jtest() throws InvalidConnectionException, LinkTypeNotExistsException, InvalidReactionRuleException, IOException {
        PureBigraph agent_a = agent();
        ReactionRule<PureBigraph> reactionRule_1 = createReactionRule_1();
        BigraphGraphvizExporter.toPNG(agent_a,
                true,
                new File(TARGET_DUMP_PATH + "agent.png")
        );
        JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
        Bigraph jAgent = encoder.encode(agent_a); //createJBigraph(); //

//        System.out.println(encoder.encode(agent_a));
        System.out.println(jAgent);

        Signature jSig = jAgent.getSignature(); //JLibBigBigraphEncoder.parseSignature(agent_a.getSignature());
        Bigraph jRedex = encoder.encode(reactionRule_1.getRedex(), jSig);
        Bigraph jReactum = encoder.encode(reactionRule_1.getReactum(), jSig);

//        RewritingRule jRule = new RewritingRule(jRedex, jReactum);
//        AgentRewritingRule jArule = new AgentRewritingRule(jAgent, jAgent);
        AgentRewritingRule jArule = new AgentRewritingRule(jRedex, jReactum, 0);
        Iterable<Bigraph> apply = jArule.apply(jAgent);
        System.out.println(apply);
        System.out.println(apply.iterator());
        System.out.println(apply.iterator().hasNext());

    }

    @Test
    void simulate() throws IOException, LinkTypeNotExistsException, InvalidConnectionException, InvalidReactionRuleException, BigraphSimulationException, ReactiveSystemException {
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(8)
                        .setMaximumTime(30)
                        .allowReducibleClasses(true)
                        .create()
                )
                .doMeasureTime(false)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(TARGET_DUMP_PATH + "transition_system.png"))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .create()
                )
        ;
        PureReactiveSystem reactiveSystem = new PureReactiveSystem();

        PureBigraph agent_a = agent();
        ReactionRule<PureBigraph> reactionRule_1 = createReactionRule_1();
        BigraphGraphvizExporter.toPNG(agent_a,
                true,
                new File(TARGET_DUMP_PATH + "agent.png")
        );
        BigraphGraphvizExporter.toPNG(reactionRule_1.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "redex1.png")
        );
        BigraphGraphvizExporter.toPNG(reactionRule_1.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "reactum1.png")
        );
        reactiveSystem.addReactionRule(reactionRule_1);
        reactiveSystem.setAgent(agent_a);
//
        PureBigraphModelChecker modelChecker = (PureBigraphModelChecker) new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts)
                .setReactiveSystemListener(this);
        modelChecker.execute();
        assertTrue(Files.exists(Paths.get(TARGET_DUMP_PATH, "transition_system.png")));
    }

    @Override
    public void onUpdateReactionRuleApplies(PureBigraph agent, ReactionRule<PureBigraph> reactionRule, BigraphMatch<PureBigraph> matchResult) {
        System.out.println("RR: " + reactionRule.getRedex());
        int cnt = 0;
        for (PureBigraph each : matchResult.getParameters()) {
            try {
                BigraphGraphvizExporter.toPNG(each,
                        true,
                        new File(TARGET_DUMP_PATH + "d" + cnt + ".png")
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
            cnt++;
        }
        try {
            BigraphGraphvizExporter.toPNG(matchResult.getContext(),
                    true,
                    new File(TARGET_DUMP_PATH + "context.png")
            );
            BigraphGraphvizExporter.toPNG(matchResult.getContextIdentity(),
                    true,
                    new File(TARGET_DUMP_PATH + "contextIdentity.png")
            );
            BigraphGraphvizExporter.toPNG(matchResult.getRedexIdentity(),
                    true,
                    new File(TARGET_DUMP_PATH + "redexIdentity.png")
            );
            BigraphGraphvizExporter.toPNG(matchResult.getRedexImage(),
                    true,
                    new File(TARGET_DUMP_PATH + "redexImage.png")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private PureBigraph agent() throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy zone1 = b.hierarchy("Zone1");
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy zone2 = b.hierarchy("Zone2");
        zone1.addChild("Robot", "rId").down().addChild("Gripper", "canGrip");
        zone2.addChild("Zone3").addChild("Object", "isFree").down().addChild("Ownership", "belongsTo");
        b.createRoot()
                .addChild(zone1).top().addChild(zone2).top();
        return b.createBigraph();
    }

    public ReactionRule<PureBigraph> createReactionRule_1() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> b2 = pureBuilder(createSignature());

        b1.createRoot().addChild("Zone1").down().addChild("Robot", "rId").down().addSite();
        b1.createRoot().addChild("Zone2").down().addChild("Zone3").addChild("Object", "isFree").down().addChild("Ownership", "belongsTo");

        b2.createRoot().addChild("Zone1");
        b2.createRoot().addChild("Zone2").down().addChild("Zone3").down().addChild("Robot", "rId").down().addSite().up().up()
                .addChild("Object", "isFree").down().addChild("Ownership", "belongsTo");

        PureBigraph redex = b1.createBigraph();
        PureBigraph reactum = b2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private DefaultDynamicSignature createSignature() {
        return pureSignatureBuilder()
                .newControl("Robot", 1).assign()
                .newControl("Gripper", 1).assign()
                .newControl("Object", 1).assign()
                .newControl("Ownership", 1).assign()
                .newControl("Zone1", 0).assign()
                .newControl("Zone2", 0).assign()
                .newControl("Zone3", 0).assign()
                .create();
    }
}
