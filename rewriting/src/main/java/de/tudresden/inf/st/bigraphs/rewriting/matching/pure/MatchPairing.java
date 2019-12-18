package de.tudresden.inf.st.bigraphs.rewriting.matching.pure;

import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Dominik Grzelak
 */
public class MatchPairing {
    int rootIndex;
    BigraphEntity redexNode;
    List<BigraphEntity> agentMatches;

    public MatchPairing() {
    }

    public MatchPairing(int rootIndex, BigraphEntity redexNode) {
        this(rootIndex, redexNode, new LinkedList<>());
    }

    public MatchPairing(int rootIndex, BigraphEntity redexNode, List<BigraphEntity> agentMatches) {
        this.rootIndex = rootIndex;
        this.redexNode = redexNode;
        this.agentMatches = agentMatches;
    }

    public int getRootIndex() {
        return rootIndex;
    }

    public BigraphEntity getRedexNode() {
        return redexNode;
    }

    public List<BigraphEntity> getAgentMatches() {
        return agentMatches;
    }
}
