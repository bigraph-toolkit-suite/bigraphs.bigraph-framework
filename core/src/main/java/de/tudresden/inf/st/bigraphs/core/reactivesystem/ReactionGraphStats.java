package de.tudresden.inf.st.bigraphs.core.reactivesystem;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionGraph;

/**
 * @author Dominik Grzelak
 */
public class ReactionGraphStats<B extends Bigraph<? extends Signature<?>>> {

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
