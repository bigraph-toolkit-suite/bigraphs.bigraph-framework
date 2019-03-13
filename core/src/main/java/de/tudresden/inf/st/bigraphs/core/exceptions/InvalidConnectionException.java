package de.tudresden.inf.st.bigraphs.core.exceptions;

/**
 * Base exception class for any invalid connection. For example, arity related.
 */
public abstract class InvalidConnectionException extends Exception {
    public InvalidConnectionException() {
    }

    public InvalidConnectionException(String message) {
        super(message);
    }
}
