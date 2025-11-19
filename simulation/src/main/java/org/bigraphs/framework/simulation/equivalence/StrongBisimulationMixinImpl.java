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
import org.bigraphs.framework.core.reactivesystem.AbstractTransitionSystem;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;

/**
 * Mixin Infrastructure-related class.
 * <p>
 * This class provides the implementation of the interface {@link BehavioralEquivalenceMixin} for the bisimulation
 * equivalence checking routine.
 * It's a mixin implementation essentially providing the implementation of {@link #isEquivalentTo}.
 * Via this mixin this new behavior can be introduced into specific sub-classes of
 * {@link AbstractTransitionSystem} by extending them like a "wrapper"
 * and adding the new method {@link #isEquivalentTo}.
 * <p>
 * Checking bisimulation between two systems states "that whatever series of visible actions one LTS may perform, the other may match."
 * [1]
 * Bisimilar systems have equal behavior, thus, can be replaced with each other.
 * <p>
 * Strong: All actions are visible.
 * <p>
 * This mixin implementation delegates the computation to the {@link BisimulationCheckerSupport} class,
 * which contains several algorithms.
 *
 * @param <AST> type of the transition system (states are bigraphs of type B, transition of type BMatchResult<B>)
 * @param <B>   type of the states of AST
 * @author Dominik Grzelak
 * @see "[1] [DaKT07] Danos, Vincent; Krivine, Jean; Tarissan, Fabien: Self-assembling Trees.
 * In: Electronic Notes in Theoretical Computer Science,
 * Proceedings of the Third Workshop on Structural Operational Semantics
 * (SOS 2006).
 * Bd. 175 (2007), Nr. 1, S. 19–32"
 */
public class StrongBisimulationMixinImpl<B extends Bigraph<? extends Signature<?>>, AST extends ReactionGraph<B>> implements BehavioralEquivalenceMixin<AST> {
    AST transitionSystem1;

    Algorithms defaultBisimAlgo = Algorithms.KANELLAKIS_SMOLKA;

    @Override
    public boolean isEquivalentTo(AST transitionSystem2) {
        BisimulationCheckerSupport<B, AST> support = new BisimulationCheckerSupport<>();
        if (defaultBisimAlgo == Algorithms.KANELLAKIS_SMOLKA) {
            return support.checkBisimulation_Bighuggies(transitionSystem1, transitionSystem2);
        }
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    public void attachToObject(AST transitionSystem) {
        this.transitionSystem1 = transitionSystem;
    }

    public Algorithms getDefaultBisimAlgo() {
        return defaultBisimAlgo;
    }

    public void setDefaultBisimAlgo(Algorithms defaultBisimAlgo) {
        this.defaultBisimAlgo = defaultBisimAlgo;
    }
}