package org.bigraphs.framework.visualization.auxiliary;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Converts a Graphviz DOT file to a PNG file by calling the dot process directly.
 *
 * @author Dominik Grzelak
 */
public class GraphvizProcess {

    public Process convert(String dotContent, String destFilePrefix) throws Exception {
        if (destFilePrefix == null || destFilePrefix.isEmpty()) {
            destFilePrefix = "output";
        }
        Path tempFilePath = writeToFile(dotContent);
        String command = prepareCommand(tempFilePath.toAbsolutePath().toString(), destFilePrefix);
        return executeCommand(command);
    }

    protected Process executeCommand(String command) throws Exception {
        return Runtime.getRuntime().exec(command);
    }

    protected Path writeToFile(String content) throws IOException {
        Path tempFilePath = Files.createTempFile(null, ".dot");
        Files.write(tempFilePath, content.getBytes(), StandardOpenOption.CREATE);
//        Files.deleteIfExists(tempFilePath);
        return tempFilePath;
    }

    protected String prepareCommand(String filePrefix, String destFilePrefix) {
        if (destFilePrefix == null || destFilePrefix.isEmpty()) destFilePrefix = filePrefix;
        return new StringBuilder("dot -Tpng ") // output type
                .append(filePrefix)   // input dot file
                .append(" -o ").append(destFilePrefix)  // output image
                .toString();
    }
}
