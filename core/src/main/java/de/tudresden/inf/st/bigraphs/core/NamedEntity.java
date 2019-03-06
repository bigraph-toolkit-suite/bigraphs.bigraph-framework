package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;

public interface NamedEntity<NT extends NamedType> {
    NT getNamedType();
}
