package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.BEdge;
import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.BNode;

//TODO: move the decorators subclasses to the matching module
/**
 * This class is used for the matching, nodes get prior transformed
 */
public class NamedNode extends NodeDecorator implements NamedType<String> {

    private StringTypedName stringTypedName;

    public NamedNode(BNode nodeInstance, String name) {
        super(nodeInstance);
        stringTypedName = StringTypedName.of(name);
    }

    public String getValue() {
        return stringTypedName.getValue();
    }

    @Override
    public String stringValue() {
        return getValue();
    }
}
