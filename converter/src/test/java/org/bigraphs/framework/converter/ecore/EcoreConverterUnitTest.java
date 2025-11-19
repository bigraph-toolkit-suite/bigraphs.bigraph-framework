/*
 * Copyright (c) 2020-2024 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.converter.ecore;

import java.io.File;
import java.io.FileOutputStream;
import org.bigraphs.framework.converter.PureReactiveSystemStub;
import org.bigraphs.framework.converter.bigmc.BigMcTransformationUnitTest;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.junit.jupiter.api.Test;


/**
 * @author Dominik Grzelak
 */
public class EcoreConverterUnitTest {

    private static final String DUMP_TARGET = "src/test/resources/dump/";

    @Test
    void counting_bigraph() throws Exception {
        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();

        PureBigraph agent_a = BigMcTransformationUnitTest.createAgent_A(3, 4);
        ReactionRule<PureBigraph> rr_1 = BigMcTransformationUnitTest.createReactionRule_1();

        reactiveSystem.setAgent(agent_a);
        reactiveSystem.addReactionRule(rr_1);
        reactiveSystem.addReactionRule(BigMcTransformationUnitTest.createReactionRule_2());
        reactiveSystem.addReactionRule(BigMcTransformationUnitTest.createReactionRule_3());

        EcoreAgentConverter prettyPrinter = new EcoreAgentConverter();
        String s = prettyPrinter.toString(reactiveSystem.getAgent());
        System.out.println(s);

        FileOutputStream fout = new FileOutputStream(new File(DUMP_TARGET + "couting.xmi"));
        prettyPrinter.toOutputStream(reactiveSystem.getAgent(), fout);
        fout.close();

        EcoreConverter converter = new EcoreConverter();
        String s1 = converter.toString(reactiveSystem);
        System.out.println(s1);
        FileOutputStream fout2 = new FileOutputStream(new File(DUMP_TARGET + "couting_stream.xmi"));
        converter.toOutputStream(reactiveSystem, fout2);
        fout2.close();
    }
}
