package org.bigraphs.framework.core.reactivesystem;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;

public class ConditionalParametricRuleDecorator<B extends Bigraph<? extends Signature<?>>> extends ParametricReactionRule<B> {

    public interface RuleConditionMatcher<B extends Bigraph<? extends Signature<?>>> {
        boolean conditionIsSatisfied(B theAgent, ReactionRule<B> theRule, BigraphMatch<B> match);
    }

    private final ParametricReactionRule<B> reactionRule;

    private final RuleConditionMatcher<B> conditionMatcher;

    public ConditionalParametricRuleDecorator(ParametricReactionRule<B> reactionRule) throws InvalidReactionRuleException {
        this(reactionRule, (agent, rule, match) -> true);
    }

    public ConditionalParametricRuleDecorator(ParametricReactionRule<B> reactionRule, RuleConditionMatcher<B> conditionMatcher) throws InvalidReactionRuleException {
        super(reactionRule.getRedex(), reactionRule.getReactum(), reactionRule.getInstantationMap(), reactionRule.isReversible());
        this.reactionRule = reactionRule;
        this.conditionMatcher = conditionMatcher;
    }

    public boolean isMatchValid(B theAgent, ReactionRule<B> theRule, BigraphMatch<B> match) {
        return this.conditionMatcher.conditionIsSatisfied(theAgent, theRule, match);
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
