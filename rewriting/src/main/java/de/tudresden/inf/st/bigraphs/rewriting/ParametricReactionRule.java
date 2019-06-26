package de.tudresden.inf.st.bigraphs.rewriting;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;

/**
 * @param <B>
 * @author Dominik Grzelak
 */
public class ParametricReactionRule<B extends Bigraph<? extends Signature>> extends AbstractReactionRule<B> {

    public ParametricReactionRule(B redex, B reactum) throws InvalidReactionRuleException {
        super(redex, reactum);
    }
    
}
