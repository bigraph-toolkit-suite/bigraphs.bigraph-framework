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

import java.util.*;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

//TODO: in a future version: find matches incrementally and not all at the start
//  next() and hasNext() must be slightly rewritten then
// do not call find

/**
 * Iterator implementation for bigraph matching.
 * <p>
 * This iterator is created by the {@link PureBigraphMatcher} class.
 *
 * @author Dominik Grzelak
 */
public class PureMatchIteratorImpl implements Iterator<PureBigraphMatch> {

    protected int cursor = 0;
    protected MutableList<PureBigraphMatch> matches = Lists.mutable.empty();
    protected PureBigraphMatchingEngine matchingEngine;

    PureMatchIteratorImpl(PureBigraphMatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
        this.findMatches();
    }

    /**
     * The behavior of {@code findMatches} may vary if a subclass overrides
     * this method to provide custom matching logic.
     * <p>
     * This can be done to provide simple constraints or filters for the matches.
     */
    protected void findMatches() {
        this.matchingEngine.beginMatch();
        if (this.matchingEngine.hasMatched()) {
            this.matchingEngine.createMatchResult();
        }
        this.matches = Lists.mutable.ofAll(this.matchingEngine.getMatches());
    }

    @Override
    public boolean hasNext() {
        if (matches.isEmpty()) return false;
        return cursor != matches.size();
    }

    @Override
    public PureBigraphMatch next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return matches.get(cursor++);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constrained Matcher
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * A Simple Constrained Matcher Implementation
     *
     * @author Dominik Grzelak
     */
    public static class FirstMatchOnly extends PureMatchIteratorImpl {

        FirstMatchOnly(PureBigraphMatchingEngine matchingEngine) {
            super(matchingEngine);
        }

        @Override
        protected void findMatches() {
            this.matchingEngine.beginMatch();
            if (this.matchingEngine.hasMatched()) {
                this.matchingEngine.createSingleMatchResult();
            }
            this.matches = Lists.mutable.ofAll(this.matchingEngine.getMatches());

        }
    }
}
