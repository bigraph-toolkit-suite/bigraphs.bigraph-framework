package org.bigraphs.framework.simulation.modelchecking;

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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Base class for supporting model checking strategy implementations.
 * Provides some useful method to keep subclasses simple.
 *
 * @author Dominik Grzelak
 */
public abstract class ModelCheckingStrategySupport<B extends Bigraph<? extends Signature<?>>> implements ModelCheckingStrategy<B> {
    protected Logger logger = LoggerFactory.getLogger(ModelCheckingStrategySupport.class);

    protected BigraphModelChecker<B> modelChecker;
    protected PredicateChecker<B> predicateChecker;
    protected int occurrenceCounter = 0;
    protected JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
    protected JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();

    public ModelCheckingStrategySupport(BigraphModelChecker<B> modelChecker) {
        this.modelChecker = modelChecker;
    }

    protected abstract Collection<B> createWorklist();

    protected abstract B removeNext(Collection<B> worklist);

    protected abstract void addToWorklist(Collection<B> worklist, B bigraph);

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

        while (!worklist.isEmpty() && iterationCounter.get() < transitionOptions.getMaximumTransitions()) {
            B theAgent = removeNext(worklist);
            String bfcfOfW = modelChecker.acquireCanonicalForm().bfcs(theAgent);
            Queue<MatchResult<B>> reactionResults = new ConcurrentLinkedQueue<>();

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
                        MatchIterable<BigraphMatch<B>> matches = modelChecker.watch(() -> modelChecker.getMatcher().match(theAgent, rule));
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

            // Predicate checking (unchanged)
            evaluatePredicates(theAgent, bfcfOfW, rootBfcs);
        }

        logger.debug("Total States: {}", iterationCounter.get());
        logger.debug("Total Transitions: {}", modelChecker.getReactionGraph().getGraph().edgeSet().size());
        logger.debug("Total Occurrences: {}", getOccurrenceCount());
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
         *
         * @return
         */
        public B getBigraph() {
            return bigraph;
        }

        public int getOccurrenceCount() {
            return occurrenceCount;
        }

        /**
         * The canonical encoding of the agent for this match result
         *
         * @return
         */
        public String getCanonicalString() {
            return canonicalStringOfResult;
        }

        public void setCanonicalStringOfResult(String canonicalStringOfResult) {
            this.canonicalStringOfResult = canonicalStringOfResult;
        }
    }
}
