package de.tudresden.inf.st.bigraphs.rewriting.encoding;

import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.ControlKind;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.SortedMaps;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
        Supplier<String> rewriteInnerNameSupplier = getBigraphCanonicalForm().createNameSupplier("x");
        Supplier<String> rewriteOuterNameSupplier = getBigraphCanonicalForm().createNameSupplier("y");

//        HashBiMap<String, BigraphEntity.Edge> E = HashBiMap.create();
        MutableSortedMap<String, BigraphEntity.Edge> E2 = SortedMaps.mutable.with();
        MutableSortedMap<String, BigraphEntity.InnerName> I2 = SortedMaps.mutable.with();
        MutableSortedMap<String, BigraphEntity.OuterName> O2 = SortedMaps.mutable.with();

//        Map<BigraphEntity, BigraphEntity> parentMap = new LinkedHashMap<>();
        MutableMap<BigraphEntity, BigraphEntity> parentMap = Maps.mutable.with();
        MutableMap<BigraphEntity, Integer> parentChildMap = Maps.mutable.with();
//        List<BigraphEntity> frontier = new LinkedList<>();
//        List<BigraphEntity> next = new LinkedList<>();
        MutableList<BigraphEntity.OuterName> idleOuterNames = Lists.mutable.empty();
        MutableList<BigraphEntity> frontier = Lists.mutable.empty();
        MutableList<BigraphEntity> next = Lists.mutable.empty();
        BigraphEntity.RootEntity theParent = bigraph.getRoots().iterator().next();
        sb.append('r').append(theParent.getIndex()).append('$');
        parentMap.put(theParent, theParent);
        frontier.add(theParent);


        // rewrite all "idle outer names" first, order is not important
        for (BigraphEntity.OuterName each : bigraph.getOuterNames()) {
            if (bigraph.getPointsFromLink(each).size() == 0 &&
                    !O2.flip().get(each).getFirstOptional().isPresent()) {
                O2.put(rewriteOuterNameSupplier.get(), each);
                idleOuterNames.add(each);
            }
        }

//        List<BigraphEntity> places = Stream.concat(bigraph.getNodes().stream(), bigraph.getSites().stream())
//                .collect(Collectors.toList());
        final AtomicBoolean checkNextRound = new AtomicBoolean(false);
        ImmutableList<BigraphEntity> places = Lists.immutable.fromStream(Stream.concat(bigraph.getNodes().stream(), bigraph.getSites().stream()));
        int maxDegree = bigraph.getOpenNeighborhoodOfVertex(theParent).size();
        final AtomicInteger lastChar = new AtomicInteger(-1);
        LinkedList<BigraphEntity> lastOrdering = new LinkedList<>();
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
                Comparator<Entry<BigraphEntity, LinkedList<BigraphEntity>>> compareControlByKey =
                        Comparator.comparing(entry -> {
                            return entry.getKey().getControl().getNamedType().stringValue() + entry.getValue().size();
                        });
                Comparator<Entry<BigraphEntity, LinkedList<BigraphEntity>>> compareControlByKey2 =
                        Comparator.comparing((entry) -> {
                            String s1 = entry.getValue().stream()
                                    .sorted(Comparator.comparing(lhs -> BigraphEntityType.isSite(lhs) ? String.valueOf(((BigraphEntity.SiteEntity) lhs).getIndex()) : lhs.getControl().getNamedType().stringValue()))
                                    .map(x -> BigraphEntityType.isSite(x) ? String.valueOf(((BigraphEntity.SiteEntity) x).getIndex()) : x.getControl().getNamedType().stringValue())
                                    .collect(Collectors.joining(""));
                            String o = entry.getKey().getControl().getNamedType().stringValue() + s1;
                            return o;
                        });
                Comparator<BigraphEntity> compareControlByKey3 =
                        Comparator.comparing((entry) -> {
//                            System.out.println(entry);
                            String s1 = bigraph.getChildrenOf(entry).stream()
                                    .sorted(Comparator.comparing(lhs -> BigraphEntityType.isSite(lhs) ? String.valueOf(((BigraphEntity.SiteEntity) lhs).getIndex()) : lhs.getControl().getNamedType().stringValue()))
                                    .map(x -> BigraphEntityType.isSite(x) ? String.valueOf(((BigraphEntity.SiteEntity) x).getIndex()) : x.getControl().getNamedType().stringValue())
                                    .collect(Collectors.joining(""));
//                            System.out.println(entry);
                            String o = entry.getControl().getNamedType().stringValue() + s1;
                            return o;
                        });

                Comparator<Entry<BigraphEntity, LinkedList<BigraphEntity>>> compareChildrenSizeByValue =
                        Comparator.comparing(entry -> {
                            return entry.getValue().size();
                        });
                Comparator<Map.Entry<BigraphEntity, LinkedList<BigraphEntity>>> compareChildrenPortSum =
                        Comparator.comparing(entry -> {
                            return (int) entry.getValue().stream().map(x -> bigraph.getPortCount(x)).reduce(0, Integer::sum);
                        });
                //                Comparator<Map.Entry<BigraphEntity, LinkedList<BigraphEntity>>> compareFirstByControlByValue =
//                        Comparator.comparing(entry -> {
//                            return entry.getValue().stream().sorted(controlComp).findAny().get().getControl().getNamedType().stringValue();
//                        });

                Comparator<BigraphEntity> compareControl = Comparator.comparing(entry -> {
                    return entry.getControl().getNamedType().stringValue();
                });
                Comparator<BigraphEntity> compareChildrenSize = Comparator.comparing(entry -> {
                    return bigraph.getChildrenOf(entry).size(); //TODO: or also string concat of all children?
                });

                Comparator<BigraphEntity> comparePortCount = Comparator.comparing(entry -> {
                    return bigraph.getPortCount(entry);
                });


                //group by parents
                //in der reihenfolge wie oben: lexicographic "from small to large", and bfs from left to right
//                Map<BigraphEntity, LinkedList<Map.Entry<BigraphEntity, Control>>> collect1 =
                Map<BigraphEntity, LinkedList<BigraphEntity>> collect = next
                        .stream()
//                        .sorted(compareControl.thenComparing(compareChildrenSize.reversed()))
                        .collect(groupingBy(e -> bigraph.getParent(e), Collectors.toCollection(LinkedList::new)));
                final AtomicInteger atLevelCnt;
                Comparator<Entry<BigraphEntity, LinkedList<BigraphEntity>>> levelComparator = compareControlByKey2.thenComparing(compareChildrenSizeByValue.reversed().thenComparing(compareChildrenPortSum.reversed()));
                Comparator<Entry<BigraphEntity, LinkedList<BigraphEntity>>> levelComparator2 = (compareChildrenPortSum.reversed());
                if (lastOrdering.size() != 0) {
                    atLevelCnt = new AtomicInteger(lastOrdering.size() - collect.size());
                    //order collect as in lastOrdering and order all childs properly
//                    System.out.println(lastOrdering);
                    LinkedHashMap<BigraphEntity, LinkedList<BigraphEntity>> collectTmp = new LinkedHashMap<>();
                    for (BigraphEntity eachOrder : lastOrdering) {
                        if (parentChildMap.get(eachOrder) != null) {
                            collectTmp.put(eachOrder, new LinkedList<>());
                        }
                        if (collect.get(eachOrder) == null) continue;
//                        long distinctLabels = collect.get(eachOrder).stream().map(x -> x.getControl().getNamedType().stringValue()).distinct().count();
                        collectTmp.put(eachOrder, collect.get(eachOrder).stream().sorted(compareControl.thenComparing(compareChildrenSize.reversed().thenComparing(comparePortCount.reversed()))).collect(Collectors.toCollection(LinkedList::new)));
                    }
                    collect = collectTmp;
                    lastOrdering.clear();
                } else {
                    atLevelCnt = new AtomicInteger(0);
                    collect = collect.entrySet()
                            .stream()
//                            .sorted(compareControlByKey2.thenComparing(compareChildrenSizeByValue.reversed().thenComparing(compareChildrenPortSum.reversed())))
                            .sorted(levelComparator)
//                            .peek(x -> lastOrdering.addAll(x.getValue()))
                            .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                                    (e1, e2) -> e1, LinkedHashMap::new));
                }
                ;//thenComparing(compareChildrenSizeByValue)

//                lastOrdering.addAll(collect.values().stream().flatMap(x -> x.stream()).collect(Collectors.toCollection(LinkedList::new)));
//                lastOrdering.addAll(collect.keySet());

                Map<BigraphEntity, LinkedList<BigraphEntity>> blu = collect;
//                .entrySet().stream()
//                        .sorted((compareControlByKey.thenComparing(compareChildrenSizeByValue))) //.thenComparing(compareChildrenPortSum.reversed()
//                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (x, y) -> y, LinkedHashMap::new));

//                Map<BigraphEntity, LinkedList<BigraphEntity>> sortedMap =
//                        collect.entrySet().stream()
//                                .sorted(compareChildrenSizeByValue.thenComparing(compareControlByKey2))
//                                .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
//                                        (e1, e2) -> e1, LinkedHashMap::new));

                MutableList<BigraphEntity> levelList = Lists.mutable.empty();//blu.values().stream().flatMap(x -> x.stream()).collect(Collectors.toCollection(LinkedList::new));
//                blu.entrySet().stream().forEachOrdered(x -> x.getValue().stream().sorted(compareControl).forEach(levelList::add));
                blu.entrySet().stream().forEachOrdered(x -> x.getValue().stream().sorted(compareControlByKey3.thenComparing(compareChildrenSize.reversed().thenComparing(comparePortCount.reversed()))).forEach(levelList::add));
//                Comparator<BigraphEntity> siblingCountComp = Comparator.comparing(entry -> {
//                    return bigraph.getSiblingsOfNode(entry).size();
//                });
//                Comparator<BigraphEntity> childCountComp = Comparator.comparing(entry -> {
//                    return bigraph.getChildrenOf(entry).size();
//                });
                //build string
                boolean allNodesAreLeaves = levelList.summarizeInt((x) -> {
                    return bigraph.getChildrenOf(x).size();
//                    return bigraph.getChildrenOf(x).size() + bigraph.getChildrenOf(y).size();
                }).getSum() == 0;
                final AtomicInteger ixCnt = new AtomicInteger(0);

                blu.entrySet().stream()
                        .sorted(levelComparator2)
                        .forEachOrdered(e -> {
                            if (e.getValue().size() == 0 && parentChildMap.get(e.getKey()) != null) { //
                                sb.append("$");
                                parentChildMap.remove(e.getKey());
                                return;
                            }
                            e.getValue()
                                    .stream()
                                    .sorted(
//                                            siblingCountComp.thenComparing
                                            compareControlByKey3.thenComparing(compareChildrenSize.reversed().thenComparing(comparePortCount.reversed()))
//                                            compareControl2.thenComparing((comparePortCount.reversed()))
//                                            )
                                    )
                                    .forEachOrdered(val -> {
                                        lastOrdering.add(val);
                                        sb.append(val.getControl().getNamedType().stringValue());
                                        //&& e.getValue().size() >= 2
                                        if (!allNodesAreLeaves && bigraph.getChildrenOf(val).size() == 0) { // && bigraph.getSiblingsOfNode(val).size() != 0) {
//                                            parentChildMap.put(val, ixCnt.get());
//                                            parentChildMap.put(val, levelList.indexOf(val));
                                            parentChildMap.put(val, atLevelCnt.get());

                                        }
                                        atLevelCnt.incrementAndGet();
                                        int num;
                                        if ((num = bigraph.getPortCount(val)) > 0) {
                                            sb.append("{"); //.append(num).append(":");
                                            bigraph.getPorts(val).stream()
                                                    .map(bigraph::getLinkOfPoint)
                                                    .map(l -> {
                                                        if (BigraphEntityType.isEdge(l)) {
//                                                            if (E.inverse().get(l) == null) {
//                                                            E.inverse().putIfAbsent((BigraphEntity.Edge) l, rewriteEdgeNameSupplier.get());
//                                                            return E.inverse().get(l);
//                                                        }
                                                            if (!E2.flip().get((BigraphEntity.Edge) l).getFirstOptional().isPresent()) {
//                                                                E.put(rewriteEdgeNameSupplier.get(), (BigraphEntity.Edge) l);
                                                                E2.put(rewriteEdgeNameSupplier.get(), (BigraphEntity.Edge) l);
                                                            }
                                                            return E2.flip().get((BigraphEntity.Edge) l).getOnly();
                                                        } else {
                                                            if (!O2.flip().get((BigraphEntity.OuterName) l).getFirstOptional().isPresent()) {
//                                                                E.put(rewriteEdgeNameSupplier.get(), (BigraphEntity.Edge) l);
                                                                O2.put(rewriteOuterNameSupplier.get(), (BigraphEntity.OuterName) l);
                                                            }
                                                            return O2.flip().get((BigraphEntity.OuterName) l).getOnly();
                                                        }
                                                    })
                                                    .sorted()
                                                    .forEachOrdered(n -> sb.append(n)); //.append("|")
//                                            sb.deleteCharAt(sb.length() - 1);
                                            sb.append("}");
                                        }
                                    });

                            sb.append("$");
                            ixCnt.incrementAndGet();
                        });

                if (parentChildMap.size() != 0) {
                    checkNextRound.set(true);
                }
            }
            frontier.clear();
            frontier.addAll(next);
            next.clear();
        }
        if (sb.charAt(sb.length() - 1) == '$') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.insert(sb.length(), "#");
        //check $# -> #
        int i = sb.lastIndexOf("$#");
        if (i != -1) {
            sb.replace(i, sb.length(), "#");
        }

        //
        // Rest of the Link Encoding concerning the idleness of links, and inner to edge/outer connections
        //

        // first: idle inner names, order is not important
        for (BigraphEntity.InnerName each : bigraph.getInnerNames()) {
            if (Objects.isNull(bigraph.getLinkOfPoint(each)) &&
                    !I2.flip().get(each).getFirstOptional().isPresent()) {
                I2.put(rewriteInnerNameSupplier.get(), each);
            }
        }
        // second: inner names connected to edges, order is important
        MutableList<BigraphEntity.InnerName> innerNames = E2.flatCollect(edge ->
                bigraph.getPointsFromLink(edge).stream().filter(BigraphEntityType::isInnerName)
                        .map(x -> {
                            if (!I2.flip().get((BigraphEntity.InnerName) x).getFirstOptional().isPresent()) {
                                I2.put(rewriteInnerNameSupplier.get(), (BigraphEntity.InnerName) x);
                            }
                            return (BigraphEntity.InnerName) x;
                        })
                        .collect(Collectors.toList())
        );
        // third: inner names connected to outer names, order is important
        MutableList<BigraphEntity.InnerName> innerNames2 = O2.flatCollect(edge ->
                bigraph.getPointsFromLink(edge).stream().filter(BigraphEntityType::isInnerName)
                        .map(x -> {
                            if (!I2.flip().get((BigraphEntity.InnerName) x).getFirstOptional().isPresent()) {
                                I2.put(rewriteInnerNameSupplier.get(), (BigraphEntity.InnerName) x);
                            }
                            return (BigraphEntity.InnerName) x;
                        })
                        .collect(Collectors.toList())
        );
        // first idle inner names, then those which are connected to edges, lastly links from inner to outer
        for (BigraphEntity.InnerName each : I2.values()) {
            BigraphEntity linkOfPoint = bigraph.getLinkOfPoint(each);
            if (Objects.nonNull(linkOfPoint)) {
                String name = null;
                switch (linkOfPoint.getType()) {
                    case EDGE:
                        name = E2.flip().get((BigraphEntity.Edge) linkOfPoint).getOnly();
                        break;
                    case OUTER_NAME:
                        name = O2.flip().get((BigraphEntity.OuterName) linkOfPoint).getOnly();
                        break;
                }
                sb.append(each.getName()).append(name).append("$");
            } else {
                sb.append(each.getName()).append("$");
            }
        }
        for (BigraphEntity.OuterName each : idleOuterNames) {
            sb.append(each.getName()).append("$");
        }
        if (bigraph.getOuterNames().size() > 0 || bigraph.getInnerNames().size() > 0) {
            if (sb.charAt(sb.length() - 1) == '$') {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.insert(sb.length(), "#");
        }

        return sb.toString();
    }
}
