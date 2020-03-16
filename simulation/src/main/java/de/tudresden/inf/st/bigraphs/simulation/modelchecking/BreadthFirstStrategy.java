package de.tudresden.inf.st.bigraphs.simulation.modelchecking;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.simulation.encoding.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.simulation.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.simulation.matching.MatchIterable;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.predicates.PredicateChecker;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.predicates.ReactiveSystemPredicates;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.reactions.InOrderReactionRuleSupplier;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.reactions.ReactionRuleSupplier;
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
public class BreadthFirstStrategy<B extends Bigraph<? extends Signature<?>>> extends ModelCheckingStrategySupport<B> {
    private Logger logger = LoggerFactory.getLogger(BreadthFirstStrategy.class);

    private PredicateChecker<B> predicateChecker;
    private ModelCheckingOptions options;
    private BigraphCanonicalForm canonicalForm;

    public BreadthFirstStrategy(BigraphModelChecker<B> modelChecker) {
        super(modelChecker);
    }

    /**
     * Compute the transition system of a bigraph with all added reaction rules so far.
     */
    public synchronized void synthesizeTransitionSystem() {
        final B initialAgent = modelChecker.getReactiveSystem().getAgent();

        this.predicateChecker = new PredicateChecker<>(modelChecker.getReactiveSystem().getPredicates());
        this.options = modelChecker.options;
        this.canonicalForm = modelChecker.canonicalForm;
        if (Objects.nonNull(options.get(ModelCheckingOptions.Options.EXPORT))) {
            ModelCheckingOptions.ExportOptions opts = options.get(ModelCheckingOptions.Options.EXPORT);
            modelChecker.getReactionGraph().setCanonicalNodeLabel(opts.getPrintCanonicalStateLabel());
        }
        modelChecker.getReactionGraph().reset();
        final Queue<B> workingQueue = new ConcurrentLinkedDeque<>();
        String rootBfcs = canonicalForm.bfcs(initialAgent);
        workingQueue.add(initialAgent);
        AtomicInteger transitionCnt = new AtomicInteger(0);
        resetOccurrenceCounter();
        ModelCheckingOptions.TransitionOptions transitionOptions = this.options.get(ModelCheckingOptions.Options.TRANSITION);
        logger.debug("Maximum transitions={}", transitionOptions.getMaximumTransitions());
        while (!workingQueue.isEmpty() && transitionCnt.get() < transitionOptions.getMaximumTransitions()) {
            // "Remove the first element w of the work queue Q."
            final B theAgent = workingQueue.remove();
            // "For each reaction rule, find all matches m1 ...mn in w"
            final String bfcfOfW = canonicalForm.bfcs(theAgent);
            InOrderReactionRuleSupplier<B> inOrder = ReactionRuleSupplier.createInOrder(modelChecker.getReactiveSystem().getReactionRules());
//            Stream.generate(inOrder)
//                    .limit(modelChecker.getReactiveSystem().getReactionRules().size())
            modelChecker.getReactiveSystem().getReactionRules().stream()
                    .parallel()
                    .peek(x -> getListener().onCheckingReactionRule(x))
                    .flatMap(eachRule -> {
                        MatchIterable<BigraphMatch<B>> match = modelChecker.watch(() -> modelChecker.getMatcher().match(theAgent, eachRule.getRedex()));
                        Iterator<BigraphMatch<B>> iterator = match.iterator();
                        MutableList<MatchResult<B>> reactionResults = Lists.mutable.empty();
                        while (iterator.hasNext()) {
                            increaseOccurrenceCounter();
                            BigraphMatch<B> next = iterator.next();
                            B reaction = null;
                            if (next.getParameters().size() == 0) {
                                reaction = getReactiveSystem().buildGroundReaction(theAgent, next, eachRule);
                            } else {
                                //TODO: beachte instantiation map
                                reaction = getReactiveSystem().buildParametricReaction(theAgent, next, eachRule);
                            }

                            Optional.ofNullable(reaction)
                                    .map(x -> {
                                        getListener().onUpdateReactionRuleApplies(theAgent, eachRule, next);
                                        return reactionResults.add(createMatchResult(eachRule, next, x, getOccurrenceCount()));
                                    })
                                    .orElseGet(() -> {
                                        getListener().onReactionIsNull();
                                        return false;
                                    });
                        }
                        transitionCnt.addAndGet(reactionResults.size());
                        return reactionResults.stream();
                    })
                    .forEachOrdered(reaction -> {
                        String bfcf = canonicalForm.bfcs(reaction.getBigraph());
                        String reactionLbl = modelChecker.getReactiveSystem().getReactionRulesMap().inverse().get(reaction.getReactionRule());
                        if (!modelChecker.getReactionGraph().containsBigraph(bfcf)) {
                            modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, reaction.getBigraph(), bfcf, reaction.getNext().getRedex(), reactionLbl);
                            workingQueue.add(reaction.getBigraph());
                            modelChecker.exportState(reaction.getBigraph(), bfcf, String.valueOf(reaction.getOccurrenceCount()));
                        } else {
                            modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, reaction.getBigraph(), bfcf, reaction.getNext().getRedex(), reactionLbl);
                        }
                    });
            if (predicateChecker.getPredicates().size() > 0) {
                // "Check each property p ∈ P against w."
                //TODO evaluate in "reaction graph spec" what should happen here: violation or stop criteria?
                // this is connected to the predicates (changes its "intent", what they are used for)
                if (predicateChecker.checkAll(theAgent)) {
                    String label = "";
                    if (modelChecker.reactionGraph.getLabeledNodeByCanonicalForm(bfcfOfW).isPresent() &&
                            modelChecker.reactionGraph.getLabeledNodeByCanonicalForm(bfcfOfW).get() instanceof ReactionGraph.DefaultLabeledNode) {
                        label = modelChecker.reactionGraph.getLabeledNodeByCanonicalForm(bfcfOfW).get().getLabel();
                    } else {
                        label = String.format("state-%s", String.valueOf(modelChecker.reactionGraph.getGraph().vertexSet().size()));
                    }
                    getListener().onAllPredicateMatched(theAgent, label);
                    Optional<ReactionGraph.LabeledNode> node = modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(bfcfOfW);
                    node.ifPresent(labeledNode -> predicateChecker.getPredicates().forEach(p -> {
                        modelChecker.getReactionGraph().addPredicateMatchToNode(labeledNode, p);
                    }));
                } else {
                    // compute counter-example trace from w back to the root
                    try {
//                DijkstraShortestPath<String, String> dijkstraShortestPath = new DijkstraShortestPath<>(reactionGraph.getGraph());
                        GraphPath<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> pathBetween = DijkstraShortestPath.findPathBetween(modelChecker.getReactionGraph().getGraph(),
                                modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(bfcfOfW).get(),
                                modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(rootBfcs).get()
                        );
                        predicateChecker.getChecked().entrySet().stream().forEach(eachPredciate -> {
                            if (!eachPredciate.getValue()) {
                                logger.debug("Counter-example trace for predicate violation: start state={}, end state={}", pathBetween.getStartVertex(), pathBetween.getEndVertex());
                                getListener().onPredicateViolated(theAgent, eachPredciate.getKey(), pathBetween);
                            } else {
                                Optional<ReactionGraph.LabeledNode> node = modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(bfcfOfW);
                                if (node.isPresent()) {
                                    modelChecker.getReactionGraph().addPredicateMatchToNode(node.get(), (ReactiveSystemPredicates) eachPredciate);
                                }
                                getListener().onPredicateMatched(theAgent, eachPredciate.getKey());
                            }
                        });
                    } catch (Exception e) {
                        getListener().onError(e);
                    }
                }
            }
            // "Repeat the procedure for the next item in the work queue, terminating successfully if the work queue is empty."
        }
        logger.debug("Total States/Transitions: {}", transitionCnt.get());
        logger.debug("Total Occurrences: {}", getOccurrenceCount());
    }
}
