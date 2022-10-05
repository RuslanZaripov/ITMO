package org.example.node;

import java.util.Arrays;

public class Value implements Node {
    public enum Token {
        INT("-?[0-9]+"),
        STRING("\".*\"");

        private final String regex;

        Token(String regex) {
            this.regex = regex;
        }
    }

    private final Token token;

    private final String text;

    public Value(String value) {
        this.text = value;
        this.token = Arrays.stream(Token.values())
                .filter(token -> value.matches(token.regex))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown token: " + value));
    }

    public String getType() {
        return token.name();
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        return token == value.token && text.equals(value.text);
    }
}
