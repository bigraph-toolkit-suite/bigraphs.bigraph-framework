package de.tudresden.inf.st.bigraphs.core.impl.signature;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
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

    private final ControlStatus statusOfControl;

    int hashed = -1;

    /**
     * Status will be set to {@link ControlStatus#ACTIVE}
     *
     * @param name  the label of the control
     * @param arity the arity of the control
     */
    protected DefaultDynamicControl(StringTypedName name, FiniteOrdinal<Integer> arity) {
        this(name, arity, ControlStatus.ACTIVE);
    }

    private DefaultDynamicControl(StringTypedName name, FiniteOrdinal<Integer> arity, ControlStatus statusOfControl) {
        super(name, arity);
        if ((statusOfControl) == null) {
            statusOfControl = ControlStatus.ACTIVE;
        }
        this.statusOfControl = statusOfControl;
    }

    public static DefaultDynamicControl createDefaultDynamicControl(StringTypedName name, FiniteOrdinal<Integer> arity,
                                                                    ControlStatus kindOfControl) {
        return new DefaultDynamicControl(name, arity, kindOfControl);
    }

    @Override
    public ControlStatus getControlKind() {
        return statusOfControl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultDynamicControl)) return false;
        if (!super.equals(o)) return false;
        DefaultDynamicControl that = (DefaultDynamicControl) o;
        return statusOfControl == that.statusOfControl;
    }

    @Override
    public int hashCode() {
        if(hashed == -1) {
            hashed = Objects.hash(super.hashCode(), statusOfControl);
        }
        return hashed;
//        return Objects.hash(super.hashCode(), statusOfControl);
    }
}
