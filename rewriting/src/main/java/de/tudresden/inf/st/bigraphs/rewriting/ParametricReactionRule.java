package de.tudresden.inf.st.bigraphs.rewriting;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;

/**
 * @param <S>
 * @author Dominik Grzelak
 */
public class ParametricReactionRule<S extends Signature> extends AbstractReactionRule<S> {

    public ParametricReactionRule(Bigraph<S> redex, Bigraph<S> reactum) throws InvalidReactionRuleException {
        super(redex, reactum);
    }
    
}
