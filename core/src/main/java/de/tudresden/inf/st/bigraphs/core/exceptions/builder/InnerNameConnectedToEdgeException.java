package de.tudresden.inf.st.bigraphs.core.exceptions.builder;

import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;

/**
 * When the user is trying to connect an inner name to an outer name where the inner name is already
 * connected to an edge which connects nodes or other inner names.
 *
 * @author Dominik Grzelak
 */
public class InnerNameConnectedToEdgeException extends InvalidConnectionException {

    public InnerNameConnectedToEdgeException() {
        super("The given inner name is already connected to an edge. And might be linking to other nodes or inner names." +
                "Thus, the inner name cannot be connected to an outername");
    }
}
