package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;

import java.util.function.Predicate;

/**
 * @author Dominik Grzelak
 */
public abstract class TransitionPredicates<B extends Bigraph<? extends Signature<?>>> implements Predicate<B> {

    @Override
    public abstract boolean test(B o);
}
