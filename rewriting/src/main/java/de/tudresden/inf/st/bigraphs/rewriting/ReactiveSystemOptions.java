package de.tudresden.inf.st.bigraphs.rewriting;

//TODO: add tactics/order/priorities for RR execution (here?)

import java.io.File;
import java.util.Map;
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

    public ReactiveSystemOptions setMeasureTime(boolean measureTime) {
        this.measureTime = measureTime;
        return this;
    }

    public <T extends Opts> T get(Options kind) {
//        optsMap.get(kind.getOptionClassType())
        return (T) kind.getOptionClassType().cast(optsMap.get(kind));
//        return (T) optsMap.get(kind.getOptionClassType());
    }

    public interface Opts {
        Options getType();
    }

    public static final class TransitionOptions implements Opts {
        private int maximumTransitions;
        private TimeUnit maximumTime;

        TransitionOptions(int maximumTransitions, TimeUnit maximumTime) {
            this.maximumTransitions = maximumTransitions;
            this.maximumTime = maximumTime;
        }

        public int getMaximumTransitions() {
            return maximumTransitions;
        }

        public TimeUnit getMaximumTime() {
            return maximumTime;
        }

        @Override
        public Options getType() {
            return Options.TRANSITION;
        }

        public static class Builder {
            private int maximumTransitions;
            private TimeUnit maximumTime;

            public Builder setMaximumTransitions(int maximumTransitions) {
                this.maximumTransitions = maximumTransitions;
                return this;
            }

            public Builder setMaximumTime(TimeUnit maximumTime) {
                this.maximumTime = maximumTime;
                return this;
            }

            public ReactiveSystemOptions.TransitionOptions create() {
                return new ReactiveSystemOptions.TransitionOptions(maximumTransitions, maximumTime);
            }
        }
    }

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

        /**
         * The file to store for the trace of the transition graph
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