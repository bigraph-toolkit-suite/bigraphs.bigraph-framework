package de.tudresden.inf.st.bigraphs.documentation;

import de.tudresden.inf.st.bigraphs.documentation.basic.GettingStartedGuide;

/**
 * The main runner for all test classes that generate code samples for the user manual.
 * <p>
 * The configuration of each test class is done in the test class itself.
 *
 * @author Dominik Grzelak
 */
public class MainDocGenerationRunner {

//    public final static String BASE_EXPORT_PATH = "documentation/docusaurus/docs/assets";
    public final static String BASE_EXPORT_PATH = "documentation/v2-docusaurus/docs/assets";

    public static void main(String[] args) {
        GettingStartedGuide gettingStartedGuide = new GettingStartedGuide();
        BaseDocumentationGeneratorSupport.runParser(gettingStartedGuide, "GettingStartedGuide.java");

    }
}
