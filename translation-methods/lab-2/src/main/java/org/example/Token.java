package org.example;

import org.example.node.type.KotlinType;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

// (VAR | VAL) ID COLON TYPE (EQUAL VALUE | eps) SEMICOLON END
public enum Token {
    VAR("var"),
    VAL("val"),
    COLON(":"),
    EQUAL("="),
    SEMICOLON(";"),
    TYPE(generateRegex(KotlinType::getName)),
    VALUE(generateRegex(KotlinType::getRegex)),
    ID("[a-zA-Z_][a-zA-Z0-9_]*"),
    EOF("$");

    private final String regex;

    Token(String regex) {
        this.regex = regex;
    }

    private static String generateRegex(Function<KotlinType, String> lambda) {
        return Arrays.stream(KotlinType.values())
                .map(lambda)
                .collect(Collectors.joining("|"));
    }

    public String getRegex() {
        return regex;
    }
}
