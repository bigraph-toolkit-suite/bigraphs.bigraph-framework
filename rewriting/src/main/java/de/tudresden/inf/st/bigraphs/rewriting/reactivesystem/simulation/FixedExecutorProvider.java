package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation;

import de.tudresden.inf.st.bigraphs.core.providers.ExecutorServicePoolProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Dominik Grzelak
 */
public class FixedExecutorProvider implements ExecutorServicePoolProvider {

    @Override
    public ExecutorService provide() {
        return Executors.newFixedThreadPool(10);
    }
}
