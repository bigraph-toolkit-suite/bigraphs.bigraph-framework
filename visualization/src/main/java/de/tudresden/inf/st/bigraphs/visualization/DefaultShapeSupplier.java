package de.tudresden.inf.st.bigraphs.visualization;

import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import guru.nidi.graphviz.attribute.Shape;

import java.util.Objects;

public class DefaultShapeSupplier extends GraphicalFeatureSupplier<Shape> {
    private final static Shape DEFAULT_SHAPE = Shape.RECTANGLE;

    public DefaultShapeSupplier() {
        super(null);
    }

    public DefaultShapeSupplier(BigraphEntity node) {
        super(node);
    }

    @Override
    public Shape get() {
        if (Objects.isNull(getNode())) return DEFAULT_SHAPE;
        switch (getNode().getType()) {
            case ROOT:
                return Shape.CIRCLE;
            case NODE:
                return Shape.RECTANGLE;
            case SITE:
                return Shape.DIAMOND;
            default:
                return DEFAULT_SHAPE;
        }


    }
}
