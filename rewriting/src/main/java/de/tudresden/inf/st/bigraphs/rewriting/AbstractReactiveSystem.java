package de.tudresden.inf.st.bigraphs.rewriting;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Dominik Grzelak
 */
public abstract class AbstractReactiveSystem<S extends Signature, B extends Bigraph<S>> implements ReactiveSystem<S, B> {

    protected ReactiveSystemListener reactiveSystemListener;

    protected List<ReactionRule<B>> reactionRules = new LinkedList<>();

    public AbstractReactiveSystem() {
        onAttachListener(this);
    }

    @Override
    public Collection<ReactionRule<B>> getReactionRules() {
        return reactionRules;
    }

    @Override
    public void setReactiveSystemListener(ReactiveSystemListener reactiveSystemListener) {
        this.reactiveSystemListener = reactiveSystemListener;
    }

    private void onAttachListener(ReactiveSystem reactiveSystem) {
        if (reactiveSystem instanceof ReactiveSystem.ReactiveSystemListener) {
            reactiveSystem.setReactiveSystemListener((ReactiveSystemListener) this);
        } else {
            //TODO: set default empty listener to ensure that we always have a listener even if the user doesn't care
        }

    }


}
