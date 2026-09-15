/*
 * Copyright (c) 2026 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.matching.tracking;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.function.Consumer;
import org.bigraphs.framework.core.BigraphMetaModelConstants;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.TrackingMap;
import org.bigraphs.framework.core.utils.emf.EMFUtils;
import org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.matching.pure.PureBigraphMatch;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.eclipse.emf.ecore.EAttribute;
import org.junit.jupiter.api.Test;

public class LinkTrackingMapTest {

    private static DynamicSignature sig(int doorArity) {
        return pureSignatureBuilder()
                .add("Door", doorArity)
                .add("Room", 1)
                .add("Box", 1)
                .add("Sensor", 1)
                .add("Flag", 1)
                .create();
    }

    @Test
    void trackLink_classifies_key_and_treats_null_as_new() {
        TrackingMap map = new TrackingMap();
        map.put("v0", "v0");
        map.trackLink("e0", "e0");
        map.trackLink("e1", null);

        assertEquals("e0", map.get("e0"));
        assertEquals("", map.get("e1"));
        assertTrue(map.isLink("e0"));
        assertTrue(map.isLink("e1"));
        assertFalse(map.isLink("v0"));
    }

    @Test
    void tracked_closed_edge_keeps_agent_name_and_leaves_context_edge() throws Exception {
        PureBigraph agent = agentWithNamedDoorLinkAndContextEdge(sig(1));
        String boxLink = linkNameOf(agent, "Box");
        ParametricReactionRule<PureBigraph> rule = identityDoorRoomRule(sig(1), map -> map.trackLink("e0", "e0"));

        PureBigraph result = applyOnce(agent, rule);

        assertEquals("Door_left", linkNameOf(result, "Door"));
        assertEquals(boxLink, linkNameOf(result, "Box"));
        assertEquals(1, countLinksNamed(result, "Door_left"));
        assertEquals("v0", nodeName(result, "Door"));
        assertEquals("v1", nodeName(result, "Room"));
    }

    @Test
    void unlisted_closed_edge_is_not_required_to_keep_agent_name() throws Exception {
        PureBigraph agent = agentWithNamedDoorLinkAndContextEdge(sig(1));
        ParametricReactionRule<PureBigraph> rule = identityDoorRoomRule(sig(1), map -> {});

        PureBigraph result = applyOnce(agent, rule);

        assertNotEquals("Door_left", linkNameOf(result, "Door"));
        assertNotEquals("Door_left", linkNameOf(result, "Box"));
        assertEquals(0, countLinksNamed(result, "Door_left"));
    }

    @Test
    void addLinkNames_without_map_entry_does_not_keep_agent_name() throws Exception {
        PureBigraph agent = agentWithNamedDoorLinkAndContextEdge(sig(1));
        ParametricReactionRule<PureBigraph> rule = identityDoorRoomRule(sig(1), map -> map.addLinkNames("e0"));

        PureBigraph result = applyOnce(agent, rule);

        assertNotEquals("Door_left", linkNameOf(result, "Door"));
        assertNotEquals("Door_left", linkNameOf(result, "Box"));
        assertEquals(0, countLinksNamed(result, "Door_left"));
    }

    @Test
    void unknown_reactum_link_is_skipped() throws Exception {
        PureBigraph agent = agentWithNamedDoorLinkAndContextEdge(sig(1));
        ParametricReactionRule<PureBigraph> rule = identityDoorRoomRule(sig(1), map -> map.trackLink("e99", "e0"));

        PureBigraph result = applyOnce(agent, rule);

        assertNotEquals("Door_left", linkNameOf(result, "Door"));
        assertNotEquals("Door_left", linkNameOf(result, "Box"));
    }

    @Test
    void new_tracked_link_keeps_reactum_name() throws Exception {
        DynamicSignature signature = sig(2);
        PureBigraphBuilder<DynamicSignature> agentBuilder = pureBuilder(signature);
        agentBuilder.root().connectByEdge("Door", "Room");
        PureBigraph agent = agentBuilder.create();
        renameLink(linkOf(agent, "Door"), "Door_left");

        PureBigraphBuilder<DynamicSignature> redex = pureBuilder(signature);
        redex.root().child("Door").linkInner("d").top().child("Room").linkInner("d");
        redex.closeInner();
        PureBigraphBuilder<DynamicSignature> reactum = pureBuilder(signature);
        reactum.root()
                .child("Door").linkInner("d").linkInner("f").top()
                .child("Room").linkInner("d").top()
                .child("Flag").linkInner("f");
        reactum.closeInner();

        TrackingMap map = nodeIdentity("v0", "v1");
        map.put("v2", "");
        map.trackLink("e0", "e0");
        map.trackLink("e1", "");
        ParametricReactionRule<PureBigraph> rule = new ParametricReactionRule<>(redex.create(), reactum.create());
        rule.withTrackingMap(map);

        PureBigraph result = applyOnce(agent, rule);

        assertEquals("Door_left", linkNameOf(result, "Door", "Room"));
        assertEquals("e1", linkNameOf(result, "Door", "Flag"));
        assertEquals(1, countLinksNamed(result, "Door_left"));
    }

    @Test
    void rewired_reactum_keeps_redex_edge_on_the_new_ports() throws Exception {
        DynamicSignature signature = sig(1);
        PureBigraph agent = agentWithNamedDoorLinkAndContextEdge(signature);

        PureBigraphBuilder<DynamicSignature> redex = pureBuilder(signature);
        redex.root().child("Door").linkInner("d").top().child("Room").linkInner("d");
        redex.closeInner();
        PureBigraphBuilder<DynamicSignature> reactum = pureBuilder(signature);
        reactum.root()
                .child("Door").linkInner("d").top()
                .child("Room").linkInner("r").top()
                .child("Flag").linkInner("d");
        reactum.closeInner();

        TrackingMap map = nodeIdentity("v0", "v1");
        map.put("v2", "");
        map.trackLink("e0", "e0");
        ParametricReactionRule<PureBigraph> rule = new ParametricReactionRule<>(redex.create(), reactum.create());
        rule.withTrackingMap(map);

        PureBigraph result = applyOnce(agent, rule);

        assertEquals("Door_left", linkNameOf(result, "Door", "Flag"));
        assertNotEquals("Door_left", linkNameOf(result, "Room"));
        assertNotEquals("Door_left", linkNameOf(result, "Box"));
        assertEquals(1, countLinksNamed(result, "Door_left"));
    }

    @Test
    void occupied_target_name_is_not_copied_onto_the_rule_edge() throws Exception {
        DynamicSignature signature = sig(1);
        PureBigraphBuilder<DynamicSignature> agentBuilder = pureBuilder(signature);
        agentBuilder.root()
                .connectByEdge("Door", "Room")
                .child("Box").linkOuter("Door_left")
                .child("Sensor").linkOuter("Door_left");
        PureBigraph agent = agentBuilder.create();
        renameLink(linkOf(agent, "Door"), "Door_left");
        ParametricReactionRule<PureBigraph> rule = identityDoorRoomRule(signature, map -> map.trackLink("e0", "e0"));

        PureBigraph result = applyOnce(agent, rule);

        assertNotEquals("Door_left", linkNameOf(result, "Door"));
        assertEquals("Door_left", linkNameOf(result, "Box"));
        assertEquals(1, countLinksNamed(result, "Door_left"));
    }

    @Test
    void tracked_outer_name_keeps_agent_name() throws Exception {
        DynamicSignature signature = sig(1);
        PureBigraphBuilder<DynamicSignature> agentBuilder = pureBuilder(signature);
        agentBuilder.root()
                .child("Door").linkOuter("Door_left")
                .child("Room").linkOuter("Door_left")
                .child("Box").linkOuter("box")
                .child("Sensor").linkOuter("box");
        PureBigraph agent = agentBuilder.create();

        PureBigraphBuilder<DynamicSignature> redex = pureBuilder(signature);
        redex.root().child("Door").linkOuter("y").top().child("Room").linkOuter("y");
        PureBigraphBuilder<DynamicSignature> reactum = pureBuilder(signature);
        reactum.root().child("Door").linkOuter("y").top().child("Room").linkOuter("y");
        ParametricReactionRule<PureBigraph> rule = new ParametricReactionRule<>(redex.create(), reactum.create());
        rule.withTrackingMap(nodeIdentity("v0", "v1").trackLink("y", "y"));

        PureBigraph result = applyOnce(agent, rule);

        assertEquals("Door_left", linkNameOf(result, "Door"));
        assertEquals("box", linkNameOf(result, "Box"));
        assertEquals(1, countLinksNamed(result, "Door_left"));
    }

    @Test
    void parametric_site_keeps_tracked_edge_and_context_child() throws Exception {
        DynamicSignature signature = sig(1);
        PureBigraphBuilder<DynamicSignature> agentBuilder = pureBuilder(signature);
        agentBuilder.root()
                .child("Door").linkInner("d").top()
                .child("Room").linkInner("d").down()
                .child("Box").linkInner("b")
                .child("Sensor").linkInner("b");
        agentBuilder.closeInner();
        PureBigraph agent = agentBuilder.create();
        renameLink(linkOf(agent, "Door"), "Door_left");
        renameLink(linkOf(agent, "Box"), "box");

        PureBigraphBuilder<DynamicSignature> redex = pureBuilder(signature);
        redex.root()
                .child("Door").linkInner("d").top()
                .child("Room").linkInner("d").down().site();
        redex.closeInner();
        PureBigraphBuilder<DynamicSignature> reactum = pureBuilder(signature);
        reactum.root()
                .child("Door").linkInner("d").top()
                .child("Room").linkInner("d").down().site();
        reactum.closeInner();
        ParametricReactionRule<PureBigraph> rule = new ParametricReactionRule<>(redex.create(), reactum.create());
        rule.withTrackingMap(nodeIdentity("v0", "v1").trackLink("e0", "e0"));

        PureBigraph result = applyOnce(agent, rule);

        assertEquals("Door_left", linkNameOf(result, "Door"));
        assertEquals("box", linkNameOf(result, "Box"));
        assertEquals("Room", parentControl(result, "Box"));
        assertEquals(1, countLinksNamed(result, "Door_left"));
        assertEquals("v0", nodeName(result, "Door"));
        assertEquals("v1", nodeName(result, "Room"));
    }

    @Test
    void unmatched_edge_into_site_keeps_agent_name() throws Exception {
        DynamicSignature signature = sig(1);
        PureBigraphBuilder<DynamicSignature> agentBuilder = pureBuilder(signature);
        agentBuilder.root()
                .child("Door").linkInner("d").top()
                .child("Room").linkInner("d").down()
                .child("Box").linkInner("b")
                .top()
                .child("Sensor").linkInner("b");
        agentBuilder.closeInner();
        PureBigraph agent = agentBuilder.create();
        renameLink(linkOf(agent, "Door"), "Door_left");
        renameLink(linkOf(agent, "Box"), "box");

        PureBigraphBuilder<DynamicSignature> redex = pureBuilder(signature);
        redex.root()
                .child("Door").linkInner("d").top()
                .child("Room").linkInner("d").down().site();
        redex.closeInner();
        PureBigraphBuilder<DynamicSignature> reactum = pureBuilder(signature);
        reactum.root()
                .child("Door").linkInner("d").top()
                .child("Room").linkInner("d").down().site();
        reactum.closeInner();
        ParametricReactionRule<PureBigraph> rule = new ParametricReactionRule<>(redex.create(), reactum.create());
        rule.withTrackingMap(nodeIdentity("v0", "v1").trackLink("e0", "e0"));

        PureBigraph result = applyOnce(agent, rule);

        assertEquals("Door_left", linkNameOf(result, "Door"));
        assertEquals("box", linkNameOf(result, "Box", "Sensor"));
        assertEquals("Room", parentControl(result, "Box"));
        assertEquals(1, countLinksNamed(result, "box"));
        assertEquals(1, countLinksNamed(result, "Door_left"));
    }

    private static PureBigraph agentWithNamedDoorLinkAndContextEdge(DynamicSignature signature) throws Exception {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        builder.root().connectByEdge("Door", "Room").connectByEdge("Box", "Sensor");
        PureBigraph agent = builder.create();
        renameLink(linkOf(agent, "Door"), "Door_left");
        return agent;
    }

    private static ParametricReactionRule<PureBigraph> identityDoorRoomRule(
            DynamicSignature signature,
            Consumer<TrackingMap> tracking
    ) throws Exception {
        PureBigraphBuilder<DynamicSignature> redex = pureBuilder(signature);
        redex.root().connectByEdge("Door", "Room");
        PureBigraphBuilder<DynamicSignature> reactum = pureBuilder(signature);
        reactum.root().connectByEdge("Door", "Room");
        TrackingMap map = nodeIdentity("v0", "v1");
        tracking.accept(map);
        ParametricReactionRule<PureBigraph> rule = new ParametricReactionRule<>(redex.create(), reactum.create());
        rule.withTrackingMap(map);
        return rule;
    }

    private static TrackingMap nodeIdentity(String... reactumToRedex) {
        TrackingMap map = new TrackingMap();
        for (String name : reactumToRedex) {
            map.put(name, name);
        }
        return map;
    }

    private static PureBigraph applyOnce(PureBigraph agent, ParametricReactionRule<PureBigraph> rule) {
        PureReactiveSystem rs = new PureReactiveSystem();
        rs.setAgent(agent);
        try {
            rs.addReactionRule(rule);
        } catch (InvalidReactionRuleException e) {
            throw new IllegalStateException(e);
        }
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<?> matches = matcher.match(agent, rule);
        Iterator<?> iterator = matches.iterator();
        assertTrue(iterator.hasNext(), "rule must match the agent");
        PureBigraphMatch match = (PureBigraphMatch) iterator.next();
        PureBigraph result = rs.buildParametricReaction(agent, match, rule);
        assertNotNull(result);
        return result;
    }

    private static String nodeName(PureBigraph bigraph, String control) {
        return node(bigraph, control).getName();
    }

    private static String parentControl(PureBigraph bigraph, String control) {
        BigraphEntity<?> parent = bigraph.getParent(node(bigraph, control));
        if (parent instanceof BigraphEntity.NodeEntity<?> parentNode) {
            return parentNode.getControl().getNamedType().stringValue();
        }
        throw new AssertionError("parent of " + control + " is not a node");
    }

    private static long countLinksNamed(PureBigraph bigraph, String name) {
        return bigraph.getAllLinks().stream().filter(link -> name.equals(link.getName())).count();
    }

    private static String linkNameOf(PureBigraph bigraph, String control) {
        return linkOf(bigraph, control).getName();
    }

    private static String linkNameOf(PureBigraph bigraph, String fromControl, String toControl) {
        BigraphEntity.NodeEntity<?> from = node(bigraph, fromControl);
        for (BigraphEntity.Port port : bigraph.getPorts(from)) {
            BigraphEntity.Link link = bigraph.getLinkOfPoint(port);
            if (link == null) {
                continue;
            }
            boolean hitsTarget = bigraph.getPointsFromLink(link).stream().anyMatch(point -> {
                if (!(point instanceof BigraphEntity.Port)) {
                    return false;
                }
                return ownerOf(bigraph, (BigraphEntity.Port) point).getControl().getNamedType().stringValue()
                        .equals(toControl);
            });
            if (hitsTarget) {
                return link.getName();
            }
        }
        throw new AssertionError("no link from " + fromControl + " to " + toControl);
    }

    private static BigraphEntity.Link linkOf(PureBigraph bigraph, String control) {
        BigraphEntity.NodeEntity<?> node = node(bigraph, control);
        BigraphEntity.Link link = bigraph.getLinkOfPoint(bigraph.getPorts(node).iterator().next());
        assertNotNull(link);
        return link;
    }

    private static BigraphEntity.NodeEntity<?> node(PureBigraph bigraph, String control) {
        return bigraph.getNodes().stream()
                .filter(n -> n.getControl().getNamedType().stringValue().equals(control))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing control " + control));
    }

    private static BigraphEntity.NodeEntity<?> ownerOf(PureBigraph bigraph, BigraphEntity.Port port) {
        return bigraph.getNodes().stream()
                .filter(n -> bigraph.getPorts(n).stream().anyMatch(p -> p.getInstance().equals(port.getInstance())))
                .findFirst()
                .orElseThrow();
    }

    private static void renameLink(BigraphEntity.Link link, String name) {
        EAttribute nameAttr = EMFUtils.findAttribute(link.getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
        link.getInstance().eSet(nameAttr, name);
    }
}
