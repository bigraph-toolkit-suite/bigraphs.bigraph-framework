package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;

/**
 * Common interface for the composition of bigraphs.
 * <p>
 * Current implementations are:
 * <ul>
 * <li>{@link de.tudresden.inf.st.bigraphs.core.impl.DefaultBigraphComposite} for pure bigraphs</li>
 * </ul>
 *
 * @param <S> type of the signature
 * @author Dominik Grzelak
 */
public interface BigraphComposite<S extends Signature> {

    /**
     * Return the outer bigraph of a composition, that is, the left part of the operator.
     *
     * @return the outer bigraph of a composition
     */
    Bigraph<S> getOuterBigraph();

    /**
     * Composes two bigraphs where a new immutable bigraph is created.
     *
     * @param inner the right part of the composition operator, i.e., the inner bigraph
     * @return a new bigraph composed of both arguments
     */
    BigraphComposite<S> compose(Bigraph<S> inner) throws IncompatibleSignatureException, IncompatibleInterfaceException;

    BigraphComposite<S> compose(BigraphComposite<S> inner) throws IncompatibleSignatureException, IncompatibleInterfaceException;

    /**
     * Compute the tensor product of two bigraphs. The outer bigraph is the composite object itself.
     * A new immutable bigraph is created and returned as a {@link BigraphComposite} to be composed again.
     *
     * @param f inner bigraph term for the juxtaposition
     * @return the juxtaposition of two bigraphs
     * @throws IncompatibleSignatureException
     * @throws IncompatibleInterfaceException
     */
    BigraphComposite<S> juxtapose(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException;

    /**
     * Convenient method for {@link BigraphComposite#juxtapose(BigraphComposite)}.
     *
     * @param f inner bigraph term for the juxtaposition
     * @return
     * @throws IncompatibleSignatureException
     * @throws IncompatibleInterfaceException
     */
    BigraphComposite<S> juxtapose(BigraphComposite<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException;

    /**
     * Compute the parallel product of two bigraphs. The parallel product is defined as tensor product, except it allows
     * name sharing. So the tensor product can be seen as a special case of the parallel product.
     * <p>
     * This implementation satisfies the "bifunctiorial property". The inner faces must not be disjoint here, as defined
     * in previous works of Milner.
     *
     * @param f inner bigraph term for the parallel product
     * @return the parallel product of two bigraphs
     * @throws IncompatibleSignatureException
     * @throws IncompatibleInterfaceException
     */
    BigraphComposite<S> parallelProduct(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException;

    BigraphComposite<S> parallelProduct(BigraphComposite<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException;
}
