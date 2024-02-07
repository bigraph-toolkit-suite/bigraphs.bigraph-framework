package org.bigraphs.framework.simulation.examples.bigrid;

public abstract class ConvergenceCriteria {

    public static ConvergenceCriteria.RelativeStateSpaceComplexity create(double p) {
        return new RelativeStateSpaceComplexity(p);
    }

    public static ConvergenceCriteria.Iterations create(long count) {
        return new Iterations(count);
    }

    public static class Iterations {
        private long count = 100;

        private Iterations(long count) {
            this.count = count;
        }

        public long getCount() {
            return count;
        }
    }

    public static class RelativeStateSpaceComplexity {
        private double percentage = 0.5;

        private RelativeStateSpaceComplexity(double percentage) {
            this.percentage = percentage;
        }

        public double getPercentage() {
            return percentage;
        }
    }

}
