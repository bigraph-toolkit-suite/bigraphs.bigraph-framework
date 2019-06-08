package de.tudresden.inf.st.bigraphs.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class RedexIsNotSimpleException extends InvalidReactionRuleException {

    public RedexIsNotSimpleException() {
        super("The redex is not simple. It is either not open, guarding, or inner-injective.");
    }
}
