package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;

import java.util.Objects;
import java.util.Set;

/**
 * Common interface of a bigraph's signature.
 * This interface is general, and not technology-specific such as {@link EcoreSignature}.
 *
 * @param <C> type of the control
 * @author Dominik Grzelak
 */
public interface Signature<C extends Control<?, ?>> {

    /**
     * Get the controls of the signature.
     *
     * @return control set of the signature
     */
    Set<C> getControls();

    /**
     * Get the control by its string identifier
     *
     * @param name the identifier of the control
     * @return the corresponding control
     */
    default C getControlByName(String name) {
        for (C next1 : getControls()) {
            if (next1.getNamedType().stringValue().equals(name)) {
                return next1;
            }
        }
        return null;
    }

    default C getControl(String name, int arity) {
        for (C next1 : getControls()) {
            if (next1.getNamedType().stringValue().equals(name) &&
                    next1.getArity().getValue().intValue() == arity) {
                return next1;
            }
        }
        return null;
    }

    default C getControl(String name, int arity, ControlStatus controlStatus) {
        for (C next1 : getControls()) {
            if (next1.getNamedType().stringValue().equals(name) &&
                    next1.getArity().getValue().intValue() == arity &&
                    next1.getControlKind().equals(controlStatus)) {
                return next1;
            }
        }
        return null;
    }

    default FiniteOrdinal<?> getArity(String controlName) {
        C controlByName = getControlByName(controlName);
        if (Objects.nonNull(controlByName)) {
            return controlByName.getArity();
        }
        return null;
    }

    default FiniteOrdinal<?> getArity(C control) {
        if (getControls().contains(control)) {
            return control.getArity();
        }
        return null;
    }
}
