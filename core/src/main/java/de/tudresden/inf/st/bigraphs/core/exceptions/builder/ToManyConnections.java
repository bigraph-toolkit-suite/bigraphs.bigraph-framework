package de.tudresden.inf.st.bigraphs.core.exceptions.builder;

import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidArityOfControlException;

/**
 * Exception is thrown inside a builder when the node has no free ports but a connection to a link (i.e., edge or outer name)
 * is tried to make.
 * <p>
 * The exception is not thrown if a control is atomic (see {@link de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException}).
 *
 * @author Dominik Grzelak
 * @see de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException
 */
public class ToManyConnections extends InvalidArityOfControlException {

    public ToManyConnections() {
        super("To many connections on this node");
    }
}
