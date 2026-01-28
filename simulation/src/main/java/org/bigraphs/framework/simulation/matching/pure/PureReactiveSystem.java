/*
 * Copyright (c) 2021-2026 Bigraph Toolkit Suite Developers
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

import it.uniud.mads.jlibbig.core.Owner;
import it.uniud.mads.jlibbig.core.attachedProperties.Property;
import it.uniud.mads.jlibbig.core.std.*;
import it.uniud.mads.jlibbig.core.util.NameGenerator;
import java.util.*;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphDecoder;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphEncoder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.AbstractSimpleReactiveSystem;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of an {@link AbstractSimpleReactiveSystem} providing a simple BRS data structure for pure bigraphs
 * (see {@link PureBigraph}) and possibly later also binding bigraphs, bigraphs with sharing etc.
 * <p>
 * Uses some functionality from jLibBig.
 *
 * @author Dominik Grzelak
 * @see PureBigraph
 */
public class PureReactiveSystem extends AbstractSimpleReactiveSystem<PureBigraph> {
    private final Logger logger = LoggerFactory.getLogger(PureReactiveSystem.class);
    JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
    JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();

    static it.uniud.mads.jlibbig.core.std.InstantiationMap constructEta(ReactionRule<PureBigraph> reactionRule) {
        org.bigraphs.framework.core.reactivesystem.InstantiationMap instantationMap = reactionRule.getInstantationMap();
        int[] imArray = new int[instantationMap.getMappings().size()];
        for (int i = 0; i < imArray.length; i++) {
            imArray[i] = instantationMap.get(i).getValue();
        }
        InstantiationMap eta = new InstantiationMap(
                reactionRule.getRedex().getSites().size(),
                imArray
        );
        return eta;
    }

    @Override
    public PureBigraph buildGroundReaction(final PureBigraph agent, final BigraphMatch<PureBigraph> match, final ReactionRule<PureBigraph> rule) {
        try {
            // Test also "AgentRewritingRule" from jLibBig

            PureBigraphMatch matchResult = (PureBigraphMatch) match;
            AgentMatch jLibMatchResult = (AgentMatch) matchResult.getJLibMatchResult();
            InstantiationMap eta = constructEta(rule);

            // Store params as pure bigraph object
            PureBigraph decodedParam = null;
            try {
                if (jLibMatchResult.getParam() != null) {
                    decodedParam = decoder.decode(jLibMatchResult.getParam());
                }
            } catch (NullPointerException ignored) {}
            ((PureBigraphMatch) match).setParam(decodedParam);

            boolean[] cloneParam = new boolean[eta.getPlaceDomain()];
            int prms[] = new int[eta.getPlaceDomain()];
            for (int i = 0; i < eta.getPlaceDomain(); i++) {
                int j = eta.getPlaceInstance(i);
                prms[i] = j;
            }
            for (int i = 0; i < prms.length; i++) {
                if (cloneParam[i])
                    continue;
                for (int j = i + 1; j < prms.length; j++) {
                    cloneParam[j] = cloneParam[j] || (prms[i] == prms[j]);
                }
            }

            BigraphBuilder bb = new BigraphBuilder(jLibMatchResult.getRedex().getSignature());
            for (int i = eta.getPlaceDomain() - 1; 0 <= i; i--) {
                bb.leftJuxtapose(jLibMatchResult.getParams().get(eta.getPlaceInstance(i)),
                        !cloneParam[i]);
            }

            Bigraph lambda = jLibMatchResult.getParamWiring();
            for (EditableInnerName n : lambda.inners.values()) {
                if (!bb.containsOuterName(n.getName())) {
                    lambda.inners.remove(n.getName());
                    n.setHandle(null);
                }
            }
            for (int i = eta.getPlaceCodomain() - eta.getPlaceDomain(); i > 0; i--) {
                lambda.roots.remove(0);
                lambda.sites.remove(0);
            }
            for (int i = eta.getPlaceDomain() - eta.getPlaceCodomain(); i > 0; i--) {
                EditableRoot r = new EditableRoot();
                r.setOwner(lambda);
                EditableSite s = new EditableSite(r);
                lambda.roots.add(r);
                lambda.sites.add(s);
            }
            bb.outerCompose(lambda, true);

            // Note: instantiateReactumV2 from AgentRewritingRule doesn't work well with our copyAttributes procedure
            Bigraph inreact = instantiateReactum(jLibMatchResult, rule);
            inreact = Bigraph.juxtapose(inreact, jLibMatchResult.getRedexId(), true);

            // Store redexImage as pure bigraph object
            PureBigraph decodedRdxImg = decoder.decode(jLibMatchResult.getRedex());
            ((PureBigraphMatch) match).setRedexImage(decodedRdxImg);

            // Collect the reactum nodes for later when we do the relabeling
            // (they get lost after jLibBig composition)
            List<String> reactumLabelNames = new ArrayList<>();
            if (rule.getTrackingMap() != null && !rule.getTrackingMap().isEmpty()) {
                reactumLabelNames.addAll(inreact.getNodes().stream().map(x -> x.getEditable().getName())
                        .toList());
            }

            bb.outerCompose(inreact, true);

            // Store context as pure bigraph object
            PureBigraph decodedContext = decoder.decode(jLibMatchResult.getContext());
            ((PureBigraphMatch) match).setContext(decodedContext);

            bb.outerCompose(jLibMatchResult.getContext(), true);

            // Do relabeling only when we have tracking map. Relabeling allows tracing of nodes
            // relabeling is done outside of jlibbig routine for SoC reasons
            if (rule.getTrackingMap() != null && !rule.getTrackingMap().isEmpty()) {
                List<Parent> collectCtxNodes = new ArrayList<>();
                Deque<Parent> q = new ArrayDeque<>(bb.getRoots());
                while (!q.isEmpty()) {
                    Parent currentElem = q.poll();
                    if (!collectCtxNodes.contains(currentElem) && currentElem.isNode()) {
                        collectCtxNodes.add(currentElem);
                        EditableNode editableNode = (EditableNode) currentElem.getEditable();
                        if (jLibMatchResult.ctxEmbMappping.get(editableNode) == null) { // is rule node
                            String originalReactumLabel = ((EditableNode) currentElem).getName();
                            if (reactumLabelNames.contains(originalReactumLabel)) {
                                String originalRedexLbl = rule.getTrackingMap().get(originalReactumLabel);
                                if (originalRedexLbl != null && !originalRedexLbl.isEmpty()) {
                                    // emb is only for redex <-> agent
                                    Optional<Node> first = jLibMatchResult.emb_nodes.keySet().stream()
                                            .filter(x -> x.isNode() && x.getEditable().getName().equals(originalRedexLbl))
                                            .findFirst();
                                    assert first.isPresent();
                                    if (first.isPresent()) {
                                        editableNode.setName(jLibMatchResult.emb_nodes.get(first.get()).getEditable().getName());
                                    }
                                } else { // else: its a new node, so we create a fresh label, which is different from all others
                                    editableNode.setName("N_" + NameGenerator.DEFAULT.generate());
                                }
                            }
                        } else { // is context node
                            String name = ((EditableNode) jLibMatchResult.ctxEmbMappping.get(editableNode)).getName();
                            editableNode.setName(name);
                        }
                    }
                    // collect next nodes in the tree
                    for (Child c : currentElem.getChildren()) {
                        if (c.isNode()) {
                            q.add((Parent) c);
                        }
                    }
                }
            }

            Bigraph result = bb.makeBigraph(true);
            PureBigraph decodedResult = decoder.decode(result, agent.getSignature());
            copyAttributes(agent, decodedResult);
            return decodedResult;
        } catch (Exception e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public PureBigraph buildParametricReaction(final PureBigraph agent, final BigraphMatch<PureBigraph> match, final ReactionRule<PureBigraph> rule) {
        try {
            return this.buildGroundReaction(agent, match, rule);
        } catch (Exception e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    /**
     * Instantiates rule's reactum with respect to the given match.
     *
     * @param match the match with respect to the reactum has to be instantiated.
     * @return the reactum instance.
     */
    // from RewritingRule
    protected final Bigraph instantiateReactum(Match match, ReactionRule<PureBigraph> rule) {
        Bigraph reactum = encoder.encode(rule.getReactum(), match.getRedex().getSignature());
        Bigraph big = new Bigraph(reactum.getSignature());
        Owner owner = big;
        Map<Handle, EditableHandle> hnd_dic = new HashMap<>();
        // replicate outer names
        for (EditableOuterName o1 : reactum.outers.values()) {
            EditableOuterName o2 = o1.replicate();
            big.outers.put(o2.getName(), o2);
            o2.setOwner(owner);
            hnd_dic.put(o1, o2);
        }
        // replicate inner names
        for (EditableInnerName i1 : reactum.inners.values()) {
            EditableInnerName i2 = i1.replicate();
            EditableHandle h1 = i1.getHandle();
            EditableHandle h2 = hnd_dic.get(h1);
            if (h2 == null) {
                // the bigraph is inconsistent if g is null
                h2 = h1.replicate();
                h2.setOwner(owner);
                hnd_dic.put(h1, h2);
            }
            i2.setHandle(h2);
            big.inners.put(i2.getName(), i2);
        }
        // replicate place structure
        // the queue is used for a breadth first visit
        class Pair {
            final EditableChild c;
            final EditableParent p;

            Pair(EditableParent p, EditableChild c) {
                this.c = c;
                this.p = p;
            }
        }
        Deque<Pair> q = new ArrayDeque<>();
        for (EditableRoot r1 : reactum.roots) {
            EditableRoot r2 = r1.replicate();
            big.roots.add(r2);
            r2.setOwner(owner);
            for (EditableChild c : r1.getEditableChildren()) {
                q.add(new Pair(r2, c));
            }
        }
        EditableSite[] sites = new EditableSite[reactum.sites.size()];
        while (!q.isEmpty()) {
            Pair t = q.poll();
            if (t.c instanceof EditableNode) {
                EditableNode n1 = (EditableNode) t.c;
                EditableNode n2 = n1.replicate();
                n2.setName(n1.getName());
                for (Property each : n1.getProperties()) {
                    if (each.getName().equals("Owner")) continue;
                    n2.attachProperty(each);
                }
                instantiateReactumNode(n1, n2, match);
                // set m's parent (which added adds m as its child)
                n2.setParent(t.p);
                for (int i = n1.getControl().getArity() - 1; 0 <= i; i--) {
                    EditableNode.EditablePort p1 = n1.getPort(i);
                    EditableHandle h1 = p1.getHandle();
                    // looks for an existing replica
                    EditableHandle h2 = hnd_dic.get(h1);
                    if (h2 == null) {
                        // the bigraph is inconsistent if g is null
                        h2 = h1.replicate();
                        h2.setOwner(owner);
                        hnd_dic.put(h1, h2);
                    }
                    n2.getPort(i).setHandle(h2);
                }

                // enqueue children for visit
                for (EditableChild c : n1.getEditableChildren()) {
                    q.add(new Pair(n2, c));
                }
            } else {
                // c instanceof EditableSite
                EditableSite s1 = (EditableSite) t.c;
                EditableSite s2 = s1.replicate();
                s2.setParent(t.p);
                sites[reactum.sites.indexOf(s1)] = s2;
            }
        }
        big.sites.addAll(Arrays.asList(sites));
        return big;
    }

    // from AgentRewritingRule
    protected final Bigraph instantiateReactumV2(Match match, ReactionRule<PureBigraph> rule) {
        Bigraph reactum = encoder.encode(rule.getReactum(), match.getRedex().getSignature());
        Bigraph big = new Bigraph(reactum.getSignature());
//        Bigraph reactum = getReactum();
//        Bigraph big = new Bigraph(reactum.signature);
        Owner owner = big;
        Map<Handle, EditableHandle> hnd_dic = new HashMap<>();
        // replicate outer names
        for (EditableOuterName o1 : reactum.outers.values()) {
            EditableOuterName o2 = o1.replicate();
            big.outers.put(o2.getName(), o2);
            o2.setOwner(owner);
            hnd_dic.put(o1, o2);
        }
        // replicate inner names
        for (EditableInnerName i1 : reactum.inners.values()) {
            EditableInnerName i2 = i1.replicate();
            EditableHandle h1 = i1.getHandle();
            EditableHandle h2 = hnd_dic.get(h1);
            if (h2 == null) {
                // the bigraph is inconsistent if g is null
                h2 = h1.replicate();
                h2.setOwner(owner);
                hnd_dic.put(h1, h2);
            }
            i2.setHandle(h2);
            big.inners.put(i2.getName(), i2);
        }
        // replicate place structure
        // the queue is used for a breadth first visit
        class Pair {
            final EditableChild c;
            final EditableParent p;

            Pair(EditableParent p, EditableChild c) {
                this.c = c;
                this.p = p;
            }
        }
        Deque<Pair> q = new ArrayDeque<>();
        for (EditableRoot r1 : reactum.roots) {
            EditableRoot r2 = r1.replicate();
            big.roots.add(r2);
            r2.setOwner(owner);
            for (EditableChild c : r1.getEditableChildren()) {
                q.add(new Pair(r2, c));
            }
        }
        EditableSite[] sites = new EditableSite[reactum.sites.size()];
        while (!q.isEmpty()) {
            Pair t = q.poll();
            if (t.c instanceof EditableNode) {
                EditableNode n1 = (EditableNode) t.c;
                EditableNode n2 = n1.replicate();
                instantiateReactumNode(n1, n2, match);
                // set m's parent (which added adds m as its child)
                n2.setParent(t.p);
                for (int i = n1.getControl().getArity() - 1; 0 <= i; i--) {
                    EditableNode.EditablePort p1 = n1.getPort(i);
                    EditableHandle h1 = p1.getHandle();
                    // looks for an existing replica
                    EditableHandle h2 = hnd_dic.get(h1);
                    if (h2 == null) {
                        // the bigraph is inconsistent if g is null
                        h2 = h1.replicate();
                        h2.setOwner(owner);
                        hnd_dic.put(h1, h2);
                    }
                    n2.getPort(i).setHandle(h2);
                }
                // enqueue children for visit
                for (EditableChild c : n1.getEditableChildren()) {
                    q.add(new Pair(n2, c));
                }
            } else {
                // c instanceof EditableSite
                EditableSite s1 = (EditableSite) t.c;
                EditableSite s2 = s1.replicate();
                s2.setParent(t.p);
                sites[reactum.sites.indexOf(s1)] = s2;
            }
        }
        big.sites.addAll(Arrays.asList(sites));
        return big;
    }

    /**
     * This method is called during the instantiation of rule's reactum. Inherit
     * this method to customise instantiation of Nodes e.g. attaching properties
     * taken from nodes in the redex image determined by the given match.
     *
     * @param original The original node from the reactum.
     * @param instance The replica to be used.
     * @param match    The match referred by the instantiation.
     */
    protected void instantiateReactumNode(Node original, Node instance,
                                          Match match) {
    }
}
