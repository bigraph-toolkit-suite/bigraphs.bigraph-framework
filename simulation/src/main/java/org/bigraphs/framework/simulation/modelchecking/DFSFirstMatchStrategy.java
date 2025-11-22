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
package org.bigraphs.framework.simulation.modelchecking;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.matching.pure.PureBigraphMatcher;

/**
 * A depth-first exploration strategy that selects only the
 * first available match in "natural matcher order" for each reaction rule when checking a state.
 * The strategy descends along this single successor path and no sibling states are explored for that rule
 * (i.e., if the rule produces more occurrences, only the first is selected).
 * <p>
 * This produces an execution trace consistent with DFS semantics.
 *
 * @author Dominik Grzelak
 */
public class DFSFirstMatchStrategy<B extends Bigraph<? extends Signature<?>>> extends DepthFirstStrategy<B> {

    public DFSFirstMatchStrategy(BigraphModelChecker<B> modelChecker) {
        super(modelChecker);
    }

    /**
     * Return only the first match
     *
     * @param rule
     * @param theAgent
     * @return
     */
    @Override
    protected MatchIterable<BigraphMatch<B>> getBigraphMatches(ReactionRule<B> rule, B theAgent) {
        return modelChecker.watch(() -> ((PureBigraphMatcher) modelChecker.getMatcher()).matchFirst((PureBigraph) theAgent, (ReactionRule<PureBigraph>) rule));
    }
}
