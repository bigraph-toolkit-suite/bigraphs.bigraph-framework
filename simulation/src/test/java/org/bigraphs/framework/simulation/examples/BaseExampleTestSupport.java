/*
 * Copyright (c) 2020-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.examples;

import java.io.File;
import java.io.IOException;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.simulation.BigraphUnitTestSupport;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;

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
