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
 * Mixin for behavioral equivalence on {@link AbstractTransitionSystem} instances.
 * <p>
 * Compares two transition systems to decide if their observable behavior matches.
 * Implementations provide {@link #isEquivalentTo(AbstractTransitionSystem)} and are typically
 * implemented via subclassing plus delegation (e.g., with a strategy).
 *
 * @param <R> transition system type
 * @author Dominik Grzelak
 */
public interface BehavioralEquivalenceMixin<R extends AbstractTransitionSystem<?, ?>> {

    enum EquivalenceAlgorithm {
        KANELLAKIS_SMOLKA // Partitioning splitter algorithm (such as Paige-Tarjan-algorithm)
        ;
    }

    /**
     * Returns whether this object is equivalent to the given transition system.
     * <p>
     * Equivalence check depends on the {@link EquivalenceAlgorithm}.
     *
     * @param system the transition system to compare against
     * @return true if equivalent; false otherwise
     */
    boolean isEquivalentTo(R system);

    /**
     * Attaches this object to the given transition system.
     *
     * @param system the transition system to attach to
     */
    void attachToTransitionSystem(R system);
}
