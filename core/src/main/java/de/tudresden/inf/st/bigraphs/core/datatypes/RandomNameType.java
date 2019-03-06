package de.tudresden.inf.st.bigraphs.core.datatypes;

import java.util.UUID;

public class RandomNameType implements NamedType<String> {
    private String name;

    private RandomNameType(String name) {
        this.name = name;
    }

    public static RandomNameType of() {
        return new RandomNameType(UUID.randomUUID().toString());
    }

    @Override
    public String stringValue() {
        return getValue();
    }

    @Override
    public String getValue() {
        return name;
    }
}
