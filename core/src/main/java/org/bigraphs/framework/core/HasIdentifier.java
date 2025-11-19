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
package org.bigraphs.framework.core;

import org.bigraphs.framework.core.datatypes.NamedType;

/**
 * Interface for entities with an identifier.
 * <p>
 * Typical examples include nodes or controls.
 *
 * @param <NT> the named type of the identifier
 * @author Dominik Grzelak
 */
public interface HasIdentifier<NT extends NamedType> {

    NT getNamedType();
}
