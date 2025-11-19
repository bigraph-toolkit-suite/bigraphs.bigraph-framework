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
package org.bigraphs.framework.simulation.modelchecking.predicates;

import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphDecoder;
import org.bigraphs.framework.converter.jlibbig.JLibBigBigraphEncoder;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystemPredicate;
import org.bigraphs.framework.simulation.encoding.BigraphCanonicalForm;
import org.bigraphs.framework.simulation.encoding.hash.BigraphHashFunction;

/**
 * Predicate implementation that returns {@code true} if two bigraphs are isomorphic (i.e., structurally equivalent).
 * Thus, it represents a complete predicate.
 * <p>
 * A predicate can be created by using the static methods the class provides.
 * <p>
 * Bigraph isomorphism is checked using the canonical string encoding.
 *
 * @see SubBigraphMatchPredicate
 * @author Dominik Grzelak
 */
public class BigraphIsoPredicate<B extends Bigraph<? extends Signature<?>>> extends ReactiveSystemPredicate<B> {

    private final B bigraphToMatch;
    private BigraphCanonicalForm canonicalForm;
    private final String bigraphEncoded;
    private BigraphHashFunction<B> hashFunction;

    private BigraphIsoPredicate(B bigraphToMatch) {
        this(bigraphToMatch, false);
    }

    private BigraphIsoPredicate(B bigraphToMatch, boolean negate) {
        it.uniud.mads.jlibbig.core.std.Bigraph encoded = new JLibBigBigraphEncoder().encode((PureBigraph) bigraphToMatch);
        this.bigraphToMatch = (B) new JLibBigBigraphDecoder().decode(encoded);
//        this.bigraphToMatch = bigraphToMatch;
        super.negate = negate;
        this.canonicalForm = BigraphCanonicalForm.createInstance();
        this.hashFunction = (BigraphHashFunction<B>) BigraphHashFunction.get(bigraphToMatch.getClass());
        this.bigraphEncoded = this.canonicalForm.bfcs(this.bigraphToMatch);
    }

    public static <B extends Bigraph<? extends Signature<?>>> BigraphIsoPredicate<B> create(B bigraphToMatch) {
        return new BigraphIsoPredicate<B>(bigraphToMatch);
    }

    public static <B extends Bigraph<? extends Signature<?>>> BigraphIsoPredicate<B> create(B bigraphToMatch, boolean negate) {
        return new BigraphIsoPredicate<B>(bigraphToMatch, negate);
    }

    @Override
    public B getBigraph() {
        return bigraphToMatch;
    }

    @Override
    public boolean test(B agent) {
        B decodedAgent = (B) new JLibBigBigraphDecoder().decode(new JLibBigBigraphEncoder().encode((PureBigraph) agent));
        if (this.hashFunction.hash(decodedAgent) !=
                this.hashFunction.hash(this.bigraphToMatch)) {
            return false;
        }
        String bfcs = this.canonicalForm.bfcs(decodedAgent);
        return bigraphEncoded.equals(bfcs);
    }

    public B getBigraphToMatch() {
        return bigraphToMatch;
    }
}
