package org.example.node.type;

import org.example.Token;
import org.example.node.Node;

import java.util.List;

public record Type(KotlinType type) implements Node {
    private static int number;

    public String toString() {
        return type.getName();
    }

    @Override
    public List<Node> getChildren() {
        return List.of(Token.COLON, Token.TYPE);
    }

    @Override
    public String getLabel() {
        return "type";
    }

    @Override
    public int getCountNumber() {
        return 3;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public void setNumber(int number) {
        Type.number = number;
    }

    public KotlinType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type that = (Type) o;
        return type == that.type;
    }
}
