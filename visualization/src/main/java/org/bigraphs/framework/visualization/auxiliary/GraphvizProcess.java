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
package org.bigraphs.framework.visualization.auxiliary;

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
