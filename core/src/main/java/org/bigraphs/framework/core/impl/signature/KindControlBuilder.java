package org.bigraphs.framework.core.impl.signature;

import org.bigraphs.framework.core.ControlBuilder;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;

/**
 * Analog implementation to {@link DynamicControlBuilder} for controls of kind signatures.
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
