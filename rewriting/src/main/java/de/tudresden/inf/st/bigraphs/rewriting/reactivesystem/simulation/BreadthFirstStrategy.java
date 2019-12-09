package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystemOptions;
import de.tudresden.inf.st.bigraphs.rewriting.encoding.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.rewriting.matching.MatchIterable;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.PredicateChecker;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.ReactionGraph;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.reactions.InOrderReactionRuleSupplier;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.reactions.ReactionRuleSupplier;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Stream;

//TODO: logger-based/export based breadthfirststrategy...via decorator

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
    B initialAgent;
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
        this.initialAgent = modelChecker.reactiveSystem.getAgent();

        this.predicateChecker = new PredicateChecker<>(modelChecker.reactiveSystem.getPredicates());
        this.options = modelChecker.options;
//        this.reactionGraph.reset();
        this.canonicalForm = modelChecker.canonicalForm;
        modelChecker.reactionGraph.reset();
        final Queue<B> workingQueue = new ConcurrentLinkedDeque<>();
        String rootBfcs = canonicalForm.bfcs(this.initialAgent);
        workingQueue.add(this.initialAgent);
        int transitionCnt = 0;
        ReactiveSystemOptions.TransitionOptions transitionOptions = this.options.get(ReactiveSystemOptions.Options.TRANSITION);
        while (!workingQueue.isEmpty() && transitionCnt < transitionOptions.getMaximumTransitions()) {
            // "Remove the first element w of the work queue Q."
            final B theAgent = workingQueue.remove();
            // "For each reaction rule, find all matches m1 ...mn in w"
            String bfcfOfW = canonicalForm.bfcs(theAgent);
            //TODO: generate appropriate supplier for the given option
            InOrderReactionRuleSupplier<B> inOrder = ReactionRuleSupplier.<B>createInOrder(modelChecker.reactiveSystem.getReactionRules());
            Stream.generate(inOrder)
                    .limit(modelChecker.reactiveSystem.getReactionRules().size())
                    .peek(x -> modelChecker.reactiveSystemListener.onCheckingReactionRule(x))
                    .forEachOrdered(eachRule -> {
                        MatchIterable<BigraphMatch<B>> match = modelChecker.watch(() -> modelChecker.matcher.match(theAgent, eachRule.getRedex()));
//                        MatchIterable<BigraphMatch<B>> match = matcher.match(theAgent, eachRule.getRedex());
                        Iterator<BigraphMatch<B>> iterator = match.iterator();
                        while (iterator.hasNext()) {
                            BigraphMatch<B> next = iterator.next();
//                            System.out.println("NEXT: " + next);
                            B reaction = null;
                            if (next.getParameters().size() == 0) {
                                reaction = modelChecker.buildGroundReaction(theAgent, next, eachRule);
                            } else {
                                //TODO: beachte instantiation map
                                reaction = modelChecker.buildParametricReaction(theAgent, next, eachRule);
                            }
//                            assert Objects.nonNull(reaction);
                            if (Objects.nonNull(reaction)) {
                                String bfcf = canonicalForm.bfcs(reaction);
                                String reactionLbl = modelChecker.reactiveSystem.getReactionRulesMap().inverse().get(eachRule);
                                if (!modelChecker.reactionGraph.containsBigraph(bfcf)) {
                                    modelChecker.reactionGraph.addEdge(theAgent, bfcfOfW, reaction, bfcf, next.getRedex(), reactionLbl);
                                    workingQueue.add(reaction);
                                } else {
                                    modelChecker.reactionGraph.addEdge(theAgent, bfcfOfW, reaction, bfcf, next.getRedex(), reactionLbl);
                                }
                            } else {
                                modelChecker.reactiveSystemListener.onReactionIsNull();
                            }
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
                        GraphPath<String, ReactionGraph.LabeledEdge> pathBetween = DijkstraShortestPath.findPathBetween(modelChecker.reactionGraph.getGraph(), bfcfOfW, rootBfcs);
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
//                System.out.println("Matched");
                    modelChecker.reactiveSystemListener.onAllPredicateMatched(theAgent);
                }
            }

            // "Repeat the procedure for the next item in the work queue, terminating successfully if the work queue is empty."
            transitionCnt++;
        }

        modelChecker.prepareOutput();
    }
}
