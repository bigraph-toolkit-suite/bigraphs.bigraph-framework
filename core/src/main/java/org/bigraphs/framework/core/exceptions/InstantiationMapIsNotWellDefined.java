/*
 * Copyright (c) 2021-2024 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.core.exceptions;

public class InstantiationMapIsNotWellDefined extends InvalidReactionRuleException {

    public InstantiationMapIsNotWellDefined() {
        super("The instantiation map of the parametric reaction rule is not well-defined. Please check whether the map is complete.");
    }
}
