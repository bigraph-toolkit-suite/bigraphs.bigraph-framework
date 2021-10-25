package de.tudresden.inf.st.bigraphs.core.exceptions.builder;

public class ControlNotExistsException extends TypeNotExistsException {

    public ControlNotExistsException() {
        super("The given control doesn't exists.");
    }

    public ControlNotExistsException(String value) {
        super(String.format("The given control %s doesn't exists.", value));
    }
}
