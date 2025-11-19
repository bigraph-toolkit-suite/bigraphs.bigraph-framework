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
package org.bigraphs.framework.converter.gcf.DefaultImpl1;
/**
 * An abstract class to represent the rel-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLRelAPIImpl extends GXLGraphContainerAPI {

    int depth=GXLOutputAPI.getCurrentDepth()+1;

    /*
    * inherited:public abstract void setAttributeValue(String attributeName,String value);
    *           public abstract void close();
    *           public abstract Object createType();
    *           public abstract void closeType();
    *           public abstract Object createAttr();
    *           public abstract void closeAttr();
    *           public abstract Object createGraph();
    *           public abstract void closeGraph();
    */

    /** Empty constructor. */
    public GXLRelAPIImpl() {
    }

    /**
     * Method to create a child-element of type relend (see GXL-DTD).
     */
    public Object createRelend() {
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write("<relend");
        return (Object)new GXLRelendAPIImpl();
    }

    /**
     * Method to close a child-element of type relend (see GXL-DTD).
     */
    public void closeRelend() {}

    public void close() {
        GXLOutputAPI.writeln (">");
        for (int i=2; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("</rel");
    }
}
