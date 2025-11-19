/*
 * Copyright (c) 2024 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.converter.vcg;

import static org.bigraphs.framework.core.BigraphMetaModelConstants.*;

import com.google.common.graph.Traverser;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bigraphs.framework.converter.BigraphPrettyPrinter;
import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;

/**
 * This pretty printer class transforms pure bigraphs of type {@link PureBigraph} to the  Visualization of Compiler Graphs (VCG) format.
 * The tool yComp shipped with GrGen.NET can read and visualize this format.
 *
 * @author Dominik Grzelak
 */
public class VCGTransformator implements BigraphPrettyPrinter<PureBigraph> {
    protected String LINE_SEP = System.getProperty("line.separator");

    @Override
    public String toString(PureBigraph bigraph) {
        return encode(bigraph);
    }

    @Override
    public void toOutputStream(PureBigraph bigraph, OutputStream outputStream) throws IOException {
        String converted = encode(bigraph);
        byte[] bytes = converted.getBytes();
        outputStream.write(bytes);
        outputStream.flush(); // Flush the OutputStream to ensure all data is written
    }

    // node&edge declarations
    String vcgTemplate_Body = "graph:{\n" +
            " infoname 1: \"Attributes\"\n" +
            " display_edge_labels: no\n" +
            " layoutalgorithm: normal //$ \"Compilergraph\"\n" +
            " port_sharing: no\n" +
            " splines: no\n" +
            " manhattan_edges: no\n" +
            " smanhattan_edges: no\n" +
            " orientation: bottom_to_top\n" +
            " edges: yes\n" +
            " nodes: yes\n" +
            "%s\n" +
            "}\n";
    // id, label
    String vcgTemplate_Node = " node:{title:\"%s\" label:\"%s\" info1: \"\" color:yellow bordercolor:darkyellow}";
    String vcgTemplate_Site = " node:{title:\"%s\" label:\"%s\" info1: \"\" color:white bordercolor:darkyellow}";
    // sourceId, targetId, label
    String vcgTemplate_Edge = " edge:{sourcename:\"%s\" targetname:\"%s\" label:\"%s\" color:darkyellow}";

    Supplier<String> createNameSupplier(final String prefix) {
        return new Supplier<>() {
            private int id = 0;

            @Override
            public String get() {
                return prefix + id++;
            }
        };
    }

    private String encode(PureBigraph bigraph) {
        StringBuilder sb = new StringBuilder();

        // (1) Loop through the node hierarchy using a BFS
        // Create all root, site, node and port elements of the graph first.
        // Also establish parent-child relations
        // Store in a map: edge/outer -> port of node (for the second run)
        Map<BigraphEntity.Link, LinkedHashSet<BigraphEntity.Port>> linkPortMap = new HashMap<>();
        Map<BigraphEntity.Link, String> linkNameMap = new HashMap<>();
        Map<BigraphEntity.Port, String> portNameMap = new HashMap<>();
        Traverser<BigraphEntity> traverser = Traverser.forTree(x -> {
            List<BigraphEntity<?>> children = bigraph.getChildrenOf(x);
            return children;
        });
        Iterable<BigraphEntity> bigraphEntities = traverser.breadthFirst(bigraph.getRoots());

        // Place Graph
        // All nodes, roots and sites are created
        // ports are linked to nodes
        bigraphEntities.forEach(x -> {
            if (BigraphEntityType.isRoot(x)) {
                String rootVar = "r" + ((BigraphEntity.RootEntity) x).getIndex();
                sb.append(String.format(vcgTemplate_Node, rootVar, rootVar + ":" + CLASS_ROOT));
                sb.append(LINE_SEP);
            }
            if (BigraphEntityType.isSite(x)) {
                String siteVar = "s" + ((BigraphEntity.SiteEntity) x).getIndex();
                sb.append(String.format(vcgTemplate_Site, siteVar, siteVar + ":" + CLASS_SITE));
                sb.append(LINE_SEP);
            }
            if (BigraphEntityType.isNode(x)) {
                BigraphEntity.NodeEntity nodeEntity = (BigraphEntity.NodeEntity) x;
                sb.append(String.format(vcgTemplate_Node, nodeEntity.getName(), nodeEntity.getName() + ":" + nodeEntity.getControl().getNamedType().stringValue()));
                sb.append(LINE_SEP);

                bigraph.getPorts(nodeEntity)
                        .forEach(p -> {
                            BigraphEntity.Link link = bigraph.getLinkOfPoint(p);
                            if (link != null) {
                                if (!linkPortMap.containsKey(link)) {
                                    linkPortMap.put(link, new LinkedHashSet<>());
                                }
                                linkPortMap.get(link).add(p);
                            }
                        });

                // Create as many Port nodes wrt the control's arity
                for (int i = 0; i < nodeEntity.getControl().getArity().getValue().intValue(); i++) {
                    String portVar = nodeEntity.getName() + "_" + "p" + i;
                    portNameMap.put(bigraph.getPorts(nodeEntity).get(i), portVar);
                    sb.append(String.format(vcgTemplate_Node, portVar, CLASS_PORT));
                    sb.append(LINE_SEP);
                    sb.append(
                            String.format(vcgTemplate_Edge, portVar, nodeEntity.getName(), REFERENCE_NODE)
                    );
                    sb.append(LINE_SEP);
                    sb.append(LINE_SEP);
                }
            }
        });
        // Rerun BFS again to create the parent-child relations now that we know we can access the node variables
        Iterable<BigraphEntity> bigraphEntities2 = traverser.breadthFirst(bigraph.getRoots());
        bigraphEntities2.forEach(x -> {
            String nodeVar = "";
            if (BigraphEntityType.isRoot(x)) {
                nodeVar = "r" + ((BigraphEntity.RootEntity) x).getIndex();
            }
            if (BigraphEntityType.isNode(x)) {
                BigraphEntity.NodeEntity nodeEntity = (BigraphEntity.NodeEntity) x;
                nodeVar = nodeEntity.getName();
            }
            // Create opposite edges bPrnt,bChilds
            for (BigraphEntity<?> each : bigraph.getChildrenOf(x)) {
                if (BigraphEntityType.isNode(each)) {
                    sb.append(
                            String.format(vcgTemplate_Edge, ((BigraphEntity.NodeEntity) each).getName(), nodeVar, REFERENCE_PARENT)
                    );
                    sb.append(LINE_SEP);
                }
                if (BigraphEntityType.isSite(each)) {
                    String siteVar = "s" + ((BigraphEntity.SiteEntity) each).getIndex();
                    sb.append(
                            String.format(vcgTemplate_Edge, siteVar, nodeVar, REFERENCE_PARENT)
                    );
                    sb.append(LINE_SEP);

                }
            }
        });

        // Link Graph
        Set<Map.Entry<BigraphEntity.Link, LinkedHashSet<BigraphEntity.Port>>> entries = linkPortMap.entrySet();
        for (Map.Entry<BigraphEntity.Link, LinkedHashSet<BigraphEntity.Port>> each : entries) {
            BigraphEntity.Link linkElem = each.getKey();
            Set<BigraphEntity.Port> ports = each.getValue();

            String nodeType = BigraphEntityType.isEdge(linkElem) ? CLASS_EDGE : CLASS_OUTERNAME;
            String nodeVar = linkElem.getName();

            linkNameMap.put(linkElem, nodeVar);
            sb.append(
                            String.format(vcgTemplate_Node, nodeVar, nodeVar + ":" + nodeType)
                    )
                    .append(LINE_SEP);
            // Create opposite edges bLink,bPoints
            for (BigraphEntity.Port p : ports) {
                String portVar = portNameMap.get(p);
                sb.append(
                        String.format(vcgTemplate_Edge, portVar, nodeVar, REFERENCE_LINK)
                );
                sb.append(LINE_SEP);
            }
        }
        // Idle outer names
        List<BigraphEntity.OuterName> ol = new ArrayList<>(bigraph.getOuterNames());
        ol.removeAll(entries.stream().map(x -> x.getKey()).collect(Collectors.toList()));
        for (BigraphEntity.OuterName each : ol) {
            sb.append(
                            String.format(vcgTemplate_Node, each.getName(), each.getName() + ":" + CLASS_OUTERNAME)
                    )
                    .append(LINE_SEP);
        }

        // All inner names
        List<BigraphEntity.InnerName> il = new ArrayList<>(bigraph.getInnerNames());
        for (BigraphEntity.InnerName each : il) {
            sb.append(
                            String.format(vcgTemplate_Node, each.getName(), each.getName() + ":" + CLASS_INNERNAME)
                    )
                    .append(LINE_SEP);
            BigraphEntity.Link linkOfPoint = bigraph.getLinkOfPoint(each);
            if (linkOfPoint != null) {
                sb.append(
                        String.format(vcgTemplate_Edge, each.getName(), linkOfPoint.getName(), REFERENCE_LINK)
                );
            }
            sb.append(LINE_SEP);
        }

        String result = String.format(vcgTemplate_Body, sb);
        return result;
    }
}
