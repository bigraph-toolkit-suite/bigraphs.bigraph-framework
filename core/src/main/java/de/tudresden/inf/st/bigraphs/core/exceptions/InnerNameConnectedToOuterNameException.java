package de.tudresden.inf.st.bigraphs.core.exceptions;

public class InnerNameConnectedToOuterNameException extends InvalidConnectionException {

    public InnerNameConnectedToOuterNameException() {
        super("The given inner name is already connected to an outer name.");
    }
}
