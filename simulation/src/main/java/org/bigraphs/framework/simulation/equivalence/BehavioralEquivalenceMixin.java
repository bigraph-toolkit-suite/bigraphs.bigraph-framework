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
 * Mixin Infrastructure-related class.
 * <p>
 * Mixin interface for behavioral equivalences on transition systems of type {@link AbstractTransitionSystem} in BTS.
 * Behavioral equivalences are used to compare two transition systems to determine whether they exhibit similar behavior.
 * This Mixin adds additional functionality to existing classes of {@link AbstractTransitionSystem} without
 * modifying their source code directly.
 * <p>
 * The concrete mixin implementations essentially providing the implementation of {@link #isEquivalentTo(AbstractTransitionSystem)}.
 * These specific mixin implementations are then used as follows:
 * So-called "extended", "decorated", or "wrapper" classes of implementations of {@link AbstractTransitionSystem} can
 * introduce this new behavior via the concrete mixin implementations (combined with a strategy pattern, for example).
 * This allows to delegate calls to the new method of the wrapper class to this mixin interface (i.e., an implementation of it).
 *
 * @param <R> type of the transition system
 * @author Dominik Grzelak
 */
// <R extends AbstractTransitionSystem<? extends Bigraph<? extends Signature<?>>, ? extends ReactionRule<?>>>
public interface BehavioralEquivalenceMixin<R extends AbstractTransitionSystem<?, ?>> {

    enum Algorithms {
        KANELLAKIS_SMOLKA // Partitioning splitter algorithm (such as Paige-Tarjan-algorithm)
    }

    boolean isEquivalentTo(R transitionSystem);

    void attachToObject(R transitionSystem);
}
