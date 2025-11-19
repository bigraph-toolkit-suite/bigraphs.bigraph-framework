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
package org.bigraphs.framework.core.exceptions;

import java.util.Arrays;

/**
 * In the definition of a reaction, the site of the context must be active, where the part of the redex is rewritten.
 * If this is not the case, this exception is thrown.
 *
 * @author Dominik Grzelak
 */
public class ContextIsNotActive extends Exception {

    public ContextIsNotActive(int siteIx) {
        super("Context is not active at site with index=" + siteIx);
    }

    public ContextIsNotActive(int[] siteIndices) {
        super("Context is not active at site with index=" + Arrays.toString(siteIndices));
    }
}
