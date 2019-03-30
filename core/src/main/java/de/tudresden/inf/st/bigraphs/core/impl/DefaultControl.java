package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;

/**
 * Immutable class. Cannot be overridden or instantiated directly.
 * Not for dynamic signatures. These kind of controls are always active or passive. The "status" cannot be changed.
 */
@Deprecated //can be replace by dynamic control and basic signature ensures that all controls have the same kind
public final class DefaultControl<NT extends NamedType, FO extends FiniteOrdinal> extends AbstractControl<NT, FO> {

    private DefaultControl(NT name, FO artiy) {
        super(name, artiy);
    }

    public static <NT extends NamedType, FO extends FiniteOrdinal> DefaultControl<NT, FO> createDefaultControl(NT name, FO artiy) {
        return new DefaultControl<>(name, artiy);
    }

    @Override
    public ControlKind getControlKind() {
        return ControlKind.ACTIVE;
    }

}
