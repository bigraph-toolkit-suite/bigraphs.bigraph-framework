package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;

/**
 * Concrete implementation of a ground reaction rule.
 *
 * @param <B> type of the bigraph
 * @author Dominik Grzelak
 */
public class GroundReactionRule<B extends Bigraph<? extends Signature<?>>> extends AbstractReactionRule<B> {

    public GroundReactionRule(B redex, B reactum) throws InvalidReactionRuleException {
        super(redex, reactum);
    }
}
