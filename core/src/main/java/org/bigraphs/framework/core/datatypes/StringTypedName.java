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
package org.bigraphs.framework.core.datatypes;

import java.util.Objects;
import java.util.UUID;
import org.bigraphs.framework.core.Signature;

/**
 * Default implementation for an element of a name set. An element of such a name set is represent as a string.
 * This class is used to represent the type of the control labels of a signature (see {@link Signature}).
 *
 * @author Dominik Grzelak
 */
public class StringTypedName implements NamedType<String> {
    protected String name;

    protected StringTypedName() {
    }

    public StringTypedName(String value) {
        this.name = value;
    }

    @Override
    public String getValue() {
        return name;
    }

    /**
     * Creates a name set element with label passed by {@code value} argument.
     *
     * @param value the name of the element for a name set
     * @return an element for a name set
     */
    public static StringTypedName of(String value) {
        return new StringTypedName(value);
    }

    /**
     * Creates a string-typed element for a name set with a random label.
     *
     * @return a randomly generated string-typed element
     */
    public static StringTypedName of() {
        return new StringTypedName(UUID.randomUUID().toString());
    }


    @Override
    public String stringValue() {
        return getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StringTypedName)) return false;
        StringTypedName that = (StringTypedName) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
