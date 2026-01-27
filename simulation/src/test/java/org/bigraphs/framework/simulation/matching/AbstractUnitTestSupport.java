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
package org.bigraphs.framework.simulation.matching;

import com.google.common.base.Stopwatch;
import it.uniud.mads.jlibbig.core.std.Match;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphDecoder;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.simulation.matching.pure.PureBigraphMatch;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.Disabled;

/**
 * @author Dominik Grzelak
 */
@Disabled
public interface AbstractUnitTestSupport {

    default void createGraphvizOutput(Bigraph<?> agent, BigraphMatch<?> next0, String path, int ix) throws IncompatibleSignatureException, IncompatibleInterfaceException, IOException {
        PureBigraphMatch next = (PureBigraphMatch) next0;
        Match result = next.getJLibMatchResult();
        JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
        PureBigraph context = decoder.decode(result.getContext());
        PureBigraph redex = next.getRedex();
        Bigraph redexImage = decoder.decode(result.getRedexImage());
        PureBigraph redexId = decoder.decode(result.getRedexId());

        //This takes a lot if time!
        System.out.println("Creating PNGs ...");
        Stopwatch timer = Stopwatch.createStarted();
        try {
            if (context != null)
                BigraphGraphvizExporter.toPNG(context,
                        true,
                        new File(path + "context-" + ix + ".png")
                );
            if (agent != null)
                BigraphGraphvizExporter.toPNG(agent,
                        true,
                        new File(path + "agent-" + ix + ".png")
                );
            if (redex != null)
                BigraphGraphvizExporter.toPNG(redex,
                        true,
                        new File(path + "redex-" + ix + ".png")
                );
            if (redexImage != null)
                BigraphGraphvizExporter.toPNG(redexImage,
                        true,
                        new File(path + "redexImage-" + ix + ".png")
                );
            if (redexId != null)
                BigraphGraphvizExporter.toPNG(redexId,
                        true,
                        new File(path + "redexId-" + ix + ".png")
                );
            long elapsed = timer.stop().elapsed(TimeUnit.MILLISECONDS);
            System.out.println("Creating PNGs took (ms) " + elapsed);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
