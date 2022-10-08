package ru.itmo.parser.node.type;

import java.util.Arrays;

public enum KotlinType {
    INT("Int", "-?[0-9]+"),
    STRING("String", "\".*\"");

    private final String name;
    private final String regex;

    KotlinType(String name, String regex) {
        this.name = name;
        this.regex = regex;
    }

    public static KotlinType fromName(String name) {
        return Arrays.stream(KotlinType.values())
                .filter(type -> type.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown type: " + name));
    }

    public String getName() {
        return name;
    }

    public String getRegex() {
        return regex;
    }
}
