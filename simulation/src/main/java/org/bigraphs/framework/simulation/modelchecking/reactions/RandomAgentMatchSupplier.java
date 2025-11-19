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
package org.bigraphs.framework.simulation.modelchecking.reactions;

import java.util.Collection;
import java.util.Random;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * Specific implementation of {@link AbstractAgentMatchSupplier}.
 * <p>
 * When deciding which paths to go in the model checking procedure, this supplier retrieves
 * any agent-match from the given set of possibly available agent-matches.
 *
 * @author Dominik Grzelak
 */
public final class RandomAgentMatchSupplier<B extends Bigraph<? extends Signature<?>>> extends AbstractAgentMatchSupplier<B> {

    private final Random randomSelection;


    protected RandomAgentMatchSupplier(Collection<B> availableAgents) {
        super(availableAgents);
        randomSelection = new Random();
    }

    @Override
    public B get() {
        return agents.get(randomSelection.nextInt(size));
    }
}
