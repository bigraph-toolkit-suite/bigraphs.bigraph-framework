package de.tudresden.inf.st.bigraphs.simulation.reactivesystem.impl;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.simulation.ReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.AbstractSimpleReactiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;

/**
 * An implementation of an {@link AbstractSimpleReactiveSystem} providing a simple BRS data structure for pure bigraphs
 * (see {@link PureBigraph}) and possibly later also binding bigraphs, bigraphs with sharing etc.
 *
 * @author Dominik Grzelak
 * @see PureBigraph
 */
public class PureReactiveSystem extends AbstractSimpleReactiveSystem<PureBigraph> {
    private final Logger logger = LoggerFactory.getLogger(PureReactiveSystem.class);

    @Override
    public PureBigraph buildGroundReaction(PureBigraph agent, BigraphMatch<PureBigraph> match, ReactionRule<PureBigraph> rule) {
        try {

            Bigraph<DefaultDynamicSignature> outerBigraph = ops(match.getContext())
                    .parallelProduct((Bigraph<DefaultDynamicSignature>) match.getContextIdentity())
                    .getOuterBigraph();

            Bigraph<DefaultDynamicSignature> agentReacted;
            Bigraph<DefaultDynamicSignature> reactumImage;
            // Compute the "reactum image" (some identity link graph and the reactum)
            if (match.getRedexIdentity() instanceof Linkings.IdentityEmpty) { // if we have no identity then compute one here
                Linkings<DefaultDynamicSignature> linkings = pureLinkings((DefaultDynamicSignature) getSignature());
                Set<StringTypedName> collect = rule.getReactum().getOuterNames().stream()
                        .filter(o -> {
                            Optional<BigraphEntity.InnerName> first = outerBigraph.getInnerNames().stream().filter(x -> x.getName().equals(o.getName())).findFirst();
                            return !first.isPresent() || outerBigraph.getLinkOfPoint(first.get()) == null;
                        })
                        .map(o -> StringTypedName.of(o.getName()))
                        .collect(Collectors.toSet());
                Set<StringTypedName> differences = outerBigraph.getInnerNames().stream()
                        .map(x -> StringTypedName.of(x.getName())).collect(Collectors.toSet());
                differences.removeAll(collect);
                PureBigraphBuilder<? extends Signature<?>> b = pureBuilder(getSignature());
                for (StringTypedName each : differences) {
                    b.createOuterName(each.getValue());
                }

                Bigraph<DefaultDynamicSignature> renamingForReactum = differences.size() != 0 ?
                        b.createBigraph() :
                        linkings.identity_e();
                reactumImage = ops(renamingForReactum).parallelProduct(rule.getReactum()).getOuterBigraph();
            } else { // otherwise, use the one that was computed in the matching phase
                reactumImage = ops((Bigraph<DefaultDynamicSignature>) match.getRedexIdentity())
                        .nesting((Bigraph<DefaultDynamicSignature>) rule.getReactum()).getOuterBigraph();
            }

            agentReacted = ops(outerBigraph)
                    .compose(reactumImage)
                    .getOuterBigraph();

            return (PureBigraph) agentReacted;
        } catch (Exception e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public PureBigraph buildParametricReaction(PureBigraph agent, BigraphMatch<PureBigraph> match, ReactionRule<PureBigraph> rule) {
        //first build parallel product of the parameters using the instantiation map
        try {

            //NOTE: juxtapose changed to parallelProduct (check if this is right)
            Bigraph<DefaultDynamicSignature> outerBigraph = ops(match.getContext())
                    .parallelProduct((Bigraph<DefaultDynamicSignature>) match.getContextIdentity())
                    .getOuterBigraph();

            // Compose all parameters
            Bigraph<DefaultDynamicSignature> d_Params;
            List<PureBigraph> parameters = new ArrayList<>(match.getParameters());
            if (parameters.size() >= 2) {
                // Consider the instantiation map here: it "swaps" the parameters basically according to the map definition
                FiniteOrdinal<Integer> mu_ix = rule.getInstantationMap().get(0);
                BigraphComposite<DefaultDynamicSignature> d1 = ops(parameters.get(mu_ix.getValue()));
                for (int i = 1, n = parameters.size(); i < n; i++) {
                    mu_ix = rule.getInstantationMap().get(i);
                    d1 = d1.parallelProduct(parameters.get(mu_ix.getValue()));
                }
                d_Params = d1.getOuterBigraph();
            } else {
                d_Params = parameters.get(0);
            }

            //id_X || R) * d_Params <=> R . d_Params
            // Build the reactum image and then add the parameters
            BigraphComposite<DefaultDynamicSignature> reactumImage =
                    ops((Bigraph) match.getRedexIdentity()).parallelProduct(rule.getReactum());//.reactumImageWithParams(d_Params).getOuterBigraph();
            Bigraph<DefaultDynamicSignature> reactumImageWithParams = reactumImage.compose(d_Params).getOuterBigraph();


            // Get all outer names of right inner bigraph and make identity graph from them
            // This resembles some of the logic from the nesting operator
            Linkings<DefaultDynamicSignature> linkings = pureLinkings((DefaultDynamicSignature) getSignature());
            Set<StringTypedName> collect = reactumImageWithParams.getOuterNames().stream()
                    .filter(o -> {
                        Optional<BigraphEntity.InnerName> first = outerBigraph.getInnerNames().stream().filter(x -> x.getName().equals(o.getName())).findFirst();
                        return !first.isPresent() || outerBigraph.getLinkOfPoint(first.get()) == null;
                    })
                    .map(o -> StringTypedName.of(o.getName()))
                    .collect(Collectors.toSet());

            Set<StringTypedName> differences = outerBigraph.getInnerNames().stream()
                    .map(x -> StringTypedName.of(x.getName())).collect(Collectors.toSet());
            differences.removeAll(collect);
            PureBigraphBuilder<? extends Signature<?>> b = pureBuilder(getSignature());
            for (StringTypedName each : differences) {
                b.createOuterName(each.getValue());
            }
            Bigraph<DefaultDynamicSignature> renamingForF = differences.size() != 0 ?
                    b.createBigraph() :
                    linkings.identity_e();
            Bigraph reactumImageWithParamsAndIdentity = ops(renamingForF).parallelProduct(reactumImageWithParams).getOuterBigraph();

            // Context * Reactum (* params)
            Bigraph<DefaultDynamicSignature> agentReacted = ops(outerBigraph)
                    .compose(reactumImageWithParamsAndIdentity)
                    .getOuterBigraph();

            return (PureBigraph) agentReacted;
        } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }
    }
}
