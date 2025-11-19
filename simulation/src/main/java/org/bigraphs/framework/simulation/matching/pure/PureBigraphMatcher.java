/*
 * Copyright (c) 2019-2024 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.simulation.matching.pure;

import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Override
    public <M extends BigraphMatch<PureBigraph>> MatchIterable<M> match(PureBigraph agent, ReactionRule<PureBigraph> rule) {
                super.agent = agent;
        super.rule = rule;
        super.redex = rule.getRedex();
        PureBigraphMatchingEngine matchingEngine = instantiateEngine();

        Stopwatch timer0 = logger.isDebugEnabled() ? Stopwatch.createStarted() : null;
        MatchIterable<PureBigraphParametricMatch> bigraphMatches = new MatchIterable<>(new PureMatchIteratorImpl(matchingEngine));
        if (logger.isDebugEnabled()) {
            long elapsed0 = timer0.stop().elapsed(TimeUnit.NANOSECONDS);
            logger.debug("Complete Matching Time: {} (ms)", (elapsed0 / 1e+6f));
        }
        return (MatchIterable<M>) bigraphMatches;
    }

//    @SuppressWarnings("unchecked")
//    public MatchIterable<PureBigraphParametricMatch> match(PureBigraph agent, ReactionRule<PureBigraph> rule) {
//        super.agent = agent;
//        super.rule = rule;
//        super.redex = rule.getRedex();
//        PureBigraphMatchingEngine matchingEngine = instantiateEngine();
//
//        Stopwatch timer0 = logger.isDebugEnabled() ? Stopwatch.createStarted() : null;
//        MatchIterable<PureBigraphParametricMatch> bigraphMatches = new MatchIterable<>(new PureMatchIteratorImpl(matchingEngine));
//        if (logger.isDebugEnabled()) {
//            long elapsed0 = timer0.stop().elapsed(TimeUnit.NANOSECONDS);
//            logger.debug("Complete Matching Time: {} (ms)", (elapsed0 / 1e+6f));
//        }
//        return bigraphMatches;
//    }

    @Override
    public PureBigraphMatchingEngine instantiateEngine() {
        return new PureBigraphMatchingEngine(this.agent, this.rule);
    }

}
