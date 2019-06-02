package de.tudresden.inf.st.bigraphs.core.exceptions;

public class IncompatibleSignatureException extends InvalidReactionRuleException {

    public IncompatibleSignatureException() {
        super("Signatures of the redex and reactum doesn't match."); //redex, reactum, outer inner bigraph for composition
    }
}
