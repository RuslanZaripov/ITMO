package org.example.node;

import java.util.Arrays;

public class Modifier implements Node {
    public enum Token {
        VAR("var"),
        VAL("val");

        private final String name;

        Token(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private final Token token;

    public Modifier(String text) {
        this.token = Arrays.stream(Token.values())
                .filter(token -> token.name.equals(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown token: " + text));
    }

    @Override
    public String toString() {
        return token.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Modifier modifier = (Modifier) o;
        return token == modifier.token;
    }
}
