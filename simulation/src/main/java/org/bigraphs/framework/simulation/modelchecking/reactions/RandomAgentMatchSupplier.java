package org.bigraphs.framework.simulation.modelchecking.reactions;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Collection;
import java.util.Random;
import java.util.function.Supplier;

/**
 * @author Dominik Grzelak
 */
public final class RandomAgentMatchSupplier<B extends Bigraph<? extends Signature<?>>> implements Supplier<B> {

    private final Random randomSelection;
    private final int size;
    private ImmutableList<B> agents;

    protected RandomAgentMatchSupplier(Collection<B> availableAgents) {
        randomSelection = new Random();
        agents = Lists.immutable.withAll(availableAgents);
        size = (agents.size() - 1 == 0) ? 1 : agents.size();
    }

    @Override
    public B get() {
        return agents.get(randomSelection.nextInt(size));
    }
}
