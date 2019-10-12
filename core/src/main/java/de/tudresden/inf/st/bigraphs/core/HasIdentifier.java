package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;

/**
 * Interface for entities that have an identifier, meaning,
 * they can be labelled. Entities can be, e.g., nodes.
 *
 * @param <NT> type of the named value
 * @author Dominik Grzelak
 */
public interface HasIdentifier<NT extends NamedType> {
    NT getNamedType();
}
