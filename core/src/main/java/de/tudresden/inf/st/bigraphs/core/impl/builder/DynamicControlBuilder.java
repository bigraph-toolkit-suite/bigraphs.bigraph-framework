package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.ControlBuilder;
import de.tudresden.inf.st.bigraphs.core.ControlKind;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;

/**
 * The concrete dynamic control builder used by the {@link DynamicSignatureBuilder}.
 * It allows to specify if a control is active, passive or atomic.
 *
 * @author Dominik Grzelak
 * @see DynamicSignatureBuilder
 */
public class DynamicControlBuilder extends ControlBuilder<StringTypedName, FiniteOrdinal<Integer>, DynamicControlBuilder> {
    private ControlKind kind;

    protected DynamicControlBuilder() {
        super();
    }

    public DynamicControlBuilder kind(ControlKind kind) {
        this.kind = kind;
        return self();
    }

    @Override
    public DynamicSignatureBuilder assign() {
        return (DynamicSignatureBuilder) super.assign();
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
