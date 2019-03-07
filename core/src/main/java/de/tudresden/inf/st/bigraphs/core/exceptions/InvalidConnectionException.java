package de.tudresden.inf.st.bigraphs.core.exceptions;

public abstract class InvalidConnectionException extends Exception {
    public InvalidConnectionException() {
    }

    public InvalidConnectionException(String message) {
        super(message);
    }
}
