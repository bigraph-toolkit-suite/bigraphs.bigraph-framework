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
package org.bigraphs.framework.simulation.matching;

import java.util.Collections;
import java.util.Iterator;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;

/**
 * Custom iterable implementation for matches of type {@link BigraphMatch}.
 *
 * @param <T> type of the bigraph within a {@link BigraphMatch} "container" holding the match result
 * @author Dominik Grzelak
 */
public record MatchIterable<T extends BigraphMatch<? extends Bigraph<?>>>(Iterator<T> iterator) implements Iterable<T> {

    public static <B extends Bigraph<? extends Signature<?>>> MatchIterable<BigraphMatch<B>> emptyMatches() {
        return new MatchIterable<>(Collections.emptyIterator());
    }

    public static <B extends Bigraph<? extends Signature<?>>> MatchIterable<BigraphMatch<B>> singletonMatches(BigraphMatch<B> match) {
        return new MatchIterable<>(Collections.singleton(match).iterator());
    }
}
