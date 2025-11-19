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
package org.bigraphs.framework.core.datatypes;

/**
 * Data type for an element of a name set which is used within an interface of a bigraph.
 * <p>
 * This data type is used for all link types of a bigraph.
 *
 * @param <V> type of the element of such a name set.
 * @author Dominik Grzelak
 */
public interface NamedType<V> {
    V getValue();

    String stringValue();
}
