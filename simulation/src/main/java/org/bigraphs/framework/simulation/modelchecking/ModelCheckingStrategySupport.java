package org.bigraphs.framework.simulation.modelchecking;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.BMatchResult;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystem;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for supporting model checking strategy implementations.
 * Provides some useful method to keep subclasses simple.
 *
 * @author Dominik Grzelak
 */
public abstract class ModelCheckingStrategySupport<B extends Bigraph<? extends Signature<?>>> implements ModelCheckingStrategy<B> {
    private Logger logger = LoggerFactory.getLogger(ModelCheckingStrategySupport.class);

    protected int occurrenceCounter = 0;

    protected BigraphModelChecker<B> modelChecker;

    public ModelCheckingStrategySupport(BigraphModelChecker<B> modelChecker) {
        this.modelChecker = modelChecker;
    }

    protected void increaseOccurrenceCounter() {
        occurrenceCounter++;
    }

    protected void resetOccurrenceCounter() {
        occurrenceCounter = 0;
    }

    int getOccurrenceCount() {
        return occurrenceCounter;
    }

    MatchResult<B> createMatchResult(ReactionRule<B> reactionRule, BigraphMatch<B> next, B bigraph, int occurrenceCount) {
        return new MatchResult<>(reactionRule, next, bigraph, "", occurrenceCount);
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

    ReactiveSystem<B> getReactiveSystem() {
        return modelChecker.getReactiveSystem();
    }

    BigraphModelChecker.ReactiveSystemListener<B> getListener() {
        return modelChecker.reactiveSystemListener;
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
