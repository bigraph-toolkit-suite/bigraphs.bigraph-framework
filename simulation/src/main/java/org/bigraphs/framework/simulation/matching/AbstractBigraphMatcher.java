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
package org.bigraphs.framework.simulation.matching;

import java.lang.reflect.InvocationTargetException;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.matching.pure.PureBigraphMatcher;


/**
 * Abstract class for matching bigraphs against reaction rules. This class provides
 * the basic structure for implementing specific bigraph matchers by extending its
 * functionality. Subclasses are required to provide implementations for custom matching
 * logic and driven by a dedicated matching engine {@link #instantiateEngine()} w.r.t. to the bigraph type.
 * <p>
 * The correct one, is created using the factory method {@link AbstractBigraphMatcher#create(Class)} by supplying the bigraph type as class.
 * <p>
 * The matcher needs an agent and redex to perform bigraph matching.
 *
 * @param <B> the type of bigraph which extends from Bigraph with a specific signature
 * @author Dominik Grzelak
 */
public abstract class AbstractBigraphMatcher<B extends Bigraph<? extends Signature<?>>> {
    protected B agent;
    protected B redex;
    protected ReactionRule<B> rule;

    protected AbstractBigraphMatcher() {

    }

    @SuppressWarnings("unchecked")
    public static <B extends Bigraph<? extends Signature<?>>> AbstractBigraphMatcher<B> create(Class<B> bigraphClass) {
        if (bigraphClass == PureBigraph.class) {
            try {
                return (AbstractBigraphMatcher<B>) Class.forName(PureBigraphMatcher.class.getCanonicalName()).getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Not Implemented Yet");
    }

    public abstract <M extends BigraphMatch<B>> MatchIterable<M> match(B agent, ReactionRule<B> rule);

    /**
     * Provide the matching engine for the specific bigraph type implemented by the subclass
     *
     * @return concrete bigraph matching engine
     */
    protected abstract BigraphMatchingEngine<B> instantiateEngine();

    /**
     * Returns the supplied agent passed via the {@link AbstractBigraphMatcher#match(Bigraph, ReactionRule)} method.
     *
     * @return the agent for the match
     */
    public B getAgent() {
        return agent;
    }

    /**
     * Returns the supplied redex passed via the {@link AbstractBigraphMatcher#match(Bigraph, ReactionRule)} method.
     *
     * @return the redex for the match
     */
    public B getRedex() {
        return redex;
    }
}
