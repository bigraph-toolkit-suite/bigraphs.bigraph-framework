/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
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
 * PureBigraphMatcher is a concrete implementation of AbstractBigraphMatcher specialized for handling
 * pure bigraphs. It is responsible for executing matching operations between a pure bigraph agent
 * and a reaction rule. The matcher uses a PureBigraphMatchingEngine for processing the matches.
 *
 * @author Dominik Grzelak
 */
public class PureBigraphMatcher extends AbstractBigraphMatcher<PureBigraph> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PureBigraphMatcher.class);

    public PureBigraphMatcher() {
        super();
    }

    @Override
    public PureBigraphMatchingEngine instantiateEngine() {
        return new PureBigraphMatchingEngine(this.agent, this.rule);
    }

    /**
     * Finds all matches of a specified reaction rule within a given pure bigraph.
     *
     * @param <M>   the type of the match.
     * @param agent the pure bigraph in which to search for matches.
     * @param rule  the reaction rule containing the redex to be matched against the agent.
     * @return an iterable collection of matches of type M found in the given pure bigraph agent.
     */
    public <M extends BigraphMatch<PureBigraph>> MatchIterable<M> matchAll(PureBigraph agent, ReactionRule<PureBigraph> rule) {
        return matchAllInternal(agent, rule, false);
    }

    /**
     * Use matchAll instead of match(); or use matchFirst() (constrained variant)
     */
    @Override
    @Deprecated
    public <M extends BigraphMatch<PureBigraph>> MatchIterable<M> match(PureBigraph agent, ReactionRule<PureBigraph> rule) {
        return this.matchAll(agent, rule);
    }

    /**
     * Finds the first match of a specified reaction rule within a given pure bigraph.
     *
     * @param <M>   the type of the match.
     * @param agent the pure bigraph in which to search for the first match.
     * @param rule  the reaction rule containing the redex to be matched against the agent.
     * @return an iterable collection containing the first match of type M found in the given pure bigraph agent.
     */
    public <M extends BigraphMatch<?>> MatchIterable<M> matchFirst(PureBigraph agent, ReactionRule<PureBigraph> rule) {
        return matchAllInternal(agent, rule, true);
    }

    /**
     * Finds matches of a specific reaction rule within a provided pure bigraph, with an option to return only the first match.
     *
     * @param <M>       the type of the match that extends {@link BigraphMatch}.
     * @param agent     the pure bigraph in which to search for matches.
     * @param rule      the reaction rule containing the redex to be matched against the agent.
     * @param firstOnly a boolean indicating whether only the first match should be returned (true) or all matches should be found (false).
     * @return an iterable collection of matches of type M found in the given pure bigraph agent.
     */
    private <M extends BigraphMatch<?>> MatchIterable<M> matchAllInternal(
            PureBigraph agent,
            ReactionRule<PureBigraph> rule,
            boolean firstOnly
    ) {
        super.agent = agent;
        super.rule = rule;
        super.redex = rule.getRedex();

        PureBigraphMatchingEngine matchingEngine = instantiateEngine();
        Stopwatch timer0 = LOGGER.isDebugEnabled() ? Stopwatch.createStarted() : null;

        MatchIterable<PureBigraphMatch> bigraphMatches =
                new MatchIterable<>(firstOnly
                        ? new PureMatchIteratorImpl.FirstMatchOnly(matchingEngine)
                        : new PureMatchIteratorImpl(matchingEngine));

        if (LOGGER.isDebugEnabled() && timer0 != null) {
            long elapsed0 = timer0.stop().elapsed(TimeUnit.NANOSECONDS);
            log(elapsed0);
        }
        return (MatchIterable<M>) bigraphMatches;
    }

    private void log(long elapsed0) {
        LOGGER.debug("Complete Matching Time: {} (ms)", (elapsed0 / 1e+6f));
    }
}
