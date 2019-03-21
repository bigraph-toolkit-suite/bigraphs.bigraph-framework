package de.tudresden.inf.st.bigraphs.core.exceptions;

public class IncompatibleSignatureException extends ReactionRuleException {

    public IncompatibleSignatureException() {
        super("Signatures of Redex and Reactum doesn't match.");
    }
}
