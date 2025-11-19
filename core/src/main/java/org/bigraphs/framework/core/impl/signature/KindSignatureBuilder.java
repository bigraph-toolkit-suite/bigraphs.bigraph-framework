/*
 * Copyright (c) 2021-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.core.impl.signature;

import java.util.*;
import java.util.stream.Collectors;
import org.bigraphs.framework.core.AbstractEcoreSignature;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.SignatureBuilder;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.builder.ControlNotExistsException;
import org.bigraphs.framework.core.factory.BigraphFactory;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;

/**
 * Builder for kind signatures.
 * <p>
 * Typical usage relies on methods such as {@code addControl},
 * {@code getControl}, or {@code addActiveKindSort}.
 *
 * @author Dominik Grzelak
 */
public class KindSignatureBuilder extends
        SignatureBuilder<StringTypedName, FiniteOrdinal<Integer>, KindControlBuilder, KindSignatureBuilder> {

    MutableMap<String, KindSort> kindSortsMap;

    @Override
    protected KindControlBuilder createControlBuilder() {
        return new KindControlBuilder();
    }

    public KindControlBuilder newControl(String name, int arity) {
        KindControlBuilder builder = createControlBuilder();
        builder.withControlListBuilder(this);
        return builder.identifier(StringTypedName.of(name)).arity(FiniteOrdinal.ofInteger(arity));
    }

    public KindSignatureBuilder addControl(String name, int arity) {
        KindControlBuilder builder = createControlBuilder();
        builder.withControlListBuilder(this);
        return builder.identifier(StringTypedName.of(name)).arity(FiniteOrdinal.ofInteger(arity)).assign();
    }

    private Optional<DynamicControl> getControl(String nameOfCtrl) {
        return getControls().stream().filter(x -> x.getNamedType().stringValue().equals(nameOfCtrl))
                .map(x -> (DynamicControl) x)
                .findFirst();
    }

    /**
     * This methods adds a place-sort for the given control.
     * The status of the control is thus {@code active}, i.e., it is an non-atomic control.
     * <p>
     * If this method is called multiple times with the same argument {@code control}, the new values {@code containingControls}
     * will be used to override the existing sorting for the control {@code control}.
     * <p>
     * If this method is called after, for example, {@link #addPassiveKindSort(String)}, then it will override
     * the previous configuration of being an atomic control.
     *
     * @param control            the control to specify the place-sorts for
     * @param containingControls the controls that can be nested under {@code control}
     * @return the same instance of the kind signature builder
     */
    public KindSignatureBuilder addActiveKindSort(String control, Collection<String> containingControls) throws ControlNotExistsException {
        Optional<DynamicControl> ctrl = getControl(control);
        if (!ctrl.isPresent()) throw new ControlNotExistsException(control);
        initMapIfRequired();
        List<DynamicControl> collect = getControls().stream()
                .filter(x -> containingControls.contains(x.getNamedType().stringValue()))
                .map(x -> (DynamicControl) x)
                .collect(Collectors.toList());
        kindSortsMap.put(control, KindSort.create(ctrl.get(), Lists.mutable.ofAll(collect)));
        return self();
    }

    /**
     * This method adds an empty place-sort for the given control.
     * The status of the control is thus {@code passive}, i.e., it is an atomic control.
     * <p>
     * If this method is called after, for example, {@link #addActiveKindSort(String, Collection)}, then it will override
     * the existing sort and declare the given control as {@code passive}.
     *
     * @param control the control to declare atomic
     * @return
     */
    public KindSignatureBuilder addPassiveKindSort(String control) throws ControlNotExistsException {
        Optional<DynamicControl> ctrl = getControl(control);
        if (!ctrl.isPresent()) throw new ControlNotExistsException(control);
        initMapIfRequired();
        kindSortsMap.put(control, KindSort.create(ctrl.get(), Lists.mutable.empty()));
        return self();
    }

    private void initMapIfRequired() {
        if (Objects.isNull(kindSortsMap)) {
            kindSortsMap = Maps.mutable.empty();
        }
    }

    @Override
    public KindSignature create() {
        if (Objects.isNull(kindSortsMap) || kindSortsMap.isEmpty())
            return (KindSignature) super.create();
        else
            return this.createWith(getControls(), kindSortsMap.values());
    }

    @Override
    public KindSignature create(EMetaModelData metaModelData) {
        if (Objects.isNull(kindSortsMap) || kindSortsMap.isEmpty())
            return (KindSignature) super.create(metaModelData);
        else
            return this.createWith(getControls(), kindSortsMap.values(), metaModelData);
    }

    @Override
    public KindSignature createWith(Iterable<? extends Control<StringTypedName, FiniteOrdinal<Integer>>> controls) {
        KindSignature sig = new KindSignature((Set<DynamicControl>) controls);
        BigraphFactory.createOrGetSignatureMetaModel((AbstractEcoreSignature<?>) sig);
        return sig;
    }

    public KindSignature createWith(Iterable<? extends Control<StringTypedName, FiniteOrdinal<Integer>>> controls,
                                    Collection<KindSort> kindSorts) {
        KindSignature sig = new KindSignature((Set<DynamicControl>) controls, kindSorts);
        BigraphFactory.createOrGetSignatureMetaModel((AbstractEcoreSignature<?>) sig);
        return sig;
    }

    public KindSignature createWith(Iterable<? extends Control<StringTypedName, FiniteOrdinal<Integer>>> controls,
                                    Collection<KindSort> kindSorts, EMetaModelData metaModelData) {
        KindSignature sig = new KindSignature((Set<DynamicControl>) controls, kindSorts);
        BigraphFactory.createOrGetSignatureMetaModel((AbstractEcoreSignature<?>) sig, metaModelData);
        return sig;
    }

    @Override
    public KindSignature createEmpty() {
        return (KindSignature) super.createEmpty();
    }

    @Override
    protected KindSignature createEmptyStub() {
        return new KindSignature(Collections.emptySet());
    }

}
