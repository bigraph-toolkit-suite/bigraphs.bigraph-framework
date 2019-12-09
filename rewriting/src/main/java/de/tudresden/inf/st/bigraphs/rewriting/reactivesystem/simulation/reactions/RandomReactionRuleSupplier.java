package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.reactions;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;

import java.util.Collection;
import java.util.Random;

/**
 * @author Dominik Grzelak
 */
public class RandomReactionRuleSupplier<B extends Bigraph<? extends Signature<?>>> extends ReactionRuleSupplier<B> {

    private final Random randomSelection;
    private final int size;

    protected RandomReactionRuleSupplier(Collection<ReactionRule<B>> availableRules) {
        super(availableRules);
        randomSelection = new Random();
        size = availableRules.size() == 0 ? 0 : availableRules.size() - 1;
    }

    @Override
    public ReactionRule<B> get() {
        return availableRules.get(randomSelection.nextInt(size));
    }
}
