package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.ControlBuilder;
import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;

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
    protected DefaultDynamicControl build() {
        return DefaultDynamicControl.createDefaultDynamicControl(getType(), getArity(), ControlStatus.ACTIVE);
    }
}
