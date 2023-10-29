package org.bigraphs.framework.converter.jlibbig;

import com.google.common.graph.Traverser;
import org.bigraphs.framework.converter.BigraphObjectEncoder;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import it.uniud.mads.jlibbig.core.attachedProperties.PropertyTarget;
import it.uniud.mads.jlibbig.core.attachedProperties.SimpleProperty;
import it.uniud.mads.jlibbig.core.std.*;
import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.eclipse.collections.impl.factory.BiMaps;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.SortedSets;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JLibBigBigraphEncoder implements BigraphObjectEncoder<it.uniud.mads.jlibbig.core.std.Bigraph, PureBigraph> {

    private Map<String, it.uniud.mads.jlibbig.core.std.OuterName> jLibBigOuterNames = new LinkedHashMap<>();
    private Map<String, it.uniud.mads.jlibbig.core.std.InnerName> jLibBigInnerNames = new LinkedHashMap<>();
    private Map<String, it.uniud.mads.jlibbig.core.std.Edge> jLibBigEdges = new LinkedHashMap<>();
    private Map<Integer, it.uniud.mads.jlibbig.core.std.Root> jLibBigRegions = new LinkedHashMap<>(); //PlaceEntity
    private Map<String, it.uniud.mads.jlibbig.core.std.Node> jLibBigNodes = new LinkedHashMap<>(); // PlaceEntity
    private Map<Integer, it.uniud.mads.jlibbig.core.std.Site> jLibBigSites = new LinkedHashMap<>(); // PlaceEntity
    private it.uniud.mads.jlibbig.core.std.Signature jLibBigSignature;
    private BigraphBuilder builder;
    private Signature signature;

    private MutableBiMap<PlaceEntity, BigraphEntity> jlib2bbigraphNodes = BiMaps.mutable.empty();
    private MutableBiMap<Handle, BigraphEntity.Link> jlib2bbigraphLinks = BiMaps.mutable.empty();

    @Override
    public synchronized Bigraph encode(PureBigraph bigraph) {
        return this.encode(bigraph, parseSignature(bigraph.getSignature()));
    }

    public synchronized Bigraph encode(PureBigraph bigraph, it.uniud.mads.jlibbig.core.std.Signature providedSig) {
        clearAllMaps();

        // Convert the signature and acquire a bigraph builder
        signature = providedSig; //parseSignature(bigraph.getSignature());
        builder = new BigraphBuilder(signature);

        // Parse the inner and outer names
        parseInnerNames(bigraph);
        parseOuterNames(bigraph);

        // Parse the place graph
        parsePlaceGraph(bigraph);
        // Connect the places
        parseLinkGraph(bigraph);

        Bigraph result = builder.makeBigraph(true);
        return result;
    }

    private void parseLinkGraph(PureBigraph bigraph) {
        bigraph.getAllLinks().forEach(l -> {
            AtomicReference<Handle> jLink = new AtomicReference<>();
            if (BigraphEntityType.isOuterName(l)) {
                jLink.set(jLibBigOuterNames.get(l.getName()));
            }

            List<BigraphEntity<?>> pointsFromLink = bigraph.getPointsFromLink(l);
            pointsFromLink.forEach(p -> {
                // Because we cannot create an edge in JLibBig, we have to get the already created edge at that position
                // when the node+port/inner was created
                // so we grab just the first edge that was created in jLibBig and reuse for all other points
                // That is why we have to check in both case if an edge was used for connecting these points
                if (BigraphEntityType.isPort(p)) {
                    BigraphEntity.NodeEntity<DefaultDynamicControl> nodeOfPort = bigraph.getNodeOfPort((BigraphEntity.Port) p);
//                    System.out.format("Node %s is connected to link %s\n", nodeOfPort.getName(), l.getName());
                    // Get the port index of our bigraph node
                    int portIndex = ((BigraphEntity.Port) p).getIndex(); //bigraph.getPorts(nodeOfPort).indexOf(p);
                    PlaceEntity correspondingJNode = jlib2bbigraphNodes.inverse().get(nodeOfPort);
                    assert correspondingJNode != null;

                    if (BigraphEntityType.isEdge(l)) {
                        if (jlib2bbigraphLinks.inverse().get(l) == null) {
                            EditableEdge handle = (EditableEdge) ((Node) correspondingJNode).getPorts().get(portIndex).getEditable().getHandle();
                            handle.setName(((BigraphEntity.Edge) l).getName());
                            jlib2bbigraphLinks.put(handle, l);
                        }
                        jLink.set(jlib2bbigraphLinks.inverse().get(l));
                    }

                    // rewrite link handle
                    ((Node) correspondingJNode).getPorts().get(portIndex).getEditable().setHandle((EditableHandle) jLink.get());
                } else if (BigraphEntityType.isInnerName(p)) {
                    InnerName correspondingJInnerName = jLibBigInnerNames.get(((BigraphEntity.InnerName) p).getName());
                    assert correspondingJInnerName != null;

                    if (BigraphEntityType.isEdge(l)) {
                        if (jlib2bbigraphLinks.inverse().get(l) == null) {
                            EditableEdge handle = (EditableEdge) correspondingJInnerName.getHandle();
                            handle.setName(((BigraphEntity.Edge) l).getName());
                            jlib2bbigraphLinks.put(handle, l);
                        }
                        jLink.set(jlib2bbigraphLinks.inverse().get(l));
                    }

                    // rewrite link handle
                    correspondingJInnerName.getEditable().setHandle((EditableHandle) jLink.get());
                }
            });
        });
    }

    private void parsePlaceGraph(PureBigraph bigraph) {
        Traverser<BigraphEntity<?>> traverser = Traverser.forTree(x -> {
            List<BigraphEntity<?>> children = bigraph.getChildrenOf(x);
//            System.out.format("%s has %d children\n", x.getType(), children.size());
            return children.stream().sorted(
                    Comparator.comparing(lhs -> {
                        if (BigraphEntityType.isSite(lhs)) {
                            return String.valueOf("c" + ((BigraphEntity.SiteEntity) lhs).getIndex());
                        } else if (BigraphEntityType.isRoot(lhs)) {
                            return String.valueOf("a" + ((BigraphEntity.RootEntity) lhs).getIndex());
                        } else {
                            return "b" + ((BigraphEntity.NodeEntity) lhs).getName();
                        }
                    })
            ).collect(Collectors.toList());
//            return children;
        });
        // We define our simple total ordering because of the site index "ordering" imposed by our BBigraph
        // That ensures that the site indices will be re-created in the same order
        Iterable<BigraphEntity<?>> bigraphEntities = traverser.depthFirstPreOrder((List) bigraph.getRoots());
        StreamSupport.stream(bigraphEntities.spliterator(), false)
//                .sorted(
//                        Comparator.comparing(lhs -> {
//                            if (BigraphEntityType.isSite(lhs)) {
//                                return String.valueOf("c" + ((BigraphEntity.SiteEntity) lhs).getIndex());
//                            } else if (BigraphEntityType.isRoot(lhs)) {
//                                return String.valueOf("a" + ((BigraphEntity.RootEntity) lhs).getIndex());
//                            } else {
//                                return "b" + ((BigraphEntity.NodeEntity) lhs).getName();
//                            }
//                        })
//                )
                .forEach(x -> {
//                    System.out.println(x);
                    switch (x.getType()) {
                        case ROOT:
                            Root root = builder.addRoot(((BigraphEntity.RootEntity) x).getIndex());
                            jLibBigRegions.put(((BigraphEntity.RootEntity) x).getIndex(), root);
                            jlib2bbigraphNodes.putIfAbsent(root, x);

                            if (builder.getSites().size() == 0) {
                                MutableSortedSet<BigraphEntity.SiteEntity> siteList = SortedSets.mutable.of(bigraph.getSites().toArray(new BigraphEntity.SiteEntity[0]));
                                for (BigraphEntity.SiteEntity each : siteList) {
                                    Site site = builder.addSite(root);
                                    jlib2bbigraphNodes.putIfAbsent(site, each);
                                    jLibBigSites.put(each.getIndex(), site);
                                }
                            }
                            break;
                        case NODE:
                        case SITE:
                            PlaceEntity jParent = null;
                            BigraphEntity<?> parent = bigraph.getParent(x);
                            if (BigraphEntityType.isRoot(parent)) {
                                jParent = jLibBigRegions.get(((BigraphEntity.RootEntity) parent).getIndex());
                            } else {
                                PlaceEntity correspondingJNode = jlib2bbigraphNodes.inverse().get(parent);
                                assert correspondingJNode != null;
                                jParent = correspondingJNode;
                            }
                            assert jParent != null;

                            if (BigraphEntityType.isNode(x)) {
                                Node node = builder.addNode(((BigraphEntity.NodeEntity) x).getControl().getNamedType().stringValue(), (Parent) jParent);
//                                System.out.println(((BigraphEntity.NodeEntity<?>) x).getAttributes());
                                if (node instanceof PropertyTarget) {
                                    if (((BigraphEntity.NodeEntity<?>) x).getAttributes() != null) {
                                        for (Map.Entry<String, Object> each : ((BigraphEntity.NodeEntity<?>) x).getAttributes().entrySet()) {
                                            try {
                                                if(each.getKey().equals("Owner")) continue;
                                                SimpleProperty<Object> name = new SimpleProperty<>(each.getKey(), true, Collections.emptyList());
                                                name.set(each.getValue());
                                                node.attachProperty(name);
                                            } catch (IllegalArgumentException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        if(((BigraphEntity.NodeEntity<?>) x).getAttributes().get("_id") instanceof String) {
                                            if(node instanceof EditableNode) {
                                                ((EditableNode)node).setName(String.valueOf(((BigraphEntity.NodeEntity<?>) x).getAttributes().get("_id")));
                                            }
                                        }

                                    }
                                }
                                node.getEditable().setName(((BigraphEntity.NodeEntity<?>) x).getName());
                                jlib2bbigraphNodes.putIfAbsent(node, x);
                                jLibBigNodes.put(node.getEditable().getName(), node);
                            } else if (BigraphEntityType.isSite(x)) {
                                //TODO add site at specific index!
//                                jlib2bbigraphNodes.putIfAbsent(site, x);
                                Site site = jLibBigSites.get(((BigraphEntity.SiteEntity) x).getIndex());
                                site.getEditable().setParent(((Parent) jParent).getEditable());//only rewrite parent
//                                Site site = builder.addSite((Parent) jParent);
//                                jlib2bbigraphNodes.putIfAbsent(site, x);
//                                jLibBigSites.put(site.getEditable().getName(), site);
                            }
                            break;
                    }
                });
    }

    private void parseOuterNames(PureBigraph bigraph) {
        bigraph.getOuterNames().stream().sorted(Comparator.comparing(BigraphEntity.OuterName::getName)).forEachOrdered(each -> {
            OuterName outerName = builder.addOuterName(each.getName());
            jLibBigOuterNames.put(outerName.getName(), outerName);
        });
    }

    private void parseInnerNames(PureBigraph bigraph) {
        bigraph.getInnerNames().stream().sorted(Comparator.comparing(BigraphEntity.InnerName::getName)).forEachOrdered(each -> {
            InnerName innerName = builder.addInnerName(each.getName());
            jLibBigInnerNames.put(innerName.getName(), innerName);
        });
    }

    public static it.uniud.mads.jlibbig.core.std.Signature parseSignature(DefaultDynamicSignature sig) {
        MutableList<Control> ctrlList = Lists.mutable.empty();
        for (DefaultDynamicControl eachControl : sig.getControls()) {
            ctrlList.add(createControl(
                            eachControl.getNamedType().stringValue(),
                            eachControl.getArity().getValue(),
                            ControlStatus.isActive(eachControl)
                    )
            );
        }
        it.uniud.mads.jlibbig.core.std.Signature signature = new Signature(ctrlList);
        return signature;
    }

    public static Control createControl(String name, int arity, boolean active) {
        return new Control(name, active, arity);
    }

    private void clearAllMaps() {
        jLibBigOuterNames.clear();
        jLibBigInnerNames.clear();
        jLibBigEdges.clear();
        jLibBigRegions.clear();
        jLibBigNodes.clear();
        jLibBigSites.clear();
        jlib2bbigraphNodes.clear();
        jlib2bbigraphLinks.clear();
    }
}
