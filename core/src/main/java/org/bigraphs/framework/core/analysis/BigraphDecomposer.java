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

import static org.bigraphs.framework.core.analysis.BigraphDecompositionStrategy.DecompositionStrategy.UnionFind_PureBigraphs;

import java.lang.reflect.InvocationTargetException;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;

/**
 * This class provides access to decomposition approaches to bigraphs.
 * The strategy pattern is implemented to select the concrete decomposition approach.
 * The specific decomposition algorithm is implemented by subclasses of the interface {@link BigraphDecompositionStrategy}.
 * <p>
 * Subclasses of {@link BigraphDecomposer} may offer additional methods depending on which specific decomposition strategy
 * was used. For example, a subclass may return the partitions or provide other information.
 * <p>
 * An instance of this class can be created by calling the factory method {@link #create(BigraphDecompositionStrategy.DecompositionStrategy)} and
 * supplying the decomposition strategy.
 *
 * @param <B> type of the bigraph
 * @author Dominik Grzelak
 */
public class BigraphDecomposer<B extends Bigraph<? extends Signature<?>>> {

    public static final BigraphDecompositionStrategy.DecompositionStrategy DEFAULT_DECOMPOSITION_STRATEGY = UnionFind_PureBigraphs;

    // Naive "multi-dispatch": based on the decomposition strategy and bigraph type, the corresponding subclass
    // will be instantiated. Otherwise, an exception is thrown.
    public static <B extends Bigraph<? extends Signature<?>>, T extends BigraphDecomposer<B>> T create(BigraphDecompositionStrategy.DecompositionStrategy strategy) {
        try {
            switch (strategy) {
                case UnionFind_PureBigraphs -> {
                    BigraphDecompositionStrategy<? extends Bigraph<? extends Signature<?>>> bigraphDecompositionStrategy = strategy.getImplClassType().getDeclaredConstructor().newInstance();
                    return (T) (new PureBigraphDecomposerImpl((BigraphDecompositionStrategy<PureBigraph>) bigraphDecompositionStrategy));
                }
                default -> throw new RuntimeException("Failed to create BigraphDecomposer instance: " + strategy);
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException("Failed to create BigraphDecomposer instance: " + strategy);
        }
    }

    protected BigraphDecompositionStrategy<B> decompositionStrategy;

    protected BigraphDecomposer(BigraphDecompositionStrategy<B> decompositionStrategy) {
        this.decompositionStrategy = decompositionStrategy;
    }

    public BigraphDecompositionStrategy<B> getDecompositionStrategy() {
        return decompositionStrategy;
    }

    public void decompose(B bigraph) {
        this.getDecompositionStrategy().decompose(bigraph);
    }
}
