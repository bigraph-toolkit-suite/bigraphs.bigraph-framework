package de.tudresden.inf.st.bigraphs.visualization;

import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
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
                return Shape.ELLIPSE;
            case NODE:
                return Shape.RECTANGLE;
            case SITE:
                return Shape.RECTANGLE;
            case INNER_NAME:
            case OUTER_NAME:
                return Shape.RECTANGLE;
            case EDGE:
                return Shape.POINT;
            default:
                return DEFAULT_SHAPE;
        }


    }
}
