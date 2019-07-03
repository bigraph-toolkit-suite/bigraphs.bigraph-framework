package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystem;

/**
 * A transition of a labelled transition system is a triple containing the source and target and the
 * label (i.e., the arrow itself which is a bigraph as well).
 *
 * @author Dominik Grzelak
 */
public class TransitionTriple<S extends Signature, B extends Bigraph<S>> {
    private ReactiveSystem<S, B> transitionOwner;

    private B source;
    private B label;
    private B target;

    public TransitionTriple(ReactiveSystem<S, B> transitionOwner, B source, B label, B target) {
        this.transitionOwner = transitionOwner;
        this.source = source;
        this.label = label;
        this.target = target;
    }

    //TODO: isEngagedTransition
    public boolean isEngagedTransition() {
        throw new RuntimeException("Not implemented yet");
    }

    public ReactiveSystem<S, B> getTransitionOwner() {
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
