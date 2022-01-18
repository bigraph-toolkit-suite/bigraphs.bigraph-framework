package de.tudresden.inf.st.bigraphs.core.reactivesystem;

/**
 * Marker interface that indicates the object has a label.
 *
 * @author Dominik Grzelak
 */
public interface HasLabel {
    String getLabel();

    /**
     * Determines if the label is set or not.
     *
     * @return {@code true}, if the label was set, i.e., it is not {@code null} or blank, containing only of whitespaces.
     */
    default boolean isDefined() {
        return getLabel() != null && !getLabel().isBlank();
    }
}
