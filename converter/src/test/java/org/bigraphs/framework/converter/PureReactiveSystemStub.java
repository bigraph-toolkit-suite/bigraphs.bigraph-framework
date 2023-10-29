package org.bigraphs.framework.converter;

import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.AbstractSimpleReactiveSystem;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;

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
