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
package org.bigraphs.framework.simulation.matching.pure;

import it.uniud.mads.jlibbig.core.std.*;
import it.uniud.mads.jlibbig.core.util.NameGenerator;
import java.util.*;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.TrackingMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Restores node and hyperedge names after a jLib rewrite when the rule has a
 * {@link TrackingMap}. Relabeling is kept outside the jLib composition path.
 */
final class TrackingMapRelabeler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackingMapRelabeler.class);

    private final ReactionRule<PureBigraph> rule;
    private final List<String> reactumLabelNames;
    private final Map<String, NodePort> reactumLinkIncidents;
    private final boolean enabled;

    private TrackingMapRelabeler(
            ReactionRule<PureBigraph> rule,
            List<String> reactumLabelNames,
            Map<String, NodePort> reactumLinkIncidents,
            boolean enabled
    ) {
        this.rule = rule;
        this.reactumLabelNames = reactumLabelNames;
        this.reactumLinkIncidents = reactumLinkIncidents;
        this.enabled = enabled;
    }

    /**
     * Capture reactum node names and link incidence before jLib compose
     * overwrites them.
     */
    static TrackingMapRelabeler prepare(ReactionRule<PureBigraph> rule, Bigraph inreact) {
        TrackingMap map = rule.getTrackingMap();
        if (map == null || map.isEmpty()) {
            return new TrackingMapRelabeler(rule, List.of(), Map.of(), false);
        }
        List<String> reactumLabelNames = inreact.getNodes().stream()
                .map(x -> x.getEditable().getName())
                .toList();
        // Incidence must come from the original reactum: instantiateReactum
        // replicates edges without copying their names (jLib EditableEdge.replicate()).
        return new TrackingMapRelabeler(
                rule,
                reactumLabelNames,
                collectReactumLinkIncidents(rule.getReactum()),
                true
        );
    }

    void apply(BigraphBuilder bb, AgentMatch match, PureBigraph agent) {
        if (!enabled) {
            return;
        }
        relabelTrackedLinks(bb, match, agent);
        restoreNonRuleLinkNames(bb, match, agent);
        relabelNodes(bb, match);
    }

    private record NodePort(String nodeName, int portIndex) {
    }

    private record NodePortRef(EditableNode node, int portIndex) {
    }

    /**
     * Renames tracked closed edges / outer names after composition, before node relabeling.
     * The result handle is the port on the rule node that carried this link in the reactum
     * (names may already have changed during compose). Zero or several such handles: do not rename.
     */
    private void relabelTrackedLinks(BigraphBuilder bb, AgentMatch match, PureBigraph agent) {
        TrackingMap trackingMap = rule.getTrackingMap();
        if (trackingMap.getLinks().isEmpty()) {
            return;
        }
        Map<String, EditableNode> ruleNodesByName = collectRuleNodesByName(bb, match);
        Set<String> occupiedNames = collectHandleNames(bb);
        for (String reactumLinkName : trackingMap.getLinks()) {
            if (!trackingMap.containsKey(reactumLinkName)) {
                continue;
            }
            NodePort incident = reactumLinkIncidents.get(reactumLinkName);
            if (incident == null) {
                LOGGER.debug("Skipping link tracking for '{}': no reactum incidence", reactumLinkName);
                continue;
            }
            EditableNode ruleNode = ruleNodesByName.get(incident.nodeName());
            if (ruleNode == null) {
                LOGGER.debug("Skipping link tracking for '{}': no unique rule node '{}'",
                        reactumLinkName, incident.nodeName());
                continue;
            }
            Handle candidate = ruleNode.getPort(incident.portIndex()).getHandle();
            if (candidate == null) {
                LOGGER.debug("Skipping link tracking for '{}': no handle on rule node '{}'",
                        reactumLinkName, incident.nodeName());
                continue;
            }
            String redexLinkName = trackingMap.get(reactumLinkName);
            String targetName;
            if (redexLinkName == null || redexLinkName.isEmpty()) {
                // Keep the reactum name (e.g. "claim"). A fresh E_… name would leak into
                // the canonical form and split isomorphic exploration states.
                targetName = reactumLinkName;
            } else {
                targetName = resolveAgentLinkName(redexLinkName, rule.getRedex(), agent, match);
                if (targetName == null || targetName.isEmpty()) {
                    LOGGER.debug("Skipping link tracking for '{}': no unique agent image for redex '{}'",
                            reactumLinkName, redexLinkName);
                    continue;
                }
            }
            String currentName = handleName(candidate);
            if (occupiedNames.contains(targetName)
                    && (currentName == null || !targetName.equals(currentName))) {
                LOGGER.debug("Skipping link tracking for '{}': name '{}' already used",
                        reactumLinkName, targetName);
                continue;
            }
            setHandleName(candidate, targetName);
            if (currentName != null) {
                occupiedNames.remove(currentName);
            }
            occupiedNames.add(targetName);
        }
    }

    /**
     * jLib mints {@code E_…} names when composing leftover context and
     * parameter edges. Copy the original agent names back onto every handle
     * that is not incident to a rule node (rule handles belong to
     * {@link #relabelTrackedLinks}).
     * <p>
     * Context-only is not enough: an unmatched door often links a context
     * node to a child that fell into a site (parameter).
     */
    private void restoreNonRuleLinkNames(BigraphBuilder bb, AgentMatch match, PureBigraph agent) {
        Map<Handle, List<NodePortRef>> incidents = collectHandleIncidents(bb);
        Set<String> occupiedNames = collectHandleNames(bb);
        for (Map.Entry<Handle, List<NodePortRef>> entry : incidents.entrySet()) {
            Handle handle = entry.getKey();
            List<NodePortRef> points = entry.getValue();
            boolean touchesRule = false;
            for (NodePortRef point : points) {
                if (isRuleNode(point.node(), match)) {
                    touchesRule = true;
                    break;
                }
            }
            if (touchesRule) {
                continue;
            }
            String targetName = null;
            for (NodePortRef point : points) {
                String agentName = agentLinkNameAt(point.node(), point.portIndex(), match, agent);
                if (agentName == null || agentName.isEmpty()) {
                    continue;
                }
                if (targetName == null) {
                    targetName = agentName;
                } else if (!targetName.equals(agentName)) {
                    targetName = null;
                    break;
                }
            }
            if (targetName == null || targetName.isEmpty()) {
                continue;
            }
            String currentName = handleName(handle);
            if (occupiedNames.contains(targetName)
                    && (currentName == null || !targetName.equals(currentName))) {
                LOGGER.debug("Skipping leftover link restore for '{}': name already used", targetName);
                continue;
            }
            setHandleName(handle, targetName);
            if (currentName != null) {
                occupiedNames.remove(currentName);
            }
            occupiedNames.add(targetName);
        }
    }

    private void relabelNodes(BigraphBuilder bb, AgentMatch match) {
        TrackingMap trackingMap = rule.getTrackingMap();
        List<Parent> collectCtxNodes = new ArrayList<>();
        Deque<Parent> q = new ArrayDeque<>(bb.getRoots());
        while (!q.isEmpty()) {
            Parent currentElem = q.poll();
            if (!collectCtxNodes.contains(currentElem) && currentElem.isNode()) {
                collectCtxNodes.add(currentElem);
                EditableNode editableNode = (EditableNode) currentElem.getEditable();
                if (match.ctxEmbMappping.get(editableNode) == null) { // is rule node
                    String originalReactumLabel = ((EditableNode) currentElem).getName();
                    if (!trackingMap.isLink(originalReactumLabel)
                            && reactumLabelNames.contains(originalReactumLabel)) {
                        String originalRedexLbl = trackingMap.get(originalReactumLabel);
                        if (originalRedexLbl != null && !originalRedexLbl.isEmpty()) {
                            // emb is only for redex <-> agent
                            Optional<Node> first = match.emb_nodes.keySet().stream()
                                    .filter(x -> x.isNode() && x.getEditable().getName().equals(originalRedexLbl))
                                    .findFirst();
                            assert first.isPresent();
                            if (first.isPresent()) {
                                editableNode.setName(match.emb_nodes.get(first.get()).getEditable().getName());
                            }
                        } else { // else: its a new node, so we create a fresh label, which is different from all others
                            editableNode.setName("N_" + NameGenerator.DEFAULT.generate());
                        }
                    }
                } else { // is context node
                    String name = ((EditableNode) match.ctxEmbMappping.get(editableNode)).getName();
                    editableNode.setName(name);
                }
            }
            for (Child c : currentElem.getChildren()) {
                if (c.isNode()) {
                    q.add((Parent) c);
                }
            }
        }
    }

    private Map<Handle, List<NodePortRef>> collectHandleIncidents(BigraphBuilder bb) {
        Map<Handle, List<NodePortRef>> incidents = new IdentityHashMap<>();
        Deque<Parent> q = new ArrayDeque<>(bb.getRoots());
        Set<Parent> seen = new HashSet<>();
        while (!q.isEmpty()) {
            Parent current = q.poll();
            if (!seen.add(current)) {
                continue;
            }
            if (current.isNode()) {
                EditableNode node = (EditableNode) current.getEditable();
                int arity = node.getControl().getArity();
                for (int i = 0; i < arity; i++) {
                    Handle handle = node.getPort(i).getHandle();
                    if (handle != null) {
                        incidents.computeIfAbsent(handle, k -> new ArrayList<>())
                                .add(new NodePortRef(node, i));
                    }
                }
            }
            for (Child child : current.getChildren()) {
                if (child.isNode()) {
                    q.add((Parent) child);
                }
            }
        }
        return incidents;
    }

    private boolean isRuleNode(EditableNode node, AgentMatch match) {
        if (match.ctxEmbMappping.get(node) != null) {
            return false;
        }
        return reactumLabelNames.contains(node.getName());
    }

    private String agentLinkNameAt(EditableNode node, int portIndex, AgentMatch match, PureBigraph agent) {
        String agentNodeName;
        Object original = match.ctxEmbMappping.get(node);
        if (original instanceof EditableNode originalNode) {
            agentNodeName = originalNode.getName();
        } else {
            agentNodeName = node.getName();
        }
        if (agentNodeName == null || agentNodeName.isEmpty()) {
            return null;
        }
        var agentNodes = agent.getNodes().stream()
                .filter(n -> agentNodeName.equals(n.getName()))
                .toList();
        if (agentNodes.size() != 1) {
            return null;
        }
        List<BigraphEntity.Port> ports = agent.getPorts(agentNodes.get(0));
        if (portIndex < 0 || portIndex >= ports.size()) {
            return null;
        }
        BigraphEntity.Link link = agent.getLinkOfPoint(ports.get(portIndex));
        if (link == null || link.getName() == null || link.getName().isEmpty()) {
            return null;
        }
        return link.getName();
    }

    private static Map<String, NodePort> collectReactumLinkIncidents(PureBigraph reactum) {
        Map<String, NodePort> incidents = new LinkedHashMap<>();
        for (BigraphEntity.NodeEntity<?> node : reactum.getNodes()) {
            for (BigraphEntity.Port port : reactum.getPorts(node)) {
                BigraphEntity.Link link = reactum.getLinkOfPoint(port);
                if (link != null && link.getName() != null && !link.getName().isEmpty()) {
                    incidents.putIfAbsent(link.getName(), new NodePort(node.getName(), port.getIndex()));
                }
            }
        }
        return incidents;
    }

    private Map<String, EditableNode> collectRuleNodesByName(BigraphBuilder bb, AgentMatch match) {
        Map<String, List<EditableNode>> grouped = new LinkedHashMap<>();
        Deque<Parent> q = new ArrayDeque<>(bb.getRoots());
        Set<Parent> seen = new HashSet<>();
        while (!q.isEmpty()) {
            Parent current = q.poll();
            if (!seen.add(current)) {
                continue;
            }
            if (current.isNode()) {
                EditableNode node = (EditableNode) current.getEditable();
                if (match.ctxEmbMappping.get(node) == null) {
                    grouped.computeIfAbsent(node.getName(), k -> new ArrayList<>()).add(node);
                }
            }
            for (Child child : current.getChildren()) {
                if (child.isNode()) {
                    q.add((Parent) child);
                }
            }
        }
        Map<String, EditableNode> unique = new LinkedHashMap<>();
        grouped.forEach((name, nodes) -> {
            if (nodes.size() == 1) {
                unique.put(name, nodes.get(0));
            }
        });
        return unique;
    }

    private Set<String> collectHandleNames(BigraphBuilder bb) {
        Set<String> names = new HashSet<>();
        Deque<Parent> q = new ArrayDeque<>(bb.getRoots());
        Set<Parent> seen = new HashSet<>();
        Set<Handle> handles = Collections.newSetFromMap(new IdentityHashMap<>());
        while (!q.isEmpty()) {
            Parent current = q.poll();
            if (!seen.add(current)) {
                continue;
            }
            if (current.isNode()) {
                EditableNode node = (EditableNode) current.getEditable();
                int arity = node.getControl().getArity();
                for (int i = 0; i < arity; i++) {
                    Handle handle = node.getPort(i).getHandle();
                    if (handle != null && handles.add(handle)) {
                        String name = handleName(handle);
                        if (name != null) {
                            names.add(name);
                        }
                    }
                }
            }
            for (Child child : current.getChildren()) {
                if (child.isNode()) {
                    q.add((Parent) child);
                }
            }
        }
        return names;
    }

    private String handleName(Handle handle) {
        if (handle instanceof Edge) {
            return ((Edge) handle).getEditable().getName();
        }
        if (handle instanceof OuterName) {
            return ((OuterName) handle).getName();
        }
        return null;
    }

    private void setHandleName(Handle handle, String name) {
        if (handle instanceof Edge) {
            ((Edge) handle).getEditable().setName(name);
            return;
        }
        if (handle instanceof OuterName) {
            ((OuterName) handle).getEditable().setName(name);
        }
    }

    private String resolveAgentLinkName(
            String redexLinkName,
            PureBigraph redex,
            PureBigraph agent,
            AgentMatch match
    ) {
        for (BigraphEntity.NodeEntity<?> node : redex.getNodes()) {
            for (BigraphEntity.Port port : redex.getPorts(node)) {
                BigraphEntity.Link link = redex.getLinkOfPoint(port);
                if (link == null || !redexLinkName.equals(link.getName())) {
                    continue;
                }
                Optional<Node> jRedex = match.emb_nodes.keySet().stream()
                        .filter(x -> x.isNode() && x.getEditable().getName().equals(node.getName()))
                        .findFirst();
                if (jRedex.isEmpty()) {
                    continue;
                }
                String agentNodeName = match.emb_nodes.get(jRedex.get()).getEditable().getName();
                var agentNode = agent.getNodes().stream()
                        .filter(n -> n.getName().equals(agentNodeName))
                        .findFirst();
                if (agentNode.isEmpty()) {
                    continue;
                }
                List<BigraphEntity.Port> agentPorts = agent.getPorts(agentNode.get());
                if (port.getIndex() >= agentPorts.size()) {
                    continue;
                }
                BigraphEntity.Link agentLink = agent.getLinkOfPoint(agentPorts.get(port.getIndex()));
                if (agentLink != null && agentLink.getName() != null && !agentLink.getName().isEmpty()) {
                    return agentLink.getName();
                }
            }
        }
        return null;
    }
}
