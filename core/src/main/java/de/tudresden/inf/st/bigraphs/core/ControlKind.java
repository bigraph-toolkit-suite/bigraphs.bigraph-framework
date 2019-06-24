package de.tudresden.inf.st.bigraphs.core;

/**
 * Kind of a control
 *
 * @author Dominik Grzelak
 */
public enum ControlKind {
    ATOMIC, ACTIVE, PASSIVE;

    /**
     * Returns {@code true} if the control is atomic, meaning, that nothing may be nested within a node incorporating
     * this control.
     *
     * @param control the control to check
     * @return {@true} if the control is atomic, otherwise {@code false}
     */
    public static boolean isAtomic(Control control) {
        return control.getControlKind() == ControlKind.ATOMIC;
    }

    public static boolean isActive(Control control) {
        return control.getControlKind() == ControlKind.ACTIVE;
    }
}
