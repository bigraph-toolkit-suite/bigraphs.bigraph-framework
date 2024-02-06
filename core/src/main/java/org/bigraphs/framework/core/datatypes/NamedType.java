package org.bigraphs.framework.core.datatypes;

/**
 * Data type for an element of a name set which is used within an interface of a bigraph.
 * <p>
 * This data type is used for all link types of a bigraph.
 *
 * @param <V> type of the element of such a name set.
 * @author Dominik Grzelak
 */
public interface NamedType<V> {
    V getValue();

    String stringValue();
}
