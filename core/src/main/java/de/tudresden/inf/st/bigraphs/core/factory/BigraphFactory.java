package de.tudresden.inf.st.bigraphs.core.factory;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.alg.generators.PureBigraphGenerator;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.DiscreteIon;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import org.eclipse.emf.ecore.EPackage;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * A bigraph factory class that provides a simplified main entry point for the developer/user to create arbitrary bigraphs and signatures.
 *
 * @author Dominik Grzelak
 */
public final class BigraphFactory {

    public static synchronized <S extends Signature<? extends Control<?, ?>>> BigraphComposite<S> ops(Bigraph<S> bigraph) {
        return FactoryCreationContext.createOperator(bigraph);
    }

    public static synchronized <S extends Signature<? extends Control<?, ?>>> EPackage createOrGetMetaModel(S signature) {
        return createOrGetMetaModel(signature, null);
    }

    public static synchronized <S extends Signature<? extends Control<?, ?>>> EPackage createOrGetMetaModel(S signature, EMetaModelData metaModelData) {
        EPackage bigraphBaseModelPackage = Registry.INSTANCE.getEPackage(signature);
        if (Objects.isNull(bigraphBaseModelPackage)) {
            PureBigraphBuilder<S> b = (PureBigraphBuilder<S>) FactoryCreationContext.createBigraphBuilder(signature, PureBigraph.class);
            EPackage loadedEPackage = b.getLoadedEPackage();
            if (Objects.nonNull(metaModelData)) {
                loadedEPackage.setNsPrefix(metaModelData.getNsPrefix());
                loadedEPackage.setNsURI(metaModelData.getNsUri());
                loadedEPackage.setName(metaModelData.getName());
            }
            Registry.INSTANCE.put(signature, loadedEPackage);
            return loadedEPackage;
        } else {
            return bigraphBaseModelPackage;
        }
    }

    public static synchronized <S extends Signature<? extends Control<?, ?>>> PureBigraphBuilder<S> pureBuilder(S signature) {
        EPackage bigraphBaseModelPackage = Registry.INSTANCE.getEPackage(signature);
        if (Objects.isNull(bigraphBaseModelPackage)) {
            PureBigraphBuilder b = (PureBigraphBuilder) FactoryCreationContext.createBigraphBuilder(signature, PureBigraph.class);
            Registry.INSTANCE.put(signature, b.getLoadedEPackage());
            return b;
        } else {
            return (PureBigraphBuilder<S>) FactoryCreationContext.createBigraphBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
        }
    }

    public static synchronized <S extends Signature<? extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>>> DiscreteIon<S> pureDiscreteIon(S signature, String name, String... outerNames) {
        EPackage bigraphBaseModelPackage = Registry.INSTANCE.getEPackage(signature);
        if (Objects.isNull(bigraphBaseModelPackage)) {
            DiscreteIon<S> b = FactoryCreationContext.createDiscreteIonBuilder(signature, name, new HashSet<>(Arrays.asList(outerNames)), PureBigraph.class);
            Registry.INSTANCE.put(signature, b.getModelPackage());
            return b;
        } else {
            return FactoryCreationContext.createDiscreteIonBuilder(signature, name, new HashSet<>(Arrays.asList(outerNames)), bigraphBaseModelPackage, PureBigraph.class);
        }
    }

    public static synchronized <S extends Signature<? extends Control<?, ?>>> Placings<S> purePlacings(S signature) {
        EPackage bigraphBaseModelPackage = Registry.INSTANCE.getEPackage(signature);
        if (Objects.isNull(bigraphBaseModelPackage)) {
            Placings<S> b = FactoryCreationContext.createPlacingsBuilder(signature, PureBigraph.class);
            Registry.INSTANCE.put(signature, b.getLoadedModelPackage());
            return b;
        } else {
            return FactoryCreationContext.createPlacingsBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
        }
    }

    public static synchronized <S extends Signature<? extends Control<?, ?>>> Linkings<S> pureLinkings(S signature) {
        EPackage bigraphBaseModelPackage = Registry.INSTANCE.getEPackage(signature);
        if (Objects.isNull(bigraphBaseModelPackage)) {
            Linkings<S> b = FactoryCreationContext.createLinkingsBuilder(signature, PureBigraph.class);
            Registry.INSTANCE.put(signature, b.getLoadedModelPackage());
            return b;
        } else {
            return FactoryCreationContext.createLinkingsBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
        }
    }

    public static synchronized <S extends Signature<? extends Control<?, ?>>> PureBigraphBuilder<S> pureBuilder(S signature, EPackage bigraphBaseModelPackage) {
        if (Registry.INSTANCE.get(signature) == null) {
            Registry.INSTANCE.put(signature, bigraphBaseModelPackage);
        } else {
            throw new RuntimeException("Signature already in the registry");
        }
        return (PureBigraphBuilder) FactoryCreationContext.createBigraphBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
    }

    public static synchronized <S extends Signature<? extends Control<?, ?>>> PureBigraphBuilder<S> pureBuilder(S signature, String bigraphBaseModelPackageFile) {
        PureBigraphBuilder b = (PureBigraphBuilder) FactoryCreationContext.createBigraphBuilder(signature, bigraphBaseModelPackageFile, PureBigraph.class);
        Registry.INSTANCE.put(signature, b.getLoadedEPackage());
        return b;
    }

    public static synchronized <S extends Signature<? extends Control<?, ?>>> PureBigraphGenerator pureRandomBuilder(DefaultDynamicSignature signature) {
        EPackage bigraphBaseModelPackage = Registry.INSTANCE.getEPackage(signature);
        if (Objects.isNull(bigraphBaseModelPackage)) {
            PureBigraphGenerator b = FactoryCreationContext.createRandomBigraphBuilder(signature, PureBigraph.class);
            Registry.INSTANCE.put(signature, b.getModelPackage());
            return b;
        } else {
            return FactoryCreationContext.createRandomBigraphBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
        }
    }

    public static synchronized <S extends Signature<? extends Control<?, ?>>> PureBigraphGenerator pureRandomBuilder(DefaultDynamicSignature signature, EPackage bigraphBaseModelPackage) {
        Registry.INSTANCE.put(signature, bigraphBaseModelPackage);
        return FactoryCreationContext.createRandomBigraphBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
    }

    public static synchronized DynamicSignatureBuilder pureSignatureBuilder() {
        return (DynamicSignatureBuilder) FactoryCreationContext.createSignatureBuilder(PureBigraph.class);
    }

    /**
     * Create a pure bigraph factory with default types for the control's label ({@link StringTypedName}) and
     * arity ({@link FiniteOrdinal}).
     * <p>
     *
     * <b>Note</b> that it is advised to use the respective builder methods (e.g., {@link #pureBuilder(Signature)})
     * for spawning the required builder classes (e.g., signatures, bigraphs, etc.).
     * This ensures correct management of the internal Ecore metamodel usage. Otherwise, the user has to take care about
     * re-using the generated bigraphical metamodel over a signature with other builders when referring to the
     * same signature.
     *
     * @return a pure bigraph factory
     */
    @Deprecated
    public static synchronized PureBigraphFactory pure() {
        FactoryCreationContext.begin(new PureBigraphFactory());
        return (PureBigraphFactory) FactoryCreationContext.current().get().getFactory();
    }

    /**
     * Uses a given factory context to create a {@link PureBigraphFactory}.
     *
     * @param context a valid factory context
     * @return a pure bigraph factory created by the provided context
     */
    public static synchronized PureBigraphFactory pure(FactoryCreationContext context) {
//        FactoryCreationContext.begin(FactoryCreationContext.createPureBigraphFactory());
        return (PureBigraphFactory) context.getFactory();
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

        BigraphFactory.Registry INSTANCE = new DefaultSignatureRegistryImpl();
    }
}
