package org.bigraphs.framework.simulation;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;

import java.io.File;
import java.io.IOException;

public interface BigraphUnitTestSupport {

    default void eb(Bigraph<?> bigraph, String name) {
        eb(bigraph, name, true);
    }

    default void eb(Bigraph<?> bigraph, String name, boolean asTree) {
        try {
            BigraphGraphvizExporter.toPNG(bigraph, asTree, new File(name + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    default void print(PureBigraph bigraph) {
        try {
            BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    default void printMetaModel(PureBigraph bigraph) {
        try {
            BigraphFileModelManagement.Store.exportAsMetaModel(bigraph, System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
