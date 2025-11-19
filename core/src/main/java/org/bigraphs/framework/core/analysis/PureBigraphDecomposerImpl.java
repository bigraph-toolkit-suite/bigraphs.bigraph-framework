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

import java.util.List;
import java.util.Map;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;

public class PureBigraphDecomposerImpl extends BigraphDecomposer<PureBigraph> {
    protected PureBigraphDecomposerImpl(BigraphDecompositionStrategy<PureBigraph> decompositionStrategy) {
        super(decompositionStrategy);
    }

    // delegate method
    public Map<Integer, List<BigraphEntity<?>>> getPartitions() {
        return ((PureLinkGraphConnectedComponents) getDecompositionStrategy()).getPartitions();
    }

    // delegate method
    public Map<BigraphEntity<?>, Integer> getIdMap() {
        return ((PureLinkGraphConnectedComponents) getDecompositionStrategy()).getIdMap();
    }

    // delegate method
    public List<PureBigraph> getConnectedComponents() {
        return ((PureLinkGraphConnectedComponents) getDecompositionStrategy()).getConnectedComponents();
    }

    /**
     * Delegate method.
     * Returns the abstract data structure to solve the Union-Find problem.
     *
     * @return the data structure
     */
    public PureLinkGraphConnectedComponents.UnionFind getUnionFindDataStructure() {
        return ((PureLinkGraphConnectedComponents) getDecompositionStrategy()).getUnionFindDataStructure();
    }
}
