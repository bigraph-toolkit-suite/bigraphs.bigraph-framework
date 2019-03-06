package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultControl;

public class DefaultControlBuilder<NT extends NamedType, FO extends FiniteOrdinal>
        extends ControlBuilder<NT, FO, DefaultControlBuilder<NT, FO>> {

    protected DefaultControlBuilder() {
        super();
    }

    @Override
    DefaultControl<NT, FO> build() {
        return DefaultControl.createDefaultControl(super.type, super.arity);
    }
}
