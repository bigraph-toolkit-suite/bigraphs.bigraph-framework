package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;

import java.util.Objects;

/**
 * Immutable dynamic control. Status of a control can be specified. If non provided the control will be active.
 * <p>
 * A node can be atomic or non-atomic which is determined by its control. Atomic nodes are empty. Non-atomic nodes
 * can be active or passive.
 *
 * @author Dominik Grzelak
 */
public class DefaultDynamicControl extends AbstractControl<StringTypedName, FiniteOrdinal<Integer>> {

    private final ControlKind kindOfControl;

    /**
     * Status will be set to {@link ControlKind#ACTIVE}
     *
     * @param name  the label of the control
     * @param arity the arity of the control
     */
    protected DefaultDynamicControl(StringTypedName name, FiniteOrdinal<Integer> arity) {
        this(name, arity, ControlKind.ACTIVE);
    }

    private DefaultDynamicControl(StringTypedName name, FiniteOrdinal<Integer> arity, ControlKind kindOfControl) {
        super(name, arity);
        if (Objects.isNull(kindOfControl)) {
            kindOfControl = ControlKind.ACTIVE;
        }
        this.kindOfControl = kindOfControl;
    }

    public static DefaultDynamicControl createDefaultDynamicControl(StringTypedName name, FiniteOrdinal<Integer> arity,
                                                                    ControlKind kindOfControl) {
        return new DefaultDynamicControl(name, arity, kindOfControl);
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
        DefaultDynamicControl that = (DefaultDynamicControl) o;
        return kindOfControl == that.kindOfControl;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), kindOfControl);
    }
}
