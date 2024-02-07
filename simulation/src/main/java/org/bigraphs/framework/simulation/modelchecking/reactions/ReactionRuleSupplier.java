package org.bigraphs.framework.simulation.modelchecking.reactions;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Dominik Grzelak
 */
public abstract class ReactionRuleSupplier<B extends Bigraph<? extends Signature<?>>> implements Supplier<ReactionRule<B>> {

    protected final ImmutableList<ReactionRule<B>> availableRules;

    protected ReactionRuleSupplier(Collection<ReactionRule<B>> availableRules) {
        this.availableRules = Lists.immutable.withAll(availableRules);
    }

    public static <B extends Bigraph<? extends Signature<?>>> InOrderReactionRuleSupplier<B> createInOrder(Collection<ReactionRule<B>> availableRules) {
        return new InOrderReactionRuleSupplier<>(availableRules);
    }

    public static <B extends Bigraph<? extends Signature<?>>> RandomAgentMatchSupplier<B> createRandom(Collection<B> availableRules) {
        return new RandomAgentMatchSupplier<>(availableRules);
    }

    public List<ReactionRule<B>> getAvailableRules() {
        return availableRules.castToList();
    }
}
