/*
 * Copyright (c) 2026 Bigraph Toolkit Suite Developers
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
 * Filter interface used by {@link ModelCheckingStrategySupport} to post-process
 * the next agent drawn from the worklist.
 * <p>
 * Users can apply a filter or perform a no-op; a standard no-op filter is provided for that {@link #noop()}.
 *
 * @param <B>
 * @author Dominik Grzelak
 */
@FunctionalInterface
public interface BigraphFilter<B extends Bigraph<? extends Signature<?>>> {

    /**
     * Applies a filter to the given bigraph.
     *
     * @param bigraph the candidate bigraph
     * @return the original bigraph, or {@code null} if it should be discarded
     */
    B apply(B bigraph);

    /**
     * Default no-op filter (accepts everything).
     */
    static <B extends Bigraph<? extends Signature<?>>> BigraphFilter<B> noop() {
        return b -> b;
    }
}
