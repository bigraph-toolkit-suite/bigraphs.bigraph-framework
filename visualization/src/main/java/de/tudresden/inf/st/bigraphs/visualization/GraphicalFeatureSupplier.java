package de.tudresden.inf.st.bigraphs.visualization;

import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;

import java.util.function.Supplier;

/**
 * Base abstract supplier class for all graphical attributes of a Graphviz graph.
 *
 * @param <V> attribute type of a style element
 */
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
