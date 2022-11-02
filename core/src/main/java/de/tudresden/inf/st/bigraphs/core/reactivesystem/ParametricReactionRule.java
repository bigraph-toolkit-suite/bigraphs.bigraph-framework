package de.tudresden.inf.st.bigraphs.core.reactivesystem;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.ParametricReactionRuleIsNotWellDefined;

/**
 * Concrete implementation of a parametric reaction rule.
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

    // We need to relax this constraint because jLibBig handles this differently
    @Override
    public boolean isProperParametricRule() {
        return getRedex().isLean() && getReactum().isLean() && // both must be lean
                getRedex().getRoots().stream().allMatch(x -> getRedex().getChildrenOf(x).size() > 0); // only the redex must have no idle roots and names
    }

    // We need to relax this constraint because jLibBig handles this differently
    @Override
    public boolean isRedexSimple() {
        return
//                getRedex().getEdges().size() == 0 && // every link is open
                        getRedex().isGuarding() && //no site has a root as parent (+ no idle inner names)
                        getRedex().isMonomorphic(); // inner-injective

    }
}
