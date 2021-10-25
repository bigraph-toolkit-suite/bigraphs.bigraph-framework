package de.tudresden.inf.st.bigraphs.visualization;

import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;

import java.util.Objects;

public class DefaultLabelSupplier extends GraphicalFeatureSupplier<String> {

    DefaultLabelSupplier() {
        super(null);
    }

    @Override
    public String get() {
        if ((getNode()) == null) return "INVALID";
        switch (getNode().getType()) {
            case NODE:
                BigraphEntity.NodeEntity node = (BigraphEntity.NodeEntity) getNode();
                return node.getControl().getNamedType().stringValue() + super.delimiterForLabel + node.getName();
            case ROOT:
                return String.format("r%s%s", super.delimiterForLabel, ((BigraphEntity.RootEntity) getNode()).getIndex());
            case SITE:
                return String.format("s%s%s", super.delimiterForLabel, ((BigraphEntity.SiteEntity) getNode()).getIndex());
            case OUTER_NAME:
                return ((BigraphEntity.OuterName) getNode()).getName();
            case INNER_NAME:
                return ((BigraphEntity.InnerName) getNode()).getName();
            case EDGE:
                return ((BigraphEntity.Edge) getNode()).getName();
            default:
                return "NONE";
        }
    }
}
