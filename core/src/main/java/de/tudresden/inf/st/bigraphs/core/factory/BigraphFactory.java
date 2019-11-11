package de.tudresden.inf.st.bigraphs.core.factory;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;

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

    /**
     * Create a pure bigraph factory with default types for the control's label ({@link StringTypedName}) and
     * arity ({@link FiniteOrdinal<Integer>}.
     *
     * @return a pure bigraph factory
     */
    public static PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> pure() {
        FactoryCreationContext.begin(FactoryCreationContext.createPureBigraphFactory());
        return (PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>>) FactoryCreationContext.current().get().getFactory();
    }

    public static void end() {
        FactoryCreationContext.end();
    }

}
