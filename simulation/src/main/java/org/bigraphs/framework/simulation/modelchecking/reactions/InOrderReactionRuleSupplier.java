package org.bigraphs.framework.simulation.modelchecking.reactions;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;

import java.util.Collection;

/**
 * Specific implementation of the {@link AbstractReactionRuleSupplier}.
 * <p>
 * A given set of reaction rule is returned in the order as they are provided.
 *
 * @author Dominik Grzelak
 */
public final class InOrderReactionRuleSupplier<B extends Bigraph<? extends Signature<?>>> extends AbstractReactionRuleSupplier<B> {
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
