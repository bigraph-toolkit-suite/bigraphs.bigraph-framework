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
package org.bigraphs.framework.core.impl.signature;

import org.bigraphs.framework.core.ControlBuilder;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;

/**
 * Counterpart to {@link DynamicControlBuilder} for constructing controls
 * in kind signatures, i.e. signatures where a place sort can be specified
 * for each control.
 *
 * @author Dominik Grzelak
 */
public class KindControlBuilder extends ControlBuilder<StringTypedName, FiniteOrdinal<Integer>, KindControlBuilder> {

    protected KindControlBuilder() {
        super();
    }

    @Override
    public KindSignatureBuilder assign() {
        return (KindSignatureBuilder) super.assign();
    }

    public KindControlBuilder identifier(String name) {
        return super.identifier(StringTypedName.of(name));
    }

    public KindControlBuilder arity(Integer arity) {
        return super.arity(FiniteOrdinal.ofInteger(arity));
    }

    @Override
    protected DynamicControl build() {
        return DynamicControl.createDynamicControl(getType(), getArity(), ControlStatus.ACTIVE);
    }
}
