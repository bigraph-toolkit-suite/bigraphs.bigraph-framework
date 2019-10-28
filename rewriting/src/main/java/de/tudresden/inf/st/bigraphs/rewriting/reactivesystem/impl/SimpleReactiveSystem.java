package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.impl;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.AbstractReactiveSystem;

import java.util.ArrayList;
import java.util.List;

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
//            System.out.println(outerBigraph);
//            GraphvizConverter.toPNG(outerBigraph,
//                    true,
//                    new File("contextimage.png")
//            );
//            Bigraph originAgent = factory.asBigraphOperator(outerBigraph).compose(match.getRedex()).getOuterBigraph();
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

    private static int cnt = 0;

    @Override
    protected PureBigraph buildParametricReaction(PureBigraph agent, BigraphMatch<PureBigraph> match, ReactionRule<PureBigraph> rule) {
        //first build parallel product of the parameters using the instantiation map
        try {

            //OK
            Bigraph outerBigraph = factory
                    .asBigraphOperator(match.getContext())
                    .juxtapose(match.getContextIdentity())
                    .getOuterBigraph();
//            GraphvizConverter.toPNG(outerBigraph,
//                    true,
//                    new File("outerBigraph.png")
//            );

            Bigraph d = null;
            List<PureBigraph> parameters = new ArrayList<>(match.getParameters());
            if (parameters.size() >= 2) {
                FiniteOrdinal<Integer> mu_ix = rule.getInstantationMap().get(0);
                BigraphComposite d1 = factory.asBigraphOperator(parameters.get(mu_ix.getValue()));
                for (int i = 1, n = parameters.size(); i < n; i++) {
                    mu_ix = rule.getInstantationMap().get(i);
                    d1 = d1.parallelProduct(parameters.get(mu_ix.getValue()));
                }
                d = d1.getOuterBigraph();
            } else {
                d = parameters.get(0);
            }

//            GraphvizConverter.toPNG(d,
//                    true,
//                    new File("counting_d.png")
//            );

            //id_X || R) * d <=> R . d
            BigraphComposite result = factory.asBigraphOperator(match.getRedexIdentity()).parallelProduct(rule.getReactum());//.compose(d).getOuterBigraph();
//            GraphvizConverter.toPNG(result.getOuterBigraph(),
//                    true,
//                    new File("counting_result.png")
//            );
            BigraphComposite compose = result.compose(d);

//            GraphvizConverter.toPNG(compose.getOuterBigraph(),
//                    true,
//                    new File("counting_compose.png")
//            );


            Bigraph agentReacted = factory.asBigraphOperator(outerBigraph)
                    .compose(compose)
                    .getOuterBigraph();
//
//            GraphvizConverter.toPNG(agentReacted,
//                    true,
//                    new File("counting_reaction" + (cnt++) + ".png")
//            );

            return (PureBigraph) agentReacted;
        } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
            e.printStackTrace();
            return null;
        }
//        catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
    }
}
