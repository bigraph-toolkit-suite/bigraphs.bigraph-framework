package de.tudresden.inf.st.bigraphs.visualization.supplier;

import de.tudresden.inf.st.bigraphs.visualization.DefaultColorSupplier;
import guru.nidi.graphviz.attribute.Color;

public class GraphvizColorSupplier extends DefaultColorSupplier<Color> {
    private final static Color DEFAULT_COLOR = Color.BLACK;

    @Override
    public Color get() {
        if ((getNode()) == null) return DEFAULT_COLOR;
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
