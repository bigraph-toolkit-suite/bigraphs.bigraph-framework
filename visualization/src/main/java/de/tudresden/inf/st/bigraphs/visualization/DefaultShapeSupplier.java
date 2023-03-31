package de.tudresden.inf.st.bigraphs.visualization;

import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import guru.nidi.graphviz.attribute.Shape;

import java.util.Objects;

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
