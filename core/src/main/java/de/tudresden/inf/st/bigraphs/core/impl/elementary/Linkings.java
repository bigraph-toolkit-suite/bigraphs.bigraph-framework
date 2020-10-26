package de.tudresden.inf.st.bigraphs.core.impl.elementary;

import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.ElementaryBigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.SortedMaps;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A linking is a node-free bigraph.
 *
 * @param <S> type of the signature
 * @author Dominik Grzelak
 */
public class Linkings<S extends Signature<? extends Control<?, ?>>> implements Serializable {

    private volatile S arbitrarySignature;
    private volatile MutableBuilder<S> mutableBuilder;
    private final EPackage loadedModelPackage;
//    private EObject instanceModel;

    public Linkings<S>.Closure closure(NamedType<?> name) {
        mutableBuilder.reset();
        return new Closure(name);
    }

    public Linkings<S>.Closure closure(Set<NamedType<?>> names) {
        mutableBuilder.reset();
        return new Closure(names);
    }

    public Linkings<S>.Substitution substitution(NamedType<?> nameOuter, NamedType<?>... nameInner) {
        mutableBuilder.reset();
        return new Substitution(nameOuter, nameInner);
    }

    public Linkings<S>.Substitution substitution(NamedType<?> nameOuter, List<NamedType<?>> nameInner) {
        mutableBuilder.reset();
        return new Substitution(nameOuter, nameInner.toArray(new NamedType<?>[0]));
    }

    public Linkings<S>.Identity identity(NamedType<?> name) {
        mutableBuilder.reset();
        return new Identity(name);
    }

    /**
     * Creates an identity link graph from a given name set. Is also a renaming - a bijective substitution.
     *
     * @param nameSet name setl
     * @return the identity link graph created from the given name set
     */
    public Linkings<S>.Identity identity(NamedType<?>... nameSet) {
        mutableBuilder.reset();
        return new Identity(nameSet);
    }

    /**
     * Constructs an empty "identity" link graph.
     *
     * @return an empty link graph of type {@link IdentityEmpty}.
     */
    public Linkings<S>.IdentityEmpty identity_e() {
        mutableBuilder.reset();
        return new IdentityEmpty();
    }

    public Linkings(S signature) {
        this(signature, (EMetaModelData) null);
    }

    public Linkings(S signature, EMetaModelData metaModelData) {
        arbitrarySignature = signature;
        if (Objects.nonNull(metaModelData)) {
            mutableBuilder = PureBigraphBuilder.newMutableBuilder(arbitrarySignature, metaModelData);
        } else {
            mutableBuilder = PureBigraphBuilder.newMutableBuilder(arbitrarySignature);
        }
        loadedModelPackage = mutableBuilder.getLoadedEPackage();
    }

    public Linkings(S signature, EPackage bigraphMetaModel) {
        arbitrarySignature = signature;
        mutableBuilder = PureBigraphBuilder.newMutableBuilder(arbitrarySignature, bigraphMetaModel);
        assert bigraphMetaModel == mutableBuilder.getLoadedEPackage();
        loadedModelPackage = mutableBuilder.getLoadedEPackage();
    }

    /**
     * @param signatureBuilder to create an empty signature of the appropriate type for working with
     *                         user-defined bigraphs of the same type created with the same factory
     */
    public Linkings(SignatureBuilder signatureBuilder) {
        arbitrarySignature = (S) signatureBuilder.createEmpty();
        mutableBuilder = PureBigraphBuilder.newMutableBuilder(arbitrarySignature);
        loadedModelPackage = mutableBuilder.getLoadedEPackage();
    }

    public class IdentityEmpty extends ElementaryBigraph<S> {

        IdentityEmpty() {
            super(null);
            metaModelPackage = loadedModelPackage; //EcoreUtil.copy(loadedModelPackage);
            instanceModel = mutableBuilder.createInstanceModel(metaModelPackage,
                    arbitrarySignature, Collections.emptyMap(), Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap());
        }

        @Override
        public S getSignature() {
            return arbitrarySignature;
        }

        @Override
        public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
            return Lists.mutable.empty();
        }
    }

    public class Identity extends Substitution {

        Identity(NamedType<?>... nameSet) {
            super(nameSet);
        }

        @Override
        public S getSignature() {
            return arbitrarySignature;
        }

        @Override
        public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
            return Collections.emptyList();
        }
    }

    public class Closure extends ElementaryBigraph<S> {
        private final MutableList<BigraphEntity.InnerName> innerNames = Lists.mutable.empty();

        Closure(Set<NamedType<?>> names) {
            super(null);
            MutableSortedMap<String, BigraphEntity.InnerName> innerNameMap = SortedMaps.mutable.empty();
            for (NamedType<?> each : names) {
                BigraphEntity.InnerName x = (BigraphEntity.InnerName) mutableBuilder.createNewInnerName(each.stringValue());
                innerNames.add(x);
                innerNameMap.put(x.getName(), x);
            }
            metaModelPackage = loadedModelPackage; //EcoreUtil.copy(loadedModelPackage);
            instanceModel = mutableBuilder.createInstanceModel(metaModelPackage,
                    arbitrarySignature,
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    innerNameMap,
                    Collections.emptyMap(),
                    Collections.emptyMap());
        }

        Closure(NamedType<?> name) {
            this(Collections.singleton(name));
        }

        @Override
        public Collection<BigraphEntity.InnerName> getInnerNames() {
            return innerNames;
        }

        @Override
        public S getSignature() {
            return arbitrarySignature;
        }

        @Override
        public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
            return Lists.mutable.empty();
        }

        /**
         * Always returns an empty list because a linking has no nodes.
         *
         * @param node argument is not considered
         * @return an empty list
         */
        @Override
        public List<BigraphEntity<?>> getSiblingsOfNode(BigraphEntity<?> node) {
            return Lists.mutable.empty();
        }
    }

    public class Substitution extends ElementaryBigraph<S> {
        private final MutableList<BigraphEntity.InnerName> innerNames;
        private final MutableList<BigraphEntity.OuterName> outerNames;

        Substitution(NamedType<?> outerName, NamedType<?>... innerNames) {
            super(null);
            if (Objects.isNull(outerName) || innerNames.length == 0) {
                throw new RuntimeException("Substitution cannot be created because outer name or inner name is missing.");
            }
            MutableSortedMap<String, BigraphEntity.InnerName> innerNameMap = SortedMaps.mutable.empty();
            MutableSortedMap<String, BigraphEntity.OuterName> outerNameMap = SortedMaps.mutable.empty();
            this.innerNames = Lists.mutable.empty();
            this.outerNames = Lists.mutable.empty();
            BigraphEntity.OuterName newOuterName = (BigraphEntity.OuterName) mutableBuilder.createNewOuterName(outerName.stringValue());
            outerNames.add(newOuterName);
            outerNameMap.put(newOuterName.getName(), newOuterName);
            for (int i = 0, n = innerNames.length; i < n; i++) {
                BigraphEntity.InnerName newInnerName = (BigraphEntity.InnerName) mutableBuilder.createNewInnerName(innerNames[i].stringValue());
                mutableBuilder.connectInnerToOuter(newInnerName, newOuterName);
                this.innerNames.add(newInnerName);
                innerNameMap.put(newInnerName.getName(), newInnerName);
            }

            metaModelPackage = loadedModelPackage; //loadedModelPackage; //EcoreUtil.copy(loadedModelPackage);
            instanceModel = mutableBuilder.createInstanceModel(metaModelPackage,
                    arbitrarySignature, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                    innerNameMap,
                    outerNameMap,
                    Collections.emptyMap());
        }

        Substitution(final NamedType<?>... names) {
            super(null);

            if (names.length == 0) {
                throw new RuntimeException("Renaming (Substitution) cannot be created because names are missing.");
            }
            MutableSortedMap<String, BigraphEntity.InnerName> innerNameMap = SortedMaps.mutable.empty();
            MutableSortedMap<String, BigraphEntity.OuterName> outerNameMap = SortedMaps.mutable.empty();

            this.innerNames = Lists.mutable.empty();
            this.outerNames = Lists.mutable.empty();
            for (int i = 0, n = names.length; i < n; i++) {
                BigraphEntity.OuterName newOuterName = (BigraphEntity.OuterName) mutableBuilder.createNewOuterName(names[i].stringValue());
                BigraphEntity.InnerName newInnerName = (BigraphEntity.InnerName) mutableBuilder.createNewInnerName(names[i].stringValue());
                mutableBuilder.connectInnerToOuter(newInnerName, newOuterName);
                this.outerNames.add(newOuterName);
                this.innerNames.add(newInnerName);
                innerNameMap.put(newInnerName.getName(), newInnerName);
                outerNameMap.put(newOuterName.getName(), newOuterName);
            }
            metaModelPackage = loadedModelPackage; //EcoreUtil.copy(loadedModelPackage);
            instanceModel = mutableBuilder.createInstanceModel(metaModelPackage,
                    arbitrarySignature, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
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
            return arbitrarySignature;
        }

        /**
         * Always returns an empty list because a linking has no nodes.
         *
         * @param node argument is not considered
         * @return an empty list
         */
        @Override
        public List<BigraphEntity<?>> getSiblingsOfNode(BigraphEntity<?> node) {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
            return this.innerNames.stream().filter(x -> !x.equals(innerName)).collect(Collectors.toList());
        }
    }

    public EPackage getLoadedModelPackage() {
        return loadedModelPackage;
    }
}
