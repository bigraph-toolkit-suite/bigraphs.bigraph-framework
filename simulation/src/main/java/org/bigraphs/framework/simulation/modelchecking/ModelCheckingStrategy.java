/*
 * Copyright (c) 2019-2024 Bigraph Toolkit Suite Developers
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

/**
 * Strategy pattern for implementing new model checking algorithms.
 * <p>
 * Current implementations:
 * <ul>
 *     <li>Breadth-first search (with cycle detection)</li>
 *     <li>Simulation (BFS without cycle checking)</li>
 *     <li>Random</li>
 * </ul>
 *
 * @author Dominik Grzelak
 */
public interface ModelCheckingStrategy<B extends Bigraph<? extends Signature<?>>> {

    /**
     * Entry point of the model checking strategy to implement.
     * <p>
     * The reaction graph (i.e., transition system) can be acquired and stored via the model checker object.
     */
    void synthesizeTransitionSystem();
}
