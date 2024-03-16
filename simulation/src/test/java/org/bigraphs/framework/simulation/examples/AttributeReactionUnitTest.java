package org.bigraphs.framework.simulation.examples;

import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicControl;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.TrackingMap;
import org.bigraphs.framework.simulation.BigraphUnitTestSupport;
import org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.simulation.modelchecking.BigraphModelChecker;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.visualization.BigraphGraphicsExporter;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.checkerframework.dataflow.qual.Pure;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

public class AttributeReactionUnitTest implements BigraphUnitTestSupport {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/attributes/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }
    @Test
    void attribute_preservation_reaction() throws InvalidReactionRuleException {
        DefaultDynamicSignature sig = pureSignatureBuilder()
                .addControl("Place", 0)
                .addControl("Token", 0)
                .create();
        PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(sig);
        PureBigraph bigraph = b.createRoot()
                .addChild("Place").down().addChild("Token").up()
                .addChild("Place")
                .createBigraph();
        eb(bigraph, TARGET_DUMP_PATH + "s_0");
        BigraphEntity.NodeEntity<DefaultDynamicControl> v1 = bigraph.getNodes().stream()
                .filter(x -> x.getName().equals("v1")).findAny().get();
        Map<String, Object> attributes = v1.getAttributes();
        attributes.put("ip", "192.168.0.1");
        v1.setAttributes(attributes);
        System.out.println(attributes);

        PureBigraphBuilder<DefaultDynamicSignature> bRedex = pureBuilder(sig);
        PureBigraphBuilder<DefaultDynamicSignature> bReactum = pureBuilder(sig);
        bRedex.createRoot().addChild("Place").down().addChild("Token").up()
                .addChild("Place");
        bReactum.createRoot().addChild("Place")
                .addChild("Place").down().addChild("Token").up();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(bRedex.createBigraph(), bReactum.createBigraph())
                .withLabel("swapRule");
        // important for tracing nodes through reactions, thus, to correctly preserve attributes
        TrackingMap eta = new TrackingMap();
        eta.put("v0", "v0");
        eta.put("v1", "v2");
        eta.put("v2", "v1");
        // assign the tracking map to the rule
        rr.withTrackingMap(eta);
        eb(rr.getRedex(), TARGET_DUMP_PATH + "rr_LHS");
        eb(rr.getReactum(), TARGET_DUMP_PATH + "rr_RHS");

        // build a reactive system
        PureReactiveSystem rs = new PureReactiveSystem();
        rs.setAgent(bigraph);
        rs.addReactionRule(rr);
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(bigraph, rr);
        Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
        int transition = 1;
        while (iterator.hasNext()) {
            BigraphMatch<PureBigraph> next = iterator.next();
            PureBigraph result = rs.buildParametricReaction(bigraph, next, rr);
            eb(result, TARGET_DUMP_PATH + "s_" + transition);
            transition++;
            Map<String, Object> attr = result.getNodes().stream()
                    .filter(x -> x.getName().equals("v1")).findAny().get().getAttributes();
            System.out.println(attr);
        }


//        ModelCheckingOptions modOpts = ModelCheckingOptions.create().and(transitionOpts().setMaximumTransitions(10).create());
//        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
//                reactiveSystem,
//                BigraphModelChecker.SimulationStrategy.Type.BFS,
//                modOpts);
//        modelChecker.setReactiveSystemListener(somewhereModality);
//        modelChecker.execute();
    }

    private static DefaultDynamicSignature createExampleSignature() {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder
                .addControl("Place", 0)
                .addControl("Token", 0)
        ;
        return signatureBuilder.create();
    }
}
