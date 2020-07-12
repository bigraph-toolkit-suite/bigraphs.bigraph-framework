package de.tudresden.inf.st.bigraphs.simulation.modelchecking;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * This class represents the available options for the model checker {@link BigraphModelChecker}.
 *
 * @author Dominik Grzelak
 */
@Configuration
@ConfigurationProperties(prefix = "model-checking", ignoreInvalidFields = true)
public class ModelCheckingOptions {
    private final static int DEFAULT_MAX_TRANSITIONS = Integer.MAX_VALUE;

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

    ModelCheckingOptions() {
    }

    public static ExportOptions.Builder exportOpts() {
        return new ExportOptions.Builder();
    }

    public static TransitionOptions.Builder transitionOpts() {
        return new TransitionOptions.Builder();
    }

    public static ModelCheckingOptions create() {
        return new ModelCheckingOptions();
    }

    public ModelCheckingOptions and(Opts opts) {
        optsMap.put(opts.getType(), opts);
        return this;
    }

    public void setMeasureTime(boolean measureTime) {
        this.measureTime = measureTime;
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
    public ModelCheckingOptions doMeasureTime(boolean measureTime) {
        this.measureTime = measureTime;
        return this;
    }

    public <T extends Opts> T get(Options kind) {
        if (optsMap.size() == 0) {
            if (transitionOpts != null) {
                and(transitionOpts);
            }
            if (exportOpts != null) {
                and(exportOpts);
            }
        }
        if (Objects.isNull(optsMap.get(kind))) return null;
        return (T) kind.getOptionClassType().cast(optsMap.get(kind));
    }

    void setExportOptions(ExportOptions exportOpts) {
        this.exportOpts = exportOpts;
    }

    void setTransitionOptions(TransitionOptions transitionOpts) {
        this.transitionOpts = transitionOpts;
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
        //        @Min(1)
        private int maximumTransitions = 100;
        private long maximumTime = 30;
        private TimeUnit maximumTimeUnit = TimeUnit.SECONDS;
        private boolean allowReducibleClasses;

        TransitionOptions() {
        }

        TransitionOptions(int maximumTransitions, long maximumTime, TimeUnit maximumTimeUnit, boolean allowReducibleClasses) {
            this.maximumTransitions = maximumTransitions;
            this.maximumTime = maximumTime;
            this.maximumTimeUnit = maximumTimeUnit;
            this.allowReducibleClasses = allowReducibleClasses;
        }

        void setMaximumTransitions(int maximumTransitions) {
            this.maximumTransitions = maximumTransitions;
        }

        void setMaximumTime(long maximumTime) {
            this.maximumTime = maximumTime;
        }

        void setMaximumTimeUnit(TimeUnit maximumTimeUnit) {
            this.maximumTimeUnit = maximumTimeUnit;
        }

        void setAllowReducibleClasses(boolean allowReducibleClasses) {
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

        public Builder toBuilder() {
            return transitionOpts().allowReducibleClasses(this.allowReducibleClasses)
                    .setMaximumTransitions(this.maximumTransitions)
                    .setMaximumTime(this.maximumTime, this.maximumTimeUnit);
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
             *
             * @param reduceStates if {@code true}, symmetries are exploited
             * @return {@code true} reaction graph considers symmetries
             */
            public Builder allowReducibleClasses(boolean reduceStates) {
                this.reduceStates = reduceStates;
                return this;
            }

            public ModelCheckingOptions.TransitionOptions create() {
                return new ModelCheckingOptions.TransitionOptions(maximumTransitions, maximumTime, maximumTimeUnit, reduceStates);
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
        private File outputStatesFolder;
        private File reactionGraphFile;
        private File rewriteResultFolder;
        private Boolean printCanonicalStateLabel;

        ExportOptions() {
            this(new File("./states/"), new File("./transition_graph.png"), new File("./results/"), false);
        }

        ExportOptions(File outputStatesFolder, File reactionGraphFile, File rewriteResultFolder, Boolean printCanonicalStateLabel) {
            this.outputStatesFolder = outputStatesFolder;
            this.reactionGraphFile = reactionGraphFile;
            this.rewriteResultFolder = rewriteResultFolder;
            this.printCanonicalStateLabel = printCanonicalStateLabel;
        }

        public File getOutputStatesFolder() {
            return outputStatesFolder;
        }

        public boolean hasOutputStatesFolder() {
            return Objects.nonNull(outputStatesFolder);
        }

        public boolean hasRewriteResultFolder() {
            return Objects.nonNull(rewriteResultFolder);
        }

        public File getRewriteResultFolder() {
            return rewriteResultFolder;
        }

        void setOutputStatesFolder(File outputStatesFolder) {
            this.outputStatesFolder = outputStatesFolder;
        }

        void setReactionGraphFile(File reactionGraphFile) {
            this.reactionGraphFile = reactionGraphFile;
        }

        void setRewriteResultFolder(File rewriteResultFolder) {
            this.rewriteResultFolder = rewriteResultFolder;
        }

        void setPrintCanonicalStateLabel(Boolean printCanonicalStateLabel) {
            this.printCanonicalStateLabel = printCanonicalStateLabel;
        }

        /**
         * Flag that can be used to determine whether the labels of the states in the reaction graph should contain
         * the canonical form of a bigraph or not, meaning, only a constant identifier is printed suffixed with an
         * incremented number.
         * <p>
         * This only affects the exported reaction graph and serves visual purposes.
         *
         * @return {@code true}, if the state labels of the reaction graph should contain the canonical form of a bigraph
         */
        public Boolean getPrintCanonicalStateLabel() {
            return printCanonicalStateLabel;
        }

        /**
         * The file to store the reaction graph (i.e., transition system)
         *
         * @return filename of the reaction graph to store
         */
        public File getReactionGraphFile() {
            return reactionGraphFile;
        }

        /**
         * Checks if the filename for the reaction graph export was set.
         *
         * @return {@code true}, if the filename for the reaction graph export was set
         */
        public boolean hasReactionGraphFile() {
            return Objects.nonNull(reactionGraphFile);
        }

        @Override
        public Options getType() {
            return Options.EXPORT;
        }

        public Builder toBuilder() {
            return exportOpts().setOutputStatesFolder(this.outputStatesFolder)
                    .setPrintCanonicalStateLabel(this.printCanonicalStateLabel)
                    .setReactionGraphFile(this.reactionGraphFile)
                    .setRewriteResultFolder(this.rewriteResultFolder);
        }

        public static class Builder {
            private File outputStatesFolder = null;
            private File reactionGraphFile = null;
            private File rewriteResultFolder = null;
            private Boolean printCanonicalStateLabel = false;

            public Builder setOutputStatesFolder(File outputStatesFolder) {
                this.outputStatesFolder = outputStatesFolder;
                return this;
            }

            public Builder setReactionGraphFile(File reactionGraphFile) {
                this.reactionGraphFile = reactionGraphFile;
                return this;
            }

            public Builder setRewriteResultFolder(File rewriteResultFolder) {
                this.rewriteResultFolder = rewriteResultFolder;
                return this;
            }

            /**
             * Flag that can be used to determine whether the labels of the states in the reaction graph should contain
             * the canonical form of a bigraph or not, meaning, only a constant identifier is printed suffixed with an
             * incremented number.
             * <p>
             * This only affects the exported reaction graph and serves visual purposes.
             * <p>
             * Default is {@code true}.
             *
             * @return {@code true}, if the state labels of the reaction graph should contain the canonical form of a bigraph
             */
            public Builder setPrintCanonicalStateLabel(boolean flag) {
                this.printCanonicalStateLabel = flag;
                return this;
            }

            public ExportOptions create() {
                return new ExportOptions(outputStatesFolder, reactionGraphFile, rewriteResultFolder, printCanonicalStateLabel);
            }
        }
    }
}