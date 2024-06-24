package org.bigraphs.framework.simulation.reactionrules;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.*;
import org.bigraphs.framework.simulation.BigraphUnitTestSupport;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.checkerframework.dataflow.qual.Pure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

public class RuleCompositionTest implements BigraphUnitTestSupport {
    private final static String DUMP = "src/test/resources/dump/rulecomposition/";

    @Test
    void test_rule_parallelProduct_01() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, IOException, IncompatibleInterfaceException {

        ReactionRule<PureBigraph> rr01 = createRR_01();
        eb(rr01.getRedex(), DUMP + "rr01_LHS");
        eb(rr01.getReactum(), DUMP + "rr01_RHS");

        ReactionRule<PureBigraph> rr02 = createRR_02();
        eb(rr02.getRedex(), DUMP + "rr02_LHS");
        eb(rr02.getReactum(), DUMP + "rr02_RHS");

        ReactionRuleComposer<ParametricReactionRule<Bigraph<?>>> rComp = new ReactionRuleComposer<>();
        ParametricReactionRule<Bigraph<?>> product = rComp.parallelProduct(rr01, rr02);
        eb(product.getRedex(), DUMP + "product_LHS");
        eb(product.getReactum(), DUMP + "product_RHS");

        System.out.println(product.getTrackingMap());
        product.getInstantationMap().getMappings().forEach((k,v) -> {
            System.out.println(k.getValue() + " --> " + v.getValue());
        });
    }

    private ReactionRule<PureBigraph> createRR_01() throws InvalidReactionRuleException {
        DefaultDynamicSignature sig = sig();
        PureBigraphBuilder<DefaultDynamicSignature> bRedex = pureBuilder(sig);
        PureBigraphBuilder<DefaultDynamicSignature> bReactum = pureBuilder(sig);

        bRedex.createRoot()
                .addChild("A").down()
                /**/.addSite().top()
        ;
        bReactum.createRoot()
                .addChild("A").down()
                /**/.addSite()
                /**/.addChild("B")
        ;
        PureBigraph redex = bRedex.createBigraph();
        PureBigraph reactum = bReactum.createBigraph();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        rr.withLabel("R1");
        TrackingMap map = new TrackingMap();
        map.put("v0", "v0");
        map.put("v1", "");
        rr.withTrackingMap(map);
        InstantiationMap iMap = InstantiationMap.create(1);
        iMap.map(0, 0);
        rr.withInstantiationMap(iMap);
        return rr;
    }

    private ReactionRule<PureBigraph> createRR_02() throws InvalidReactionRuleException {
        DefaultDynamicSignature sig = sig();
        PureBigraphBuilder<DefaultDynamicSignature> bRedex = pureBuilder(sig);
        PureBigraphBuilder<DefaultDynamicSignature> bReactum = pureBuilder(sig);

        bRedex.createRoot()
                .addChild("C").down()
                /**/.addChild("B").down()
                /**//**/.addSite().addChild("C")
        ;

        bReactum.createRoot()
                .addChild("C").down()
                /**/.addChild("B").down()
                /**//**/.addSite().addChild("C")
        ;

        PureBigraph redex = bRedex.createBigraph();
        PureBigraph reactum = bReactum.createBigraph();
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        rr.withLabel("R2");
        TrackingMap map = new TrackingMap();
        map.put("v0", "v0");
        map.put("v1", "v1");
        map.put("v2", "v2");
        rr.withTrackingMap(map);
        InstantiationMap iMap = InstantiationMap.create(1);
        iMap.map(0, 0);
        rr.withInstantiationMap(iMap);
        return rr;
    }

    private DefaultDynamicSignature sig() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .addControl("A", 1)
                .addControl("B", 1)
                .addControl("C", 1)
                .addControl("D", 1)
        ;
        return defaultBuilder.create();
    }


}
