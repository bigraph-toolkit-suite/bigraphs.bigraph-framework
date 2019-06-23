package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;

/**
 * Interface for entities that can be labelled, e.g., nodes or names.
 *
 * @param <NT> type of the named value
 * @author Dominik Grzelak
 */
public interface NamedEntity<NT extends NamedType> {
    NT getNamedType();
}
