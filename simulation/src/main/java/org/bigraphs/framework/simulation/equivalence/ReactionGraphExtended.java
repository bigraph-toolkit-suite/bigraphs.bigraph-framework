/*
 * Copyright (c) 2024 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.equivalence;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;

/**
 * This is a "wrapper" class that extends the basic reaction graph structure {@link ReactionGraph}, and contains an instance of the
 * behavioral equivalence mixin implementation of {@link BehavioralEquivalenceMixin}.
 * This allows us to delegate calls to the extended method to the mixin.
 * For instance, to compute bisimilarity.
 * <p>
 * Note: Better is a dynamic mixin implementation at run-time.
 *
 * @param <B> the type of the bigraph of the states and transition relations of the transition system
 * @author Dominik Grzelak
 */
public class ReactionGraphExtended<B extends Bigraph<? extends Signature<?>>> extends ReactionGraph<B> {
    private final BehavioralEquivalenceMixin<ReactionGraphExtended<B>> mixin;

    public ReactionGraphExtended(BehavioralEquivalenceMixin<ReactionGraphExtended<B>> mixin) {
        this.mixin = mixin;
    }

    //    @Override
    public boolean isEquivalentTo(ReactionGraphExtended<B> transitionSystem) {
        mixin.attachToObject(this);
        return mixin.isEquivalentTo(transitionSystem);
    }
}
