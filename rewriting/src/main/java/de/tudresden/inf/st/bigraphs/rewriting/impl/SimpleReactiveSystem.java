package de.tudresden.inf.st.bigraphs.rewriting.impl;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.AbstractReactiveSystem;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.visualization.GraphvizConverter;

import java.io.File;
import java.io.IOException;

/**
 * @author Dominik Grzelak
 */
public class SimpleReactiveSystem extends AbstractReactiveSystem<DefaultDynamicSignature, PureBigraph> {
    private PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();

    @Override
    protected void buildGroundReaction(PureBigraph agent, BigraphMatch<?> match, ReactionRule<PureBigraph> rule) {

        try {
            Bigraph outerBigraph = factory.asBigraphOperator((PureBigraph) match.getContext())
                    .juxtapose(match.getContextIdentity()).getOuterBigraph();
            System.out.println(outerBigraph);
            GraphvizConverter.toPNG(outerBigraph,
                    true,
                    new File("contextimage.png")
            );
            Bigraph originAgent = factory.asBigraphOperator(outerBigraph).compose(match.getRedex()).getOuterBigraph();
            GraphvizConverter.toPNG(originAgent,
                    true,
                    new File("agentOrigin.png")
            );
            GraphvizConverter.toPNG(rule.getRedex(),
                    true,
                    new File("redex.png")
            );
            Bigraph agentReacted = factory.asBigraphOperator(outerBigraph)
                    .compose(rule.getReactum())
                    .getOuterBigraph();
            GraphvizConverter.toPNG(agentReacted,
                    true,
                    new File("agentReacted.png")
            );
        } catch (IncompatibleSignatureException e) {
            e.printStackTrace();
        } catch (IncompatibleInterfaceException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void buildParametricReaction(PureBigraph agent, BigraphMatch<?> match, ReactionRule<PureBigraph> rule) {

    }
}
