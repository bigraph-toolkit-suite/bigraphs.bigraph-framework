package org.bigraphs.framework.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class InvalidOrdinalTypeException extends RuntimeException {

    public InvalidOrdinalTypeException() {
        super("Ordinal type is not valid. Must be an integer or long.");
    }
}
