package org.example.node;

import org.example.Token;

import java.util.List;

public class Modifier extends AbstractNode {
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
    public List<Node> getChildren() {
        return List.of(token);
    }

    @Override
    public String getLabel() {
        return "mod";
    }

    @Override
    public int getCountNumber() {
        return 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Modifier that = (Modifier) o;
        return token == that.token;
    }
}
