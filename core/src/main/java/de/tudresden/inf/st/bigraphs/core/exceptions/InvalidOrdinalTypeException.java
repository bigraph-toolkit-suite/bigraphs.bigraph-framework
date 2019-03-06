package de.tudresden.inf.st.bigraphs.core.exceptions;

public class InvalidOrdinalTypeException extends RuntimeException {

    public InvalidOrdinalTypeException() {
        super("Ordinal type is not valid. Must be an integer or long.");
    }
}
