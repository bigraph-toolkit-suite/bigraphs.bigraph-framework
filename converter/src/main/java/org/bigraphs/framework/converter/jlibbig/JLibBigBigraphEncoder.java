/*
 * Copyright (c) 2021-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.converter.jlibbig;

import com.google.common.graph.Traverser;
import it.uniud.mads.jlibbig.core.attachedProperties.PropertyTarget;
import it.uniud.mads.jlibbig.core.attachedProperties.SimpleProperty;
import it.uniud.mads.jlibbig.core.std.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.bigraphs.framework.converter.BigraphObjectEncoder;
import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.eclipse.collections.impl.factory.BiMaps;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.SortedSets;

public class JLibBigBigraphEncoder implements BigraphObjectEncoder<it.uniud.mads.jlibbig.core.std.Bigraph, PureBigraph> {

    private final Map<String, it.uniud.mads.jlibbig.core.std.OuterName> jLibBigOuterNames = new LinkedHashMap<>();
    private final Map<String, it.uniud.mads.jlibbig.core.std.InnerName> jLibBigInnerNames = new LinkedHashMap<>();
    private final Map<String, it.uniud.mads.jlibbig.core.std.Edge> jLibBigEdges = new LinkedHashMap<>();
    private final Map<Integer, it.uniud.mads.jlibbig.core.std.Root> jLibBigRegions = new LinkedHashMap<>(); //PlaceEntity
    private final Map<String, it.uniud.mads.jlibbig.core.std.Node> jLibBigNodes = new LinkedHashMap<>(); // PlaceEntity
    private final Map<Integer, it.uniud.mads.jlibbig.core.std.Site> jLibBigSites = new LinkedHashMap<>(); // PlaceEntity
    private it.uniud.mads.jlibbig.core.std.Signature jLibBigSignature;
    private BigraphBuilder builder;
    private Signature signature;

    private final MutableBiMap<PlaceEntity, BigraphEntity> jlib2bbigraphNodes = BiMaps.mutable.empty();
    private final MutableBiMap<Handle, BigraphEntity.Link> jlib2bbigraphLinks = BiMaps.mutable.empty();

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
                    BigraphEntity.NodeEntity<DynamicControl> nodeOfPort = bigraph.getNodeOfPort((BigraphEntity.Port) p);
//                    System.out.format("Node %s is connected to link %s\n", nodeOfPort.getName(), l.getName());
                    // Get the port index of our bigraph node
                    int portIndex = ((BigraphEntity.Port) p).getIndex(); //bigraph.getPorts(nodeOfPort).indexOf(p);
                    PlaceEntity correspondingJNode = jlib2bbigraphNodes.inverse().get(nodeOfPort);
                    assert correspondingJNode != null;

                    if (BigraphEntityType.isEdge(l)) {
                        if (jlib2bbigraphLinks.inverse().get(l) == null) {
                            EditableEdge handle = (EditableEdge) ((Node) correspondingJNode).getPorts().get(portIndex).getEditable().getHandle();
                            handle.setName(l.getName());
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
                            handle.setName(l.getName());
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
                            return "c" + ((BigraphEntity.SiteEntity) lhs).getIndex();
                        } else if (BigraphEntityType.isRoot(lhs)) {
                            return "a" + ((BigraphEntity.RootEntity) lhs).getIndex();
                        } else {
                            return "b" + ((BigraphEntity.NodeEntity) lhs).getName();
                        }
                    })
            ).collect(Collectors.toList());
        });
        // We define our simple total ordering because of the site index "ordering" imposed by our BBigraph
        // That ensures that the site indices will be re-created in the same order
        Iterable<BigraphEntity<?>> bigraphEntities = traverser.depthFirstPreOrder((List) bigraph.getRoots());
        StreamSupport.stream(bigraphEntities.spliterator(), false)
                .forEach(x -> {
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
                                Site site = jLibBigSites.get(((BigraphEntity.SiteEntity) x).getIndex());
                                site.getEditable().setParent(((Parent) jParent).getEditable());//only rewrite parent
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

    public static it.uniud.mads.jlibbig.core.std.Signature parseSignature(DynamicSignature sig) {
        MutableList<Control> ctrlList = Lists.mutable.empty();
        for (DynamicControl eachControl : sig.getControls()) {
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
