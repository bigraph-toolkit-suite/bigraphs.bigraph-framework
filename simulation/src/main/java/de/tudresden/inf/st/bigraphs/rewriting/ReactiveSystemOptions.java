package de.tudresden.inf.st.bigraphs.rewriting;

//TODO: add tactics/order/priorities for RR execution (here?)

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Dominik Grzelak
 */
public class ReactiveSystemOptions {
    private final static int DEFAULT_MAX_TRANSITIONS = Integer.MAX_VALUE;
    //    private int maximumTransitions;
    private ExportOptions exportOpts;
    private TransitionOptions transitionOpts;
    private Map<Options, Opts> optsMap = new ConcurrentHashMap<>();
    private boolean measureTime = false;

    public enum Options {
        TRANSITION(TransitionOptions.class), EXPORT(ExportOptions.class);

        private Class<?> optionClassType;

        <T extends Opts> Options(Class<T> c) {
            this.optionClassType = c;
        }

        <T extends Opts> Class<T> getOptionClassType() {
            return (Class<T>) optionClassType;
        }
    }

    private ReactiveSystemOptions() {
    }

    public static ExportOptions.Builder exportOpts() {
        return new ExportOptions.Builder();
    }

    public static TransitionOptions.Builder transitionOpts() {
        return new TransitionOptions.Builder();
    }

    public static ReactiveSystemOptions create() {
        return new ReactiveSystemOptions();
    }

    public ReactiveSystemOptions and(Opts opts) {
        optsMap.put(opts.getType(), opts);
        return this;
    }

    public boolean isMeasureTime() {
        return measureTime;
    }

    /**
     * Instruct the simulation to measure the time for individual steps of the current used simulation algorithm.
     * Defaults to {@code false}.
     * Useful for debugging purposes.
     *
     * @param measureTime flag to enable or disable time measurement
     * @return the current options instance
     */
    public ReactiveSystemOptions doMeasureTime(boolean measureTime) {
        this.measureTime = measureTime;
        return this;
    }

    public <T extends Opts> T get(Options kind) {
        if (Objects.isNull(optsMap.get(kind))) return null;
        return (T) kind.getOptionClassType().cast(optsMap.get(kind));
    }

    public interface Opts {
        Options getType();
    }

    /**
     * Class that represents simulation-specific options.
     *
     * @author Dominik Grzelak
     */
    public static final class TransitionOptions implements Opts {
        private int maximumTransitions;
        private long maximumTime;
        private TimeUnit maximumTimeUnit;
        private boolean allowReducibleClasses;

        TransitionOptions(int maximumTransitions, long maximumTime, TimeUnit maximumTimeUnit, boolean allowReducibleClasses) {
            this.maximumTransitions = maximumTransitions;
            this.maximumTime = maximumTime;
            this.maximumTimeUnit = maximumTimeUnit;
            this.allowReducibleClasses = allowReducibleClasses;
        }

        public int getMaximumTransitions() {
            return maximumTransitions;
        }

        public TimeUnit getMaximumTimeUnit() {
            return maximumTimeUnit;
        }

        public long getMaximumTime() {
            return maximumTime;
        }

        /**
         * Flag that denotes whether to exploit the symmetries of the reaction graph or not.
         * <p>
         * Default value is {@code false}.
         *
         * @return {@code true} reaction graph considers symmetries
         */
        public boolean allowReducibleClasses() {
            return allowReducibleClasses;
        }

        @Override
        public Options getType() {
            return Options.TRANSITION;
        }

        /**
         * Default values: <br>
         * <ul>
         *     <li>allow reducible classes: {@code false}</li>
         * </ul>
         */
        public static class Builder {
            private int maximumTransitions;
            private TimeUnit maximumTimeUnit = TimeUnit.SECONDS;
            private long maximumTime = 30;
            private boolean reduceStates = false;

            public Builder setMaximumTransitions(int maximumTransitions) {
                this.maximumTransitions = maximumTransitions;
                return this;
            }

            public Builder setMaximumTime(long maximumTime, TimeUnit maximumTimeUnit) {
                this.maximumTime = maximumTime;
                this.maximumTimeUnit = maximumTimeUnit;
                return this;
            }

            public Builder setMaximumTime(long maximumTime) {
                this.maximumTime = maximumTime;
                return this;
            }

            /**
             * Flag to set whether to allow reducible classes for the reaction graph.
             * @param reduceStates if {@code true}, symmetries are exploited
             * @return {@code true} reaction graph considers symmetries
             */
            public Builder allowReducibleClasses(boolean reduceStates) {
                this.reduceStates = reduceStates;
                return this;
            }

            public ReactiveSystemOptions.TransitionOptions create() {
                return new ReactiveSystemOptions.TransitionOptions(maximumTransitions, maximumTime, maximumTimeUnit, reduceStates);
            }
        }
    }

    /**
     * This class represents export-specific options regarding the generated artifacts when synthesizing the transition
     * system.
     *
     * @author Dominik Grzelak
     */
    public static final class ExportOptions implements Opts {
        private final File outputStatesFolder;
        private final File traceFile;

        ExportOptions(File outputStatesFolder, File traceFile) {
            this.outputStatesFolder = outputStatesFolder;
            this.traceFile = traceFile;
        }

        public File getOutputStatesFolder() {
            return outputStatesFolder;
        }

        public boolean hasOutputStatesFolder() {
            return Objects.nonNull(outputStatesFolder);
        }

        /**
         * The file to store for the trace of the transition graph
         *
         * @return
         */
        public File getTraceFile() {
            return traceFile;
        }

        @Override
        public Options getType() {
            return Options.EXPORT;
        }

        public static class Builder {
            private File outputStatesFolder;
            private File traceFile;

            public Builder setOutputStatesFolder(File outputStatesFolder) {
                this.outputStatesFolder = outputStatesFolder;
                return this;
            }

            public Builder setTraceFile(File traceFile) {
                this.traceFile = traceFile;
                return this;
            }

            public ExportOptions create() {
                return new ExportOptions(outputStatesFolder, traceFile);
            }
        }
    }
}