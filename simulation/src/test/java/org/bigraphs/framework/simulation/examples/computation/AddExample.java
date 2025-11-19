/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.examples.computation;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.base.Stopwatch;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.FileUtils;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.ElementaryBigraph;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.*;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.factory.BigraphFactory;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.examples.BaseExampleTestSupport;
import org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
@Disabled
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
    void simulate() throws LinkTypeNotExistsException, InvalidConnectionException, IOException, InvalidReactionRuleException, IncompatibleInterfaceException {
        int a = 3, b = 3;
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
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        PureBigraphBuilder<DynamicSignature>.Hierarchy leftNode =
                builder.hierarchy(signature.getControlByName("Left"))
                        .child("S");
        PureBigraphBuilder<DynamicSignature>.Hierarchy rightNode =
                builder.hierarchy(signature.getControlByName("Right"))
                        .child("S");

        for (int i = 0; i < left - 1; i++) {
            leftNode = leftNode.down().child("S");
        }
        leftNode = leftNode.down().child("Z").top();
        for (int i = 0; i < right - 1; i++) {
            rightNode = rightNode.down().child("S");
        }
        rightNode = rightNode.down().child("Z").top();

        builder.root()
                .child("Plus")
                .down()
                .child(leftNode)
                .child(rightNode)
//                .child(s.top())
        ;
        builder.makeGround();
        return builder.create();
    }

    /**
     * react r1 = Left.S | Right.S -> Left | Right;
     */
    public static ReactionRule<PureBigraph> createReactionRule_1() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root()
                .child("Left").down().child("S").down().site()
                .top()
                .child("Right").down().site()
        ;
        builder2.root()
                .child("Left").down().site()
                .top()
                .child("Right").down().child("S").down().site()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
//        InstantiationMap instantiationMap = InstantiationMap.create(2);
//        instantiationMap.map(0, 0);
//        instantiationMap.map(1, 0);
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum); //instantiationMap
        return rr;
    }

    public static ReactionRule<PureBigraph> createReactionRule_2() throws ControlIsAtomicException, InvalidReactionRuleException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DynamicSignature> builder2 = pureBuilder(signature);

        builder.root()
                .child("Left").down().child("Z")
                .top()
                .child("Right").down().child("S").down().site()
        ;
        builder2.root()
                .child("S").down().site()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        return new ParametricReactionRule<>(redex, reactum);
    }

    static void createGraphvizOutput(Bigraph<?> agent, BigraphMatch<?> next, String path) throws IncompatibleSignatureException, IncompatibleInterfaceException, IOException {
        PureBigraph context = (PureBigraph) next.getContext();
        PureBigraph redex = (PureBigraph) next.getRedex();
        Bigraph contextIdentity = next.getContextIdentity();
        ElementaryBigraph<DynamicSignature> identityForParams = next.getRedexIdentity();
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

    private static DynamicSignature createExampleSignature() {
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
