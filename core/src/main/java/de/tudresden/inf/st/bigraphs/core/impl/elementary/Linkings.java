package de.tudresden.inf.st.bigraphs.core.impl.elementary;

import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.ElementaryBigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A linking is a node-free bigraph.
 *
 * @param <S> type of the signature
 * @author Dominik Grzelak
 */
public class Linkings<S extends Signature<? extends Control<?,?>>> implements Serializable {

    private volatile S emptySignature;
    private volatile MutableBuilder<S> mutableBuilder;
    private final EPackage loadedModelPacakge;
    private EObject instanceModel;

    public Linkings<S>.Closure closure(NamedType<?> name) {
        return new Closure(name);
    }

    public Linkings<S>.Substitution substitution(NamedType<?> nameOuter, NamedType<?>... nameInner) {
        return new Substitution(nameOuter, nameInner);
    }

    public Linkings<S>.Identity identity(NamedType<?> name) {
        return new Identity(name);
    }

    /**
     * Creates an identity link graph from a given name set. Is also a renaming - a bijective substitution.
     *
     * @param nameSet name setl
     * @return the identity link graph created from the given name set
     */
    public Linkings<S>.Identity identity(NamedType<?>... nameSet) {
        return new Identity(nameSet);
    }

    /**
     * Constructs an empty "identity" link graph.
     *
     * @return an empty link graph of type {@link IdentityEmpty}.
     */
    public Linkings<S>.IdentityEmpty identity_e() {
        return new IdentityEmpty();
    }

//    @Deprecated
//    public Linkings(AbstractBigraphFactory<S, ?, ?> factory) {
////        AbstractBigraphFactory factory = new PureBigraphFactory<>();
//        SignatureBuilder<?, ?, ?, ?> signatureBuilder = factory.createSignatureBuilder();
//        emptySignature = (S) signatureBuilder.createEmpty();
//        mutableBuilder = PureBigraphBuilder.newMutableBuilder(emptySignature);
//        loadedModelPacakge = mutableBuilder.getLoadedEPackage();
//    }

    public Linkings(S signature) {
        this(signature, null);
    }

    public Linkings(S signature, EMetaModelData metaModelData) {
        emptySignature = signature;
        if (Objects.nonNull(metaModelData)) {
            mutableBuilder = PureBigraphBuilder.newMutableBuilder(emptySignature, metaModelData);
        } else {
            mutableBuilder = PureBigraphBuilder.newMutableBuilder(emptySignature);
        }
        loadedModelPacakge = mutableBuilder.getLoadedEPackage();
    }

    /**
     * @param signatureBuilder to create an empty signature of the appropriate type for working with
     *                         user-defined bigraphs of the same type created with the same factory
     */
    public Linkings(SignatureBuilder signatureBuilder) {
//        AbstractBigraphFactory factory = new PureBigraphFactory<>();
//        SignatureBuilder signatureBuilder = factory.createSignatureBuilder();
        emptySignature = (S) signatureBuilder.createEmpty();
        mutableBuilder = PureBigraphBuilder.newMutableBuilder(emptySignature);
        loadedModelPacakge = mutableBuilder.getLoadedEPackage();
    }

    public class IdentityEmpty extends ElementaryBigraph<S> {

        public IdentityEmpty() {
            super(null);
            instanceModel = mutableBuilder.createInstanceModel(loadedModelPacakge,
                    emptySignature, Collections.emptyMap(), Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap());
        }

        @Override
        public S getSignature() {
            return emptySignature;
        }

        @Override
        public EPackage getModelPackage() {
            return loadedModelPacakge;
        }

        @Override
        public EObject getModel() {
            return instanceModel;
        }
    }

    public class Identity extends Substitution {

        Identity(NamedType<?>... nameSet) {
            super(nameSet);
        }

        @Override
        public S getSignature() {
            return emptySignature;
        }

        @Override
        public EPackage getModelPackage() {
            return loadedModelPacakge;
        }

        @Override
        public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
            return Collections.emptyList();
        }
    }

    public class Closure extends ElementaryBigraph<S> {
        private final BigraphEntity.InnerName x;
        private final Collection<BigraphEntity.InnerName> innerNames = new ArrayList<>(1);

        Closure(NamedType<?> name) {
            super(null);
            HashMap<String, BigraphEntity.InnerName> innerNameMap = new HashMap<>();
            HashMap<String, BigraphEntity.OuterName> outerNameMap = new HashMap<>();
            x = (BigraphEntity.InnerName) mutableBuilder.createNewInnerName(name.stringValue());
            innerNames.add(x);
            innerNameMap.put(x.getName(), x);

            //TODO
            instanceModel = mutableBuilder.createInstanceModel(loadedModelPacakge,
                    emptySignature, Collections.emptyMap(), Collections.emptyMap(),
                    Collections.emptyMap(),
                    innerNameMap,
                    Collections.emptyMap(),
                    Collections.emptyMap());
        }

        @Override
        public Collection<BigraphEntity.InnerName> getInnerNames() {
            return innerNames;
        }

        @Override
        public S getSignature() {
            return emptySignature;
        }

        @Override
        public EPackage getModelPackage() {
            return loadedModelPacakge;
        }

        /**
         * Always returns an empty list because a linking has no nodes.
         *
         * @param node argument is not considered
         * @return an empty list
         */
        @Override
        public Collection<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
            return Collections.EMPTY_LIST;
        }

        @Override
        public EObject getModel() {
            return instanceModel;
        }
    }

    public class Substitution extends ElementaryBigraph<S> {
        private final Collection<BigraphEntity.OuterName> outerNames;
        private final Collection<BigraphEntity.InnerName> innerNames;

        Substitution(NamedType<?> outerName, NamedType<?>... innerNames) {
            super(null);
            if (Objects.isNull(outerName) || innerNames.length == 0) {
                throw new RuntimeException("Substitution cannot be created because outer name or inner name is missing.");
            }
            this.innerNames = new ArrayList<>(innerNames.length);
            this.outerNames = new ArrayList<>(1);
            BigraphEntity.OuterName newOuterName = (BigraphEntity.OuterName) mutableBuilder.createNewOuterName(outerName.stringValue());
            outerNames.add(newOuterName);
            for (int i = 0, n = innerNames.length; i < n; i++) {
                BigraphEntity.InnerName newInnerName = (BigraphEntity.InnerName) mutableBuilder.createNewInnerName(innerNames[i].stringValue());
                mutableBuilder.connectInnerToOuter(newInnerName, newOuterName);
                this.innerNames.add(newInnerName);
            }
        }

        Substitution(final NamedType<?>... names) {
            super(null);

            if (names.length == 0) {
                throw new RuntimeException("Renaming (Substitution) cannot be created because names are missing.");
            }
            HashMap<String, BigraphEntity.InnerName> innerNameMap = new HashMap<>();
            HashMap<String, BigraphEntity.OuterName> outerNameMap = new HashMap<>();

            this.innerNames = new ArrayList<>(names.length);
            this.outerNames = new ArrayList<>(names.length);
            for (int i = 0, n = names.length; i < n; i++) {
                BigraphEntity.OuterName newOuterName = (BigraphEntity.OuterName) mutableBuilder.createNewOuterName(names[i].stringValue());
                BigraphEntity.InnerName newInnerName = (BigraphEntity.InnerName) mutableBuilder.createNewInnerName(names[i].stringValue());
                mutableBuilder.connectInnerToOuter(newInnerName, newOuterName);
                this.outerNames.add(newOuterName);
                this.innerNames.add(newInnerName);
                innerNameMap.put(newInnerName.getName(), newInnerName);
                outerNameMap.put(newOuterName.getName(), newOuterName);
            }

            instanceModel = mutableBuilder.createInstanceModel(loadedModelPacakge,
                    emptySignature, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                    innerNameMap,
                    outerNameMap,
                    Collections.emptyMap());
        }

        @Override
        public Collection<BigraphEntity.InnerName> getInnerNames() {
            return innerNames;
        }

        @Override
        public Collection<BigraphEntity.OuterName> getOuterNames() {
            return outerNames;
        }

        @Override
        public S getSignature() {
            return emptySignature;
        }

        @Override
        public EPackage getModelPackage() {
            return loadedModelPacakge;
        }

        /**
         * Always returns an empty list because a linking has no nodes.
         *
         * @param node argument is not considered
         * @return an empty list
         */
        @Override
        public Collection<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
            return this.innerNames.stream().filter(x -> !x.equals(innerName)).collect(Collectors.toList());
        }

        @Override
        public EObject getModel() {
            return instanceModel;
        }
    }
}
