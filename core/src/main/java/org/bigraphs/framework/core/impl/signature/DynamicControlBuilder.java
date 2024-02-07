package org.bigraphs.framework.core.impl.signature;

import org.bigraphs.framework.core.ControlBuilder;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;

/**
 * The concrete dynamic control builder used by the {@link DynamicSignatureBuilder}.
 * It allows to specify if a control is active, passive or atomic.
 *
 * @author Dominik Grzelak
 * @see DynamicSignatureBuilder
 */
public class DynamicControlBuilder extends ControlBuilder<StringTypedName, FiniteOrdinal<Integer>, DynamicControlBuilder> {
    private ControlStatus status;

    protected DynamicControlBuilder() {
        super();
    }

    public DynamicControlBuilder status(ControlStatus status) {
        this.status = status;
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
        return DefaultDynamicControl.createDefaultDynamicControl(getType(), getArity(), this.status);
    }
}
