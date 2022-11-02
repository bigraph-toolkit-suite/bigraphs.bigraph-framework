package de.tudresden.inf.st.bigraphs.simulation.modelchecking;

import de.tudresden.inf.st.bigraphs.converter.jlibbig.JLibBigBigraphDecoder;
import de.tudresden.inf.st.bigraphs.converter.jlibbig.JLibBigBigraphEncoder;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionGraph;
import de.tudresden.inf.st.bigraphs.simulation.encoding.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.BigraphMatch;
import de.tudresden.inf.st.bigraphs.simulation.matching.MatchIterable;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.predicates.PredicateChecker;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.reactions.InOrderReactionRuleSupplier;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.reactions.ReactionRuleSupplier;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
//        this.canonicalForm = modelChecker.canonicalForm;
        if (Objects.nonNull(options.get(ModelCheckingOptions.Options.EXPORT))) {
            ModelCheckingOptions.ExportOptions opts = options.get(ModelCheckingOptions.Options.EXPORT);
            modelChecker.getReactionGraph().setCanonicalNodeLabel(opts.getPrintCanonicalStateLabel());
        }
        modelChecker.getReactionGraph().reset();
        final Queue<B> workingQueue = new ConcurrentLinkedDeque<>();

        B initialAgent = modelChecker.getReactiveSystem().getAgent();
        it.uniud.mads.jlibbig.core.std.Bigraph encoded = new JLibBigBigraphEncoder().encode((PureBigraph) initialAgent);
        initialAgent = (B) new JLibBigBigraphDecoder().decode(encoded);
        String rootBfcs = modelChecker.acquireCanonicalForm().bfcs(initialAgent);
        workingQueue.add(initialAgent);
        AtomicInteger iterationCounter = new AtomicInteger(0);
        resetOccurrenceCounter();
        ModelCheckingOptions.TransitionOptions transitionOptions = options.get(ModelCheckingOptions.Options.TRANSITION);
        logger.debug("Maximum transitions={}", transitionOptions.getMaximumTransitions());
        while (!workingQueue.isEmpty() && iterationCounter.get() < transitionOptions.getMaximumTransitions()) {
            // "Remove the first element w of the work queue Q."
            final B theAgent = workingQueue.remove();
            final String bfcfOfW = modelChecker.acquireCanonicalForm().bfcs(theAgent);
            // "For each reaction rule, find all matches m1 ...mn in w"
            InOrderReactionRuleSupplier<B> inOrder = ReactionRuleSupplier.createInOrder(modelChecker.getReactiveSystem().getReactionRules());
//            Stream.generate(inOrder)
//                    .limit(modelChecker.getReactiveSystem().getReactionRules().size())
            Queue<MatchResult<B>> reactionResults = new ConcurrentLinkedQueue<>();
            modelChecker.getReactiveSystem().getReactionRules().stream()
                    .parallel()
                    .peek(x -> getListener().onCheckingReactionRule(x))
                    .flatMap(eachRule -> {
                        MatchIterable<BigraphMatch<B>> match = modelChecker.watch(() -> modelChecker.getMatcher().match(theAgent, eachRule));
                        //                        MutableList<MatchResult<B>> reactionResults = Lists.mutable.empty();
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
//                                        getListener().onUpdateReactionRuleApplies(theAgent, eachRule, eachMatch);
                                        return reactionResults.add(createMatchResult(eachRule, eachMatch, x, getOccurrenceCount()));
                                    })
                                    .orElseGet(() -> {
                                        getListener().onReactionIsNull();
                                        return false;
                                    });
                        }
                        return reactionResults.stream();
                    }) //;
                    .forEachOrdered(matchResult -> {
//                    .collect(Collectors.toList());

//            for (MatchResult<B> matchResult : reactionResults) {
//            reactionResults.stream().forEach(matchResult -> {
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

//                DijkstraShortestPath<String, String> dijkstraShortestPath = new DijkstraShortestPath<>(reactionGraph.getGraph());
                    predicateChecker.getChecked().entrySet().forEach(eachPredicate -> {
                        if (!eachPredicate.getValue()) {
                            try {
                                GraphPath<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> pathBetween = DijkstraShortestPath.findPathBetween(modelChecker.getReactionGraph().getGraph(),
                                        modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(bfcfOfW).get(),
                                        modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(rootBfcs).get()
                                );
                                logger.debug("Counter-example trace for predicate violation: start state={}, end state={}", pathBetween.getStartVertex(), pathBetween.getEndVertex());
                                getListener().onPredicateViolated(theAgent, eachPredicate.getKey(), pathBetween);
                            } catch (Exception e) {
                                getListener().onError(e);
                            }
                        } else {
                            Optional<ReactionGraph.LabeledNode> node = modelChecker.getReactionGraph().getLabeledNodeByCanonicalForm(bfcfOfW);
                            node.ifPresent(labeledNode -> {
                                modelChecker.getReactionGraph().addPredicateMatchToNode(labeledNode, eachPredicate.getKey());
                            });
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
                        }
                    });

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
