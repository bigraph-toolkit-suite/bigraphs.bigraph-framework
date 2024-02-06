package org.bigraphs.framework.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class OuterNameIsIdleException extends InvalidReactionRuleException {

    public OuterNameIsIdleException() {
        super("Outer name is idle which is not allow for the redex.");
    }
}
