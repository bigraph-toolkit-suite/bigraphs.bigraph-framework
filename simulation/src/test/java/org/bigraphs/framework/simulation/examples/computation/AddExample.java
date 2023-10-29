package org.bigraphs.framework.simulation.examples.computation;

import com.google.common.base.Stopwatch;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.ElementaryBigraph;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.*;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.examples.BaseExampleTestSupport;
import org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test shows how to add two numbers in a bigraphical fashion.
 * Therefore, a specific reactive system is defined (extending from {@link PureReactiveSystem}, which is called
 * {@link AddExpr}.
 * <p>
 * It is shown how one can use reaction rules to program such computations and encapsulate it in a sub-BRS.
 * The developer can use the API of Bigraph Framework to define a custom logic how the rules are going to be executed.
 * Additionally, a model checker may be used as well, which is not incorporated here in this example (because it is not
 * needed). This would allow to check automatically for mistakes if, e.g., the order of rules is changed accidentally.
 *
 * @author Dominik Grzelak
 */
public class AddExample extends BaseExampleTestSupport {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/add/";

    public AddExample() {
        super(TARGET_DUMP_PATH, true);
    }

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
    }

    @Test
    void simulate_counting_example() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, InvalidReactionRuleException, IncompatibleInterfaceException {
        int a = 2, b = 3;
        AddExpr reactiveSystem = new AddExpr(a, b);
        eb(reactiveSystem.agent_a, "agent");
        eb(reactiveSystem.reactionRule_1.getRedex(), "r1-redex");
        eb(reactiveSystem.reactionRule_1.getReactum(), "r1-reactum");
        eb(reactiveSystem.reactionRule_2.getRedex(), "r2-redex");
        eb(reactiveSystem.reactionRule_2.getReactum(), "r2-reactum");

        PureBigraph result = reactiveSystem.execute();

        long s = result.getNodes().stream().filter(x -> x.getControl().getNamedType().stringValue().equals("S"))
                .count();
        assertEquals(a + b, s);
        BigraphGraphvizExporter.toPNG(result,
                true,
                new File(TARGET_DUMP_PATH + "result.png")
        );
    }

    public static class AddExpr extends PureReactiveSystem {
        PureBigraph agent_a;
        ReactionRule<PureBigraph> reactionRule_1;
        ReactionRule<PureBigraph> reactionRule_2;

        public AddExpr(int a, int b) throws LinkTypeNotExistsException, InvalidConnectionException, InvalidReactionRuleException, IOException {
            agent_a = createAgent_A(a, b);
            setAgent(agent_a);
            reactionRule_1 = createReactionRule_1();
            reactionRule_2 = createReactionRule_2();
            addReactionRule(reactionRule_1);
            addReactionRule(reactionRule_2);
        }

        public PureBigraph execute() throws IOException {
            AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);

            PureBigraph agentTmp = getAgent();
            int cnt = 0;
            while (true) {
                MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agentTmp, getReactionRulesMap().get("r0"));
                Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
                if (!iterator.hasNext()) {
                    break;
                }
                while (iterator.hasNext()) {
                    BigraphMatch<PureBigraph> next = iterator.next();
//                    createGraphvizOutput(getAgent(), next, TARGET_DUMP_PATH + "/");
//                    System.out.println("NEXT: " + next);
                    agentTmp = buildParametricReaction(agentTmp, next, getReactionRulesMap().get("r0"));
                    BigraphGraphvizExporter.toPNG(agentTmp,
                            true,
                            new File(TARGET_DUMP_PATH + cnt + "_agent_reacted.png")
                    );
                    cnt++;
                }
            }

            MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agentTmp, getReactionRulesMap().get("r1"));
            Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
            if (iterator.hasNext()) {
                BigraphMatch<PureBigraph> next = iterator.next();
//                createGraphvizOutput(getAgent(), next, TARGET_DUMP_PATH + "/");
                System.out.println("NEXT: " + next);
                agentTmp = buildParametricReaction(agentTmp, next, getReactionRulesMap().get("r1"));
                BigraphGraphvizExporter.toPNG(agentTmp,
                        true,
                        new File(TARGET_DUMP_PATH + cnt + "_agent_reacted.png")
                );
                cnt++;
            }
            return agentTmp; // the result
        }
    }


    /**
     * big numberLeft = Left.S.S.S.S.S.S.Z;
     * big numberRight = Right.S.S.S.S.Z;
     * big start = Plus . (numberLeft | numberRight);
     */
    public static PureBigraph createAgent_A(final int left, final int right) throws ControlIsAtomicException, InvalidArityOfControlException, LinkTypeNotExistsException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy leftNode =
                builder.hierarchy(signature.getControlByName("Left"))
                        .addChild("S");
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy rightNode =
                builder.hierarchy(signature.getControlByName("Right"))
                        .addChild("S");

        for (int i = 0; i < left - 1; i++) {
            leftNode = leftNode.down().addChild("S");
        }
        leftNode = leftNode.down().addChild("Z").top();
        for (int i = 0; i < right - 1; i++) {
            rightNode = rightNode.down().addChild("S");
        }
        rightNode = rightNode.down().addChild("Z").top();

        builder.createRoot()
                .addChild("Plus")
                .down()
                .addChild(leftNode)
                .addChild(rightNode)
//                .addChild(s.top())
        ;
        builder.makeGround();
        return builder.createBigraph();
    }

    /**
     * react r1 = Left.S | Right.S -> Left | Right;
     */
    public static ReactionRule<PureBigraph> createReactionRule_1() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("Left").down().addChild("S").down().addSite()
                .top()
                .addChild("Right").down().addSite()
        ;
        builder2.createRoot()
                .addChild("Left").down().addSite()
                .top()
                .addChild("Right").down().addChild("S").down().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
//        InstantiationMap instantiationMap = InstantiationMap.create(2);
//        instantiationMap.map(0, 0);
//        instantiationMap.map(1, 0);
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum); //instantiationMap
        return rr;
    }

    public static ReactionRule<PureBigraph> createReactionRule_2() throws ControlIsAtomicException, InvalidReactionRuleException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("Left").down().addChild("Z")
                .top()
                .addChild("Right").down().addChild("S").down().addSite()
        ;
        builder2.createRoot()
                .addChild("S").down().addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        return new ParametricReactionRule<>(redex, reactum);
    }

    static void createGraphvizOutput(Bigraph<?> agent, BigraphMatch<?> next, String path) throws IncompatibleSignatureException, IncompatibleInterfaceException, IOException {
        PureBigraph context = (PureBigraph) next.getContext();
        PureBigraph redex = (PureBigraph) next.getRedex();
        Bigraph contextIdentity = next.getContextIdentity();
        ElementaryBigraph<DefaultDynamicSignature> identityForParams = next.getRedexIdentity();
        PureBigraph contextComposed = (PureBigraph) BigraphFactory.ops(context).parallelProduct(contextIdentity).getOuterBigraph();
//            BigraphModelFileStore.exportAsInstanceModel(contextComposed, "contextComposed",
//                    new FileOutputStream("src/test/resources/graphviz/contextComposed.xmi"));
        BigraphGraphvizExporter.toPNG(contextComposed,
                true,
                new File(path + "contextComposed.png")
        );

        Stopwatch timer = Stopwatch.createStarted();
        try {
            String convert = BigraphGraphvizExporter.toPNG(context,
                    true,
                    new File(path + "context.png")
            );
//            System.out.println(convert);
            BigraphGraphvizExporter.toPNG(agent,
                    true,
                    new File(path + "agent.png")
            );
            BigraphGraphvizExporter.toPNG(redex,
                    true,
                    new File(path + "redex.png")
            );
            BigraphGraphvizExporter.toPNG(contextIdentity,
                    true,
                    new File(path + "identityForContext.png")
            );
            BigraphGraphvizExporter.toPNG(identityForParams,
                    true,
                    new File(path + "identityForParams.png")
            );

//            BigraphComposite bigraphComposite = factory
//                    .asBigraphOperator(identityForParams).parallelProduct(redex); //.compose();
//            GraphvizConverter.toPNG(bigraphComposite.getOuterBigraph(),
//                    true,
//                    new File(path + "redexImage.png")
//            );

            AtomicInteger cnt = new AtomicInteger(0);
            next.getParameters().forEach(x -> {
                try {
                    BigraphGraphvizExporter.toPNG((PureBigraph) x,
                            true,
                            new File(path + "param_" + cnt.incrementAndGet() + ".png")
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            long elapsed = timer.stop().elapsed(TimeUnit.MILLISECONDS);
            System.out.println("Create png's took (millisecs) " + elapsed);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static DefaultDynamicSignature createExampleSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Plus")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Sum")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("S")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Z")).arity(FiniteOrdinal.ofInteger(0)).assign()
//                .newControl().identifier(StringTypedName.of("True")).arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().identifier(StringTypedName.of("False")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Left")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Right")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;

        return defaultBuilder.create();
    }
}
