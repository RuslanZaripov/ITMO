package org.example;

import org.example.node.Node;
import org.example.node.type.KotlinType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

// (VAR | VAL) ID COLON TYPE (EQUAL VALUE | eps) SEMICOLON END
public enum Token implements Node {
    VAR("var", "VAR"),
    VAL("val", "VAL"),
    COLON(":", ":"),
    EQUAL("=", "="),
    SEMICOLON(";", ";"),
    TYPE(generateRegex(KotlinType::getName), "TYPE"),
    VALUE(generateRegex(KotlinType::getRegex), "VALUE"),
    ID("[a-zA-Z_][a-zA-Z0-9_]*", "ID"),
    EOF("$", "EOF"),
    EPS("eps", "EPS");

    private final String regex;
    private final String label;
    private int number;

    Token(String regex, String label) {
        this.regex = regex;
        this.label = label;
    }

    private static String generateRegex(Function<KotlinType, String> lambda) {
        return Arrays.stream(KotlinType.values())
                .map(lambda)
                .collect(Collectors.joining("|"));
    }

    public String getRegex() {
        return regex;
    }

    @Override
    public List<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int getCountNumber() {
        return 1;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }
}
