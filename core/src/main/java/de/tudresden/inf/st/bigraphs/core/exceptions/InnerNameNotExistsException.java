package de.tudresden.inf.st.bigraphs.core.exceptions;

import de.tudresden.inf.st.bigraphs.core.exceptions.building.LinkTypeNotExistsException;

public class InnerNameNotExistsException extends LinkTypeNotExistsException {

    public InnerNameNotExistsException() {
        super("The given inner name doesn't exists.");
    }
}
