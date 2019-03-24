package de.tudresden.inf.st.bigraphs.core.impl.factory;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;

//TODO: erstellt entweder dynamic controls oder default controls signaturen (also builder und signaturebuilder werden
//und compositor

//eine bridge dann zwischen den zwei varianten (falls es später andere bigraph extensions gibt, e.g. sharing):
//nämlich die art der signatur/controls und dem bigraph typ
//grundlage bleibt immer ecore

//richtigerweise ersellt per unterklasse
//ecore bigraph bekommt dann abstracte klasse zwischen interface bigraph noch
//Später kann man noch über attributed bigraphs nachdenken (kann auch für matching verwendet werden - constraints)


public abstract class AbstractBigraphFactory {

    //TODO
    //Der builder enthält schon das fertige model: wird vorher gebaut und dann übergeben
    //hat auch schon die signatur
    //einen abstrakt builder verwenden später
//    abstract Bigraph createBigraph(BigraphBuilder builder);


    /**
     * Creates an empty bigraph with a single root
     *
     * @param signature
     * @return
     */
    abstract Bigraph createEmptyBigraph(Signature signature); //with defined signature is OK
//    public abstract Signature createSignature(Iterable<Control> controls);
//    public abstract Bigraph createEmptyBigraph();

}
