package de.tudresden.inf.st.bigraphs.simulation.exceptions;

import de.tudresden.inf.st.bigraphs.core.exceptions.ReactiveSystemException;

/**
 * @author Dominik Grzelak
 */
public class InvalidSimulationStrategy extends ReactiveSystemException {

    public InvalidSimulationStrategy() {
        super("The simulation strategy is not valid. Must not be null.");
    }
}
