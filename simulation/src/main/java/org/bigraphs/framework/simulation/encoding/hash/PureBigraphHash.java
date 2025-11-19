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
package org.bigraphs.framework.simulation.encoding.hash;

import org.bigraphs.framework.core.impl.pure.PureBigraph;

/**
 * @author Dominik Grzelak
 */
public class PureBigraphHash implements BigraphHashFunction<PureBigraph> {

    /**
     * Computes a "hash" for a pure bigraph by only considering the number of places, edges, inner and outer names.
     * It does not compute a unique hash. However, it can be used as "pre-check" to early terminate a more complex algorithm.
     *
     * @param bigraph the bigraph
     * @return a possibly non-unique hash
     */
    @Override
    public long hash(PureBigraph bigraph) {
        return bigraph.getAllPlaces().size() +
                bigraph.getEdges().size() +
                bigraph.getOuterNames().size() +
                bigraph.getInnerNames().size()
                ;
    }
}
