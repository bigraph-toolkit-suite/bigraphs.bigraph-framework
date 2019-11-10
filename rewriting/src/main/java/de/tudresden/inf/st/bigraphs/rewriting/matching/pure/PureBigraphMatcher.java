package de.tudresden.inf.st.bigraphs.rewriting.matching.pure;

import com.google.common.base.Stopwatch;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.matching.AbstractBigraphMatcher;
import de.tudresden.inf.st.bigraphs.rewriting.matching.MatchIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * A matcher for {@link PureBigraph}s.
 * <p>
 * Creates the correct matching engine and iterator to return the matches.
 *
 * @author Dominik Grzelak
 */
public class PureBigraphMatcher extends AbstractBigraphMatcher<PureBigraph> {
    Logger logger = LoggerFactory.getLogger(PureBigraphMatcher.class);

    public PureBigraphMatcher() {
        super();
    }

    @SuppressWarnings("unchecked")
    public MatchIterable<PureBigraphParametricMatch> match(PureBigraph agent, PureBigraph redex) {
        super.agent = agent;
        super.redex = redex;
        PureBigraphMatchingEngine matchingEngine = instantiateEngine();

        Stopwatch timer0 = logger.isDebugEnabled() ? Stopwatch.createStarted() : null;
        MatchIterable<PureBigraphParametricMatch> bigraphMatches = new MatchIterable<>(new PureMatchIteratorImpl(matchingEngine));
        if (logger.isDebugEnabled()) {
            long elapsed0 = timer0.stop().elapsed(TimeUnit.NANOSECONDS);
            logger.debug("Complete Matching Time: {} ms", (elapsed0 / 1e+6f));
        }
        return bigraphMatches;
    }

    @Override
    public PureBigraphMatchingEngine instantiateEngine() {
        return new PureBigraphMatchingEngine(this.agent, this.redex);
    }

}
