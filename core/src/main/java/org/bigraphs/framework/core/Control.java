package org.bigraphs.framework.core;

import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.NamedType;

/**
 * Superinterface for all control representations.
 * <p>
 * A control is a {@link HasIdentifier} with label type {@code <NT>} and arity type {@code <T>}.
 *
 * @param <NT> the label type
 * @param <T>  the arity type
 * @author Dominik Grzelak
 */
public interface Control<NT extends NamedType, T extends FiniteOrdinal> extends HasIdentifier<NT> {

    /**
     * Returns the arity of the control
     *
     * @return the arity of type {@code T}
     */
    T getArity();

    /**
     * Returns the kind of the control.
     *
     * @return kind of the control
     */
    ControlStatus getControlKind();

}
