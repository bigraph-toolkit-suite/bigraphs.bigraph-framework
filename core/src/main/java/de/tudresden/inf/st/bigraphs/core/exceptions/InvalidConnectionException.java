package de.tudresden.inf.st.bigraphs.core.exceptions;

/**
 * Base exception class for any invalid connection. For example, arity related.
 *
 * @author Dominik Grzelak
 */
public abstract class InvalidConnectionException extends Exception {
    public InvalidConnectionException() {
    }

    public InvalidConnectionException(String message) {
        super(message);
    }
}
