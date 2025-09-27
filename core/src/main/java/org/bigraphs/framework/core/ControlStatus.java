package org.bigraphs.framework.core;

/**
 * Enum of control <i>status</i> values.
 *
 * @author Dominik Grzelak
 */
public enum ControlStatus {

    ATOMIC, ACTIVE, PASSIVE;

    /**
     * Returns {@code true} if the control is atomic, meaning, that nothing may be nested within a node incorporating
     * this control.
     *
     * @param control the control to check
     * @return {@true} if the control is atomic, otherwise {@code false}
     */
    public static boolean isAtomic(Control control) {
        return control.getControlKind() == ControlStatus.ATOMIC;
    }

    public static boolean isActive(Control control) {
        return control.getControlKind() == ControlStatus.ACTIVE;
    }

    public static ControlStatus fromString(String value) {
        if (value.equalsIgnoreCase("atomic")) return ATOMIC;
        if (value.equalsIgnoreCase("active")) return ACTIVE;
        if (value.equalsIgnoreCase("passive")) return PASSIVE;
        return ACTIVE;
    }
}
