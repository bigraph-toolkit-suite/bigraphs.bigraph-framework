package de.tudresden.inf.st.bigraphs.core.exceptions;

public class LinkTypeNotExistsException extends Exception {

    public LinkTypeNotExistsException() {
        this("This bigraph type doesn't exists.");
    }

    public LinkTypeNotExistsException(String message) {
        super(message);
    }
}
