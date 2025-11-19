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
package org.bigraphs.framework.simulation.encoding;

import org.bigraphs.framework.core.Bigraph;

/**
 * @author Dominik Grzelak
 */
public abstract class BigraphCanonicalFormStrategy<B extends Bigraph<?>> {

    private final BigraphCanonicalForm bigraphCanonicalForm;
    boolean printNodeIdentifiers = false;
    boolean rewriteOpenLinks = false;

    public BigraphCanonicalFormStrategy(BigraphCanonicalForm bigraphCanonicalForm) {
        this.bigraphCanonicalForm = bigraphCanonicalForm;
    }

    public boolean isPrintNodeIdentifiers() {
        return printNodeIdentifiers;
    }

    public BigraphCanonicalFormStrategy<B> setPrintNodeIdentifiers(boolean printNodeIdentifiers) {
        this.printNodeIdentifiers = printNodeIdentifiers;
        return this;
    }

    public boolean isRewriteOpenLinks() {
        return rewriteOpenLinks;
    }

    public BigraphCanonicalFormStrategy<B> setRewriteOpenLinks(boolean rewriteOpenLinks) {
        this.rewriteOpenLinks = rewriteOpenLinks;
        return this;
    }

    public abstract String compute(B bigraph);

    public BigraphCanonicalForm getBigraphCanonicalForm() {
        return bigraphCanonicalForm;
    }
}
