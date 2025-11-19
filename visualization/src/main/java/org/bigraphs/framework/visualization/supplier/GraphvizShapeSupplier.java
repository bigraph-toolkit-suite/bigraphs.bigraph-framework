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

import guru.nidi.graphviz.attribute.Shape;
import org.bigraphs.framework.visualization.DefaultShapeSupplier;

public class GraphvizShapeSupplier extends DefaultShapeSupplier<Shape> {

    private final static Shape DEFAULT_SHAPE = Shape.RECTANGLE;

    @Override
    public Shape get() {
        if ((getNode()) == null) return DEFAULT_SHAPE;
        switch (getNode().getType()) {
            case ROOT:
                return Shape.ELLIPSE;
            case NODE:
                return Shape.RECTANGLE;
            case SITE:
                return Shape.RECTANGLE;
            case INNER_NAME:
            case OUTER_NAME:
                return Shape.RECTANGLE;
            case EDGE:
                return Shape.POINT;
            default:
                return DEFAULT_SHAPE;
        }


    }
}
