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
package org.bigraphs.framework.simulation.modelchecking.reactions;

import java.util.Collection;
import java.util.function.Supplier;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * Abstract base class for all agent match suppliers.
 * A supplier is a argument-less function that returns something.
 * <p>
 * This class is used in the model checking procedure as a generic interface.
 *
 * @author Dominik Grzelak
 */
public abstract class AbstractAgentMatchSupplier<B extends Bigraph<? extends Signature<?>>> implements Supplier<B> {
    protected final int size;
    protected ImmutableList<B> agents;

    public AbstractAgentMatchSupplier(Collection<B> availableAgents) {
        agents = Lists.immutable.withAll(availableAgents);
        size = (agents.size() - 1 == 0) ? 1 : agents.size();
    }
}
