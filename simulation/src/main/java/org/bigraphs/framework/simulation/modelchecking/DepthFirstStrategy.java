package org.bigraphs.framework.simulation.modelchecking;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.modelchecking.predicates.PredicateChecker;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.bigraphs.framework.simulation.modelchecking.reactions.AbstractReactionRuleSupplier;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * This algorithm implements a depth-first model checking algorithm.
 * It also detects cycles.
 * This algorithm can be used to conduct reachability analysis.
 * <p>
 * It can be useful for agent-based path planning problems.
 *
 * @author Dominik Grzelak
 */
public class DepthFirstStrategy<B extends Bigraph<? extends Signature<?>>> extends ModelCheckingStrategySupport<B> {

    public DepthFirstStrategy(BigraphModelChecker<B> modelChecker) {
        super(modelChecker);
    }

    @Override
    public void synthesizeTransitionSystem() {
        Stack<B> workingStack = new Stack<>();
        Set<String> visitedStates = ConcurrentHashMap.newKeySet();

        this.predicateChecker = new PredicateChecker<>(modelChecker.getReactiveSystem().getPredicates());
        ModelCheckingOptions options = modelChecker.options;
        boolean reactionGraphWithCycles = options.isReactionGraphWithCycles();
        ModelCheckingOptions.TransitionOptions transitionOptions = options.get(ModelCheckingOptions.Options.TRANSITION);

        modelChecker.getReactionGraph().reset();
        B initialAgent = modelChecker.getReactiveSystem().getAgent();

// Normalize initial agent
        it.uniud.mads.jlibbig.core.std.Bigraph encoded = encoder.encode((PureBigraph) initialAgent);
        initialAgent = (B) decoder.decode(encoded);
        String rootBfcs = modelChecker.acquireCanonicalForm().bfcs(initialAgent);
        workingStack.push(initialAgent);
        visitedStates.add(rootBfcs);
        resetOccurrenceCounter();

        AtomicInteger iterationCounter = new AtomicInteger(0);

        while (!workingStack.isEmpty() && iterationCounter.get() < transitionOptions.getMaximumTransitions()) {
            B theAgent = workingStack.pop();
            String bfcfOfW = modelChecker.acquireCanonicalForm().bfcs(theAgent);

            Queue<MatchResult<B>> reactionResults = new ConcurrentLinkedQueue<>();

            AbstractReactionRuleSupplier<B> inOrder = AbstractReactionRuleSupplier.createInOrder(modelChecker.getReactiveSystem().getReactionRules());
            Stream<ReactionRule<B>> rrStream = Stream.generate(inOrder);

            if (options.isParallelRuleMatching()) {
                rrStream = rrStream.parallel();
            }

            rrStream
                    .limit(modelChecker.getReactiveSystem().getReactionRules().size())
                    .peek(rule -> getListener().onCheckingReactionRule(rule))
                    .flatMap(rule -> {
                        MatchIterable<BigraphMatch<B>> matches = modelChecker.watch(() -> modelChecker.getMatcher().match(theAgent, rule));
                        for (BigraphMatch<B> match : matches) {
                            increaseOccurrenceCounter();
                            B reaction = (theAgent.getSites().isEmpty() || match.getParameters().isEmpty())
                                    ? getReactiveSystem().buildGroundReaction(theAgent, match, rule)
                                    : getReactiveSystem().buildParametricReaction(theAgent, match, rule);

                            if (reaction != null) {
                                reactionResults.add(createMatchResult(rule, match, reaction, bfcfOfW, getOccurrenceCount()));
                            } else {
                                getListener().onReactionIsNull();
                            }
                        }
                        return reactionResults.stream();
                    })
                    .forEachOrdered(matchResult -> {
                        String bfcf = modelChecker.acquireCanonicalForm().bfcs(matchResult.getBigraph());
                        String ruleLabel = modelChecker.getReactiveSystem().getReactionRulesMap().inverse().get(matchResult.getReactionRule());

                        if (!visitedStates.contains(bfcf)) {
                            modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, matchResult.getBigraph(), bfcf, matchResult, ruleLabel);
                            workingStack.push(matchResult.getBigraph());
                            visitedStates.add(bfcf);
                            getListener().onUpdateReactionRuleApplies(theAgent, matchResult.getReactionRule(), matchResult.getMatch());
                            modelChecker.exportState(matchResult.getBigraph(), bfcf, String.valueOf(matchResult.getOccurrenceCount()));
                            iterationCounter.incrementAndGet();
                        } else if (reactionGraphWithCycles) {
                            modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, matchResult.getBigraph(), bfcf, matchResult, ruleLabel);
                        }
                    });

            // Predicate evaluation
            if (!predicateChecker.getPredicates().isEmpty()) {
                if (predicateChecker.checkAll(theAgent)) {
                    Optional<ReactionGraph.LabeledNode> tmp = modelChecker.reactionGraph.getLabeledNodeByCanonicalForm(bfcfOfW);
                    String label = tmp.map(ReactionGraph.LabeledNode::getLabel)
                            .orElse(String.format("state-%s", modelChecker.reactionGraph.getGraph().vertexSet().size()));
                    getListener().onAllPredicateMatched(theAgent, label);
                    modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(bfcfOfW).ifPresent(node ->
                            predicateChecker.getPredicates().forEach(p ->
                                    modelChecker.getReactionGraph().addPredicateMatchToNode(node, p)
                            )
                    );
                } else {
                    predicateChecker.getChecked().forEach((pred, passed) -> {
                        if (!passed) {
                            try {
                                GraphPath<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> trace =
                                        DijkstraShortestPath.findPathBetween(modelChecker.getReactionGraph().getGraph(),
                                                modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(bfcfOfW).get(),
                                                modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(rootBfcs).get());
                                getListener().onPredicateViolated(theAgent, pred, trace);
                            } catch (Exception e) {
                                getListener().onError(e);
                            }
                        } else {
                            getListener().onPredicateMatched(theAgent, pred);
                            if (pred instanceof SubBigraphMatchPredicate) {
                                getListener().onSubPredicateMatched(
                                        theAgent, pred,
                                        (B) ((SubBigraphMatchPredicate) pred).getContextBigraphResult(),
                                        (B) ((SubBigraphMatchPredicate) pred).getSubBigraphResult(),
                                        (B) ((SubBigraphMatchPredicate) pred).getSubRedexResult(),
                                        (B) ((SubBigraphMatchPredicate) pred).getSubBigraphParamResult()
                                );
                            }
                            modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(bfcfOfW).ifPresent(node ->
                                    modelChecker.getReactionGraph().addPredicateMatchToNode(node, pred)
                            );
                        }
                    });
                }
            }
        }
    }
}

