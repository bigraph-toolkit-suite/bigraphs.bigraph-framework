package de.tudresden.inf.st.bigraphs.rewriting.reactions;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;

import java.util.Collection;

/**
 * @author Dominik Grzelak
 */
public class InOrderReactionRuleSupplier extends ReactionRuleSupplier {
    private int currentCounter = 0;

    <B extends Bigraph<?>> InOrderReactionRuleSupplier(Collection<ReactionRule<B>> availableRules) {
        super(availableRules);
    }

    @Override
    public ReactionRule get() {
        if (currentCounter < availableRules.size()) {
            return availableRules.get(currentCounter++);
        }
        return null;
    }
}
