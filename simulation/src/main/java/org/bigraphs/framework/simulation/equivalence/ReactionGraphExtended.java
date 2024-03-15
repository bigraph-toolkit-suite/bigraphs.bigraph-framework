package org.bigraphs.framework.simulation.equivalence;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;

/**
 * This is a "wrapper" class that extends the basic reaction graph structure {@link ReactionGraph}, and contains an instance of the
 * behavioral equivalence mixin implementation of {@link BehavioralEquivalenceMixin}.
 * This allows us to delegate calls to the extended method to the mixin.
 * For instance, to compute bisimilarity.
 * <p>
 * The mixin interface is additionally implemented to enforce same method names and completeness of all the available
 * additional extension-methods.
 *
 * @param <B> the type of the bigraph of the states and transition relations of the transition system
 * @author Dominik Grzelak
 */
public class ReactionGraphExtended<B extends Bigraph<? extends Signature<?>>> extends ReactionGraph<B> implements BehavioralEquivalenceMixin<ReactionGraphExtended<B>> {
    private final BehavioralEquivalenceMixin<ReactionGraphExtended<B>> mixin;

    public ReactionGraphExtended(BehavioralEquivalenceMixin<ReactionGraphExtended<B>> mixin) {
        this.mixin = mixin;
    }

    @Override
    public boolean isEquivalentTo(ReactionGraphExtended<B> transitionSystem) {
        return mixin.isEquivalentTo(transitionSystem);
    }
}
