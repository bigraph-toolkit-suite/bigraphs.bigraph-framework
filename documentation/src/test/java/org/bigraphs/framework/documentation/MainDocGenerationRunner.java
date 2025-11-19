/*
 * Copyright (c) 2020-2024 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.documentation;

import org.bigraphs.framework.documentation.basic.GettingStartedGuide;
import org.bigraphs.framework.documentation.converter.ConverterBigrapher;
import org.bigraphs.framework.documentation.persistence.PersistingBigraphs;

/**
 * The main runner for all test classes that generate code samples for the user manual.
 * <p>
 * The configuration of each test class is done in the test class itself.
 *
 * @author Dominik Grzelak
 */
public class MainDocGenerationRunner {

    public final static String BASE_EXPORT_PATH = "documentation/v2-docusaurus/docs/assets";

    public static void main(String[] args) {
        GettingStartedGuide gettingStartedGuide = new GettingStartedGuide();
        BaseDocumentationGeneratorSupport.runParser(gettingStartedGuide, "GettingStartedGuide.java");

        PersistingBigraphs persistingBigraphs = new PersistingBigraphs();
        BaseDocumentationGeneratorSupport.runParser(persistingBigraphs, "PersistingBigraphs.java");

        ConverterBigrapher converterBigrapher = new ConverterBigrapher();
        BaseDocumentationGeneratorSupport.runParser(converterBigrapher, "ConverterBigrapher.java");
    }
}
