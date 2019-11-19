package de.tudresden.inf.st.bigraphs.rewriting.encoding;

import com.google.common.collect.HashBiMap;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.ControlKind;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

/**
 * The concrete strategy to compute the canonical string of a pure bigraph ({@link PureBigraph}).
 *
 * @author Dominik Grzelak
 */
public class PureCanonicalForm extends BigraphCanonicalFormStrategy<PureBigraph> {


    public PureCanonicalForm(BigraphCanonicalForm bigraphCanonicalForm) {
        super(bigraphCanonicalForm);
    }

    @Override
    public String compute(PureBigraph bigraph) {
        //        assertBigraphIsPrime(bigraph);
        getBigraphCanonicalForm().assertControlsAreAtomic(bigraph);

        final StringBuilder sb = new StringBuilder();

        Supplier<String> rewriteEdgeNameSupplier = getBigraphCanonicalForm().createNameSupplier("e");
        HashBiMap<String, BigraphEntity.Edge> E = HashBiMap.create();

        Map<BigraphEntity, BigraphEntity> parentMap = new LinkedHashMap<>();
        List<BigraphEntity> frontier = new LinkedList<>();
        List<BigraphEntity> next = new LinkedList<>();
        BigraphEntity.RootEntity theParent = bigraph.getRoots().iterator().next();
        sb.append('r').append(theParent.getIndex()).append('$');
        parentMap.put(theParent, theParent);
        frontier.add(theParent);

        //TODO eclipse collection bfs search impl. vorher nachher messen
        List<BigraphEntity> places = Stream.concat(bigraph.getNodes().stream(), bigraph.getSites().stream())
                .collect(Collectors.toList());
        int maxDegree = bigraph.getOpenNeighborhoodOfVertex(theParent).size();
        while (!frontier.isEmpty()) {
            for (BigraphEntity u : places) { //bigraph.getNodes()) {
                if (parentMap.get(u) == null) {
                    // special case for sites: re-assign label
                    if (u.getType() == BigraphEntityType.SITE) {
                        String newLabel = "" + ((BigraphEntity.SiteEntity) u).getIndex();
                        DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>> defaultDynamicControl =
                                DefaultDynamicControl.createDefaultDynamicControl(StringTypedName.of(newLabel),
                                        FiniteOrdinal.ofInteger(0), ControlKind.ATOMIC);
                        BigraphEntity parent = bigraph.getParent(u);
                        //rewrite parent
                        u = BigraphEntity.createNode(u.getInstance(), defaultDynamicControl);
                        getBigraphCanonicalForm().setParentOfNode(u, parent);
                    }
                    //single-step bottom-up approach
                    List<BigraphEntity> openNeighborhoodOfVertex = bigraph.getOpenNeighborhoodOfVertex(u);
                    if (maxDegree < openNeighborhoodOfVertex.size()) {
                        maxDegree = openNeighborhoodOfVertex.size();
                    }
                    for (BigraphEntity v : openNeighborhoodOfVertex) {
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
                                            int count1 = lhs.getValue().stream().map(x -> bigraph.getPortCount(x.getKey())).reduce(0, Integer::sum);
                                            int count = rhs.getValue().stream().map(x -> bigraph.getPortCount(x.getKey())).reduce(0, Integer::sum);
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
                        .forEachOrdered(e -> {
                            e.getValue()
//                                    .stream().flatMap(x -> x.getValue().stream())
                                    .stream()
                                    .sorted(
                                            Comparator.comparing((Map.Entry<BigraphEntity, Control> k) ->
                                                    k.getValue().getNamedType().stringValue() //+ "" + bigraph.getPorts(k.getKey()).size()
                                            ).thenComparing(Comparator.comparing((Map.Entry<BigraphEntity, Control> k) ->
                                                    bigraph.getPortCount(k.getKey())).reversed())
                                    )
                                    .forEachOrdered(val -> {
                                        sb.append(val.getValue().getNamedType().stringValue());
                                        int num;
                                        if ((num = bigraph.getPortCount(val.getKey())) > 0) {
                                            sb.append("{"); //.append(num).append(":");
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
            if (linkOfPoint.getType() == BigraphEntityType.EDGE) {
                String name; // = ((BigraphEntity.Edge) linkOfPoint).getName();
                if (E.inverse().get(linkOfPoint) == null) {
                    E.put(rewriteEdgeNameSupplier.get(), (BigraphEntity.Edge) linkOfPoint);
                }
                name = E.inverse().get(linkOfPoint);
                sb.append(name).append('$').append(each.getName()).append("|");
            }
        }
        for (BigraphEntity.InnerName each : bigraph.getInnerNames()) {
            BigraphEntity linkOfPoint = bigraph.getLinkOfPoint(each);
            if (linkOfPoint.getType() == BigraphEntityType.OUTER_NAME) {
                String name = ((BigraphEntity.OuterName) linkOfPoint).getName();
                sb.append(name).append('$').append(each.getName()).append('|');
            }
        }
        if (sb.charAt(sb.length() - 1) == '|') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
