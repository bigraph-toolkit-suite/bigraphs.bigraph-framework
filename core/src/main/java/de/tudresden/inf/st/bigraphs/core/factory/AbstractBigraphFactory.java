package de.tudresden.inf.st.bigraphs.core.factory;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.generators.PureBigraphGenerator;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.DiscreteIon;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import org.eclipse.emf.ecore.EPackage;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * Abstract factory class for all kind of bigraphs.
 *
 * @author Dominik Grzelak
 */
public abstract class AbstractBigraphFactory<S extends Signature<? extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>>>
        implements BigraphFactoryElement {
    protected Type successorClass = null; //TODO: one for the signature type (and one for the bigraph)

    @Override
    public Type getSuccessorImpl() {
        return successorClass;
    }

//    /**
//     * Create a pure bigraph factory with default types for the control's label ({@link StringTypedName}) and
//     * arity ({@link FiniteOrdinal}.
//     *
//     * @return a pure bigraph factory
//     */
//    public static PureBigraphFactory createPureBigraphFactory() {
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

    public PureBigraphGenerator createRandomBuilder(DefaultDynamicSignature signature) {
        return new PureBigraphGenerator(signature);
    }

    public PureBigraphGenerator createRandomBuilder(DefaultDynamicSignature signature, EPackage metaModel) {
        return new PureBigraphGenerator(signature, metaModel);
    }

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

    public abstract BigraphBuilder<S> createBigraphBuilder(Signature<?> signature, EPackage bigraphMetaModel);

//    @Deprecated
//    public abstract Placings<S> createPlacings();

    public abstract Placings<S> createPlacings(S signature);

    public abstract Placings<S> createPlacings(S signature, EPackage bigraphMetaModel);

    public abstract Placings<S> createPlacings(S signature, EMetaModelData metaModelData);

    public abstract Linkings<S> createLinkings(S signature);

    public abstract Linkings<S> createLinkings(S signature, EPackage bigraphMetaModel);

    public abstract Linkings<S> createLinkings(S signature, EMetaModelData metaModelData);

    /**
     * Throws a runtime exception either because of InvalidConnectionException or TypeNotExistsException when connecting
     * the outer names to the node.
     *
     * @param name       the control's name for the ion
     * @param outerNames a set of outer names the ion shall have
     * @param signature  the signature of that ion
     * @return a discrete ion
     */
    public abstract DiscreteIon<S> createDiscreteIon(NamedType<?> name, Set<NamedType<?>> outerNames, S signature);

    public abstract DiscreteIon<DefaultDynamicSignature> createDiscreteIon(String name, Set<String> outerNames, S signature);

    public abstract DiscreteIon<DefaultDynamicSignature> createDiscreteIon(String name, Set<String> outerNames, S signature,
                                                                           EPackage bigraphMetaModel);

    /**
     * Create a composition object for a given bigraph which allows to compose bigraphs.
     * By that, bigraph operations can be extended and easily linked together.
     *
     * @param outerBigraph the outer bigraph in terms of a composition operator when an operator is applied
     * @return a bigraph composition operator based on the passed bigraph
     */
    public abstract BigraphComposite<S> asBigraphOperator(Bigraph<S> outerBigraph);
}
