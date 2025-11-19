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

import java.util.Objects;
import java.util.Set;
import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.NamedType;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.factory.AbstractBigraphFactory;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.eclipse.emf.ecore.EPackage;

/**
 * This class represents a discrete ion.
 *
 * @author Dominik Grzelak
 */
public class DiscreteIon<S extends AbstractEcoreSignature<? extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>>>
        extends ElementaryBigraph<S> {
    private volatile PureBigraphBuilder<S> builder;

    public DiscreteIon(NamedType<?> name, Set<NamedType<?>> outerNames, S signature, EPackage bigraphMetamodel, AbstractBigraphFactory<S> factory) {
        super(null);
        builder = Objects.nonNull(bigraphMetamodel) ?
                (PureBigraphBuilder<S>) factory.createBigraphBuilder(signature, bigraphMetamodel) :
                (PureBigraphBuilder<S>) factory.createBigraphBuilder(signature);

        try {
            PureBigraphBuilder<S>.Hierarchy hierarchy = builder.root().child(signature.getControlByName(name.stringValue()));
            outerNames.forEach(x -> {
                try {
                    hierarchy.linkOuter(builder.createOuter(x.stringValue()));
                } catch (TypeNotExistsException | InvalidConnectionException e) {
                    throw new RuntimeException(e);
                }
            });
            if (signature.getControlByName(name.stringValue()).getControlKind() != ControlStatus.ATOMIC)
                hierarchy.down().site();
        } catch (ControlIsAtomicException e) {
            throw new RuntimeException("Control shouldn't be atomic!");
        }
        bigraphDelegate = (Bigraph<S>) builder.create();
    }

    public DiscreteIon(NamedType<?> name, Set<NamedType<?>> outerNames, S signature, AbstractBigraphFactory<S> factory) {
        this(name, outerNames, signature, null, factory);
    }
}
