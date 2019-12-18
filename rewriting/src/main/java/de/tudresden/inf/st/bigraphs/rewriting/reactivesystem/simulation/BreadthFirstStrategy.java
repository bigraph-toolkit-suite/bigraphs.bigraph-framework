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
import java.util.concurrent.atomic.AtomicInteger;
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
        int transitionCnt = 0;
        AtomicInteger CNT = new AtomicInteger(0);
        AtomicInteger CNT2 = new AtomicInteger(0);
        ReactiveSystemOptions.TransitionOptions transitionOptions = this.options.get(ReactiveSystemOptions.Options.TRANSITION);
        System.out.println("MAXIMUM: " + transitionOptions.getMaximumTransitions());
        while (!workingQueue.isEmpty() && transitionCnt < transitionOptions.getMaximumTransitions()) {
            // "Remove the first element w of the work queue Q."
            final B theAgent = workingQueue.remove();
            // "For each reaction rule, find all matches m1 ...mn in w"
            final String bfcfOfW = canonicalForm.bfcs(theAgent);
            InOrderReactionRuleSupplier<B> inOrder = ReactionRuleSupplier.<B>createInOrder(modelChecker.getReactiveSystem().getReactionRules());
            Stream.generate(inOrder)
                    .limit(modelChecker.getReactiveSystem().getReactionRules().size())
//                    .peek(x -> modelChecker.reactiveSystemListener.onCheckingReactionRule(x))
//                    .peek(x -> System.out.println("ABCDEFG"))
                    .forEachOrdered(eachRule -> {
//                        modelChecker.reactiveSystemListener.onCheckingReactionRule(eachRule);
                        MatchIterable<BigraphMatch<B>> match = modelChecker.watch(() -> modelChecker.getMatcher().match(theAgent, eachRule.getRedex()));
//                        MatchIterable<BigraphMatch<B>> match = modelChecker.getMatcher().match(theAgent, eachRule.getRedex()); //modelChecker.watch(() -> modelChecker.getMatcher().match(theAgent, eachRule.getRedex()));
//                        MatchIterable<BigraphMatch<B>> match = matcher.match(theAgent, eachRule.getRedex());
                        Iterator<BigraphMatch<B>> iterator = match.iterator();
                        CNT.incrementAndGet();
                        while (iterator.hasNext()) {
                            CNT2.incrementAndGet();
                            BigraphMatch<B> next = iterator.next();
//                            System.out.println("NEXT: " + next);
                            B reaction = null;
                            if (next.getParameters().size() == 0) {
                                reaction = modelChecker.buildGroundReaction(theAgent, next, eachRule);
                            } else {
                                //TODO: beachte instantiation map
                                reaction = modelChecker.buildParametricReaction(theAgent, next, eachRule);
                            }
                            assert Objects.nonNull(reaction);
                            if (Objects.nonNull(reaction)) {
                                String bfcf = canonicalForm.bfcs(reaction);
                                String reactionLbl = modelChecker.getReactiveSystem().getReactionRulesMap().inverse().get(eachRule);
                                if (!modelChecker.getReactionGraph().containsBigraph(bfcf)) {
                                    modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, reaction, bfcf, next.getRedex(), reactionLbl);
                                    workingQueue.add(reaction);
                                } else {
                                    modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, reaction, bfcf, next.getRedex(), reactionLbl);
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
//                System.out.println("Matched");
                    modelChecker.reactiveSystemListener.onAllPredicateMatched(theAgent);
                }
            }

            // "Repeat the procedure for the next item in the work queue, terminating successfully if the work queue is empty."
            transitionCnt++;
        }
        System.out.println("transitionCnt=" + transitionCnt);
        System.out.println("Cnt=" + CNT.get());
        System.out.println("Cnt2=" + CNT2.get());

//        modelChecker.prepareOutput();
    }
}
