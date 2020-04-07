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

    public static void main(String[] args) {
        GettingStartedGuide gettingStartedGuide = new GettingStartedGuide();
        BaseDocumentationGeneratorSupport.runParser(gettingStartedGuide, "GettingStartedGuide.java");

    }
}
