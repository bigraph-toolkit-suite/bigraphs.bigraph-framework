package de.tudresden.inf.st.bigraphs.visualization;

import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import guru.nidi.graphviz.attribute.Color;

import java.util.Objects;

/**
 * A supplier for the color of a node or edge in a graph.
 *
 * @param <V> the type
 * @author Dominik Grzelak
 */
public abstract class DefaultColorSupplier<V> extends GraphicalFeatureSupplier<V> {

    public DefaultColorSupplier() {
        super(null);
    }

    public DefaultColorSupplier(BigraphEntity node) {
        super(node);
    }
}
