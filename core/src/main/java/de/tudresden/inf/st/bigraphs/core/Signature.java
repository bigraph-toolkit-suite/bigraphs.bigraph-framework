package de.tudresden.inf.st.bigraphs.core;

import java.util.Iterator;
import java.util.Set;

/**
 * Common interface of a bigraph's signature.
 *
 * @param <C> type of the control
 * @author Dominik Grzelak
 */
public interface Signature<C extends Control> {

    /**
     * Get the controls of the signature.
     *
     * @return control set of the signature
     */
    Set<C> getControls();

    /**
     * Get the control by its string identifier
     *
     * @param name the identifier of the control
     * @return the corresponding control
     */
    default C getControlByName(String name) {
        C selected = null;
        Iterator<C> iterator = getControls().iterator();
        while (iterator.hasNext()) {
            C next1 = iterator.next();
            if (next1.getNamedType().stringValue().equals(name)) {
                selected = next1;
                break;
            }
        }
        return selected;
    }
}
