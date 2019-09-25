package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;

public class DynamicControlBuilder<NT extends NamedType, V extends FiniteOrdinal>
        extends ControlBuilder<NT, V, DynamicControlBuilder<NT, V>> {
    private ControlKind kind;

    protected DynamicControlBuilder() {
        super();
    }

    public DynamicControlBuilder<NT, V> kind(ControlKind kind) {
        this.kind = kind;
        return self();
    }

    @Override
    protected DefaultDynamicControl<NT, V> build() {
        return DefaultDynamicControl.createDefaultDynamicControl(getType(), getArity(), this.kind);
    }
}
