package org.bigraphs.framework.core.reactivesystem;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * A transition of a labelled transition system is a triple containing the source and target and the
 * label (i.e., the arrow itself which is a bigraph as well).
 *
 * @author Dominik Grzelak
 */
public class TransitionTriple<B extends Bigraph<? extends Signature<?>>> {
    private ReactiveSystem<B> transitionOwner;

    private B source;
    private B label;
    private B target;

    public TransitionTriple(ReactiveSystem<B> transitionOwner, B source, B label, B target) {
        this.transitionOwner = transitionOwner;
        this.source = source;
        this.label = label;
        this.target = target;
    }

    //TODO: isEngagedTransition
    public boolean isEngagedTransition() {
        throw new RuntimeException("Not implemented yet");
    }

    public ReactiveSystem<B> getTransitionOwner() {
        return transitionOwner;
    }

    public B getSource() {
        return source;
    }

    public B getLabel() {
        return label;
    }

    public B getTarget() {
        return target;
    }
}
