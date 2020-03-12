package de.tudresden.inf.st.bigraphs.converter.bigred;

/**
 * @author Dominik Grzelak
 */
public interface BigRedXmlLoader {
    /**
     * Parse the a BigRed XML file: agent, reaction rule or signature
     *
     * @param file filename of a BigRed XML file
     */
    void readXml(String file);

}
