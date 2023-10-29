package org.bigraphs.framework.core.exceptions;

import java.util.Arrays;

/**
 * In the definition of a reaction, the site of the context must be active, where the part of the redex is rewritten.
 * If this is not the case, this exception is thrown.
 *
 * @author Dominik Grzelak
 */
public class ContextIsNotActive extends Exception {

    public ContextIsNotActive(int siteIx) {
        super("Context is not active at site with index=" + siteIx);
    }

    public ContextIsNotActive(int[] siteIndices) {
        super("Context is not active at site with index=" + Arrays.toString(siteIndices));
    }
}
