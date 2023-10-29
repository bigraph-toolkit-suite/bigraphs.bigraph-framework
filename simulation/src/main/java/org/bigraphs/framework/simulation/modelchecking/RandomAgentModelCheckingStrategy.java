package org.bigraphs.framework.simulation.modelchecking;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import org.bigraphs.framework.simulation.encoding.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.modelchecking.reactions.InOrderReactionRuleSupplier;
import org.bigraphs.framework.simulation.modelchecking.reactions.RandomAgentMatchSupplier;
import org.bigraphs.framework.simulation.modelchecking.reactions.ReactionRuleSupplier;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author Dominik Grzelak
 */
public class RandomAgentModelCheckingStrategy<B extends Bigraph<? extends Signature<?>>> extends ModelCheckingStrategySupport<B> {
    private Logger logger = LoggerFactory.getLogger(RandomAgentModelCheckingStrategy.class);

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
            final InOrderReactionRuleSupplier<B> inOrder = ReactionRuleSupplier.<B>createInOrder(modelChecker.getReactiveSystem().getReactionRules());
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
                            increaseOccurrenceCounter();
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
                                if (!modelChecker.getReactionGraph().containsBigraph(bfcf)) {
                                    modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, reaction, bfcf, next.getRedex(), reactionLbl);
                                } else {
                                    modelChecker.getReactionGraph().addEdge(theAgent, bfcfOfW, reaction, bfcf, next.getRedex(), reactionLbl);
                                }
                            } else {
                                modelChecker.reactiveSystemListener.onReactionIsNull();
                            }
                        }
                    });
            if (rewrittenAgents.size() > 0) {
                RandomAgentMatchSupplier<B> randomSupplier = ReactionRuleSupplier.createRandom(rewrittenAgents);
                workingQueue.add(randomSupplier.get());
            }

            // "Repeat the procedure for the next item in the work queue, terminating successfully if the work queue is empty."
            transitionCnt++;
        }

        logger.debug("Total Transitions: {}", transitionCnt);
        logger.debug("Total Occurrences: {}", getOccurrenceCount());
    }
}
