package de.tudresden.inf.st.bigraphs.core.exceptions.builder;

import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;

/**
 * @author Dominik Grzelak
 */
public class InnerNameConnectedToOuterNameException extends InvalidConnectionException {

    public InnerNameConnectedToOuterNameException() {
        super("The given inner name is already connected to an outer name.");
    }
}
