/*
 * Copyright (c) 2024-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.core.reactivesystem;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * Generic interface for bigraph rewriting matching results.
 *
 * @param <B> type of the underlying bigraph
 * @author Dominik Grzelak
 */
public interface BMatchResult<B extends Bigraph<? extends Signature<?>>> {

    ReactionRule<B> getReactionRule();

    BigraphMatch<B> getMatch();

    /**
     * This stores the rewritten bigraph for reference
     * @return
     */
    B getBigraph();

    int getOccurrenceCount();

    /**
     * Get the canonical string of the agent for this match result
     * @return
     */
    String getCanonicalString();
}
