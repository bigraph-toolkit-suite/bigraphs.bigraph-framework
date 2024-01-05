package org.bigraphs.framework.simulation.modelchecking;

import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphDecoder;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphEncoder;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.modelchecking.predicates.PredicateChecker;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.bigraphs.framework.simulation.modelchecking.reactions.ReactionRuleSupplier;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * The algorithm implemented here to synthesize the "reaction graph" is adopted from [1].
 * It is a breadth-first simulation which also checks some given predicates.
 * <p>
 * Can be used to conduct reachability analysis.
 *
 * @author Dominik Grzelak
 * @see <a href="https://pure.itu.dk/portal/files/39500908/thesis_GianDavidPerrone.pdf">[1] G. Perrone, “Domain-Specific Modelling Languages in Bigraphs,” IT University of Copenhagen, 2013.</a>
 */
public class BreadthFirstStrategy<B extends Bigraph<? extends Signature<?>>> extends ModelCheckingStrategySupport<B> {
    private final Logger logger = LoggerFactory.getLogger(BreadthFirstStrategy.class);

    private PredicateChecker<B> predicateChecker;
    //    private BigraphCanonicalForm canonicalForm;

    public BreadthFirstStrategy(BigraphModelChecker<B> modelChecker) {
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
        it.uniud.mads.jlibbig.core.std.Bigraph encoded = new JLibBigBigraphEncoder().encode((PureBigraph) initialAgent);
        initialAgent = (B) new JLibBigBigraphDecoder().decode(encoded);
        String rootBfcs = modelChecker.acquireCanonicalForm().bfcs(initialAgent);
        workingQueue.add(initialAgent);
        AtomicInteger iterationCounter = new AtomicInteger(0);
        resetOccurrenceCounter();
        while (!workingQueue.isEmpty() && iterationCounter.get() < transitionOptions.getMaximumTransitions()) {
            Queue<MatchResult<B>> reactionResults = new ConcurrentLinkedQueue<>();

            // "Remove the first element w of the work queue Q."
            final B theAgent = workingQueue.remove();
            final String bfcfOfW = modelChecker.acquireCanonicalForm().bfcs(theAgent);
            // "For each reaction rule, find all matches m1 ...mn in w"
            ReactionRuleSupplier<B> inOrder = ReactionRuleSupplier.createInOrder(modelChecker.getReactiveSystem().getReactionRules());
            Stream<ReactionRule<B>> rrStream = Stream.generate(inOrder);
            if (options.isParallelRuleMatching()) {
                rrStream = rrStream.parallel();
            }
            rrStream
                    .limit(modelChecker.getReactiveSystem().getReactionRules().size())
                    .peek(x -> getListener().onCheckingReactionRule(x))
                    .flatMap(eachRule -> {
                        MatchIterable<BigraphMatch<B>> match = modelChecker.watch(() -> modelChecker.getMatcher().match(theAgent, eachRule));
                        for (BigraphMatch<B> eachMatch : match) {
                            //TODO check if conditional rule and call checkCondition()
                            increaseOccurrenceCounter();
                            B reaction = null;
                            if (theAgent.getSites().size() == 0 || eachMatch.getParameters().size() == 0) {
                                reaction = getReactiveSystem().buildGroundReaction(theAgent, eachMatch, eachRule);
                            } else {
                                reaction = getReactiveSystem().buildParametricReaction(theAgent, eachMatch, eachRule);
                            }

                            Optional.ofNullable(reaction).map(x -> {
                                return reactionResults.add(createMatchResult(eachRule, eachMatch, x, getOccurrenceCount()));
                            }).orElseGet(() -> {
                                getListener().onReactionIsNull();
                                return false; // return value not used
                            });
                        }
                        return reactionResults.stream();
                    })
//                    .collect(Collectors.toList());
//            for (MatchResult<B> matchResult : reactionResults) {
                    // iterate through all occurrences
                    .forEachOrdered(matchResult -> {
                        String bfcf = modelChecker.acquireCanonicalForm().bfcs(matchResult.getBigraph());
                        String reactionLbl = modelChecker.getReactiveSystem().getReactionRulesMap().inverse().get(matchResult.getReactionRule());
                        if (!modelChecker.getReactionGraph().containsBigraph(bfcf)) {
                            modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, matchResult.getBigraph(), bfcf, matchResult.getMatch().getRedex(), reactionLbl);
                            workingQueue.add(matchResult.getBigraph());
                            getListener().onUpdateReactionRuleApplies(theAgent, matchResult.getReactionRule(), matchResult.getMatch());
                            modelChecker.exportState(matchResult.getBigraph(), bfcf, String.valueOf(matchResult.getOccurrenceCount()));
                            iterationCounter.incrementAndGet();
                        } else {
                            modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, matchResult.getBigraph(), bfcf, matchResult.getMatch().getRedex(), reactionLbl);
                        }
//                modelChecker.exportGraph(modelChecker.getReactionGraph(), new File("graph.png"));
                    });

            // Predicate evaluation for the current state
            if (!predicateChecker.getPredicates().isEmpty()) {
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
                    // inform listeners
                    getListener().onAllPredicateMatched(theAgent, label);
                    // add info that predicates matched at the current state
                    // this info is later used in class mxReactionGraph for visualization purposes
                    modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(bfcfOfW).ifPresent(labeledNode -> {
                        predicateChecker.getPredicates().forEach(p -> {
                            modelChecker.getReactionGraph().addPredicateMatchToNode(labeledNode, p);
                        });
                    });
                } else { // not all predicates evaluated to true, so we have to check each return value individually
                    predicateChecker.getChecked().entrySet().forEach(eachPredicate -> {
                        // Predicate matching failed: compute counter-example trace from w back to the root
                        if (!eachPredicate.getValue()) {
                            try {
                                GraphPath<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> pathBetween = DijkstraShortestPath.findPathBetween(modelChecker.getReactionGraph().getGraph(),
                                        modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(bfcfOfW).get(),
                                        modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(rootBfcs).get()
                                );
                                logger.debug("Counter-example trace for predicate violation: start state={}, end state={}", pathBetween.getStartVertex(), pathBetween.getEndVertex());
                                // inform attached listeners
                                getListener().onPredicateViolated(theAgent, eachPredicate.getKey(), pathBetween);
                            } catch (Exception e) {
                                getListener().onError(e);
                            }
                        } else { // Predicate matching success
                            // inform attached listeners
                            getListener().onPredicateMatched(theAgent, eachPredicate.getKey());
                            if (eachPredicate.getKey() instanceof SubBigraphMatchPredicate) {
                                getListener().onSubPredicateMatched(
                                        theAgent,
                                        eachPredicate.getKey(),
                                        (B) ((SubBigraphMatchPredicate) eachPredicate.getKey()).getContextBigraphResult(),
                                        (B) ((SubBigraphMatchPredicate) eachPredicate.getKey()).getSubBigraphResult(),
                                        (B) ((SubBigraphMatchPredicate) eachPredicate.getKey()).getSubRedexResult(),
                                        (B) ((SubBigraphMatchPredicate) eachPredicate.getKey()).getSubBigraphParamResult()
                                );
                            }
                            // add info that predicate matched at the current state
                            // this info is later used in class mxReactionGraph for visualization purposes
                            modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(bfcfOfW).ifPresent(labeledNode -> {
                                modelChecker.getReactionGraph().addPredicateMatchToNode(labeledNode, eachPredicate.getKey());
                            });

                        }
                    });

                }
            }
            // "Repeat the procedure for the next item in the work queue, terminating successfully if the work queue is empty."
        }
        logger.debug("Total States: {}", iterationCounter.get());
        logger.debug("Total Transitions: {}", modelChecker.getReactionGraph().getGraph().edgeSet().size());
        logger.debug("Total Occurrences: {}", getOccurrenceCount());
    }
}
