package org.bigraphs.framework.simulation.modelchecking.reactions;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Abstract base class for all agent match suppliers.
 * A supplier is a argument-less function that returns something.
 * <p>
 * This class is used in the model checking procedure as a generic interface.
 *
 * @author Dominik Grzelak
 */
public abstract class AbstractAgentMatchSupplier<B extends Bigraph<? extends Signature<?>>> implements Supplier<B> {
    protected final int size;
    protected ImmutableList<B> agents;

    public AbstractAgentMatchSupplier(Collection<B> availableAgents) {
        agents = Lists.immutable.withAll(availableAgents);
        size = (agents.size() - 1 == 0) ? 1 : agents.size();
    }
}
