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
package org.bigraphs.framework.visualization.supplier;

import guru.nidi.graphviz.attribute.Color;
import org.bigraphs.framework.visualization.DefaultColorSupplier;

public class GraphvizColorSupplier extends DefaultColorSupplier<Color> {
    private final static Color DEFAULT_COLOR = Color.BLACK;

    @Override
    public Color get() {
        if ((getNode()) == null) return DEFAULT_COLOR;
        switch (getNode().getType()) {
            case ROOT:
                return Color.BLACK;
            case NODE:
                return Color.BLACK;
            case SITE:
                return Color.GRAY;
            case INNER_NAME:
                return Color.OLIVEDRAB;
            case OUTER_NAME:
                return Color.GREENYELLOW;
            case EDGE:
                return Color.GREEN;
            default:
                return DEFAULT_COLOR;
        }
    }
}
