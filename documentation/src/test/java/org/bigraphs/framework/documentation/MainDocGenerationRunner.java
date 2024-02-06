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
