/*
 * Copyright (c) 2019-2024 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.core.reactivesystem;

import java.io.Serializable;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * @author Dominik Grzelak
 */
public class ReactionGraphStats<B extends Bigraph<? extends Signature<?>>> implements Serializable {

    ReactionGraph<B> reactionGraph;
    int numberOfTransitions = -1;
    int numberOfOccurrences = -1;
    int numberOfStates = -1;

    public ReactionGraphStats(ReactionGraph<B> reactionGraph) {
        this.reactionGraph = reactionGraph;
        this.computeStatistics();
    }

    void computeStatistics() {
        numberOfStates = reactionGraph.getGraph().vertexSet().size();
        numberOfTransitions = reactionGraph.getGraph().edgeSet().size();
    }

    public int getTransitionCount() {
        return numberOfTransitions;
    }

    public int getOccurrenceCount() {
        return numberOfOccurrences;
    }

    public int getStateCount() {
        return numberOfStates;
    }

    public void setOccurrenceCount(int value) {
        this.numberOfOccurrences = value;
    }
}
