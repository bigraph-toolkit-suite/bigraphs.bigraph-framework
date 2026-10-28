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
package org.bigraphs.framework.core.impl.pure;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The points of a link and the ports of a node are cached lazily by {@link PureBigraph}.
 * Every mutation of a {@link PureBigraphMutable} must invalidate these caches, otherwise
 * {@link PureBigraph#getPointsFromLink(BigraphEntity.Link)} keeps returning the state of its first call.
 */
public class PureBigraphMutableLinkCacheTest {

    private DynamicSignature sig;
    private PureBigraphMutable g;
    private BigraphEntity.NodeEntity<DynamicControl> a1, a2, a3;
    private BigraphEntity.Edge e;

    @BeforeEach
    void setUp() {
        sig = pureSignatureBuilder().add("A", 1).add("B", 2).create();
        g = pureBuilder(sig).createMutable();
        BigraphEntity.RootEntity root = g.addRoot();
        a1 = g.addNode(root, sig.getControlByName("A"), "a1");
        a2 = g.addNode(root, sig.getControlByName("A"), "a2");
        a3 = g.addNode(root, sig.getControlByName("A"), "a3");
        e = g.addEdge("e");
        g.connectNodeToLink(a1, e);
        g.connectNodeToLink(a2, e);
        assertEquals(Set.of(a1, a2), nodesOf(e)); // fills the cache
    }

    @Test
    void connectingAPortIsVisibleAfterTheLinkWasRead() {
        g.connectNodeToLink(a3, e);
        assertEquals(Set.of(a1, a2, a3), nodesOf(e));
    }

    @Test
    void disconnectingAPortIsVisibleAfterTheLinkWasRead() {
        g.connectNodeToLink(a3, e);
        nodesOf(e);
        g.disconnectPort(g.getPorts(a1).get(0));
        assertEquals(Set.of(a2, a3), nodesOf(e));
    }

    @Test
    void removingANodeIsVisibleAfterTheLinkWasRead() {
        g.connectNodeToLink(a3, e);
        nodesOf(e);
        g.disconnectPort(g.getPorts(a1).get(0));
        nodesOf(e);
        g.removeNode(a2);
        assertEquals(Set.of(a3), nodesOf(e));
    }

    @Test
    void movingAPortToAnotherLinkUpdatesBothLinks() {
        BigraphEntity.OuterName x = g.addOuterName("x");
        nodesOf(x);
        BigraphEntity.Port p1 = g.getPorts(a1).get(0);
        g.disconnectPort(p1);
        g.connectPortToLink(p1, x);
        assertEquals(Set.of(a2), nodesOf(e));
        assertEquals(Set.of(a1), nodesOf(x));
    }

    @Test
    void innerNamesAreVisibleAfterTheLinkWasRead() {
        BigraphEntity.InnerName y = g.addInnerName("y");
        g.connectInnerNameToLink(y, e);
        assertTrue(g.getPointsFromLink(e).contains(y));
        g.disconnectInnerName(y);
        assertFalse(g.getPointsFromLink(e).contains(y));
    }

    @Test
    void portsAddedAfterTheyWereReadAreVisible() {
        BigraphEntity.NodeEntity<DynamicControl> b = g.addNode(g.getRoots().get(0), sig.getControlByName("B"), "b");
        int before = g.getPorts(b).size(); // caches the ports of b
        BigraphEntity.Port added = g.addPortToNode(b);
        assertEquals(before + 1, g.getPorts(b).size());
        BigraphEntity.OuterName x = g.addOuterName("x");
        g.connectPortToLink(added, x);
        assertEquals(1, g.getPointsFromLink(x).size());
        assertEquals(b, g.getNodeOfPort((BigraphEntity.Port) g.getPointsFromLink(x).get(0)));
    }

    @Test
    void cachedViewAgreesWithAFreshWrapper() throws Exception {
        g.connectNodeToLink(a3, e);
        g.disconnectPort(g.getPorts(a1).get(0));
        g.removeNode(a2);
        PureBigraphMutable fresh = PureBigraphBuilder.create(sig, g.getMetaModel(), g.getInstanceModel()).createMutable();
        BigraphEntity.Edge freshE = fresh.getEdges().get(0);
        assertEquals(
                fresh.getPointsFromLink(freshE).stream().map(p -> p.getInstance()).collect(Collectors.toSet()),
                g.getPointsFromLink(e).stream().map(p -> p.getInstance()).collect(Collectors.toSet()));
    }

    private Set<BigraphEntity.NodeEntity<DynamicControl>> nodesOf(BigraphEntity.Link link) {
        return g.getPointsFromLink(link).stream()
                .filter(p -> p instanceof BigraphEntity.Port)
                .map(p -> g.getNodeOfPort((BigraphEntity.Port) p))
                .collect(Collectors.toSet());
    }
}
