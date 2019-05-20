package de.tudresden.inf.st.bigraphs.visualization;

import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;

import java.util.function.Supplier;

public abstract class GraphicalFeatureSupplier<V> implements Supplier<V> {
    private BigraphEntity node;

    public GraphicalFeatureSupplier(BigraphEntity node) {
        with(node);
    }

    public GraphicalFeatureSupplier<V> with(BigraphEntity node) {
        this.node = node;
        return this;
    }

    BigraphEntity getNode() {
        return node;
    }

    @Override
    public abstract V get();
}
