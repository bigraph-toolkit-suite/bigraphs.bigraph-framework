package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.AbstractControl;
import de.tudresden.inf.st.bigraphs.core.ControlKind;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;

public class BindingControl<NT extends NamedType, FO extends FiniteOrdinal> extends DefaultDynamicControl<NT, FO> {

    protected BindingControl(NT name, FO arity) {
        super(name, arity);
    }

    public boolean isBindingControl() {
        return getArity().equals(FiniteOrdinal.ofInteger(0)) && getControlKind().equals(ControlKind.PASSIVE);
    }
}
