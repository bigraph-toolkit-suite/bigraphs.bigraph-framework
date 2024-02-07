package org.bigraphs.framework.simulation.exceptions;

import org.bigraphs.framework.core.exceptions.ReactiveSystemException;

/**
 * @author Dominik Grzelak
 */
public class InvalidSimulationStrategy extends ReactiveSystemException {

    public InvalidSimulationStrategy() {
        super("The simulation strategy is not valid. Must not be null.");
    }
}
