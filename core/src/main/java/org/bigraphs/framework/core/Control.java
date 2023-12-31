package org.bigraphs.framework.core;

import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.NamedType;

/**
 * Super interface for all control representations.
 * <p>
 * A control is a {@link HasIdentifier} of type {@code <NT>} and has an arity of type {@code <T>}.
 *
 * @param <NT> type of the label
 * @param <T>  type of the arity
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
