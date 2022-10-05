package org.example.node;

import java.util.Arrays;

public class Type implements Node {
    public enum Token {
        INT("Int"),
        STRING("String");

        private final String name;

        Token(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private final Token token;

    public Type(String text) {
        this.token = Arrays.stream(Token.values())
                .filter(token -> token.name.equals(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown token: " + text));
    }

    public String getType() {
        return token.name();
    }

    @Override
    public String toString() {
        return token.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return token == type.token;
    }
}
