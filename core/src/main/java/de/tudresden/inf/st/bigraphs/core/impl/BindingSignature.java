package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.AbstractSignature;
import de.tudresden.inf.st.bigraphs.core.ControlKind;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;

import java.util.Set;

/**
 * Points can be bound in binding bigraphs and are represented by this signature type.
 * <p>
 * According to Milner, a binding implies that for a node it has arity 0 and it is passive.
 * <p>
 * Can only be used within binding bigraphs.
 */
public class BindingSignature extends AbstractSignature<DefaultDynamicControl<? extends NamedType, ? extends FiniteOrdinal>> {

    public BindingSignature(Set<DefaultDynamicControl<? extends NamedType, ? extends FiniteOrdinal>> controls) {
        super(controls);
    }

    public boolean isBinding(DefaultDynamicControl<? extends NamedType, ? extends FiniteOrdinal> control) {
        if (!getControls().contains(control)) return false;
        DefaultDynamicControl<? extends NamedType, ? extends FiniteOrdinal> controlByName = getControlByName(control.getNamedType().stringValue());
        return controlByName.getArity().equals(FiniteOrdinal.ofInteger(0)) && controlByName.getControlKind().equals(ControlKind.PASSIVE);
    }
}