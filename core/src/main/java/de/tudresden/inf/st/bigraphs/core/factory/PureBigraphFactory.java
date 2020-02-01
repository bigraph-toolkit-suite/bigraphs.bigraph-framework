package de.tudresden.inf.st.bigraphs.core.factory;

import com.google.common.reflect.TypeToken;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphComposite;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.DiscreteIon;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;

import java.util.Set;

/**
 * @author Dominik Grzelak
 */
public class PureBigraphFactory
        extends AbstractBigraphFactory<DefaultDynamicSignature, StringTypedName, FiniteOrdinal<Integer>> {

    PureBigraphFactory() {
        super.successorClass = new TypeToken<DefaultDynamicControl>() {
        }.getType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public DynamicSignatureBuilder createSignatureBuilder() {
        return new DynamicSignatureBuilder();
    }

    @Override
    public PureBigraphBuilder<DefaultDynamicSignature> createBigraphBuilder(Signature<?> signature) {
        return PureBigraphBuilder.create(DefaultDynamicSignature.class.cast(signature));
    }

    @Override
    public PureBigraphBuilder<DefaultDynamicSignature> createBigraphBuilder(Signature<?> signature, EMetaModelData metaModelData) {
        return PureBigraphBuilder.create(DefaultDynamicSignature.class.cast(signature), metaModelData);
    }

    @Override
    public BigraphBuilder<DefaultDynamicSignature> createBigraphBuilder(Signature<?> signature, String metaModelFileName) {
        return PureBigraphBuilder.create(DefaultDynamicSignature.class.cast(signature), metaModelFileName);
    }

    @Override
    public Placings<DefaultDynamicSignature> createPlacings() {
        return new Placings<DefaultDynamicSignature>(this.createSignatureBuilder());
    }

    @Override
    public Placings<DefaultDynamicSignature> createPlacings(DefaultDynamicSignature signature) {
        return new Placings<>(signature);
    }

    @Override
    public Linkings<DefaultDynamicSignature> createLinkings() {
        return new Linkings<DefaultDynamicSignature>(this.createSignatureBuilder());
    }

    @Override
    public Linkings<DefaultDynamicSignature> createLinkings(DefaultDynamicSignature signature) {
        return new Linkings<>(signature);
    }

    @Override
    public DiscreteIon<DefaultDynamicSignature, StringTypedName, FiniteOrdinal<Integer>> createDiscreteIon(StringTypedName name, Set<StringTypedName> outerNames, DefaultDynamicSignature signature) {
        return new DiscreteIon<>(
                name,
                outerNames,
                signature,
                this
        );
    }

    @Override
    public PureBigraphComposite<DefaultDynamicSignature> asBigraphOperator(Bigraph<DefaultDynamicSignature> outerBigraph) {
        return new PureBigraphComposite<>(outerBigraph);
    }

}
