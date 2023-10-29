package org.bigraphs.framework.core.providers;

import java.util.concurrent.ExecutorService;

/**
 * @author Dominik Grzelak
 */
public interface ExecutorServicePoolProvider {

    ExecutorService provide();
}
