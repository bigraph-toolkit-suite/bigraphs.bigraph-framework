/*
 * Copyright (c) 2023-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.visualization;


import java.awt.*;
import java.io.*;
import java.util.function.Supplier;
import org.bigraphs.framework.core.Bigraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class GraphStreamTest {
    static final String DUMP_PATH = "src/test/resources/dump/graphstream/random-graphs";

    @BeforeEach
    void setUp() {
        System.setProperty("java.awt.headless", "false");
        System.setProperty("org.graphstream.ui", "swing");
    }

    @Test
    @Disabled
    public void test_graphStream_randomBigraph() throws InterruptedException {
//        Graph graph = new SingleGraph("Random Bigraph 1");
////        graph.setAttribute("ui.stylesheet", "node.control { fill-color: red; } node.root {fill-color: blue;} node.site {fill-color: gray;}");
//        InputStream styleStream = GraphStreamTest.class.getResourceAsStream("/graphStreamStyleDark.css");
//        String style = new BufferedReader(new InputStreamReader(styleStream))
//                .lines()
//                .collect(Collectors.joining("\n"));
//        graph.setAttribute("ui.stylesheet", style);
//        Viewer viewer = graph.display();
////        viewer.disableAutoLayout();
//
//        Supplier<String> edgeNameSupplier = createNameSupplier("e");
//        DemoBigraphProvider provider = new DemoBigraphProvider();
////        PureBigraph demoBig = provider.getRandomBigraphSingleRoot(15, 0);
//        PureBigraph demoBig = provider.getRandomBigraphMultipleRoots(3, 20);
//        eb(demoBig, "graphstream-random-1", DUMP_PATH);
//        Traverser<BigraphEntity> traverser = Traverser.forTree(x -> {
//            List<BigraphEntity<?>> children = demoBig.getChildrenOf(x);
//            System.out.format("%s has %d children\n", x.getType(), children.size());
//            if (children.size() > 0)
//                System.out.println("Level: " + demoBig.getLevelOf(children.get(0)));
//            return children;
//        });
//        Iterable<BigraphEntity> bigraphEntities = traverser.breadthFirst(demoBig.getRoots());
//        bigraphEntities.forEach(x -> {
//            System.out.println(x);
//            String id = getUniqueIdOfBigraphEntity(x);
//            assert !Objects.equals(id, "");
//            Node gsNode = graph.addNode(id);
//            switch (x.getType()) {
//                case ROOT -> {
//                    gsNode.setAttribute("ui.class", "root");
//                    // only when auto-layout is disabled
////                    gsNode.setAttribute("xyz", ((BigraphEntity.RootEntity)x).getIndex()+1, 1, 0);
//                }
//                case NODE -> {
//                    gsNode.setAttribute("ui.class", "control");
//                }
//                case SITE -> {
//                    gsNode.setAttribute("ui.class", "site");
//                }
//            }
//
//            BigraphEntity<?> prntNode = demoBig.getParent(x);
//            if (prntNode != null) {
//                String idPrnt = getUniqueIdOfBigraphEntity(prntNode);
//                assert !Objects.equals(idPrnt, "");
//                graph.addEdge(edgeNameSupplier.get(), id, idPrnt);
//
//            }
//        });
//
//        while (true) {
//            Thread.sleep(250);
//        }
    }

    void eb(Bigraph<?> bigraph, String name, String basePath) {
        eb(bigraph, name, basePath, true);
    }

    void eb(Bigraph<?> bigraph, String name, String basePath, boolean asTree) {
        try {
            BigraphGraphvizExporter.toPNG(bigraph, asTree, new File(basePath + name + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Supplier<String> createNameSupplier(final String prefix) {
        return new Supplier<>() {
            private int id = 0;

            @Override
            public String get() {
                return prefix + id++;
            }
        };
    }

}
