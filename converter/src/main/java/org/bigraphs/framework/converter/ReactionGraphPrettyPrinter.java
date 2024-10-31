package org.bigraphs.framework.converter;

import org.bigraphs.framework.core.reactivesystem.AbstractTransitionSystem;

import java.io.IOException;
import java.io.OutputStream;

/**
 * General interface for all exporters handling transition system objects.
 *
 * @author Dominik Grzelak
 */
public interface ReactionGraphPrettyPrinter<B extends AbstractTransitionSystem<?, ?>> extends PrettyPrinter<B> {

    String toString(B transitionSystem);

    void toOutputStream(B transitionSystem, OutputStream outputStream) throws IOException;
}
