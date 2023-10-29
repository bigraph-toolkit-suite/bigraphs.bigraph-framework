package org.bigraphs.framework.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class NonConformReactionRuleInterfaces extends InvalidReactionRuleException {

    public NonConformReactionRuleInterfaces() {
        super("Reaction rule are not conform to the following definition: R: m -> J and R': m' -> J");
    }
}
