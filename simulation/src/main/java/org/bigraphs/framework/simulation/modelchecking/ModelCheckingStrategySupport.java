package org.bigraphs.framework.simulation.modelchecking;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionRule;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactiveSystem;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.BigraphMatch;
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
        return new MatchResult<>(reactionRule, next, bigraph, occurrenceCount);
    }

    ReactiveSystem<B> getReactiveSystem() {
        return modelChecker.getReactiveSystem();
    }

    BigraphModelChecker.ReactiveSystemListener<B> getListener() {
        return modelChecker.reactiveSystemListener;
    }

    static class MatchResult<B extends Bigraph<? extends Signature<?>>> {
        private final ReactionRule<B> reactionRule;
        private final BigraphMatch<B> next;
        private final B bigraph;
        private final int occurrenceCount;

        private MatchResult(ReactionRule<B> reactionRule, BigraphMatch<B> next, B bigraph, int occurrenceCount) {
            this.reactionRule = reactionRule;
            this.next = next;
            this.bigraph = bigraph;
            this.occurrenceCount = occurrenceCount;
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
    }
}
