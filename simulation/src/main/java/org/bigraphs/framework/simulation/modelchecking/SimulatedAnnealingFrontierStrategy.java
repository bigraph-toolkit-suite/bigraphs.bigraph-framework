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
package org.bigraphs.framework.simulation.modelchecking;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * @author Dominik Grzelak
 */
public abstract class SimulatedAnnealingFrontierStrategy<B extends Bigraph<? extends Signature<?>>>
        extends ModelCheckingStrategySupport<B> {

    protected double energyEps = 1e-12; // energy tolerance for acceptation (stop criteria)
    private volatile boolean stop = false;

    /**
     * Fairness: every fairnessK expansions, do one FIFO expansion.
     */
    private final int fairnessK;

    /**
     * Cool temperature after {@code n} iterations of an epoch before starting a new epoch.
     */
    private final int epochSize;

    /**
     * Goal prototype states
     */
    private final List<B> goalExemplars;

    // Frontier as "fairness" queue
    private final Deque<B> fifoQueue = new ConcurrentLinkedDeque<>();

    // Caches
    private final Map<B, Double> energyCache = new LinkedHashMap<>();

    // SA state
    private double temperature;
    private int iterationInEpoch = 0;
    private int currentEpoch = 0;
    public int maxEpoch = 100;
    private int fairnessCounter = 0;

    public SimulatedAnnealingFrontierStrategy(
            List<B> goalExemplars,
            double initialTemperature,
            int epochSize,
            int fairnessK
    ) {
        super();
        this.goalExemplars = Objects.requireNonNull(goalExemplars);
        this.temperature = initialTemperature;
        this.epochSize = epochSize;
        this.fairnessK = fairnessK;
    }

    public SimulatedAnnealingFrontierStrategy<B> withModelChecker(BigraphModelChecker<B> modelChecker) {
        this.modelChecker = modelChecker;
        return this;
    }

    @Override
    protected Collection<B> createWorklist() {
        return new ConcurrentLinkedDeque<>();
    }

    /**
     * Called when the framework wants the next state to expand.
     * State is picked from worklist using either fairness FIFO or SA Boltzmann sampling.
     */
    @Override
    protected B removeNext(Collection<B> worklist) {
        if (stop || worklist.isEmpty()) return null;
        if (currentEpoch >= maxEpoch) return null;

        final B chosen;
        if (fairnessK > 0 && fairnessCounter >= fairnessK) {
            chosen = popExistingInOpenFiFo(worklist);
            fairnessCounter = 0;
        } else {
            chosen = boltzmannPick(worklist, temperature);
        }

        if (chosen == null) return null;

        // STOP when best energy reached
        if (energy(chosen) <= energyEps) {
            stop = true;
            return chosen; // or return "null" if: to stop BEFORE expanding it
        }

        worklist.remove(chosen);

        // update counters + cooling
        iterationInEpoch++;
        fairnessCounter++;

        if (epochSize > 0 && iterationInEpoch % epochSize == 0) {
            currentEpoch++;
            temperature = cool_geometric(temperature, currentEpoch);
        }

        return chosen;
    }

    @Override
    protected void addToWorklist(Collection<B> worklist, B bigraph) {
        // Mirror pseudocode: add to OPEN and FIFO only once
        if (worklist.add(bigraph)) {
            fifoQueue.addLast(bigraph);
            // optional early cache
            energy(bigraph);
        }
    }

    /**
     * A bigraph kernel (e.g., Weisfeiler–Lehman) over the encoded graphs.
     * Return a non-negative similarity value.
     */
    protected double graphKernel(B graphA, B graphB) {
        throw new UnsupportedOperationException("wlKernel(graphA, graphB) not implemented");
    }

    /**
     * Normalized bigraph kernel for two bigraphs.
     * <p>
     * The specific kernel specified in {@link #graphKernel(Bigraph, Bigraph)} must be implemented via subclassing.
     *
     * @param s left bigraph
     * @param g right bigraph
     */
    protected double kTilde(B s, B g) {
        double ksg = graphKernel((s), (g));
        double kss = graphKernel((s), (s));
        double kgg = graphKernel((g), (g));
        double denom = Math.sqrt(Math.max(1e-12, kss * kgg));
        return ksg / denom;
    }

    private B popExistingInOpenFiFo(Collection<B> worklist) {
        while (!fifoQueue.isEmpty()) {
            B s = fifoQueue.removeFirst();
            if (worklist.contains(s)) return s;
        }
        // If FIFO is empty/outdated, fall back to SA pick.
        return boltzmannPick(worklist, temperature);
    }

    private B boltzmannPick(Collection<B> worklist, double t) {
        // Guard: if T is tiny, behave greedily (lowest energy).
        if (t <= 1e-12) {
            return worklist.stream()
                    .min(Comparator.comparingDouble(this::energy))
                    .orElse(null);
        }

        // Compute weights
        double sum = 0.0;
        Map<B, Double> w = new HashMap<>(worklist.size());
        for (B s : worklist) {
            double e = energy(s);
            double weight = Math.exp(-e / t);
            w.put(s, weight);
            sum += weight;
        }

        // Sample cumulative
        double u = ThreadLocalRandom.current().nextDouble(0.0, sum);
        double cum = 0.0;
        for (B s : worklist) {
            cum += w.get(s);
            if (cum >= u) return s;
        }

        // Numerical fallback
        return worklist.iterator().next();
    }

    private double energy(B s) {
//        String bfcfOfW = modelChecker.acquireCanonicalForm().bfcs(s);
        Double cached = energyCache.get(s); //bfcfOfW);
        if (cached != null) return cached;

        double best = Double.POSITIVE_INFINITY;

        for (B g : goalExemplars) {
            double sim = clamp01(kTilde(s, g)); // normalized similarity in [0,1]
            double d = Math.sqrt(Math.max(0.0, 2.0 - 2.0 * sim)); // distance from normalized kernel
            if (d < best) best = d;
        }

        energyCache.put(s, best);
        return best;
    }

    private static double clamp01(double x) {
        if (x < 0.0) return 0.0;
        return Math.min(x, 1.0);
    }

    /**
     * Geometric cooling schedule: T <- alpha * T
     */
    protected double cool_geometric(double currentT, int epoch) {
        double alpha = 0.95;
        return Math.max(1e-12, alpha * currentT);
    }

    protected double cool_linear(double t, int epoch) {
        double beta = 0.01;
        return Math.max(0, t - beta);
    }

    protected double cool_log(double t0, int epoch) {
        return t0 / Math.log(epoch + 1.0);
    }
}
