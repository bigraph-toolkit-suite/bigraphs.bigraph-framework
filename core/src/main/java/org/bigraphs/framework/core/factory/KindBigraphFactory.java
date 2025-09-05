package org.bigraphs.framework.core.factory;

import com.google.common.reflect.TypeToken;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.datatypes.NamedType;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.impl.elementary.DiscreteIon;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.KindBigraph;
import org.bigraphs.framework.core.impl.pure.KindBigraphBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraphComposite;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.bigraphs.framework.core.impl.signature.KindSignature;
import org.bigraphs.framework.core.impl.signature.KindSignatureBuilder;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dominik Grzelak
 */
public class KindBigraphFactory extends AbstractBigraphFactory<KindSignature> {

    KindBigraphFactory() {
        super.signatureImplType = new TypeToken<DynamicControl>() {
        }.getType();
        super.bigraphClassType = KindBigraph.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public KindSignatureBuilder createSignatureBuilder() {
        return new KindSignatureBuilder();
    }

    @Override
    public KindBigraphBuilder createBigraphBuilder(Signature<?> signature) {
        return KindBigraphBuilder.create(KindSignature.class.cast(signature));
    }

    @Override
    public KindBigraphBuilder createBigraphBuilder(Signature<?> signature, EMetaModelData metaModelData) {
        return KindBigraphBuilder.create(KindSignature.class.cast(signature), metaModelData);
    }

    @Override
    public KindBigraphBuilder createBigraphBuilder(Signature<?> signature, String metaModelFileName) {
        return KindBigraphBuilder.create(KindSignature.class.cast(signature), metaModelFileName);
    }

    @Override
    public KindBigraphBuilder createBigraphBuilder(Signature<?> signature, EPackage bigraphMetaModel) {
        return KindBigraphBuilder.create(KindSignature.class.cast(signature), bigraphMetaModel, (EObject) null);
    }

    @Override
    public Placings<KindSignature> createPlacings(KindSignature signature) {
        return new Placings<>(signature);
    }

    @Override
    public Placings<KindSignature> createPlacings(KindSignature signature, EPackage bigraphMetaModel) {
        return new Placings<>(signature, bigraphMetaModel);
    }

    @Override
    public Placings<KindSignature> createPlacings(KindSignature signature, EMetaModelData metaModelData) {
        return new Placings<>(signature, metaModelData);
    }

    @Override
    public Linkings<KindSignature> createLinkings(KindSignature signature) {
        return new Linkings<>(signature);
    }

    @Override
    public Linkings<KindSignature> createLinkings(KindSignature signature, EPackage bigraphMetaModel) {
        return new Linkings<>(signature, bigraphMetaModel);
    }

    @Override
    public Linkings<KindSignature> createLinkings(KindSignature signature, EMetaModelData metaModelData) {
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
    public DiscreteIon<KindSignature> createDiscreteIon(NamedType<?> name, Set<NamedType<?>> outerNames, KindSignature signature) {
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
     * @see KindBigraphFactory#createDiscreteIon(NamedType, Set, KindSignature)
     */
    public DiscreteIon<KindSignature> createDiscreteIon(String name, Set<String> outerNames, KindSignature signature) {
        return new DiscreteIon<>(
                StringTypedName.of(name),
                outerNames.stream().map(StringTypedName::of).collect(Collectors.toSet()),
                signature,
                this
        );
    }

    public DiscreteIon<KindSignature> createDiscreteIon(String name, Set<String> outerNames, KindSignature signature, EPackage bigraphMetaModel) {
        return new DiscreteIon<>(
                StringTypedName.of(name),
                outerNames.stream().map(StringTypedName::of).collect(Collectors.toSet()),
                signature,
                bigraphMetaModel,
                this
        );
    }

    @Override
    public PureBigraphComposite<KindSignature> asBigraphOperator(Bigraph<KindSignature> outerBigraph) {
//        return new PureBigraphComposite<>(outerBigraph);//TODO
        throw new RuntimeException("Not implemented yet");
    }

}
