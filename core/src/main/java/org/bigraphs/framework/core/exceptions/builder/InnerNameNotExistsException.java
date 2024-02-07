package org.bigraphs.framework.core.exceptions.builder;

/**
 * @author Dominik Grzelak
 */
public class InnerNameNotExistsException extends LinkTypeNotExistsException {

    public InnerNameNotExistsException() {
        super("The given inner name doesn't exists.");
    }
}
