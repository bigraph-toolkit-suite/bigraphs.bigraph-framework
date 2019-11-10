package de.tudresden.inf.st.bigraphs.rewriting.matching.pure;

import com.google.common.base.Stopwatch;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.matching.AbstractBigraphMatcher;
import de.tudresden.inf.st.bigraphs.rewriting.matching.MatchIterable;

import java.util.concurrent.TimeUnit;

/**
 * A matcher for {@link PureBigraph}s.
 * <p>
 * Creates the correct matching engine and iterator to return the matches.
 *
 * @author Dominik Grzelak
 */
public class PureBigraphMatcher extends AbstractBigraphMatcher<PureBigraph> {

    public PureBigraphMatcher() {
        super();
    }

    @SuppressWarnings("unchecked")
    public MatchIterable<PureBigraphParametricMatch> match(PureBigraph agent, PureBigraph redex) {
        super.agent = agent;
        super.redex = redex;
        PureBigraphMatchingEngine matchingEngine = instantiateEngine();
        Stopwatch timer0 = Stopwatch.createStarted();
        MatchIterable<PureBigraphParametricMatch> bigraphMatches = new MatchIterable<>(new PureMatchIteratorImpl(matchingEngine));
        long elapsed0 = timer0.stop().elapsed(TimeUnit.NANOSECONDS);
        System.out.println("INITTIME2 (millisecs) " + (elapsed0 / 1e+6f));
        return bigraphMatches;
    }

    @Override
    public PureBigraphMatchingEngine instantiateEngine() {
        return new PureBigraphMatchingEngine(this.agent, this.redex);
    }

}
