package org.bigraphs.framework.core.reactivesystem;

import org.bigraphs.framework.core.BigraphComposite;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bigraphs.framework.core.factory.BigraphFactory.ops;


public class ReactionRuleComposer<R extends ReactionRule<?>> {


    public R parallelProduct(ReactionRule<PureBigraph> left, ReactionRule<PureBigraph> right) throws InvalidReactionRuleException, IncompatibleInterfaceException {
        PureBigraph redexLHS = left.getRedex();
        PureBigraph redexRHS = right.getRedex();

        PureBigraph reactumLHS = left.getReactum();
        PureBigraph reactumRHS = right.getReactum();

        ops(redexLHS).parallelProduct(redexRHS);


        BigraphComposite<DefaultDynamicSignature> productRedex = ops(redexLHS).parallelProduct(redexRHS);
        BigraphComposite<DefaultDynamicSignature> productReactum = ops(reactumLHS).parallelProduct(reactumRHS);

        // Rewrite tracking map
        TrackingMap tMap = new TrackingMap();
        tMap.putAll(left.getTrackingMap());

        int sizeTM_left = left.getTrackingMap().size();
        // These are nodes that have no reactum->redex node mapping,
        // i.e., these nodes are freshly created in the reactum.
        // This is the amount we have to add to the "right" reaction rule (the right-most term of the product)
        long freshNodeCount = left.getTrackingMap().values().stream().filter(x -> x.isEmpty() || x.isBlank()).count();
        TrackingMap tMapRight = right.getTrackingMap();
        tMapRight.forEach((k, v) -> {
            String newNodeID_reactum = extractNodePrefix(k) + (extractNodeIdentifier(k) + sizeTM_left);
            String newNodeID_redex = "";
            if (!v.isBlank() && !v.isEmpty()) {
                newNodeID_redex = extractNodePrefix(v) + (extractNodeIdentifier(v) - freshNodeCount + sizeTM_left);
            }
            tMap.put(newNodeID_reactum, newNodeID_redex);
        });

        // Rewrite the instantiation map
        InstantiationMap iMap = InstantiationMap.create(left.getInstantationMap().getMappings().size() + right.getInstantationMap().getMappings().size());
        int numSitesRedex_left = redexLHS.getSites().size();
        int numSitesReactum_right = reactumLHS.getSites().size();
        iMap.getMappings().putAll(left.getInstantationMap().getMappings());
        right.getInstantationMap().getMappings().entrySet().forEach(entry -> {
            // increment the original indexes of the "right term"
            iMap.map(
                    entry.getKey().getValue() + numSitesReactum_right,
                    entry.getValue().getValue() + numSitesRedex_left
            );
        });

        ParametricReactionRule<PureBigraph> ruleProduct = new ParametricReactionRule<>(productRedex.getOuterBigraph(), productReactum.getOuterBigraph());
        ruleProduct.withTrackingMap(tMap)
                .withInstantiationMap(iMap);
        return (R) ruleProduct;
    }

    private int extractNodeIdentifier(String identifier) {
        Pattern pattern = Pattern.compile("\\d+$");
        Matcher matcher = pattern.matcher(identifier);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        } else {
            throw new IllegalArgumentException("No number found in the identifier");
        }
    }

    private String extractNodePrefix(String identifier) {
        Pattern pattern = Pattern.compile("^(\\D+)");
        Matcher matcher = pattern.matcher(identifier);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("No prefix found in the identifier");
        }
    }

}
