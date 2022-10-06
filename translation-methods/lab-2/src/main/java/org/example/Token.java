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
    private int number;

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

    @Override
    public List<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String getLabel() {
        return name();
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
