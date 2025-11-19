/*
 * Copyright (c) 2024-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.converter.dot;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import org.bigraphs.framework.converter.ReactionGraphPrettyPrinter;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.jgrapht.Graph;
import org.jgrapht.nio.*;
import org.jgrapht.nio.dot.DOTExporter;

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
