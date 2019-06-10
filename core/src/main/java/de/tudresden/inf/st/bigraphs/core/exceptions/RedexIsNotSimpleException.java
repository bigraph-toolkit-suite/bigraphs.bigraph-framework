package de.tudresden.inf.st.bigraphs.core.exceptions;

/**
 * Exception that indicates that a redex of a parametric reaction rule is not simple.
 *
 * @author Dominik Grzelak
 */
public class RedexIsNotSimpleException extends InvalidReactionRuleException {

    public RedexIsNotSimpleException() {
        super("The redex is not simple. It is either not open, guarding, or inner-injective.");
    }
}
