package de.tudresden.inf.st.bigraphs.core.exceptions;

public class RedexIsNotSimpleException extends ReactionRuleException {

    public RedexIsNotSimpleException() {
        super("The redex is not simple. It is either not open, guarding, or inner-injective.");
    }
}
