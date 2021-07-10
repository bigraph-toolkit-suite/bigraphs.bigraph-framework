package de.tudresden.inf.st.bigraphs.converter;

import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.AbstractSimpleReactiveSystem;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.BigraphMatch;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionRule;

public class PureReactiveSystemStub extends AbstractSimpleReactiveSystem<PureBigraph> {
    @Override
    public PureBigraph buildGroundReaction(PureBigraph agent, BigraphMatch<PureBigraph> match, ReactionRule<PureBigraph> rule) {
        return null;
    }

    @Override
    public PureBigraph buildParametricReaction(PureBigraph agent, BigraphMatch<PureBigraph> match, ReactionRule<PureBigraph> rule) {
        return null;
    }
}
