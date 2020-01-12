package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;

public class DynamicControlBuilder extends ControlBuilder<StringTypedName,
        FiniteOrdinal<Integer>, DynamicControlBuilder> {
    private ControlKind kind;

    protected DynamicControlBuilder() {
        super();
    }

    public DynamicControlBuilder kind(ControlKind kind) {
        this.kind = kind;
        return self();
    }

    public DynamicControlBuilder identifier(String name) {
        return super.identifier(StringTypedName.of(name));
    }

    public DynamicControlBuilder arity(Integer arity) {
        return super.arity(FiniteOrdinal.ofInteger(arity));
    }

    @Override
    protected DefaultDynamicControl build() {
        return DefaultDynamicControl.createDefaultDynamicControl(getType(), getArity(), this.kind);
    }
}
