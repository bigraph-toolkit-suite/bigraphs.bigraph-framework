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
import it.uniud.mads.jlibbig.core.std.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphEncoder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.matching.BigraphMatchingEngine;
import org.bigraphs.framework.simulation.matching.BigraphMatchingSupport;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matching algorithm for pure bigraphs (see {@link PureBigraph}).
 *
 * @author Dominik Grzelak
 */
public class PureBigraphMatchingEngine extends BigraphMatchingSupport implements BigraphMatchingEngine<PureBigraph> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PureBigraphMatchingEngine.class);

    private Stopwatch matchingTimer;

    protected boolean hasMatched = false;
    protected final MutableList<PureBigraphMatch> matches = Lists.mutable.empty();

    protected ReactionRule<PureBigraph> reactionRule;

    protected final JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();

    protected it.uniud.mads.jlibbig.core.std.Bigraph jLibAgent;
    protected it.uniud.mads.jlibbig.core.std.Bigraph jLibRedex;

    // Matcher matcher = new Matcher();
    protected AgentMatcher agentMatcher = new AgentMatcher();
    protected Iterable<? extends AgentMatch> jLibMatchIterator;

    protected PureBigraphMatchingEngine(PureBigraph agent, ReactionRule<PureBigraph> reactionRule) {
        Stopwatch timer = LOGGER.isDebugEnabled() ? Stopwatch.createStarted() : null;

        this.reactionRule = reactionRule;
        this.jLibAgent = encoder.encode(agent);
        this.jLibRedex = encoder.encode(reactionRule.getRedex(), jLibAgent.getSignature());

        if (LOGGER.isDebugEnabled() && Objects.nonNull(timer))
            LOGGER.debug("Initialization time: {} (ms)", (timer.stop().elapsed(TimeUnit.NANOSECONDS) / 1e+6f));
    }

    @Override
    public List<PureBigraphMatch> getMatches() {
        return matches;
    }

    /**
     * Checks if any match could be found and also if <emph>_all_</emph> redex roots could be matched.
     *
     * @return {@code true}, if a correct match could be found, otherwise {@code false}
     */
    public boolean hasMatched() {
        return hasMatched;
    }

    /**
     * Computes all matches
     * <p>
     * First, structural matching, afterward link matching
     */
    protected void beginMatch() {
        if (LOGGER.isDebugEnabled()) {
            matchingTimer = Stopwatch.createStarted();
        }

        it.uniud.mads.jlibbig.core.std.InstantiationMap eta = PureReactiveSystem.constructEta(reactionRule);
        int prms[] = new int[eta.getPlaceDomain()];
        boolean[] neededParam = new boolean[reactionRule.getRedex().getSites().size()];
        for (int i = 0; i < eta.getPlaceDomain(); i++) {
            int j = eta.getPlaceInstance(i);
            neededParam[j] = true;
            prms[i] = j;
        }
        jLibMatchIterator = agentMatcher.match(jLibAgent, jLibRedex, neededParam);
        // jLibMatchIterator = agentMatcher.match(jLibAgent, jLibRedex);

        hasMatched = true;

        LOGGER.debug("Matches found?: {}", hasMatched());
    }

    /**
     * This methods builds the actual bigraphs determined by the matching algorithm (see {@link #beginMatch()}).
     */
    protected void createMatchResult() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Matching took: {} (ms)", (matchingTimer.stop().elapsed(TimeUnit.NANOSECONDS) / 1e+6f));
            matchingTimer.reset().start();
        }
        try {
            for (Iterator<? extends AgentMatch> it = jLibMatchIterator.iterator(); it.hasNext(); ) {
                AgentMatch each = it.next();
                PureBigraphMatch matchResult = convert(each);
                this.matches.add(matchResult);
            }
        } catch (AssertionError error) {
            error.printStackTrace();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Time to build the match result: {} ms", (matchingTimer.stop().elapsed(TimeUnit.NANOSECONDS) / 1e+6f));
        }
    }

    protected void createSingleMatchResult() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Matching took: {} (ms)", (matchingTimer.stop().elapsed(TimeUnit.NANOSECONDS) / 1e+6f));
            matchingTimer.reset().start();
        }
        try {
            for (Iterator<? extends AgentMatch> it = jLibMatchIterator.iterator(); it.hasNext(); ) {
                AgentMatch each = it.next();
                PureBigraphMatch matchResult = convert(each);
                this.matches.add(matchResult);
                break;
            }
        } catch (AssertionError error) {
            error.printStackTrace();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Time to build the match result: {} ms", (matchingTimer.stop().elapsed(TimeUnit.NANOSECONDS) / 1e+6f));
        }
    }

    private PureBigraphMatch convert(AgentMatch each) {
        PureBigraph redex = reactionRule.getRedex();
        PureBigraph context = null;
        PureBigraph redexImage = null;
        PureBigraph redexIdentity = null;
        Collection<PureBigraph> params = new LinkedList<>();
        PureBigraph paramWiring = null; //decoder.decode(each.getParamWiring());
        return new PureBigraphMatch(
                each,
                context,
                redex,
                redexImage,
                redexIdentity,
                paramWiring,
                params
        );
    }
}
