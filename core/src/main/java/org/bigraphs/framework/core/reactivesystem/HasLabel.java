/*
 * Copyright (c) 2022-2024 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.core.reactivesystem;

/**
 * Marker interface that indicates the object has a label.
 *
 * @author Dominik Grzelak
 */
public interface HasLabel {
    String getLabel();

    /**
     * Determines if the label is set or not.
     *
     * @return {@code true}, if the label was set, i.e., it is not {@code null} or blank, containing only of whitespaces.
     */
    default boolean isDefined() {
        return getLabel() != null && !getLabel().isBlank();
    }
}
