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
package org.bigraphs.framework.core.analysis;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;

/**
 * Strategy interface for concrete bigraph decomposition implementations.
 *
 * @param <B>  bigraph type
 */
public interface BigraphDecompositionStrategy<B extends Bigraph<? extends Signature<?>>> {

    enum DecompositionStrategy {
        UnionFind_PureBigraphs(PureBigraph.class, PureLinkGraphConnectedComponents.class, PureBigraphDecomposerImpl.class);

        Class<? extends Bigraph<? extends Signature<?>>> bigraphClassType;
        Class<? extends BigraphDecompositionStrategy<? extends Bigraph<? extends Signature<?>>>> implClassType;
        Class<? extends BigraphDecomposer<? extends Bigraph<? extends Signature<?>>>> decomposerClassType;
        DecompositionStrategy(Class<? extends Bigraph<? extends Signature<?>>> clazz1, Class<? extends BigraphDecompositionStrategy<? extends Bigraph<? extends Signature<?>>>> clazz2, Class<? extends BigraphDecomposer<? extends Bigraph<? extends Signature<?>>>> decomposerClassType) {
            this.bigraphClassType = clazz1;
            this.implClassType = clazz2;
            this.decomposerClassType = decomposerClassType;
        }

        public Class<? extends Bigraph<? extends Signature<?>>> getBigraphClassType() {
            return bigraphClassType;
        }

        public Class<? extends BigraphDecompositionStrategy<? extends Bigraph<? extends Signature<?>>>> getImplClassType() {
            return implClassType;
        }

        public Class<? extends BigraphDecomposer<? extends Bigraph<? extends Signature<?>>>> getDecomposerClassType() {
            return decomposerClassType;
        }
    }

    DecompositionStrategy getDecompositionStrategyType();
    //TODO: no param, additional withBigraph Method like fragment needs this
    void decompose(B bigraph);
}
