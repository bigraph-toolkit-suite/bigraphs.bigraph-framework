package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;

public interface Control<NT extends NamedType, T extends FiniteOrdinal> extends NamedEntity<NT> {
    T getArity();
    ControlKind getControlKind();

}
