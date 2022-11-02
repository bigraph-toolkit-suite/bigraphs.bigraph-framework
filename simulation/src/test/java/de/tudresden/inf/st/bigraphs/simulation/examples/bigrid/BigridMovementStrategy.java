package de.tudresden.inf.st.bigraphs.simulation.examples.bigrid;

import java.util.List;

// Applies the strategy pattern to provide different movement strategies in form of rule sets.
// Each rule set indicate a specific form of movement
public interface BigridMovementStrategy {

    // a bunch of pre-defined  rule collections ... (statically and immutable)
    class Defaults {
        static final public List DIRECT_FORWARD = null;
        static final public List DIRECT_BACKWARD = null;
        static final public List DIRECT_LEFT = null;
        static final public List DIRECT_RIGHT = null;
    }
}
