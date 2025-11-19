/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.core.impl.elementary;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import org.bigraphs.framework.core.AbstractEcoreSignature;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.ElementaryBigraph;
import org.bigraphs.framework.core.SignatureBuilder;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.datatypes.NamedType;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.MutableBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.SortedMaps;
import org.eclipse.emf.ecore.EPackage;

/**
 * A linking is a node-free bigraph.
 *
 * @param <S> type of the signature
 * @author Dominik Grzelak
 */
public class Linkings<S extends AbstractEcoreSignature<? extends Control<?, ?>>> implements Serializable {

    private volatile S arbitrarySignature;
    private volatile MutableBuilder<S> mutableBuilder;
    private final EPackage loadedModelPackage;
//    private EObject instanceModel;

    public Linkings<S>.Closure closure(NamedType<?> name) {
        mutableBuilder.reset();
        return new Closure(name);
    }

    public Linkings<DynamicSignature>.Closure closure(String name) {
        mutableBuilder.reset();
        return (Linkings<DynamicSignature>.Closure) new Closure(StringTypedName.of(name));
    }

    public Linkings<S>.Closure closure(Set<NamedType<?>> names) {
        mutableBuilder.reset();
        return new Closure(names);
    }

    public Linkings<S>.Substitution substitution(NamedType<?> nameOuter, NamedType<?>... nameInner) {
        mutableBuilder.reset();
        return new Substitution(nameOuter, nameInner);
    }

    public Linkings<S>.Substitution substitution(String nameOuter, String... nameInner) {
        mutableBuilder.reset();
        return new Substitution(StringTypedName.of(nameOuter), Arrays.stream(nameInner).map(StringTypedName::of).toArray(StringTypedName[]::new));
    }

    public Linkings<S>.Substitution substitution(NamedType<?> nameOuter, List<NamedType<?>> nameInner) {
        mutableBuilder.reset();
        return new Substitution(nameOuter, nameInner.toArray(new NamedType<?>[0]));
    }

    public Linkings<S>.Identity identity(NamedType<?> name) {
        mutableBuilder.reset();
        return new Identity(name);
    }

    public Linkings<S>.Identity identity(String name) {
        mutableBuilder.reset();
        return new Identity(StringTypedName.of(name));
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

    public Linkings<S>.Identity identity(String... nameSet) {
        mutableBuilder.reset();
        return new Identity(Arrays.stream(nameSet).map(StringTypedName::of).toArray(size -> new StringTypedName[nameSet.length]));
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
            mutableBuilder = MutableBuilder.newMutableBuilder(arbitrarySignature, metaModelData);
        } else {
            mutableBuilder = MutableBuilder.newMutableBuilder(arbitrarySignature);
        }
        loadedModelPackage = mutableBuilder.getMetaModel();
    }

    public Linkings(S signature, EPackage bigraphMetaModel) {
        arbitrarySignature = signature;
        mutableBuilder = MutableBuilder.newMutableBuilder(arbitrarySignature, bigraphMetaModel);
        assert bigraphMetaModel == mutableBuilder.getMetaModel();
        loadedModelPackage = mutableBuilder.getMetaModel();
    }

    /**
     * @param signatureBuilder to create an empty signature of the appropriate type for working with
     *                         user-defined bigraphs of the same type created with the same factory
     */
    public Linkings(SignatureBuilder signatureBuilder) {
        arbitrarySignature = (S) signatureBuilder.createEmpty();
        mutableBuilder = MutableBuilder.newMutableBuilder(arbitrarySignature);
        loadedModelPackage = mutableBuilder.getMetaModel();
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
            if ((outerName) == null || innerNames.length == 0) {
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
                throw new RuntimeException("Identity/Renaming cannot be created because names are missing.");
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
