package org.bigraphs.framework.simulation;


import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;


class ReactiveSystemOptionTests {

    @Test
    @DisplayName("Create Reactive System options")
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