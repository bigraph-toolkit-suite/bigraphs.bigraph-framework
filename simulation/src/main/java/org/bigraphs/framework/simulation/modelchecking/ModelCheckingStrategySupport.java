/*
 * Copyright (c) 2019-2026 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.modelchecking;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphDecoder;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphEncoder;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.*;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.modelchecking.predicates.PredicateChecker;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for supporting model checking strategy implementations.
 * Provides some useful method to keep subclasses simple.
 *
 * @author Dominik Grzelak
 * @see BreadthFirstStrategy
 * @see DepthFirstStrategy
 * @see RandomAgentModelCheckingStrategy
 * @see BFSFirstMatchStrategy
 * @see DFSFirstMatchStrategy
 *
 */
public abstract class ModelCheckingStrategySupport<B extends Bigraph<? extends Signature<?>>> implements ModelCheckingStrategy<B> {
    protected Logger logger = LoggerFactory.getLogger(ModelCheckingStrategySupport.class);

    protected BigraphModelChecker<B> modelChecker;
    protected PredicateChecker<B> predicateChecker;
    protected int occurrenceCounter = 0;
    protected JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
    protected JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();

    protected BigraphFilter<B> worklistFilter = BigraphFilter.noop();

    protected boolean isRunning = true;

    public ModelCheckingStrategySupport() {
    }

    public ModelCheckingStrategySupport(BigraphModelChecker<B> modelChecker) {
        this.modelChecker = modelChecker;
    }

    public abstract Collection<B> createWorklist();

    public abstract B removeNext(Collection<B> worklist);

    public abstract void addToWorklist(Collection<B> worklist, B bigraph);

    protected void resetOccurrenceCounter() {
        occurrenceCounter = 0;
    }

    int getOccurrenceCount() {
        return occurrenceCounter;
    }

    ReactiveSystem<B> getReactiveSystem() {
        return modelChecker.getReactiveSystem();
    }

    BigraphModelChecker.ReactiveSystemListener<B> getListener() {
        return modelChecker.reactiveSystemListener;
    }

    /**
     * @param reactionRule
     * @param next
     * @param bigraphRewritten
     * @param bfcfOfInitialBigraph the canonical form of the agent that leads to this result
     * @param occurrenceCount
     * @return
     */
    MatchResult<B> createMatchResult(ReactionRule<B> reactionRule, BigraphMatch<B> next, B bigraphRewritten, String bfcfOfInitialBigraph, int occurrenceCount) {
        return new MatchResult<>(reactionRule, next, bigraphRewritten, bfcfOfInitialBigraph, occurrenceCount);
    }

    /**
     * Main method for model checking.
     * The mode of traversal can be changed
     * by implementing the {@link #createWorklist()} and {@link #removeNext(Collection)} methods.
     * <p>
     * Alternatively, the #synthesizeTransitionSystem() method can be simply overridden.
     */
    public synchronized void synthesizeTransitionSystem() {
        Collection<B> worklist = createWorklist();
        Set<String> visitedStates = Collections.newSetFromMap(new ConcurrentHashMap<>());
        AtomicInteger iterationCounter = new AtomicInteger(0);

        this.predicateChecker = new PredicateChecker<>(modelChecker.getReactiveSystem().getPredicates());
        ModelCheckingOptions options = modelChecker.options;
        ModelCheckingOptions.TransitionOptions transitionOptions = options.get(ModelCheckingOptions.Options.TRANSITION);
        boolean reactionGraphWithCycles = options.isReactionGraphWithCycles();

        modelChecker.getReactionGraph().reset();

        B initialAgent = modelChecker.getReactiveSystem().getAgent();
        it.uniud.mads.jlibbig.core.std.Bigraph encoded = encoder.encode((PureBigraph) initialAgent);
        initialAgent = (B) decoder.decode(encoded);
        String rootBfcs = modelChecker.acquireCanonicalForm().bfcs(initialAgent);

        addToWorklist(worklist, initialAgent);
        visitedStates.add(rootBfcs);
        resetOccurrenceCounter();

        while (
                isRunning &&
                        !worklist.isEmpty() &&
                        iterationCounter.get() < transitionOptions.getMaximumTransitions()
        ) {
            B theAgent = worklistFilter.apply(removeNext(worklist));
            if (theAgent == null) continue;

            // String encoding for hashing and iso check
            String bfcfOfW = modelChecker.acquireCanonicalForm().bfcs(theAgent);

            // Predicate checking (unchanged)
            evaluatePredicates(theAgent, bfcfOfW, rootBfcs);

            Queue<MatchResult<B>> reactionResults = new ConcurrentLinkedQueue<>();

            // Reaction Rules
//            AbstractReactionRuleSupplier<B> inOrder = AbstractReactionRuleSupplier.createInOrder(modelChecker.getReactiveSystem().getReactionRules());
            Stream<ReactionRule<B>> rrStream; // = Stream.generate(inOrder);
            // Sort by priority
            List<ReactionRule<B>> sortedRules = new ArrayList<>(modelChecker.getReactiveSystem().getReactionRules());
            sortedRules.sort(Comparator.comparingLong(HasPriority::getPriority));
            rrStream = sortedRules.stream();

            if (options.isParallelRuleMatching()) rrStream = rrStream.parallel();

            rrStream
                    .limit(modelChecker.getReactiveSystem().getReactionRules().size())
                    .peek(rule -> getListener().onCheckingReactionRule(rule))
                    .flatMap(rule -> {
                        MatchIterable<BigraphMatch<B>> matches = getBigraphMatches(rule, theAgent);
                        for (BigraphMatch<B> match : matches) {
                            occurrenceCounter++;
                            B reaction = (theAgent.getSites().isEmpty() || match.getParameters().isEmpty())
                                    ? getReactiveSystem().buildGroundReaction(theAgent, match, rule)
                                    : getReactiveSystem().buildParametricReaction(theAgent, match, rule);

                            if (reaction != null)
                                reactionResults.add(createMatchResult(rule, match, reaction, bfcfOfW, getOccurrenceCount()));
                            else
                                getListener().onReactionIsNull();
                        }
                        return reactionResults.stream();
                    })
                    .forEachOrdered(matchResult -> {
                        String bfcf = modelChecker.acquireCanonicalForm().bfcs(matchResult.getBigraph());
                        String ruleLabel = modelChecker.getReactiveSystem().getReactionRulesMap().inverse().get(matchResult.getReactionRule());

                        if (!visitedStates.contains(bfcf)) {
                            modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, matchResult.getBigraph(), bfcf, matchResult, ruleLabel);
                            addToWorklist(worklist, matchResult.getBigraph());
                            visitedStates.add(bfcf);
                            getListener().onUpdateReactionRuleApplies(theAgent, matchResult.getReactionRule(), matchResult.getMatch());
                            modelChecker.exportState(matchResult.getBigraph(), bfcf, String.valueOf(matchResult.getOccurrenceCount()));
                            iterationCounter.incrementAndGet();
                        } else if (reactionGraphWithCycles) {
                            modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, matchResult.getBigraph(), bfcf, matchResult, ruleLabel);
                        }
                    });
        }

        logger.debug("Total States: {}", iterationCounter.get());
        logger.debug("Total Transitions: {}", modelChecker.getReactionGraph().getGraph().edgeSet().size());
        logger.debug("Total Occurrences: {}", getOccurrenceCount());
    }

    /**
     * Retrieves all matches of the given reaction rule for the provided agent bigraph.
     * <p>
     * Subclasses may override this method to introduce additional filtering,
     * pruning strategies, or domain-specific constraints on the match set
     * (e.g., restricting matches by attributes, spatial bounds, or metadata).
     * <p>
     * By default, this method delegates to the underlying
     * {@link org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher}.
     *
     * @param rule     the reaction rule whose redex will be matched against the agent
     * @param theAgent the agent (host) bigraph in which matches should be searched
     * @return an iterable collection of all matches found for the given rule on the agent;
     * never {@code null}, but may be empty if no matches exist. Depends on {@link org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher}.
     */
    protected MatchIterable<BigraphMatch<B>> getBigraphMatches(ReactionRule<B> rule, B theAgent) {
        return modelChecker.watch(() -> modelChecker.getMatcher().match(theAgent, rule));
    }

    /**
     * Defaults to {@link BigraphFilter#noop()} unless a subclass overrides this to add logic.
     *
     * @param filter the filter to apply
     */
    public void setFilter(BigraphFilter<B> filter) {
        this.worklistFilter = Objects.requireNonNullElse(filter, BigraphFilter.noop());
    }

    protected void evaluatePredicates(B agent, String canonical, String root) {
        if (predicateChecker.getPredicates().isEmpty()) return;

        if (predicateChecker.checkAll(agent)) {
            Optional<ReactionGraph.LabeledNode> tmp = modelChecker.reactionGraph.getLabeledNodeByCanonicalForm(canonical);
            String label = tmp.map(ReactionGraph.LabeledNode::getLabel)
                    .orElse(String.format("state-%s", modelChecker.reactionGraph.getGraph().vertexSet().size()));
            getListener().onAllPredicateMatched(agent, label);
            modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(canonical)
                    .ifPresent(node -> predicateChecker.getPredicates()
                            .forEach(p -> modelChecker.getReactionGraph().addPredicateMatchToNode(node, p)));
        } else {
            predicateChecker.getChecked().forEach((pred, passed) -> {
                if (!passed) {
                    try {
                        GraphPath<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> trace =
                                DijkstraShortestPath.findPathBetween(
                                        modelChecker.getReactionGraph().getGraph(),
                                        modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(canonical).get(),
                                        modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(root).get());
                        getListener().onPredicateViolated(agent, pred, trace);
                    } catch (Exception e) {
                        getListener().onError(e);
                    }
                } else {
                    getListener().onPredicateMatched(agent, pred);
                    if (pred instanceof SubBigraphMatchPredicate) {
                        getListener().onSubPredicateMatched(
                                agent, pred,
                                (B) ((SubBigraphMatchPredicate) pred).getContextBigraphResult(),
                                (B) ((SubBigraphMatchPredicate) pred).getSubBigraphResult(),
                                (B) ((SubBigraphMatchPredicate) pred).getSubRedexResult(),
                                (B) ((SubBigraphMatchPredicate) pred).getSubBigraphParamResult());
                    }
                    modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(canonical)
                            .ifPresent(node -> modelChecker.getReactionGraph().addPredicateMatchToNode(node, pred));
                }
            });
        }
    }

    public static class MatchResult<B extends Bigraph<? extends Signature<?>>> implements BMatchResult<B> {
        private final ReactionRule<B> reactionRule;
        private final BigraphMatch<B> next;
        private final B bigraph;
        private final int occurrenceCount;
        /**
         * The canonical encoding of the agent
         */
        private String canonicalStringOfResult = "";

        public MatchResult(ReactionRule<B> reactionRule, BigraphMatch<B> next, B bigraph, String bfcf, int occurrenceCount) {
            this.reactionRule = reactionRule;
            this.next = next;
            this.bigraph = bigraph;
            this.occurrenceCount = occurrenceCount;
            this.canonicalStringOfResult = bfcf;
        }

        public ReactionRule<B> getReactionRule() {
            return reactionRule;
        }

        public BigraphMatch<B> getMatch() {
            return next;
        }

        /**
         * This stores the rewritten bigraph for reference
         */
        public B getBigraph() {
            return bigraph;
        }

        public int getOccurrenceCount() {
            return occurrenceCount;
        }

        /**
         * The canonical encoding of the agent for this match result
         */
        public String getCanonicalString() {
            return canonicalStringOfResult;
        }

        public void setCanonicalStringOfResult(String canonicalStringOfResult) {
            this.canonicalStringOfResult = canonicalStringOfResult;
        }
    }
}
