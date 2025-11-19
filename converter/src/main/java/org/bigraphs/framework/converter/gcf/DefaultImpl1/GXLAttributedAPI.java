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
 * Abstract class to provide the create-and close-methods for any GXL-construct that is
 * attributed. For further information see the GXL-DTD, inherited and the
 * class-hierarchy of the GXL-Converter-Framework.
 */
public abstract class GXLAttributedAPI extends GXLStandardAPI {

    int depth=GXLOutputAPI.getCurrentDepth()+1;

    /*
    * inherited :  public abstract void setAttributeValue(String attributeName,String value);
    *              public abstract void close();
    */

    /** Empty constructor. */
    public GXLAttributedAPI() {
    }

    /**
     * Method to create a child-element of type attr (see GXL-DTD).
     */
    public Object createAttr() {
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("<attr");
        return (Object)new GXLAttrAPIImpl();
    }

    /**
     * Method to close a child-element of type attr (see GXL-DTD).
     */
    public void closeAttr() {}

}
