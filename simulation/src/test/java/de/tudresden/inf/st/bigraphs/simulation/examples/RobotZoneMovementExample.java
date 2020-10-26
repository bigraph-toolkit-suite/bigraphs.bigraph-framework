package de.tudresden.inf.st.bigraphs.simulation.examples;

import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.simulation.ReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.exceptions.BigraphSimulationException;
import de.tudresden.inf.st.bigraphs.simulation.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.BigraphModelChecker;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.PureBigraphModelChecker;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.impl.PureReactiveSystem;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pure;
import static de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dominik Grzelak
 */
public class RobotZoneMovementExample implements BigraphModelChecker.ReactiveSystemListener<PureBigraph> {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/robot-mvmt/";
    private static PureBigraphFactory factory = pure();

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    @Test
    void simulate() throws IOException, LinkTypeNotExistsException, InvalidConnectionException, InvalidReactionRuleException, BigraphSimulationException {
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
        reactiveSystem.addReactionRule(createReactionRule_1());
        reactiveSystem.setAgent(agent_a);
//
        PureBigraphModelChecker modelChecker = (PureBigraphModelChecker) new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationType.BREADTH_FIRST,
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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private PureBigraph agent() throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> b = pure().createBigraphBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy zone1 = b.hierarchy("Zone1");
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy zone2 = b.hierarchy("Zone2");
        zone1.addChild("Robot", "rId").down().addChild("Gripper", "canGrip");
        zone2.addChild("Zone3").addChild("Object", "isFree").down().addChild("Ownership", "belongsTo");
        b.createRoot()
                .addChild(zone1).top().addChild(zone2).top();
        return b.createBigraph();
    }

    public ReactionRule<PureBigraph> createReactionRule_1() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> b1 = pure().createBigraphBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> b2 = pure().createBigraphBuilder(createSignature());

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
        return pure().createSignatureBuilder()
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
