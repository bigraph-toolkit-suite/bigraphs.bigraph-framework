package de.tudresden.inf.st.bigraphs.core.exceptions;

public class InstantiationMapIsNotWellDefined extends InvalidReactionRuleException {

    public InstantiationMapIsNotWellDefined() {
        super("The instantiation map of the parametric reaction rule is not well-defined. Please check whether the map is complete.");
    }
}
