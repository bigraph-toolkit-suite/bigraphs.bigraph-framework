package de.tudresden.inf.st.bigraphs.core.factory;

import com.google.common.reflect.TypeToken;
import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultBigraphComposite;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.DiscreteIon;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;

import java.util.Set;

//TODO default should be "change nsURI in to "http: ///ecore_file_name.ecore" and nsPrefix into "ecore_file_name" it woks great" when saving

/**
 * @author Dominik Grzelak
 */
public class PureBigraphFactory<NT extends NamedType, FT extends FiniteOrdinal>
        extends AbstractBigraphFactory<DefaultDynamicSignature, NT, FT> {

    PureBigraphFactory() {
        super.successorClass = new TypeToken<DefaultDynamicControl<NT, FT>>() {
        }.getType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <SB extends SignatureBuilder> SB createSignatureBuilder() {
        return (SB) new DynamicSignatureBuilder<NT, FT>();
    }

    @Override
    public PureBigraphBuilder<DefaultDynamicSignature> createBigraphBuilder(Signature<?> signature) {
//        PureBigraphBuilder<DefaultDynamicSignature> builder = new PureBigraphBuilder<DefaultDynamicSignature>().useSignature(signature);
//        PureBigraphBuilder.start(DefaultDynamicSignature.class.cast(signature));
        return PureBigraphBuilder.withSignature(DefaultDynamicSignature.class.cast(signature));
    }

    @Override
    public Placings<DefaultDynamicSignature> createPlacings() {
        return new Placings<>(this.createSignatureBuilder());
    }

    @Override
    public Linkings<DefaultDynamicSignature> createLinkings() {
        return new Linkings<>(this.createSignatureBuilder());
    }

    @Override
    public DiscreteIon<DefaultDynamicSignature, NT, FT> createDiscreteIon(NT name, Set<NT> outerNames, DefaultDynamicSignature signature) {
        return new DiscreteIon<>(
                name,
                outerNames,
                signature,
                this
        );
    }

    @Override
    public DefaultBigraphComposite<DefaultDynamicSignature> asBigraphOperator(Bigraph<DefaultDynamicSignature> outerBigraph) {
        return new DefaultBigraphComposite<>(outerBigraph);
    }

}
