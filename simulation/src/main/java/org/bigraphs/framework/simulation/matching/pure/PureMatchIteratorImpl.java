/*
 * Copyright (c) 2019-2026 Bigraph Toolkit Suite Developers
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

import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.simulation.matching.AbstractBigraphMatchIterator;
import org.eclipse.collections.impl.factory.Lists;

/**
 * Iterator implementation for bigraph matching.
 * <p>
 * This iterator is created by the {@link PureBigraphMatcher} class.
 *
 * @author Dominik Grzelak
 */
public class PureMatchIteratorImpl extends AbstractBigraphMatchIterator<PureBigraph> {

    public PureMatchIteratorImpl(PureBigraphMatchingEngine matchingEngine) {
        super(matchingEngine);
        this.findMatches();
    }

    /**
     * The behavior of {@code findMatches} may vary if a subclass overrides
     * this method to provide custom matching logic.
     * <p>
     * This can be done to provide simple constraints or filters for the matches.
     */
    protected void findMatches() {
        ((PureBigraphMatchingEngine) this.matchingEngine).beginMatch();
        if (((PureBigraphMatchingEngine) this.matchingEngine).hasMatched()) {
            ((PureBigraphMatchingEngine) this.matchingEngine).getAllMatches();
        }
        this.matches = Lists.mutable.ofAll(this.matchingEngine.getMatches());
    }

    public static class FirstMatchOnly extends AbstractBigraphMatchIterator.FirstMatchOnly<PureBigraph> {

        public FirstMatchOnly(PureBigraphMatchingEngine matchingEngine) {
            super(matchingEngine);
            this.findMatches();
        }

        @Override
        protected void findMatches() {
            ((PureBigraphMatchingEngine) this.matchingEngine).beginMatch();
            if (((PureBigraphMatchingEngine) this.matchingEngine).hasMatched()) {
                ((PureBigraphMatchingEngine) this.matchingEngine).getSingleMatch();
            }
            this.matches = Lists.mutable.ofAll(this.matchingEngine.getMatches());
        }
    }

}
