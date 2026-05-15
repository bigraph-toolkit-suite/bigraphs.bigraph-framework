/*
 * Copyright (c) 2021-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.matching.jlibbig;

import it.uniud.mads.jlibbig.core.std.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class JLibBigUnitTest {

    @Test
    void test_01() {
        Signature signature = new Signature(createControls());
        BigraphBuilder builder = new BigraphBuilder(signature);
        
        Root root = builder.addRoot();
        Node building = builder.addNode("B", root);
        Node roomLeft = builder.addNode("R", building);
        Node roomLeft2 = builder.addNode("R", roomLeft);
        builder.addNode("U", roomLeft2);
        Node roomRight = builder.addNode("R", building);
        builder.addNode("U", roomRight);

        Bigraph agent = builder.makeBigraph();
        System.out.printf("Agent: %s\n", agent.toString());

        Bigraph redex = createRedex(signature);
        Bigraph reactum = createReactum(signature);
        RewritingRule transmitRule = new RewritingRule(redex, reactum, 0, 0, 0);

        Matcher m = new Matcher();
        Iterable<? extends Match> match = m.match(agent, transmitRule.getRedex());
        Iterator<? extends Match> iterator = match.iterator();
        while (iterator.hasNext()) {

            Match next = iterator.next();
            System.out.println(next.toString());
            Bigraph context = next.getContext();
            Bigraph redexId = next.getRedexId();
            Bigraph redexImage = next.getRedexImage();
            Bigraph juxtapose = Bigraph.juxtapose(redexImage, redexId);
            System.out.println("RedexId = ");
            System.out.println(redexId);
            System.out.println("RedexImage = ");
            System.out.println(redexImage);
            System.out.println("Juxtaposed = ");
            System.out.println(juxtapose);
            System.out.println("Context  = ");
            System.out.println(context);
        }

        // Rewrite Result
        int cnt = 0;
        for (Bigraph big : transmitRule.apply(m, agent)) {
            System.out.println("\r\nCounter = " + cnt);
            System.out.println(big.toString());
            cnt++;
        }
        System.out.println("\r\nTotal Rewrite Count = " + cnt);

    }

    //REDEX
    public Bigraph createRedex(Signature signature) {
        BigraphBuilder builder = new BigraphBuilder(signature);
        Root root = builder.addRoot();
        builder.addSite(builder.addNode("R", root));
        builder.addSite(builder.addNode("R", root));
        Bigraph redex = builder.makeBigraph();
        System.out.printf("Redex = %s\n", redex.toString());
        return redex;
    }

    public Bigraph createReactum(Signature signature) {
        BigraphBuilder builder = new BigraphBuilder(signature);
        Root root = builder.addRoot();
        Node roomLeft = builder.addNode("R", root);
        builder.addSite(roomLeft);
        builder.addNode("U", roomLeft);
        builder.addSite(builder.addNode("R", root));
        builder.addSite(builder.addNode("R", root));
        Bigraph redex = builder.makeBigraph();
        System.out.printf("Reactum = %s\n", redex.toString());
        return redex;
    }

    public static Collection<Control> createControls() {
        return Collections.unmodifiableList(Arrays.asList(
                createControl("B", 1),
                createControl("R", 1),
                createControl("U", 0),
                createControl("C", 0),
                createControl("S", 0),
                createControl("D", 0)
        ));
    }

    private static Control createControl(String name, int arity) {
        return new Control(name, true, arity);
    }
}
