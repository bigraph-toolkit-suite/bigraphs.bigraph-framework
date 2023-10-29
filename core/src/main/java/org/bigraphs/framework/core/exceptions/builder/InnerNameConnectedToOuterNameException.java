package org.bigraphs.framework.core.exceptions.builder;

import org.bigraphs.framework.core.exceptions.InvalidConnectionException;

/**
 * @author Dominik Grzelak
 */
public class InnerNameConnectedToOuterNameException extends InvalidConnectionException {

    public InnerNameConnectedToOuterNameException() {
        super("The given inner name is already connected to an outer name.");
    }
}
