package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.AbstractControl;
import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;

/**
 * <strong>Note:</strong> This class is not yet implemented!
 * <p>
 * A binding control for a binding signature for binding bigraphs.
 *
 * @param <NT> type of the label
 * @param <FO> type of the arity
 * @author Dominik Grzelak
 */
public class BindingControl<NT extends NamedType<?>, FO extends FiniteOrdinal<?>> extends AbstractControl<NT, FO> {

    protected BindingControl(NT name, FO arity) {
        super(name, arity);
    }

    public boolean isBindingControl() {
        return getArity().equals(FiniteOrdinal.ofInteger(0)) && getControlKind().equals(ControlStatus.PASSIVE);
    }

    @Override
    public ControlStatus getControlKind() {
        throw new RuntimeException("Not yet implemented");
    }
}
