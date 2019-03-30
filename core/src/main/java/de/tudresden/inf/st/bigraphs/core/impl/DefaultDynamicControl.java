package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;

import java.util.Objects;

/**
 * Immutable dynamic control. Status of a control can be specified. If non provided the control will be active.
 *
 * @param <NT>
 * @param <FO>
 */
public class DefaultDynamicControl<NT extends NamedType, FO extends FiniteOrdinal> extends AbstractControl<NT, FO> {

    private final ControlKind kindOfControl;

    /**
     * Status will be set to {@link ControlKind#ACTIVE}
     *
     * @param name
     * @param arity
     */
    protected DefaultDynamicControl(NT name, FO arity) {
        this(name, arity, ControlKind.ACTIVE);
    }

    private DefaultDynamicControl(NT name, FO arity, ControlKind kindOfControl) {
        super(name, arity);
        this.kindOfControl = kindOfControl;
    }

    public static <NT extends NamedType, FO extends FiniteOrdinal> DefaultDynamicControl<NT, FO> createDefaultDynamicControl(NT name, FO arity, ControlKind kindOfControl) {
        return new DefaultDynamicControl<>(name, arity, kindOfControl);
    }

    @Override
    public ControlKind getControlKind() {
        return kindOfControl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultDynamicControl)) return false;
        if (!super.equals(o)) return false;
        DefaultDynamicControl<?, ?> that = (DefaultDynamicControl<?, ?>) o;
        return kindOfControl == that.kindOfControl;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), kindOfControl);
    }
}
