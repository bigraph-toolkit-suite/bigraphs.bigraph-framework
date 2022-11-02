package de.tudresden.inf.st.bigraphs.simulation.encoding;

import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.utils.BigraphUtil;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.tudresden.inf.st.bigraphs.simulation.encoding.BigraphCanonicalForm.PREFIX_BARREN;
import static de.tudresden.inf.st.bigraphs.simulation.encoding.BigraphCanonicalFormSupport.createNameSupplier;
import static java.util.stream.Collectors.groupingBy;

/**
 * The concrete strategy to compute the canonical string of a pure bigraph ({@link PureBigraph}).
 *
 * @author Dominik Grzelak
 */
public class PureCanonicalForm extends BigraphCanonicalFormStrategy<PureBigraph> {

    RewriteFunction rewriteFunction = new RewriteFunction();
    TreeSortedMap<String, BigraphEntity.Edge> E2 = new TreeSortedMap<>(); //.mutable.with();
    TreeSortedMap<String, BigraphEntity.InnerName> I2 = new TreeSortedMap<>(); //SortedMaps.mutable.with();
    TreeSortedMap<String, BigraphEntity.OuterName> O2 = new TreeSortedMap<>(); //SortedMaps.mutable.with();
    MutableMap<BigraphEntity<?>, BigraphEntity<?>> parentMap = Maps.mutable.with();
    MutableMap<BigraphEntity<?>, Integer> parentChildMap = Maps.mutable.with();
    MutableList<BigraphEntity.OuterName> idleOuterNames = Lists.mutable.empty();
    MutableList<BigraphEntity<?>> frontier = Lists.mutable.empty();
    MutableList<BigraphEntity<?>> next = Lists.mutable.empty();
    PureBigraph bigraph;
    Supplier<String> rewriteEdgeNameSupplier;
    Supplier<String> rewriteInnerNameSupplier;
    Supplier<String> rewriteOuterNameSupplier;
//    private boolean rewriteLinkNames = false;

    public PureCanonicalForm(BigraphCanonicalForm bigraphCanonicalForm) {
        super(bigraphCanonicalForm);
    }

    private void reset() {
        rewriteFunction = new RewriteFunction();
        E2.clear();
        I2.clear();
        O2.clear();
        parentMap.clear();
        parentChildMap.clear();
        idleOuterNames.clear();
        frontier.clear();
        next.clear();
        bigraph = null;
        rewriteEdgeNameSupplier = createNameSupplier("e");
        rewriteInnerNameSupplier = createNameSupplier("x");
        rewriteOuterNameSupplier = createNameSupplier("y");
    }

    LinkedList<BigraphEntity<?>> totalOrdering = new LinkedList<>();

    @Override
    public synchronized String compute(PureBigraph bigraph) {
        reset();
        this.bigraph = bigraph;
        //        assertBigraphIsPrime(bigraph);
        getBigraphCanonicalForm().assertBigraphHasRoots(bigraph);
        getBigraphCanonicalForm().assertControlsAreAtomic(bigraph);

        final StringBuilder sb = new StringBuilder();

//        if(getBigraphCanonicalForm().withNodeIdentifiers) {
//            exploitSymmetries = true;
//        }

        // prepare the comparators depending on whether to consider symmetries or not (which are made up by the link names somehow)

        final Comparator<BigraphEntity<?>> levelComp2 =
                compareControlByKey3.thenComparing(
                        compareChildrenSize.reversed().thenComparing(
                                comparePortCount.reversed()
                                        .thenComparing(
                                                compareLinkNames.reversed()
                                        )
                        )
                );
        final Comparator<Entry<BigraphEntity<?>, LinkedList<BigraphEntity<?>>>> levelComparator =
                compareControlOfParentAndChildren.thenComparing(
                        compareChildrenSizeByValue.reversed().thenComparing(
                                compareChildrenPortSum.reversed().thenComparing(
                                        compareChildrenLinkNames.reversed()
                                )
                        )
                );


        for (BigraphEntity.RootEntity theParent : bigraph.getRoots()) {
            totalOrdering.clear();
            sb.append(PREFIX_BARREN).append(theParent.getIndex()).append('$');
            parentMap.put(theParent, theParent);
            frontier.clear();
            frontier.add(theParent);

            // rewrite all "idle outer names" first, order is not important
            for (BigraphEntity.OuterName each : bigraph.getOuterNames()) {
                if (bigraph.getPointsFromLink(each).size() == 0 &&
                        O2.flip().get(each).getFirstOptional().isEmpty()) {
                    if (rewriteOpenLinks)
                        O2.put(rewriteOuterNameSupplier.get(), each);
                    else
                        O2.put(each.getName(), each);
                    idleOuterNames.add(each);
                }
            }
            // rewrite all idle inner names first, order is not important
            for (BigraphEntity.InnerName each : bigraph.getInnerNames()) {
                if ((bigraph.getLinkOfPoint(each) == null) &&
                        I2.flip().get(each).getFirstOptional().isEmpty()) {
                    if (rewriteOpenLinks) {
                        I2.put(rewriteInnerNameSupplier.get(), each);
                    } else {
                        I2.put(each.getName(), each);
                    }
                }
            }

            final AtomicBoolean checkNextRound = new AtomicBoolean(false);
//            ImmutableList<BigraphEntity> places = Lists.immutable.fromStream(Stream.concat(bigraph.getNodes().stream(), bigraph.getSites().stream()));
            List<BigraphEntity<?>> places0 = new LinkedList<>();
            places0.addAll(bigraph.getNodes());
            for (BigraphEntity.SiteEntity s : bigraph.getSites()) {
                places0.add(s);
            }
//            places0.addAll((Collection<? extends BigraphEntity<?>>) bigraph.getSites());
            int maxDegree = bigraph.getOpenNeighborhoodOfVertex(theParent).size();
            LinkedList<BigraphEntity<?>> lastOrdering = new LinkedList<>();
            while (!frontier.isEmpty()) {
//                List<BigraphEntity<?>> placesSorted = places0.stream().sorted(levelComp2).collect(Collectors.toList());
                for (BigraphEntity<?> u : places0) {
                    if (parentMap.get(u) == null) {
                        // special case for sites: re-assign label: consider it as a "normal" node with index as label
                        if (u.getType() == BigraphEntityType.SITE) {
                            String newLabel = String.valueOf(((BigraphEntity.SiteEntity) u).getIndex());
                            DefaultDynamicControl defaultDynamicControl =
                                    DefaultDynamicControl.createDefaultDynamicControl(StringTypedName.of(newLabel),
                                            FiniteOrdinal.ofInteger(0), ControlStatus.ATOMIC);
                            BigraphEntity<?> parent = bigraph.getParent(u);
                            //rewrite parent
                            u = BigraphEntity.createNode(u.getInstance(), defaultDynamicControl);
                            BigraphUtil.setParentOfNode(u, parent);
                        }
                        //single-step bottom-up approach
                        List<BigraphEntity<?>> openNeighborhoodOfVertex = bigraph.getOpenNeighborhoodOfVertex(u);
                        if (maxDegree < openNeighborhoodOfVertex.size()) {
                            maxDegree = openNeighborhoodOfVertex.size();
                        }
//                        List<BigraphEntity<?>> openNeighborhoodOfVertexSorted = openNeighborhoodOfVertex.stream().sorted(levelComp2).collect(Collectors.toList());
                        for (BigraphEntity<?> v : openNeighborhoodOfVertex) {
                            if (frontier.contains(v)) {
                                next.add(u);
                                parentMap.put(u, v);
                                break;
                            }
                        }
                    }
                }

                if (next.size() > 0) {

                    // A) Group by parents
                    // in der reihenfolge wie oben: lexicographic "from small to large", and bfs from left to right

                    LinkedList<BigraphEntity<?>> next0 = next
                            .stream()
                            .sorted(levelComp2) //IMPORTANT (NEW@12/2020: ADDED)
                            .collect(Collectors.toCollection(LinkedList::new));
                    Map<BigraphEntity<?>, LinkedList<BigraphEntity<?>>> collect = new LinkedHashMap<>();
                    for (BigraphEntity<?> each : next0) {
                        BigraphEntity<?> p = bigraph.getParent(each);
                        collect.putIfAbsent(p, new LinkedList<>());
                        collect.get(p).add(each);
                    }

                    for (Map.Entry<BigraphEntity<?>, LinkedList<BigraphEntity<?>>> each : collect.entrySet()) {
                        collect.put(each.getKey(), each.getValue().stream().sorted(
//                                compareControlByKey3.thenComparing(
//                                        compareChildrenSize.reversed().thenComparing(
//                                                comparePortCount.reversed()
//                                                        .thenComparing(
//                                                                compareLinkNames.reversed()
//                                                        )
//                                        )
//                                )
                                levelComp2
                        ).collect(Collectors.toCollection(LinkedList::new)));
                    }

                    final AtomicInteger atLevelCnt; // = new AtomicInteger(0);
//                Comparator<Entry<BigraphEntity, LinkedList<BigraphEntity>>> levelComparator2 = (compareChildrenPortSum.reversed());
                    // we must also respect the last ordering of the former parents
                    if (lastOrdering.size() != 0) {
                        atLevelCnt = new AtomicInteger(lastOrdering.size() - collect.size());
                        //order collect as in lastOrdering and order all childs properly
                        LinkedHashMap<BigraphEntity<?>, LinkedList<BigraphEntity<?>>> collectTmp = new LinkedHashMap<>();
                        List<BigraphEntity<?>> skip = new ArrayList<>();
                        for (BigraphEntity<?> eachOrder : lastOrdering) {
                            if (parentChildMap.get(eachOrder) != null) {
                                collectTmp.put(eachOrder, new LinkedList<>());
                            }
                            if (collect.get(eachOrder) == null) continue;

                            boolean processed = false;
                            long distinctLabels = collect.get(eachOrder)
                                    .stream().filter(x -> BigraphEntityType.isNode(x)).map(x -> x.getControl().getNamedType().stringValue()).distinct().count();
                            if (collect.get(eachOrder).size() > 1 && distinctLabels == 1) {
                                LinkedList<BigraphEntity<?>> bigraphEntities = collect.get(((BigraphEntity.NodeEntity) eachOrder));
                                List<BigraphEntity<?>> collect2 = bigraphEntities.stream().sorted(levelComp2).collect(Collectors.toList());
                                List<? extends BigraphEntity<?>> collect1 = collect2.stream().map(x -> bigraph.getParent(x)).distinct().collect(Collectors.toList());
                                if (collect1.size() > 0) {
                                    BigraphEntity<?> eO = null;
                                    for (BigraphEntity<?> t : collect1) {
                                        if (skip.contains(t)) continue;
                                        eO = t;
                                        break;
                                    }
//                                    BigraphEntity<?> eO = collect1.get(0);
//                                    if (skip.contains(eO)) continue;
                                    skip.add(eO);
                                    collectTmp.put(eO, collect.get(eO).stream()
                                            .sorted(
                                                    levelComp2
                                            )
                                            .collect(Collectors.toCollection(LinkedList::new)));
                                    processed = true;
                                }
                            }

                            if (!processed) {
                                collectTmp.put(eachOrder, collect.get(eachOrder).stream()
                                        .sorted(
                                                levelComp2
                                        )
                                        .collect(Collectors.toCollection(LinkedList::new)));
                            }
                        }
                        collect = collectTmp;
                        totalOrdering.addAll(lastOrdering);
                        lastOrdering.clear();
                    } else { // we are in the "first" level or the current level has no children (see below)
                        atLevelCnt = new AtomicInteger(0);
                        collect = collect.entrySet()
                                .stream()
                                .sorted(levelComparator)
                                .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                                        (e1, e2) -> e1, LinkedHashMap::new));
                    }

                    final AtomicInteger ixCnt = new AtomicInteger(0);
                    MutableList<BigraphEntity<?>> levelList = Lists.mutable.empty();
                    Map<BigraphEntity<?>, LinkedList<BigraphEntity<?>>> blu = collect;
                    blu.forEach((key, value) -> levelList.addAll(value));
                    // check if in the next level all nodes are leaves
                    boolean allNodesAreLeaves = levelList.summarizeInt((x) -> {
                        return bigraph.getChildrenOf(x).size();
                    }).getSum() == 0;

                    blu.entrySet().stream()
//                        .sorted(levelComparator)
//                            .sorted(compareChildrenPortSum.reversed()) //IMPORTANT (NEW@12/2020: REMOVED)
                            .forEachOrdered(e -> {
                                if (e.getValue().size() == 0 && parentChildMap.get(e.getKey()) != null) { //
                                    sb.append("$");
                                    parentChildMap.remove(e.getKey());
                                    return;
                                }
                                List<BigraphEntity<?>> childrenOrdered = e.getValue()
                                        .stream()
                                        .sorted(compareByLinkGraphOrdering)
                                        .peek(x -> {
                                            lastOrdering.add(x);
                                            totalOrdering.add(x);
                                        })
                                        .collect(Collectors.toList());

                                childrenOrdered
                                        .stream()
//                                        .sorted(compareByLinkGraphOrdering)
//                                        .sorted(levelComp2) //IMPORTANT (NEW@12/2020: REMOVED)
                                        .forEachOrdered(val -> {
//                                        System.out.println("Val:" + val);
//                                            lastOrdering.add(val);
                                            sb.append(label(val));
                                            if (!allNodesAreLeaves && bigraph.getChildrenOf(val).size() == 0) {
                                                parentChildMap.put(val, atLevelCnt.get());
                                            }
                                            atLevelCnt.incrementAndGet();
                                            if (bigraph.getPortCount((BigraphEntity.NodeEntity) val) > 0) {
                                                sb.append("{"); //.append(num).append(":");
                                                bigraph.getPorts(val).stream().sorted()
                                                        .map(bigraph::getLinkOfPoint)
                                                        .filter(Objects::nonNull)
                                                        .map(l -> {
//                                                            System.out.println(bigraph.getNodeOfPort((BigraphEntity.Port) bigraph.getPointsFromLink(l).get(0)));
                                                            if (rewriteOpenLinks)
                                                                return rewriteFunction.rewrite(E2, O2, l,
                                                                        rewriteEdgeNameSupplier, rewriteOuterNameSupplier, printNodeIdentifiers);
                                                            else
                                                                return rewriteFunction.rewrite(E2, O2, l,
                                                                        rewriteEdgeNameSupplier, null, printNodeIdentifiers);
                                                        })
                                                        .sorted()
                                                        .forEachOrdered(n -> sb.append(n)); //.append("|")
//                                            sb.deleteCharAt(sb.length() - 1);
                                                sb.append("}");
                                            }
                                        });

                                sb.append("$");
                                ixCnt.incrementAndGet();
//                            System.out.println();
//                            O2.values().forEach(x -> System.out.print(x.getName() + ", "));
//                            System.out.println();
//                            O2.keySet().forEach(x -> System.out.print(x + ", "));
//                            System.out.println();
//                            System.out.println();
                            });

                    if (parentChildMap.size() != 0) {
                        checkNextRound.set(true);
                    }
                }
                frontier.clear();
                frontier.addAll(next);
                next.clear();
            }

            cleanUpEndOfEncoding(sb);
        }
        //
        // Rest of the Link Encoding concerning the idleness of links, and inner to edge/outer connections
        //
        // first: idle inner names are already rewritten (see beginning of the algo)
        // second: inner names connected to edges, order is important
        E2.values().forEach(edge ->
                        bigraph.getPointsFromLink(edge).stream().filter(BigraphEntityType::isInnerName)
                                .forEachOrdered(x -> {
                                    if (I2.flip().get((BigraphEntity.InnerName) x).getFirstOptional().isEmpty()) {
                                        if (rewriteOpenLinks) {
                                            I2.put(rewriteInnerNameSupplier.get(), (BigraphEntity.InnerName) x);
                                        } else {
                                            I2.put(((BigraphEntity.InnerName) x).getName(), (BigraphEntity.InnerName) x);
                                        }
                                    }
//                                    return (BigraphEntity.InnerName) x;
                                })
//                                .collect(Collectors.toList())
        );
        // third: inner names connected to outer names, order is important
        O2.values().forEach(edge ->
                        bigraph.getPointsFromLink(edge).stream().filter(BigraphEntityType::isInnerName)
                                .forEachOrdered(x -> {
                                    if (I2.flip().get((BigraphEntity.InnerName) x).getFirstOptional().isEmpty()) {
                                        if (rewriteOpenLinks) {
                                            I2.put(rewriteInnerNameSupplier.get(), (BigraphEntity.InnerName) x);
                                        } else {
                                            I2.put(((BigraphEntity.InnerName) x).getName(), (BigraphEntity.InnerName) x);
                                        }
                                    }
//                                    return (BigraphEntity.InnerName) x;
                                })
//                                .collect(Collectors.toList())
        );
        // first idle inner names, then those which are connected to edges, lastly links from inner to outer
        // Identifiers are already sorted due to the TreeSortedMap structure
        for (BigraphEntity.InnerName each : I2.values()) {
            BigraphEntity.Link linkOfPoint = bigraph.getLinkOfPoint(each);
            if (linkOfPoint != null) {
                String name = null;
                switch (linkOfPoint.getType()) {
                    case EDGE:
//                        name = E2.flip().get((BigraphEntity.Edge) linkOfPoint).getOnly();
                        name = rewriteFunction.labelE(E2, (BigraphEntity.Edge) linkOfPoint);
                        break;
                    case OUTER_NAME:
//                        name = O2.flip().get((BigraphEntity.OuterName) linkOfPoint).getOnly();
                        name = rewriteFunction.labelO(O2, (BigraphEntity.OuterName) linkOfPoint);
                        break;
                }
                sb.append(I2.flip().get(each).getOnly()).append(name).append("$");
            } else {
                sb.append(I2.flip().get(each).getOnly()).append("$");
            }
        }
        // lastly links from inner to outer
        // Identifiers are already sorted due to the TreeSortedMap structure
        for (BigraphEntity.OuterName each : idleOuterNames) {
//            sb.append(O2.flip().get(each).getOnly()).append("$");
            sb.append(rewriteFunction.labelO(O2, each)).append("$");
        }
        if (bigraph.getOuterNames().size() > 0 || bigraph.getInnerNames().size() > 0) {
            if (sb.charAt(sb.length() - 1) == '$') {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.insert(sb.length(), "#");
        }

        return sb.toString().replaceAll("\\$#", "#").replaceAll("##", "#");
    }

    /**
     * Removes invalid combinations such as {@code '$#'} at the end of the string that were introduced due to for-loop
     * behaviour.
     *
     * @param sb the string builder used for storing the encoding
     */
    private void cleanUpEndOfEncoding(StringBuilder sb) {
        if (sb.charAt(sb.length() - 1) == '$') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.insert(sb.length(), "#");
        //check $# -> #
        int i = sb.lastIndexOf("$#");
        if (i != -1) {
            sb.replace(i, sb.length(), "#");
        }
    }

    String getLinkName(PureBigraph bigraph, BigraphEntity<?> node) {
        if (!printNodeIdentifiers) return "";
        List<BigraphEntity.Port> ports = bigraph.getPorts(node);
        if (ports.size() == 0) return "";
        StringBuilder s = new StringBuilder();
        for (BigraphEntity.Port p : ports) {
            if (bigraph.getLinkOfPoint(p) != null) {
                s.append(bigraph.getLinkOfPoint(p).getName());
            }
        }
        return s.toString();
    }

    String label(BigraphEntity<?> val) {
//        if (printNodeIdentifiers) {
//            return val.getControl().getNamedType().stringValue() + ":" + ((BigraphEntity.NodeEntity) val).getName();
//        } else {
        if (BigraphEntityType.isRoot(val)) return "";
        if (BigraphEntityType.isSite(val)) return String.valueOf(((BigraphEntity.SiteEntity) val).getIndex());
        return val.getControl().getNamedType().stringValue();
//        }
    }

//    public String recurs(Comparator<BigraphEntity<?>> levelComp2, BigraphEntity node, PureBigraph bigraph) {
//        bigraph.getChildrenOf(node).stream().sorted(levelComp2)
//        return "";
//    }

    public static class RewriteFunction {
        boolean printNodeIdentifiers;

        public String rewrite(MutableSortedMap<String, BigraphEntity.Edge> E2,
                              MutableSortedMap<String, BigraphEntity.OuterName> O2,
                              BigraphEntity.Link l,
                              Supplier<String> rewriteEdgeNameSupplier,
                              Supplier<String> rewriteOuterNameSupplier,
                              boolean printNodeIdentifiers) {
            this.printNodeIdentifiers = printNodeIdentifiers;
            if (BigraphEntityType.isEdge(l)) {
                if (E2.flip().get((BigraphEntity.Edge) l).getFirstOptional().isEmpty()) {
                    E2.put(rewriteEdgeNameSupplier.get(), (BigraphEntity.Edge) l);
                }
                return labelE(E2, (BigraphEntity.Edge) l);
            } else {
                if (O2.flip().get((BigraphEntity.OuterName) l).getFirstOptional().isEmpty()) {
                    if (rewriteOuterNameSupplier != null)
                        O2.put(rewriteOuterNameSupplier.get(), (BigraphEntity.OuterName) l);
                    else
                        O2.put(l.getName(), (BigraphEntity.OuterName) l);
                }
                return labelO(O2, (BigraphEntity.OuterName) l);
            }
        }

        String labelE(MutableSortedMap<String, BigraphEntity.Edge> map, BigraphEntity.Edge val) {
            if (printNodeIdentifiers) {
                return val.getName();
            } else {
                return map.flip().get(val).getOnly();
            }
        }

        String labelO(MutableSortedMap<String, BigraphEntity.OuterName> map, BigraphEntity.OuterName val) {
            if (printNodeIdentifiers) {
                return val.getName();
            } else {
                return map.flip().get(val).getOnly();
            }
        }
    }

    Comparator<BigraphEntity<?>> compareByLinkGraphOrdering =
            Comparator.comparing((entry) -> {
                        if (BigraphEntityType.isNode(entry) && bigraph.getPortCount((BigraphEntity.NodeEntity) entry) > 0) {
                            List<Integer> collect = bigraph.getPorts(entry).stream().sorted()
                                    .map(bigraph::getLinkOfPoint)
                                    .filter(Objects::nonNull)
                                    .flatMap(x -> bigraph.getPointsFromLink(x).stream())
                                    .filter(BigraphEntityType::isPort)
                                    .map(x -> bigraph.getNodeOfPort((BigraphEntity.Port) x))
                                    .map(x -> totalOrdering.indexOf(x))
                                    .filter(x -> x != -1)
                                    .sorted().collect(Collectors.toList());
//                            System.out.println(collect);
                            Integer reduce = collect.stream().reduce(0, Integer::sum);
                            return reduce;
//
                        }
                        return Integer.MIN_VALUE;
                    }
            );
    Comparator<BigraphEntity<?>> compareControlByKey3 =
            Comparator.comparing((entry) -> {
                Queue<BigraphEntity> queue = new ArrayDeque<>();
                queue.add(entry);
                StringBuilder s1 = new StringBuilder();
                while (!queue.isEmpty()) {
                    BigraphEntity currentNode = queue.remove();
                    List<BigraphEntity<?>> sorted = bigraph.getChildrenOf(currentNode).stream()
                            .sorted(Comparator.comparing(lhs -> BigraphEntityType.isSite(lhs) ?
                                    String.valueOf(((BigraphEntity.SiteEntity) lhs).getIndex()) :
                                    label(lhs) + getLinkName(bigraph, lhs)))
                            .collect(Collectors.toList());
                    queue.addAll(sorted);
                    s1.append(label(currentNode)).append(getLinkName(bigraph, currentNode)).append(sorted.stream().map(x -> BigraphEntityType.isSite(x) ?
                                    String.valueOf(((BigraphEntity.SiteEntity) x).getIndex()) :
                                    label(x) + getLinkName(bigraph, x))
                            .collect(Collectors.joining("")));
                }
                return s1.toString();
            });
    //        Comparator<BigraphEntity<?>> compareControlByKey3 =
//            Comparator.comparing((entry) -> {
//                String s1 = bigraph.getChildrenOf(entry).stream()
//                        .sorted(Comparator.comparing(lhs -> BigraphEntityType.isSite(lhs) ?
//                                String.valueOf(((BigraphEntity.SiteEntity) lhs).getIndex()) :
//                                label(lhs) + getLinkName(bigraph, lhs)))
//                        .map(x -> BigraphEntityType.isSite(x) ?
//                                String.valueOf(((BigraphEntity.SiteEntity) x).getIndex()) :
//                                label(x) + getLinkName(bigraph, x))
//                        .collect(Collectors.joining(""));
//
//                String o = label(entry) + getLinkName(bigraph, entry) + s1;
//                return o;
//            });
    Comparator<Entry<BigraphEntity<?>, LinkedList<BigraphEntity<?>>>> compareControlOfParentAndChildren =
            Comparator.comparing((entry) -> {
                String s1 = entry.getValue().stream()
//                        .sorted(compareControlByKey3)
                        .sorted(Comparator.comparing(lhs -> BigraphEntityType.isSite(lhs) ? String.valueOf(((BigraphEntity.SiteEntity) lhs).getIndex()) : label(lhs) + getLinkName(bigraph, lhs)))
                        .map(x -> BigraphEntityType.isSite(x) ? String.valueOf(((BigraphEntity.SiteEntity) x).getIndex()) : label(x) + getLinkName(bigraph, x))
                        .collect(Collectors.joining(""));

                String o = label(entry.getKey()) + getLinkName(bigraph, entry.getKey()) + s1;
                return o;
            });


    static final Comparator<Entry<BigraphEntity<?>, LinkedList<BigraphEntity<?>>>> compareChildrenSizeByValue =
            Comparator.comparing(entry -> {
                return entry.getValue().size();
            });
    final Comparator<Map.Entry<BigraphEntity<?>, LinkedList<BigraphEntity<?>>>> compareChildrenPortSum =
            Comparator.comparing(entry -> {
                int sum = 0;
                for (int i = 0; i < entry.getValue().size(); i++) {
                    BigraphEntity<?> bigraphEntity = entry.getValue().get(i);
                    sum += bigraph.getPortCount((BigraphEntity.NodeEntity<?>) bigraphEntity);
                }
                return sum;
            });

    final Comparator<BigraphEntity<?>> compareChildrenSize = Comparator.comparing(entry -> {
        return bigraph.getChildrenOf(entry).size(); //TODO: or also string concat of all children?
    });

    final Comparator<BigraphEntity<?>> comparePortCount = Comparator.comparing(entry -> {
        return bigraph.getPortCount((BigraphEntity.NodeEntity<DefaultDynamicControl>) entry);
    });

    final Comparator<BigraphEntity<?>> compareLinkNames = Comparator.comparing(entry -> {
        if (rewriteOpenLinks && !printNodeIdentifiers) return "";
        String collect = bigraph.getPorts(entry).stream().sorted().map(x -> bigraph.getLinkOfPoint(x))
                .map(x -> ((BigraphEntity.Link) x).getName())
                .sorted()
                .collect(Collectors.joining(""));
        return collect;
//        Integer a = bigraph.getPorts(entry).stream()
//                .map(x -> bigraph.getPointsFromLink(x).size()).reduce(0, Integer::sum);
//        return a;
    });

    final Comparator<Map.Entry<BigraphEntity<?>, LinkedList<BigraphEntity<?>>>> compareChildrenLinkNames =
            Comparator.comparing((entry) -> {
//                if (printNodeIdentifiers) {
                String s1 = entry.getValue().stream()
                        .sorted(compareLinkNames)
                        .filter(BigraphEntityType::isNode)
                        .map(x -> bigraph.getLinkOfPoint(x))
                        .filter(Objects::nonNull)
                        .map(BigraphEntity.Link::getName)
                        .sorted()
                        .collect(Collectors.joining(""));
//                            String o = entry.getKey().getControl().getNamedType().stringValue() + s1;
                String o = label(entry.getKey()) + s1;
                return o;
            });


}
