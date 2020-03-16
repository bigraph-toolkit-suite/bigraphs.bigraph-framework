package de.tudresden.inf.st.bigraphs.simulation;

import de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
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
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts.and(transitionOpts()
                .setMaximumTransitions(4)
                .setMaximumTime(60, TimeUnit.SECONDS)
                .create()
        ).and(ModelCheckingOptions.exportOpts()
                .setOutputStatesFolder(new File(""))
                .setReactionGraphFile(new File(""))
                .create()
        );

        // overwrite old settings
        opts.and(transitionOpts()
                .setMaximumTransitions(5)
                .setMaximumTime(30, TimeUnit.MILLISECONDS)
                .create());

        ModelCheckingOptions.TransitionOptions opts1 = opts.get(ModelCheckingOptions.Options.TRANSITION);
        assertEquals(opts1.getMaximumTransitions(), 5);
        assertEquals(opts1.getMaximumTimeUnit(), TimeUnit.MILLISECONDS);
        assertEquals(opts1.getMaximumTime(), 30);
    }
}
