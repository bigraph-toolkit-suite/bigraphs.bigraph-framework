package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.BNode;
import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.impl.BNodeImpl;

public abstract class NodeDecorator extends BNodeImpl {
    protected BNode nodeInstance;

    public NodeDecorator(BNode nodeInstance) {
        this.nodeInstance = nodeInstance;
    }

    public BNode getEdgeInstance() {
        return nodeInstance;
    }
}
