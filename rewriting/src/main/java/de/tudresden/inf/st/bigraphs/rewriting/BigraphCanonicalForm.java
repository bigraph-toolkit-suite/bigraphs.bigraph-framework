package de.tudresden.inf.st.bigraphs.rewriting;

import com.google.common.collect.Lists;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.exceptions.BigraphIsNotGroundException;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Create a unique (canonical) label for a place graph of a bigraph such that two isomorphic place graphs have the same label. Also known as
 * string representation of a graph. With the implemented canonical forms in this class, we create a unique representation
 * for labelled rooted unordered trees, that are, place graph without sites.
 * <p>
 * This implementation works only for ground bigraphs (i.e. agents)
 *
 * @author Dominik Grzelak
 */
public class BigraphCanonicalForm {

    private static BigraphCanonicalForm instance = new BigraphCanonicalForm();

    public static BigraphCanonicalForm getInstance() {
        return instance;
    }

    private BigraphCanonicalForm() {
    }

    private <B extends Bigraph<?>> void assertBigraphIsGroundAndPrime(B bigraph) {
        if (!bigraph.isGround() || !bigraph.isPrime()) {
            throw new BigraphIsNotGroundException();
        }
    }

    public static <B extends Bigraph<?>> String bfcf(B bigraph) {
        getInstance().assertBigraphIsGroundAndPrime(bigraph);
        final StringBuilder sb = new StringBuilder();
        sb.append("r0$");
        Set<BigraphEntity> visited = new HashSet<>();
        Traverser<BigraphEntity> childrenTraverser2 = Traverser.forTree(x -> {
            Collection<BigraphEntity> childrenOf = bigraph.getChildrenOf(x);
            if (!visited.contains(x)) {
                childrenOf.stream().map(x3 -> x3.getControl())
                        .filter(Objects::nonNull)
                        .map(x3 -> x3.getNamedType().stringValue()).sorted()
                        .forEach(sb::append);
                if (childrenOf.size() != 0)
                    sb.append("$");
                visited.add(x);
            }
            return childrenOf;
        });


        BigraphEntity.RootEntity firstRoot = bigraph.getRoots().iterator().next();
        Lists.newArrayList(childrenTraverser2.depthFirstPostOrder(firstRoot));
        if (sb.charAt(sb.length() - 1) == '$') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.insert(sb.length(), "#");
        return sb.toString();
    }
//    Traverser<BigraphEntity> childrenTraverser = Traverser.forTree(bigraph::getChildrenOf);
//        if (false) {
//        Map<BigraphEntity, Integer> collect = StreamSupport
//                .stream(childrenTraverser.breadthFirst(firstRoot).spliterator(), false)
//                .collect(Collectors.toMap(p -> p, bigraph::getLevelOf));
//        Integer max = Collections.max(collect.values());
//        for (int i = max; i > 0; i--) {
//            int finalI = i;
//            List<String> controlList = collect.entrySet().stream()
//                    .filter(x2 -> x2.getValue().equals(finalI))
//                    .map(x3 -> x3.getKey().getControl())
//                    .filter(Objects::nonNull)
//                    .map(x3 -> x3.getNamedType().stringValue()).sorted().collect(Collectors.toList());
//            controlList.stream().forEach(s -> sb.insert(0, s));
//            sb.append("$");
//        }
//        sb.insert(0, "r0$");
//            .collect(Collectors.groupingBy(e -> e, Collectors.toCollection(ArrayList::new)));
//    }
}
