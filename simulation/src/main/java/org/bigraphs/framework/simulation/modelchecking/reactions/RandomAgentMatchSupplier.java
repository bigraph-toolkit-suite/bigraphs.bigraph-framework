package org.bigraphs.framework.simulation.modelchecking.reactions;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Collection;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Specific implementation of {@link AbstractAgentMatchSupplier}.
 * <p>
 * When deciding which paths to go in the model checking procedure, this supplier retrieves
 * any agent-match from the given set of possibly available agent-matches.
 *
 * @author Dominik Grzelak
 */
public final class RandomAgentMatchSupplier<B extends Bigraph<? extends Signature<?>>> extends AbstractAgentMatchSupplier<B> {

    private final Random randomSelection;


    protected RandomAgentMatchSupplier(Collection<B> availableAgents) {
        super(availableAgents);
        randomSelection = new Random();
    }

    @Override
    public B get() {
        return agents.get(randomSelection.nextInt(size));
    }
}
