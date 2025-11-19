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

import java.util.Objects;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

/**
 * A place-sort for a kind signature.
 *
 * @author Dominik Grzelak
 * @see KindSignature
 */
public class KindSort {
    private DynamicControl control;
    private MutableList<DynamicControl> kindsOfControl = Lists.mutable.empty();

    public static KindSort create(DynamicControl control, MutableList<DynamicControl> kindsOfControl) {
        return new KindSort(control, kindsOfControl);
    }

    private KindSort(DynamicControl control, MutableList<DynamicControl> kindsOfControl) {
        this.control = control;
        this.kindsOfControl = kindsOfControl;
    }

    public DynamicControl getControl() {
        return control;
    }

    public MutableList<DynamicControl> getKindsOfControl() {
        return kindsOfControl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KindSort kindSort = (KindSort) o;
        return control.equals(kindSort.control) &&
                kindsOfControl.equals(kindSort.kindsOfControl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(control, kindsOfControl);
    }
}
