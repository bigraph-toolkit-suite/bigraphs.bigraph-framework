package de.tudresden.inf.st.bigraphs.core.factory;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;

import java.util.Optional;

/**
 * @author Dominik Grzelak
 */
public final class BigraphFactory {

    public static <S extends Signature> BigraphComposite<S> ops(Bigraph<S> bigraph) {
        return FactoryCreationContext.createOperator(bigraph);
    }

    public static <S extends Signature> PureBigraphBuilder<S> pureBuilder(S signature) {
        return (PureBigraphBuilder) FactoryCreationContext.createBigraphBuilder(signature, PureBigraph.class);
    }

    public static <S extends Signature> PureBigraphBuilder<S> pureBuilder(S signature, String metaModel) {
        return (PureBigraphBuilder) FactoryCreationContext.createBigraphBuilder(signature, metaModel, PureBigraph.class);
    }

    public static DynamicSignatureBuilder pureSignatureBuilder() {
        return (DynamicSignatureBuilder) FactoryCreationContext.createSignatureBuilder(PureBigraph.class);
    }

    /**
     * Create a pure bigraph factory with default types for the control's label ({@link StringTypedName}) and
     * arity ({@link FiniteOrdinal}).
     *
     * @return a pure bigraph factory
     */
    public static PureBigraphFactory pure() {
        FactoryCreationContext.begin(AbstractBigraphFactory.createPureBigraphFactory());
        return (PureBigraphFactory) FactoryCreationContext.current().get().getFactory();
    }

    /**
     * Uses a given factory context to create a {@link PureBigraphFactory}.
     *
     * @param context a valid factory context
     * @return a pure bigraph factory created by the provided context
     */
    public static PureBigraphFactory pure(FactoryCreationContext context) {
//        FactoryCreationContext.begin(FactoryCreationContext.createPureBigraphFactory());
        return (PureBigraphFactory) context.getFactory();
    }

    /**
     * Return the current factory context.
     *
     * @return the current factory context.
     */
    public static Optional<FactoryCreationContext> context() {
        return FactoryCreationContext.current();
    }

    public static void end() {
        FactoryCreationContext.end();
    }

}
