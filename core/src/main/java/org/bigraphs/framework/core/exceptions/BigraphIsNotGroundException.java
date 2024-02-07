package org.bigraphs.framework.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class BigraphIsNotGroundException extends RuntimeException {
    public BigraphIsNotGroundException() {
        super("Bigraph is not ground.");
    }
}
