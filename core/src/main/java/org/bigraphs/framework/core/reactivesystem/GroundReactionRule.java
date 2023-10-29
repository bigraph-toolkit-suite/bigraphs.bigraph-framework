package org.bigraphs.framework.core.reactivesystem;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;

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
