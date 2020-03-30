package de.tudresden.inf.st.bigraphs.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * A distributed random number generator.
 *
 * @see <a href="https://stackoverflow.com/a/20329901">https://stackoverflow.com/a/20329901</a>
 */
public class DistributedRandomNumberGenerator {

    private Map<Integer, Float> distribution;
    private double distSum;

    public DistributedRandomNumberGenerator() {
        distribution = new HashMap<>();
    }

    public void addNumber(int value, float distribution) {
        if (this.distribution.get(value) != null) {
            distSum -= this.distribution.get(value);
        }
        this.distribution.put(value, distribution);
        distSum += distribution;
    }

    public int getDistributedRandomNumber() {
        double rand = Math.random();
        double ratio = 1.0f / distSum;
        double tempDist = 0;
        for (Integer i : distribution.keySet()) {
            tempDist += distribution.get(i);
            if (rand / ratio <= tempDist) {
                return i;
            }
        }
        return 0;
    }

}