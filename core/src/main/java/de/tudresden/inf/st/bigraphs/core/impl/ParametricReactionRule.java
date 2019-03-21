package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.AbstractReactionRule;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.ReactionRule;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.exceptions.ReactionRuleException;

public class ParametricReactionRule<S extends Signature> extends AbstractReactionRule<S> {

    public ParametricReactionRule(Bigraph<S> redex, Bigraph<S> reactum) throws ReactionRuleException {
        super(redex, reactum);
    }


}
