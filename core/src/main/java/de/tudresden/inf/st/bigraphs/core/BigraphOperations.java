package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;

public interface BigraphOperations<S extends Signature> {

    /**
     * Composes two bigraphs where a new immutable bigraph is created.
     *
     * @param left  the left-hand side
     * @param right the right-hand side
     * @return a new bigraph composed of both arguments
     */
    Bigraph<S> compose(Bigraph<S> left, Bigraph<S> right) throws IncompatibleSignatureException, IncompatibleInterfaceException;
}
