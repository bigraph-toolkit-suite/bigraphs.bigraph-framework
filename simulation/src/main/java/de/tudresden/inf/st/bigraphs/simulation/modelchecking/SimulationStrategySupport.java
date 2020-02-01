package de.tudresden.inf.st.bigraphs.simulation.modelchecking;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.simulation.ReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.matching.BigraphMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for supporting other simulation strategy implementations.
 * Provides some useful method to keep sub-classes simple.
 *
 * @author Dominik Grzelak
 */
public abstract class SimulationStrategySupport<B extends Bigraph<? extends Signature<?>>> implements SimulationStrategy<B> {
    private Logger logger = LoggerFactory.getLogger(SimulationStrategySupport.class);

    protected int occurrenceCounter = 0;

    protected BigraphModelChecker<B> modelChecker;

    public SimulationStrategySupport(BigraphModelChecker<B> modelChecker) {
        this.modelChecker = modelChecker;
    }

    protected void increaseOccurrenceCounter() {
        occurrenceCounter++;
    }

    protected void resetOccurrenceCounter() {
        occurrenceCounter = 0;
    }

    public int getOccurrenceCount() {
        return occurrenceCounter;
    }

    public MatchResult<B> createMatchResult(ReactionRule<B> reactionRule, BigraphMatch<B> next, B bigraph, int occurrenceCount) {
        return new MatchResult<>(reactionRule, next, bigraph, occurrenceCount);
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

        public BigraphMatch<B> getNext() {
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
