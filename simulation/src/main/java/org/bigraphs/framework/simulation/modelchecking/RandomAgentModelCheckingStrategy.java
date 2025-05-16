package org.bigraphs.framework.simulation.modelchecking;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.simulation.encoding.BigraphCanonicalForm;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.modelchecking.reactions.InOrderReactionRuleSupplier;
import org.bigraphs.framework.simulation.modelchecking.reactions.RandomAgentMatchSupplier;
import org.bigraphs.framework.simulation.modelchecking.reactions.AbstractReactionRuleSupplier;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Random state-space traversal without cycle-checking and predicate evaluation.
 *
 * @author Dominik Grzelak
 */
public class RandomAgentModelCheckingStrategy<B extends Bigraph<? extends Signature<?>>> extends ModelCheckingStrategySupport<B> {

    public RandomAgentModelCheckingStrategy(BigraphModelChecker<B> modelChecker) {
        super(modelChecker);
    }

    @Override
    public void synthesizeTransitionSystem() {
        final Queue<B> workingQueue = new ConcurrentLinkedDeque<>();
        final ModelCheckingOptions options = modelChecker.options;
        final BigraphCanonicalForm canonicalForm = modelChecker.canonicalForm;
        final ModelCheckingOptions.TransitionOptions transitionOptions = options.get(ModelCheckingOptions.Options.TRANSITION);
        if (Objects.nonNull(options.get(ModelCheckingOptions.Options.EXPORT))) {
            ModelCheckingOptions.ExportOptions opts = options.get(ModelCheckingOptions.Options.EXPORT);
            modelChecker.getReactionGraph().setCanonicalNodeLabel(opts.getPrintCanonicalStateLabel());
        }
        int transitionCnt = 0;

        modelChecker.getReactionGraph().reset();
        workingQueue.add(modelChecker.getReactiveSystem().getAgent());
        resetOccurrenceCounter();
        while (!workingQueue.isEmpty() && transitionCnt < transitionOptions.getMaximumTransitions()) {
            final B theAgent = workingQueue.remove();
            final String bfcfOfW = canonicalForm.bfcs(theAgent);
            final InOrderReactionRuleSupplier<B> inOrder = AbstractReactionRuleSupplier.<B>createInOrder(modelChecker.getReactiveSystem().getReactionRules());
            MutableList<B> rewrittenAgents = Lists.mutable.empty();
//            Stream.generate(inOrder)
//                    .limit(modelChecker.getReactiveSystem().getReactionRules().size())
            modelChecker.getReactiveSystem().getReactionRules().stream()
                    .parallel()
                    .forEachOrdered(eachRule -> {
                        modelChecker.reactiveSystemListener.onCheckingReactionRule(eachRule);
                        MatchIterable<BigraphMatch<B>> match = modelChecker.watch(() -> modelChecker.getMatcher().match(theAgent, eachRule));
                        Iterator<BigraphMatch<B>> iterator = match.iterator();
                        while (iterator.hasNext()) {
                            occurrenceCounter++;
                            BigraphMatch<B> next = iterator.next();
                            B reaction = null;
                            if (next.getParameters().size() == 0) {
                                reaction = getReactiveSystem().buildGroundReaction(theAgent, next, eachRule);
                            } else {
                                reaction = getReactiveSystem().buildParametricReaction(theAgent, next, eachRule);
                            }
                            if (Objects.nonNull(reaction)) {
                                String bfcf = canonicalForm.bfcs(reaction);
                                String reactionLbl = modelChecker.getReactiveSystem().getReactionRulesMap().inverse().get(eachRule);
                                rewrittenAgents.add(reaction);
                                MatchResult<B> matchResult = createMatchResult(eachRule, next, reaction, bfcfOfW, getOccurrenceCount());
                                if (!modelChecker.getReactionGraph().containsBigraph(bfcf)) {
//                                    modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, reaction, bfcf, next.getRedex(), reactionLbl);
                                    modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, reaction, bfcf, matchResult, reactionLbl);
                                } else {
//                                    modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, reaction, bfcf, next.getRedex(), reactionLbl);
                                    modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, reaction, bfcf, matchResult, reactionLbl);
                                }
                            } else {
                                modelChecker.reactiveSystemListener.onReactionIsNull();
                            }
                        }
                    });
            if (rewrittenAgents.size() > 0) {
                RandomAgentMatchSupplier<B> randomSupplier = AbstractReactionRuleSupplier.createRandom(rewrittenAgents);
                workingQueue.add(randomSupplier.get());
            }

            // "Repeat the procedure for the next item in the work queue, terminating successfully if the work queue is empty."
            transitionCnt++;
        }

        logger.debug("Total Transitions: {}", transitionCnt);
        logger.debug("Total Occurrences: {}", getOccurrenceCount());
    }

    // UNUSED METHODS since the method #synthesizeTransitionSystem() is overridden

    @Override
    protected Collection<B> createWorklist() {
        return List.of();
    }

    @Override
    protected B removeNext(Collection<B> worklist) {
        return null;
    }

    @Override
    protected void addToWorklist(Collection<B> worklist, B bigraph) {

    }
}
