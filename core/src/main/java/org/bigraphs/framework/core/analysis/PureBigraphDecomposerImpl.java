package org.bigraphs.framework.core.analysis;

import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;

import java.util.List;
import java.util.Map;

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
