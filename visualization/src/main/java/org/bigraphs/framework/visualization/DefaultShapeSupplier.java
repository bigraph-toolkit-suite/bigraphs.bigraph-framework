package org.bigraphs.framework.visualization;

import org.bigraphs.framework.core.impl.BigraphEntity;

/**
 * A supplier for the shape of a node.
 *
 * @param <V> the type
 * @author Dominik Grzelak
 */
public abstract class DefaultShapeSupplier<V> extends GraphicalFeatureSupplier<V> {

    public DefaultShapeSupplier() {
        super(null);
    }

    public DefaultShapeSupplier(BigraphEntity node) {
        super(node);
    }
}
