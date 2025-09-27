package org.bigraphs.framework.core.impl.signature;

import org.bigraphs.framework.core.ControlBuilder;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;

/**
 * Counterpart to {@link DynamicControlBuilder} for constructing controls
 * in kind signatures, i.e. signatures where a place sort can be specified
 * for each control.
 *
 * @author Dominik Grzelak
 */
public class KindControlBuilder extends ControlBuilder<StringTypedName, FiniteOrdinal<Integer>, KindControlBuilder> {

    protected KindControlBuilder() {
        super();
    }

    @Override
    public KindSignatureBuilder assign() {
        return (KindSignatureBuilder) super.assign();
    }

    public KindControlBuilder identifier(String name) {
        return super.identifier(StringTypedName.of(name));
    }

    public KindControlBuilder arity(Integer arity) {
        return super.arity(FiniteOrdinal.ofInteger(arity));
    }

    @Override
    protected DynamicControl build() {
        return DynamicControl.createDynamicControl(getType(), getArity(), ControlStatus.ACTIVE);
    }
}
