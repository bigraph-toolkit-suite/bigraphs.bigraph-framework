package org.bigraphs.framework.simulation.modelchecking.reactions;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;

import java.util.Collection;

/**
 * @author Dominik Grzelak
 */
public final class InOrderReactionRuleSupplier<B extends Bigraph<? extends Signature<?>>> extends ReactionRuleSupplier<B> {
    private int currentCounter = 0;

    InOrderReactionRuleSupplier(Collection<ReactionRule<B>> availableRules) {
        super(availableRules);
    }

    @Override
    public ReactionRule<B> get() {
        if (currentCounter < availableRules.size()) {
            return availableRules.get(currentCounter++);
        }
        return null;
    }
}
