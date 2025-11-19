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

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * This algorithm implements a depth-first model checking algorithm.
 * It also detects cycles.
 * This algorithm can be used to conduct reachability analysis.
 *
 * @author Dominik Grzelak
 */
public class DepthFirstStrategy<B extends Bigraph<? extends Signature<?>>> extends ModelCheckingStrategySupport<B> {
    public DepthFirstStrategy(BigraphModelChecker<B> modelChecker) {
        super(modelChecker);
    }

    @Override
    protected Collection<B> createWorklist() {
        return new ConcurrentLinkedDeque<>();
    }

    @Override
    protected B removeNext(Collection<B> worklist) {
        return ((Deque<B>) worklist).removeLast(); // LIFO
    }

    @Override
    protected void addToWorklist(Collection<B> worklist, B bigraph) {
        ((Deque<B>) worklist).addLast(bigraph);
    }
}
