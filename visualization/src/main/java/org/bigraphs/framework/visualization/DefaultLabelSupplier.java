package org.bigraphs.framework.visualization;

import org.bigraphs.framework.core.impl.BigraphEntity;

/**
 * A generic supplier for String labels.
 * Is used for edges and nodes of a graph.
 *
 * @author Dominik Grzelak
 */
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
                return node.getName() + super.delimiterForLabel + node.getControl().getNamedType().stringValue();
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
