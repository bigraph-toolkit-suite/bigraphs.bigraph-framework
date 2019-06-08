package de.tudresden.inf.st.bigraphs.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class NoConformReactionRuleInterfaces extends InvalidReactionRuleException {

    public NoConformReactionRuleInterfaces() {
        super("Reaction rule are not conform to the following definition: R: m -> J and R': m' -> J");
    }
}
