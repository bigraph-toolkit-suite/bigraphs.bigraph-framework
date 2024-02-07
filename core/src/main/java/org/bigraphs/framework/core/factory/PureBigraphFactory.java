package org.bigraphs.framework.core.factory;

import com.google.common.reflect.TypeToken;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.datatypes.NamedType;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicControl;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.elementary.DiscreteIon;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraphComposite;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dominik Grzelak
 */
public class PureBigraphFactory extends AbstractBigraphFactory<DefaultDynamicSignature> {

    PureBigraphFactory() {
        super.signatureImplType = new TypeToken<DefaultDynamicControl>() {
        }.getType();
        super.bigraphClassType = PureBigraph.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DynamicSignatureBuilder createSignatureBuilder() {
        return new DynamicSignatureBuilder();
    }

//    @Override
//    @SuppressWarnings("unchecked")
//    public KindSignatureBuilder createKindSignatureBuilder() {
//        return new KindSignatureBuilder();
//    }

    @Override
    public PureBigraphBuilder<DefaultDynamicSignature> createBigraphBuilder(Signature<?> signature) {
        return PureBigraphBuilder.create(DefaultDynamicSignature.class.cast(signature));
    }

    @Override
    public PureBigraphBuilder<DefaultDynamicSignature> createBigraphBuilder(Signature<?> signature, EMetaModelData metaModelData) {
        return PureBigraphBuilder.create(DefaultDynamicSignature.class.cast(signature), metaModelData);
    }

    @Override
    public PureBigraphBuilder<DefaultDynamicSignature> createBigraphBuilder(Signature<?> signature, String metaModelFileName) {
        return PureBigraphBuilder.create(DefaultDynamicSignature.class.cast(signature), metaModelFileName);
    }

    @Override
    public PureBigraphBuilder<DefaultDynamicSignature> createBigraphBuilder(Signature<?> signature, EPackage bigraphMetaModel) {
        return PureBigraphBuilder.create(DefaultDynamicSignature.class.cast(signature), bigraphMetaModel, (EObject) null);
    }

    @Override
    public Placings<DefaultDynamicSignature> createPlacings(DefaultDynamicSignature signature) {
        return new Placings<>(signature);
    }

    @Override
    public Placings<DefaultDynamicSignature> createPlacings(DefaultDynamicSignature signature, EPackage bigraphMetaModel) {
        return new Placings<>(signature, bigraphMetaModel);
    }

    @Override
    public Placings<DefaultDynamicSignature> createPlacings(DefaultDynamicSignature signature, EMetaModelData metaModelData) {
        return new Placings<>(signature, metaModelData);
    }

    @Override
    public Linkings<DefaultDynamicSignature> createLinkings(DefaultDynamicSignature signature) {
        return new Linkings<>(signature);
    }

    @Override
    public Linkings<DefaultDynamicSignature> createLinkings(DefaultDynamicSignature signature, EPackage bigraphMetaModel) {
        return new Linkings<>(signature, bigraphMetaModel);
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

    /**
     * Convenient method.
     *
     * @see PureBigraphFactory#createDiscreteIon(NamedType, Set, DefaultDynamicSignature)
     */
    public DiscreteIon<DefaultDynamicSignature> createDiscreteIon(String name, Set<String> outerNames, DefaultDynamicSignature signature) {
        return new DiscreteIon<>(
                StringTypedName.of(name),
                outerNames.stream().map(StringTypedName::of).collect(Collectors.toSet()),
                signature,
                this
        );
    }

    public DiscreteIon<DefaultDynamicSignature> createDiscreteIon(String name, Set<String> outerNames, DefaultDynamicSignature signature, EPackage bigraphMetaModel) {
        return new DiscreteIon<>(
                StringTypedName.of(name),
                outerNames.stream().map(StringTypedName::of).collect(Collectors.toSet()),
                signature,
                bigraphMetaModel,
                this
        );
    }

    @Override
    public PureBigraphComposite<DefaultDynamicSignature> asBigraphOperator(Bigraph<DefaultDynamicSignature> outerBigraph) {
        return new PureBigraphComposite<>(outerBigraph);
    }

}
