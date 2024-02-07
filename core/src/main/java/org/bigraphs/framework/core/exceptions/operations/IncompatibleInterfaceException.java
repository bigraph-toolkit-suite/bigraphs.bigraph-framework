package org.bigraphs.framework.core.exceptions.operations;

/**
 * @author Dominik Grzelak
 */
public class IncompatibleInterfaceException extends Exception {
    public IncompatibleInterfaceException() {
        super("Interface not compatible");
    }

    public IncompatibleInterfaceException(String message) {
        super(message);
    }
}
