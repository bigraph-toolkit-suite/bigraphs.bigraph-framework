/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation;

import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.concurrent.TimeUnit;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.junit.jupiter.api.*;

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
