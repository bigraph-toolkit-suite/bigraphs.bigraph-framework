package de.tudresden.inf.st.bigraphs.core.datatypes;

import java.util.Objects;
import java.util.UUID;

/**
 * Default implementation for an element of a name set. An element of such a name set is represent as a string.
 * This class is used to represent the type of the control labels of a signature (see {@link de.tudresden.inf.st.bigraphs.core.Signature}).
 *
 * @author Dominik Grzelak
 */
public class StringTypedName implements NamedType<String> {
    protected String name;

    protected StringTypedName() {
    }

    protected StringTypedName(String value) {
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
