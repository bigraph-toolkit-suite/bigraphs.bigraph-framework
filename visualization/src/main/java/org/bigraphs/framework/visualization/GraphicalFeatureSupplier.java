package org.bigraphs.framework.visualization;

import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;

import java.util.function.Supplier;

/**
 * Base abstract supplier class for all graphical attributes of a Graphviz graph.
 *
 * @param <V> attribute type of a style element
 */
public abstract class GraphicalFeatureSupplier<V> implements Supplier<V> {
    private BigraphEntity node;
    protected char delimiterForLabel;

    public GraphicalFeatureSupplier(BigraphEntity node) {
        with(node, ':');
    }

    public GraphicalFeatureSupplier(BigraphEntity node, char delimiterForLabel) {
        with(node, delimiterForLabel);
    }

    public GraphicalFeatureSupplier<V> with(BigraphEntity node) {
        return with(node, ':');
    }

    public GraphicalFeatureSupplier<V> with(BigraphEntity node, char delimiterForLabel) {
        this.node = node;
        this.delimiterForLabel = delimiterForLabel;
        return this;
    }

    protected BigraphEntity getNode() {
        return node;
    }

    @Override
    public abstract V get();
}
