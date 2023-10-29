package org.bigraphs.framework.visualization.supplier;

import org.bigraphs.framework.visualization.DefaultShapeSupplier;
import guru.nidi.graphviz.attribute.Shape;

public class GraphvizShapeSupplier extends DefaultShapeSupplier<Shape> {

    private final static Shape DEFAULT_SHAPE = Shape.RECTANGLE;

    @Override
    public Shape get() {
        if ((getNode()) == null) return DEFAULT_SHAPE;
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
