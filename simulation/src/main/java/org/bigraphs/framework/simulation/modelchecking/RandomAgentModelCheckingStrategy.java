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
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * Random state-space traversal.
 * <p>
 * Evaluates all rules, then selects the next agent from the shuffled worklist at random.
 *
 * @author Dominik Grzelak
 */
public class RandomAgentModelCheckingStrategy<B extends Bigraph<? extends Signature<?>>> extends ModelCheckingStrategySupport<B> {

    private final Random rnd = new Random();

    public RandomAgentModelCheckingStrategy(BigraphModelChecker<B> modelChecker) {
        super(modelChecker);
    }

    @Override
    public Collection<B> createWorklist() {
        return new ArrayList<>();
    }

    @Override
    public B removeNext(Collection<B> worklist) {
        Collections.shuffle((List<?>) worklist, rnd);
        B agent = ((List<B>) worklist).getFirst();
        worklist.clear();
        return agent;
    }

    @Override
    public void addToWorklist(Collection<B> worklist, B bigraph) {
        worklist.add(bigraph);
    }
}
