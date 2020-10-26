package de.tudresden.inf.st.bigraphs.core.factory;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.generators.PureBigraphGenerator;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.DiscreteIon;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import org.eclipse.emf.ecore.EPackage;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * A bigraph factory class that provides a simplified main entry point for the developer/user to create arbitrary bigraphs and signatures.
 *
 * @author Dominik Grzelak
 */
public final class BigraphFactory {

    public static synchronized <S extends Signature> BigraphComposite<S> ops(Bigraph<S> bigraph) {
        return FactoryCreationContext.createOperator(bigraph);
    }

    public static synchronized <S extends Signature> PureBigraphBuilder<S> pureBuilder(S signature) {
        EPackage bigraphBaseModelPackage = Registry.INSTANCE.getEPackage(signature);
        if (Objects.isNull(bigraphBaseModelPackage)) {
            PureBigraphBuilder b = (PureBigraphBuilder) FactoryCreationContext.createBigraphBuilder(signature, PureBigraph.class);
            Registry.INSTANCE.put(signature, b.getLoadedEPackage());
            return b;
        } else {
            return (PureBigraphBuilder<S>) FactoryCreationContext.createBigraphBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
        }
    }

    public static synchronized <S extends Signature> DiscreteIon pureDiscreteIon(S signature, String name, Set<String> outerNames) {
        EPackage bigraphBaseModelPackage = Registry.INSTANCE.getEPackage(signature);
        if (Objects.isNull(bigraphBaseModelPackage)) {
            DiscreteIon b = FactoryCreationContext.createDiscreteIonBuilder(signature, name, outerNames, PureBigraph.class);
            Registry.INSTANCE.put(signature, b.getModelPackage());
            return b;
        } else {
            return FactoryCreationContext.createDiscreteIonBuilder(signature, name, outerNames, bigraphBaseModelPackage, PureBigraph.class);
        }
    }

    public static synchronized <S extends Signature> Placings purePlacings(S signature) {
        EPackage bigraphBaseModelPackage = Registry.INSTANCE.getEPackage(signature);
        if (Objects.isNull(bigraphBaseModelPackage)) {
            Placings b = FactoryCreationContext.createPlacingsBuilder(signature, PureBigraph.class);
            Registry.INSTANCE.put(signature, b.getLoadedModelPackage());
            return b;
        } else {
            return FactoryCreationContext.createPlacingsBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
        }
    }

    public static synchronized <S extends Signature> Linkings pureLinkings(S signature) {
        EPackage bigraphBaseModelPackage = Registry.INSTANCE.getEPackage(signature);
        if (Objects.isNull(bigraphBaseModelPackage)) {
            Linkings b = FactoryCreationContext.createLinkingsBuilder(signature, PureBigraph.class);
            Registry.INSTANCE.put(signature, b.getLoadedModelPackage());
            return b;
        } else {
            return FactoryCreationContext.createLinkingsBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
        }
    }

    public static synchronized <S extends Signature> PureBigraphBuilder<S> pureBuilder(S signature, EPackage bigraphBaseModelPackage) {
        Registry.INSTANCE.put(signature, bigraphBaseModelPackage);
        return (PureBigraphBuilder) FactoryCreationContext.createBigraphBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
    }

    public static synchronized <S extends Signature> PureBigraphBuilder<S> pureBuilder(S signature, String bigraphBaseModelPackageFile) {
        PureBigraphBuilder b = (PureBigraphBuilder) FactoryCreationContext.createBigraphBuilder(signature, bigraphBaseModelPackageFile, PureBigraph.class);
        Registry.INSTANCE.put(signature, b.getLoadedEPackage());
        return b;
    }

    public static synchronized <S extends Signature> PureBigraphGenerator pureRandomBuilder(DefaultDynamicSignature signature) {
        EPackage bigraphBaseModelPackage = Registry.INSTANCE.getEPackage(signature);
        if (Objects.isNull(bigraphBaseModelPackage)) {
            PureBigraphGenerator b = FactoryCreationContext.createRandomBigraphBuilder(signature, PureBigraph.class);
            Registry.INSTANCE.put(signature, b.getModelPackage());
            return b;
        } else {
            return FactoryCreationContext.createRandomBigraphBuilder(signature, bigraphBaseModelPackage, PureBigraph.class);
        }
    }

    public static synchronized <S extends Signature> PureBigraphGenerator pureRandomBuilder(DefaultDynamicSignature signature, EPackage bigraphBaseModelPackage) {
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
