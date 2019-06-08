package de.tudresden.inf.st.bigraphs.core.exceptions.builder;

import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidArityOfControlException;

//TODO: node related...

/**
 * @author Dominik Grzelak
 */
public class ToManyConnections extends InvalidArityOfControlException {

    public ToManyConnections() {
        super("To many connections on this node");
    }
}
