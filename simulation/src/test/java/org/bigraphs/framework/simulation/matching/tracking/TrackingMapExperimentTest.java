package org.bigraphs.framework.simulation.matching.tracking;

import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
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
import java.util.Map;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

public class TrackingMapExperimentTest implements BigraphUnitTestSupport {
    private final static String DUMP_PATH = "src/test/resources/dump/tracking/test1/";

    @Test
    void test() throws InvalidReactionRuleException {
        SimpleBRS simpleBRS = new SimpleBRS();
        PureBigraph agent = agent();
        //Read attributes
        System.out.println(agent.getNodes().get(0).getAttributes());
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
        simpleBRS.addReactionRule(rr2); // test switching
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
                int ruleIx = 1;
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
        PureBigraphBuilder<DynamicSignature> bL = pureBuilder(createTrueFalseSignature());
        PureBigraphBuilder<DynamicSignature> bR = pureBuilder(createTrueFalseSignature());

        bL.root()
                .child("True").down().child("Set").top()
                .child("False").down().child("Empty").top()
        ;
        bR.root()
                .child("False").down().child("Set").top()
                .child("True").down().child("Empty").top()
        ;
        TrackingMap map = new TrackingMap(); // reactum -> redex
        // (!) Note: other mapping also possible, different meanings then
        // semantics: either "node changes its label", or "node moves"
        map.put("v0", "v2");
        map.put("v1", "v1");
        map.put("v2", "v0");
        map.put("v3", "v3");
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(bL.create(), bR.create(), true);
        rr.withTrackingMap(map);
        return rr;
    }

    // switches false to true
    private ParametricReactionRule<PureBigraph> switchRule2() throws InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> bL = pureBuilder(createTrueFalseSignature());
        PureBigraphBuilder<DynamicSignature> bR = pureBuilder(createTrueFalseSignature());

        bL.root()
                .child("False").down().child("Set").top()
                .child("True").down().child("Empty").top()
        ;
        bR.root()
                .child("True").down().child("Set").top()
                .child("False").down().child("Empty").top()
        ;
        TrackingMap map = new TrackingMap(); // reactum -> redex
        map.put("v0", "v2");
        map.put("v1", "v1");
        map.put("v2", "v0");
        map.put("v3", "v3");
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(bL.create(), bR.create(), true);
        rr.withTrackingMap(map);
        return rr;
    }

    // a new node is added, previously unknown to the agent
    private ParametricReactionRule<PureBigraph> addSetUnderEmpty() throws InvalidReactionRuleException {
        PureBigraphBuilder<DynamicSignature> bL = pureBuilder(createTrueFalseSignature());
        PureBigraphBuilder<DynamicSignature> bR = pureBuilder(createTrueFalseSignature());

        bL.root()
                .child("Box").down().site().top()
        ;
        bR.root()
                .child("Box").down().site().child("Set").top()
        ;
        TrackingMap map = new TrackingMap(); // reactum -> redex
        map.put("v0", "v0");
        map.put("v1", "");
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(bL.create(), bR.create(), true);
        rr.withTrackingMap(map);
        return rr;
    }


    // agent that can switch between two internal states by placing a token either in the one or the other container node
    // The True or False control is activated, but not both
    private PureBigraph agent() {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(createTrueFalseSignature());
        b.root()
                .child("True")
                .down().child("Empty").top()
                .child("False").down().child("Set").top()
                .child("Box").down()
                .child("Set").child("Set").child("Set").top()
        ;
        PureBigraph big = b.create();
        BigraphEntity.NodeEntity<DynamicControl> theNode = big.getNodes().get(0);
        Map<String, Object> attributes = theNode.getAttributes();
        attributes.put("myKey", "myValue");
        theNode.setAttributes(attributes);
        System.out.println("Attributes set for node = " + theNode);
        return big;
    }

    private DynamicSignature createTrueFalseSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add("True", 0)
                .add("False", 0)
                .add("Set", 0)
                .add("Empty", 0)
                .add("Box", 0)
        ;
        return defaultBuilder.create();
    }

    private DynamicSignature createAlphabetSignature() {
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
