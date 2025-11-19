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
package org.bigraphs.framework.simulation.equivalence.adapter;

import java.io.ByteArrayInputStream;
import org.bigraphs.framework.core.reactivesystem.AbstractTransitionSystem;

/**
 * An adapter for {@link org.bigraphs.framework.core.reactivesystem.AbstractTransitionSystem} objects in
 * Bigraph Framework for the external Java library "bighuggies:bisimulation"
 * (shaded in the Simulation Module dependency).
 * <p>
 * "bighuggies:bisimulation" computes bisimilarity for two LTSs.
 * This functionality is made available to objects of type
 * {@link org.bigraphs.framework.core.reactivesystem.AbstractTransitionSystem}
 * in Bigraph Framework via this adapter class.
 * <p>
 * An AST is converted to a String representation of a Process that is parsed by "bighuggies" to create a Process.
 * So this is less efficient than the {@link BighuggiesBisimulationProcessAdapter}.
 *
 * @author Dominik Grzelak
 * @see "https://github.com/bighuggies/bisimulation"
 */
public class BighuggiesBisimulationStringAdapter<AST extends AbstractTransitionSystem<?, ?>> {
    AST system;

    public BighuggiesBisimulationStringAdapter(AST transitionSystem) {
        this.system = transitionSystem;
    }

    /**
     * "Applies" the adapter. The AST is translated to a String first, which represents the process specification as
     * used in bighuggies.
     * <p>
     * An input stream is delivered containing the string.
     *
     * @return process specification as input stream
     */
    public ByteArrayInputStream apply() {
        throw new RuntimeException("Not implemented yet!");
//        StringBuilder sb = new StringBuilder("1,a:2\n" +
//                "1,b:3\n" +
//                "2,a:3\n" +
//                "!");
//        //
//        return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
