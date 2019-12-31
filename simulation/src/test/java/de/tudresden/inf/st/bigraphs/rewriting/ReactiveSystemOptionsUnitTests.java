package de.tudresden.inf.st.bigraphs.rewriting;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.impl.PureReactiveSystem;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.BigraphModelChecker;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.PureBigraphModelChecker;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.exceptions.BigraphSimulationException;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.predicates.SubBigraphMatchPredicate;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystemOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Option classes unit test used within a simulation.
 *
 * @author Dominik Grzelak
 */
public class ReactiveSystemOptionsUnitTests {

//    public static void main(String[] args) throws Exception {
        //https://www.baeldung.com/java-microbenchmark-harness
//        org.openjdk.jmh.Main.main(args);
//    }

//    @Benchmark
//    @Fork(value = 1, warmups = 2)
//    @BenchmarkMode(Mode.Throughput)
//    @BenchmarkMode(Mode.AverageTime)
//    public void init() {
//        System.out.println("init");
//    }

    @Test
    void reactionsystem_options_test() {
        ReactiveSystemOptions opts = ReactiveSystemOptions.create();
        opts.and(transitionOpts()
                .setMaximumTransitions(4)
                .setMaximumTime(60, TimeUnit.SECONDS)
                .create()
        ).and(ReactiveSystemOptions.exportOpts()
                .setOutputStatesFolder(new File(""))
                .setTraceFile(new File(""))
                .create()
        );

        // overwrite old settings
        opts.and(transitionOpts()
                .setMaximumTransitions(5)
                .setMaximumTime(30, TimeUnit.MILLISECONDS)
                .create());

        ReactiveSystemOptions.TransitionOptions opts1 = opts.get(ReactiveSystemOptions.Options.TRANSITION);
        assertEquals(opts1.getMaximumTransitions(), 5);
        assertEquals(opts1.getMaximumTimeUnit(), TimeUnit.MILLISECONDS);
        assertEquals(opts1.getMaximumTime(), 30);
    }
}
