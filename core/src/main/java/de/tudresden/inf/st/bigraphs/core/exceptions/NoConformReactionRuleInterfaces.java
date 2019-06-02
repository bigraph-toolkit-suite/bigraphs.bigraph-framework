package de.tudresden.inf.st.bigraphs.core.exceptions;

public class NoConformReactionRuleInterfaces extends ReactionRuleException {

    public NoConformReactionRuleInterfaces() {
        super("Reaction rule are not conform to the following definition: R: m -> J and R': m' -> J");
    }
}
