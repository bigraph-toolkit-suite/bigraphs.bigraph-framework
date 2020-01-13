package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.AbstractSignature;
import de.tudresden.inf.st.bigraphs.core.ControlKind;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;

import java.util.Collections;
import java.util.Set;

/**
 * <strong>Note:</strong> This class is not yet implemented!
 * <p>
 * Points can be bound in binding bigraphs and are represented by this signature type.
 * <p>
 * According to Milner, a binding implies that for a node it has arity 0 and it is passive.
 * <p>
 * Can only be used within binding bigraphs.
 *
 * @author Dominik Grzelak
 */
public class BindingSignature extends AbstractSignature<BindingControl<? extends NamedType, ? extends FiniteOrdinal>> {

    public BindingSignature(Set<BindingControl<? extends NamedType, ? extends FiniteOrdinal>> controls) {
        super(controls);
    }

    public boolean isBindingControl(BindingControl<? extends NamedType, ? extends FiniteOrdinal> control) {
        if (!getControls().contains(control)) return false;
        BindingControl<? extends NamedType, ? extends FiniteOrdinal> controlByName = getControlByName(control.getNamedType().stringValue());
        return controlByName.isBindingControl();
    }
}
