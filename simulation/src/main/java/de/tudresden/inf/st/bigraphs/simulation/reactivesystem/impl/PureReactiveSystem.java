package de.tudresden.inf.st.bigraphs.simulation.reactivesystem.impl;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.simulation.ReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.AbstractSimpleReactiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of an {@link AbstractSimpleReactiveSystem} providing a simple BRS data structure for pure bigraphs
 * (see {@link PureBigraph}) and possibly later also binding bigraphs, bigraphs with sharing etc.
 *
 * @author Dominik Grzelak
 * @see PureBigraph
 */
public class PureReactiveSystem extends AbstractSimpleReactiveSystem<PureBigraph> {
    private final Logger logger = LoggerFactory.getLogger(PureReactiveSystem.class);

    private final PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();

    //    private static int cnt = 1;
    @Override
    public PureBigraph buildGroundReaction(PureBigraph agent, BigraphMatch<PureBigraph> match, ReactionRule<PureBigraph> rule) {
        try {

            Bigraph<DefaultDynamicSignature> outerBigraph = factory
                    .asBigraphOperator(match.getContext())
                    .parallelProduct((Bigraph<DefaultDynamicSignature>) match.getContextIdentity())
                    .getOuterBigraph();


            Bigraph<DefaultDynamicSignature> agentReacted;
            if (match.getRedexIdentity() instanceof Linkings.IdentityEmpty) {
                agentReacted = factory.asBigraphOperator(outerBigraph)
                        .compose(rule.getReactum())
                        .getOuterBigraph();
            } else {
//                BigraphArtifacts.exportAsInstanceModel((EcoreBigraph) outerBigraph, System.out);
//                BigraphGraphvizExporter.toPNG(outerBigraph, true, new File("outerBigraph"));
//                BigraphArtifacts.exportAsInstanceModel(match.getRedexIdentity(), System.out);
                BigraphComposite<DefaultDynamicSignature> reactumImage = factory.asBigraphOperator(match.getRedexIdentity())
                        .nesting(rule.getReactum());
//                BigraphArtifacts.exportAsInstanceModel(reactumImage.getOuterBigraph(), System.out);
//                BigraphGraphvizExporter.toPNG(reactumImage.getOuterBigraph(), true, new File("reactumImage"));
                BigraphComposite<DefaultDynamicSignature> compose = factory.asBigraphOperator(outerBigraph).compose(reactumImage);
//                BigraphArtifacts.exportAsInstanceModel(compose.getOuterBigraph(), System.out);
//                BigraphGraphvizExporter.toPNG(compose.getOuterBigraph(), true, new File("compose"));
                agentReacted = compose.getOuterBigraph();
            }

            return (PureBigraph) agentReacted;
        } catch (Exception e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    //TODO: beachte instantiation map
    @Override
    public PureBigraph buildParametricReaction(PureBigraph agent, BigraphMatch<PureBigraph> match, ReactionRule<PureBigraph> rule) {
        //first build parallel product of the parameters using the instantiation map
        try {

            //NOTE: juxtapose changed to parallelProduct (check if this is right)
            Bigraph<DefaultDynamicSignature> outerBigraph = factory
                    .asBigraphOperator(match.getContext())
                    .parallelProduct((Bigraph<DefaultDynamicSignature>) match.getContextIdentity())
                    .getOuterBigraph();

            Bigraph<DefaultDynamicSignature> d_Params;
            List<PureBigraph> parameters = new ArrayList<>(match.getParameters());
            if (parameters.size() >= 2) {
                FiniteOrdinal<Integer> mu_ix = rule.getInstantationMap().get(0);
                BigraphComposite<DefaultDynamicSignature> d1 = factory.asBigraphOperator(parameters.get(mu_ix.getValue()));
                for (int i = 1, n = parameters.size(); i < n; i++) {
                    mu_ix = rule.getInstantationMap().get(i);
                    d1 = d1.parallelProduct(parameters.get(mu_ix.getValue()));
                }
                d_Params = d1.getOuterBigraph();
            } else {
                d_Params = parameters.get(0);
            }

            //id_X || R) * d_Params <=> R . d_Params
            BigraphComposite<DefaultDynamicSignature> reactumImage = factory.asBigraphOperator(match.getRedexIdentity())
                    .parallelProduct(rule.getReactum());//.compose(d_Params).getOuterBigraph();
            BigraphComposite<DefaultDynamicSignature> compose = reactumImage.compose(d_Params);


            Bigraph<DefaultDynamicSignature> agentReacted = factory.asBigraphOperator(outerBigraph)
                    .compose(compose)
                    .getOuterBigraph();

            return (PureBigraph) agentReacted;
        } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }
    }
}
