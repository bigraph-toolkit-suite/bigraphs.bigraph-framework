package org.bigraphs.framework.core;

import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.pure.PureBigraphComposite;
import org.bigraphs.framework.core.datatypes.EMetaModelData;

/**
 * Generic interface for composing bigraphs.
 * <p>
 * Current implementation:
 * <ul>
 *   <li>{@link PureBigraphComposite} for pure bigraphs</li>
 * </ul>
 *
 * @param <S> the signature type
 * @author Dominik Grzelak
 */
public interface BigraphComposite<S extends Signature<? extends Control<?, ?>>> {

    /**
     * Return the outer bigraph of a composition, that is, the left part of the operator (i.e., outer bigraph).
     *
     * @return the outer bigraph of a composition
     */
    <B extends Bigraph<S>> B getOuterBigraph();

    /**
     * Composes two bigraphs where a new immutable bigraph is created.
     * <p>
     * The {@link EMetaModelData} of the outer bigraph is used.
     *
     * @param inner the right part of the composition operator, i.e., the inner bigraph
     * @return a new bigraph composed of both arguments
     */
    BigraphComposite<S> compose(Bigraph<S> inner) throws IncompatibleSignatureException, IncompatibleInterfaceException;

    /**
     * Composes two bigraphs where a new immutable bigraph is created.
     * <p>
     * The {@link EMetaModelData} of the outer bigraph is used.
     *
     * @param inner the right part of the composition operator, i.e., the inner bigraph
     * @return a new bigraph composed of both arguments
     */
    BigraphComposite<S> compose(BigraphComposite<S> inner) throws IncompatibleSignatureException, IncompatibleInterfaceException;

    /**
     * Nesting operation for two bigraphs. It works like composition but with sharing of names.
     * It is a derived operation and uses parallel and composition operations.
     *
     * @param inner the inner bigraph
     * @return a new composed bigraph
     * @throws IncompatibleSignatureException if signatures do not match
     * @throws IncompatibleInterfaceException if both bigraphs have incompatible interface definitions
     */
    BigraphComposite<S> nesting(Bigraph<S> inner) throws IncompatibleSignatureException, IncompatibleInterfaceException;

    /**
     * Nesting operation for two bigraphs. It works like composition but with sharing of names.
     * It is a derived operation and uses parallel and composition operations.
     *
     * @param inner the inner bigraph of type {@link BigraphComposite}
     * @return a new composed bigraph
     * @throws IncompatibleSignatureException if signatures do not match
     * @throws IncompatibleInterfaceException if both bigraphs have incompatible interface definitions
     */
    BigraphComposite<S> nesting(BigraphComposite<S> inner) throws IncompatibleSignatureException, IncompatibleInterfaceException;

    /**
     * Compute the tensor product of two bigraphs. The outer bigraph is the composite object itself.
     * A new immutable bigraph is created and returned as a {@link BigraphComposite} to be composed again.
     * <p>
     * The {@link EMetaModelData} of the outer bigraph is used.
     *
     * @param f inner bigraph term for the juxtaposition
     * @return the juxtaposition of two bigraphs
     * @throws IncompatibleSignatureException
     * @throws IncompatibleInterfaceException
     */
    BigraphComposite<S> juxtapose(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException;

    /**
     * Convenient method for {@link BigraphComposite#juxtapose(BigraphComposite)}.
     * <p>
     * The {@link EMetaModelData} of the outer bigraph is used.
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
     * <p>
     * The {@link EMetaModelData} of the outer bigraph is used.
     *
     * @param f inner bigraph term for the parallel product
     * @return the parallel product of two bigraphs
     * @throws IncompatibleSignatureException if the signature of both bigraphs are not the same
     * @throws IncompatibleInterfaceException if the interfaces of both bigraphs are not appropriate for the operator
     */
    BigraphComposite<S> parallelProduct(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException;

    /**
     * Compute the parallel product of two bigraphs. The parallel product is defined as tensor product, except it allows
     * name sharing. So the tensor product can be seen as a special case of the parallel product.
     * <p>
     * This implementation satisfies the "bifunctiorial property". The inner faces must not be disjoint here, as defined
     * in previous works of Milner.
     * <p>
     * The {@link EMetaModelData} of the outer bigraph is used.
     *
     * @param f inner bigraph term for the parallel product
     * @return the parallel product of two bigraphs
     * @throws IncompatibleSignatureException if the signature of both bigraphs are not the same
     * @throws IncompatibleInterfaceException if the interfaces of both bigraphs are not appropriate for the operator
     */
    BigraphComposite<S> parallelProduct(BigraphComposite<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException;

    BigraphComposite<S> merge(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException;
    BigraphComposite<S> merge(BigraphComposite<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException;

    /**
     * Compute the parallel product in a row.
     * <p>
     * The {@link EMetaModelData} of the outer bigraph is used.
     *
     * @param bigraphs the bigraphs to juxtapose in the order the appear
     * @return the juxtaposed bigraph
     */
    BigraphComposite<S> parallelProductOf(Bigraph<S>... bigraphs) throws IncompatibleSignatureException, IncompatibleInterfaceException;

    /**
     * Compute the tensor product in a row.
     * <p>
     * The {@link EMetaModelData} of the outer bigraph is used.
     *
     * @param bigraphs the bigraphs to juxtapose in the order the appear
     * @return the juxtaposed bigraph
     */
    BigraphComposite<S> juxtpositionOf(Bigraph<S>... bigraphs) throws IncompatibleSignatureException, IncompatibleInterfaceException;
}
