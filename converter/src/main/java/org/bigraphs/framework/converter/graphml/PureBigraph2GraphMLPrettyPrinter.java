package org.bigraphs.framework.converter.graphml;

import org.bigraphs.framework.converter.BigraphPrettyPrinter;
import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.jdom2.Element;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * A pretty printer for bigraphs that need to be encoded as GraphML documents.
 *
 * @author Dominik Grzelak
 */
public class PureBigraph2GraphMLPrettyPrinter implements BigraphPrettyPrinter<PureBigraph> {

    private final String ATTRIBUTE_TYPE_VALUE_ROOT = "root";
    private final String ATTRIBUTE_TYPE_VALUE_NODE = "node";
    private final String ATTRIBUTE_TYPE_VALUE_SITE = "site";
    private final String ATTRIBUTE_TYPE_VALUE_OUTER = "outer";
    private final String ATTRIBUTE_TYPE_VALUE_INNER = "inner";
    private final String ATTRIBUTE_TYPE_VALUE_EDGE = "edge";

    public PureBigraph2GraphMLPrettyPrinter() {

    }

    @Override
    public String toString(PureBigraph bigraph) {
        GraphMLDomBuilder graphMLBuilder = encode(bigraph);
        return graphMLBuilder.printToString();
    }

    @Override
    public void toOutputStream(PureBigraph bigraph, OutputStream outputStream) throws IOException {
        GraphMLDomBuilder graphMLBuilder = encode(bigraph);
        graphMLBuilder.toOutputStream(outputStream);
    }

    private GraphMLDomBuilder encode(PureBigraph bigraph) {
        GraphMLDomBuilder graphMLBuilder = new GraphMLDomBuilder();
        graphMLBuilder.addHeader();

        // Declare attributes first
        graphMLBuilder.addKey("d0", "type", "string");
        graphMLBuilder.addKey("d1", "control", "string");
        graphMLBuilder.addKey("d2", "refName", "string");

        // add place graph first
        bigraph.getRoots().forEach(r -> {
            List<BigraphEntity> visited = new ArrayList<>();
            Queue<BigraphEntity> queue = new ArrayDeque<>();
            visited.add(r);
            queue.add(r);
            graphMLBuilder.addNode(String.valueOf(r.getIndex()), ATTRIBUTE_TYPE_VALUE_ROOT);
            while (!queue.isEmpty()) {
                BigraphEntity poll = queue.poll();
                List<BigraphEntity<?>> children = bigraph.getChildrenOf(poll);
                for (BigraphEntity eachChild : children) {
                    if (!visited.contains(eachChild)) {
                        queue.add(eachChild);
                        visited.add(eachChild);
                        String idSource = "";
                        if (BigraphEntityType.isNode(eachChild)) {
                            BigraphEntity.NodeEntity n = (BigraphEntity.NodeEntity) eachChild;
                            Element nodeElement = graphMLBuilder.addNode(n.getName(),
                                    ATTRIBUTE_TYPE_VALUE_NODE,
                                    n.getControl().getNamedType().stringValue());
                            // add also ports
                            bigraph.getPorts(n).forEach(p -> {
                                graphMLBuilder.addPortToNode(nodeElement, String.valueOf(p.getIndex()));
                            });
                            idSource = n.getName();
                        } else if (BigraphEntityType.isSite(eachChild)) {
                            String id = String.valueOf(((BigraphEntity.SiteEntity) eachChild).getIndex());
                            graphMLBuilder.addNode(
                                    id,
                                    ATTRIBUTE_TYPE_VALUE_SITE
                            );
                            idSource = id;
                        }
                        String idTarget = BigraphEntityType.isRoot(poll) ?
                                String.valueOf(((BigraphEntity.RootEntity) poll).getIndex()) :
                                ((BigraphEntity.NodeEntity) poll).getName();
                        graphMLBuilder.addEdge(
                                String.format("%s:%s", idSource, idTarget),
                                idSource,
                                idTarget
                        );
                    }
                }
            }
        });

        // add outer names as nodes
        bigraph.getOuterNames().forEach(o -> {
            graphMLBuilder.addNode(o.getName(), ATTRIBUTE_TYPE_VALUE_OUTER);
        });
        // add inner names as nodes
        bigraph.getInnerNames().forEach(i -> {
            Element innerNode = graphMLBuilder.addNode(i.getName(), ATTRIBUTE_TYPE_VALUE_INNER);
        });

        // add closed links aka edges as hyperedges
        bigraph.getAllLinks().forEach(l -> {
            Element hyperedge = graphMLBuilder.addHyperedge();
            if (BigraphEntityType.isEdge(l)) {
                graphMLBuilder.addDataAttributeToElement(hyperedge, "d0", ATTRIBUTE_TYPE_VALUE_EDGE);
            } else if (BigraphEntityType.isOuterName(l)) {
                graphMLBuilder.addDataAttributeToElement(hyperedge, "d2", l.getName());
            }
            bigraph.getPointsFromLink(l).forEach(p -> {
                if (BigraphEntityType.isPort(p)) {
                    BigraphEntity.Port port = (BigraphEntity.Port) p;
                    Optional.ofNullable(bigraph.getNodeOfPort(port)).ifPresent(nodeOfPort -> {
                        graphMLBuilder.addEndpointToHyperedge(hyperedge, nodeOfPort.getName(), String.valueOf(port.getIndex()));
                    });
                } else if (BigraphEntityType.isInnerName(p)) {
                    BigraphEntity.InnerName innerName = (BigraphEntity.InnerName) p;
                    graphMLBuilder.addEndpointToHyperedge(hyperedge, innerName.getName(), null);
                }
            });
        });
        return graphMLBuilder;
    }
}
