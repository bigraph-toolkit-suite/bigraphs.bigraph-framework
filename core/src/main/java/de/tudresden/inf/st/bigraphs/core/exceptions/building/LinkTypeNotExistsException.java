package de.tudresden.inf.st.bigraphs.core.exceptions.building;

public class LinkTypeNotExistsException extends TypeNotExistsException {

    public LinkTypeNotExistsException() {
        this("This bigraph link type doesn't exists.");
    }

    public LinkTypeNotExistsException(String message) {
        super(message);
    }
}
