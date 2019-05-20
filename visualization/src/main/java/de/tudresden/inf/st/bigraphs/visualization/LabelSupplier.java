package de.tudresden.inf.st.bigraphs.visualization;

import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;

import java.util.function.Supplier;

public abstract class LabelSupplier extends GraphicalFeatureSupplier<String> {

    public LabelSupplier() {
        super(null);
    }

    public LabelSupplier(BigraphEntity node) {
        super(node);
    }
}
