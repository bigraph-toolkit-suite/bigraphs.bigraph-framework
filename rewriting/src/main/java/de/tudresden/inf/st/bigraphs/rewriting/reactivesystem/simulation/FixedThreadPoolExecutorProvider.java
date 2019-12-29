package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation;

import de.tudresden.inf.st.bigraphs.core.providers.ExecutorServicePoolProvider;

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
        return Executors.newFixedThreadPool(10);
    }
}
