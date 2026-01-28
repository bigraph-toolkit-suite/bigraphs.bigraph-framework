/*
 * Copyright (c) 2019-2026 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.matching;

import java.util.Collection;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;

/**
 * An interface representing a matching engine for bigraphs. This engine
 * is responsible for finding matches of a specific bigraph type (e.g., pure bigraphs, bigraphs with sharing, ...).
 *
 * @param <B> the type of bigraph that extends the generic Bigraph with a
 *            specific {@link Signature}.
 * @author Dominik Grzelak
 */
public interface BigraphMatchingEngine<B extends Bigraph<? extends Signature<?>>> {

    Collection<? extends BigraphMatch<B>> getMatches();
}
