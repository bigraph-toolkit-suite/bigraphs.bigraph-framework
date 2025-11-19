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
 * An abstract class to represent the gxl-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLGXLAPIImpl extends GXLStandardAPI {

    /*
    * inherited :  public abstract void setAttributeValue(String attributeName,String value);
    *              public abstract void close();
    */

    /** Empty constructor. */
    public GXLGXLAPIImpl() {
    }

    /**
     * Method to create the DOCTYPE declaration of the GXL-document..
     */
    public void createDoctypeDecl(String name, String pubid, String sysid) {
        GXLOutputAPI.writeln("<!DOCTYPE " + name +((pubid==""||pubid==null) ? " SYSTEM \""+sysid : " PUBLIC \""+
                             pubid+"\" \""+sysid)+"\">");
    }

    /**
     * Method to create a Processing Instruction in the GXL-document..
     */
    public void createProcessingInstruction(String target, String data) {
        GXLOutputAPI.writeln("<?"+target+" "+data+"?>");
    }

    /**
     * Method to create a the GXL-Node (see GXL-DTD).
     */
    public void createGXL() {
        GXLOutputAPI.write("<gxl");
    }

    /**
     * Method to create a child-element of type graph (see GXL-DTD).
     */
    public Object createGraph() {
        GXLOutputAPI.writeln (">");
        GXLOutputAPI.write("  <graph");
        return (Object)new GXLGraphAPIImpl();
    }

    /**
     * Method to close a child-element of type graph (see GXL-DTD).
     */
    public void closeGraph() {}

    public void close() {
        GXLOutputAPI.writeln(">");
        GXLOutputAPI.writeln("</gxl>");
    }
}
