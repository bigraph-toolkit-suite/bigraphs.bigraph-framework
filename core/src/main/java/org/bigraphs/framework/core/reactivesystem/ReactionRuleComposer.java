package org.bigraphs.framework.core.reactivesystem;

import org.bigraphs.framework.core.BigraphComposite;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bigraphs.framework.core.factory.BigraphFactory.ops;


public class ReactionRuleComposer<R extends ReactionRule<?>> {

    //TODO add tensor product (distinct names)

    /**
     * This is the intermediate string used when composing the labels of two rules.
     */
    private String separator = "_PP_";

    /**
     * This method composes two rules by using the parallel product.
     * The instantiation map, the tracking map (if employed), and the labels are also updated accordingly.
     * If redexes or reactums of both rules share the same outer name, they will be merged.
     * <p>
     * Note: The labels of both rules must be set.
     *
     * @param left  the left rule
     * @param right the right rule
     * @return the parallel product of the two rules
     * @throws InvalidReactionRuleException   if the product is invalid
     * @throws IncompatibleInterfaceException if the product is invalid
     */
    public R parallelProduct(ReactionRule<PureBigraph> left, ReactionRule<PureBigraph> right) throws InvalidReactionRuleException, IncompatibleInterfaceException {
        assertRuleLabelNotEmpty(left.getLabel());
        assertRuleLabelNotEmpty(right.getLabel());
        assertSeparatorStringNotContainedInRuleLabel(left.getLabel());
        assertSeparatorStringNotContainedInRuleLabel(right.getLabel());

        // Redexes
        PureBigraph redexLHS = left.getRedex();
        PureBigraph redexRHS = right.getRedex();
        // Reactums
        PureBigraph reactumLHS = left.getReactum();
        PureBigraph reactumRHS = right.getReactum();
        // Create product between redexes, and reactums
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

        if (left.getLabel() != null && !left.getLabel().isEmpty() &&
                right.getLabel() != null && !right.getLabel().isEmpty()) {
            String labelComp = left.getLabel() + separator + right.getLabel();
            ruleProduct.withLabel(labelComp);
        }
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

    /**
     * When two rules are composed, their labels are also composed.
     * The "separator string" is the intermediate string between these two rule labels.
     *
     * {@link ReactionRuleComposer#withSeparator(String)}
     * @param separator any string used to separate two rule labels
     * @return this instance
     */
    public ReactionRuleComposer<R> withSeparator(String separator) {
        this.separator = Objects.requireNonNull(separator);
        return this;
    }

    /**
     * Get the "intermediate string" used to separate two rule labels when they are composed.
     */
    public String getSeparator() {
        return separator;
    }

    protected void assertSeparatorStringNotContainedInRuleLabel(String ruleLabel) {
        if (ruleLabel.contains(separator)) {
            throw new RuntimeException(
                    String.format("The separator string '%s' cannot be used because it is contained already in the rule label", separator)
            );
        }
    }

    protected void assertRuleLabelNotEmpty(String ruleLabel) {
        if (ruleLabel.trim().isEmpty()) {
            throw new RuntimeException(
                    "The rule has no rule label, which is required for composition"
            );
        }
    }
}
