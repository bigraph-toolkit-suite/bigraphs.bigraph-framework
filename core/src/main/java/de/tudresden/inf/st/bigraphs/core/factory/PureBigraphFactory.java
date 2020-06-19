package de.tudresden.inf.st.bigraphs.core.factory;

import com.google.common.reflect.TypeToken;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
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
public class PureBigraphFactory extends AbstractBigraphFactory<DefaultDynamicSignature> {

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
        return new Placings<>(this.createSignatureBuilder());
    }

    @Override
    public Placings<DefaultDynamicSignature> createPlacings(DefaultDynamicSignature signature) {
        return new Placings<>(signature);
    }

    @Override
    public Placings<DefaultDynamicSignature> createPlacings(DefaultDynamicSignature signature, EMetaModelData metaModelData) {
        return new Placings<>(signature, metaModelData);
    }

    @Override
    public Linkings<DefaultDynamicSignature> createLinkings() {
        return new Linkings<>(this.createSignatureBuilder());
    }

    @Override
    public Linkings<DefaultDynamicSignature> createLinkings(DefaultDynamicSignature signature) {
        return new Linkings<>(signature);
    }

    @Override
    public Linkings<DefaultDynamicSignature> createLinkings(DefaultDynamicSignature signature, EMetaModelData metaModelData) {
        return new Linkings<>(signature, metaModelData);
    }

    /**
     * Throws a runtime exception either because of InvalidConnectionException or TypeNotExistsException when connecting
     * the outer names to the node.
     *
     * @param name       the control's name for the ion, must be of type {@link StringTypedName}
     * @param outerNames a set of outer names the ion shall have, must be of type {@link StringTypedName}
     * @param signature  the signature of that ion
     * @return a discrete ion
     */
    @Override
    public DiscreteIon<DefaultDynamicSignature> createDiscreteIon(NamedType<?> name, Set<NamedType<?>> outerNames, DefaultDynamicSignature signature) {
        assert name instanceof StringTypedName;
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
