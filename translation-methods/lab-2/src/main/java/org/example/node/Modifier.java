package org.example.node;

import org.example.Token;

public class Modifier implements Node {
    private final Token token;

    public Modifier(Token token) {
        switch (token) {
            case VAR, VAL -> this.token = token;
            default -> throw new IllegalArgumentException("Expected 'var' or 'val'");
        }
    }

    @Override
    public String toString() {
        return token.getRegex();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Modifier that = (Modifier) o;
        return token == that.token;
    }
}
