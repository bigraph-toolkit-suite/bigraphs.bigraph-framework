package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.reactions;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Dominik Grzelak
 */
public abstract class ReactionRuleSupplier implements Supplier<ReactionRule> {

    protected List<ReactionRule> availableRules;

    protected <B extends Bigraph<?>> ReactionRuleSupplier(Collection<ReactionRule<B>> availableRules) {
        this.availableRules = new ArrayList<>(availableRules);
    }

    public static <B extends Bigraph<?>> InOrderReactionRuleSupplier createInOrder(Collection<ReactionRule<B>> availableRules) {
        return new InOrderReactionRuleSupplier(availableRules);
    }


    public List<ReactionRule> getAvailableRules() {
        return availableRules;
    }
}
