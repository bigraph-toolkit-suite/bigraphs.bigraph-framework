package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.impl;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.AbstractReactiveSystem;

/**
 * An implementation of an {@link AbstractReactiveSystem} providing a simple BRS for pure bigraphs (see {@link PureBigraph}).
 *
 * @author Dominik Grzelak
 * @see PureBigraph
 */
public class SimpleReactiveSystem extends AbstractReactiveSystem<PureBigraph> {
    private PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();

    @Override
    protected PureBigraph buildGroundReaction(PureBigraph agent, BigraphMatch<PureBigraph> match, ReactionRule<PureBigraph> rule) {

        try {
            Bigraph outerBigraph = factory
                    .asBigraphOperator(match.getContext())
                    .juxtapose(match.getContextIdentity())
                    .getOuterBigraph();
            System.out.println(outerBigraph);
//            GraphvizConverter.toPNG(outerBigraph,
//                    true,
//                    new File("contextimage.png")
//            );
            Bigraph originAgent = factory.asBigraphOperator(outerBigraph).compose(match.getRedex()).getOuterBigraph();
//            GraphvizConverter.toPNG(originAgent,
//                    true,
//                    new File("agentOrigin.png")
//            );
//            GraphvizConverter.toPNG(rule.getRedex(),
//                    true,
//                    new File("redex.png")
//            );
            Bigraph agentReacted = factory.asBigraphOperator(outerBigraph)
                    .compose(rule.getReactum())
                    .getOuterBigraph();
//            GraphvizConverter.toPNG(agentReacted,
//                    true,
//                    new File("agentReacted.png")
//            );
            return (PureBigraph) agentReacted;
        } catch (IncompatibleSignatureException e) {
            e.printStackTrace();
        } catch (IncompatibleInterfaceException e) {
            e.printStackTrace();
        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
        return null;
    }

    @Override
    protected PureBigraph buildParametricReaction(PureBigraph agent, BigraphMatch<?> match, ReactionRule<PureBigraph> rule) {
        return null;
    }
}
