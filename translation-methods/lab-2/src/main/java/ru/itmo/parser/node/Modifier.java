package ru.itmo.parser.node;

import ru.itmo.parser.Token;

import java.util.List;

public class Modifier extends AbstractNode {
    private final Token token;

    public Modifier(final Token token) {
        this.token = switch (token) {
            case VAR, VAL -> token;
            default -> throw new IllegalArgumentException("Expected 'var' or 'val'");
        };
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
    public boolean equals(final Object o) {
        Modifier that = (Modifier) o;
        return super.equals(o) && token == that.token;
    }
}
