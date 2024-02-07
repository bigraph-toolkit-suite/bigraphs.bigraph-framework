package org.bigraphs.framework.simulation.modelchecking;

import org.bigraphs.framework.core.providers.ExecutorServicePoolProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Default executor provider for the model checking task submission. It creates a fixed thread pool of size 10.
 *
 * @author Dominik Grzelak
 */
public class FixedThreadPoolExecutorProvider implements ExecutorServicePoolProvider {

    @Override
    public ExecutorService provide() {
        final int cpus = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(cpus);
    }
}
