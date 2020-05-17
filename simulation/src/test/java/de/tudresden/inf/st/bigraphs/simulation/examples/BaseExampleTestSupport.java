package de.tudresden.inf.st.bigraphs.simulation.examples;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphArtifacts;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;

/**
 * @author Dominik Grzelak
 */
public abstract class BaseExampleTestSupport {

    private String basePath;
    private boolean autoCleanBefore;

    public BaseExampleTestSupport(String basePath, boolean autoCleanBefore) {
        this.basePath = basePath;
        this.autoCleanBefore = autoCleanBefore;
    }

    public void eb(Bigraph<?> bigraph, String name) {
        try {
            BigraphGraphvizExporter.toPNG(bigraph, true, new File(basePath + name + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void print(PureBigraph bigraph) {
        try {
            BigraphArtifacts.exportAsInstanceModel(bigraph, System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
