package de.tudresden.inf.st.bigraphs.core.reactivesystem;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;

public class ConditionalParametricRuleDecorator<B extends Bigraph<? extends Signature<?>>> extends ParametricReactionRule<B> {

    public interface RuleConditionMatcher<B extends Bigraph<? extends Signature<?>>> {
        boolean conditionIsSatisfied(BigraphMatch<B> match);
    }

    private final ParametricReactionRule<B> reactionRule;

    private final RuleConditionMatcher<B> conditionMatcher;

    public ConditionalParametricRuleDecorator(ParametricReactionRule<B> reactionRule) throws InvalidReactionRuleException {
        this(reactionRule, match -> true);
    }

    public ConditionalParametricRuleDecorator(ParametricReactionRule<B> reactionRule, RuleConditionMatcher<B> conditionMatcher) throws InvalidReactionRuleException {
        super(reactionRule.getRedex(), reactionRule.getReactum(), reactionRule.getInstantationMap(), reactionRule.isReversible());
        this.reactionRule = reactionRule;
        this.conditionMatcher = conditionMatcher;
    }

    public boolean isMatchValid(BigraphMatch<B> match) {
        return this.conditionMatcher.conditionIsSatisfied(match);
    }

    /**
     * Returns the underlying reaction rule.
     *
     * @return the reaction rule of the conditional decorator
     */
    public ParametricReactionRule<B> getReactionRule() {
        return reactionRule;
    }

    public RuleConditionMatcher<B> getConditionMatcher() {
        return conditionMatcher;
    }
}