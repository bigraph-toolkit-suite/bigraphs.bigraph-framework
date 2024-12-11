package org.bigraphs.framework.simulation.modelchecking;

import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphDecoder;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphEncoder;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.modelchecking.predicates.PredicateChecker;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The algorithm implemented here is a variant of the BreadthFirstSimulationStrategy without cycle checking.
 * Thus, graph isomorphism checks are not conducted.
 * <p>
 * Leads to higher state-spaces than BFS with cycle checking but less expensive because canonical label is not computed.
 *
 * @author Dominik Grzelak
 */
@Deprecated
public class BreadthFirstSimulationStrategy<B extends Bigraph<? extends Signature<?>>> extends ModelCheckingStrategySupport<B> {
    private final Logger logger = LoggerFactory.getLogger(BreadthFirstSimulationStrategy.class);

    protected PredicateChecker<B> predicateChecker;
    //    private BigraphCanonicalForm canonicalForm;
    protected JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
    protected JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();

    public BreadthFirstSimulationStrategy(BigraphModelChecker<B> modelChecker) {
        super(modelChecker);
    }

    /**
     * Compute the transition system of a bigraph with all added reaction rules so far.
     */
    public synchronized void synthesizeTransitionSystem() {
        this.predicateChecker = new PredicateChecker<>(modelChecker.getReactiveSystem().getPredicates());
        ModelCheckingOptions options = modelChecker.options;
        if (Objects.nonNull(options.get(ModelCheckingOptions.Options.EXPORT))) {
            ModelCheckingOptions.ExportOptions opts = options.get(ModelCheckingOptions.Options.EXPORT);
            modelChecker.getReactionGraph().setCanonicalNodeLabel(opts.getPrintCanonicalStateLabel());
        }
        ModelCheckingOptions.TransitionOptions transitionOptions = options.get(ModelCheckingOptions.Options.TRANSITION);
        logger.debug("Maximum transitions={}", transitionOptions.getMaximumTransitions());
        modelChecker.getReactionGraph().reset();
        final Queue<B> workingQueue = new ConcurrentLinkedDeque<>();

        B initialAgent = modelChecker.getReactiveSystem().getAgent();
        it.uniud.mads.jlibbig.core.std.Bigraph encoded = encoder.encode((PureBigraph) initialAgent);
        initialAgent = (B) decoder.decode(encoded);
        String rootBfcs = String.valueOf(initialAgent.hashCode());
        workingQueue.add(initialAgent);
        AtomicInteger iterationCounter = new AtomicInteger(0);
        resetOccurrenceCounter();
        while (!workingQueue.isEmpty() && iterationCounter.get() < transitionOptions.getMaximumTransitions()) {
            Queue<MatchResult<B>> reactionResults = new ConcurrentLinkedQueue<>();

            // "Remove the first element w of the work queue Q."
            final B theAgent = workingQueue.remove();
            final String bfcfOfW = String.valueOf(theAgent.hashCode());
            // "For each reaction rule, find all matches m1 ...mn in w"
            for (ReactionRule<B> eachRule : modelChecker.getReactiveSystem().getReactionRules()) {
                getListener().onCheckingReactionRule(eachRule);
                MatchIterable<BigraphMatch<B>> match = modelChecker.watch(() -> modelChecker.getMatcher().match(theAgent, eachRule));
                for (BigraphMatch<B> eachMatch : match) {
                    increaseOccurrenceCounter();
                    B reaction = null;
                    if (theAgent.getSites().size() == 0 || eachMatch.getParameters().size() == 0) {
                        reaction = getReactiveSystem().buildGroundReaction(theAgent, eachMatch, eachRule);
                    } else {
                        reaction = getReactiveSystem().buildParametricReaction(theAgent, eachMatch, eachRule);
                    }

                    Optional.ofNullable(reaction)
                            .map(x -> {
                                getListener().onUpdateReactionRuleApplies(theAgent, eachRule, eachMatch);
                                return reactionResults.add(createMatchResult(eachRule, eachMatch, x, bfcfOfW, getOccurrenceCount()));
                            })
                            .orElseGet(() -> {
                                getListener().onReactionIsNull();
                                return false;
                            });
                }
            }

            for (MatchResult<B> matchResult : reactionResults) {
                String bfcf = String.valueOf(matchResult.getBigraph().hashCode());
                String reactionLbl = modelChecker.getReactiveSystem().getReactionRulesMap().inverse().get(matchResult.getReactionRule());
                modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, matchResult.getBigraph(), bfcf, matchResult, reactionLbl);
                workingQueue.add(matchResult.getBigraph());
                modelChecker.exportState(matchResult.getBigraph(), bfcf, String.valueOf(matchResult.getOccurrenceCount()));
                iterationCounter.incrementAndGet();
            }
            if (predicateChecker.getPredicates().size() > 0) {
                // "Check each property p ∈ P against w."
                //TODO evaluate in "reaction graph spec" what should happen here: violation or stop criteria?
                // this is connected to the predicates (changes its "intent", what they are used for)
                if (predicateChecker.checkAll(theAgent)) {
                    String label = "";
                    Optional<ReactionGraph.LabeledNode> tmp = modelChecker.reactionGraph.getLabeledNodeByCanonicalForm(bfcfOfW);
                    if (tmp.isPresent() && tmp.get() instanceof ReactionGraph.DefaultLabeledNode) {
                        label = tmp.get().getLabel();
                    } else {
                        label = String.format("state-%s", String.valueOf(modelChecker.reactionGraph.getGraph().vertexSet().size()));
                    }
                    getListener().onAllPredicateMatched(theAgent, label);
                    Optional<ReactionGraph.LabeledNode> node = modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(bfcfOfW);
                    node.ifPresent(labeledNode -> {
                        predicateChecker.getPredicates().forEach(p -> {
                            modelChecker.getReactionGraph().addPredicateMatchToNode(labeledNode, p);
                        });
                    });
                } else {
                    // compute counter-example trace from w back to the root
                    try {
//                DijkstraShortestPath<String, String> dijkstraShortestPath = new DijkstraShortestPath<>(reactionGraph.getGraph());
                        GraphPath<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> pathBetween = DijkstraShortestPath.findPathBetween(modelChecker.getReactionGraph().getGraph(),
                                modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(bfcfOfW).get(),
                                modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(rootBfcs).get()
                        );
                        predicateChecker.getChecked().entrySet().stream().forEach(eachPredicate -> {
                            if (!eachPredicate.getValue()) {
                                logger.debug("Counter-example trace for predicate violation: start state={}, end state={}", pathBetween.getStartVertex(), pathBetween.getEndVertex());
                                getListener().onPredicateViolated(theAgent, eachPredicate.getKey(), pathBetween);
                            } else {
                                Optional<ReactionGraph.LabeledNode> node = modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(bfcfOfW);
                                node.ifPresent(labeledNode -> {
                                    modelChecker.getReactionGraph().addPredicateMatchToNode(labeledNode, eachPredicate.getKey());
                                });
                                getListener().onPredicateMatched(theAgent, eachPredicate.getKey());
                            }
                        });
                    } catch (Exception e) {
                        getListener().onError(e);
                    }
                }
            }
//            transitionCnt.set(modelChecker.getReactionGraph().getGraph().vertexSet().size());
            // "Repeat the procedure for the next item in the work queue, terminating successfully if the work queue is empty."
        }
        logger.debug("Total States: {}", iterationCounter.get());
        logger.debug("Total Transitions: {}", modelChecker.getReactionGraph().getGraph().edgeSet().size());
        logger.debug("Total Occurrences: {}", getOccurrenceCount());
    }
}
