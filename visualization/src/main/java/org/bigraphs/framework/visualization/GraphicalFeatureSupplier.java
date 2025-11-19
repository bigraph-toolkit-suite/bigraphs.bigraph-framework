/*
 * Copyright (c) 2019-2024 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.visualization;

import java.util.function.Supplier;
import org.bigraphs.framework.core.impl.BigraphEntity;

/**
 * Base abstract supplier class for all graphical attributes of a Graphviz graph.
 *
 * @param <V> attribute type of a style element
 */
public abstract class GraphicalFeatureSupplier<V> implements Supplier<V> {
    private BigraphEntity node;
    protected char delimiterForLabel;

    public GraphicalFeatureSupplier(BigraphEntity node) {
        with(node, ':');
    }

    public GraphicalFeatureSupplier(BigraphEntity node, char delimiterForLabel) {
        with(node, delimiterForLabel);
    }

    public GraphicalFeatureSupplier<V> with(BigraphEntity node) {
        return with(node, ':');
    }

    public GraphicalFeatureSupplier<V> with(BigraphEntity node, char delimiterForLabel) {
        this.node = node;
        this.delimiterForLabel = delimiterForLabel;
        return this;
    }

    protected BigraphEntity getNode() {
        return node;
    }

    @Override
    public abstract V get();
}
