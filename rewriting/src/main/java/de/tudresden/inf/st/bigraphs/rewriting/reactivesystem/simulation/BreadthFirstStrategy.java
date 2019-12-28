package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystemOptions;
import de.tudresden.inf.st.bigraphs.rewriting.encoding.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.rewriting.matching.MatchIterable;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.PredicateChecker;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.reactions.InOrderReactionRuleSupplier;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.reactions.ReactionRuleSupplier;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The algorithm implemented here to synthesize the "reaction graph" is adopted from [1].
 * It is a breadth-first simulation which also checks some given predicates.
 * <p>
 * Can be used to conduct reachability analysis.
 *
 * @author Dominik Grzelak
 * @see <a href="https://pure.itu.dk/portal/files/39500908/thesis_GianDavidPerrone.pdf">[1] G. Perrone, “Domain-Specific Modelling Languages in Bigraphs,” IT University of Copenhagen, 2013.</a>
 */
public class BreadthFirstStrategy<B extends Bigraph<? extends Signature<?>>> extends SimulationStrategySupport<B> {
    private Logger logger = LoggerFactory.getLogger(BreadthFirstStrategy.class);

    PredicateChecker<B> predicateChecker;
    ReactiveSystemOptions options;
    BigraphCanonicalForm canonicalForm;

    public BreadthFirstStrategy(BigraphModelChecker<B> modelChecker) {
        super(modelChecker);
    }

    /**
     * Compute the transition system of a bigraph with all added reaction rules so far.
     * <p>
     * //     * @param agent      the initial agent
     * //     * @param options    additional options
     * //     * @param predicates additional predicates to check at each states
     */
    public synchronized void synthesizeTransitionSystem() {
        final B initialAgent = modelChecker.getReactiveSystem().getAgent();

        this.predicateChecker = new PredicateChecker<>(modelChecker.getReactiveSystem().getPredicates());
        this.options = modelChecker.options;
        this.canonicalForm = modelChecker.canonicalForm;
        modelChecker.getReactionGraph().reset();
        final Queue<B> workingQueue = new ConcurrentLinkedDeque<>();
        String rootBfcs = canonicalForm.bfcs(initialAgent);
        workingQueue.add(initialAgent);
        AtomicInteger transitionCnt = new AtomicInteger(0);
        resetOccurrenceCounter();
        ReactiveSystemOptions.TransitionOptions transitionOptions = this.options.get(ReactiveSystemOptions.Options.TRANSITION);
        logger.debug("Maximum transitions={}", transitionOptions.getMaximumTransitions());
        while (!workingQueue.isEmpty() && transitionCnt.get() < transitionOptions.getMaximumTransitions()) {
            // "Remove the first element w of the work queue Q."
            final B theAgent = workingQueue.remove();
            // "For each reaction rule, find all matches m1 ...mn in w"
            final String bfcfOfW = canonicalForm.bfcs(theAgent);
            InOrderReactionRuleSupplier<B> inOrder = ReactionRuleSupplier.createInOrder(modelChecker.getReactiveSystem().getReactionRules());
//            Stream.generate(inOrder)
            modelChecker.getReactiveSystem().getReactionRules().stream()
                    .parallel()
                    .limit(modelChecker.getReactiveSystem().getReactionRules().size())
                    .peek(x -> modelChecker.reactiveSystemListener.onCheckingReactionRule(x))
                    .flatMap(eachRule -> {
                        MatchIterable<BigraphMatch<B>> match = modelChecker.watch(() -> modelChecker.getMatcher().match(theAgent, eachRule.getRedex()));
                        Iterator<BigraphMatch<B>> iterator = match.iterator();
                        MutableList<MatchResult<B>> reactionResults = Lists.mutable.empty();
                        while (iterator.hasNext()) {
                            increaseOccurrenceCounter();
                            BigraphMatch<B> next = iterator.next();
                            B reaction = null;
                            if (next.getParameters().size() == 0) {
                                reaction = modelChecker.buildGroundReaction(theAgent, next, eachRule);
                            } else {
                                //TODO: beachte instantiation map
                                reaction = modelChecker.buildParametricReaction(theAgent, next, eachRule);
                            }

                            reactionResults.add(createMatchResult(eachRule, next, reaction, getOccurrenceCount()));
                        }
                        transitionCnt.addAndGet(reactionResults.size());
                        return reactionResults.stream();
                    })
//                    .filter(x -> x.getBigraph() != null)
                    .forEachOrdered(reaction -> {
                        if (Objects.nonNull(reaction) && Objects.nonNull(reaction.getBigraph())) {
                            modelChecker.exportState(reaction.getBigraph(), String.valueOf(reaction.getOccurrenceCount()));
                            String bfcf = canonicalForm.bfcs(reaction.getBigraph());
                            String reactionLbl = modelChecker.getReactiveSystem().getReactionRulesMap().inverse().get(reaction.getReactionRule());
                            if (!modelChecker.getReactionGraph().containsBigraph(bfcf)) {
                                modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, reaction.getBigraph(), bfcf, reaction.getNext().getRedex(), reactionLbl);
                                workingQueue.add(reaction.getBigraph());
                            } else {
                                modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, reaction.getBigraph(), bfcf, reaction.getNext().getRedex(), reactionLbl);
                            }
                        } else {
                            modelChecker.reactiveSystemListener.onReactionIsNull();
                        }
                    });
            if (predicateChecker.getPredicates().size() > 0) {
                // "Check each property p ∈ P against w."
                //TODO evaluate in options what should happen here: violation or stop criteria?
                // this is connected to the predicates (changes its "intent", what they are used for)
                if (!predicateChecker.checkAll(theAgent)) {
                    // compute counter-example trace from w back to the root

                    try {
//                DijkstraShortestPath<String, String> dijkstraShortestPath = new DijkstraShortestPath<>(reactionGraph.getGraph());
                        GraphPath<String, ReactionGraph.LabeledEdge> pathBetween = DijkstraShortestPath.findPathBetween(modelChecker.getReactionGraph().getGraph(), bfcfOfW, rootBfcs);
                        predicateChecker.getChecked().entrySet().stream().forEach(eachPredciate -> {
                            if (!eachPredciate.getValue()) {
                                logger.debug("Counter-example trace for predicate violation: start state={}, end state={}", pathBetween.getStartVertex(), pathBetween.getEndVertex());
                                modelChecker.reactiveSystemListener.onPredicateViolated(theAgent, eachPredciate.getKey(), pathBetween);
                            } else {

                            }
                        });
                    } catch (Exception e) {
                        modelChecker.reactiveSystemListener.onError(e);
                    }
                } else {
                    modelChecker.reactiveSystemListener.onAllPredicateMatched(theAgent);
                }
            }
//            transitionCnt.incrementAndGet();
            // "Repeat the procedure for the next item in the work queue, terminating successfully if the work queue is empty."
        }
//        System.out.println("transitionCnt=" + transitionCnt);
        logger.debug("Total States/Transitions: {}", transitionCnt.get());
        logger.debug("Total Occurrences: {}", getOccurrenceCount());
    }
}
