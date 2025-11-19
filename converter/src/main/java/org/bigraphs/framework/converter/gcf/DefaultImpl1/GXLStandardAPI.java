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
 * Abstract class to provide the setAttributeValue() and close()-method for any
 * GXL-construct except the atomic Values. It is the superior class of the GXL-
 * Converter-Framework. For further information see the GXL-DTD and the class-hierarchy
 * of the GXL-Converter-Framework.
 */
public abstract class GXLStandardAPI extends Object {

    /** Empty constructor. */
    public GXLStandardAPI() {
    }

    /**
     * Method to set an attribute-value (see GXL-DTD).
     */
    public void setAttributeValue(String attributeName,String value){
        GXLOutputAPI.write (" "+attributeName+"=\""+value+"\"");
    }

    /**
     * Method to close the current GXL-construct.
     */
    public void close() {
        GXLOutputAPI.write ("/");
    }

    /**
     * Method to get the list of child elements.
     */
    public Object getChildElements() {
	return null;
    }

    /**
     * Method to return the list of attributes.
     */
    public Object getAttributes () {
        return null;
    }

}
