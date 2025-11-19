/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.converter.gxl;

//import GCF.GXLConverterAPI;

import GCF.GXLConverterAPI;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.bigraphs.framework.converter.gcf.DefaultImpl1.GXLGXLAPIImpl;
import org.bigraphs.framework.converter.gcf.DefaultImpl1.GXLOutputAPI;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class ReadGXLUnitTest {

    private static final String TARGET_DUMP_PATH = "src/test/resources/dump/";

    private URI getResourceFileURI(String resourceName) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourceName).getFile());
        return file.toURI();
    }

    @Test
    void read() throws MalformedURLException {
        // create the GXLObject
        GXLConverterAPI.createGXL(new GXLGXLAPIImpl());
        // set the URL where the default implementation 1 is located
        GXLConverterAPI.setImplementationURL(null);
        // set the package name of the default implementation 1
        GXLConverterAPI.setPackageName("org.bigraphs.framework.converter.gcf.DefaultImpl1");
        // IMPORTANT : 1st create the GCF.GXLConnector...
        GXLConverterAPI.createConnector(GXLConverterAPI.CONNECTOR);
        // ...and then the GCF.GXLDocumentHandler !!!
        GXLConverterAPI.createDocumentHandler();

        // optionally create an outputFile, the default output
        // stream is System.out
        GXLOutputAPI.createOutputFile(TARGET_DUMP_PATH + "Demo.xml");

//        File f = new File("/home/dominik/git/BigraphFramework/visualization/gxldemo.xml");
//        Path path = Paths.get("/home/dominik/git/BigraphFramework/visualization/gxldemo.xml");
        URI resourceFilePath = getResourceFileURI("test-1.xmi");
        Path path = Paths.get("/home/dominik/git/BigraphFramework/visualization/test-1.xmi");
        path = Paths.get(resourceFilePath);
        GCF.GXLConverterAPI.parse(path.toAbsolutePath().toString());

        GXLOutputAPI.closeOutputFile();
    }
}
