package de.tudresden.inf.st.bigraphs.core.impl.factory;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;

//TODO: eventuell abstractfactory weglassen, wenn wir nur mit ecore arbeiten
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
