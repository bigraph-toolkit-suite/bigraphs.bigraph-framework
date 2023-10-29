package org.bigraphs.framework.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class IncompatibleSignatureException extends InvalidReactionRuleException {

    public IncompatibleSignatureException() {
        super("Signatures of the redex and reactum doesn't match."); //redex, reactum, outer inner bigraph for composition
    }
}
