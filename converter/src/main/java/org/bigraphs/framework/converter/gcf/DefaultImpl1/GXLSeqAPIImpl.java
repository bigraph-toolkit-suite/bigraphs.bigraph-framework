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
 * An abstract class to represent the seq-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLSeqAPIImpl extends GXLUntypedStandardValueContainerAPI {

    /*
     * inherited: public abstract void setAttributeValue(String attributeName,String value);
     *            public abstract void close();
     *            public abstract Object createLocator();
     *            public abstract Object createSeq();
     *            public abstract Object createSet();
     *            public abstract Object createBag();
     *            public abstract Object createTup();
     *            public abstract Object createBool();
     *            public abstract Object createString();
     *            public abstract Object createInt();
     *            public abstract Object createFloat();
     *            public abstract Object createEnum();
     *            public abstract void closeLocator();
     *            public abstract void closeSeq();
     *            public abstract void closeSet();
     *            public abstract void closeBag();
     *            public abstract void closeTup();
     *            public abstract void closeBool();
     *            public abstract void closeString();
     *            public abstract void closeInt();
     *            public abstract void closeFloat();
     *            public abstract void closeEnum();
     */

    /** Empty constructor. */
    public GXLSeqAPIImpl() {
    }

    public void close() {
        GXLOutputAPI.writeln(">");
        int depth=GXLOutputAPI.getCurrentDepth()-1;
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write("</seq");
    }

}
