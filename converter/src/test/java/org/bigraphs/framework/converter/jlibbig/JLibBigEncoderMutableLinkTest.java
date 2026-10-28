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
package org.bigraphs.framework.converter.jlibbig;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;

import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Edge;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraphMutable;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.junit.jupiter.api.Test;

/**
 * The encoder reads the link graph through {@code getPointsFromLink}. A port connected to an edge
 * after the edge was read must end up on the same jLibBig edge, not on an edge of its own.
 */
public class JLibBigEncoderMutableLinkTest {

    @Test
    void portConnectedAfterTheEdgeWasReadStaysOnTheSameEdge() {
        DynamicSignature sig = pureSignatureBuilder().add("A", 1).create();
        PureBigraphMutable g = pureBuilder(sig).createMutable();
        BigraphEntity.RootEntity root = g.addRoot();
        BigraphEntity.NodeEntity<DynamicControl> a1 = g.addNode(root, sig.getControlByName("A"), "a1");
        BigraphEntity.NodeEntity<DynamicControl> a2 = g.addNode(root, sig.getControlByName("A"), "a2");
        BigraphEntity.NodeEntity<DynamicControl> a3 = g.addNode(root, sig.getControlByName("A"), "a3");
        BigraphEntity.Edge e = g.addEdge("e");
        g.connectNodeToLink(a1, e);
        g.connectNodeToLink(a2, e);
        new JLibBigBigraphEncoder().encode(g); // reads the points of e

        g.connectNodeToLink(a3, e);
        Bigraph encoded = new JLibBigBigraphEncoder().encode(g);

        assertEquals(1, encoded.getEdges().size());
        Edge only = encoded.getEdges().iterator().next();
        assertEquals(3, only.getPoints().size());
    }
}
