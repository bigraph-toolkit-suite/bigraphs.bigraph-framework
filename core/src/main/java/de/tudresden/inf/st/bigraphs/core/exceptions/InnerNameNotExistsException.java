package de.tudresden.inf.st.bigraphs.core.exceptions;

public class InnerNameNotExistsException extends LinkTypeNotExistsException {

    public InnerNameNotExistsException() {
        super("The given inner name doesn't exists.");
    }
}
