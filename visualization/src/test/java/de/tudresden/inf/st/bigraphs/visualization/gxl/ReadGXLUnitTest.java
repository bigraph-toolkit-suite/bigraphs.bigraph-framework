package de.tudresden.inf.st.bigraphs.visualization.gxl;

//import GCF.GXLConverterAPI;

import GCF.GXLConverterAPI;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Dominik Grzelak
 */
public class ReadGXLUnitTest {

    @Test
    void read() throws MalformedURLException {
        // create the GXLObject
        GXLConverterAPI.createGXL(new GCF.DefaultImpl1.GXLGXLAPIImpl());
        // set the URL where the default implementation 1 is located
        GXLConverterAPI.setImplementationURL(
                new URL("file:///home/dominik/git/BigraphFramework/visualization/src/main/GCF"));
        // set the package name of the default implementation 1
        GXLConverterAPI.setPackageName("GCF.DefaultImpl1");
        // IMPORTANT : 1st create the GCF.GXLConnector...
        GXLConverterAPI.createConnector(GXLConverterAPI.CONNECTOR);
        // ...and then the GCF.GXLDocumentHandler !!!
        GXLConverterAPI.createDocumentHandler();

        // optionally create an outputFile, the default output
        // stream is System.out
        GCF.DefaultImpl1.GXLOutputAPI.createOutputFile("Demo.xml");

//        File f = new File("/home/dominik/git/BigraphFramework/visualization/gxldemo.xml");
//        Path path = Paths.get("/home/dominik/git/BigraphFramework/visualization/gxldemo.xml");
        Path path = Paths.get("/home/dominik/git/BigraphFramework/visualization/test-1.xmi");
        GCF.GXLConverterAPI.parse(path.toAbsolutePath().toString());

        GCF.DefaultImpl1.GXLOutputAPI.closeOutputFile();
    }
}
