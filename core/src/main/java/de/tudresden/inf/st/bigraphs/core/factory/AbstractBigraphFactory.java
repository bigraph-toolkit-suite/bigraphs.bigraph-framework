package de.tudresden.inf.st.bigraphs.core.factory;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;

import java.lang.reflect.Type;

//TODO: erstellt entweder dynamic controls oder default controls signaturen (also builder und signaturebuilder werden
//und compositor

//später wird noch type mitgegeben in factory um zu unterscheiden welche art von bigraph
// verwendet wird (falls es später andere bigraph extensions gibt, e.g. sharing, with sortings):
//Dann wird entsprechender builder und signaturebuilder geholt (art der signatur/controls und dem bigraph typ angepasst)
//grundlage bleibt immer ecore

//richtigerweise ersellt per unterklasse
//ecore bigraph bekommt dann abstracte klasse zwischen interface bigraph noch
//Später kann man noch über attributed bigraphs nachdenken (kann auch für matching verwendet werden - constraints)

/**
 * @author Dominik Grzelak
 */
public abstract class AbstractBigraphFactory<S extends Signature, NT extends NamedType, FT extends FiniteOrdinal> implements BigraphFactoryElement {
    protected Type successorClass = null; //TODO: one for the signature type (and one for the bigraph)

    @Override
    public Type getSuccessorImpl() {
        return successorClass;
    }

    //TODO move outside this class
    public static PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> createPureBigraphFactory() {
        return new PureBigraphFactory<>();
    }

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

    public abstract Placings<S> createPlacings();

    public abstract Linkings<S> createLinkings();

    /**
     * Create a composition object for a given bigraph which allows to compose bigraphs.
     * By that, bigraph operations can be extended and easily linked together.
     *
     * @param outerBigraph the outer bigraph in terms of a composition operator when an operator is applied
     * @return a bigraph composition operator based on the passed bigraph
     */
    public abstract BigraphComposite<S> asBigraphOperator(Bigraph<S> outerBigraph);

}
