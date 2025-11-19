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
 * Interface to define the create-and close-methods and the printData-method for
 * the standard values.
 */
public interface GXLStandardValueMethods {

    public Object createLocator();
    public Object createSeq();
    public Object createSet();
    public Object createBag();
    public Object createTup();
    public Object createBool();
    public Object createString();
    public Object createInt();
    public Object createFloat();
    public Object createEnum();

    public void closeLocator();
    public void closeSeq();
    public void closeSet();
    public void closeBag();
    public void closeTup();
    public void closeBool();
    public void closeString();
    public void closeInt();
    public void closeFloat();
    public void closeEnum();
}
