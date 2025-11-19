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

import org.bigraphs.framework.core.reactivesystem.AbstractTransitionSystem;

/**
 * This class provides the implementation of the interface {@link BehavioralEquivalenceMixin} for the bisimulation equivalence relation.
 * It's a mixin implementation essentially providing the implementation of {@link #isEquivalentTo}.
 * "Wrapper" classes of implementations of {@link AbstractTransitionSystem} can introduce this new behavior via this mixin.
 * <p>
 * Bisimulation is an equivalence between two systems, which states "that whatever series of visible actions one LTS may perform, the other may match." [1]
 * Bisimilar systems have equal behavior, thus, can be replaced with each other.
 * <p>
 * Weak: tau transitions are discarded (i.e., invisible, internal actions are omitted).
 * <p>
 * This mixin implementation delegates the computation to the {@link BisimulationCheckerSupport} class.
 *
 * @author Dominik Grzelak
 * @see "[1] [DaKT07]  Danos, Vincent ; Krivine, Jean ; Tarissan, Fabien: Self-assembling Trees. In: Electronic Notes in Theoretical Computer Science, Proceedings of the Third Workshop on Structural Operational Semantics (SOS 2006). Bd. 175 (2007), Nr. 1, S. 19–32"
 */
public class WeakBisimulationMixinImpl {

}
