package org.bigraphs.framework.simulation.equivalence;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.AbstractTransitionSystem;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;

/**
 * This class provides the implementation of the interface {@link BehavioralEquivalenceMixin} for the bisimulation equivalence relation.
 * It's a mixin implementation essentially providing the implementation of {@link #isEquivalentTo}.
 * "Wrapper" classes of implementations of {@link AbstractTransitionSystem} can introduce this new behavior via this mixin.
 * <p>
 * Bisimulation is an equivalence between two systems, which states "that whatever series of visible actions one LTS may perform, the other may match." [1]
 * Bisimilar systems have equal behavior, thus, can be replaced with each other.
 * <p>
 * Weak: tau transitions are discarded (i.e., invisible, internal actions are omitted).
 * <p>
 * This mixin implementation delegates the computation to the {@link BisimulationCheckerSupport} class.
 *
 * @author Dominik Grzelak
 * @see "[1] [DaKT07]  Danos, Vincent ; Krivine, Jean ; Tarissan, Fabien: Self-assembling Trees. In: Electronic Notes in Theoretical Computer Science, Proceedings of the Third Workshop on Structural Operational Semantics (SOS 2006). Bd. 175 (2007), Nr. 1, S. 19–32"
 */
public class WeakBisimulationMixinImpl {

}
