package de.tudresden.inf.st.bigraphs.simulation.examples;

import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.simulation.ReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.exceptions.BigraphSimulationException;
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

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pure;
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureBuilder;
import static de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions.transitionOpts;

/**
 * @author Dominik Grzelak
 */
public class TraceabilityExample {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/traceability/";

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    @Test
    void test() throws InvalidConnectionException, IOException, InvalidReactionRuleException, BigraphSimulationException {
        PureBigraph agent = createAgent();
        BigraphGraphvizExporter.toPNG(agent,
                true,
                new File(TARGET_DUMP_PATH + "agent.png")
        );

        ReactionRule<PureBigraph> rr1 = moveRoom();
        ReactionRule<PureBigraph> rr2 = connectToComputer();
        BigraphGraphvizExporter.toPNG(rr1.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "redex1.png")
        );
        BigraphGraphvizExporter.toPNG(rr1.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "reactum1.png")
        );
        BigraphGraphvizExporter.toPNG(rr2.getRedex(),
                true,
                new File(TARGET_DUMP_PATH + "redex2.png")
        );
        BigraphGraphvizExporter.toPNG(rr2.getReactum(),
                true,
                new File(TARGET_DUMP_PATH + "reactum2.png")
        );

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addReactionRule(rr1);
        reactiveSystem.addReactionRule(rr2);

        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(20)
                        .setMaximumTime(30)
                        .allowReducibleClasses(false) // use symmetries to make the transition graph smaller?
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setTraceFile(new File(TARGET_DUMP_PATH, "transition_graph.png"))
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .setPrintCanonicalStateLabel(true)
                        .create()
                )
        ;

        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(reactiveSystem,
                BigraphModelChecker.SimulationType.BREADTH_FIRST,
                opts);
        modelChecker.execute();
    }

    PureBigraph createAgent() throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(createSignature());
        builder.createRoot()
                .addChild("Room").down().addChild("Computer", "link").up()
                .addChild("Person")
        ;
        return builder.createBigraph();
    }

    ReactionRule<PureBigraph> moveRoom() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builderReactum = pureBuilder(createSignature());

        builderRedex.createRoot().addChild("Room").down().addSite().up().addChild("Person");

        builderReactum.createRoot().addChild("Room").down().addSite().addChild("Person");

        PureBigraph redex = builderRedex.createBigraph();
        PureBigraph reactum = builderReactum.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }

    ReactionRule<PureBigraph> connectToComputer() throws InvalidConnectionException, InvalidReactionRuleException {
        PureBigraphBuilder<DefaultDynamicSignature> builderRedex = pureBuilder(createSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builderReactum = pureBuilder(createSignature());

        builderRedex.createRoot().addChild("Room").down().addChild("Computer", "link").addChild("Person");

        builderReactum.createRoot().addChild("Room").down().addChild("Computer", "link").addChild("Person", "link");

        PureBigraph redex = builderRedex.createBigraph();
        PureBigraph reactum = builderReactum.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }

    private DefaultDynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pure().createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Person")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
        ;

        return defaultBuilder.create();
    }

}