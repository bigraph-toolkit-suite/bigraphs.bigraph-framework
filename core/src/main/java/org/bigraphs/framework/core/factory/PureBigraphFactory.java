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
package org.bigraphs.framework.core.factory;

import com.google.common.reflect.TypeToken;
import java.util.Set;
import java.util.stream.Collectors;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.datatypes.NamedType;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.impl.elementary.DiscreteIon;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraphComposite;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

/**
 * @author Dominik Grzelak
 */
public class PureBigraphFactory extends AbstractBigraphFactory<DynamicSignature> {

    PureBigraphFactory() {
        super.signatureImplType = new TypeToken<DynamicControl>() {
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
    public PureBigraphBuilder<DynamicSignature> createBigraphBuilder(Signature<?> signature) {
        return PureBigraphBuilder.create(DynamicSignature.class.cast(signature));
    }

    @Override
    public PureBigraphBuilder<DynamicSignature> createBigraphBuilder(Signature<?> signature, EMetaModelData metaModelData) {
        return PureBigraphBuilder.create(DynamicSignature.class.cast(signature), metaModelData);
    }

    @Override
    public PureBigraphBuilder<DynamicSignature> createBigraphBuilder(Signature<?> signature, String metaModelFileName) {
        return PureBigraphBuilder.create(DynamicSignature.class.cast(signature), metaModelFileName);
    }

    @Override
    public PureBigraphBuilder<DynamicSignature> createBigraphBuilder(Signature<?> signature, EPackage bigraphMetaModel) {
        return PureBigraphBuilder.create(DynamicSignature.class.cast(signature), bigraphMetaModel, (EObject) null);
    }

    @Override
    public Placings<DynamicSignature> createPlacings(DynamicSignature signature) {
        return new Placings<>(signature);
    }

    @Override
    public Placings<DynamicSignature> createPlacings(DynamicSignature signature, EPackage bigraphMetaModel) {
        return new Placings<>(signature, bigraphMetaModel);
    }

    @Override
    public Placings<DynamicSignature> createPlacings(DynamicSignature signature, EMetaModelData metaModelData) {
        return new Placings<>(signature, metaModelData);
    }

    @Override
    public Linkings<DynamicSignature> createLinkings(DynamicSignature signature) {
        return new Linkings<>(signature);
    }

    @Override
    public Linkings<DynamicSignature> createLinkings(DynamicSignature signature, EPackage bigraphMetaModel) {
        return new Linkings<>(signature, bigraphMetaModel);
    }

    @Override
    public Linkings<DynamicSignature> createLinkings(DynamicSignature signature, EMetaModelData metaModelData) {
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
    public DiscreteIon<DynamicSignature> createDiscreteIon(NamedType<?> name, Set<NamedType<?>> outerNames, DynamicSignature signature) {
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
     * @see PureBigraphFactory#createDiscreteIon(NamedType, Set, DynamicSignature)
     */
    public DiscreteIon<DynamicSignature> createDiscreteIon(String name, Set<String> outerNames, DynamicSignature signature) {
        return new DiscreteIon<>(
                StringTypedName.of(name),
                outerNames.stream().map(StringTypedName::of).collect(Collectors.toSet()),
                signature,
                this
        );
    }

    public DiscreteIon<DynamicSignature> createDiscreteIon(String name, Set<String> outerNames, DynamicSignature signature, EPackage bigraphMetaModel) {
        return new DiscreteIon<>(
                StringTypedName.of(name),
                outerNames.stream().map(StringTypedName::of).collect(Collectors.toSet()),
                signature,
                bigraphMetaModel,
                this
        );
    }

    @Override
    public PureBigraphComposite<DynamicSignature> asBigraphOperator(Bigraph<DynamicSignature> outerBigraph) {
        return new PureBigraphComposite<>(outerBigraph);
    }

}
