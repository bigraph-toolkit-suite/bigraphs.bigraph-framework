package org.bigraphs.framework.core.exceptions;

/**
 * Exception that is thrown when it is tried to nest a another node within an atomic node.
 *
 * @author Dominik Grzelak
 */
public class ControlIsAtomicException extends RuntimeException {

    public ControlIsAtomicException() {
        super("Control is atomic");
    }
}
