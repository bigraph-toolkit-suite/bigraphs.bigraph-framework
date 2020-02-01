package de.tudresden.inf.st.bigraphs.core.factory;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.DiscreteIon;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * Abstract factory class for all kind of bigraphs.
 *
 * @author Dominik Grzelak
 */
public abstract class AbstractBigraphFactory<S extends Signature, NT extends NamedType, FT extends FiniteOrdinal> implements BigraphFactoryElement {
    protected Type successorClass = null; //TODO: one for the signature type (and one for the bigraph)

    @Override
    public Type getSuccessorImpl() {
        return successorClass;
    }

    /**
     * Create a pure bigraph factory with default types for the control's label ({@link StringTypedName}) and
     * arity ({@link FiniteOrdinal}.
     *
     * @return a pure bigraph factory
     */
    public static PureBigraphFactory createPureBigraphFactory() {
        return new PureBigraphFactory();
    }

//    /**
//     * Create a pure bigraph factory
//     * <p>
//     * //     * @param nameTypeClass    class of the control's labels
//     * //     * @param ordinalTypeClass class of the control's arity
//     *
//     * @param <NT> type of the control's labels
//     * @param <FT> type of the control's arity
//     * @return a pure bigraph factory with the provided types
//     */
//    public static <NT extends NamedType, FT extends Number> PureBigraphFactory createPureBigraphFactory(Class<NT> nameTypeClass, Class<FT> ordinalTypeClass) {
//        return new PureBigraphFactory();
//    }

    /**
     * Creates a builder for creating signatures.
     *
     * @param <SB> type of the signature builder (i.e., the type of the controls)
     * @return a new signature builder instance
     */
    public abstract <SB extends SignatureBuilder> SB createSignatureBuilder();

    /**
     * Throws a class cast exception when a signature is passed as argument which was not created with the
     * {@link SignatureBuilder} created by the same instance of this factory. Thus, they must have the same
     * signature type.
     *
     * @param signature the signature of the bigraph to build
     * @return a {@link PureBigraphBuilder} instance with signature type S which is inferred from the given signature.
     */
    public abstract BigraphBuilder<S> createBigraphBuilder(Signature<?> signature);

    /**
     * Throws a class cast exception when a signature is passed as argument which was not created with the
     * {@link SignatureBuilder} created by the same instance of this factory. Thus, they must have the same
     * signature type.
     *
     * @param signature     the signature of the bigraph to build
     * @param metaModelData meta data for the ecore files
     * @return a {@link PureBigraphBuilder} instance with signature type S which is inferred from the given signature.
     */
    public abstract BigraphBuilder<S> createBigraphBuilder(Signature<?> signature, EMetaModelData metaModelData);

    public abstract BigraphBuilder<S> createBigraphBuilder(Signature<?> signature, String metaModelFileName);

    @Deprecated
    public abstract Placings<S> createPlacings();

    public abstract Placings<S> createPlacings(S signature);

    @Deprecated
    public abstract Linkings<S> createLinkings();

    public abstract Linkings<S> createLinkings(S signature);

    /**
     * Throws a runtime exception either because of InvalidConnectionException or TypeNotExistsException when connecting
     * the outer names to the node.
     *
     * @param name
     * @param outerNames
     * @param signature
     * @return
     */
    public abstract DiscreteIon<S, NT, FT> createDiscreteIon(NT name, Set<NT> outerNames, S signature);

    /**
     * Create a composition object for a given bigraph which allows to compose bigraphs.
     * By that, bigraph operations can be extended and easily linked together.
     *
     * @param outerBigraph the outer bigraph in terms of a composition operator when an operator is applied
     * @return a bigraph composition operator based on the passed bigraph
     */
    public abstract BigraphComposite<S> asBigraphOperator(Bigraph<S> outerBigraph);

}
