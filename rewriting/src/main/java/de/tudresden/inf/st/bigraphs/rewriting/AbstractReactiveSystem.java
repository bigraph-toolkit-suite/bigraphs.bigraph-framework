package de.tudresden.inf.st.bigraphs.rewriting;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;

import java.util.Collection;

/**
 * @author Dominik Grzelak
 */
public abstract class AbstractReactiveSystem<S extends Signature, B extends Bigraph<S>> implements ReactiveSystem<S, B> {

    protected ReactiveSystemListener reactiveSystemListener;

    public AbstractReactiveSystem() {
        onAttachListener(this);
    }

    @Override
    public Collection<ReactionRule<S>> getReactionRules() {
        return null;
    }

    @Override
    public void setReactiveSystemListener(ReactiveSystemListener reactiveSystemListener) {
        this.reactiveSystemListener = reactiveSystemListener;
    }

    private void onAttachListener(ReactiveSystem reactiveSystem) {
        if (reactiveSystem instanceof ReactiveSystem.ReactiveSystemListener) {
            reactiveSystem.setReactiveSystemListener((ReactiveSystemListener) this);
        } else {
            //TODO: set default empty listener
        }

    }


}
