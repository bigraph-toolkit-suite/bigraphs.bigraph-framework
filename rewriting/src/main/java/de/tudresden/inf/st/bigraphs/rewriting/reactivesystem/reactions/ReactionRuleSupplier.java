package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.reactions;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Dominik Grzelak
 */
public abstract class ReactionRuleSupplier<B extends Bigraph<? extends Signature<?>>> implements Supplier<ReactionRule<B>> {

    protected List<ReactionRule<B>> availableRules;

    protected ReactionRuleSupplier(Collection<ReactionRule<B>> availableRules) {
        this.availableRules = new ArrayList<>(availableRules);
    }

    public static <B extends Bigraph<? extends Signature<?>>> InOrderReactionRuleSupplier createInOrder(Collection<ReactionRule<B>> availableRules) {
        return new InOrderReactionRuleSupplier(availableRules);
    }


    public List<ReactionRule<B>> getAvailableRules() {
        return availableRules;
    }
}
