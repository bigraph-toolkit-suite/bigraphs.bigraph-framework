package de.tudresden.inf.st.bigraphs.simulation.modelchecking.predicates;

import de.tudresden.inf.st.bigraphs.converter.jlibbig.JLibBigBigraphDecoder;
import de.tudresden.inf.st.bigraphs.converter.jlibbig.JLibBigBigraphEncoder;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactiveSystemPredicate;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.simulation.encoding.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.simulation.encoding.hash.BigraphHashFunction;

/**
 * Predicate implementation that returns {@code true} if two bigraphs are isomorphic (i.e., structurally equivalent).
 * <p>
 * A predicate can be created by using the static methods the class provides.
 * <p>
 * Bigraph isomorphism is checked using the canonical string encoding.
 *
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
