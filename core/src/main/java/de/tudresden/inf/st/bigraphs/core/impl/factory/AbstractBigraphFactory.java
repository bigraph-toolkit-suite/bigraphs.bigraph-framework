package de.tudresden.inf.st.bigraphs.core.impl.factory;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphComposition;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;

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


public abstract class AbstractBigraphFactory<S extends Signature, NT extends NamedType, FT extends FiniteOrdinal> implements BigraphFactoryElement {
    protected Type successorClass = null;

    @Override
    public Type getSuccessorImpl() {
        return successorClass;
    }

    public abstract <SB extends SignatureBuilder> SB createSignatureBuilder();

    /**
     * Throws a class cast exception when a signature is passed as argument which was not created with the same signaturebuilder created
     * by this factory.
     *
     * @param signature
     * @return
     */
    public abstract BigraphBuilder<S> createBigraphBuilder(Signature<?> signature);

    public abstract BigraphComposition<S> createBigraphOperations(Bigraph<S> bigraph);

}
