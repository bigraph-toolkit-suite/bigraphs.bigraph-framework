package org.bigraphs.framework.core;

import org.bigraphs.framework.core.datatypes.NamedType;

/**
 * Interface for entities with an identifier.
 * <p>
 * Typical examples include nodes or controls.
 *
 * @param <NT> the named type of the identifier
 * @author Dominik Grzelak
 */
public interface HasIdentifier<NT extends NamedType> {

    NT getNamedType();
}
