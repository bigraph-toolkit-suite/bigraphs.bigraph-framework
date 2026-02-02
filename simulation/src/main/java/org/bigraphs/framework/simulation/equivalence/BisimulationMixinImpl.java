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
package org.bigraphs.framework.simulation.equivalence;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;

/**
 * Bisimulation checks whether two labeled transition systems exhibit the same behavior:
 * every sequence of visible actions in one system can be matched by the other.
 * <p>
 * <b>Strong bisimulation:</b> all actions are observable.<br>
 * <b>Weak bisimulation:</b> τ (tau) transitions are internal and abstracted away.
 *
 * @param <AST> transition-system type (states are bigraphs of type B; transitions are BMatchResult&lt;B&gt;)
 * @param <B>   state type used by AST
 * @author Dominik Grzelak
 */
public class BisimulationMixinImpl<B extends Bigraph<? extends Signature<?>>, AST extends ReactionGraph<B>> implements BehavioralEquivalenceMixin<AST> {
    AST transitionSystem1;

    EquivalenceAlgorithm defaultBisimAlgo = EquivalenceAlgorithm.KANELLAKIS_SMOLKA;

    @Override
    public boolean isEquivalentTo(AST transitionSystem2) {
        BisimulationCheckerSupport<B, AST> support = new BisimulationCheckerSupport<>();
        if (defaultBisimAlgo == EquivalenceAlgorithm.KANELLAKIS_SMOLKA) {
            return support.checkBisimulation_Bighuggies(transitionSystem1, transitionSystem2);
        }
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    public void attachToTransitionSystem(AST transitionSystem) {
        this.transitionSystem1 = transitionSystem;
    }

    public EquivalenceAlgorithm getBisimType() {
        return defaultBisimAlgo;
    }

    public void setBisimType(EquivalenceAlgorithm defaultBisimAlgo) {
        this.defaultBisimAlgo = defaultBisimAlgo;
    }
}