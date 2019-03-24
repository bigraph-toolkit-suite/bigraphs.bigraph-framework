package de.tudresden.inf.st.bigraphs.core.exceptions.building;

public class OuterNameNotExistsException extends LinkTypeNotExistsException {

    public OuterNameNotExistsException() {
        super("The given outername doesn't exists.");
    }
}
