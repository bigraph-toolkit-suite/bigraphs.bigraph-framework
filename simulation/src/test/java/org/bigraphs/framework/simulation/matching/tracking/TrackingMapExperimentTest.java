package org.bigraphs.framework.simulation.matching.tracking;

import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.AbstractReactionRule;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.TrackingMap;
import org.bigraphs.framework.simulation.BigraphUnitTestSupport;
import org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

public class TrackingMapExperimentTest implements BigraphUnitTestSupport {
    private final static String DUMP_PATH = "src/test/resources/dump/tracking/test1/";

    @Test
    void test() throws InvalidReactionRuleException {
        SimpleBRS simpleBRS = new SimpleBRS();
        PureBigraph agent = agent();
        ParametricReactionRule<PureBigraph> rr = switchRule1().withLabel("r0");
        ParametricReactionRule<PureBigraph> rr2 = switchRule2().withLabel("r1");
        AbstractReactionRule<PureBigraph> rr3 = addSetUnderEmpty().withLabel("r2");

        eb(agent, DUMP_PATH + "agent");
        eb(rr.getRedex(), DUMP_PATH + "switch1LHS");
        eb(rr.getReactum(), DUMP_PATH + "switch1RHS");
        eb(rr2.getRedex(), DUMP_PATH + "switch2LHS");
        eb(rr2.getReactum(), DUMP_PATH + "switch2RHS");
        eb(rr3.getRedex(), DUMP_PATH + "emptyAddSetLHS");
        eb(rr3.getReactum(), DUMP_PATH + "emptyAddSetRHS");

        simpleBRS.setAgent(agent);
//        simpleBRS.addReactionRule(rr); // test switching
//        simpleBRS.addReactionRule(rr2); // test switching
//        simpleBRS.addReactionRule(rr3); // test addition

        simpleBRS.execute();

//        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
//                simpleBRS,
//                BigraphModelChecker.SimulationStrategy.Type.BFS,
//                opts());
//        modelChecker.execute();
    }

    public class SimpleBRS extends PureReactiveSystem {

        public void execute() {
            AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
            PureBigraph currentAgent = getAgent();
            int ruleExecCounter = 2;
            int ixCnt = 0;
            while (ruleExecCounter > 0) {
//                int ruleIx = ruleExecCounter % 2 == 0 ? 1 : 0;
                int ruleIx = 2;
                MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(currentAgent, getReactionRulesMap().get("r" + ruleIx));
                Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
                if (!iterator.hasNext()) {
                    break;
                }
                PureBigraph agentTmp = null;
                while (iterator.hasNext()) {
                    BigraphMatch<PureBigraph> next = iterator.next();
                    agentTmp = buildParametricReaction(currentAgent, next, getReactionRulesMap().get("r" + ruleIx));
                    TrackingMapExperimentTest.this.eb(agentTmp, DUMP_PATH + "agent-" + ixCnt);
                    ixCnt++;
                }

                currentAgent = agentTmp;

                ruleExecCounter--;
            }
        }
    }

    private ModelCheckingOptions opts() {
        Path completePath = Paths.get(DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(60)
                        .setMaximumTime(60)
                        .allowReducibleClasses(false)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(completePath.toUri()))
                        .setPrintCanonicalStateLabel(false)
                        .setOutputStatesFolder(new File(DUMP_PATH + "states/"))
                        .create()
                )
        ;
        return opts;
    }

    // switches true to false
    private ParametricReactionRule<PureBigraph> switchRule1() throws InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> bL = pureBuilder(createTrueFalseSignature());
        PureBigraphBuilder<DefaultDynamicSignature> bR = pureBuilder(createTrueFalseSignature());

        bL.createRoot()
                .addChild("True").down().addChild("Set").top()
                .addChild("False").down().addChild("Empty").top()
        ;
        bR.createRoot()
                .addChild("False").down().addChild("Set").top()
                .addChild("True").down().addChild("Empty").top()
        ;
        TrackingMap map = new TrackingMap(); // reactum -> redex
        // (!) Note: other mapping also possible, different meanings then
        // semantics: either "node changes its label", or "node moves"
        map.put("v0", "v2");
        map.put("v1", "v1");
        map.put("v2", "v0");
        map.put("v3", "v3");
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(bL.createBigraph(), bR.createBigraph(), true);
        rr.withTrackingMap(map);
        return rr;
    }

    // switches false to true
    private ParametricReactionRule<PureBigraph> switchRule2() throws InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> bL = pureBuilder(createTrueFalseSignature());
        PureBigraphBuilder<DefaultDynamicSignature> bR = pureBuilder(createTrueFalseSignature());

        bL.createRoot()
                .addChild("False").down().addChild("Set").top()
                .addChild("True").down().addChild("Empty").top()
        ;
        bR.createRoot()
                .addChild("True").down().addChild("Set").top()
                .addChild("False").down().addChild("Empty").top()
        ;
        TrackingMap map = new TrackingMap(); // reactum -> redex
        map.put("v0", "v2");
        map.put("v1", "v1");
        map.put("v2", "v0");
        map.put("v3", "v3");
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(bL.createBigraph(), bR.createBigraph(), true);
        rr.withTrackingMap(map);
        return rr;
    }

    // a new node is added, previously unknown to the agent
    private ParametricReactionRule<PureBigraph> addSetUnderEmpty() throws InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> bL = pureBuilder(createTrueFalseSignature());
        PureBigraphBuilder<DefaultDynamicSignature> bR = pureBuilder(createTrueFalseSignature());

        bL.createRoot()
                .addChild("Box").down().addSite().top()
        ;
        bR.createRoot()
                .addChild("Box").down().addSite().addChild("Set").top()
        ;
        TrackingMap map = new TrackingMap(); // reactum -> redex
        map.put("v0", "v0");
        map.put("v1", "");
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(bL.createBigraph(), bR.createBigraph(), true);
        rr.withTrackingMap(map);
        return rr;
    }


    // agent that can switch between two internal states by placing a token either in the one or the other container node
    // The True or False control is activated, but not both
    private PureBigraph agent() {
        PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(createTrueFalseSignature());
        b.createRoot()
                .addChild("True")
                .down().addChild("Empty").top()
                .addChild("False").down().addChild("Set").top()
                .addChild("Box").down()
                .addChild("Set").addChild("Set").addChild("Set").top()
        ;
        return b.createBigraph();
    }

    private DefaultDynamicSignature createTrueFalseSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .addControl("True", 0)
                .addControl("False", 0)
                .addControl("Set", 0)
                .addControl("Empty", 0)
                .addControl("Box", 0)
        ;
        return defaultBuilder.create();
    }

    private DefaultDynamicSignature createAlphabetSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("C")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("D")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("E")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("F")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("G")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("H")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("I")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("J")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("Q")).arity(FiniteOrdinal.ofInteger(5)).assign()
                .newControl().identifier(StringTypedName.of("R")).arity(FiniteOrdinal.ofInteger(5)).assign()
        ;

        return defaultBuilder.create();
    }
}
