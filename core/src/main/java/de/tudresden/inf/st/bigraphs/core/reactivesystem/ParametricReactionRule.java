package de.tudresden.inf.st.bigraphs.core.reactivesystem;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;

/**
 * Concrete implementation of a pamarametric reaction rule.
 *
 * @param <B> type of the bigraph
 * @author Dominik Grzelak
 */
public class ParametricReactionRule<B extends Bigraph<? extends Signature<?>>> extends AbstractReactionRule<B> {

    public ParametricReactionRule(B redex, B reactum) throws InvalidReactionRuleException {
        super(redex, reactum);
    }

    public ParametricReactionRule(B redex, B reactum, InstantiationMap instantiationMap) throws InvalidReactionRuleException {
        super(redex, reactum, instantiationMap);
    }

    public ParametricReactionRule(B redex, B reactum, InstantiationMap instantiationMap, boolean isReversible) throws InvalidReactionRuleException {
        super(redex, reactum, instantiationMap, isReversible);
    }

    public ParametricReactionRule(B redex, B reactum, boolean isReversible) throws InvalidReactionRuleException {
        super(redex, reactum, isReversible);
    }
}
