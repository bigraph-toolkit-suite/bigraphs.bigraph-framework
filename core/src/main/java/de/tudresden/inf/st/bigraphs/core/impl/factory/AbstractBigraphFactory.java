package de.tudresden.inf.st.bigraphs.core.impl.factory;

import de.tudresden.inf.st.bigraphs.core.BigraphOperations;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicControlBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;

//TODO: erstellt entweder dynamic controls oder default controls signaturen (also builder und signaturebuilder werden
//und compositor

//später wird noch type mitgegeben in factory um zu unterscheiden welche art von bigraph
// verwendet wird (falls es später andere bigraph extensions gibt, e.g. sharing, with sortings):
//Dann wird entsprechender builder und signaturebuilder geholt (art der signatur/controls und dem bigraph typ angepasst)
//grundlage bleibt immer ecore

//richtigerweise ersellt per unterklasse
//ecore bigraph bekommt dann abstracte klasse zwischen interface bigraph noch
//Später kann man noch über attributed bigraphs nachdenken (kann auch für matching verwendet werden - constraints)


public abstract class AbstractBigraphFactory {

    public abstract <S extends SignatureBuilder> S createSignatureBuilder();

    public abstract <S extends Signature> BigraphBuilder createBigraphBuilder(S signature);

    public abstract BigraphOperations createBigraphOperations();

}
