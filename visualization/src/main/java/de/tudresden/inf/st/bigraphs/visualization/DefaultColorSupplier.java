package de.tudresden.inf.st.bigraphs.visualization;

import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import guru.nidi.graphviz.attribute.Color;

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
                return Color.OLIVEDRAB;
            case OUTER_NAME:
                return Color.GREENYELLOW;
            case EDGE:
                return Color.GREEN;
            default:
                return DEFAULT_COLOR;
        }
    }
}
