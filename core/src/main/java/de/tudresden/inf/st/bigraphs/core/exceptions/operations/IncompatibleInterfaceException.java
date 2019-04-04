package de.tudresden.inf.st.bigraphs.core.exceptions.operations;

public class IncompatibleInterfaceException extends Exception {
    public IncompatibleInterfaceException() {
        super("Interface not compatible");
    }

    public IncompatibleInterfaceException(String message) {
        super(message);
    }
}