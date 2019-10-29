package de.tudresden.inf.st.bigraphs.rewriting;

import com.google.common.collect.HashBiMap;
import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.BigraphIsNotGroundException;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

/**
 * This helper class creates a unique (canonical) label for a place graph of a bigraph such that two isomorphic place graphs
 * have the same label. This is also known as string representation of a graph. With the implemented canonical forms in
 * this class, we create a unique representation for labelled rooted unordered trees, that are, place graphs without sites.
 * <p>
 * The string representations are the minimal of all these possible breadth-first/depth-first representation according
 * to the lexicographic order (= constraint). This guarantees the uniques of the string representation.
 * <p>
 * This implementation works only for ground bigraphs (i.e. agents).
 * <p>
 * The algorithm used to generate the canonical form is adopted from {@code [1]}. The needed top-down BFS is an implementation
 * described in {@code [2]} (a sequential bottom-up BFS algorithm).
 * <p>
 * <b>References</b>
 * <p>
 * [1] Chi, Y., Yang, Y., Muntz, R.R.: Canonical forms for labelled trees and their applications in frequent subtree mining. Knowl Inf Syst. 8, 203–234 (2005). https://doi.org/10.1007/s10115-004-0180-7.
 * <br>
 * [2] Beamer, S., Asanović, K., Patterson, D.: Direction-optimizing Breadth-first Search. In: Proceedings of the International Conference on High Performance Computing, Networking, Storage and Analysis. pp. 12:1–12:10. IEEE Computer Society Press, Los Alamitos, CA, USA (2012).
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

    //TODO problem with alphabet, consider control "LL" and and control "L". one node "LL" and two nodes "L" and "L"
    // would be same but there aren't!: controls must be atomic!

    /**
     * Build a breadth-first canonical string (BFCS) for a pure bigraph
     * according to the lexicographic order of the control's labels. The representation is unique.
     * <p>
     * //     * The bigraph must be prime, i.e., the place graph must only have one root.
     *
     * @param bigraph the bigraph
     * @param <B>     the type of the bigraph
     * @return the BFCS of the place graph of the given bigraph
     */
    public <B extends Bigraph<?>> String bfcs(B bigraph) {
//        assertBigraphIsPrime(bigraph);
        assertControlsAreAtomic(bigraph);
        final StringBuilder sb = new StringBuilder();

        Supplier<String> rewriteEdgeNameSupplier = createNameSupplier("e");
        HashBiMap<String, BigraphEntity.Edge> E = HashBiMap.create();

        Map<BigraphEntity, BigraphEntity> parentMap = new LinkedHashMap<>();
        List<BigraphEntity> frontier = new LinkedList<>();
        List<BigraphEntity> next = new LinkedList<>();
        BigraphEntity.RootEntity theParent = bigraph.getRoots().iterator().next();
        sb.append('r').append(theParent.getIndex()).append('$');
        parentMap.put(theParent, theParent);
        frontier.add(theParent);

        List<BigraphEntity> places = Stream.concat(bigraph.getNodes().stream(), bigraph.getSites().stream())
                .collect(Collectors.toList());
        while (!frontier.isEmpty()) {
            for (BigraphEntity u : places) { //bigraph.getNodes()) {
                if (parentMap.get(u) == null) {
                    if (u.getType() == BigraphEntityType.SITE) {
                        DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>> defaultDynamicControl = DefaultDynamicControl.createDefaultDynamicControl(StringTypedName.of("" + ((BigraphEntity.SiteEntity) u).getIndex()),
                                FiniteOrdinal.ofInteger(0), ControlKind.ATOMIC);
                        BigraphEntity parent = bigraph.getParent(u);
                        //rewrite parent
                        u = BigraphEntity.createNode(u.getInstance(), defaultDynamicControl);
                        setParentOfNode(u, parent);
                    }
                    for (BigraphEntity v : bigraph.getOpenNeighborhoodOfVertex(u)) {
                        if (frontier.contains(v)) {
                            next.add(u);
                            parentMap.put(u, v);
                            break;
                        }
                    }
                }
            }

            if (next.size() > 0) {
                //sort next by control
                Map<BigraphEntity, Control> collect = next.stream()
                        .sorted(Comparator.comparing(bigraphEntity -> bigraphEntity.getControl().getNamedType().stringValue()))
                        .collect(Collectors.toMap(Function.identity(),
                                BigraphEntity::getControl,
                                (v1, v2) -> v1,
                                LinkedHashMap::new));
                //group by parents
                //in der reihenfolge wie oben: lexicographic "from small to large", and bfs from left to right
                Map<BigraphEntity, LinkedList<Map.Entry<BigraphEntity, Control>>> collect1 = collect.entrySet().stream()
                        .sorted(Comparator.comparing(k -> k.getKey().getControl().getNamedType().stringValue()))
                        .collect(groupingBy(e -> bigraph.getParent(e.getKey()), Collectors.toCollection(LinkedList::new)));
                LinkedHashMap<BigraphEntity, LinkedList<Map.Entry<BigraphEntity, Control>>> collect2 = collect1.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(BigraphCanonicalForm::compareByControl))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                //build string
                collect2.entrySet().stream()
                        .sorted(
//                                this::compareByControlThenChildrenSize
                                (lhs, rhs) -> {
                                    if (lhs.getKey().getControl().getNamedType().stringValue().equals(rhs.getKey().getControl().getNamedType().stringValue())) {
                                        // child count
                                        if (rhs.getValue().size() - lhs.getValue().size() == 0) {
                                            // port count
                                            int count1 = lhs.getValue().stream().map(x -> bigraph.getPorts(x.getKey()).size()).reduce(0, Integer::sum);
                                            int count = rhs.getValue().stream().map(x -> bigraph.getPorts(x.getKey()).size()).reduce(0, Integer::sum);
                                            return (int) (count1 - count);
//                                            if (count1 - count == 0) {
//                                                return lhs.getKey().getControl().getNamedType().stringValue().compareTo(rhs.getKey().getControl().getNamedType().stringValue()); //(int) (count1 - count);
//                                            }
//                                            return bigraph.getPorts(lhs.getKey()).size() - bigraph.getPorts(rhs.getKey()).size();
                                        }
                                        return rhs.getValue().size() - lhs.getValue().size();
                                    } else {
                                        return lhs.getKey().getControl().getNamedType().stringValue().compareTo(rhs.getKey().getControl().getNamedType().stringValue());
                                    }
                                }
                        )
//                        .collect(groupingBy(bigraphEntityLinkedListEntry -> {
//                            if (Objects.isNull(bigraphEntityLinkedListEntry.getKey().getControl()))
//                                return "0";
//                            return bigraphEntityLinkedListEntry.getKey().getControl().getNamedType().stringValue();
////                            return bigraphEntityLinkedListEntry.getValue().stream().map(x -> bigraph.getPorts(x.getKey()).size()).reduce(0, Integer::sum);
//                        }, LinkedHashMap::new, toList()))
//                        .values().stream()
//                        .collect(groupingBy(x -> x % 2 == 0,
//                                groupingBy(x, LinkedHashMap::new, toList()))
//                        )
                        .forEachOrdered(e -> {
                            e.getValue()
//                                    .stream().flatMap(x -> x.getValue().stream())
                                    .stream()
                                    .sorted(
//                                            Comparator.comparing(
//                                                    (Map.Entry<BigraphEntity, Control> k) ->
//                                                            bigraph.getPorts(k.getKey()).size()
//                                            ).reversed()
//                                                    .thenComparing(
//                                                            (Map.Entry<BigraphEntity, Control> k) ->
//                                                                    k.getKey().getControl().getNamedType().stringValue()
//                                                    )
//
                                            Comparator.comparing((Map.Entry<BigraphEntity, Control> k) ->
                                                    k.getKey().getControl().getNamedType().stringValue() //+ "" + bigraph.getPorts(k.getKey()).size()
                                            ).thenComparing(Comparator.comparing((Map.Entry<BigraphEntity, Control> k) ->
                                                    bigraph.getPorts(k.getKey()).size()).reversed())
                                    )
                                    .forEachOrdered(val -> {
                                        sb.append(val.getValue().getNamedType().stringValue());
//                                        String suffix = "";
                                        int num;
                                        if ((num = bigraph.getPorts(val.getKey()).size()) > 0) {
                                            sb.append("{"); //.append(num).append(":");
//                                            String edgeName = E.inverse().get(linkQofF);
                                            bigraph.getPorts(val.getKey()).stream()
                                                    .map(bigraph::getLinkOfPoint)
                                                    .map(l -> {
                                                        if (BigraphEntityType.isEdge(l)) {
                                                            if (E.inverse().get(l) == null) {
                                                                E.put(rewriteEdgeNameSupplier.get(), (BigraphEntity.Edge) l);
                                                            }
//                                                            E.inverse().putIfAbsent((BigraphEntity.Edge) l, rewriteEdgeNameSupplier.get());
                                                            return E.inverse().get(l);
                                                        } else {
                                                            return ((BigraphEntity.OuterName) l).getName();
                                                        }
                                                    })
                                                    .sorted()
                                                    .forEachOrdered(n -> sb.append(n).append("|"));
                                            sb.deleteCharAt(sb.length() - 1);
                                            sb.append("}");
                                        }
                                    });
                            sb.append("$");
                        });
            }
            frontier.clear();
            frontier.addAll(next);
            next.clear();
        }
        if (sb.charAt(sb.length() - 1) == '$') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.insert(sb.length(), "#");

        List<BigraphEntity.InnerName> collect = bigraph.getInnerNames().stream()
                .sorted(Comparator.comparing(BigraphEntity.InnerName::getName))
                .collect(Collectors.toList());
        //TODO: sort inner names
        for (BigraphEntity.InnerName each : bigraph.getInnerNames()) {
            BigraphEntity linkOfPoint = bigraph.getLinkOfPoint(each);
            switch (linkOfPoint.getType()) {
                case EDGE:
                    String name; // = ((BigraphEntity.Edge) linkOfPoint).getName();
                    if (E.inverse().get(linkOfPoint) == null) {
                        E.put(rewriteEdgeNameSupplier.get(), (BigraphEntity.Edge) linkOfPoint);
                    }
                    name = E.inverse().get(linkOfPoint);
                    sb.append(name).append('$').append(each.getName()).append("|");
                    break;
            }
        }
        for (BigraphEntity.InnerName each : bigraph.getInnerNames()) {
            BigraphEntity linkOfPoint = bigraph.getLinkOfPoint(each);
            switch (linkOfPoint.getType()) {
                case OUTER_NAME:
                    String name = ((BigraphEntity.OuterName) linkOfPoint).getName();
                    sb.append(name).append('$').append(each.getName()).append('|');
                    break;
            }
        }
        if (sb.charAt(sb.length() - 1) == '|') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private void setParentOfNode(final BigraphEntity node, final BigraphEntity parent) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        node.getInstance().eSet(prntRef, parent.getInstance()); // child is automatically added to the parent according to the ecore model
    }

    /**
     * the children size is sorted in reversed order where control names sorting is in lexicographic order and has
     * precedence of the children size.
     * <p>
     * This is for map entries where children nodes are grouped by their parents.
     *
     * @param lhs left-hand side
     * @param rhs right-hand side
     * @return integer indicating the ordering
     */
    public int compareByControlThenChildrenSize(Map.Entry<BigraphEntity, LinkedList<Map.Entry<BigraphEntity, Control>>> lhs, Map.Entry<BigraphEntity, LinkedList<Map.Entry<BigraphEntity, Control>>> rhs) {
        if (lhs.getKey().getControl().getNamedType().stringValue().equals(rhs.getKey().getControl().getNamedType().stringValue())) {
//            if (rhs.getValue().size() - lhs.getValue().size() == 0) {
//                return bigraph.getPorts(rhs.getKey()).size();
//            }
            return rhs.getValue().size() - lhs.getValue().size();
        } else {
            return lhs.getKey().getControl().getNamedType().stringValue().compareTo(rhs.getKey().getControl().getNamedType().stringValue());
        }
    }

    private static int compareByControl(LinkedList<Map.Entry<BigraphEntity, Control>> lhs, LinkedList<Map.Entry<BigraphEntity, Control>> rhs) {
        String s1 = lhs.stream().map(x -> x.getValue().getNamedType().stringValue()).sorted().collect(Collectors.joining(""));
        String s2 = rhs.stream().map(x -> x.getValue().getNamedType().stringValue()).sorted().collect(Collectors.joining(""));
        return s1.compareTo(s2);
//        if (s1.equals(s2)) {
//            return rhs.size() - lhs.size();
//        } else {
//            String prefix1 = lhs.size() >= rhs.size() ? "1" : "0";
//            String prefix2 = rhs.size() >= lhs.size() ? "1" : "0";
//            return (prefix1 + s1).compareTo(prefix2 + s2);
//        }
    }

    private <B extends Bigraph<?>> void assertBigraphIsGroundAndPrime(B bigraph) {
        if (!bigraph.isGround() || !bigraph.isPrime()) {
            throw new BigraphIsNotGroundException();
        }
    }

    /**
     * Checks if the alphabet of the signature contains only atomic labels.
     * Otherwise the BFCS method isn't reliable.
     *
     * @param bigraph the bigraph
     * @param <B>     the type of the bigraph
     */
    private <B extends Bigraph<?>> void assertControlsAreAtomic(B bigraph) {
        //TODO: check
    }


    private <B extends Bigraph<?>> void assertBigraphIsPrime(B bigraph) {
        if (!bigraph.isPrime()) {
            throw new BigraphIsNotPrimeException();
        }
    }

    private Supplier<String> createNameSupplier(final String prefix) {
        return new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return prefix + id++;
            }
        };
    }
//    public static int compareByControl(Map.Entry<BigraphEntity, LinkedList<Map.Entry<BigraphEntity, Control>>> lhs, Map.Entry<BigraphEntity, LinkedList<Map.Entry<BigraphEntity, Control>>> rhs) {
//        String s1 = lhs.getValue().stream().map(x -> x.getValue().getNamedType().stringValue()).collect(Collectors.joining(""));
//        String s2 = rhs.getValue().stream().map(x -> x.getValue().getNamedType().stringValue()).collect(Collectors.joining(""));
//        return s1.compareTo(s2);
//    }

//    public <B extends Bigraph<?>> String bfcs3(B bigraph) {
//        getInstance().assertBigraphIsGroundAndPrime(bigraph);
//        final StringBuilder sb = new StringBuilder();
//        BigraphEntity.RootEntity firstRoot = bigraph.getRoots().iterator().next();
//        Traverser<BigraphEntity> childrenTraverser = Traverser.forTree(bigraph::getChildrenOf);
//
//        Map<BigraphEntity, Integer> collect = StreamSupport
//                .stream(childrenTraverser.breadthFirst(firstRoot).spliterator(), false)
//                .collect(Collectors.toMap(p -> p, bigraph::getLevelOf));
//        Integer max = Collections.max(collect.values());
//        for (int i = max; i > 0; i--) {
//            int finalI = i;
//
////                Map<BigraphEntity, Control> collect =
//            LinkedHashMap<BigraphEntity, Control> collect1 = collect.entrySet().stream()
//                    .filter(x2 -> x2.getValue().equals(finalI))
////                        .sorted(Comparator.comparing(bigraphEntity -> bigraphEntity.getKey().getControl().getNamedType().stringValue()))
//                    .map(e -> e.getKey())
//                    .collect(Collectors.toMap(Function.identity(),
//                            BigraphEntity::getControl,
//                            (v1, v2) -> v1,
//                            LinkedHashMap::new));
//            Map<BigraphEntity, LinkedList<Map.Entry<BigraphEntity, Control>>> collect2 = collect1.entrySet().stream()
////                        .sorted(Comparator.comparing(k -> k.getKey().getControl().getNamedType().stringValue())) // keine auswirkung
//                    .collect(Collectors.groupingBy(e -> bigraph.getParent(e.getKey()), Collectors.toCollection(LinkedList::new)));
//
//            collect2.entrySet().stream()
//                    .sorted(Comparator.comparing(k -> k.getKey().getControl().getNamedType().stringValue()))
//                    .forEachOrdered(e -> {
//                        e.getValue().stream()
//                                .sorted(Comparator.comparing(k -> k.getKey().getControl().getNamedType().stringValue()))
//                                .forEachOrdered(val -> sb.append(val.getValue().getNamedType().stringValue()));
//                        sb.append("$");
//                    });
////                List<String> controlList = collect.entrySet().stream()
////                        .filter(x2 -> x2.getValue().equals(finalI))
////                        .map(x3 -> x3.getKey().getControl())
////                        .filter(Objects::nonNull)
////                        .map(x3 -> x3.getNamedType().stringValue()).sorted().collect(Collectors.toList());
////                controlList.stream().forEach(s -> sb.insert(0, s));
////                sb.append("$");
//        }
//        sb.insert(0, "r0$");
////            .collect(Collectors.groupingBy(e -> e, Collectors.toCollection(ArrayList::new)));
//
//
//        if (sb.charAt(sb.length() - 1) == '$') {
//            sb.deleteCharAt(sb.length() - 1);
//        }
//        sb.insert(sb.length(), "#");
//        return sb.toString();
//    }

}
