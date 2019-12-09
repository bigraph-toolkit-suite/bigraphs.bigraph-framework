package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.exceptions;

/**
 * @author Dominik Grzelak
 */
public abstract class BigraphSimulationException extends Exception {

    public BigraphSimulationException() {
    }

    public BigraphSimulationException(String message) {
        super(message);
    }
}
