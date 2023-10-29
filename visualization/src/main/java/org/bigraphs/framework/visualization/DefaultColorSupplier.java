package org.bigraphs.framework.visualization;

import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;

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
