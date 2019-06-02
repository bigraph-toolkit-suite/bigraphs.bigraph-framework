package de.tudresden.inf.st.bigraphs.core.exceptions;

public class OuterNameIsIdleException extends InvalidReactionRuleException {

    public OuterNameIsIdleException() {
        super("Outer name is idle which is not allow for the redex.");
    }
}
