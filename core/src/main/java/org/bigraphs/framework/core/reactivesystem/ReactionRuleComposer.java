/*
 * Copyright (c) 2024-2025 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.core.reactivesystem;

import static org.bigraphs.framework.core.factory.BigraphFactory.ops;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bigraphs.framework.core.BigraphComposite;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;

/**
 * Utility class to compose reaction rules into a new single rule.
 * <p>
 * This class supports two operations:
 * <ul>
 *   <li><strong>Parallel product</strong>: places the redex and reactum of two rules side by side under distinct roots.</li>
 *   <li><strong>Merge product</strong>: places the redex and reactum of two rules side by side under a single, common root.</li>
 * </ul>
 * During composition the following artifacts are combined accordingly:
 * <ul>
 *   <li>Instantiation map</li>
 *   <li>Tracking map (if present)</li>
 *   <li>Rule label: created by concatenating the labels using a configurable separator ({@code _PP_})</li>
 * </ul>
 * The separator defaults to {@code "_PP_"} and can be changed via {@link #withSeparator(String)}.
 *
 * @param <R> the concrete reaction rule type produced by this composer
 * @author Dominik Grzelak
 * @see #parallelProduct(ReactionRule, ReactionRule)
 * @see #mergeProduct(ReactionRule, ReactionRule)
 * @see #withSeparator(String)
 */
public class ReactionRuleComposer<R extends ReactionRule<?>> {

    protected enum RuleProductOperation {
        PARALLEL,
        MERGE
    }

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
        return ruleProduct(left, right, RuleProductOperation.PARALLEL);
    }

    /**
     * Computes the merge product of two reaction rules.
     * <p>
     * This operation combines the two given rules by merging their structures.
     * The instantiation map, the tracking map (if present), and the labels are
     * updated accordingly. If the redexes or reactums of both rules share the same
     * outer name, those components are merged into a single one.
     * <p>
     * <strong>Note:</strong> Both reaction rules must have their labels defined
     * prior to invoking this method.
     *
     * @param left  the first reaction rule to be merged
     * @param right the second reaction rule to be merged
     * @return the merge product of the two reaction rules
     * @throws InvalidReactionRuleException   if the resulting merge product is not a valid reaction rule
     * @throws IncompatibleInterfaceException if the interfaces of the two reaction rules are incompatible
     */
    public R mergeProduct(ReactionRule<PureBigraph> left, ReactionRule<PureBigraph> right) throws InvalidReactionRuleException, IncompatibleInterfaceException {
        return ruleProduct(left, right, RuleProductOperation.MERGE);
    }

    protected R ruleProduct(
            ReactionRule<PureBigraph> left,
            ReactionRule<PureBigraph> right,
            RuleProductOperation operation
    ) throws InvalidReactionRuleException, IncompatibleInterfaceException {

        assertRuleLabelNotEmpty(left.getLabel());
        assertRuleLabelNotEmpty(right.getLabel());
        // assertSeparatorStringNotContainedInRuleLabel(left.getLabel());
        assertSeparatorStringNotContainedInRuleLabel(right.getLabel());

        // Redexes
        PureBigraph redexLHS = left.getRedex();
        PureBigraph redexRHS = right.getRedex();
        // Reactums
        PureBigraph reactumLHS = left.getReactum();
        PureBigraph reactumRHS = right.getReactum();

        BigraphComposite<DynamicSignature> productRedex =
                composeBigraphs(redexLHS, redexRHS, operation);
        BigraphComposite<DynamicSignature> productReactum =
                composeBigraphs(reactumLHS, reactumRHS, operation);

        // Rewrite tracking map
        TrackingMap tMap = new TrackingMap();
        tMap.putAll(left.getTrackingMap());

        int sizeTM_left = left.getTrackingMap().size();
        // These are nodes that have no reactum->redex node mapping,
        // i.e., these nodes are freshly created in the reactum.
        // This is the amount we have to add to the "right" reaction rule (the right-most term of the product)
        long freshNodeCount = left.getTrackingMap().values().stream().filter(String::isBlank).count();
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

    protected BigraphComposite<DynamicSignature> composeBigraphs(
            PureBigraph left,
            PureBigraph right,
            RuleProductOperation operation
    ) throws InvalidReactionRuleException, IncompatibleInterfaceException {

        return switch (operation) {
            case PARALLEL -> ops(left).parallelProduct(right);
            case MERGE -> ops(left).merge(right);
        };
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
     * <p>
     * {@link ReactionRuleComposer#withSeparator(String)}
     *
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
