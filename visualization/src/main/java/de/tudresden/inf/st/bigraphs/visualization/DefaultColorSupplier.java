package de.tudresden.inf.st.bigraphs.visualization;

import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Shape;

import java.util.Objects;

public class DefaultColorSupplier extends GraphicalFeatureSupplier<Color> {
    private final static Color DEFAULT_COLOR = Color.BLACK;

    public DefaultColorSupplier() {
        super(null);
    }

    public DefaultColorSupplier(BigraphEntity node) {
        super(node);
    }

    @Override
    public Color get() {
        if (Objects.isNull(getNode())) return DEFAULT_COLOR;
        switch (getNode().getType()) {
            case ROOT:
                return Color.BLACK;
            case NODE:
                return Color.BLACK;
            case SITE:
                return Color.GRAY;
            case INNER_NAME:
                return Color.BLUE;
            case OUTER_NAME:
                return Color.GREEN;
            case EDGE:
                return Color.WHITE;
            default:
                return DEFAULT_COLOR;
        }
    }
}
