package de.tudresden.inf.st.bigraphs.core.exceptions.builder;

/**
 * @author Dominik Grzelak
 */
public class OuterNameNotExistsException extends LinkTypeNotExistsException {

    public OuterNameNotExistsException() {
        super("The given outername doesn't exists.");
    }
}
