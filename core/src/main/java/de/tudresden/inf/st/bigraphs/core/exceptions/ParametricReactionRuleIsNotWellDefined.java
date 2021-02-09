package de.tudresden.inf.st.bigraphs.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class ParametricReactionRuleIsNotWellDefined extends InvalidReactionRuleException {
    public ParametricReactionRuleIsNotWellDefined() {
        super("The redex or reactum are not lean, or the redex has idle roots or idle names.");
    }
}
