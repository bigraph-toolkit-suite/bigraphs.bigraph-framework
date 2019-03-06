package de.tudresden.inf.st.bigraphs.core.datatypes;

import java.util.Objects;

public class StringTypedName implements NamedType<String> {
    protected String name;

    @Override
    public String getValue() {
        return name;
    }

    public static StringTypedName of(String value) {
        return new StringTypedName(value);
    }

    private StringTypedName(String value) {
        this.name = value;
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
