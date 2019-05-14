package de.tudresden.inf.st.bigraphs.core.exceptions;

public class IncompatibleSignatureException extends ReactionRuleException {

    public IncompatibleSignatureException() {
        super("Signatures doesn't match."); //redex, reactum, outer inner bigraph for composition
    }
}
