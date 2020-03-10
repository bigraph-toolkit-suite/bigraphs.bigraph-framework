package de.tudresden.inf.st.bigraphs.converter.bigred;

import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import org.junit.jupiter.api.Test;

import java.net.URL;

/**
 * @author Dominik Grzelak
 */
public class ReadTestBigRed {

    @Test
    void read_test() {
        URL resource = getClass().getResource("/bigreg1.bigraph-signature");
        SignatureXMLLoader loader = new SignatureXMLLoader();
        loader.readConfig(resource.getPath());
        DefaultDynamicSignature signature2 = loader.importObject();
    }
}
