package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.BEdge;
import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.impl.BEdgeImpl;

public abstract class EdgeDecorator extends BEdgeImpl {
    protected BEdge edgeInstance;

    public EdgeDecorator(BEdge edgeInstance) {
        this.edgeInstance = edgeInstance;
    }

    public BEdge getEdgeInstance() {
        return edgeInstance;
    }
}
