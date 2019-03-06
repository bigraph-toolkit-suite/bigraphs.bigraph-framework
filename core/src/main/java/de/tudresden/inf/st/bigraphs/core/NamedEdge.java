package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.BEdge;
import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.impl.BEdgeImpl;

//InstanceClassName wäre das gleiche??

//TODO: move the decorator subclasses to the matching module
public class NamedEdge extends EdgeDecorator implements NamedType<String> {
    private StringTypedName stringTypedName;

    public NamedEdge(BEdge edgeInstance, String name) {
        super(edgeInstance);
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
