/*
 * Copyright (c) 2019-2024 Bigraph Toolkit Suite Developers
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
 * An abstract class to represent the node-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLNodeAPIImpl extends GXLGraphContainerAPI {

    /*
    * inherited:public abstract void setAttributeValue(String attributeName,String value);
    *           public abstract void close();
    *           public abstract Object createType();
    *           public abstract Object createAttr();
    *           public abstract Object createGraph();
    */

    /** Empty constructor. */
    public GXLNodeAPIImpl(){
    }

    public void close() {
        GXLOutputAPI.writeln(">");
        int depth=GXLOutputAPI.getCurrentDepth()-1;
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write("</node");
    }
}
