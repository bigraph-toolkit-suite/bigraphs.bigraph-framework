package org.bigraphs.framework.core.exceptions.builder;

/**
 * @author Dominik Grzelak
 */
public class OuterNameNotExistsException extends LinkTypeNotExistsException {

    public OuterNameNotExistsException() {
        super("The given outername doesn't exists.");
    }
}
