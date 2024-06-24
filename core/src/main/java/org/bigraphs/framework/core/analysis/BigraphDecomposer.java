package org.bigraphs.framework.core.analysis;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;

import java.lang.reflect.InvocationTargetException;

import static org.bigraphs.framework.core.analysis.BigraphDecompositionStrategy.DecompositionStrategy.UnionFind_PureBigraphs;

/**
 * This class provides access to decomposition approaches to bigraphs.
 * The strategy pattern is implemented to select the concrete decomposition approach.
 * The specific decomposition algorithm is implemented by subclasses of the interface {@link BigraphDecompositionStrategy}.
 * <p>
 * Subclasses of {@link BigraphDecomposer} may offer additional methods depending on which specific decomposition strategy
 * was used. For example, a subclass may return the partitions or provide other information.
 * <p>
 * An instance of this class can be created by calling the factory method {@link #create(BigraphDecompositionStrategy.DecompositionStrategy)} and
 * supplying the decomposition strategy.
 *
 * @param <B> type of the bigraph
 * @author Dominik Grzelak
 */
public class BigraphDecomposer<B extends Bigraph<? extends Signature<?>>> {

    public static final BigraphDecompositionStrategy.DecompositionStrategy DEFAULT_DECOMPOSITION_STRATEGY = UnionFind_PureBigraphs;

    public static <B extends Bigraph<? extends Signature<?>>> BigraphDecomposer<B> create() {
        return (BigraphDecomposer<B>) create(DEFAULT_DECOMPOSITION_STRATEGY, PureBigraphDecomposerImpl.class);
//        return (BigraphDecomposer<B>) create(DEFAULT_DECOMPOSITION_STRATEGY);
    }

    // Naive "multi-dispatch": based on the decomposition strategy and bigraph type, the corresponding subclass
    // will be instantiated. Otherwise, an exception is thrown.
    public static <B extends Bigraph<? extends Signature<?>>, T extends BigraphDecomposer<B>> T create(BigraphDecompositionStrategy.DecompositionStrategy strategy, Class<T> tClass) {
        try {
            switch (strategy) {
                case UnionFind_PureBigraphs -> {
                    BigraphDecompositionStrategy<? extends Bigraph<? extends Signature<?>>> bigraphDecompositionStrategy = strategy.getImplClassType().getDeclaredConstructor().newInstance();
                    return (T) (new PureBigraphDecomposerImpl((BigraphDecompositionStrategy<PureBigraph>) bigraphDecompositionStrategy));
                }
                default -> throw new RuntimeException("Failed to create BigraphDecomposer instance: " + strategy);
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException("Failed to create BigraphDecomposer instance: " + strategy);
        }
    }

    public static <B extends Bigraph<? extends Signature<?>>, T extends BigraphDecomposer<B>> T create(BigraphDecompositionStrategy.DecompositionStrategy strategy) {
        return create(strategy, null);
    }

    protected BigraphDecompositionStrategy<B> decompositionStrategy;

    protected BigraphDecomposer(BigraphDecompositionStrategy<B> decompositionStrategy) {
        this.decompositionStrategy = decompositionStrategy;
    }

    public BigraphDecompositionStrategy<B> getDecompositionStrategy() {
        return decompositionStrategy;
    }

    public void decompose(B bigraph) {
        this.getDecompositionStrategy().decompose(bigraph);
    }
}
