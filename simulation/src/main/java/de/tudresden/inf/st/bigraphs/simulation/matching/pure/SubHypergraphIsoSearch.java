package de.tudresden.inf.st.bigraphs.simulation.matching.pure;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dominik Grzelak
 */
public class SubHypergraphIsoSearch {
    private final Bigraph<?> redex;
    private final Bigraph<?> agent;

    IHSFilter ihsFilter;
    Map<BigraphEntity.NodeEntity<?>, List<BigraphEntity.NodeEntity<?>>> candidates = new HashMap<>();
    boolean initialized;

    public SubHypergraphIsoSearch(Bigraph<?> redex, Bigraph<?> agent) {
        this.redex = redex;
        this.agent = agent;
        ihsFilter = new IHSFilter(redex, agent);
        initialized = false;
    }

    public void init() {
        if (!initialized) {
            candidates.clear();
            for (BigraphEntity.NodeEntity<?> u_i : redex.getNodes()) {
                if (redex.getPortCount(u_i) > 0)
                    candidates.putIfAbsent(u_i, new ArrayList<>());
            }
            initialized = true;
        }
    }

    public boolean allCandidatesFound() {
        if (!initialized) return false;

        int linkCountRedex = redex.getAllLinks().size();
        int linkCountAgent = agent.getAllLinks().size();
        if (linkCountRedex == 0 && linkCountAgent == 0) return true;
        if (linkCountRedex > 0 && linkCountAgent > 0) {
            long positveArityNode = redex.getNodes().stream().filter(x -> redex.getPortCount(x) > 0).count();
            if (candidates.size() == positveArityNode) {
                return candidates.values().stream().allMatch(x -> x.size() > 0);
            }
        }
        return linkCountRedex == 0 && linkCountAgent > 0;
    }

    public void search() {
        init();


        for (BigraphEntity.NodeEntity<?> each : redex.getNodes()) {
            search(each);
        }
    }

    public void search(BigraphEntity.NodeEntity<?> u_s) {
        init();

        for (BigraphEntity.NodeEntity<?> v_s : agent.getNodes()) {
            if (agent.getPortCount(v_s) > 0 && redex.getPortCount(u_s) > 0) {
                if (ihsFilter.condition1(u_s, v_s) &&
                        ihsFilter.condition2(u_s, v_s) &&
                        ihsFilter.condition3(u_s, v_s) &&
                        ihsFilter.condition4(u_s, v_s)) {
                    candidates.get(u_s).add(v_s);
                }
            }
        }
    }

    public Map<BigraphEntity.NodeEntity<?>, List<BigraphEntity.NodeEntity<?>>> getCandidates() {
        return candidates;
    }
}
