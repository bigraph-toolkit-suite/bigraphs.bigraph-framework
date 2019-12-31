package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.reactions;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;

import java.util.Collection;

/**
 * @author Dominik Grzelak
 */
public final class InOrderReactionRuleSupplier<B extends Bigraph<? extends Signature<?>>> extends ReactionRuleSupplier<B> {
    private int currentCounter = 0;

    InOrderReactionRuleSupplier(Collection<ReactionRule<B>> availableRules) {
        super(availableRules);
    }

    @Override
    public ReactionRule<B> get() {
        if (currentCounter < availableRules.size()) {
            return availableRules.get(currentCounter++);
        }
        return null;
    }
}
