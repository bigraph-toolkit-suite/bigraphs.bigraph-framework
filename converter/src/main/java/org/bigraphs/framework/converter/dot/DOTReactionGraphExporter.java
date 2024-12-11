package org.bigraphs.framework.converter.dot;

import org.bigraphs.framework.converter.ReactionGraphPrettyPrinter;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.jgrapht.Graph;
import org.jgrapht.nio.*;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * DOT file exporter for transition systems of type {@link ReactionGraph}.
 *
 * @author Dominik Grzelak
 */
public class DOTReactionGraphExporter implements ReactionGraphPrettyPrinter<ReactionGraph<?>> {

    @Override
    public String toString(ReactionGraph<?> transitionSystem) {
        Graph<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> graph = transitionSystem.getGraph();
        DOTExporter<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> exporter = new DOTExporter<>();
        exporter.setVertexAttributeProvider((v) -> Map.of("label", DefaultAttribute.createAttribute(v.toString())));
        exporter.setEdgeAttributeProvider((e) -> Map.of("label", DefaultAttribute.createAttribute(e.toString())));
        Writer writer = new StringWriter();
        exporter.exportGraph(graph, writer);
        return writer.toString();
    }

    @Override
    public void toOutputStream(ReactionGraph<?> transitionSystem, OutputStream outputStream) throws IOException {
        String converted = toString(transitionSystem);
        byte[] bytes = converted.getBytes();
        outputStream.write(bytes);
        outputStream.flush();
    }
}
