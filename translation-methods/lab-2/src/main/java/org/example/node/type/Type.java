package org.example.node.type;

import org.example.Token;
import org.example.node.AbstractNode;
import org.example.node.Node;

import java.util.List;

public class Type extends AbstractNode {
    private final KotlinType type;

    public Type(KotlinType type) {
        this.type = type;
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

    public KotlinType getType() {
        return type;
    }

    public String toString() {
        return ": " + type.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type that = (Type) o;
        return type == that.type;
    }
}
