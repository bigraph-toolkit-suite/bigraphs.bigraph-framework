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
 * This class is an abstraction of a model checking strategy
 * based on the Simulated Annealing algorithm.
 * <p>
 * This strategy is implemented using a cooling schedule, which helps in controlling the temperature
 * of the annealing process. The temperature influences the probability of moving to a state of lower
 * quality, allowing the algorithm to potentially escape local optima in the search space.
 * <p>
 * When computing state (bigraph) similarity via a kernel, the normalized kernel will be computed by this strategy afterward ({@link #kTilde(Bigraph, Bigraph)}.
 * <p>
 * The class is designed to be extended and requires subclasses to provide implementations for some
 * specific methods such as:
 * <ul>
 *     <li>{@link #graphKernel(Bigraph, Bigraph)}: Calculation of a graph kernel for computing similarity between two bigraphs</li>
 *     <li>{@link CoolingSchedules}: Default implementations are provided such as linear, geometric and logarithmic cooling.</li>
 * </ul>
 *
 * @param <B> the type of bigraph elements processed by this strategy
 */
public abstract class SimulatedAnnealingFrontierStrategy<B extends Bigraph<? extends Signature<?>>>
        extends ModelCheckingStrategySupport<B> {

    /**
     * Functional interface representing a cooling schedule used in simulated annealing processes.
     * It provides a method to calculate the next temperature based on the current temperature
     * and the current epoch or iteration.
     */
    @FunctionalInterface
    public interface CoolingSchedule {

        /**
         * @param currentT current temperature
         * @param epoch    current epoch
         */
        double cool(double currentT, int epoch);
    }

    /**
     * The class includes three static methods that return implementations of the CoolingSchedule
     * interface.
     * These strategies define how the temperature is decreased over time, which influences the
     * algorithm's ability to explore the solution space.
     */
    static final class CoolingSchedules {

        /**
         * Geometric cooling schedule: T <- alpha * T
         */
        static CoolingSchedule geometric(double alpha) {
            return (t, e) -> Math.max(1e-12, alpha * t);
        }

        /**
         * Linear cooling schedule: T <- T - beta
         *
         * @param beta e.g., beta = 0.01
         */
        static CoolingSchedule linear(double beta) {
            return (t, e) -> Math.max(0.0, t - beta);
        }

        /**
         * Logarithmic cooling schedule: T <- T / log(epoch + 1)
         */
        static CoolingSchedule log() {
            return (t, e) -> t / Math.log(e + 1.0);
        }
    }


    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private CoolingSchedule cooling = CoolingSchedules.log();

    protected double energyEps = 1e-12; // energy tolerance for acceptation (stop criteria)
    protected volatile boolean internalStop = false;

    /**
     * Fairness: every fairnessK expansions, do one FIFO expansion.
     */
    protected final int fairnessK;

    /**
     * Cool temperature after {@code n} iterations of an epoch before starting a new epoch.
     */
    protected final int epochSize;

    /**
     * Goal prototype states
     */
    protected final List<B> goalExemplars;

    /**
     * Frontier as fairness queue
     */
    protected final Deque<B> fifoQueue = new ConcurrentLinkedDeque<>();

    /**
     * Caches
     */
    protected final Map<B, Double> energyCache = new LinkedHashMap<>();

    /**
     * State variables of the SA algorithms
     */
    protected double temperature;
    protected int iterationInEpoch = 0;
    protected int currentEpoch = 0;
    public int maxEpoch = 100;
    protected int fairnessCounter = 0;

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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


    public SimulatedAnnealingFrontierStrategy<B> withCooling(CoolingSchedule c) {
        this.cooling = Objects.requireNonNull(c);
        return this;
    }

    public SimulatedAnnealingFrontierStrategy<B> withModelChecker(BigraphModelChecker<B> modelChecker) {
        this.modelChecker = modelChecker;
        return this;
    }

    @Override
    public Collection<B> createWorklist() {
        return new ConcurrentLinkedDeque<>();
    }

    /**
     * Called when the framework wants the next state to expand.
     * State is picked from worklist using either fairness FIFO or SA Boltzmann sampling.
     */
    @Override
    public B removeNext(Collection<B> worklist) {
        if (internalStop || worklist.isEmpty()) {
            isRunning = false;
            return null;
        }
        if (currentEpoch >= maxEpoch) return null;

        B chosen;
        if (fairnessK > 0 && fairnessCounter >= fairnessK) {
            chosen = popExistingInOpenFiFo(worklist);
            fairnessCounter = 0;
        } else {
            chosen = boltzmannPick(worklist, temperature);
        }

        // Repeat
        if (chosen == null) {
            return removeNext(worklist);
        }

        // Stop when best energy reached
        double e = energy(chosen);
        if (e <= energyEps) {
            stopProcedure(chosen);
            return chosen; // or return "null" to stop BEFORE expanding it
        }

        worklist.remove(chosen);

        // update counters + cooling
        iterationInEpoch++;
        fairnessCounter++;

        if (epochSize > 0 && iterationInEpoch % epochSize == 0) {
            currentEpoch++;
            temperature = cooling.cool(temperature, currentEpoch);
//            System.out.println("temperature = " + temperature);
        }

        return chosen;
    }

    protected void stopProcedure(B chosen) {
        internalStop = true;
    }

    @Override
    public void addToWorklist(Collection<B> worklist, B bigraph) {
        if (worklist.add(bigraph)) {
            fifoQueue.addLast(bigraph);
            energy(bigraph);
        }
    }

    /**
     * A bigraph kernel (e.g., Weisfeiler–Lehman) over the encoded graphs.
     * Return a non-negative similarity value.
     */
    protected double graphKernel(B graphA, B graphB) {
        throw new UnsupportedOperationException("Method graphKernel(graphA, graphB) not implemented! Override this method to provide the kernel.");
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

    protected B popExistingInOpenFiFo(Collection<B> worklist) {
        while (!fifoQueue.isEmpty()) {
            B s = fifoQueue.removeFirst();
            if (worklist.contains(s)) return s;
        }
        // If FIFO is empty, fall back to SA pick.
        return boltzmannPick(worklist, temperature);
    }

    protected B boltzmannPick(Collection<B> worklist, double t) {
        // Guard: if T is tiny, behave greedily (lowest energy)
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
        double u = 0;
        try {
            u = ThreadLocalRandom.current().nextDouble(0.0, sum);
        } catch (IllegalArgumentException ignored) {
        }
        double cum = 0.0;
        for (B s : worklist) {
            cum += w.get(s);
            if (cum >= u) return s;
        }

        // Numerical fallback
        return worklist.iterator().next();
    }

    protected double energy(B s) {
        // String bfcfOfW = modelChecker.acquireCanonicalForm().bfcs(s);
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
}
