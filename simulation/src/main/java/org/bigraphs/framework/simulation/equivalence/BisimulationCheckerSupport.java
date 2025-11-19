/*
 * Copyright (c) 2024 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.equivalence;

import bighuggies.bisimulation.BisimulationChecker;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.bigraphs.framework.simulation.equivalence.adapter.BighuggiesBisimulationProcessAdapter;

/**
 * This implementation provides a basic extensible (template) method for computing bisimilarity of LTSs.
 * The type of the transition system is those whose transition relations are reaction rules, and states are bigraphs.
 * <p>
 * This class contains the {@link #checkBisimulation_Bighuggies(ReactionGraph, ReactionGraph)}
 * method that implements the bisimulation check using a breadth-first search approach.
 * <p>
 * Depending on the complexity of the transition systems, this method needs to be adapted with more sophisticated logic.
 *
 * @param <B>   the type of the state of {@code <AST>}
 * @param <AST> the type of the transition system
 * @author Dominik Grzelak
 * @see "https://github.com/bighuggies/bisimulation"
 */
public class BisimulationCheckerSupport<B extends Bigraph<? extends Signature<?>>, AST extends ReactionGraph<B>> {

    /**
     * Bisimilarity checking using the KANELLAKIS_SMOLKA algorithm from the external "bighuggies:bisimulation"
     * Java library.
     * <p>
     * Input: Two states, one from each transition system.
     * Output: Boolean value indicating whether the states are bisimilar.
     *
     * @param system1
     * @param system2
     * @return {@code true}, if both systems are bisimilar; otherwise {@code false}
     */
    public boolean checkBisimulation_Bighuggies(AST system1, AST system2) {
        BisimulationChecker bisim = new BisimulationChecker();
        bisim.setProcessP(new BighuggiesBisimulationProcessAdapter<>("Process P", "p", system1));
        bisim.setProcessQ(new BighuggiesBisimulationProcessAdapter<>("Process Q", "q", system2));
        bisim.performBisimulation();
        return bisim.areBisimilar();
    }

}
