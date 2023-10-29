package org.bigraphs.framework.simulation.examples;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import org.bigraphs.framework.simulation.BigraphUnitTestSupport;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;

import java.io.File;
import java.io.IOException;

/**
 * @author Dominik Grzelak
 */
public abstract class BaseExampleTestSupport implements BigraphUnitTestSupport {

    private String basePath;
    private boolean autoCleanBefore;

    public BaseExampleTestSupport(String basePath, boolean autoCleanBefore) {
        this.basePath = basePath;
        this.autoCleanBefore = autoCleanBefore;
    }

    @Override
    public void eb(Bigraph<?> bigraph, String name, boolean asTree) {
        try {
            BigraphGraphvizExporter.toPNG(bigraph, asTree, new File(basePath + name + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
