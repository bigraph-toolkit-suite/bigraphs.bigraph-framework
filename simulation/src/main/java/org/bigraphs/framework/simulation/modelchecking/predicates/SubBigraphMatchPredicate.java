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
package org.bigraphs.framework.simulation.modelchecking.predicates;

import it.uniud.mads.jlibbig.core.std.AgentMatch;
import it.uniud.mads.jlibbig.core.std.AgentMatcher;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphDecoder;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphEncoder;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystemPredicate;
import org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher;

/**
 * Simple predicate implementation that returns true if one bigraph is contained in another (subbigraph matching problem).
 * Thus, it represents a partial predicate.
 * <p>
 * A predicate can be created by using the static methods the class provides.
 * <p>
 * This predicate internally creates a bigraph matcher instance for the concrete bigraph kind automatically.
 * Therefore, {@link AbstractBigraphMatcher} is used. The class member {@code matcher} is used inside the
 * {@link SubBigraphMatchPredicate#test(Bigraph)} method.
 *
 * @author Dominik Grzelak
 * @see BigraphIsoPredicate
 */
public class SubBigraphMatchPredicate<B extends Bigraph<? extends Signature<?>>> extends ReactiveSystemPredicate<B> {

    private final B bigraphToMatch;
    private final it.uniud.mads.jlibbig.core.std.Bigraph jBigraphToMatch;
    private AbstractBigraphMatcher<B> matcher;
    private final JLibBigBigraphEncoder enc = new JLibBigBigraphEncoder();

    private B subBigraphResult;
    private B subBigraphParamResult;
    private B contextBigraphResult;
    private B subRedexResult;

    private SubBigraphMatchPredicate(B bigraphToMatch) {
        this(bigraphToMatch, false);
    }

    private SubBigraphMatchPredicate(B bigraphToMatch, boolean negate) {
        this.bigraphToMatch = bigraphToMatch;
        super.negate = negate;
        this.matcher = AbstractBigraphMatcher.create((Class<B>) bigraphToMatch.getClass());
        this.jBigraphToMatch = enc.encode((PureBigraph) bigraphToMatch);
    }

    public static <B extends Bigraph<? extends Signature<?>>> SubBigraphMatchPredicate<B> create(B bigraphToMatch) {
        return new SubBigraphMatchPredicate<B>(bigraphToMatch);
    }

    public static <B extends Bigraph<? extends Signature<?>>> SubBigraphMatchPredicate<B> create(B bigraphToMatch, boolean negate) {
        return new SubBigraphMatchPredicate<B>(bigraphToMatch, negate);
    }

    @Override
    public B getBigraph() {
        return bigraphToMatch;
    }

    @Override
    public boolean test(B agent) {
        AgentMatcher matcher = new AgentMatcher();
        it.uniud.mads.jlibbig.core.std.Bigraph a = enc.encode((PureBigraph) agent, jBigraphToMatch.getSignature());
        Iterable<? extends AgentMatch> match = matcher.match(a, jBigraphToMatch);
        if (match.iterator().hasNext()) {
            AgentMatch next = match.iterator().next();
            it.uniud.mads.jlibbig.core.std.Bigraph compose = it.uniud.mads.jlibbig.core.std.Bigraph.compose(next.getRedex(), next.getParam());
            JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
            subBigraphResult = (B) decoder.decode(compose);
            subRedexResult = (B) decoder.decode(next.getRedex());
            subBigraphParamResult = (B) decoder.decode(next.getParam());
            contextBigraphResult = (B) decoder.decode(next.getContext());
            return true;
        } else {
            return false;
        }
    }

    public B getSubBigraphResult() {
        return subBigraphResult;
    }

    public B getSubBigraphParamResult() {
        return subBigraphParamResult;
    }

    public B getBigraphToMatch() {
        return bigraphToMatch;
    }

    public B getContextBigraphResult() {
        return contextBigraphResult;
    }

    public B getSubRedexResult() {
        return subRedexResult;
    }
}
