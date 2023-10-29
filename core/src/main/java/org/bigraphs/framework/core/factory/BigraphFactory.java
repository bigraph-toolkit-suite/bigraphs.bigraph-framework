package org.bigraphs.framework.core.factory;

import de.tudresden.inf.st.bigraphs.core.*;
import org.bigraphs.framework.core.alg.generators.PureBigraphGenerator;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.NamedType;
import org.bigraphs.framework.core.impl.elementary.DiscreteIon;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.KindBigraph;
import org.bigraphs.framework.core.impl.pure.KindBigraphBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.signature.KindSignature;
import org.bigraphs.framework.core.impl.signature.KindSignatureBuilder;
import org.bigraphs.framework.core.*;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * A bigraph factory class that provides a simplified main entry point for the developer/user to create arbitrary bigraphs and signatures.
 *
 * @author Dominik Grzelak
 */
public final class BigraphFactory {

    // ///////////////////////////////////
    // Bigraph Operator-related factories
    // ///////////////////////////////////

    public static synchronized <S extends Signature<? extends Control<?, ?>>> BigraphComposite<S> ops(Bigraph<S> bigraph) {
        return FactoryCreationContext.createOperator(bigraph);
    }


    // ///////////////////////////////////
    // Signature-related factories (Ecore)
    // ///////////////////////////////////


    public static synchronized <S extends AbstractEcoreSignature<? extends Control<?, ?>>> EPackage createOrGetSignatureMetaModel(S signatureObject) {
        return BigraphFactory.createOrGetSignatureMetaModel(signatureObject, (EMetaModelData) null);
    }

    /**
     * Overwrites the metadata of the metamodel.
     *
     * @param signatureObject
     * @param metaModelData
     * @param <S>
     * @return
     */
    public static synchronized <S extends AbstractEcoreSignature<? extends Control<?, ?>>> EPackage createOrGetSignatureMetaModel(S signatureObject, EMetaModelData metaModelData) {
        if ((Registry.INSTANCE_SIG.getEPackage(signatureObject)) == null) {
            Registry.INSTANCE_SIG.put(signatureObject, signatureObject.getMetaModel());
        }
        if (Objects.nonNull(metaModelData)) {
            EPackage ePackageMetaModel = signatureObject.getMetaModel();
            ePackageMetaModel.setNsPrefix(metaModelData.getNsPrefix());
            ePackageMetaModel.setNsURI(metaModelData.getNsUri());
            ePackageMetaModel.setName(metaModelData.getName());
        }
        return Registry.INSTANCE_SIG.getEPackage(signatureObject);
    }

    /**
     * Registers a signature metamodel.
     *
     * @param signatureInstanceModel the Ecore signature instance model
     * @param <S>                    the type of the signature
     * @return the signature object (freshly created, or the one already present in the internal registry)
     */
    public static synchronized <S extends AbstractEcoreSignature<? extends Control<?, ?>>> S createOrGetSignature(EObject signatureInstanceModel) {
        return createOrGetSignature(signatureInstanceModel, (EMetaModelData) null);
    }

    /**
     * Registers a signature metamodel
     * Overwrites the metadata of the metamodel.
     *
     * @param signatureInstanceModel the Ecore signature instance model
     * @param metaModelData          additional meta data in case the signature metamodel is going to be re-created;
     *                               otherwise it is ignored.
     * @param <S>                    the type of the signature
     * @return the signature object (freshly created, or the one already present in the internal registry)
     */
    public static synchronized <S extends AbstractEcoreSignature<? extends Control<?, ?>>> S createOrGetSignature(EObject signatureInstanceModel, EMetaModelData metaModelData) {
        if (!Registry.INSTANCE_SIG.containsValue(signatureInstanceModel.eClass().getEPackage())) {
            AbstractEcoreSignature<? extends Control<?, ?>> signatureFromMetaModel = BigraphBuilderSupport.getSignatureFromMetaModel(signatureInstanceModel);
            createOrGetSignatureMetaModel((S) signatureFromMetaModel, metaModelData);
            return (S) signatureFromMetaModel;
        } else {
            EPackage ePackageMetaModel = signatureInstanceModel.eClass().getEPackage();
            if (Objects.nonNull(metaModelData)) {
                ePackageMetaModel.setNsPrefix(metaModelData.getNsPrefix());
                ePackageMetaModel.setNsURI(metaModelData.getNsUri());
                ePackageMetaModel.setName(metaModelData.getName());
            }
            return (S) Registry.INSTANCE_SIG.getKeyForVal(signatureInstanceModel.eClass().getEPackage()).get();
        }
    }

    /**
     * After finalizing the signature builder (i.e., actually creating the signature object), it will be registered
     * automatically by the registry of this factory.
     *
     * @return a dynamic signature builder
     */
    public static synchronized DynamicSignatureBuilder pureSignatureBuilder() {
        return (DynamicSignatureBuilder) FactoryCreationContext.createSignatureBuilder(PureBigraph.class);
    }

    /**
     * After finalizing the signature builder (i.e., actually creating the signature object), it will be registered
     * automatically by the registry of this factory.
     *
     * @return a kind signature builder
     */
    public static synchronized KindSignatureBuilder kindSignatureBuilder() {
        return (KindSignatureBuilder) FactoryCreationContext.createSignatureBuilder(KindBigraph.class);
    }


    // /////////////////////////////////
    // Bigraph-related factories (Ecore)
    // /////////////////////////////////


    /**
     * Registers the bigraph metamodel.
     *
     * @param signature the signature object
     * @param <S>       the type of the signature
     * @return the Ecore bigraph metamodel for the given signature object
     */
    public static synchronized <S extends AbstractEcoreSignature<? extends Control<?, ?>>> EPackage createOrGetBigraphMetaModel(S signature) {
        return createOrGetBigraphMetaModel(signature, null);
    }

    /**
     * Registers the bigraph metamodel.
     * Overwrites the metadata of the metamodel.
     *
     * @param signature     the signature object
     * @param metaModelData additional meta data in case the signature metamodel is going to be re-created;
     *                      otherwise it is ignored.
     * @param <S>           the type of the signature
     * @return the Ecore bigraph metamodel for the given signature object
     */
    public static synchronized <S extends AbstractEcoreSignature<? extends Control<?, ?>>> EPackage createOrGetBigraphMetaModel(S signature, EMetaModelData metaModelData) {
        // Check here as well, whether the signature metamodel is in the registry
        createOrGetSignatureMetaModel(signature);
        EPackage bigraphBaseModelPackage = Registry.INSTANCE_BIG.getEPackage(signature);
        if (bigraphBaseModelPackage == null) {
            PureBigraphBuilder<S> b = (PureBigraphBuilder<S>) FactoryCreationContext.createBigraphBuilder(signature, PureBigraph.class);
            bigraphBaseModelPackage = b.getMetaModel();
            Registry.INSTANCE_BIG.put(signature, bigraphBaseModelPackage);
        }
        if (Objects.nonNull(metaModelData)) {
            bigraphBaseModelPackage.setNsPrefix(metaModelData.getNsPrefix());
            bigraphBaseModelPackage.setNsURI(metaModelData.getNsUri());
            bigraphBaseModelPackage.setName(metaModelData.getName());
        }
        return bigraphBaseModelPackage;
    }

    public static synchronized <S extends AbstractEcoreSignature<? extends Control<? extends NamedType<?>,
            ? extends FiniteOrdinal<?>>>> DiscreteIon<S> pureDiscreteIon(S signature, String name, String... outerNames) {
        // Check here as well, whether the signature metamodel is in the registry
        createOrGetSignatureMetaModel(signature);
        EPackage bigraphBaseModelPackage = Registry.INSTANCE_BIG.getEPackage(signature);
        if (Objects.isNull(bigraphBaseModelPackage)) {
            DiscreteIon<S> b = FactoryCreationContext.createDiscreteIonBuilder(signature, name, new HashSet<>(Arrays.asList(outerNames)), PureBigraph.class);
            Registry.INSTANCE_BIG.put(signature, b.getMetaModel());
            return b;
        } else {
            return FactoryCreationContext.createDiscreteIonBuilder(signature, name, new HashSet<>(Arrays.asList(outerNames)), bigraphBaseModelPackage, PureBigraph.class);
        }
    }

    public static synchronized <S extends AbstractEcoreSignature<? extends Control<?, ?>>> Placings<S> purePlacings(S signature) {
        // Check here as well, whether the signature metamodel is in the registry
        createOrGetSignatureMetaModel(signature);
        EPackage bigraphBaseModelPackage = Registry.INSTANCE_BIG.getEPackage(signature);
        if ((bigraphBaseModelPackage) == null) {
            Placings<S> b = FactoryCreationContext.createPlacingsBuilder(signature, PureBigraph.class);
            Registry.INSTANCE_BIG.put(signature, b.getLoadedModelPackage());
            return b;
        } else {
            return FactoryCreationContext.createPlacingsBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
        }
    }

    public static synchronized <S extends AbstractEcoreSignature<? extends Control<?, ?>>> Linkings<S> pureLinkings(S signature) {
        // Check here as well, whether the signature metamodel is in the registry
        createOrGetSignatureMetaModel(signature);
        EPackage bigraphBaseModelPackage = Registry.INSTANCE_BIG.getEPackage(signature);
        if ((bigraphBaseModelPackage) == null) {
            Linkings<S> b = FactoryCreationContext.createLinkingsBuilder(signature, PureBigraph.class);
            Registry.INSTANCE_BIG.put(signature, b.getLoadedModelPackage());
            return b;
        } else {
            return FactoryCreationContext.createLinkingsBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
        }
    }

    public static synchronized <S extends AbstractEcoreSignature<? extends Control<?, ?>>> PureBigraphBuilder<S> pureBuilder(S signature) {
        // Check here as well, whether the signature metamodel is in the registry
        createOrGetSignatureMetaModel(signature);
        EPackage bigraphBaseModelPackage = Registry.INSTANCE_BIG.getEPackage(signature);
        if ((bigraphBaseModelPackage) == null) {
            PureBigraphBuilder b = (PureBigraphBuilder) FactoryCreationContext.createBigraphBuilder(signature, PureBigraph.class);
            Registry.INSTANCE_BIG.put(signature, b.getMetaModel());
            return b;
        } else {
            return (PureBigraphBuilder<S>) FactoryCreationContext.createBigraphBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
        }
    }

    public static synchronized <S extends AbstractEcoreSignature<? extends Control<?, ?>>> PureBigraphBuilder<S> pureBuilder(S signature, EPackage bigraphBaseModelPackage) {
        // Check here as well, whether the signature metamodel is in the registry
        createOrGetSignatureMetaModel(signature);
        if (Registry.INSTANCE_BIG.get(signature) == null) {
            Registry.INSTANCE_BIG.put(signature, bigraphBaseModelPackage);
        } else {
            throw new RuntimeException("Signature already in the registry");
        }
        return (PureBigraphBuilder) FactoryCreationContext.createBigraphBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
    }

    public static synchronized <S extends AbstractEcoreSignature<? extends Control<?, ?>>> PureBigraphBuilder<S> pureBuilder(S signature, String bigraphBaseModelPackageFile) {
        // Check here as well, whether the signature metamodel is in the registry
        createOrGetSignatureMetaModel(signature);
        PureBigraphBuilder b = (PureBigraphBuilder) FactoryCreationContext.createBigraphBuilder(signature, bigraphBaseModelPackageFile, PureBigraph.class);
        Registry.INSTANCE_BIG.put(signature, b.getMetaModel());
        return b;
    }

    public static synchronized KindBigraphBuilder kindBuilder(KindSignature signature) {
        // Check here as well, whether the signature metamodel is in the registry
        createOrGetSignatureMetaModel(signature);
        EPackage bigraphBaseModelPackage = Registry.INSTANCE_BIG.getEPackage(signature);
        if ((bigraphBaseModelPackage) == null) {
            KindBigraphBuilder b = (KindBigraphBuilder) FactoryCreationContext.createBigraphBuilder(signature, KindBigraph.class);
            Registry.INSTANCE_BIG.put(signature, b.getMetaModel());
            return b;
        } else {
            return (KindBigraphBuilder) FactoryCreationContext.createBigraphBuilder(signature, bigraphBaseModelPackage, KindBigraph.class);
        }
    }

    public static synchronized <S extends AbstractEcoreSignature<? extends Control<?, ?>>> PureBigraphGenerator pureRandomBuilder(S signature) {
        // Check here as well, whether the signature metamodel is in the registry
        createOrGetSignatureMetaModel(signature);
        EPackage bigraphBaseModelPackage = Registry.INSTANCE_BIG.getEPackage(signature);
        if (Objects.isNull(bigraphBaseModelPackage)) {
            PureBigraphGenerator b = FactoryCreationContext.createRandomBigraphBuilder(signature, PureBigraph.class);
            Registry.INSTANCE_BIG.put(signature, b.getModelPackage());
            return b;
        } else {
            return FactoryCreationContext.createRandomBigraphBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
        }
    }

    public static synchronized <S extends AbstractEcoreSignature<? extends Control<?, ?>>> PureBigraphGenerator pureRandomBuilder(S signature, EPackage bigraphBaseModelPackage) {
        // Check here as well, whether the signature metamodel is in the registry
        createOrGetSignatureMetaModel(signature);
        Registry.INSTANCE_BIG.put(signature, bigraphBaseModelPackage);
        return FactoryCreationContext.createRandomBigraphBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
    }


    /**
     * Return the current factory context.
     *
     * @return the current factory context.
     */
    public static synchronized Optional<FactoryCreationContext> context() {
        return FactoryCreationContext.current();
    }

    public static synchronized void end() {
        FactoryCreationContext.end();
    }

    /**
     * A map from signature to bigraphical {@link EPackage} (typed graph with signature extension).
     */
    public interface Registry extends ConcurrentMap<Signature, EPackage> {
        /**
         * Looks up the value in the map
         */
        EPackage getEPackage(Signature signature);

        default Optional<Signature> getKeyForVal(final EPackage val) {
            return entrySet().stream()
                    .filter(e -> e.getValue().equals(val))
                    .map(Map.Entry::getKey)
                    .findFirst();
        }

        BigraphFactory.Registry INSTANCE_BIG = new DefaultBigraphModelsRegistryImpl();
        BigraphFactory.Registry INSTANCE_SIG = new DefaultSignatureModelsRegistryImpl();
    }
}
